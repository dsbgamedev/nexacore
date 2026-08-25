package dao;

import conexao.Conexao;
import model.MovimentacaoEnvio;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovimentacaoEnvioDAO {

	public Long inserir(MovimentacaoEnvio envio, List<Long> idsEquipamentos) throws SQLException {
	    String sqlEnvio = "INSERT INTO movimentacao_envio (data_envio, origem_id, destino_id, responsavel, transportadora, codigo_rastreio, data_previsa_entrega, observacoes, status_id, numero_nota) " +
	                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_envio";
	    
	    // Busca a descrição da situação atual do equipamento
	    String sqlBuscaSituacao = "SELECT s.nome FROM equipamentos e JOIN situacao_equipamento s ON e.situacao_id = s.id WHERE e.id_equipamento = ?";
	    
	    // Inclui a coluna status_equipamento_momento no insert
	    String sqlItem = "INSERT INTO movimentacao_envio_itens (id_envio, id_equipamento, status_equipamento_momento) VALUES (?, ?, ?)";

	    // Atualiza a situação do equipamento para 9 (Aguardando Envio)
	    String sqlAtualizaEquip = "UPDATE equipamentos SET situacao_id = 9 WHERE id_equipamento = ?";

	    Connection conn = null;
	    PreparedStatement stmtEnvio = null;
	    PreparedStatement stmtBuscaSit = null;
	    PreparedStatement stmtItem = null;
	    PreparedStatement stmtAtualizaEquip = null;
	    ResultSet rs = null;
	    Long idEnvioGerado = null;

	    try {
	        conn = Conexao.conectar();
	        conn.setAutoCommit(false);

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
	        stmtEnvio.setLong(9, envio.getStatusId() != null ? envio.getStatusId() : 1L);
	        stmtEnvio.setString(10, envio.getNumeroNota());

	        rs = stmtEnvio.executeQuery();
	        if (rs.next()) {
	            idEnvioGerado = rs.getLong(1);
	        }
	        
	        // Grava o histórico inicial
	        String sqlHistInserir = "INSERT INTO movimentacao_historico (id_envio, status_id, observacao) VALUES (?, ?, ?)";
	        try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistInserir)) {
	            stmtHist.setLong(1, idEnvioGerado);
	            stmtHist.setLong(2, envio.getStatusId() != null ? envio.getStatusId() : 1L);
	            stmtHist.setString(3, "Envio ID #" + idEnvioGerado + " criado e aguardando envio.");
	            stmtHist.executeUpdate();
	        }

	        stmtBuscaSit = conn.prepareStatement(sqlBuscaSituacao);
	        stmtItem = conn.prepareStatement(sqlItem);
	        stmtAtualizaEquip = conn.prepareStatement(sqlAtualizaEquip);

	        for (Long idEquipamento : idsEquipamentos) {
	            // 1. Descobre o nome da situação atual em que o equipamento se encontrava
	            stmtBuscaSit.setLong(1, idEquipamento);
	            String situacaoMomento = "Disponível";
	            try (ResultSet rsSit = stmtBuscaSit.executeQuery()) {
	                if (rsSit.next()) {
	                    situacaoMomento = rsSit.getString("nome");
	                }
	            }

	            // 2. Insere na tabela de itens gravando o status do momento
	            stmtItem.setLong(1, idEnvioGerado);
	            stmtItem.setLong(2, idEquipamento);
	            stmtItem.setString(3, situacaoMomento);
	            stmtItem.addBatch();

	            // 3. Atualiza o status do equipamento para 9 (Aguardando Envio)
	            stmtAtualizaEquip.setLong(1, idEquipamento);
	            stmtAtualizaEquip.addBatch();
	        }

	        stmtItem.executeBatch();
	        stmtAtualizaEquip.executeBatch();
	        
	        conn.commit();
	        
	    } catch (SQLException e) {
	        if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
	        throw e;
	    } finally {
	        if (stmtBuscaSit != null) try { stmtBuscaSit.close(); } catch (Exception e) {}
	        if (stmtItem != null) try { stmtItem.close(); } catch (Exception e) {}
	        if (stmtAtualizaEquip != null) try { stmtAtualizaEquip.close(); } catch (Exception e) {}
	        if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
	    }

	    return idEnvioGerado;
	}
    
	public List<MovimentacaoEnvio> listarTodos() throws SQLException {
	    String sql = "SELECT e.*, " +
	                 "orig.nome_empresa AS nome_origem, " +
	                 "dest.nome_empresa AS nome_destino, " +
	                 "ms.nome AS status_nome, ms.cor AS status_cor " +
	                 "FROM movimentacao_envio e " +
	                 "LEFT JOIN filiais orig ON e.origem_id = orig.id_filial " +
	                 "LEFT JOIN filiais dest ON e.destino_id = dest.id_filial " +
	                 "LEFT JOIN movimentacao_status ms ON e.status_id = ms.id " +
	                 "ORDER BY e.data_envio DESC, e.id_envio DESC";

	    String sqlItens = "SELECT iei.id_equipamento, eq.id_sistema, eq.patrimonio, eq.numero_serie, " +
	                      "p.modelo AS nome_produto, m.nome_marca AS marca " +
	                      "FROM movimentacao_envio_itens iei " +
	                      "INNER JOIN equipamentos eq ON iei.id_equipamento = eq.id_equipamento " +
	                      "INNER JOIN produtos p ON eq.id_produto = p.id " +
	                      "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
	                      "WHERE iei.id_envio = ?";

        String sqlHistoricoEnvio = "SELECT h.*, ms.nome AS status_nome, ms.cor AS status_cor " +
                                   "FROM movimentacao_historico h " +
                                   "LEFT JOIN movimentacao_status ms ON h.status_id = ms.id " +
                                   "WHERE h.id_envio = ? ORDER BY h.data_hora ASC";

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
	            env.setNumeroNota(rs.getString("numero_nota"));
	            
	            if (rs.getDate("data_previsa_entrega") != null) {
	                env.setDataPrevisaoEntrega(rs.getDate("data_previsa_entrega").toLocalDate());
	            }
	            env.setObservacoes(rs.getString("observacoes"));
	            env.setStatusId(rs.getLong("status_id"));
	            env.setStatusNome(rs.getString("status_nome") != null ? rs.getString("status_nome") : "Desconhecido");
	            env.setStatusCor(rs.getString("status_cor") != null ? rs.getString("status_cor") : "#6c757d");
	            
	            // Busca os produtos/itens vinculados
	            List<Map<String, Object>> produtosEnvio = new java.util.ArrayList<>();
	            try (PreparedStatement stmtItens = conn.prepareStatement(sqlItens)) {
	                stmtItens.setLong(1, env.getIdEnvio());
	                try (ResultSet rsItens = stmtItens.executeQuery()) {
	                    while (rsItens.next()) {
	                        Map<String, Object> prod = new HashMap<>();
	                        prod.put("idSistema", rsItens.getString("id_sistema"));
	                        prod.put("patrimonio", rsItens.getString("patrimonio"));
	                        prod.put("produtoNome", (rsItens.getString("marca") != null ? rsItens.getString("marca") + " - " : "") + rsItens.getString("nome_produto"));
	                        prod.put("numeroSerie", rsItens.getString("numero_serie"));
	                        produtosEnvio.add(prod);
	                    }
	                }
	            }
	            env.setProdutos(produtosEnvio);

	            // Busca o histórico do envio
	            List<model.MovimentacaoHistorico> listaHistorico = new java.util.ArrayList<>();
	            try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistoricoEnvio)) {
	                stmtHist.setLong(1, env.getIdEnvio());
	                try (ResultSet rsHist = stmtHist.executeQuery()) {
	                    while (rsHist.next()) {
	                        model.MovimentacaoHistorico hist = new model.MovimentacaoHistorico();
	                        hist.setIdHistorico(rsHist.getLong("id_historico"));
	                        hist.setIdEnvio(rsHist.getLong("id_envio"));
	                        hist.setStatusId(rsHist.getLong("status_id"));
	                        hist.setStatusNome(rsHist.getString("status_nome"));
	                        
	                        if (rsHist.getTimestamp("data_hora") != null) {
	                            hist.setDataHora(rsHist.getTimestamp("data_hora").toLocalDateTime());
	                        }
	                        
	                        hist.setObservacao(rsHist.getString("observacao"));
	                        listaHistorico.add(hist);
	                    }
	                }
	            }
	            env.setHistorico(listaHistorico);
                lista.add(env); // <--- Adicionado para preencher a lista corretamente!
	        }
	    }
	    return lista;
	}
	
	public void confirmarRecebimento(Long idEnvio, Long destinoId) throws SQLException {
	    // 1. Verifica o status atual no banco para blindar contra cache / dupla aba
	    String sqlVerificaStatus = "SELECT status_id FROM movimentacao_envio WHERE id_envio = ?";
	    String sqlBuscaCodigoFilial = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
	    String sqlBuscaItens = "SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?";
	    String sqlAtualizaEnvioStatus = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ?"; // 3 = Recebido
	    
	    // 2. Atualiza a situação para 1 (Disponível na nova filial) e atualiza o 'origem_codigo'
	    String sqlAtualizaEquipamento = "UPDATE equipamentos SET situacao_id = 1, origem_codigo = ? WHERE id_equipamento = ?";

	    Connection conn = null;
	    try {
	        conn = Conexao.conectar();
	        conn.setAutoCommit(false);

	        // =========================================================================
	        // TRAVA DE SEGURANÇA CONTRA CACHE (Bloqueia se já foi cancelado ou recebido)
	        // =========================================================================
	        try (PreparedStatement stmtStatus = conn.prepareStatement(sqlVerificaStatus)) {
	            stmtStatus.setLong(1, idEnvio);
	            try (ResultSet rs = stmtStatus.executeQuery()) {
	                if (rs.next()) {
	                    Long statusAtual = rs.getLong("status_id");
	                    // Se o status for diferente de 1 (Aguardando) e 2 (Em Trânsito), rejeita!
	                    if (statusAtual != null && statusAtual != 1L && statusAtual != 2L) {
	                        throw new SQLException("Ação negada: Este envio foi cancelado ou já foi finalizado em outra tela.");
	                    }
	                } else {
	                    throw new SQLException("Envio não encontrado.");
	                }
	            }
	        }

	        Long origemCodigoDestino = null;
	        try (PreparedStatement stmtFilial = conn.prepareStatement(sqlBuscaCodigoFilial)) {
	            stmtFilial.setLong(1, destinoId);
	            try (ResultSet rs = stmtFilial.executeQuery()) {
	                if (rs.next()) {
	                    origemCodigoDestino = rs.getLong("origem_codigo");
	                } else {
	                    throw new SQLException("Filial de destino não encontrada.");
	                }
	            }
	        }

	        // Atualiza o status do envio para Recebido (ID 3)
	        try (PreparedStatement stmtUpEnvio = conn.prepareStatement(sqlAtualizaEnvioStatus)) {
	            stmtUpEnvio.setLong(1, idEnvio);
	            stmtUpEnvio.executeUpdate();
	        }

	        // Registra a baixa no histórico
	        String sqlHistRecebimento = "INSERT INTO movimentacao_historico (id_envio, status_id, observacao) VALUES (?, ?, ?)";
	        try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistRecebimento)) {
	            stmtHist.setLong(1, idEnvio);
	            stmtHist.setLong(2, 3L); 
	            stmtHist.setString(3, "Recebimento do envio ID #" + idEnvio + " confirmado com sucesso na filial.");
	            stmtHist.executeUpdate();
	        }

	        // Atualiza cada item vinculado ao envio
	        try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens);
	             PreparedStatement stmtUpEq = conn.prepareStatement(sqlAtualizaEquipamento)) {
	            
	            stmtBusca.setLong(1, idEnvio);
	            try (ResultSet rs = stmtBusca.executeQuery()) {
	                while (rs.next()) {
	                    Long idEquipamento = rs.getLong("id_equipamento");
	                    stmtUpEq.setLong(1, origemCodigoDestino); // Aplica o novo origem_codigo da filial de destino
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
        // 1. Busca a origem_id e O STATUS ATUAL real do envio no banco de dados para evitar cancelamento via cache/dupla aba
        String sqlBuscaEnvio = "SELECT origem_id, status_id FROM movimentacao_envio WHERE id_envio = ?";
        String sqlBuscaItensEnvio = "SELECT iei.id_equipamento, iei.status_equipamento_momento, e.origem_codigo as origem_atual " +
                                     "FROM movimentacao_envio_itens iei " +
                                     "JOIN equipamentos e ON iei.id_equipamento = e.id_equipamento " +
                                     "WHERE iei.id_envio = ?";
        
        // 2. Busca a situação (id) na tabela situacao_equipamento com base no nome salvo no momento do envio
        String sqlBuscaIdSituacaoPorNome = "SELECT id FROM situacao_equipamento WHERE LOWER(nome) = LOWER(?)";
        
        // 3. Atualiza o equipamento de volta para a situação e filial de origem originais
        String sqlVoltaEquip = "UPDATE equipamentos SET situacao_id = ?, origem_codigo = ? WHERE id_equipamento = ?";
        
        // 4. Cancela o status do envio apenas se o status atual no banco for 1 (Aguardando) ou 2 (Em Trânsito)
        String sqlCancelaEnvioStatus = "UPDATE movimentacao_envio SET status_id = 4 WHERE id_envio = ?";
        
        // 5. Registra o histórico
        String sqlHistCancelamento = "INSERT INTO movimentacao_historico (id_envio, status_id, observacao) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // Descobre a filial de origem e o status atual REAL do banco de dados
            Long origemIdOriginal = null;
            Long statusAtual = null;
            try (PreparedStatement stmtOrigem = conn.prepareStatement(sqlBuscaEnvio)) {
                stmtOrigem.setLong(1, idEnvio);
                try (ResultSet rs = stmtOrigem.executeQuery()) {
                    if (rs.next()) {
                        origemIdOriginal = rs.getLong("origem_id");
                        statusAtual = rs.getLong("status_id");
                    } else {
                        throw new SQLException("Envio não encontrado.");
                    }
                }
            }

            // TRAVA DE SEGURANÇA CONTRA CACHE: Se o status não for 1 (Aguardando) nem 2 (Em Trânsito), bloqueia!
            // Ex: Status 3 = Recebido, Status 4 = Já Cancelado.
            if (statusAtual != null && statusAtual != 1L && statusAtual != 2L) {
                throw new SQLException("Ação negada: Esta movimentação já foi finalizada (Recebida ou Cancelada) e não pode ser cancelada.");
            }

            // Descobre o código da filial de origem (origem_codigo) para retornar o equipamento para o lugar certo
            Long origemCodigoReal = null;
            String sqlBuscaCodigoFilial = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
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

            // Restaura cada equipamento para o status que ele tinha ANTES do envio
            try (PreparedStatement stmtBuscaItens = conn.prepareStatement(sqlBuscaItensEnvio);
                 PreparedStatement stmtBuscaSitId = conn.prepareStatement(sqlBuscaIdSituacaoPorNome);
                 PreparedStatement stmtVolta = conn.prepareStatement(sqlVoltaEquip)) {
                
                stmtBuscaItens.setLong(1, idEnvio);
                try (ResultSet rsItens = stmtBuscaItens.executeQuery()) {
                    while (rsItens.next()) {
                        Long idEquipamento = rsItens.getLong("id_equipamento");
                        String statusMomento = rsItens.getString("status_equipamento_momento");
                        
                        Long situacaoIdOriginal = 1L; 
                        if (statusMomento != null && !statusMomento.isEmpty()) {
                            stmtBuscaSitId.setString(1, statusMomento);
                            try (ResultSet rsSit = stmtBuscaSitId.executeQuery()) {
                                if (rsSit.next()) {
                                    situacaoIdOriginal = rsSit.getLong("id");
                                }
                            }
                        }

                        stmtVolta.setLong(1, situacaoIdOriginal);
                        stmtVolta.setLong(2, origemCodigoReal);
                        stmtVolta.setLong(3, idEquipamento);
                        stmtVolta.addBatch();
                    }
                    stmtVolta.executeBatch();
                }
            }

            // Altera o status do envio para Cancelado (ID 4)
            try (PreparedStatement stmtCancela = conn.prepareStatement(sqlCancelaEnvioStatus)) {
                stmtCancela.setLong(1, idEnvio);
                stmtCancela.executeUpdate();
            }

            // Registra no histórico do envio
            try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistCancelamento)) {
                stmtHist.setLong(1, idEnvio);
                stmtHist.setLong(2, 4L); 
                stmtHist.setString(3, "Envio ID #" + idEnvio + " foi cancelado e os equipamentos retornaram à origem.");
                stmtHist.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }
	
	public boolean existeEnvioPendenteParaEquipamento(Long idEquipamento) throws SQLException {
        // Verifica se o equipamento está vinculado a algum envio ativo que ainda não foi recebido ou cancelado (status 1 = Aguardando, 2 = Em Trânsito)
        String sql = "SELECT COUNT(*) FROM movimentacao_envio_itens mei " +
                     "JOIN movimentacao_envio me ON mei.id_envio = me.id_envio " +
                     "WHERE mei.id_equipamento = ? AND me.status_id IN (1, 2)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEquipamento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
	
    public void efetivarEnvio(Long idEnvio) throws SQLException {
        String sqlUpdateEnvio = "UPDATE movimentacao_envio SET status_id = 2 WHERE id_envio = ?";
        String sqlUpdateEquip = "UPDATE equipamentos SET situacao_id = 3 WHERE id_equipamento IN (SELECT id_equipamento FROM movimentacao_envio_itens WHERE id_envio = ?)";
        String sqlHist = "INSERT INTO movimentacao_historico (id_envio, status_id, observacao) VALUES (?, 2, ?)";

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlUpdateEnvio);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlUpdateEquip);
                 PreparedStatement stmt3 = conn.prepareStatement(sqlHist)) {
                
                // 1. Muda status do envio para 2 (Em Trânsito)
                stmt1.setLong(1, idEnvio);
                stmt1.executeUpdate();
                
                // 2. Muda situação dos equipamentos para 3 (Em Trânsito)
                stmt2.setLong(1, idEnvio);
                stmt2.executeUpdate();
                
                // 3. Registra no histórico
                stmt3.setLong(1, idEnvio);
                stmt3.setString(2, "Envio efetivado. Equipamentos em trânsito.");
                stmt3.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
}