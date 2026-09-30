package dao;

import conexao.Conexao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovimentacaoRecebimentoDAO {

    // ============================================================
    // 1. LISTAR ENVIOS EM TRÂNSITO
    // ============================================================

    public List<Map<String, Object>> listarEnviosEmTransito() {

        List<Map<String, Object>> lista = new ArrayList<>();

        String sql =
                "SELECT " +
                "    e.id_envio, " +
                "    e.codigo_rastreio, " +
                "    e.destino_id, " +
                "    COALESCE(orig.origem_codigo || ' - ' || orig.sufixo, " +
                "             'Origem #' || e.origem_id) AS origem_nome, " +
                "    COALESCE(dest.origem_codigo || ' - ' || dest.sufixo, " +
                "             'Filial Destino #' || e.destino_id) AS destino_nome " +
                "FROM movimentacao_envio e " +
                "LEFT JOIN filiais orig ON orig.id_filial = e.origem_id " +
                "LEFT JOIN filiais dest ON dest.id_filial = e.destino_id " +
                "WHERE e.status_id = 2 " +
                "  AND (e.codigo_rastreio IS NULL " +
                "       OR e.codigo_rastreio NOT LIKE 'DEV-%') " +
                "ORDER BY e.id_envio DESC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> map = new HashMap<>();

                map.put("idEnvio", rs.getLong("id_envio"));
                map.put("codigoRastreio", rs.getString("codigo_rastreio"));
                map.put("destinoId", rs.getLong("destino_id"));
                map.put("origemNome", rs.getString("origem_nome"));
                map.put("destinoNome", rs.getString("destino_nome"));

                lista.add(map);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    // ============================================================
    // 2. DETALHES DO ENVIO
    // ============================================================
    public Map<String, Object> buscarDetalhesEnvio(int idEnvio) {

        Map<String, Object> resultado = new HashMap<>();

        String sqlEnvio =
                "SELECT " +
                "    e.id_envio, " +
                "    e.origem_id, " +
                "    e.destino_id, " +
                "    e.transportadora, " +
                "    e.codigo_rastreio, " +
                "    e.data_envio, " +
                "    e.data_previsa_entrega, " +
                "    e.observacoes, " +
                "    e.status_id, " +
                "    e.numero_nota, " +
                "    COALESCE(orig.origem_codigo || ' - ' || orig.sufixo, " +
                "             'Origem #' || e.origem_id) AS origem_nome, " +
                "    COALESCE(dest.origem_codigo || ' - ' || dest.sufixo, " +
                "             'Destino #' || e.destino_id) AS destino_nome " +
                "FROM movimentacao_envio e " +
                "LEFT JOIN filiais orig ON orig.id_filial = e.origem_id " +
                "LEFT JOIN filiais dest ON dest.id_filial = e.destino_id " +
                "WHERE e.id_envio = ?";

        String sqlItens =
                "SELECT " +
                "    eei.id_equipamento, " +
                "    eq.patrimonio, " +
                "    eq.nome_identificador, " +
                "    eq.numero_serie " +
                "FROM movimentacao_envio_itens eei " +
                "INNER JOIN equipamentos eq " +
                "        ON eq.id_equipamento = eei.id_equipamento " +
                "WHERE eei.id_envio = ? " +
                "ORDER BY eei.id_equipamento";

        try (Connection conn = Conexao.conectar()) {

            // ----------------------------------------------------
            // Dados do envio
            // ----------------------------------------------------
            try (PreparedStatement stmt = conn.prepareStatement(sqlEnvio)) {
                stmt.setInt(1, idEnvio);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return resultado;
                    }
                    resultado.put("idEnvio", rs.getLong("id_envio"));
                    resultado.put("origemId", rs.getLong("origem_id"));
                    resultado.put("destinoId", rs.getLong("destino_id"));
                    resultado.put("origemNome", rs.getString("origem_nome"));
                    resultado.put("destinoNome", rs.getString("destino_nome"));
                    resultado.put("transportadora",rs.getString("transportadora"));
                    resultado.put("codigoRastreio", rs.getString("codigo_rastreio"));
                    resultado.put("numeroNota", rs.getString("numero_nota"));
                    resultado.put("observacoes", rs.getString("observacoes"));
                    resultado.put("statusId", rs.getLong("status_id"));
                }
            }
            // ----------------------------------------------------
            // Equipamentos do envio
            // ----------------------------------------------------
            List<Map<String, Object>> itens = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {

                stmt.setInt(1, idEnvio);

                try (ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {

                        Map<String, Object> item = new HashMap<>();

                        long idEquipamento =rs.getLong("id_equipamento");
                        item.put("idSistema","EQ" + String.format("%07d", idEquipamento));
                        item.put("idEquipamento", idEquipamento);
                        item.put("patrimonio", rs.getString("patrimonio"));
                        item.put("nomeCpu", rs.getString("nome_identificador"));
                        item.put("produto","Equipamento");
                        item.put("numeroSerie", rs.getString("numero_serie"));
                        itens.add(item);
                    }
                }
            }
            resultado.put("itens", itens);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
    // ============================================================
    // 3. REGISTRAR RECEBIMENTO DE ENVIO
    // ============================================================
    public boolean registrarRecebimento(
            int idEnvio,
            String dataRecebimento,
            String responsavel,
            String condicaoGeral,
            String caminhoComprovante) {

    	Connection conn = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);
            // ----------------------------------------------------
            // 1. Atualiza o envio SOMENTE se ainda estiver aguardando ou em trânsito.
            //    Isso também funciona como trava contra: duas abas, duplo clique e usuário tentando receber novamente
            // ----------------------------------------------------
            String sqlAtualizaEnvio = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ? AND status_id IN (1, 2)";                    
            int linhasAtualizadas;
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaEnvio)) {
                stmt.setInt(1, idEnvio);
                linhasAtualizadas = stmt.executeUpdate();
            }
            if (linhasAtualizadas == 0) {
                throw new SQLException("Ação negada: o envio não existe, já foi recebido ou foi cancelado.");
            }
            // ----------------------------------------------------
            // 2. Busca o código da filial de destino
            // ----------------------------------------------------
            String sqlDestino = "SELECT f.origem_codigo FROM movimentacao_envio e INNER JOIN filiais f ON f.id_filial = e.destino_id " 
            					+"WHERE e.id_envio = ?";

            int origemCodigoDestino;
            try (PreparedStatement stmt = conn.prepareStatement(sqlDestino)) {
                stmt.setInt(1, idEnvio);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Filial de destino não encontrada para o envio."
                        );
                    }
                    origemCodigoDestino = rs.getInt("origem_codigo");
                }
            }
            // ----------------------------------------------------
            // 3. Registra o recebimento
            // ----------------------------------------------------

            String sqlRecebimento = "INSERT INTO movimentacao_recebimento (id_envio, data_recebimento, responsavel_recebimento,"
            		+ " condicao_geral, comprovante) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecebimento)) {
                stmt.setInt(1, idEnvio);
                stmt.setDate(2, Date.valueOf(dataRecebimento));
                stmt.setString(3, responsavel);
                stmt.setString(4, condicaoGeral);
                if (caminhoComprovante != null && !caminhoComprovante.trim().isEmpty()) {
                    stmt.setString(5, caminhoComprovante);
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                }
                stmt.executeUpdate();
            }
            // ----------------------------------------------------
            // 4. Histórico
            // ----------------------------------------------------

            String sqlHistorico = "INSERT INTO movimentacao_historico (id_envio, status_id, data_hora, observacao) VALUES (?, 3, NOW(), ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlHistorico)) {
                stmt.setInt(1, idEnvio);
                stmt.setString(2,"Recebimento do envio ID #" + idEnvio + " confirmado por " + responsavel + ". Condição: "
                + (condicaoGeral != null ? condicaoGeral : "Não informada"));
                stmt.executeUpdate();
            }
            // ----------------------------------------------------
            // 5. Atualiza TODOS os equipamentos de uma vez
            // Antes: SELECT item UPDATE equipamento SELECT item UPDATE equipamento
            //Agora: UM UPDATE
            // ----------------------------------------------------
            String sqlAtualizaEquipamentos =
                    "UPDATE equipamentos " +
                    "SET origem_codigo = ?, " +
                    "    situacao_id = 7 " +
                    "WHERE id_equipamento IN " +
                    "( " +
                    "    SELECT id_equipamento " +
                    "    FROM movimentacao_envio_itens " +
                    "    WHERE id_envio = ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaEquipamentos)) {
                stmt.setInt(1, origemCodigoDestino);
                stmt.setInt(2, idEnvio);
                stmt.executeUpdate();
            }

            // ----------------------------------------------------
            // 6. Confirma tudo
            // ----------------------------------------------------
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            throw new RuntimeException("Erro ao registrar recebimento: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // ============================================================
    // 4. LISTAR DEVOLUÇÕES EM TRÂNSITO
    // ============================================================
    public List<Map<String, Object>> listarDevolucoesEmTransito() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql =
                "SELECT " +
                "    e.id_envio, " +
                "    e.codigo_rastreio, " +
                "    e.destino_id, " +
                "    COALESCE(orig.origem_codigo || ' - ' || orig.sufixo, " +
                "             'Origem #' || e.origem_id) AS origem_nome, " +
                "    COALESCE(dest.origem_codigo || ' - ' || dest.sufixo, " +
                "             'Filial Destino #' || e.destino_id) AS destino_nome " +
                "FROM movimentacao_envio e " +
                "LEFT JOIN filiais orig ON orig.id_filial = e.origem_id " +
                "LEFT JOIN filiais dest ON dest.id_filial = e.destino_id " +
                "WHERE e.status_id = 2 " +
                "  AND (e.codigo_rastreio LIKE 'DEV-%' " +
                "       OR e.observacoes ILIKE '%devolução%') " +
                "ORDER BY e.id_envio DESC";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("idEnvio", rs.getLong("id_envio"));
                map.put("codigoRastreio", rs.getString("codigo_rastreio"));
                map.put("destinoId", rs.getLong("destino_id"));
                map.put("origemNome", rs.getString("origem_nome"));
                map.put("destinoNome", rs.getString("destino_nome"));
                lista.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    // ============================================================
    // 5. DETALHES DA DEVOLUÇÃO
    // ============================================================
    public Map<String, Object> buscarDetalhesDevolucao(int idDevolucao) {
        Map<String, Object> resultado = new HashMap<>();

        String sqlDev =
                "SELECT " +
                "    e.id_envio, " +
                "    e.origem_id, " +
                "    e.destino_id, " +
                "    e.transportadora, " +
                "    e.codigo_rastreio, " +
                "    e.status_id, " +
                "    COALESCE(orig.origem_codigo || ' - ' || orig.sufixo, " +
                "             'Origem #' || e.origem_id) AS origem_nome, " +
                "    COALESCE(dest.origem_codigo || ' - ' || dest.sufixo, " +
                "             'Destino #' || e.destino_id) AS destino_nome " +
                "FROM movimentacao_envio e " +
                "LEFT JOIN filiais orig ON orig.id_filial = e.origem_id " +
                "LEFT JOIN filiais dest ON dest.id_filial = e.destino_id " +
                "WHERE e.id_envio = ?";
        String sqlItens =
                "SELECT " +
                "    eei.id_equipamento, " +
                "    eq.patrimonio, " +
                "    eq.nome_identificador, " +
                "    eq.numero_serie " +
                "FROM movimentacao_envio_itens eei " +
                "INNER JOIN equipamentos eq " +
                "        ON eq.id_equipamento = eei.id_equipamento " +
                "WHERE eei.id_envio = ? " +
                "ORDER BY eei.id_equipamento";
        try (Connection conn = Conexao.conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlDev)) {
                stmt.setInt(1, idDevolucao);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return resultado;
                    }
                    resultado.put("idEnvio", rs.getLong("id_envio"));
                    resultado.put("origemNome", rs.getString("origem_nome"));
                    resultado.put("destinoNome", rs.getString("destino_nome"));
                    resultado.put("transportadora", rs.getString("transportadora"));
                    resultado.put("codigoRastreio", rs.getString("codigo_rastreio"));
                    resultado.put("statusId", rs.getLong("status_id"));
                }
            }
            List<Map<String, Object>> itens =  new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {
                stmt.setInt(1, idDevolucao);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> item =  new HashMap<>();
                        long idEquipamento = rs.getLong("id_equipamento");
                        item.put("idSistema","EQ" + String.format("%07d", idEquipamento));
                        item.put("idEquipamento", idEquipamento);
                        item.put("patrimonio", rs.getString("patrimonio"));
                        item.put("nomeCpu", rs.getString("nome_identificador"));
                        item.put("produto","Equipamento");
                        item.put("numeroSerie", rs.getString("numero_serie"));
                        itens.add(item);
                    }
                }
            }
            resultado.put("itens", itens);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
    // ============================================================
    // 6. REGISTRAR RECEBIMENTO DE DEVOLUÇÃO
    // ============================================================
    public boolean registrarRecebimentoDevolucao(
            int idDevolucao,
            String dataRecebimento,
            String responsavel,
            String condicaoGeral,
            String caminhoComprovante) {

        Connection conn = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false);
            // ----------------------------------------------------
            // 1. Atualiza o status da devolução de forma atômica
            // ----------------------------------------------------
            String sqlAtualizaDev = "UPDATE movimentacao_envio SET status_id = 3 WHERE id_envio = ? AND status_id IN (1, 2)";
            int linhasAtualizadas;
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaDev)) {
                stmt.setInt(1, idDevolucao);
                linhasAtualizadas = stmt.executeUpdate();
            }
            if (linhasAtualizadas == 0) {
                throw new SQLException("Ação negada: esta devolução não existe, já foi finalizada ou foi cancelada.");
            }
            // ----------------------------------------------------
            // 2. Busca a filial de destino
            // ----------------------------------------------------
            String sqlDestino ="SELECT f.origem_codigo " +
                    "FROM movimentacao_envio e " +
                    "INNER JOIN filiais f " +
                    "        ON f.id_filial = e.destino_id " +
                    "WHERE e.id_envio = ?";
            int origemCodigoDestino;
            try (PreparedStatement stmt = conn.prepareStatement(sqlDestino)) {
                stmt.setInt(1, idDevolucao);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Filial de destino não encontrada.");
                    }
                    origemCodigoDestino = rs.getInt("origem_codigo");
                }
            }
            // ----------------------------------------------------
            // 3. Registra recebimento
            // ----------------------------------------------------
            String sqlRecebimento =
                    "INSERT INTO movimentacao_recebimento " +
                    "(id_envio, data_recebimento, " +
                    " responsavel_recebimento, condicao_geral, comprovante) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRecebimento)) {
                stmt.setInt(1, idDevolucao);
                stmt.setDate(2, Date.valueOf(dataRecebimento));
                stmt.setString(3, responsavel);
                stmt.setString(4, condicaoGeral);
                if (caminhoComprovante != null && !caminhoComprovante.trim().isEmpty()) {
                    stmt.setString(5, caminhoComprovante);
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                }
                stmt.executeUpdate();
            }
            // ----------------------------------------------------
            // 4. Histórico
            // ----------------------------------------------------
            String sqlHistorico =
                    "INSERT INTO movimentacao_historico " +
                    "(id_envio, status_id, data_hora, observacao) " +
                    "VALUES (?, 3, NOW(), ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlHistorico)) {
                stmt.setInt(1, idDevolucao);
                stmt.setString(2, "Recebimento da devolução ID #" + idDevolucao + " confirmado por " + responsavel + ". Condição: "
                        + (condicaoGeral != null ? condicaoGeral : "Não informada"));
                stmt.executeUpdate();
            }
            // ----------------------------------------------------
            // 5. Atualiza todos os equipamentos de uma vez
            // ----------------------------------------------------
            String sqlAtualizaEquipamentos =
                    "UPDATE equipamentos " +
                    "SET status_id = 1, " +
                    "    origem_codigo = ?, " +
                    "    situacao_id = 1 " +
                    "WHERE id_equipamento IN " +
                    "( " +
                    "    SELECT id_equipamento " +
                    "    FROM movimentacao_envio_itens " +
                    "    WHERE id_envio = ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaEquipamentos)) {
                stmt.setInt(1, origemCodigoDestino);
                stmt.setInt(2, idDevolucao);
                stmt.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            throw new RuntimeException("Erro ao registrar recebimento da devolução: " + e.getMessage(),e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // ============================================================
    // 7. BUSCAR DESTINO DA MOVIMENTAÇÃO
    // ============================================================
    public int buscarDestinoIdPorMovimentacao(int idMovimentacao) {
        String sql ="SELECT destino_id FROM movimentacao_envio WHERE id_envio = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMovimentacao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("destino_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    // ============================================================
    // 8. TOTAL RECEBIDO HOJE
    // IMPORTANTE:unidadesPermitidas contém ORIGEM_CODIGO.
    // Não fazemos mais: FilialDAO.buscarIdFilialPorOrigemCodigo(...) para cada unidade.
    // O próprio JOIN filiais resolve isso.
    // ============================================================
    public int contarRecebidosHojePorUnidades(List<Integer> unidadesPermitidas) {
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return 0;
        }
        String inClause = gerarPlaceholders(unidadesPermitidas.size());
        String sql =
                "SELECT COUNT(DISTINCT e.id_envio) " +
                "FROM movimentacao_recebimento r " +
                "INNER JOIN movimentacao_envio e " +
                "        ON e.id_envio = r.id_envio " +
                "INNER JOIN filiais f " +
                "        ON f.id_filial = e.destino_id " +
                "WHERE e.status_id = 3 " +
                "  AND f.origem_codigo IN (" +
                inClause +
                ") " +
                "  AND r.data_recebimento = CURRENT_DATE";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherInteiros(stmt, unidadesPermitidas, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    // ============================================================
    // 9. TOTAL AGUARDANDO RECEBIMENTO
    // ============================================================
    public int contarPendentesRecebimentoPorUnidades(List<Integer> unidadesPermitidas) {
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return 0;
        }
        String inClause = gerarPlaceholders(unidadesPermitidas.size());
        String sql ="SELECT COUNT(*) FROM movimentacao_envio e INNER JOIN filiais f ON f.id_filial = e.destino_id WHERE e.status_id IN (1, 2) " +
                "  AND f.origem_codigo IN (" + inClause +")";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherInteiros(stmt, unidadesPermitidas, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    /**
     * Gera:
     *
     * ?, ?, ?, ?
     *
     * conforme a quantidade recebida.
     */
    private String gerarPlaceholders(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade de parâmetros inválida.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quantidade; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        return sb.toString();
    }
    /**
     * Preenche os parâmetros de uma lista de Integer.
     */
    private void preencherInteiros(PreparedStatement stmt, List<Integer> valores, int parametroInicial) throws SQLException {
        int indice = parametroInicial;
        for (Integer valor : valores) {
            stmt.setInt(indice++, valor);
        }
    }
}