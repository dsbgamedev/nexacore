package dao;

import conexao.Conexao;
import dto.FreteConsultaFiltro;
import model.Frete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO específico para realizar a consulta de fretes com filtros dinâmicos,
 * incluindo paginação e um novo método para exportação por streaming (ResultSet).
 */
public class ConsultaFreteDAO {

    private static final Logger LOGGER = Logger.getLogger(ConsultaFreteDAO.class.getName());
    
    // SQL base (os filtros serão adicionados dinamicamente)
    private static final String SQL_SELECT_BASE = 
	    "SELECT f.id, f.id_filial, f.data_lancamento, f.hora_lancamento, f.id_transportadora, " +
	    "COALESCE(NULLIF(t.nome_fantasia, ''), t.razao_social) AS nome_transportadora, " +
	    "f.nota_fiscal, f.volume, f.peso_liquido, f.valor_nf, f.observacao, f.nome_empresa, f.nome_cliente, f.porcentagem_frete, f.valor_frete " +
	    "FROM frete f " +
	    "LEFT JOIN transportadoras t ON f.id_transportadora = t.id_transportadora " +
	    "WHERE 1=1 ";

   // SQL para COUNT e Agregados
    private static final String SQL_COUNT = 
        "SELECT COUNT(*) AS total_registros FROM frete f LEFT JOIN transportadoras t ON f.id_transportadora = t.id_transportadora WHERE 1=1 ";
        
    private static final String SQL_AGREGADOS = 
        "SELECT SUM(f.volume) AS total_volume, SUM(f.peso_liquido) AS total_peso_liquido, SUM(f.valor_nf) AS total_valor_nf, SUM(f.valor_frete) AS total_valor_frete " +
        "FROM frete f LEFT JOIN transportadoras t ON f.id_transportadora = t.id_transportadora WHERE 1=1 ";


    /**
     * Método auxiliar para adicionar cláusulas WHERE dinâmicas e preencher a lista de parâmetros.
     */
    private void adicionarFiltros(StringBuilder sqlBuilder, FreteConsultaFiltro filtro, List<Object> parametros) {
        
    	// --- FILTRO OBRIGATÓRIO: FILIAL ATIVA ---
        if (filtro.getIdFilial() != null) {
            sqlBuilder.append(" AND f.id_filial = ?");
            parametros.add(filtro.getIdFilial());
        }
    	
        // 1. Filtro por Período de Data
        if (filtro.getDataInicio() != null && !filtro.getDataInicio().trim().isEmpty()) {
            sqlBuilder.append(" AND f.data_lancamento >= ?");
            parametros.add(filtro.getDataInicio()); 
        }

        if (filtro.getDataFim() != null && !filtro.getDataFim().trim().isEmpty()) {
            sqlBuilder.append(" AND f.data_lancamento <= ?");
            parametros.add(filtro.getDataFim());
        }
        
        // 2. Filtro por Transportadora
        if (filtro.getIdTransportadora() != null && filtro.getIdTransportadora() > 0) {
            sqlBuilder.append(" AND f.id_transportadora = ?");
            parametros.add(filtro.getIdTransportadora());
        }
        
        // 3. NOVO: Filtro por Termo de Pesquisa com Seleção de Coluna
        if (filtro.getTermoPesquisa() != null && !filtro.getTermoPesquisa().trim().isEmpty()) {
            String termoLike = "%" + filtro.getTermoPesquisa().toLowerCase() + "%";
            String campo = filtro.getCampoPesquisa(); // Valor que vem do <select>

         // Se for "todos" ou nulo, mantém a busca global
            if (campo == null || campo.equals("todos")) {
                sqlBuilder.append(" AND (LOWER(t.nome_fantasia) LIKE ? OR LOWER(t.razao_social) LIKE ? OR LOWER(f.nota_fiscal) LIKE ? OR LOWER(f.nome_cliente) LIKE ? OR LOWER(f.nome_empresa) LIKE ? OR LOWER(f.observacao) LIKE ?)");
                for (int i = 0; i < 6; i++) parametros.add(termoLike);
            } 
            else {
                switch (campo) {
                    case "notaFiscal":
                        sqlBuilder.append(" AND LOWER(f.nota_fiscal) LIKE ?");
                        break;
                    case "nomeTransportadora":
                        sqlBuilder.append(" AND (LOWER(t.nome_fantasia) LIKE ? OR LOWER(t.razao_social) LIKE ?)");
                        parametros.add(termoLike); // Adiciona um extra para o OR acima
                        break;
                    case "nomeCliente":
                        sqlBuilder.append(" AND LOWER(f.nome_cliente) LIKE ?");
                        break;
                    case "nomeEmpresa":
                        sqlBuilder.append(" AND LOWER(f.nome_empresa) LIKE ?");
                        break;
                    case "observacao":
                        sqlBuilder.append(" AND LOWER(f.observacao) LIKE ?");
                        break;
                }
                parametros.add(termoLike);
            }
        }
    }
    
