package dao;

import conexao.Conexao;
import model.Frete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;
import java.time.LocalDate;
import java.sql.Date; 
import java.sql.Time; 

public class FreteDAO {
    
    private static final String SQL_INSERT = 
        "INSERT INTO frete (id_transportadora, nota_fiscal, volume, peso_liquido, valor_nf, observacao, nome_empresa, nome_cliente, valor_frete, id_filial, porcentagem_frete) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE =
        "UPDATE frete SET " +
        "id_transportadora = ?, nota_fiscal = ?, volume = ?, peso_liquido = ?, " +
        "valor_nf = ?, observacao = ?, nome_empresa = ?, nome_cliente = ?, valor_frete = ?, id_filial = ?, porcentagem_frete = ? " +
        "WHERE id = ?";

    private static final String SQL_SELECT_JOIN = 
    		"SELECT f.id, f.id_filial, f.data_lancamento, f.hora_lancamento, f.id_transportadora, t.nome_fantasia AS nome_transportadora, " +
		    "f.nota_fiscal, f.volume, f.peso_liquido, f.valor_nf, f.observacao, f.nome_empresa, f.nome_cliente, f.valor_frete, f.porcentagem_frete " +
		    "FROM frete f " +
		    "JOIN transportadoras t ON f.id_transportadora = t.id_transportadora";
    
    private static final String SQL_SELECT_BY_ID = SQL_SELECT_JOIN + " WHERE f.id = ?";

