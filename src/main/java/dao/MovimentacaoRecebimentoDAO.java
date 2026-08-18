package dao;

import conexao.Conexao;
import java.sql.*;
import java.util.*;

public class MovimentacaoRecebimentoDAO {

    // 1. Lista os envios que estão em trânsito (excluindo devoluções)
	public List<Map<String, Object>> listarEnviosEmTransito() {
	    List<Map<String, Object>> lista = new ArrayList<>();
	    // Alterado de INNER JOIN para LEFT JOIN para evitar que o envio suma caso o ID da filial varie
	    String sql = "SELECT e.id_envio, e.codigo_rastreio, COALESCE(f.nome_empresa, 'Filial Destino #' || e.destino_id) as destino_nome " +
	                 "FROM movimentacao_envio e " +
	                 "LEFT JOIN filiais f ON e.destino_id = f.id_filial " +
	                 "WHERE e.status_id = 2 AND (e.codigo_rastreio NOT LIKE 'DEV-%' OR e.codigo_rastreio IS NULL) " +
	                 "ORDER BY e.id_envio DESC";

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

    // 3. Registra o recebimento de Envio (Transação segura)
    public boolean registrarRecebimento(int idEnvio, String dataRecebimento, String responsavel, String condicaoGeral) {
        String sqlRecebimento = "INSERT INTO movimentacao_recebimento (id_envio, data_recebimento, responsavel_recebimento, condicao_geral) VALUES (?, ?, ?, ?)";
        String sqlAtualizaEnvio = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ?";
        String sqlHistorico = "INSERT INTO movimentacao_historico (id_envio, status_id, data_hora, observacao) VALUES (?, 3, NOW(), ?)";
        
        // --> ALTERE AQUI: Faça o JOIN com a tabela filiais para buscar o 'origem_codigo' correto da filial de destino
        String sqlBuscaDestino = "SELECT f.origem_codigo FROM movimentacao_envio e " +
                                 "JOIN filiais f ON e.destino_id = f.id_filial " +
                                 "WHERE e.id_envio = ?";
                                 
        String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";
        String sqlAtualizaEquip = "UPDATE equipamentos SET origem_codigo = ?, situacao_id = 7 WHERE id_equipamento = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // 1. Pega o origem_codigo real da filial de destino
            int origemCodigoDestino = 0;
            try (PreparedStatement stmtDestino = conn.prepareStatement(sqlBuscaDestino)) {
                stmtDestino.setInt(1, idEnvio);
                try (ResultSet rsDestino = stmtDestino.executeQuery()) {
                    if (rsDestino.next()) {
                        origemCodigoDestino = rsDestino.getInt("origem_codigo");
                    }
                }
            }

            // 2. Insere o recebimento
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecebimento)) {
                stmt.setInt(1, idEnvio);
                stmt.setDate(2, java.sql.Date.valueOf(dataRecebimento));
                stmt.setString(3, responsavel);
                stmt.setString(4, condicaoGeral);
                stmt.executeUpdate();
            }

