package dao;

import conexao.Conexao;
import model.ManutencaoChamado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoDAO {

	public Long inserir(ManutencaoChamado chamado) throws SQLException {
	    // 1. Incluído 'responsavel_tecnico' na lista de colunas e um placeholder '?' a mais
	    String sqlChamado = "INSERT INTO manutencao_chamados (" +
	            "id_equipamento, filial_origem_id, departamento_id, data_abertura, " +
	            "solicitante, tipo_problema, prioridade, descricao_problema, " +
	            "previsao_atendimento, observacoes, id_status_chamado, responsavel_tecnico" +
	            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_chamado";
	                        
	    String sqlStatusEquipamento = "UPDATE equipamentos SET situacao_id = 6, status_id = 2 WHERE id_equipamento = ?";

	    Connection conn = null;
	    PreparedStatement stmtChamado = null;
	    PreparedStatement stmtStatus = null;
	    ResultSet rs = null;
	    Long idGerado = null;

	    try {
	        conn = Conexao.conectar();
	        conn.setAutoCommit(false);

	        stmtChamado = conn.prepareStatement(sqlChamado);
	        stmtChamado.setLong(1, chamado.getIdEquipamento());
	        
	        if (chamado.getFilialOrigemId() != null) {
	            stmtChamado.setLong(2, chamado.getFilialOrigemId());
	        } else {
	            stmtChamado.setNull(2, Types.INTEGER);
	        }
	        
	        if (chamado.getIdDepartamento() != null) {
	            stmtChamado.setLong(3, chamado.getIdDepartamento());
	        } else {
	            stmtChamado.setNull(3, Types.INTEGER);
	        }
	        
	        stmtChamado.setDate(4, Date.valueOf(chamado.getDataAbertura()));
	        stmtChamado.setString(5, chamado.getSolicitante());
	        stmtChamado.setString(6, chamado.getTipoProblema());
	        stmtChamado.setString(7, chamado.getPrioridade());
	        stmtChamado.setString(8, chamado.getDescricaoProblema());
	        
	        if (chamado.getPrevisaoAtendimento() != null) {
	            stmtChamado.setDate(9, Date.valueOf(chamado.getPrevisaoAtendimento()));
	        } else {
	            stmtChamado.setNull(9, Types.DATE);
	        }
	        
	        stmtChamado.setString(10, chamado.getObservacoes());
	        stmtChamado.setLong(11, 1L); // ID 1 = Status inicial "Aberto"
	        
	        // 2. Definir o valor do responsável técnico na posição 12
	        if (chamado.getResponsavelTecnico() != null && !chamado.getResponsavelTecnico().trim().isEmpty()) {
	            stmtChamado.setString(12, chamado.getResponsavelTecnico());
	        } else {
	            stmtChamado.setNull(12, Types.VARCHAR);
	        }

	        rs = stmtChamado.executeQuery();
	        if (rs.next()) {
	            idGerado = rs.getLong(1);
	        }

	        stmtStatus = conn.prepareStatement(sqlStatusEquipamento);
	        stmtStatus.setLong(1, chamado.getIdEquipamento());
	        stmtStatus.executeUpdate();

	        conn.commit();

	    } catch (SQLException e) {
	        if (conn != null) {
	            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
	        }
	        throw e;
	    } finally {
	        try { if (stmtChamado != null) stmtChamado.close(); } catch (Exception e) {}
	        try { if (stmtStatus != null) stmtStatus.close(); } catch (Exception e) {}
	        Conexao.fechar(rs, null, conn);
	    }

	    return idGerado;
	}
	
    public List<ManutencaoChamado> listarPorEquipamento(Long idEquipamento) throws SQLException {
        List<ManutencaoChamado> lista = new ArrayList<>();
        String sql = "SELECT c.*, s.nome_status FROM manutencao_chamados c " +
                "LEFT JOIN status_chamado s ON c.id_status_chamado = s.id_status_chamado " +
                "WHERE c.id_equipamento = ? ORDER BY c.data_abertura DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idEquipamento);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ManutencaoChamado m = new ManutencaoChamado();
                m.setIdChamado(rs.getLong("id_chamado"));
                m.setIdEquipamento(rs.getLong("id_equipamento"));
                
                long filialId = rs.getLong("filial_origem_id");
                if (!rs.wasNull()) {
                    m.setFilialOrigemId(filialId);
                }

                m.setDataAbertura(rs.getDate("data_abertura").toLocalDate());
                m.setSolicitante(rs.getString("solicitante"));
                m.setTipoProblema(rs.getString("tipo_problema"));
                m.setPrioridade(rs.getString("prioridade"));
                m.setDescricaoProblema(rs.getString("descricao_problema"));
                m.setResponsavelTecnico(rs.getString("responsavel_tecnico"));
                m.setNomeStatus(rs.getString("nome_status"));
                
                if (rs.getDate("previsao_atendimento") != null) {
                    m.setPrevisaoAtendimento(rs.getDate("previsao_atendimento").toLocalDate());
                }
                
                m.setIdStatusChamado(rs.getLong("id_status_chamado"));
                lista.add(m);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }

        return lista;
    }
    
    public List<ManutencaoChamado> listarTodos() throws SQLException {
        List<ManutencaoChamado> lista = new ArrayList<>();
        // JOIN com equipamentos para trazer o nome identificador ou patrimônio
        String sql = "SELECT c.*, s.nome_status, e.nome_identificador, e.patrimonio FROM manutencao_chamados c " +
                     "LEFT JOIN status_chamado s ON c.id_status_chamado = s.id_status_chamado " +
                     "LEFT JOIN equipamentos e ON c.id_equipamento = e.id_equipamento " +
                     "ORDER BY c.id_chamado DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ManutencaoChamado m = new ManutencaoChamado();
                m.setIdChamado(rs.getLong("id_chamado"));
                m.setIdEquipamento(rs.getLong("id_equipamento"));
                
                // Define o nome amigável do equipamento (ex: nome_identificador ou patrimônio)
                String nomeEquip = rs.getString("nome_identificador");
                String patrimonio = rs.getString("patrimonio");
                m.setNomeEquipamento(nomeEquip != null ? nomeEquip + " (Pat: " + patrimonio + ")" : "EQ-" + m.getIdEquipamento());

                if (rs.getDate("data_abertura") != null) {
                    m.setDataAbertura(rs.getDate("data_abertura").toLocalDate());
                }
                m.setSolicitante(rs.getString("solicitante"));
                m.setTipoProblema(rs.getString("tipo_problema"));
                m.setPrioridade(rs.getString("prioridade"));
                m.setDescricaoProblema(rs.getString("descricao_problema"));
                m.setResponsavelTecnico(rs.getString("responsavel_tecnico"));
                m.setDiagnostico(rs.getString("diagnostico")); // Certifique-se de ter essa coluna no banco/modelo
                m.setSolucaoRealizada(rs.getString("solucao_realizada")); // Certifique-se de ter essa coluna
                
                String nomeStatus = rs.getString("nome_status");
                m.setNomeStatus(nomeStatus != null ? nomeStatus : "Aberto");
                m.setIdStatusChamado(rs.getLong("id_status_chamado"));
                
                lista.add(m);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }

        return lista;
    }
    
    public List<ManutencaoChamado> listarComFiltros(String busca, String status, String tipo, String prioridade) throws SQLException {
        List<ManutencaoChamado> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, s.nome_status, e.nome_identificador, e.patrimonio FROM manutencao_chamados c " +
            "LEFT JOIN status_chamado s ON c.id_status_chamado = s.id_status_chamado " +
            "LEFT JOIN equipamentos e ON c.id_equipamento = e.id_equipamento WHERE 1=1 "
        );

        List<Object> parametros = new ArrayList<>();

        if (busca != null && !busca.trim().isEmpty()) {
            sql.append("AND (CAST(c.id_chamado AS TEXT) ILIKE ? OR e.patrimonio ILIKE ? OR c.descricao_problema ILIKE ?) ");
            String termo = "%" + busca.trim() + "%";
            parametros.add(termo);
            parametros.add(termo);
            parametros.add(termo);
        }

        if (status != null && !status.trim().isEmpty() && !status.equals("Todos")) {
            sql.append("AND s.nome_status = ? ");
            parametros.add(status);
        }

        if (tipo != null && !tipo.trim().isEmpty() && !tipo.equals("Todas")) {
            sql.append("AND c.tipo_problema = ? ");
            parametros.add(tipo);
        }

        if (prioridade != null && !prioridade.trim().isEmpty() && !prioridade.equals("Todas")) {
            sql.append("AND c.prioridade = ? ");
            parametros.add(prioridade);
        }

        sql.append(" ORDER BY c.id_chamado DESC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                ManutencaoChamado m = new ManutencaoChamado();
                m.setIdChamado(rs.getLong("id_chamado"));
                m.setIdEquipamento(rs.getLong("id_equipamento"));
                
                String nomeEquip = rs.getString("nome_identificador");
                String patrimonio = rs.getString("patrimonio");
                m.setNomeEquipamento(nomeEquip != null ? nomeEquip + " (Pat: " + patrimonio + ")" : "EQ-" + m.getIdEquipamento());

                if (rs.getDate("data_abertura") != null) {
                    m.setDataAbertura(rs.getDate("data_abertura").toLocalDate());
                }
                m.setSolicitante(rs.getString("solicitante"));
                m.setTipoProblema(rs.getString("tipo_problema"));
                m.setPrioridade(rs.getString("prioridade"));
                m.setDescricaoProblema(rs.getString("descricao_problema"));
                m.setResponsavelTecnico(rs.getString("responsavel_tecnico"));
                m.setDiagnostico(rs.getString("diagnostico"));
                m.setSolucaoRealizada(rs.getString("solucao_realizada"));
                
                String nomeStatus = rs.getString("nome_status");
                m.setNomeStatus(nomeStatus != null ? nomeStatus : "Aberto");
                m.setIdStatusChamado(rs.getLong("id_status_chamado"));
                
                lista.add(m);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }

        return lista;
    }
    /*Consulta o banco por meio desse buscarPorId(idChamado), recuperando o responsavel_tecnico (ou solicitante) 
     * gravado na base de dados para validar se ele realmente tem o direito de alterar ou excluir 
     * aquele registro específico.*/
    public ManutencaoChamado buscarPorId(Long idChamado) throws SQLException {
        // 1. Adicionado u.perfil AS perfil_solicitante no SELECT e o LEFT JOIN com a tabela usuarios
        String sql = "SELECT c.*, s.nome_status, e.nome_identificador, e.patrimonio, u.perfil AS perfil_solicitante " +
                     "FROM manutencao_chamados c " +
                     "LEFT JOIN status_chamado s ON c.id_status_chamado = s.id_status_chamado " +
                     "LEFT JOIN equipamentos e ON c.id_equipamento = e.id_equipamento " +
                     "LEFT JOIN usuarios u ON c.solicitante = u.username " +
                     "WHERE c.id_chamado = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ManutencaoChamado m = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idChamado);
            rs = stmt.executeQuery();

            if (rs.next()) {
                m = new ManutencaoChamado();
                m.setIdChamado(rs.getLong("id_chamado"));
                m.setIdEquipamento(rs.getLong("id_equipamento"));
                
                String nomeEquip = rs.getString("nome_identificador");
                String patrimonio = rs.getString("patrimonio");
                m.setNomeEquipamento(nomeEquip != null ? nomeEquip + " (Pat: " + patrimonio + ")" : "EQ-" + m.getIdEquipamento());

                if (rs.getDate("data_abertura") != null) {
                    m.setDataAbertura(rs.getDate("data_abertura").toLocalDate());
                }
                m.setSolicitante(rs.getString("solicitante"));
                
                // 2. Popula o perfil do solicitante para a hierarquia no front-end funcionar
                m.setPerfilSolicitante(rs.getString("perfil_solicitante"));

                m.setTipoProblema(rs.getString("tipo_problema"));
                m.setPrioridade(rs.getString("prioridade"));
                m.setDescricaoProblema(rs.getString("descricao_problema"));
                m.setResponsavelTecnico(rs.getString("responsavel_tecnico"));
                m.setDiagnostico(rs.getString("diagnostico"));
                m.setSolucaoRealizada(rs.getString("solucao_realizada"));
                
                String nomeStatus = rs.getString("nome_status");
                m.setNomeStatus(nomeStatus != null ? nomeStatus : "Aberto");
                m.setIdStatusChamado(rs.getLong("id_status_chamado"));
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }

        return m;
    }
    public String buscarPerfilUsuarioPorUsername(String username) throws SQLException {
        String sql = "SELECT perfil FROM usuarios WHERE username = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("perfil");
                }
            }
        }
        return "usuario"; // Padrão caso não encontre
    }
    
    public void atualizar(ManutencaoChamado chamado, boolean reparado) throws SQLException {
        String sqlChamado = "UPDATE manutencao_chamados SET " +
                     "id_status_chamado = ?, " +
                     "responsavel_tecnico = ?, " +
                     "diagnostico = ?, " +
                     "solucao_realizada = ? " +
                     "WHERE id_chamado = ?";

        // SQL para devolver o equipamento para Disponível (Situação ID = 1 por exemplo, ajuste conforme seu banco) e Status Ativo (ID = 1)
        String sqlEquipamento = "UPDATE equipamentos SET situacao_id = 1, status_id = 1 WHERE id_equipamento = ?";

        Connection conn = null;
        PreparedStatement stmtChamado = null;
        PreparedStatement stmtEquip = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // 1. Atualiza o chamado
            stmtChamado = conn.prepareStatement(sqlChamado);
            if (chamado.getIdStatusChamado() != null) {
                stmtChamado.setLong(1, chamado.getIdStatusChamado());
            } else {
                stmtChamado.setNull(1, Types.INTEGER);
            }
            
            stmtChamado.setString(2, chamado.getResponsavelTecnico());
            stmtChamado.setString(3, chamado.getDiagnostico());
            stmtChamado.setString(4, chamado.getSolucaoRealizada());
            stmtChamado.setLong(5, chamado.getIdChamado());
            stmtChamado.executeUpdate();

            // 2. Se o chamado foi finalizado (6) E o equipamento foi reparado, libera o equipamento
            if (chamado.getIdStatusChamado() != null && chamado.getIdStatusChamado() == 6 && reparado) {
                // Precisamos descobrir o ID do equipamento deste chamado
                String sqlBuscaEquip = "SELECT id_equipamento FROM manutencao_chamados WHERE id_chamado = ?";
                try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaEquip)) {
                    stmtBusca.setLong(1, chamado.getIdChamado());
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (rs.next()) {
                            Long idEquipamento = rs.getLong("id_equipamento");
                            stmtEquip = conn.prepareStatement(sqlEquipamento);
                            stmtEquip.setLong(1, idEquipamento);
                            stmtEquip.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            try { if (stmtChamado != null) stmtChamado.close(); } catch (Exception e) {}
            try { if (stmtEquip != null) stmtEquip.close(); } catch (Exception e) {}
            Conexao.fechar(null, null, conn);
        }
    }
    
    public void excluir(Long idChamado) throws SQLException {
        String sqlChamado = "UPDATE manutencao_chamados SET id_status_chamado = 7 WHERE id_chamado = ?";
        String sqlEquipamento = "UPDATE equipamentos SET situacao_id = 1, status_id = 1 WHERE id_equipamento = ?";

        Connection conn = null;
        PreparedStatement stmtChamado = null;
        PreparedStatement stmtEquip = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);

            // 1. Descobre qual equipamento está vinculado a este chamado antes de cancelar
            Long idEquipamento = null;
            String sqlBuscaEquip = "SELECT id_equipamento FROM manutencao_chamados WHERE id_chamado = ?";
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaEquip)) {
                stmtBusca.setLong(1, idChamado);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    if (rs.next()) {
                        idEquipamento = rs.getLong("id_equipamento");
                    }
                }
            }

            // 2. Atualiza o status do chamado para Cancelado (7)
            stmtChamado = conn.prepareStatement(sqlChamado);
            stmtChamado.setLong(1, idChamado);
            stmtChamado.executeUpdate();

            // 3. Altera o equipamento automaticamente para Ativo (1) e Disponível (1)
            if (idEquipamento != null) {
                stmtEquip = conn.prepareStatement(sqlEquipamento);
                stmtEquip.setLong(1, idEquipamento);
                stmtEquip.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            try { if (stmtChamado != null) stmtChamado.close(); } catch (Exception e) {}
            try { if (stmtEquip != null) stmtEquip.close(); } catch (Exception e) {}
            Conexao.fechar(null, null, conn);
        }
    }
}