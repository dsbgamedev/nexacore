package dao;

import conexao.Conexao;
import model.Transportadora;
import model.TransportadoraEndereco;
import model.TransportadoraFilial; // <-- Novo Import

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportadoraDAO {

    /**
     * Salva a transportadora, seus múltiplos endereços e as filiais atendidas usando transação (ACID) e batch.
     */
    public boolean salvarComEnderecosEFiliais(Transportadora t, List<TransportadoraEndereco> enderecos, List<TransportadoraFilial> filiais) throws SQLException {
        String sqlTransp = "INSERT INTO transportadoras (razao_social, nome_fantasia, cnpj, inscricao_estadual, rntrc, telefone, celular, email, site, status, observacao) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_transportadora";
        
        String sqlEnd = "INSERT INTO transportadora_enderecos (id_transportadora, tipo_endereco_id, cep, logradouro, numero, complemento, bairro, cidade, uf, pais, referencia, principal) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_endereco";

        String sqlFilial = "INSERT INTO transportadora_filiais (id_transportadora, id_filial, id_endereco, ativo) " +
                           "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psTransp = null;
        PreparedStatement psEnd = null;
        PreparedStatement psFilial = null;
        ResultSet rs = null;
        boolean sucesso = false;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // Inicia a transação

            // 1. Insere Transportadora
            psTransp = conn.prepareStatement(sqlTransp);
            psTransp.setString(1, t.getRazaoSocial());
            psTransp.setString(2, t.getNomeFantasia());
            psTransp.setString(3, t.getCnpj());
            psTransp.setString(4, t.getInscricaoEstadual());
            psTransp.setString(5, t.getRntrc());
            psTransp.setString(6, t.getTelefone());
            psTransp.setString(7, t.getCelular());
            psTransp.setString(8, t.getEmail());
            psTransp.setString(9, t.getSite());
            psTransp.setString(10, t.getStatus());
            psTransp.setString(11, t.getObservacao());

            rs = psTransp.executeQuery();
            Long idGerado = null;
            if (rs.next()) {
                idGerado = rs.getLong(1);
            }

            if (idGerado == null) {
                conn.rollback();
                return false;
            }

            // 2. Insere os Endereços e guarda os IDs gerados para mapear nas filiais
            List<Long> idsEnderecosGerados = new ArrayList<>();
            if (enderecos != null && !enderecos.isEmpty()) {
                psEnd = conn.prepareStatement(sqlEnd);
                for (TransportadoraEndereco end : enderecos) {
                    psEnd.setLong(1, idGerado);
                    psEnd.setInt(2, end.getTipoEnderecoId());
                    psEnd.setString(3, end.getCep());
                    psEnd.setString(4, end.getLogradouro());
                    psEnd.setString(5, end.getNumero());
                    psEnd.setString(6, end.getComplemento());
                    psEnd.setString(7, end.getBairro());
                    psEnd.setString(8, end.getCidade());
                    psEnd.setString(9, end.getUf());
                    psEnd.setString(10, end.getPais() != null ? end.getPais() : "Brasil");
                    psEnd.setString(11, end.getReferencia());
                    psEnd.setBoolean(12, end.isPrincipal());
                    
                    try (ResultSet rsEnd = psEnd.executeQuery()) {
                        if (rsEnd.next()) {
                            idsEnderecosGerados.add(rsEnd.getLong(1));
                        }
                    }
                }
            }

            // 3. Insere as Filiais Atendidas vinculando o ID da Transportadora e o ID do Endereço correspondente
            if (filiais != null && !filiais.isEmpty()) {
                psFilial = conn.prepareStatement(sqlFilial);
                for (TransportadoraFilial filial : filiais) {
                    int indiceEndereco = filial.getEnderecoIndice(); // Índice selecionado no combo da tela
                    
                    // Valida se o índice do endereço é seguro
                    if (indiceEndereco >= 0 && indiceEndereco < idsEnderecosGerados.size()) {
                        Long idEnderecoReal = idsEnderecosGerados.get(indiceEndereco);

                        psFilial.setLong(1, idGerado);
                        psFilial.setInt(2, filial.getIdFilial());
                        psFilial.setLong(3, idEnderecoReal);
                        psFilial.setBoolean(4, true); // ativo = true por padrão
                        psFilial.addBatch();
                    }
                }
                psFilial.executeBatch();
            }

            conn.commit(); // Confirma todas as alterações na base de dados
            sucesso = true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Desfaz em caso de erro
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (psEnd != null) { try { psEnd.close(); } catch (SQLException e) { e.printStackTrace(); } }
            if (psFilial != null) { try { psFilial.close(); } catch (SQLException e) { e.printStackTrace(); } }
            Conexao.fechar(rs, psTransp, conn);
        }

        return sucesso;
    }

    /**
     * Lista todas as transportadoras cadastradas para exibição na tela de consulta.
     */
    public List<Transportadora> listar() throws SQLException {
        List<Transportadora> lista = new ArrayList<>();
        String sql = "SELECT * FROM transportadoras ORDER BY id_transportadora DESC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transportadora t = new Transportadora();
                t.setIdTransportadora(rs.getLong("id_transportadora"));
                t.setRazaoSocial(rs.getString("razao_social"));
                t.setNomeFantasia(rs.getString("nome_fantasia"));
                t.setCnpj(rs.getString("cnpj"));
                t.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                t.setRntrc(rs.getString("rntrc"));
                t.setTelefone(rs.getString("telefone"));
                t.setCelular(rs.getString("celular"));
                t.setEmail(rs.getString("email"));
                t.setSite(rs.getString("site"));
                t.setStatus(rs.getString("status"));
                t.setObservacao(rs.getString("observacao"));
                lista.add(t);
            }
        }
        return lista;
    }
    
    /**
     * Busca todos os tipos de endereço disponíveis na base de dados.
     */
    public List<Map<String, Object>> listarTiposEndereco() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_endereco, nome FROM tipos_endereco ORDER BY id_tipo_endereco";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> tipo = new HashMap<>();
                tipo.put("id", rs.getInt("id_tipo_endereco"));
                tipo.put("nome", rs.getString("nome"));
                lista.add(tipo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
    //Usado para tela Consulta Transportadora
    public List<Transportadora> listarComFiltros(String pesquisa, String cnpjFiltro, String statusFiltro) throws SQLException {
        List<Transportadora> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transportadoras WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (pesquisa != null && !pesquisa.trim().isEmpty()) {
            sql.append(" AND (LOWER(razao_social) LIKE ? OR LOWER(nome_fantasia) LIKE ?)");
            parametros.add("%" + pesquisa.toLowerCase() + "%");
            parametros.add("%" + pesquisa.toLowerCase() + "%");
        }
        if (cnpjFiltro != null && !cnpjFiltro.trim().isEmpty()) {
            sql.append(" AND cnpj LIKE ?");
            parametros.add("%" + cnpjFiltro.replaceAll("\\D", "") + "%");
        }
        if (statusFiltro != null && !statusFiltro.trim().isEmpty() && !statusFiltro.equalsIgnoreCase("Todos")) {
            sql.append(" AND status = ?");
            parametros.add(statusFiltro);
        }

        sql.append(" ORDER BY id_transportadora DESC");

        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transportadora t = new Transportadora();
                    t.setIdTransportadora(rs.getLong("id_transportadora"));
                    t.setRazaoSocial(rs.getString("razao_social"));
                    t.setNomeFantasia(rs.getString("nome_fantasia"));
                    t.setCnpj(rs.getString("cnpj"));
                    t.setTelefone(rs.getString("telefone"));
                    t.setEmail(rs.getString("email"));
                    t.setStatus(rs.getString("status"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }
    /**
     * Busca uma transportadora específica pelo ID para edição ou visualização.
     */
    public Transportadora buscarPorId(Long id) throws SQLException {
        Transportadora t = null;
        String sql = "SELECT * FROM transportadoras WHERE id_transportadora = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = new Transportadora();
                    t.setIdTransportadora(rs.getLong("id_transportadora"));
                    t.setRazaoSocial(rs.getString("razao_social"));
                    t.setNomeFantasia(rs.getString("nome_fantasia"));
                    t.setCnpj(rs.getString("cnpj"));
                    t.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                    t.setRntrc(rs.getString("rntrc"));
                    t.setTelefone(rs.getString("telefone"));
                    t.setCelular(rs.getString("celular"));
                    t.setEmail(rs.getString("email"));
                    t.setSite(rs.getString("site"));
                    t.setStatus(rs.getString("status"));
                    t.setObservacao(rs.getString("observacao"));
                }
            }
        }
        return t;
    }
    
    /**
     * Lista as filiais vinculadas/atendidas por uma transportadora para exibição no modal de detalhes.
     */
    public List<Map<String, Object>> listarFiliaisPorTransportadora(Long idTransportadora) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT tf.id_filial, f.nome as nome_filial, te.logradouro, te.numero, te.cidade, te.uf " +
                     "FROM transportadora_filiais tf " +
                     "INNER JOIN filiais f ON tf.id_filial = f.id_filial " +
                     "INNER JOIN transportadora_enderecos te ON tf.id_endereco = te.id_endereco " +
                     "WHERE tf.id_transportadora = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTransportadora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("idFilial", rs.getInt("id_filial"));
                    map.put("nomeFilial", rs.getString("nome_filial"));
                    map.put("endereco", rs.getString("logradouro") + ", " + rs.getString("numero") + " - " + rs.getString("cidade") + "/" + rs.getString("uf"));
                    lista.add(map);
                }
            }
        }
        return lista;
    }

    /**
     * Lista os endereços vinculados a uma transportadora (usado na visualização do modal).
     */
    public List<TransportadoraEndereco> listarEnderecosPorTransportadora(Long idTransportadora) throws SQLException {
        List<TransportadoraEndereco> lista = new ArrayList<>();
        String sql = "SELECT te.*, t.nome as nome_tipo_endereco FROM transportadora_enderecos te " +
                     "LEFT JOIN tipos_endereco t ON te.tipo_endereco_id = t.id_tipo_endereco " +
                     "WHERE te.id_transportadora = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTransportadora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TransportadoraEndereco end = new TransportadoraEndereco();
                    end.setIdEndereco(rs.getLong("id_endereco"));
                    end.setTipoEnderecoId(rs.getInt("tipo_endereco_id"));
                    // Se sua classe Model tiver um campo para o nome do tipo (ex: setNomeTipoEndereco), preencha aqui
                    end.setCep(rs.getString("cep"));
                    end.setLogradouro(rs.getString("logradouro"));
                    end.setNumero(rs.getString("numero"));
                    end.setComplemento(rs.getString("complemento"));
                    end.setBairro(rs.getString("bairro"));
                    end.setCidade(rs.getString("cidade"));
                    end.setUf(rs.getString("uf"));
                    end.setPais(rs.getString("pais"));
                    end.setReferencia(rs.getString("referencia"));
                    end.setPrincipal(rs.getBoolean("principal"));
                    lista.add(end);
                }
            }
        }
        return lista;
    }

    /**
     * Exclui uma transportadora (e por cascade ou dependência os endereços/filiais se configurado no banco).
     */
    public boolean excluir(Long id) throws SQLException {
        String sql = "DELETE FROM transportadoras WHERE id_transportadora = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}