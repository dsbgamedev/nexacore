package dao;

import conexao.Conexao;
import model.Equipamento;
import model.EspecificacaoEquipamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipamentoDAO {

	public int inserir(Equipamento eq) throws SQLException {
		// Se o status escolhido for Devolução (6), força a situação para Em Devolução (8)
	    if (eq.getStatusId() == 6) {
	        eq.setSituacaoId(8);
	    }
	    
	    if (eq.getIdSistema() == null || eq.getIdSistema().trim().isEmpty()) {
	        eq.setIdSistema(gerarProximoIdSistema());
	    }

	    if (eq.getSituacaoId() <= 0) {
	        eq.setSituacaoId(1);
	    }

	    String sqlEquipamento = "INSERT INTO equipamentos (id_produto, id_sistema, patrimonio, numero_serie, nome_identificador, origem_codigo, ip_atual, status_id, situacao_id, usuario_atual, departamento_id, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_equipamento";
	    
	    String sqlEspecificacao = "INSERT INTO equipamento_especificacoes (id_equipamento, campo_id, valor) VALUES (?, ?, ?) " +
	                              "ON CONFLICT (id_equipamento, campo_id) DO UPDATE SET valor = EXCLUDED.valor";

	    Connection conn = null;
	    PreparedStatement stmtEq = null;
	    PreparedStatement stmtEsp = null;
	    ResultSet rs = null;
	    
	    int tentativas = 3;
	    for (int i = 0; i < tentativas; i++) {
	        try {
	            conn = Conexao.conectar();
	            conn.setAutoCommit(false); // Inicia a transação
	            
	            stmtEq = conn.prepareStatement(sqlEquipamento);
	            
	            stmtEq.setInt(1, eq.getIdProduto());
	            stmtEq.setString(2, eq.getIdSistema());
	            stmtEq.setString(3, eq.getPatrimonio());
	            stmtEq.setString(4, eq.getNumeroSerie());
	            stmtEq.setString(5, eq.getNomeIdentificador());
	            
	            if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
	                Integer codigoReal = buscarOrigemCodigoPorIdFilial(conn, eq.getOrigemCodigo());
	                stmtEq.setInt(6, codigoReal);
	            } else {
	                stmtEq.setNull(6, Types.INTEGER);
	            }
	            
	            stmtEq.setString(7, eq.getIpAtual());
	            stmtEq.setInt(8, eq.getStatusId() > 0 ? eq.getStatusId() : 1);
	            stmtEq.setInt(9, eq.getSituacaoId());
	            stmtEq.setString(10, eq.getUsuarioAtual());
	            
	            if (eq.getDepartamentoId() != null) {
	                stmtEq.setInt(11, eq.getDepartamentoId());
	            } else {
	                stmtEq.setNull(11, Types.INTEGER);
	            }
	            
	            stmtEq.setString(12, eq.getObservacoes());
	            
	            rs = stmtEq.executeQuery();
	            int idGerado = 0;
	            if (rs.next()) {
	                idGerado = rs.getInt(1);
	                eq.setIdEquipamento(idGerado); // Atualiza o ID no objeto
	            }
	            
	            // Grava as especificações dinâmicas se houverem
	            if (eq.getEspecificacoes() != null && !eq.getEspecificacoes().isEmpty() && idGerado > 0) {
	                stmtEsp = conn.prepareStatement(sqlEspecificacao);
	                for (model.EspecificacaoEquipamento esp : eq.getEspecificacoes()) {
	                    stmtEsp.setInt(1, idGerado);
	                    stmtEsp.setInt(2, esp.getCampoId());
	                    stmtEsp.setString(3, esp.getValor());
	                    stmtEsp.addBatch();
	                }
	                stmtEsp.executeBatch();
	            }
	            
	            conn.commit(); // Confirma todas as operações
	            return idGerado; // Retorna o ID gerado para o Servlet
	            
	        } catch (SQLException e) {
	            if (conn != null) {
	                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
	            }
	            if ("23505".equals(e.getSQLState()) && i < tentativas - 1) {
	                eq.setIdSistema(gerarProximoIdSistema());
	            } else {
	                throw e;
	            }
	        } finally {
	            if (conn != null) {
	                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
	            }
	            Conexao.fechar(rs, stmtEq, null);
	            Conexao.fechar(null, stmtEsp, conn);
	        }
	    }
	    return 0;
	}

	public List<Equipamento> listar(List<Integer> unidadesPermitidas) throws SQLException {
	    List<Equipamento> lista = new ArrayList<>();
	    
	    if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
	        return lista;
	    }

	    StringBuilder sql = new StringBuilder(
	        "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, t.nome as nome_tipo, " +
	        "se.nome AS status_nome, se.cor AS status_cor, " +
	        "sit.nome AS situacao_nome, " +
	        "mc.id_status_chamado AS status_chamado_id, " +
	        "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo, " +
	        "(f.origem_codigo || '-' || f.sufixo) AS nome_origem, " +
	        "d.nome_departamento AS nome_departamento " +
	        "FROM equipamentos e " +
	        "INNER JOIN produtos p ON e.id_produto = p.id " +
	        "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
	        "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
	        "LEFT JOIN filiais f ON e.origem_codigo = f.origem_codigo " +
	        "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
	        "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
	        "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
	        "LEFT JOIN ( " +
	        "    SELECT m1.id_equipamento, m1.id_status_chamado, " +
	        "    ROW_NUMBER() OVER(PARTITION BY m1.id_equipamento ORDER BY m1.id_chamado DESC) as rn " + 
	        "    FROM manutencao_chamados m1 " +
	        ") mc ON mc.id_equipamento = e.id_equipamento AND mc.rn = 1 " +
	        "WHERE e.origem_codigo IN ("
	    );

	    for (int i = 0; i < unidadesPermitidas.size(); i++) {
	        sql.append(i == 0 ? "?" : ", ?");
	    }
	    sql.append(") ORDER BY e.id_equipamento DESC");
	    
	    try (Connection conn = Conexao.conectar();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        
	        for (int i = 0; i < unidadesPermitidas.size(); i++) {
	            stmt.setInt(i + 1, unidadesPermitidas.get(i));
	        }
	        
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
	                
	                int origemCod = rs.getInt("origem_codigo");
	                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);

	                eq.setIpAtual(rs.getString("ip_atual"));
	                eq.setStatusId(rs.getInt("status_id"));
	                eq.setSituacaoId(rs.getInt("situacao_id"));
	                eq.setStatusNome(rs.getString("status_nome"));
	                eq.setStatusCor(rs.getString("status_cor"));
	                eq.setSituacaoNome(rs.getString("situacao_nome"));

	                int statusChamadoId = rs.getInt("status_chamado_id");
	                eq.setStatusChamadoId(rs.wasNull() ? null : statusChamadoId);

	                eq.setUsuarioAtual(rs.getString("usuario_atual"));
	                
	                int depId = rs.getInt("departamento_id");
	                eq.setDepartamentoId(rs.wasNull() ? null : depId);

	                eq.setObservacoes(rs.getString("observacoes"));
	                eq.setDataCadastro(rs.getString("data_cadastro"));
	                
	                try { eq.getClass().getMethod("setNomeOrigem", String.class).invoke(eq, rs.getString("nome_origem")); } catch (Exception ignored) {}
	                try { eq.getClass().getMethod("setNomeDepartamento", String.class).invoke(eq, rs.getString("nome_departamento")); } catch (Exception ignored) {}

	                lista.add(eq);
	            }
	        }
	    }
	    return lista;
	}
	public List<Equipamento> listarComFiltros(String pesquisaGlobal, String idSistema, String patrimonio, String serial, String origem, String departamento, String statusIdFiltro, String situacaoIdFiltro, String produto, String usuario, List<Integer> unidadesPermitidas) throws SQLException {
	    List<Equipamento> lista = new ArrayList<>();
	    
	    if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
	        return lista; 
	    }
	    
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    try {
	        conn = Conexao.conectar();

	        StringBuilder sql = new StringBuilder(
	            "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, " +
	            "se.nome AS status_nome, se.cor AS status_cor, " +
	            "sit.nome AS situacao_nome, " +
	            "mc.id_status_chamado AS status_chamado_id, " +
	            "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo, " +
	            "(f.origem_codigo || '-' || f.sufixo) AS nome_origem, " +
	            "d.nome_departamento AS nome_departamento " +
	            "FROM equipamentos e " +
	            "INNER JOIN produtos p ON e.id_produto = p.id " +
	            "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
	            "LEFT JOIN filiais f ON e.origem_codigo = f.origem_codigo " +
	            "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
	            "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
	            "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
	            "LEFT JOIN ( " +
	            "    SELECT m1.id_equipamento, m1.id_status_chamado, " +
	            "    ROW_NUMBER() OVER(PARTITION BY m1.id_equipamento ORDER BY m1.id_chamado DESC) as rn " + 
	            "    FROM manutencao_chamados m1 " +
	            ") mc ON mc.id_equipamento = e.id_equipamento AND mc.rn = 1 " +
	            "WHERE 1=1"
	        );
	        
	        List<Object> parametros = new ArrayList<>();
	        
	        sql.append(" AND e.origem_codigo IN (");
	        for (int i = 0; i < unidadesPermitidas.size(); i++) {
	            sql.append(i == 0 ? "?" : ", ?");
	            parametros.add(unidadesPermitidas.get(i));
	        }
	        sql.append(")");

	        if ((statusIdFiltro == null || statusIdFiltro.trim().isEmpty()) && (pesquisaGlobal == null || pesquisaGlobal.trim().isEmpty())) {
	            sql.append(" AND e.status_id != 3");
	        }

	        if (pesquisaGlobal != null && !pesquisaGlobal.trim().isEmpty()) {
	            // CORRIGIDO: Substituído f.nome_empresa por f.sufixo e f.origem_codigo para evitar erro de coluna
	            sql.append(" AND (e.id_sistema ILIKE ? OR e.patrimonio ILIKE ? OR e.numero_serie ILIKE ? OR e.usuario_atual ILIKE ? OR e.nome_identificador ILIKE ? OR p.modelo ILIKE ? OR p.codigo_catalogo ILIKE ? OR f.sufixo ILIKE ? OR CAST(f.origem_codigo AS TEXT) ILIKE ? OR d.nome_departamento ILIKE ?)");
	            String termoGlobal = "%" + pesquisaGlobal.trim() + "%";
	            for (int i = 0; i < 10; i++) { // Ajustado para 10 parâmetros pois adicionamos um campo na busca
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
	                // Ignora
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
	        sql.append(" LIMIT 20");

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
	            
	            int statusChamadoId = rs.getInt("status_chamado_id");
	            eq.setStatusChamadoId(rs.wasNull() ? null : statusChamadoId);

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
	
	public List<Map<String, Object>> listarCamposEspecificacaoPorProduto(int produtoId) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        // Retorna o ID da tabela campos_tipo_produto (ctp.id) para satisfazer a chave estrangeira do equipamento
        String sql = "SELECT ctp.id as campo_id, a.nome as nome_campo " +
                     "FROM campos_tipo_produto ctp " +
                     "INNER JOIN atributos a ON ctp.atributo_id = a.id " +
                     "INNER JOIN produtos p ON p.tipo_id = ctp.tipo_id " +
                     "WHERE p.id = ? ORDER BY ctp.ordem, ctp.id";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> campo = new HashMap<>();
                    campo.put("id", rs.getInt("campo_id"));
                    campo.put("nomeCampo", rs.getString("nome_campo"));
                    lista.add(campo);
                }
            }
        }
        return lista;
    }

    public List<Map<String, Object>> listarCamposComValoresPorEquipamento(int produtoId, int equipamentoId) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT ctp.id as campo_id, a.nome as nome_campo, ee.valor as valor_preenchido " +
                     "FROM campos_tipo_produto ctp " +
                     "INNER JOIN atributos a ON ctp.atributo_id = a.id " +
                     "INNER JOIN produtos p ON p.tipo_id = ctp.tipo_id " +
                     "LEFT JOIN equipamento_especificacoes ee ON ee.campo_id = ctp.id AND ee.id_equipamento = ? " +
                     "WHERE p.id = ? ORDER BY ctp.ordem, ctp.id";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipamentoId);
            stmt.setInt(2, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> campo = new HashMap<>();
                    campo.put("id", rs.getInt("campo_id"));
                    campo.put("nomeCampo", rs.getString("nome_campo"));
                    campo.put("valorPreenchido", rs.getString("valor_preenchido"));
                    lista.add(campo);
                }
            }
        }
        return lista;
    }
    
    public Equipamento buscarPorId(int idEquipamento, List<Integer> unidadesPermitidas) throws SQLException {
		if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
	        return null;
	    }
	    
		// Constrói a query de forma dinâmica para incluir o IN com base nas unidades permitidas
	    StringBuilder sqlEquipamento = new StringBuilder(
	        "SELECT e.*, p.codigo_catalogo, p.modelo, p.descricao_catalogo, " +
	        "m.nome_marca, t.nome as nome_tipo, d.nome_departamento, " +
	        "se.nome AS status_nome, se.cor AS status_cor, sit.nome AS situacao_nome " +
	        "FROM equipamentos e " +
	        "INNER JOIN produtos p ON e.id_produto = p.id " +
	        "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
	        "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
	        "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
	        "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
	        "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
	        "WHERE e.id_equipamento = ? AND e.origem_codigo IN ("
	    );
	    
	    for (int i = 0; i < unidadesPermitidas.size(); i++) {
	        sqlEquipamento.append(i == 0 ? "?" : ", ?");
	    }
	    sqlEquipamento.append(")");
	    
	    String sqlEspecificacoes = "SELECT ee.campo_id, ee.valor, a.nome as nome_campo " +
	                               "FROM equipamento_especificacoes ee " +
	                               "LEFT JOIN atributos a ON ee.campo_id = a.id " +
	                               "WHERE ee.id_equipamento = ?";
	    
	    
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    Equipamento eq = null;
	    
	    try {
	        conn = Conexao.conectar();
	        
	       // 1. Busca os dados principais aplicando o ID e as unidades permitidas
	        stmt = conn.prepareStatement(sqlEquipamento.toString());
	        stmt.setInt(1, idEquipamento);
	        
	       // Seta os valores das unidades permitidas a partir do índice 2
	        for (int i = 0; i < unidadesPermitidas.size(); i++) {
	            stmt.setInt(i + 2, unidadesPermitidas.get(i));
	        }
	        
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
	        
	        rs.close();
	        stmt.close();

	        // 2. Busca as especificações dinâmicas vinculadas
	        if (eq != null) {
	            stmt = conn.prepareStatement(sqlEspecificacoes);
	            stmt.setInt(1, idEquipamento);
	            rs = stmt.executeQuery();
	            
	            List<EspecificacaoEquipamento> listaEsp = new ArrayList<>();
	            while (rs.next()) {
	                EspecificacaoEquipamento esp = new EspecificacaoEquipamento();
	                esp.setCampoId(rs.getInt("campo_id"));
	                esp.setValor(rs.getString("valor"));
	                esp.setNomeCampo(rs.getString("nome_campo"));
	                listaEsp.add(esp);
	            }
	            eq.setEspecificacoes(listaEsp);
	        }
	        
	    } finally {
	        Conexao.fechar(rs, stmt, conn);
	    }
	    return eq;
	}

    public boolean atualizar(Equipamento eq) throws SQLException {
        // Proteção segura contra NullPointerException caso o statusId venha nulo
        Integer statusId = eq.getStatusId();
        if (statusId != null && statusId == 6) {
            eq.setSituacaoId(8);
        }
		
        String sqlEquipamento = "UPDATE equipamentos SET id_produto = ?, patrimonio = ?, numero_serie = ?, nome_identificador = ?, origem_codigo = ?, ip_atual = ?, status_id = ?, situacao_id = ?, usuario_atual = ?, departamento_id = ?, observacoes = ? WHERE id_equipamento = ?";
        
        // Upsert: Se já existe o campo para este equipamento, atualiza o valor; se não existe, insere.
        String sqlUpsertEspecificacao = "INSERT INTO equipamento_especificacoes (id_equipamento, campo_id, valor) VALUES (?, ?, ?) " +
                                        "ON CONFLICT (id_equipamento, campo_id) DO UPDATE SET valor = EXCLUDED.valor";
        
        // Caso o usuário queira explicitamente limpar/apagar o valor de um campo específico que antes estava preenchido
        String sqlDeletarEspecificacaoIndividual = "DELETE FROM equipamento_especificacoes WHERE id_equipamento = ? AND campo_id = ?";

        Connection conn = null;
        PreparedStatement stmtEq = null;
        PreparedStatement stmtEsp = null;
        PreparedStatement stmtDel = null;
        
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // Inicia transação
            
            // 1. Atualiza dados principais do equipamento
            stmtEq = conn.prepareStatement(sqlEquipamento);
            
            // Correção da checagem do ID do produto
            if (eq.getIdProduto() > 0) {
                stmtEq.setInt(1, eq.getIdProduto());
            } else {
                stmtEq.setNull(1, Types.INTEGER);
            }
            
            stmtEq.setString(2, eq.getPatrimonio());
            stmtEq.setString(3, eq.getNumeroSerie());
            stmtEq.setString(4, eq.getNomeIdentificador());
            
            if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
                Integer codigoReal = buscarOrigemCodigoPorIdFilial(conn, eq.getOrigemCodigo());
                stmtEq.setInt(5, codigoReal);
            } else {
                stmtEq.setNull(5, Types.INTEGER);
            }
            
            stmtEq.setString(6, eq.getIpAtual());
            
            // Uso seguro das variáveis encapsuladas
            stmtEq.setInt(7, (statusId != null && statusId > 0) ? statusId : 1);
            stmtEq.setInt(8, (eq.getSituacaoId() != null && eq.getSituacaoId() > 0) ? eq.getSituacaoId() : 1);
            
            stmtEq.setString(9, eq.getUsuarioAtual());
            
            if (eq.getDepartamentoId() != null) {
                stmtEq.setInt(10, eq.getDepartamentoId());
            } else {
                stmtEq.setNull(10, Types.INTEGER);
            }
            
            stmtEq.setString(11, eq.getObservacoes());
            stmtEq.setInt(12, eq.getIdEquipamento());
            stmtEq.executeUpdate();
            
            // 2. Tratamento inteligente das especificações dinâmicas opcionais
            if (eq.getEspecificacoes() != null && !eq.getEspecificacoes().isEmpty()) {
                stmtEsp = conn.prepareStatement(sqlUpsertEspecificacao);
                stmtDel = conn.prepareStatement(sqlDeletarEspecificacaoIndividual);
                
                for (model.EspecificacaoEquipamento esp : eq.getEspecificacoes()) {
                    String valor = esp.getValor();
                    
                    if (valor != null && !valor.trim().isEmpty()) {
                        // Se o usuário digitou algo, faz o Upsert (insere ou atualiza)
                        stmtEsp.setInt(1, eq.getIdEquipamento());
                        stmtEsp.setInt(2, esp.getCampoId());
                        stmtEsp.setString(3, valor.trim());
                        stmtEsp.addBatch();
                    } else {
                        // Se o usuário limpou o campo na tela de edição, removemos aquele registro específico do banco
                        stmtDel.setInt(1, eq.getIdEquipamento());
                        stmtDel.setInt(2, esp.getCampoId());
                        stmtDel.addBatch();
                    }
                }
                stmtEsp.executeBatch();
                stmtDel.executeBatch();
            }
            
            conn.commit(); // Confirma alterações
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            Conexao.fechar(null, stmtEq, null);
            Conexao.fechar(null, stmtDel, null);
            Conexao.fechar(null, stmtEsp, conn);
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
    
   // Conta o total geral considerando a lista de unidades permitidas
    public int contarTotalEquipamentos(List<Integer> unidadesPermitidas) throws SQLException {
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM equipamentos WHERE status_id != 3 AND origem_codigo IN (");
        for (int i = 0; i < unidadesPermitidas.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < unidadesPermitidas.size(); i++) {
                stmt.setInt(i + 1, unidadesPermitidas.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
 // Conta os ativos considerando a lista de unidades permitidas
    public int contarEquipamentosAtivos(List<Integer> unidadesPermitidas) throws SQLException {
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM equipamentos WHERE status_id = 1 AND origem_codigo IN (");
        for (int i = 0; i < unidadesPermitidas.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < unidadesPermitidas.size(); i++) {
                stmt.setInt(i + 1, unidadesPermitidas.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    //Tela que faz parte do dashboard
   // Tela que faz parte do dashboard
    public List<Equipamento> listarPorStatusEUnidades(int statusId, List<Integer> unidadesPermitidas) throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        
        // Retorna lista vazia caso não haja unidades permitidas na sessão
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return lista;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, " +
            "se.nome AS status_nome, se.cor AS status_cor, " +
            "sit.nome AS situacao_nome, " +
            "CASE WHEN m.nome_marca IS NOT NULL AND m.nome_marca <> '' THEN m.nome_marca || ' - ' || p.modelo ELSE p.modelo END AS produto_completo " +
            "FROM equipamentos e " +
            "INNER JOIN produtos p ON e.id_produto = p.id " +
            "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
            "LEFT JOIN status_equipamento se ON e.status_id = se.id " +
            "LEFT JOIN situacao_equipamento sit ON e.situacao_id = sit.id " +
            "WHERE e.status_id = ? AND e.origem_codigo IN ("
        );

        for (int i = 0; i < unidadesPermitidas.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(") ORDER BY e.id_equipamento DESC");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Passa o ID 5 diretamente (Encaminhado p/ Chamado)
            stmt.setInt(1, statusId);
            
            for (int i = 0; i < unidadesPermitidas.size(); i++) {
                stmt.setInt(i + 2, unidadesPermitidas.get(i));
            }
            
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
    
 // Realiza a virada automática do status quando o chamado de manutenção é salvo
    public void atualizarStatusParaEmManutencao(long idEquipamento) throws SQLException {
        String sql = "UPDATE equipamentos SET status_id = 2, situacao_id = 2 WHERE id_equipamento = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEquipamento);
            stmt.executeUpdate();
        }
    }
    
 // Realiza a virada automática do status e situação quando a devolução é iniciada
    public void atualizarStatusParaDevolucao(long idEquipamento) throws SQLException {
        String sql = "UPDATE equipamentos SET status_id = 6, situacao_id = 8 WHERE id_equipamento = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEquipamento);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Reverte o status para 1 (Ativo) e ajusta a situação:
     * - Se a filial for 161 (Matriz): Retorna para 1 (Disponível)
     * - Se for qualquer outra filial: Retorna para 2 (Em Uso)
     */
    public void reverterStatusDevolucao(Long idEquipamento) throws SQLException {
        // Como você utiliza origem_codigo, vamos buscar a filial associada ao equipamento de forma segura via JOIN
        // Se a matriz for origem_codigo = 161, ajustamos para 1 (Disponível), senão 2 (Em Uso).
        
        String sql = "UPDATE equipamentos e " +
                     "SET status_id = 1, " +
                     "    situacao_id = CASE " +
                     "                      WHEN e.origem_codigo = 161 THEN 1 " + // Matriz -> Disponível
                     "                      ELSE 2 " +                              // Outras filiais -> Em Uso
                     "                  END " +
                     "WHERE e.id_equipamento = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEquipamento);
            stmt.executeUpdate();
        }
    }
}