package dao;

import conexao.Conexao;
import model.Fabricante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FabricanteDAO {

    public List<Fabricante> listarTodos() throws SQLException {
        List<Fabricante> lista = new ArrayList<>();
        // Atualizado para buscar todas as colunas de endereço e dados fiscais
        String sql = "SELECT id_fabricante, razao_social, cnpj, inscricao_estadual, cep, logradouro, numero, complemento, bairro, cidade, estado, pais_origem, ativo, data_cadastro FROM fabricantes ORDER BY razao_social ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Fabricante f = new Fabricante();
                f.setIdFabricante(rs.getInt("id_fabricante"));
                f.setRazaoSocial(rs.getString("razao_social"));
                f.setCnpj(rs.getString("cnpj"));
                f.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                f.setCep(rs.getString("cep"));
                f.setLogradouro(rs.getString("logradouro"));
                f.setNumero(rs.getString("numero"));
                f.setComplemento(rs.getString("complemento"));
                f.setBairro(rs.getString("bairro"));
                f.setCidade(rs.getString("cidade"));
                f.setEstado(rs.getString("estado"));
                f.setPaisOrigem(rs.getString("pais_origem"));
                f.setAtivo(rs.getBoolean("ativo"));
                f.setDataCadastro(rs.getTimestamp("data_cadastro"));
                lista.add(f);
            }
        }
        return lista;
    }

    public void salvar(Fabricante fabricante) throws SQLException {
        if (fabricante.getIdFabricante() > 0) {
            atualizar(fabricante);
        } else {
            inserir(fabricante);
        }
    }

    private void inserir(Fabricante fabricante) throws SQLException {
    	String sql = "INSERT INTO fabricantes (razao_social, cnpj, inscricao_estadual, cep, logradouro, numero, complemento, bairro, cidade, estado, pais_origem, ativo, data_cadastro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        	stmt.setString(1, fabricante.getRazaoSocial());
        	stmt.setString(2, fabricante.getCnpj());
        	stmt.setString(3, fabricante.getInscricaoEstadual());
        	stmt.setString(4, fabricante.getCep());
        	stmt.setString(5, fabricante.getLogradouro());
        	stmt.setString(6, fabricante.getNumero());
        	stmt.setString(7, fabricante.getComplemento());
        	stmt.setString(8, fabricante.getBairro());
        	stmt.setString(9, fabricante.getCidade());
        	stmt.setString(10, fabricante.getEstado());
        	stmt.setString(11, fabricante.getPaisOrigem());
        	stmt.setBoolean(12, fabricante.isAtivo());
            stmt.executeUpdate();
        }
    }

    private void atualizar(Fabricante fabricante) throws SQLException {
    	String sql = "UPDATE fabricantes SET razao_social = ?, cnpj = ?, inscricao_estadual = ?, cep = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, pais_origem = ?, ativo = ? WHERE id_fabricante = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        	stmt.setString(1, fabricante.getRazaoSocial());
        	stmt.setString(2, fabricante.getCnpj());
        	stmt.setString(3, fabricante.getInscricaoEstadual());
        	stmt.setString(4, fabricante.getCep());
        	stmt.setString(5, fabricante.getLogradouro());
        	stmt.setString(6, fabricante.getNumero());
        	stmt.setString(7, fabricante.getComplemento());
        	stmt.setString(8, fabricante.getBairro());
        	stmt.setString(9, fabricante.getCidade());
        	stmt.setString(10, fabricante.getEstado());
        	stmt.setString(11, fabricante.getPaisOrigem());
        	stmt.setBoolean(12, fabricante.isAtivo());
        	stmt.setInt(13, fabricante.getIdFabricante());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM fabricantes WHERE id_fabricante = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}