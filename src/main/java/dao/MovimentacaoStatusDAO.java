package dao;

import conexao.Conexao;
import model.MovimentacaoStatus; // Ajuste o import do model conforme seu pacote

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoStatusDAO {

    public List<MovimentacaoStatus> listarTodos() {
        List<MovimentacaoStatus> lista = new ArrayList<>();
        String sql = "SELECT id, nome, cor, ativo FROM movimentacao_status WHERE ativo = true ORDER BY id";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MovimentacaoStatus status = new MovimentacaoStatus();
                status.setId(rs.getInt("id"));
                status.setNome(rs.getString("nome"));
                status.setCor(rs.getString("cor"));
                status.setAtivo(rs.getBoolean("ativo"));
                lista.add(status);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar status de movimentação: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lista;
    }
}

