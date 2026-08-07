package dao;

import conexao.Conexao;
import java.sql.*;
import java.util.*;

public class MovimentacaoRecebimentoDAO {

    // 1. Lista os envios que estão em trânsito (status_id = 2) para o <select>
    public List<Map<String, Object>> listarEnviosEmTransito() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT e.id_envio, e.codigo_rastreio, f.nome_empresa as destino_nome " +
                     "FROM movimentacao_envio e " +
                     "JOIN filiais f ON e.destino_id = f.id_filial " +
                     "WHERE e.status_id = 2 ORDER BY e.id_envio DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("idEnvio", rs.getInt("id_envio"));
                map.put("codigoRastreio", rs.getString("codigo_rastreio"));
                map.put("destinoNome", rs.getString("destino_nome"));
                lista.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return lista;
    }

    // 2. Busca os detalhes de um envio específico e seus itens/equipamentos
    public Map<String, Object> buscarDetalhesEnvio(int idEnvio) {
        Map<String, Object> resultado = new HashMap<>();
        String sqlEnvio = "SELECT e.*, f.nome_empresa as origem_nome FROM movimentacao_envio e " +
                          "JOIN filiais f ON e.origem_id = f.id_filial WHERE e.id_envio = ?";
        
        Connection conn = null;
        PreparedStatement stmtEnvio = null;
        ResultSet rsEnvio = null;

        try {
            conn = Conexao.conectar();
            stmtEnvio = conn.prepareStatement(sqlEnvio);
            stmtEnvio.setInt(1, idEnvio);
            rsEnvio = stmtEnvio.executeQuery();

            if (rsEnvio.next()) {
                resultado.put("idEnvio", rsEnvio.getInt("id_envio"));
                resultado.put("origemNome", rsEnvio.getString("origem_nome"));
                resultado.put("transportadora", rsEnvio.getString("transportadora"));
                resultado.put("codigoRastreio", rsEnvio.getString("codigo_rastreio"));
            }
            Conexao.fechar(rsEnvio, stmtEnvio, null);

            // Pega os itens do envio usando id_equipamento corretamente
            List<Map<String, Object>> itens = new ArrayList<>();
            String sqlItensReal = "SELECT eei.id_equipamento, eq.id_equipamento as eq_id, eq.patrimonio, eq.nome_identificador, eq.numero_serie " +
                                  "FROM movimentacao_envio_itens eei " +
                                  "JOIN equipamentos eq ON eei.id_equipamento = eq.id_equipamento " +
                                  "WHERE eei.id_envio = ?";
            
            PreparedStatement stmtItens = conn.prepareStatement(sqlItensReal);
            stmtItens.setInt(1, idEnvio);
            ResultSet rsItens = stmtItens.executeQuery();

            while (rsItens.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("idSistema", "EQ" + String.format("%07d", rsItens.getInt("id_equipamento")));
                item.put("patrimonio", rsItens.getString("patrimonio"));
                item.put("nomeCpu", rsItens.getString("nome_identificador"));
                item.put("produto", "Equipamento");
                item.put("numeroSerie", rsItens.getString("numero_serie"));
                itens.add(item);
            }
            Conexao.fechar(rsItens, stmtItens, null);

            resultado.put("itens", itens);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conexao.fechar(null, null, conn);
        }
        return resultado;
    }

    // 3. Registra o recebimento (Transação segura)
    public boolean registrarRecebimento(int idEnvio, String dataRecebimento, String responsavel, String condicaoGeral) {
        String sqlRecebimento = "INSERT INTO movimentacao_recebimento (id_envio, data_recebimento, responsavel_recebimento, condicao_geral) VALUES (?, ?, ?, ?)";
        String sqlAtualizaEnvio = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ?";
        String sqlHistorico = "INSERT INTO movimentacao_historico (id_envio, status_id, data_hora, observacao) VALUES (?, 3, NOW(), ?)";
        String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // 1. Insere o registro de recebimento
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecebimento)) {
                stmt.setInt(1, idEnvio);
                stmt.setDate(2, java.sql.Date.valueOf(dataRecebimento));
                stmt.setString(3, responsavel);
                stmt.setString(4, condicaoGeral);
                stmt.executeUpdate();
            }

            // 2. Atualiza o status do envio para 3 (Recebido)
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaEnvio)) {
                stmt.setInt(1, idEnvio);
                stmt.executeUpdate();
            }

            // 3. Registra no histórico
            try (PreparedStatement stmt = conn.prepareStatement(sqlHistorico)) {
                stmt.setInt(1, idEnvio);
                stmt.setString(2, "Recebido na filial por " + responsavel + ". Condição: " + condicaoGeral);
                stmt.executeUpdate();
            }

            // 4. Atualiza a situação dos equipamentos vinculados para 7 (Baixado) concluindo o trânsito e liberando para edição
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens)) {
                stmtBusca.setInt(1, idEnvio);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    while (rs.next()) {
                        int idEq = rs.getInt("id_equipamento");
                        // Altera a situação do equipamento para 7 (Baixado)
                        String sqlAtualizaEquip = "UPDATE equipamentos SET situacao_id = 7 WHERE id_equipamento = ?";
                        try (PreparedStatement stmtEq = conn.prepareStatement(sqlAtualizaEquip)) {
                            stmtEq.setInt(1, idEq);
                            stmtEq.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}