    /**
     * Define os parâmetros no PreparedStatement.
     */
    private void setStatementParameters(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object param = parametros.get(i);
            int parameterIndex = i + 1;

            if (param == null) {
                stmt.setNull(parameterIndex, java.sql.Types.NULL);
            } else if (param instanceof String) {
                String strParam = (String) param;
                // Verifica se a string segue o padrão YYYY-MM-DD (4 dígitos - 2 dígitos - 2 dígitos)
                if (strParam.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        // Se parece data, converte para java.sql.Date
                        stmt.setDate(parameterIndex, Date.valueOf(strParam));
                    } catch (IllegalArgumentException e) {
                        // Se falhar a conversão por algum motivo, envia como String
                        stmt.setString(parameterIndex, strParam);
                    }
                } else {
                    stmt.setString(parameterIndex, strParam);
                }
            } else if (param instanceof Integer) {
                stmt.setInt(parameterIndex, (Integer) param);
            } else if (param instanceof Date) {
                stmt.setDate(parameterIndex, (Date) param);
            } else if (param instanceof Double) {
                stmt.setDouble(parameterIndex, (Double) param);
            } else {
                stmt.setObject(parameterIndex, param);
            }
        }
    }

    /**
     * Implementa a Paginação, Ordenação e Totais Agregados (para a tela de consulta).
     */
    public Map<String, Object> consultarComPaginacaoEAgregados(FreteConsultaFiltro filtro, int offset, int limit) throws SQLException {
        
        LOGGER.log(Level.INFO, "DEBUG (DAO): Iniciando consulta PAGINADA. Offset: {0}, Limit: {1}", new Object[]{offset, limit});

        Map<String, Object> resultado = new HashMap<>();
        List<Object> parametros = new ArrayList<>();
        
        // --- 1. CONSTRUÇÃO DA CLÁUSULA WHERE (Comum a todas as consultas) ---
        StringBuilder whereClause = new StringBuilder();
        adicionarFiltros(whereClause, filtro, parametros);
        
        int totalRegistros = 0;
        
        // --------------------------------------------------------------------------------
        // --- 2. CONSULTA 1: COUNT (Total de Registros) ---
        // --------------------------------------------------------------------------------
        
        String sqlCount = SQL_COUNT + whereClause.toString();
        LOGGER.log(Level.FINE, "DEBUG (DAO): SQL COUNT: {0}", sqlCount);

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmtCount = conn.prepareStatement(sqlCount)) {
            
            setStatementParameters(stmtCount, parametros);

            try (ResultSet rsCount = stmtCount.executeQuery()) {
                if (rsCount.next()) {
                    totalRegistros = rsCount.getInt("total_registros");
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao obter COUNT de fretes.");
            throw e;
        }
        resultado.put("totalRegistros", totalRegistros);
        
        if (totalRegistros == 0) {
            resultado.put("fretes", Collections.emptyList());
            resultado.put("totalVolume", 0.0);
            resultado.put("totalPesoLiquido", 0.0);
            resultado.put("totalValorNf", 0.0);
            resultado.put("totalValorFrete", 0.0);
            return resultado;
        }

        // --------------------------------------------------------------------------------
        // --- 3. CONSULTA 2: AGREGADOS (Somas para o Rodapé) ---
        // --------------------------------------------------------------------------------
        
        String sqlAgregados = SQL_AGREGADOS + whereClause.toString();
        LOGGER.log(Level.FINE, "DEBUG (DAO): SQL AGREGADOS: {0}", sqlAgregados);

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmtAgregados = conn.prepareStatement(sqlAgregados)) {
            
            setStatementParameters(stmtAgregados, parametros);

            try (ResultSet rsAgregados = stmtAgregados.executeQuery()) {
                if (rsAgregados.next()) {
                    resultado.put("totalVolume", rsAgregados.getDouble("total_volume"));
                    resultado.put("totalPesoLiquido", rsAgregados.getDouble("total_peso_liquido"));
                    resultado.put("totalValorNf", rsAgregados.getDouble("total_valor_nf"));
                    resultado.put("totalValorFrete", rsAgregados.getDouble("total_valor_frete"));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao obter totais AGREGADOS de fretes.");
            throw e;
        }


        // --------------------------------------------------------------------------------
        // --- 4. CONSULTA 3: DADOS PAGINADOS (A lista de 50 Fretes) ---
        // --------------------------------------------------------------------------------

        List<Frete> listaFretes = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(SQL_SELECT_BASE).append(whereClause);
        
        // --- ORDENAÇÃO DINÂMICA ---
        String sortColumn = filtro.getSortColumn(); 
        String sortDirection = filtro.getSortDirection(); 
        
        if (sortColumn != null && !sortColumn.trim().isEmpty()) {
            String dbColumn;
            switch(sortColumn) {
            	case "id": default: dbColumn = "f.id"; break;
                case "dataLancamento": dbColumn = "f.data_lancamento"; break;
                case "horaLancamento": dbColumn = "f.hora_lancamento"; break;
                case "nomeEmpresa": dbColumn = "f.nome_empresa"; break;
                case "nomeTransportadora": dbColumn = "COALESCE(NULLIF(t.nome_fantasia, ''), t.razao_social)"; break;
                case "nomeCliente": 
                    // No PostgreSQL, usamos LOWER para ignorar se está em maiúsculo ou minúsculo
                    dbColumn = "LOWER(f.nome_cliente)"; 
                    break;
             // Dentro do método consultarComPaginacaoEAgregados no seu ConsultaFreteDAO
                case "notaFiscal": 
                    // PostgreSQL: Usamos INTEGER e REGEXP_REPLACE para limpar letras se existirem
                    dbColumn = "CAST(NULLIF(REGEXP_REPLACE(f.nota_fiscal, '[^0-9]', '', 'g'), '') AS INTEGER)"; 
                    break;
                case "volume": 
                    // Garantimos que o banco trate como número
                    dbColumn = "CAST(f.volume AS INTEGER)"; 
                    break; 
                case "pesoLiquido": 
                    dbColumn = "f.peso_liquido"; 
                    break;
                case "valorNf": 
                    dbColumn = "f.valor_nf"; 
                    break;
                case "porcentagemFrete": 
                    dbColumn = "f.porcentagem_frete"; 
                    break;    
                case "valorFrete": dbColumn = "f.valor_frete"; break;
               // case "id": default: dbColumn = "f.data_lancamento"; break;
              // --- ADICIONADO 29.04.2026 PARA ORDENAR CAMPO OBSERVACAO
                case "observacao": dbColumn = "LOWER(f.observacao)"; break; 
               
            }
            
            String direction = (sortDirection != null && sortDirection.equalsIgnoreCase("asc")) ? "ASC" : "DESC";

            sqlBuilder.append(" ORDER BY ").append(dbColumn).append(" ").append(direction);
        } else {
             sqlBuilder.append(" ORDER BY f.data_lancamento DESC, f.hora_lancamento DESC");
        }
        
        // --- ADICIONA LIMIT e OFFSET ---
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        
        // Adiciona os parâmetros LIMIT e OFFSET (após todos os parâmetros do WHERE)
        parametros.add(limit);
        parametros.add(offset);


        String sqlFinal = sqlBuilder.toString();
        LOGGER.log(Level.INFO, "DEBUG (DAO): SQL DADOS PAGINADOS: {0}", sqlFinal);

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmtFinal = conn.prepareStatement(sqlFinal)) {

            setStatementParameters(stmtFinal, parametros);

            try (ResultSet rs = stmtFinal.executeQuery()) {
                while (rs.next()) {
                    listaFretes.add(preencherFreteComTransportadora(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao consultar DADOS PAGINADOS de fretes.");
            e.printStackTrace();
            throw e; 
        }
        
        resultado.put("fretes", listaFretes);
        LOGGER.log(Level.INFO, "DEBUG (DAO): Consulta retornou {0} fretes (página atual).", listaFretes.size());
        
        return resultado;
    }
    
    
    // --------------------------------------------------------------------------------
    // --- CONSULTAR LISTA COMPLETA (Não usa streaming, pode ser problema se for grande) ---
    // --------------------------------------------------------------------------------

    /**
     * Consulta TODOS os fretes que atendem ao filtro, sem paginação.
     * Este método é usado pelo ExportServlet para gerar relatórios completos (Excel/PDF).
     * @param filtro Objeto DTO com os critérios de busca.
     * @return Lista de objetos Frete que correspondem aos filtros.
     */
    public List<Frete> consultarPorFiltro(FreteConsultaFiltro filtro) throws SQLException {
        
        List<Frete> listaFretes = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder(SQL_SELECT_BASE);
        adicionarFiltros(sqlBuilder, filtro, parametros); // Reutiliza a lógica WHERE
        
        // Ordenação Padrão (Importante para a exportação)
        sqlBuilder.append(" ORDER BY f.data_lancamento DESC, f.hora_lancamento DESC");
        
        String sqlFinal = sqlBuilder.toString();
        LOGGER.log(Level.INFO, "DEBUG (DAO - Export): SQL para Exportação (Completa): {0}", sqlFinal);

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sqlFinal)) {

            setStatementParameters(stmt, parametros);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    listaFretes.add(preencherFreteComTransportadora(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO ao consultar dados para Exportação.");
            e.printStackTrace();
            throw e; 
        }
        
        LOGGER.log(Level.INFO, "DEBUG (DAO - Export): Consulta completa retornou {0} fretes.", listaFretes.size());
        return listaFretes;
    }
    
    
    // --------------------------------------------------------------------------------
    // --- MÉTODO CRÍTICO: EXPORTAÇÃO POR STREAMING (CORRIGIDO PARA POSTGRESQL) ---
    // --------------------------------------------------------------------------------

    /**
     * Consulta TODOS os fretes aplicando os filtros do DTO, retornando o ResultSet
     * diretamente para o Servlet. O chamador (ExportServlet) é responsável por
     * fechar o Connection, PreparedStatement e o ResultSet.
     * * @param conn Conexão JDBC aberta (DEVE ter setAutoCommit(false) ativado pelo chamador).
     * @param filtro Objeto DTO com os critérios de busca.
     * @param outStmt Array de PreparedStatement para que o chamador possa fechar o recurso.
     * @return ResultSet aberto com todos os registros para iteração.
     */
    public ResultSet consultarResultSetParaExportacao(Connection conn, FreteConsultaFiltro filtro, PreparedStatement[] outStmt) throws SQLException {
        
        List<Object> parametros = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder(SQL_SELECT_BASE);
        adicionarFiltros(sqlBuilder, filtro, parametros);
        
        // Ordenação Padrão (Importante para a exportação)
        sqlBuilder.append(" ORDER BY f.data_lancamento DESC, f.hora_lancamento DESC");

        String sqlFinal = sqlBuilder.toString();
        LOGGER.log(Level.INFO, "DEBUG (DAO - Export): SQL Final para Streaming: {0}", sqlFinal);

        // Configuração para streaming (necessário para PostgreSQL)
        PreparedStatement stmt = conn.prepareStatement(
            sqlFinal,
            ResultSet.TYPE_FORWARD_ONLY, 
            ResultSet.CONCUR_READ_ONLY
        );
        
        // *** AJUSTE CRÍTICO PARA POSTGRESQL: Define o tamanho do fetch.
        // O driver do PostgreSQL usa este valor para ativar o cursor (streaming) 
        // e funciona SOMENTE se a conexão estiver com autoCommit(false).
        stmt.setFetchSize(1000); 

        // Seta os parâmetros
        setStatementParameters(stmt, parametros);
        
        // Armazena o PreparedStatement no array de saída
        if (outStmt != null && outStmt.length > 0) {
            outStmt[0] = stmt;
        }

        // Executa e retorna o ResultSet (Aberto)
        return stmt.executeQuery();
    }
    
    
    // MANTIDO: Método auxiliar de mapeamento
     /**
     * Método auxiliar para preencher um objeto Frete a partir do ResultSet, 
     * usado para a consulta PAGINADA e COMPLETA.
     */
    private Frete preencherFreteComTransportadora(ResultSet rs) throws SQLException {
        Frete f = new Frete();
        
        f.setId(rs.getInt("id")); 

        // --- CORREÇÃO PARA DATA E HORA JSTL ---
        Date sqlDate = rs.getDate("data_lancamento");
        Time sqlTime = rs.getTime("hora_lancamento");
        
        if (sqlDate != null) {
            // Seta o campo LocalDate
            f.setDataLancamento(sqlDate.toLocalDate()); 
        }
         if (sqlTime != null) {
            // Seta o campo LocalTime
            f.setHoraLancamento(sqlTime.toLocalTime()); 
        }
        
        // Injeta os tipos java.sql.Date/Time nos campos *Jsp via Reflection para JSTL
        try {
            // Data
            Method setDataMethod = f.getClass().getMethod("setDataLancamentoJsp", java.util.Date.class);
            if (setDataMethod != null && sqlDate != null) {
                setDataMethod.invoke(f, sqlDate); 
            }
            // Hora
            Method setHoraMethod = f.getClass().getMethod("setHoraLancamentoJsp", java.util.Date.class);
            if (setHoraMethod != null && sqlTime != null) {
                setHoraMethod.invoke(f, sqlTime);
            }
        } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Falha ao injetar data/hora nos setters *Jsp via Reflection. Erro: " + e.getMessage());
        }
        // ----------------------------------------------------------------------

        // Dados da Transportadora
        f.setIdTransportadora(rs.getInt("id_transportadora"));
        f.setNomeTransportadora(rs.getString("nome_transportadora")); 

        f.setNotaFiscal(rs.getString("nota_fiscal"));
        
        int volume = rs.getInt("volume");
        f.setVolume(rs.wasNull() ? null : volume);
        
        f.setObservacao(rs.getString("observacao"));
        
        f.setNomeEmpresa(rs.getString("nome_empresa")); 
        f.setNomeCliente(rs.getString("nome_cliente")); 
        
        // --- ADICIONE ESTA LINHA AQUI ---30.04.2026
        f.setPorcentagemFrete(rs.getDouble("porcentagem_frete")); 
        // --------------------------------
        
        Double valorFret = rs.getDouble("valor_frete");
        f.setValorFrete(rs.wasNull() ? null : valorFret);

        Double peso = rs.getDouble("peso_liquido");
        f.setPesoLiquido(rs.wasNull() ? null : peso);
        
        Double valor = rs.getDouble("valor_nf");
        f.setValorNf(rs.wasNull() ? null : valor);
        
     // Verifique se o nome da coluna no banco é id_filial ou id_filial
        f.setIdFilial(rs.getInt("id_filial"));
        
        return f;
    }
}