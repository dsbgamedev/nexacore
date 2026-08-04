package dao;

import model.SituacaoEquipamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import conexao.Conexao;

public class SituacaoEquipamentoDAO {
    
    public List<SituacaoEquipamento> listarTodos() throws SQLException {
        List<SituacaoEquipamento> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM situacao_equipamento ORDER BY nome";
        
        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                SituacaoEquipamento situacao = new SituacaoEquipamento();
                situacao.setId(rs.getInt("id"));
                situacao.setNome(rs.getString("nome"));
                lista.add(situacao);
            }
        }
        return lista;
    }
}