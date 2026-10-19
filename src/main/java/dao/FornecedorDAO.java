package dao;

import conexao.Conexao;
import model.Fornecedor;
import model.FornecedorEndereco;

import java.sql.*;
import java.util.*;

public class FornecedorDAO {

    public long salvar(Fornecedor f) throws SQLException {
        Connection c = null;
        try {
            c = Conexao.conectar();
            c.setAutoCommit(false);
            long id;

            if (f.getIdFornecedor() == null || f.getIdFornecedor() <= 0) {
                String sql = "INSERT INTO public.fornecedores " +
                             "(tipo_pessoa, razao_social, nome_fantasia, documento, inscricao_estadual, " +
                             "inscricao_municipal, id_categoria, contato_principal, telefone, celular, email, site, status, observacao) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_fornecedor";
                
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    preencher(ps, f);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
            } else {
                id = f.getIdFornecedor();
                String sql = "UPDATE public.fornecedores SET " +
                             "tipo_pessoa=?, razao_social=?, nome_fantasia=?, documento=?, inscricao_estadual=?, " +
                             "inscricao_municipal=?, id_categoria=?, contato_principal=?, telefone=?, celular=?, " +
                             "email=?, site=?, status=?, observacao=?, data_atualizacao=CURRENT_TIMESTAMP " +
                             "WHERE id_fornecedor=?";
                
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    preencher(ps, f);
                    ps.setLong(15, id);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Fornecedor não encontrado.");
                    }
                }
            }

