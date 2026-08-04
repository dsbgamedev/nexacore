package dao;

import model.StatusEquipamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import conexao.Conexao;

public class StatusEquipamentoDAO {
    
    public List<StatusEquipamento> listarTodos() throws SQLException {
        List<StatusEquipamento> lista = new ArrayList<>();
        // Incluindo cor e ativo caso queira usá-los nas badges do front-end
        String sql = "SELECT id, nome, cor, ativo FROM status_equipamento ORDER BY nome"; 
        
        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                StatusEquipamento status = new StatusEquipamento();
                status.setId(rs.getInt("id")); // Ajustado para int conforme o Model
                status.setNome(rs.getString("nome"));
                status.setCor(rs.getString("cor"));
                status.setAtivo(rs.getBoolean("ativo"));
                lista.add(status);
            }
        }
        return lista;
    }
}