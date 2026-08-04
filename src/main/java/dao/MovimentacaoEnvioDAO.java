package dao;

import conexao.Conexao;
import model.MovimentacaoEnvio;
import java.sql.*;
import java.util.List;

public class MovimentacaoEnvioDAO {

	public Long inserir(MovimentacaoEnvio envio, List<Long> idsEquipamentos) throws SQLException {
        // 1. Buscar os códigos reais (origem_codigo) das filiais a partir dos IDs de tela
        String sqlBuscaCodigoFilial = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
        
        String sqlVerificaEmTransito = "SELECT id_sistema FROM equipamentos " +
                                       "WHERE id_equipamento = ? AND situacao_id = 3";

        String sqlEnvio = "INSERT INTO movimentacao_envio (data_envio, origem_id, destino_id, responsavel, transportadora, codigo_rastreio, data_previsa_entrega, observacoes, status_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_envio";
        
        String sqlItem = "INSERT INTO movimentacao_envio_itens (id_envio, id_equipamento) VALUES (?, ?)";
        
        // Atualiza a situação para 3 (Em Trânsito)
        String sqlUpdateEquipamento = "UPDATE equipamentos SET situacao_id = 3 WHERE id_equipamento = ?";

        Connection conn = null;
        PreparedStatement stmtVerifica = null;
        PreparedStatement stmtEnvio = null;
        PreparedStatement stmtItem = null;
        PreparedStatement stmtUpdate = null;
        ResultSet rs = null;
        Long idEnvioGerado = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // Validação de equipamentos em trânsito
            stmtVerifica = conn.prepareStatement(sqlVerificaEmTransito);
            for (Long idEquipamento : idsEquipamentos) {
                stmtVerifica.setLong(1, idEquipamento);
                try (ResultSet rsVerifica = stmtVerifica.executeQuery()) {
                    if (rsVerifica.next()) {
                        String idSistemaEncontrado = rsVerifica.getString("id_sistema");
                        throw new SQLException("Atenção: O equipamento com o ID de Sistema '" + idSistemaEncontrado + "' já está em trânsito!");
                    }
                }
            }

            stmtEnvio = conn.prepareStatement(sqlEnvio);
            stmtEnvio.setDate(1, Date.valueOf(envio.getDataEnvio()));
            stmtEnvio.setLong(2, envio.getOrigemId());
            stmtEnvio.setLong(3, envio.getDestinoId());
            stmtEnvio.setString(4, envio.getResponsavel());
            stmtEnvio.setString(5, envio.getTransportadora());
            stmtEnvio.setString(6, envio.getCodigoRastreio());
            if (envio.getDataPrevisaoEntrega() != null) {
                stmtEnvio.setDate(7, Date.valueOf(envio.getDataPrevisaoEntrega()));
            } else {
                stmtEnvio.setNull(7, Types.DATE);
            }
            stmtEnvio.setString(8, envio.getObservacoes());
            stmtEnvio.setLong(9, envio.getStatusId() != null ? envio.getStatusId() : 2L);

            rs = stmtEnvio.executeQuery();
            if (rs.next()) {
                idEnvioGerado = rs.getLong(1);
            }

            stmtItem = conn.prepareStatement(sqlItem);
            stmtUpdate = conn.prepareStatement(sqlUpdateEquipamento);

            for (Long idEquipamento : idsEquipamentos) {
                stmtItem.setLong(1, idEnvioGerado);
                stmtItem.setLong(2, idEquipamento);
                stmtItem.addBatch();

                stmtUpdate.setLong(1, idEquipamento);
                stmtUpdate.addBatch();
            }

            stmtItem.executeBatch();
            stmtUpdate.executeBatch();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            // Fechamentos de recursos...
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmtVerifica != null) stmtVerifica.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmtEnvio != null) stmtEnvio.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmtItem != null) stmtItem.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmtUpdate != null) stmtUpdate.close(); } catch (SQLException e) { e.printStackTrace(); }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        return idEnvioGerado;
    }
    
	public List<MovimentacaoEnvio> listarTodos() throws SQLException {
        // Sem o WHERE e.status_id = 2, ele traz todo o histórico para a tela
        String sql = "SELECT e.*, " +
                     "orig.nome_empresa AS nome_origem, " +
                     "dest.nome_empresa AS nome_destino, " +
                     "ms.nome AS status_nome, ms.cor AS status_cor " +
                     "FROM movimentacao_envio e " +
                     "LEFT JOIN filiais orig ON e.origem_id = orig.id_filial " +
                     "LEFT JOIN filiais dest ON e.destino_id = dest.id_filial " +
                     "LEFT JOIN movimentacao_status ms ON e.status_id = ms.id " +
                     "ORDER BY e.data_envio DESC, e.id_envio DESC";

        List<MovimentacaoEnvio> lista = new java.util.ArrayList<>();

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MovimentacaoEnvio env = new MovimentacaoEnvio();
                env.setIdEnvio(rs.getLong("id_envio"));
                env.setDataEnvio(rs.getDate("data_envio").toLocalDate());
                env.setOrigemId(rs.getLong("origem_id"));
                env.setDestinoId(rs.getLong("destino_id"));
                env.setNomeOrigem(rs.getString("nome_origem"));
                env.setNomeDestino(rs.getString("nome_destino"));
                env.setResponsavel(rs.getString("responsavel"));
                env.setTransportadora(rs.getString("transportadora"));
                env.setCodigoRastreio(rs.getString("codigo_rastreio"));
                
                if (rs.getDate("data_previsa_entrega") != null) {
                    env.setDataPrevisaoEntrega(rs.getDate("data_previsa_entrega").toLocalDate());
                }
                env.setObservacoes(rs.getString("observacoes"));
                env.setStatusId(rs.getLong("status_id"));
                env.setStatusNome(rs.getString("status_nome"));
                env.setStatusCor(rs.getString("status_cor"));
                
                lista.add(env);
            }
        }
        return lista;
    }

    public void confirmarRecebimento(Long idEnvio, Long destinoId) throws SQLException {
        // Busca o origem_codigo real da filial de destino a partir do id_filial (destinoId)
        String sqlBuscaCodigoFilial = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
        String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";
        
        // Atualiza o status do envio para 3 (Recebido)
        String sqlAtualizaEnvioStatus = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ?";
        
        // Atualiza o equipamento com o código de origem correto da filial
        String sqlAtualizaEquipamento = "UPDATE equipamentos SET situacao_id = 1, origem_codigo = ? WHERE id_equipamento = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            Long origemCodigoReal = null;
            try (PreparedStatement stmtFilial = conn.prepareStatement(sqlBuscaCodigoFilial)) {
                stmtFilial.setLong(1, destinoId);
                try (ResultSet rs = stmtFilial.executeQuery()) {
                    if (rs.next()) {
                        origemCodigoReal = rs.getLong("origem_codigo");
                    } else {
                        throw new SQLException("Filial de destino não encontrada.");
                    }
                }
            }

            try (PreparedStatement stmtUpEnvio = conn.prepareStatement(sqlAtualizaEnvioStatus)) {
                stmtUpEnvio.setLong(1, idEnvio);
                stmtUpEnvio.executeUpdate();
            }

            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens);
                 PreparedStatement stmtUpEq = conn.prepareStatement(sqlAtualizaEquipamento)) {
                
                stmtBusca.setLong(1, idEnvio);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    while (rs.next()) {
                        Long idEquipamento = rs.getLong("id_equipamento");
                        stmtUpEq.setLong(1, origemCodigoReal); // Passa o código correto da filial
                        stmtUpEq.setLong(2, idEquipamento);
                        stmtUpEq.addBatch();
                    }
                    stmtUpEq.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public void cancelarEnvio(Long idEnvio) throws SQLException {
        String sqlBuscaEnvio = "SELECT origem_id FROM movimentacao_envio WHERE id_envio = ?";
        // Busca o origem_codigo real da filial de origem original
        String sqlBuscaCodigoFilial = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
        String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";
        
        // Retorna a situação do equipamento para 1 (Disponível) usando o código real da filial
        String sqlVoltaEquip = "UPDATE equipamentos SET situacao_id = 1, origem_codigo = ? WHERE id_equipamento = ?";
        
        // Atualiza o status do envio para 4 (Cancelado)
        String sqlCancelaEnvioStatus = "UPDATE movimentacao_envio SET status_id = 4 WHERE id_envio = ?";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            Long origemIdOriginal = null;
            try (PreparedStatement stmtOrigem = conn.prepareStatement(sqlBuscaEnvio)) {
                stmtOrigem.setLong(1, idEnvio);
                try (ResultSet rs = stmtOrigem.executeQuery()) {
                    if (rs.next()) {
                        origemIdOriginal = rs.getLong("origem_id");
                    } else {
                        throw new SQLException("Envio não encontrado.");
                    }
                }
            }

            Long origemCodigoReal = null;
            try (PreparedStatement stmtFilial = conn.prepareStatement(sqlBuscaCodigoFilial)) {
                stmtFilial.setLong(1, origemIdOriginal);
                try (ResultSet rs = stmtFilial.executeQuery()) {
                    if (rs.next()) {
                        origemCodigoReal = rs.getLong("origem_codigo");
                    } else {
                        throw new SQLException("Filial de origem não encontrada.");
                    }
                }
            }

            try (PreparedStatement stmtBuscaItens = conn.prepareStatement(sqlBuscaItens);
                 PreparedStatement stmtVolta = conn.prepareStatement(sqlVoltaEquip)) {
                
                stmtBuscaItens.setLong(1, idEnvio);
                try (ResultSet rs = stmtBuscaItens.executeQuery()) {
                    while (rs.next()) {
                        Long idEquipamento = rs.getLong("id_equipamento");
                        stmtVolta.setLong(1, origemCodigoReal); // Passa o código correto da filial
                        stmtVolta.setLong(2, idEquipamento);
                        stmtVolta.addBatch();
                    }
                    stmtVolta.executeBatch();
                }
            }

            try (PreparedStatement stmtCancela = conn.prepareStatement(sqlCancelaEnvioStatus)) {
                stmtCancela.setLong(1, idEnvio);
                stmtCancela.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }
}