            substituirEnderecos(c, id, f.getEnderecos());
            substituirFiliais(c, id, f.getFiliaisIds());
            c.commit();
            return id;

        } catch (SQLException e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignored) {}
                try { c.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void preencher(PreparedStatement ps, Fornecedor f) throws SQLException {
        ps.setString(1, norm(f.getTipoPessoa(), "PJ"));
        ps.setString(2, f.getRazaoSocial() != null ? f.getRazaoSocial().trim() : null);
        ps.setString(3, vazio(f.getNomeFantasia()));
        ps.setString(4, somenteNumeros(f.getDocumento()));
        ps.setString(5, vazio(f.getInscricaoEstadual()));
        ps.setString(6, vazio(f.getInscricaoMunicipal()));
        
        if (f.getIdCategoria() != null) {
            ps.setInt(7, f.getIdCategoria());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        ps.setString(8, vazio(f.getContatoPrincipal()));
        ps.setString(9, vazio(f.getTelefone()));
        ps.setString(10, vazio(f.getCelular()));
        ps.setString(11, vazio(f.getEmail()));
        ps.setString(12, vazio(f.getSite()));
        ps.setString(13, norm(f.getStatus(), "ATIVO"));
        ps.setString(14, vazio(f.getObservacao()));
    }

    private void substituirEnderecos(Connection c, long id, List<FornecedorEndereco> lista) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM public.fornecedor_enderecos WHERE id_fornecedor=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }

        if (lista == null || lista.isEmpty()) {
            return;
        }

        boolean principalEncontrado = false;
        String sql = "INSERT INTO public.fornecedor_enderecos " +
                     "(id_fornecedor, tipo_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, pais, referencia, principal) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (FornecedorEndereco e : lista) {
                boolean p = e.isPrincipal() && !principalEncontrado;
                if (p) {
                    principalEncontrado = true;
                }

                ps.setLong(1, id);
                ps.setString(2, norm(e.getTipoEndereco(), "COMERCIAL"));
                ps.setString(3, vazio(e.getCep()));
                ps.setString(4, vazio(e.getLogradouro()));
                ps.setString(5, vazio(e.getNumero()));
                ps.setString(6, vazio(e.getComplemento()));
                ps.setString(7, vazio(e.getBairro()));
                ps.setString(8, vazio(e.getCidade()));
                ps.setString(9, vazio(e.getUf()));
                ps.setString(10, norm(e.getPais(), "Brasil"));
                ps.setString(11, vazio(e.getReferencia()));
                ps.setBoolean(12, p);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void substituirFiliais(Connection c, long id, List<Integer> ids) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM public.fornecedor_filiais WHERE id_fornecedor=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }

        if (ids == null || ids.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO public.fornecedor_filiais (id_fornecedor, id_filial, ativo) VALUES (?, ?, TRUE)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Set<Integer> unicos = new LinkedHashSet<>(ids);
            for (Integer filial : unicos) {
                if (filial == null || filial <= 0) continue;
                ps.setLong(1, id);
                ps.setInt(2, filial);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Fornecedor buscarPorId(long id) throws SQLException {
        String sql = "SELECT * FROM public.fornecedores WHERE id_fornecedor=?";
        try (Connection c = Conexao.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Fornecedor f = map(rs);
                f.setEnderecos(listarEnderecos(c, id));
                f.setFiliaisIds(listarFiliaisIds(c, id));
                f.setListaFiliaisNomes(buscarFiliaisFormatadasPorFornecedor(c, id)); // <--- ADICIONADO AQUI
                return f;
            }
        }
    }

    public List<Fornecedor> listar(String busca, String documento, String status, Integer idFilial) throws SQLException {
        List<Fornecedor> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT f.*, COUNT(DISTINCT e.id_endereco) qtd_enderecos, COUNT(DISTINCT ff.id_filial) qtd_filiais " +
            "FROM public.fornecedores f " +
            "LEFT JOIN public.fornecedor_enderecos e ON e.id_fornecedor = f.id_fornecedor " +
            "LEFT JOIN public.fornecedor_filiais ff ON ff.id_fornecedor = f.id_fornecedor AND ff.ativo = TRUE " +
            "WHERE 1=1 "
        );

        List<Object> args = new ArrayList<>();

        if (busca != null && !busca.trim().isEmpty()) {
            sql.append(" AND (LOWER(f.razao_social) LIKE LOWER(?) OR LOWER(COALESCE(f.nome_fantasia,'')) LIKE LOWER(?) OR LOWER(COALESCE(f.contato_principal,'')) LIKE LOWER(?)) ");
            String p = "%" + busca.trim() + "%";
            args.add(p);
            args.add(p);
            args.add(p);
        }

        if (documento != null && !documento.trim().isEmpty()) {
            sql.append(" AND regexp_replace(f.documento, '[^0-9]', '', 'g') LIKE ? ");
            args.add("%" + somenteNumeros(documento) + "%");
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND f.status=? ");
            args.add(status.trim().toUpperCase());
        }

        if (idFilial != null && idFilial > 0) {
            sql.append(" AND EXISTS (SELECT 1 FROM public.fornecedor_filiais fx WHERE fx.id_fornecedor = f.id_fornecedor AND fx.id_filial = ? AND fx.ativo = TRUE) ");
            args.add(idFilial);
        }

        sql.append(" GROUP BY f.id_fornecedor ORDER BY f.razao_social ASC");

        try (Connection c = Conexao.conectar(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Fornecedor f = map(rs);
                    f.setQuantidadeEnderecos(rs.getInt("qtd_enderecos"));
                    f.setQuantidadeFiliais(rs.getInt("qtd_filiais"));
                    f.setListaFiliaisNomes(buscarFiliaisFormatadasPorFornecedor(c, f.getIdFornecedor())); // <--- ADICIONADO AQUI
                    out.add(f);
                }
            }
        }
        return out;
    }

    // --- NOVO MÉTODO AUXILIAR PARA FORMATAR AS FILIAIS (origem_codigo + sufixo) ---
    public List<String> buscarFiliaisFormatadasPorFornecedor(Connection c, long idFornecedor) throws SQLException {
        List<String> filiaisFormatadas = new ArrayList<>();
        String sql = "SELECT f.origem_codigo || ' ' || f.sufixo AS filial_str " +
                     "FROM public.fornecedor_filiais ff " +
                     "JOIN public.filiais f ON ff.id_filial = f.id_filial " +
                     "WHERE ff.id_fornecedor = ? AND ff.ativo = TRUE";
                     
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idFornecedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filiaisFormatadas.add(rs.getString("filial_str"));
                }
            }
        }
        return filiaisFormatadas;
    }

    public List<Map<String, Object>> listarFiliais() throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        String sql = "SELECT id_filial, origem_codigo, sufixo, nome_empresa FROM public.filiais ORDER BY nome_empresa";
        
        try (Connection c = Conexao.conectar(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idFilial", rs.getInt("id_filial"));
                m.put("origemCodigo", rs.getInt("origem_codigo"));
                m.put("sufixo", rs.getString("sufixo"));
                m.put("nomeEmpresa", rs.getString("nome_empresa"));
                out.add(m);
            }
        }
        return out;
    }

    public boolean excluir(long id) throws SQLException {
        String sql = "DELETE FROM public.fornecedores WHERE id_fornecedor=?";
        try (Connection c = Conexao.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<FornecedorEndereco> listarEnderecos(Connection c, long id) throws SQLException {
        List<FornecedorEndereco> l = new ArrayList<>();
        String sql = "SELECT * FROM public.fornecedor_enderecos WHERE id_fornecedor=? ORDER BY principal DESC, id_endereco";
        
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FornecedorEndereco e = new FornecedorEndereco();
                    e.setIdEndereco(rs.getLong("id_endereco"));
                    e.setTipoEndereco(rs.getString("tipo_endereco"));
                    e.setCep(rs.getString("cep"));
                    e.setLogradouro(rs.getString("logradouro"));
                    e.setNumero(rs.getString("numero"));
                    e.setComplemento(rs.getString("complemento"));
                    e.setBairro(rs.getString("bairro"));
                    e.setCidade(rs.getString("cidade"));
                    e.setUf(rs.getString("uf"));
                    e.setPais(rs.getString("pais"));
                    e.setReferencia(rs.getString("referencia"));
                    e.setPrincipal(rs.getBoolean("principal"));
                    l.add(e);
                }
            }
        }
        return l;
    }

    private List<Integer> listarFiliaisIds(Connection c, long id) throws SQLException {
        List<Integer> l = new ArrayList<>();
        String sql = "SELECT id_filial FROM public.fornecedor_filiais WHERE id_fornecedor=? AND ativo=TRUE ORDER BY id_filial";
        
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(rs.getInt(1));
                }
            }
        }
        return l;
    }

    private Fornecedor map(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(rs.getLong("id_fornecedor"));
        f.setTipoPessoa(rs.getString("tipo_pessoa"));
        f.setRazaoSocial(rs.getString("razao_social"));
        f.setNomeFantasia(rs.getString("nome_fantasia"));
        f.setDocumento(rs.getString("documento"));
        f.setInscricaoEstadual(rs.getString("inscricao_estadual"));
        f.setInscricaoMunicipal(rs.getString("inscricao_municipal"));
        
        int idCat = rs.getInt("id_categoria");
        f.setIdCategoria(rs.wasNull() ? null : idCat);

        f.setContatoPrincipal(rs.getString("contato_principal"));
        f.setTelefone(rs.getString("telefone"));
        f.setCelular(rs.getString("celular"));
        f.setEmail(rs.getString("email"));
        f.setSite(rs.getString("site"));
        f.setStatus(rs.getString("status"));
        f.setObservacao(rs.getString("observacao"));
        f.setDataCadastro(rs.getTimestamp("data_cadastro"));
        f.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
        return f;
    }

    private String vazio(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    private String norm(String s, String d) {
        String v = vazio(s);
        return v == null ? d : v.toUpperCase();
    }

    private String somenteNumeros(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }
   
    public List<Map<String, Object>> listarCategorias() throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        String sql = "SELECT id_categoria, nome FROM public.fornecedor_categorias WHERE ativo = TRUE ORDER BY nome";
        try (Connection c = Conexao.conectar(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idCategoria", rs.getInt("id_categoria"));
                m.put("nome", rs.getString("nome"));
                out.add(m);
            }
        }
        return out;
    }
}