            // 3. Atualiza o status do envio para Recebido (3)
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaEnvio)) {
                stmt.setInt(1, idEnvio);
                stmt.executeUpdate();
            }

            // 4. Insere no histórico
            try (PreparedStatement stmt = conn.prepareStatement(sqlHistorico)) {
                stmt.setInt(1, idEnvio);
                stmt.setString(2, "Recebido na filial por " + responsavel + ". Condição: " + condicaoGeral);
                stmt.executeUpdate();
            }

            // 5. Atualiza o equipamento com o origem_codigo correto da filial
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens);
                 PreparedStatement stmtEq = conn.prepareStatement(sqlAtualizaEquip)) {
                
                stmtBusca.setInt(1, idEnvio);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    while (rs.next()) {
                        int idEq = rs.getInt("id_equipamento");
                        stmtEq.setInt(1, origemCodigoDestino); // Joga o código correto (ex: 101, 161)
                        stmtEq.setInt(2, idEq);
                        stmtEq.executeUpdate();
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

    // ==========================================
    // MÉTODOS PARA DEVOLUÇÕES
    // ==========================================

    // 4. Lista as devoluções em trânsito
    public List<Map<String, Object>> listarDevolucoesEmTransito() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT e.id_envio, e.codigo_rastreio, f.nome_empresa as origem_nome " +
                     "FROM movimentacao_envio e " +
                     "JOIN filiais f ON e.origem_id = f.id_filial " +
                     "WHERE e.status_id = 2 AND (e.codigo_rastreio LIKE 'DEV-%' OR e.observacoes ILIKE '%devolução%') " +
                     "ORDER BY e.id_envio DESC";

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
                map.put("origemNome", rs.getString("origem_nome"));
                lista.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return lista;
    }

    // 5. Busca os detalhes de uma devolução específica e seus itens
    public Map<String, Object> buscarDetalhesDevolucao(int idDevolucao) {
        Map<String, Object> resultado = new HashMap<>();
        String sqlDev = "SELECT e.*, f.nome_empresa as origem_nome FROM movimentacao_envio e " +
                        "JOIN filiais f ON e.origem_id = f.id_filial WHERE e.id_envio = ?";
        
        Connection conn = null;
        PreparedStatement stmtDev = null;
        ResultSet rsDev = null;

        try {
            conn = Conexao.conectar();
            stmtDev = conn.prepareStatement(sqlDev);
            stmtDev.setInt(1, idDevolucao);
            rsDev = stmtDev.executeQuery();

            if (rsDev.next()) {
                resultado.put("idEnvio", rsDev.getInt("id_envio"));
                resultado.put("origemNome", rsDev.getString("origem_nome"));
                resultado.put("transportadora", rsDev.getString("transportadora"));
                resultado.put("codigoRastreio", rsDev.getString("codigo_rastreio"));
            }
            Conexao.fechar(rsDev, stmtDev, null);

            List<Map<String, Object>> itens = new ArrayList<>();
            String sqlItensDev = "SELECT eei.id_equipamento, eq.id_equipamento as eq_id, eq.patrimonio, eq.nome_identificador, eq.numero_serie " +
                                 "FROM movimentacao_envio_itens eei " +
                                 "JOIN equipamentos eq ON eei.id_equipamento = eq.id_equipamento " +
                                 "WHERE eei.id_envio = ?";
            
            PreparedStatement stmtItens = conn.prepareStatement(sqlItensDev);
            stmtItens.setInt(1, idDevolucao);
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

 // 6. Registra o recebimento de Devolução (Com correção no resgate do código da filial)
    public boolean registrarRecebimentoDevolucao(int idDevolucao, String dataRecebimento, String responsavel, String condicaoGeral) {
        String sqlRecebimento = "INSERT INTO movimentacao_recebimento (id_envio, data_recebimento, responsavel_recebimento, condicao_geral) VALUES (?, ?, ?, ?)";
        String sqlAtualizaDev = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ?";
        
        // CORRIGIDO: Faz o JOIN com filiais para buscar o 'origem_codigo' real, evitando inconsistência
        String sqlBuscaDestino = "SELECT f.origem_codigo FROM movimentacao_envio e " +
                                 "JOIN filiais f ON e.destino_id = f.id_filial " +
                                 "WHERE e.id_envio = ?";
                                 
        String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";
        String sqlAtualizaEquip = "UPDATE equipamentos SET situacao_id = 1, origem_codigo = ? WHERE id_equipamento = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // A. Busca o origem_codigo real da filial de destino onde a devolução está sendo recebida
            int origemCodigoDestino = 0;
            try (PreparedStatement stmtDestino = conn.prepareStatement(sqlBuscaDestino)) {
                stmtDestino.setInt(1, idDevolucao);
                try (ResultSet rsDestino = stmtDestino.executeQuery()) {
                    if (rsDestino.next()) {
                        origemCodigoDestino = rsDestino.getInt("origem_codigo");
                    }
                }
            }

            // B. Insere o recebimento da devolução
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecebimento)) {
                stmt.setInt(1, idDevolucao);
                stmt.setDate(2, java.sql.Date.valueOf(dataRecebimento));
                stmt.setString(3, responsavel);
                stmt.setString(4, condicaoGeral);
                stmt.executeUpdate();
            }

            // C. Atualiza o status da devolução para Recebido (3)
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaDev)) {
                stmt.setInt(1, idDevolucao);
                stmt.executeUpdate();
            }

            // D. Atualiza os equipamentos de volta para Disponível (1) e aplica o origem_codigo correto
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens);
                 PreparedStatement stmtEq = conn.prepareStatement(sqlAtualizaEquip)) {
                
                stmtBusca.setInt(1, idDevolucao);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    while (rs.next()) {
                        int idEq = rs.getInt("id_equipamento");
                        stmtEq.setInt(1, origemCodigoDestino); // Joga o código correto da filial de destino
                        stmtEq.setInt(2, idEq);
                        stmtEq.executeUpdate();
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