package dao;

import conexao.Conexao;
import model.Marca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {

    public List<Marca> listarTodas() throws SQLException {
        List<Marca> lista = new ArrayList();
        String sql = "SELECT m.id_marca, m.id_fabricante, m.nome_marca, m.logo_url, m.ativo, m.data_cadastro, f.razao_social " +
                     "FROM marcas m " +
                     "JOIN fabricantes f ON m.id_fabricante = f.id_fabricante " +
                     "ORDER BY m.nome_marca ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Marca m = new Marca();
                m.setIdMarca(rs.getInt("id_marca"));
                m.setIdFabricante(rs.getInt("id_fabricante"));
                m.setNomeMarca(rs.getString("nome_marca"));
                m.setLogoUrl(rs.getString("logo_url"));
                m.setAtivo(rs.getBoolean("ativo"));
                m.setDataCadastro(rs.getTimestamp("data_cadastro"));
                m.setNomeFabricante(rs.getString("razao_social"));
                lista.add(m);
            }
        }
        return lista;
    }

    public void salvar(Marca marca) throws SQLException {
        if (marca.getIdMarca() > 0) {
            atualizar(marca);
        } else {
            inserir(marca);
        }
    }

    private void inserir(Marca marca) throws SQLException {
        String sql = "INSERT INTO marcas (id_fabricante, nome_marca, logo_url, ativo, data_cadastro) VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, marca.getIdFabricante());
            stmt.setString(2, marca.getNomeMarca());
            stmt.setString(3, marca.getLogoUrl());
            stmt.setBoolean(4, marca.isAtivo());
            stmt.executeUpdate();
        }
    }

    private void atualizar(Marca marca) throws SQLException {
        String sql = "UPDATE marcas SET id_fabricante = ?, nome_marca = ?, logo_url = ?, ativo = ? WHERE id_marca = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, marca.getIdFabricante());
            stmt.setString(2, marca.getNomeMarca());
            stmt.setString(3, marca.getLogoUrl());
            stmt.setBoolean(4, marca.isAtivo());
            stmt.setInt(5, marca.getIdMarca());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM marcas WHERE id_marca = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            // Lança a exceção adiante para o Servlet tratar a violação de chave estrangeira (23503)
            throw e; 
        }
    }
}