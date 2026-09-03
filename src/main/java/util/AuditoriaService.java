package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import conexao.Conexao;

public class AuditoriaService {

    public static void registrar(
            Long usuarioId, 
            String usuarioNome, 
            String modulo, 
            String acao, 
            String entidade, 
            Long registroId, 
            String descricao, 
            String dadosAnterioresJson, 
            String dadosNovosJson, 
            String ipOrigem) {

        String sql = "INSERT INTO auditoria (usuario_id, usuario_nome, modulo, acao, entidade, registro_id, descricao, dados_anteriores, dados_novos, ip_origem) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);

            if (usuarioId != null) {
                stmt.setLong(1, usuarioId);
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }
            
            stmt.setString(2, usuarioNome);
            stmt.setString(3, modulo);
            stmt.setString(4, acao);
            stmt.setString(5, entidade);
            
            if (registroId != null) {
                stmt.setLong(6, registroId);
            } else {
                stmt.setNull(6, java.sql.Types.BIGINT);
            }
            
            stmt.setString(7, descricao);
            stmt.setString(8, dadosAnterioresJson != null ? dadosAnterioresJson : "{}");
            stmt.setString(9, dadosNovosJson != null ? dadosNovosJson : "{}");
            stmt.setString(10, ipOrigem);

            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erro ao registrar auditoria: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Utiliza o método seguro de fechamento da sua classe Conexao
            Conexao.fechar(null, stmt, conn);
        }
    }
}