package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import conexao.Conexao;
import model.Auditoria;

public class AuditoriaDAO {

	public List<Auditoria> listarComFiltros(String usuario, String modulo, String acao, String entidade, String dataInicio, String dataFim, int limite, int offset) {
	    List<Auditoria> lista = new ArrayList<>();
	    StringBuilder sql = new StringBuilder("SELECT id, data_hora, usuario_id, usuario_nome, modulo, acao, entidade, registro_id, ip_origem, sucesso FROM auditoria WHERE 1=1");
	    
	    List<Object> parametros = new ArrayList<>();

	    if (usuario != null && !usuario.isEmpty()) {
	        sql.append(" AND usuario_nome ILIKE ?");
	        parametros.add("%" + usuario + "%");
	    }
	    if (modulo != null && !modulo.isEmpty()) {
	        sql.append(" AND modulo = ?");
	        parametros.add(modulo);
	    }
	    if (acao != null && !acao.isEmpty()) {
	        sql.append(" AND acao = ?");
	        parametros.add(acao);
	    }
	    if (entidade != null && !entidade.isEmpty()) {
	        sql.append(" AND entidade = ?");
	        parametros.add(entidade);
	    }
	    if (dataInicio != null && !dataInicio.isEmpty()) {
	        sql.append(" AND data_hora >= ?::timestamp");
	        parametros.add(dataInicio + " 00:00:00");
	    }
	    if (dataFim != null && !dataFim.isEmpty()) {
	        sql.append(" AND data_hora <= ?::timestamp");
	        parametros.add(dataFim + " 23:59:59");
	    }

	    // Aplica ordenação e paginação dinâmica (limitando a 15 por padrão)
	    sql.append(" ORDER BY data_hora DESC LIMIT ? OFFSET ?");
	    parametros.add(limite);
	    parametros.add(offset);

	    try (Connection conn = Conexao.conectar();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        
	        for (int i = 0; i < parametros.size(); i++) {
	            stmt.setObject(i + 1, parametros.get(i));
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Auditoria audit = new Auditoria();
	                audit.setId(rs.getLong("id"));
	                audit.setDataHora(rs.getTimestamp("data_hora"));
	                audit.setUsuarioId(rs.getLong("usuario_id"));
	                audit.setUsuarioNome(rs.getString("usuario_nome"));
	                audit.setModulo(rs.getString("modulo"));
	                audit.setAcao(rs.getString("acao"));
	                audit.setEntidade(rs.getString("entidade"));
	                audit.setRegistroId(rs.getLong("registro_id"));
	                audit.setIpOrigem(rs.getString("ip_origem"));
	                
	                lista.add(audit);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return lista;
	}
    
    public Auditoria buscarPorId(Long id) {
        String sql = "SELECT id, data_hora, usuario_id, usuario_nome, modulo, acao, entidade, registro_id, descricao, dados_anteriores, dados_novos, ip_origem, sucesso FROM auditoria WHERE id = ?";
        Auditoria audit = null;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    audit = new Auditoria();
                    audit.setId(rs.getLong("id"));
                    // Mapeie os demais campos conforme os setters da sua model Auditoria
                    audit.setUsuarioNome(rs.getString("usuario_nome"));
                    audit.setDescricao(rs.getString("descricao"));
                    audit.setDadosAnteriores(rs.getString("dados_anteriores"));
                    audit.setDadosNovos(rs.getString("dados_novos"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return audit;
    }
    
 // Método para excluir um registro de auditoria por ID
    public boolean excluir(Long id) {
        String sql = "DELETE FROM auditoria WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}