    public boolean existeNfParaTransportadora(String nf, int idTransp) throws SQLException {
        String sql = "SELECT COUNT(*) FROM frete WHERE UPPER(nota_fiscal) = UPPER(?) AND id_transportadora = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nf.trim());
            stmt.setInt(2, idTransp);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean existeNfParaTransportadoraEdicao(String nf, int idTransp, int idAtual) throws SQLException {
        String sql = "SELECT COUNT(*) FROM frete WHERE UPPER(nota_fiscal) = UPPER(?) AND id_transportadora = ? AND id != ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nf.trim());
            stmt.setInt(2, idTransp);
            stmt.setInt(3, idAtual);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private String mapearColuna(String colunaJS) {
        if (colunaJS == null || colunaJS.isEmpty()) return "f.data_lancamento DESC, f.hora_lancamento DESC";
        
        switch (colunaJS) {
            case "id": return "f.id";
            case "data": return "f.data_lancamento";
            case "empresa": return "f.nome_empresa";
            case "transportadora": return "t.nome_fantasia";
            case "cliente": return "f.nome_cliente";
            case "valorNf": return "f.valor_nf";
            case "valorFrete": return "f.valor_frete";
            case "peso": return "f.peso_liquido";
            case "volume": return "f.volume";
            case "notaFiscal": return "f.nota_fiscal";
            default: return "f.data_lancamento DESC, f.hora_lancamento DESC";
        }
    }

    public List<Frete> listarFretesDoDiaPaginado(LocalDate dataConsulta, int limite, int offset, String orderBy, String orderDir, int idFilial) throws SQLException {
        List<Frete> listaFretes = new ArrayList<>();
        String colunaSql = mapearColuna(orderBy);
        String direcao = (orderDir != null && orderDir.equalsIgnoreCase("ASC")) ? "ASC" : "DESC";

        String sql = SQL_SELECT_JOIN + 
                     " WHERE f.data_lancamento = ? AND f.id_filial = ? " + 
                     " ORDER BY " + colunaSql + " " + direcao + 
                     " LIMIT ? OFFSET ?"; 

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(dataConsulta)); 
            stmt.setInt(2, idFilial);                   
            stmt.setInt(3, limite);                      
            stmt.setInt(4, offset);                      
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) listaFretes.add(preencherFreteComTransportadora(rs));
            }
        }
        return listaFretes;
    }

    public List<Frete> listarPorTermoPaginado(String termo, int limite, int offset, String orderBy, String orderDir, int idFilial) throws SQLException {
        List<Frete> listaFretes = new ArrayList<>();
        String colunaSql = mapearColuna(orderBy);
        String direcao = (orderDir != null && orderDir.equalsIgnoreCase("ASC")) ? "ASC" : "DESC";

        String sql = SQL_SELECT_JOIN +
                     " WHERE (LOWER(t.nome_fantasia) LIKE ? OR LOWER(f.nota_fiscal) LIKE ? OR LOWER(f.nome_cliente) LIKE ? OR LOWER(f.nome_empresa) LIKE ? OR LOWER(f.observacao) LIKE ? OR CAST(f.id AS TEXT) LIKE ?) " +
                     " AND f.id_filial = ? " + 
                     " ORDER BY " + colunaSql + " " + direcao + 
                     " LIMIT ? OFFSET ?"; 
        
        String termoLike = "%" + termo.toLowerCase() + "%";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for(int i=1; i<=5; i++) stmt.setString(i, termoLike);
            stmt.setString(6, "%" + termo + "%");
            stmt.setInt(7, idFilial); 
            stmt.setInt(8, limite); 
            stmt.setInt(9, offset); 
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) listaFretes.add(preencherFreteComTransportadora(rs));
            }
        }
        return listaFretes;
    }

    private Frete preencherFreteComTransportadora(ResultSet rs) throws SQLException {
    	Frete f = new Frete();
        
        f.setId(rs.getInt("id")); 
        f.setIdFilial(rs.getInt("id_filial")); 
        
        java.sql.Date sqlDate = rs.getDate("data_lancamento");
        if (sqlDate != null) f.setDataLancamento(sqlDate.toLocalDate());
        
        java.sql.Time sqlTime = rs.getTime("hora_lancamento");
        if (sqlTime != null) f.setHoraLancamento(sqlTime.toLocalTime());

        f.setIdTransportadora(rs.getInt("id_transportadora"));
        f.setNomeTransportadora(rs.getString("nome_transportadora")); 
        f.setNotaFiscal(rs.getString("nota_fiscal"));
        
        int volume = rs.getInt("volume");
        f.setVolume(rs.wasNull() ? null : volume);
        
        f.setValorFrete(rs.getObject("valor_frete") != null ? rs.getDouble("valor_frete") : null);
        f.setValorNf(rs.getObject("valor_nf") != null ? rs.getDouble("valor_nf") : null);
        f.setPesoLiquido(rs.getObject("peso_liquido") != null ? rs.getDouble("peso_liquido") : null);
        f.setPorcentagemFrete(rs.getObject("porcentagem_frete") != null ? rs.getDouble("porcentagem_frete") : null);
        
        f.setObservacao(rs.getString("observacao"));
        f.setNomeEmpresa(rs.getString("nome_empresa")); 
        f.setNomeCliente(rs.getString("nome_cliente")); 
        
        return f;
    }

    public Frete salvarERetornar(Frete frete) throws SQLException {
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) { 
            preencherStatement(stmt, frete);
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return buscarPorIdComNomeTransportadora(rs.getInt(1));
                }
            }
        }
        return null;
    }

    public boolean atualizar(Frete frete) throws SQLException {
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            preencherStatement(stmt, frete);
            stmt.setInt(12, frete.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    private void preencherStatement(PreparedStatement stmt, Frete frete) throws SQLException {
        stmt.setInt(1, frete.getIdTransportadora());
        stmt.setString(2, frete.getNotaFiscal() != null ? frete.getNotaFiscal().toUpperCase().trim() : null);
        
        if (frete.getVolume() != null) stmt.setInt(3, frete.getVolume()); else stmt.setNull(3, java.sql.Types.INTEGER);
        if (frete.getPesoLiquido() != null) stmt.setDouble(4, frete.getPesoLiquido()); else stmt.setNull(4, java.sql.Types.NUMERIC);
        if (frete.getValorNf() != null) stmt.setDouble(5, frete.getValorNf()); else stmt.setNull(5, java.sql.Types.NUMERIC);
        
        stmt.setString(6, frete.getObservacao() != null ? frete.getObservacao().toUpperCase().trim() : null);
        stmt.setString(7, frete.getNomeEmpresa() != null ? frete.getNomeEmpresa().toUpperCase().trim() : null);
        stmt.setString(8, frete.getNomeCliente() != null ? frete.getNomeCliente().toUpperCase().trim() : null);
        
        if (frete.getValorFrete() != null) stmt.setDouble(9, frete.getValorFrete()); else stmt.setNull(9, java.sql.Types.NUMERIC);

        if (frete.getIdFilial() != null) {
            stmt.setInt(10, frete.getIdFilial());
        } else {
            stmt.setInt(10, 7); 
        }
        
        if (frete.getPorcentagemFrete() != null) {
            stmt.setDouble(11, frete.getPorcentagemFrete());
        } else {
            stmt.setNull(11, java.sql.Types.NUMERIC);
        }
    }

    public Frete buscarPorIdComNomeTransportadora(int id) throws SQLException {
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return preencherFreteComTransportadora(rs);
            }
        }
        return null;
    }

    public int contarTotalFretesDoDia(LocalDate dataConsulta, int idFilial) throws SQLException {
        String sql = "SELECT COUNT(id) FROM frete WHERE data_lancamento = ? AND id_filial = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataConsulta)); 
            stmt.setInt(2, idFilial); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1); 
            }
        }
        return 0;
    }

    public int contarTotalFretesPorTermo(String termo, int idFilial) throws SQLException {
        String sql = "SELECT COUNT(f.id) FROM frete f " +
                     "JOIN transportadoras t ON f.id_transportadora = t.id_transportadora " +
                     "WHERE (LOWER(t.nome_fantasia) LIKE ? OR LOWER(f.nota_fiscal) LIKE ? OR LOWER(f.nome_cliente) LIKE ? OR LOWER(f.nome_empresa) LIKE ? OR LOWER(f.observacao) LIKE ? OR CAST(f.id AS TEXT) LIKE ?) " +
                     "AND f.id_filial = ?";
        String termoLike = "%" + termo.toLowerCase() + "%";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for(int i=1; i<=5; i++) stmt.setString(i, termoLike);
            stmt.setString(6, "%" + termo + "%");
            stmt.setInt(7, idFilial); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1); 
            }
        }
        return 0;
    }

    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM frete WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stat = conn.prepareStatement(sql)) {
            stat.setInt(1, id);
            return stat.executeUpdate() > 0;
        }
    }
}