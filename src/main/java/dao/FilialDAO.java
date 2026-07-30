package dao;

import conexao.Conexao;
import model.Filial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilialDAO {

    public boolean inserir(Filial f) throws SQLException {
        String sql = "INSERT INTO filiais (origem_codigo, sufixo, nome_empresa, cnpj, inscricao_estadual, endereco, numero, bairro, municipio, uf, cep, ddd_telefone1, telefone1, ddd_telefone2, telefone2, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, f.getOrigemCodigo());
            stmt.setString(2, f.getSufixo());
            stmt.setString(3, f.getNomeEmpresa());
            stmt.setString(4, f.getCnpj());
            stmt.setString(5, f.getInscricaoEstadual());
            stmt.setString(6, f.getEndereco());
            stmt.setString(7, f.getNumero());
            stmt.setString(8, f.getBairro());
            stmt.setString(9, f.getMunicipio());
            stmt.setString(10, f.getUf());
            stmt.setString(11, f.getCep());
            stmt.setString(12, f.getDddTelefone1());
            stmt.setString(13, f.getTelefone1());
            stmt.setString(14, f.getDddTelefone2());
            stmt.setString(15, f.getTelefone2());
            stmt.setString(16, f.getEmail());
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Filial> listar() throws SQLException {
        List<Filial> lista = new ArrayList<>();
        String sql = "SELECT * FROM filiais ORDER BY origem_codigo ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Filial f = new Filial();
                f.setIdFilial(rs.getInt("id_filial"));
                f.setOrigemCodigo(rs.getInt("origem_codigo"));
                f.setSufixo(rs.getString("sufixo"));
                f.setNomeEmpresa(rs.getString("nome_empresa"));
                f.setCnpj(rs.getString("cnpj"));
                f.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                f.setEndereco(rs.getString("endereco"));
                f.setNumero(rs.getString("numero"));
                f.setBairro(rs.getString("bairro"));
                f.setMunicipio(rs.getString("municipio"));
                f.setUf(rs.getString("uf"));
                f.setCep(rs.getString("cep"));
                f.setDddTelefone1(rs.getString("ddd_telefone1"));
                f.setTelefone1(rs.getString("telefone1"));
                f.setDddTelefone2(rs.getString("ddd_telefone2"));
                f.setTelefone2(rs.getString("telefone2"));
                f.setEmail(rs.getString("email"));
                
                lista.add(f);
            }
        }
        return lista;
    }

    // --- NOVO: Método para Atualizar com base na origem_codigo ---
    public boolean atualizar(Filial f) throws SQLException {
        String sql = "UPDATE filiais SET sufixo = ?, nome_empresa = ?, cnpj = ?, inscricao_estadual = ?, " +
                     "endereco = ?, numero = ?, bairro = ?, municipio = ?, uf = ?, cep = ?, " +
                     "ddd_telefone1 = ?, telefone1 = ?, ddd_telefone2 = ?, telefone2 = ?, email = ? " +
                     "WHERE origem_codigo = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, f.getSufixo());
            stmt.setString(2, f.getNomeEmpresa());
            stmt.setString(3, f.getCnpj());
            stmt.setString(4, f.getInscricaoEstadual());
            stmt.setString(5, f.getEndereco());
            stmt.setString(6, f.getNumero());
            stmt.setString(7, f.getBairro());
            stmt.setString(8, f.getMunicipio());
            stmt.setString(9, f.getUf());
            stmt.setString(10, f.getCep());
            stmt.setString(11, f.getDddTelefone1());
            stmt.setString(12, f.getTelefone1());
            stmt.setString(13, f.getDddTelefone2());
            stmt.setString(14, f.getTelefone2());
            stmt.setString(15, f.getEmail());
            stmt.setInt(16, f.getOrigemCodigo()); // Condição WHERE
            
            return stmt.executeUpdate() > 0;
        }
    }

    // --- NOVO: Método para Excluir com base na origem_codigo ---
    public boolean excluir(int origemCodigo) throws SQLException {
        String sql = "DELETE FROM filiais WHERE origem_codigo = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, origemCodigo);
            return stmt.executeUpdate() > 0;
        }
    }
    
 // Adicione este método dentro de FilialDAO.java
    public boolean existePorOrigemCodigo(int origemCodigo) throws SQLException {
        String sql = "SELECT 1 FROM filiais WHERE origem_codigo = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, origemCodigo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retorna true se encontrar algum registro
            }
        }
    }
    
 // Método para verificar se já existe o sufixo cadastrado
    public boolean existePorSufixo(String sufixo) throws SQLException {
        if (sufixo == null || sufixo.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM filiais WHERE sufixo = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sufixo.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}