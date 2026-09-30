package dao;

import conexao.Conexao;
import model.Transportadora;
import model.TransportadoraEndereco;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportadoraDAO {

    /**
     * Salva a transportadora e seus múltiplos endereços usando transação (ACID) e batch.
     */
    public boolean salvarComEnderecos(Transportadora t, List<TransportadoraEndereco> enderecos) throws SQLException {
        String sqlTransp = "INSERT INTO transportadoras (razao_social, nome_fantasia, cnpj, inscricao_estadual, rntrc, telefone, celular, email, site, status, observacao) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_transportadora";
        
        String sqlEnd = "INSERT INTO transportadora_enderecos (id_transportadora, tipo_endereco_id, cep, logradouro, numero, complemento, bairro, cidade, uf, pais, referencia, principal) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psTransp = null;
        PreparedStatement psEnd = null;
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

            // 2. Insere os Endereços vinculados em lote (Batch)
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
                    psEnd.addBatch();
                }
                psEnd.executeBatch();
            }

            conn.commit(); // Confirma as alterações
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
            if (psEnd != null) {
                try { psEnd.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
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

        // Substitua 'ConexaoBanco.getConexao()' pela forma como obtém a conexão no seu projeto
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
}
