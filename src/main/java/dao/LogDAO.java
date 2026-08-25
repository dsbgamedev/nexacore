package dao;

import conexao.Conexao;
import model.Log;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    public static void registrar(Integer usuarioId, String username, String acao, String modulo, String detalhes, String ip) {
        String sql = "INSERT INTO logs_sistema (usuario_id, usuario_nome, acao, modulo, detalhes, ip_maquina) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.conectar();
            ps = conn.prepareStatement(sql);
            if (usuarioId != null) ps.setInt(1, usuarioId); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, username != null ? username : "SISTEMA/ANONIMO");
            ps.setString(3, acao);
            ps.setString(4, modulo);
            ps.setString(5, detalhes);
            ps.setString(6, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());
        } finally {
            Conexao.fechar(null, ps, conn);
        }
    }

    public List<Log> listarPaginadoComFiltro(int limit, int offset, String busca, String ordem, String direcao) {
        List<Log> logs = new ArrayList<>();
        
        // Validação de colunas para evitar SQL Injection
        String colunaSort;
        switch (ordem != null ? ordem : "") {
            case "usuario": colunaSort = "usuario_nome"; break;
            case "acao":    colunaSort = "acao"; break;
            case "modulo":  colunaSort = "modulo"; break;
            case "detalhes": colunaSort = "detalhes"; break; // Adicionado
            case "ip":       colunaSort = "ip_maquina"; break; // Adicionado
            default:        colunaSort = "data_hora"; break;
        }
        
        String direcaoSort = "ASC".equalsIgnoreCase(direcao) ? "ASC" : "DESC";

        String sql = "SELECT * FROM logs_sistema WHERE usuario_nome LIKE ? OR modulo LIKE ? OR detalhes LIKE ? " +
                     "ORDER BY " + colunaSort + " " + direcaoSort + " LIMIT ? OFFSET ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.conectar();
            ps = conn.prepareStatement(sql);
            String termo = "%" + busca + "%";
            ps.setString(1, termo);
            ps.setString(2, termo);
            ps.setString(3, termo);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            rs = ps.executeQuery();
            while (rs.next()) { 
                logs.add(mapearLog(rs)); 
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar logs: " + e.getMessage());
        } finally {
            Conexao.fechar(rs, ps, conn);
        }
        return logs;
    }

    public int contarLogsComFiltro(String busca) {
        String sql = "SELECT COUNT(*) FROM logs_sistema WHERE usuario_nome LIKE ? OR modulo LIKE ? OR detalhes LIKE ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.conectar();
            ps = conn.prepareStatement(sql);
            String termo = "%" + busca + "%";
            ps.setString(1, termo);
            ps.setString(2, termo);
            ps.setString(3, termo);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erro ao contar logs: " + e.getMessage());
        } finally {
            Conexao.fechar(rs, ps, conn);
        }
        return 0;
    }

    public boolean excluirMuitos(String ids) {
        if (ids == null || !ids.matches("^[0-9,]+$")) return false;
        String sql = "DELETE FROM logs_sistema WHERE id IN (" + ids + ")";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.conectar();
            ps = conn.prepareStatement(sql);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        } finally {
            Conexao.fechar(null, ps, conn);
        }
    }

    private Log mapearLog(ResultSet rs) throws SQLException {
        return new Log(
            rs.getInt("id"),
            rs.getInt("usuario_id"),
            rs.getString("usuario_nome"),
            rs.getString("acao"),
            rs.getString("modulo"),
            rs.getString("detalhes"),
            rs.getString("ip_maquina"),
            rs.getTimestamp("data_hora")
        );
    }
}