package dao;

import conexao.Conexao;
import model.Equipamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipamentoDAO {

	public boolean inserir(Equipamento eq) throws SQLException {
	    if (eq.getIdSistema() == null || eq.getIdSistema().trim().isEmpty()) {
	        eq.setIdSistema(gerarProximoIdSistema());
	    }

	    // Garante que todo novo equipamento comece obrigatoriamente como Disponível (ID 1) caso venha vazio/zerado
	    if (eq.getSituacaoId() <= 0) {
	        eq.setSituacaoId(1);
	    }

	    String sql = "INSERT INTO equipamentos (id_produto, id_sistema, patrimonio, numero_serie, nome_identificador, origem_codigo, ip_atual, status_id, situacao_id, usuario_atual, departamento_id, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    
	    int tentativas = 3;
	    for (int i = 0; i < tentativas; i++) {
	        try {
	            conn = Conexao.conectar();
	            stmt = conn.prepareStatement(sql);
	            
	            stmt.setInt(1, eq.getIdProduto());
	            stmt.setString(2, eq.getIdSistema());
	            stmt.setString(3, eq.getPatrimonio());
	            stmt.setString(4, eq.getNumeroSerie());
	            stmt.setString(5, eq.getNomeIdentificador());
	            
	            // Tratativa corrigida para buscar o código real da filial
	            if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
	                Integer codigoReal = buscarOrigemCodigoPorIdFilial(conn, eq.getOrigemCodigo());
	                stmt.setInt(6, codigoReal);
	            } else {
	                stmt.setNull(6, Types.INTEGER);
	            }
	            
	            stmt.setString(7, eq.getIpAtual());
	            
	            // Status do Equipamento
	            stmt.setInt(8, eq.getStatusId() > 0 ? eq.getStatusId() : 1);
	            
	            // Situação do Equipamento (Força 1 se for menor ou igual a 0)
	            stmt.setInt(9, eq.getSituacaoId());
	            
	            stmt.setString(10, eq.getUsuarioAtual());
	            
	            if (eq.getDepartamentoId() != null) {
	                stmt.setInt(11, eq.getDepartamentoId());
	            } else {
	                stmt.setNull(11, Types.INTEGER);
	            }
	            
	            stmt.setString(12, eq.getObservacoes());
	            
	            return stmt.executeUpdate() > 0;
	            
	        } catch (SQLException e) {
	            if ("23505".equals(e.getSQLState()) && i < tentativas - 1) {
	                eq.setIdSistema(gerarProximoIdSistema());
	                Conexao.fechar(null, stmt, conn);
	            } else {
	                throw e;
	            }
	        } finally {
	            Conexao.fechar(null, stmt, conn);
	        }
	    }
	    return false;
	}

	public List<Equipamento> listar() throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        // Ajustado de status_id != 0 para status_id != 3 (Inativo)
        String sql = "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, t.nome as nome_tipo, " +
                     "se.nome AS status_nome, se.cor AS status_cor, " +
                     "sit.nome AS situacao_nome, " +
                     "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo " +
                     "FROM equipamentos e " +
                     "INNER JOIN produtos p ON e.id_produto = p.id " +
                     "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                     "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
                     "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
                     "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
                     "WHERE e.status_id != 3 " + 
                     "ORDER BY e.id_equipamento DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Equipamento eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                eq.setNomeProduto(rs.getString("produto_completo"));
                
                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);

                eq.setIpAtual(rs.getString("ip_atual"));
                
                eq.setStatusId(rs.getInt("status_id"));
                eq.setSituacaoId(rs.getInt("situacao_id"));
                eq.setStatusNome(rs.getString("status_nome"));
                eq.setStatusCor(rs.getString("status_cor"));
                eq.setSituacaoNome(rs.getString("situacao_nome"));

                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);

                eq.setObservacoes(rs.getString("observacoes"));
                eq.setDataCadastro(rs.getString("data_cadastro"));
                
                lista.add(eq);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return lista;
    }
    
	public List<Equipamento> listarComFiltros(String pesquisaGlobal, String idSistema, String patrimonio, String serial, String origem, String departamento, String statusIdFiltro, String situacaoIdFiltro, String produto, String usuario) throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();

            StringBuilder sql = new StringBuilder(
                "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, " +
                "se.nome AS status_nome, se.cor AS status_cor, " +
                "sit.nome AS situacao_nome, " +
                "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo, " +
                "f.nome_empresa as nome_origem, d.nome_departamento as nome_departamento " +
                "FROM equipamentos e " +
                "INNER JOIN produtos p ON e.id_produto = p.id " +
                "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                "LEFT JOIN filiais f ON e.origem_codigo = f.origem_codigo " +
                "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
                "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
                "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
                "WHERE 1=1"
            );
            
            List<Object> parametros = new ArrayList<>();

            if (statusIdFiltro == null || statusIdFiltro.trim().isEmpty()) {
                sql.append(" AND e.status_id != 3");
            }

            if (pesquisaGlobal != null && !pesquisaGlobal.trim().isEmpty()) {
                sql.append(" AND (e.id_sistema ILIKE ? OR e.patrimonio ILIKE ? OR e.numero_serie ILIKE ? OR e.usuario_atual ILIKE ? OR e.nome_identificador ILIKE ? OR p.modelo ILIKE ? OR p.codigo_catalogo ILIKE ? OR f.nome_empresa ILIKE ? OR d.nome_departamento ILIKE ?)");
                String termoGlobal = "%" + pesquisaGlobal.trim() + "%";
                for (int i = 0; i < 9; i++) {
                    parametros.add(termoGlobal);
                }
            }

            if (idSistema != null && !idSistema.trim().isEmpty()) {
                sql.append(" AND e.id_sistema ILIKE ?");
                parametros.add("%" + idSistema.trim() + "%");
            }
            if (patrimonio != null && !patrimonio.trim().isEmpty()) {
                sql.append(" AND e.patrimonio ILIKE ?");
                parametros.add("%" + patrimonio.trim() + "%");
            }
            if (serial != null && !serial.trim().isEmpty()) {
                sql.append(" AND e.numero_serie ILIKE ?");
                parametros.add("%" + serial.trim() + "%");
            }
            if (origem != null && !origem.trim().isEmpty()) {
                try {
                    int valorOrigem = Integer.parseInt(origem.trim());
                    Integer codigoReal = buscarOrigemCodigoPorIdFilial(conn, valorOrigem);
                    sql.append(" AND e.origem_codigo = ?");
                    parametros.add(codigoReal != null ? codigoReal : valorOrigem);
                } catch (NumberFormatException e) {
                    // Se não for um número válido, ignora o filtro de origem para não quebrar a listagem
                }
            }
            if (departamento != null && !departamento.trim().isEmpty()) {
                sql.append(" AND e.departamento_id = ?");
                parametros.add(Integer.parseInt(departamento));
            }
            if (statusIdFiltro != null && !statusIdFiltro.trim().isEmpty()) {
                sql.append(" AND e.status_id = ?");
                parametros.add(Integer.parseInt(statusIdFiltro));
            }
            if (situacaoIdFiltro != null && !situacaoIdFiltro.trim().isEmpty()) {
                sql.append(" AND e.situacao_id = ?");
                parametros.add(Integer.parseInt(situacaoIdFiltro));
               // ADICIONE ESTE BLOCO LOGO ABAIXO: Se for a situação 6 (Assistência), garante que não há chamado ativo
                if ("6".equals(situacaoIdFiltro.trim())) {
                    sql.append(" AND NOT EXISTS (SELECT 1 FROM manutencao_chamados c WHERE c.id_equipamento = e.id_equipamento AND c.id_status_chamado IN (1, 2, 3, 4, 5))");
                }
            }
            if (produto != null && !produto.trim().isEmpty()) {
                sql.append(" AND (p.modelo ILIKE ? OR p.codigo_catalogo ILIKE ?)");
                parametros.add("%" + produto.trim() + "%");
                parametros.add("%" + produto.trim() + "%");
            }
            if (usuario != null && !usuario.trim().isEmpty()) {
                sql.append(" AND e.usuario_atual ILIKE ?");
                parametros.add("%" + usuario.trim() + "%");
            }

            sql.append(" ORDER BY e.id_equipamento DESC");

            stmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Equipamento eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                eq.setNomeProduto(rs.getString("produto_completo")); 
                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);
                
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);

                eq.setIpAtual(rs.getString("ip_atual"));
                eq.setStatusId(rs.getInt("status_id"));
                eq.setSituacaoId(rs.getInt("situacao_id"));
                eq.setStatusNome(rs.getString("status_nome"));
                eq.setStatusCor(rs.getString("status_cor"));
                eq.setSituacaoNome(rs.getString("situacao_nome"));
                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                eq.setObservacoes(rs.getString("observacoes"));
                
                try { eq.getClass().getMethod("setNomeOrigem", String.class).invoke(eq, rs.getString("nome_origem")); } catch (Exception ignored) {}
                try { eq.getClass().getMethod("setNomeDepartamento", String.class).invoke(eq, rs.getString("nome_departamento")); } catch (Exception ignored) {}

                lista.add(eq);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return lista;
    }
	
	// Lista apenas as situações que permitem alteração/cadastro direto (ocultando as de fluxo automático)
    public List<Map<String, Object>> listarSituacoesEdicaoDireta() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM situacao_equipamento WHERE permite_edicao_direta = true ORDER BY nome";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> sit = new HashMap<>();
                sit.put("id", rs.getInt("id"));
                sit.put("nome", rs.getString("nome"));
                lista.add(sit);
            }
        }
        return lista;
    }
    
	public String gerarProximoIdSistema() throws SQLException {
        String sql = "SELECT id_sistema FROM equipamentos ORDER BY id_equipamento DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        long proximoNumero = 1;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String ultimoId = rs.getString("id_sistema");
                if (ultimoId != null && ultimoId.startsWith("EQ")) {
                    String apenasNumeros = ultimoId.replaceAll("\\D+", "");
                    proximoNumero = Long.parseLong(apenasNumeros) + 1;
                }
            }
        } catch (Exception e) {
            proximoNumero = 1;
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        
        return String.format("EQ%010d", proximoNumero);
    }
	
	public List<Equipamento> listarDisponiveisPorOrigem(long origemCodigo) throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        String sql = "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, " +
                     "se.nome AS status_nome, se.cor AS status_cor, " +
                     "sit.nome AS situacao_nome, " +
                     "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo " +
                     "FROM equipamentos e " +
                     "INNER JOIN produtos p ON e.id_produto = p.id " +
                     "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                     "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
                     "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
                     "WHERE e.origem_codigo = ? AND e.status_id != 3 AND e.situacao_id = 1 " +
                     "AND NOT EXISTS (" +
                     "    SELECT 1 FROM manutencao_chamados c " +
                     "    WHERE c.id_equipamento = e.id_equipamento " +
                     "    AND c.id_status_chamado IN (1, 2, 3, 4, 5)" +
                     ") " +
                     "ORDER BY p.modelo ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, origemCodigo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipamento eq = new Equipamento();
                    eq.setIdEquipamento(rs.getInt("id_equipamento"));
                    eq.setIdProduto(rs.getInt("id_produto"));
                    eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                    eq.setNomeProduto(rs.getString("produto_completo"));
                    eq.setIdSistema(rs.getString("id_sistema"));
                    eq.setPatrimonio(rs.getString("patrimonio"));
                    eq.setNumeroSerie(rs.getString("numero_serie"));
                    eq.setNomeIdentificador(rs.getString("nome_identificador"));
                    eq.setOrigemCodigo(rs.getInt("origem_codigo"));
                    eq.setIpAtual(rs.getString("ip_atual"));
                    eq.setStatusId(rs.getInt("status_id"));
                    eq.setSituacaoId(rs.getInt("situacao_id"));
                    eq.setStatusNome(rs.getString("status_nome"));
                    eq.setStatusCor(rs.getString("status_cor"));
                    eq.setSituacaoNome(rs.getString("situacao_nome"));
                    eq.setUsuarioAtual(rs.getString("usuario_atual"));
                    
                    int depId = rs.getInt("departamento_id");
                    eq.setDepartamentoId(rs.wasNull() ? null : depId);

                    eq.setObservacoes(rs.getString("observacoes"));
                    eq.setDataCadastro(rs.getString("data_cadastro"));
                    
                    lista.add(eq);
                }
            }
        }
        return lista;
    }
    
	public Equipamento buscarPorId(int idEquipamento) throws SQLException {
        String sql = "SELECT e.*, p.codigo_catalogo, p.modelo, p.descricao_catalogo, " +
                     "m.nome_marca, t.nome as nome_tipo, d.nome_departamento, " +
                     "se.nome AS status_nome, se.cor AS status_cor, sit.nome AS situacao_nome " +
                     "FROM equipamentos e " +
                     "INNER JOIN produtos p ON e.id_produto = p.id " +
                     "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                     "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
                     "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
                     "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
                     "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
                     "WHERE e.id_equipamento = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Equipamento eq = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idEquipamento);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                eq.setNomeProduto(rs.getString("modelo"));
                
                eq.setModelo(rs.getString("modelo"));
                eq.setNomeMarca(rs.getString("nome_marca"));
                eq.setNomeTipo(rs.getString("nome_tipo"));
                eq.setDescricaoDetalhada(rs.getString("descricao_catalogo")); 

                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);
                
                eq.setIpAtual(rs.getString("ip_atual"));
                eq.setStatusId(rs.getInt("status_id"));
                eq.setSituacaoId(rs.getInt("situacao_id"));
                eq.setStatusNome(rs.getString("status_nome"));
                eq.setStatusCor(rs.getString("status_cor"));
                eq.setSituacaoNome(rs.getString("situacao_nome"));
                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);
                
                eq.setNomeDepartamento(rs.getString("nome_departamento"));
                eq.setObservacoes(rs.getString("observacoes"));
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return eq;
    }

    public boolean atualizar(Equipamento eq) throws SQLException {
        String sql = "UPDATE equipamentos SET id_produto = ?, patrimonio = ?, numero_serie = ?, nome_identificador = ?, origem_codigo = ?, ip_atual = ?, status_id = ?, situacao_id = ?, usuario_atual = ?, departamento_id = ?, observacoes = ? WHERE id_equipamento = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, eq.getIdProduto());
            stmt.setString(2, eq.getPatrimonio());
            stmt.setString(3, eq.getNumeroSerie());
            stmt.setString(4, eq.getNomeIdentificador());
            
            if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
                stmt.setInt(5, eq.getOrigemCodigo());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setString(6, eq.getIpAtual());
            stmt.setInt(7, eq.getStatusId() > 0 ? eq.getStatusId() : 1);
            stmt.setInt(8, eq.getSituacaoId() > 0 ? eq.getSituacaoId() : 1);
            stmt.setString(9, eq.getUsuarioAtual());
            
            if (eq.getDepartamentoId() != null) {
                stmt.setInt(10, eq.getDepartamentoId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            
            stmt.setString(11, eq.getObservacoes());
            stmt.setInt(12, eq.getIdEquipamento());
            
            return stmt.executeUpdate() > 0;
        } finally {
            Conexao.fechar(null, stmt, conn);
        }
    }
    
    public boolean atualizarSituacao(int idEquipamento, int novoSituacaoId) throws SQLException {
        String sql = "UPDATE equipamentos SET situacao_id = ? WHERE id_equipamento = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoSituacaoId);
            stmt.setInt(2, idEquipamento);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public void excluirEquipamento(int idEquipamento) throws SQLException {
        // Altera o status para Inativo (3) e a situação para Baixado/Inativo (ex: ID 7)
        String sql = "UPDATE equipamentos SET status_id = 3, situacao_id = 7 WHERE id_equipamento = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEquipamento);
            int linhas = stmt.executeUpdate();

            if (linhas == 0) {
                throw new SQLException("Equipamento não encontrado.");
            }
        }
    }

    private Integer buscarOrigemCodigoPorIdFilial(Connection conn, int idFilialOuCodigo) throws SQLException {
        String sql = "SELECT origem_codigo FROM filiais WHERE id_filial = ? OR origem_codigo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFilialOuCodigo);
            stmt.setInt(2, idFilialOuCodigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("origem_codigo");
                }
            }
        }
        return idFilialOuCodigo;
    }
}