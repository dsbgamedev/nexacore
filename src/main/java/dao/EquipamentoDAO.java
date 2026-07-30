package dao;

import conexao.Conexao; // Importa a sua classe de conexão
import model.Equipamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoDAO {

    public boolean inserir(Equipamento eq) throws SQLException {
        // Se o idSistema veio vazio ou nulo da tela, gera na hora
        if (eq.getIdSistema() == null || eq.getIdSistema().trim().isEmpty()) {
            eq.setIdSistema(gerarProximoIdSistema());
        }

        // Alterado de 'departamento' para 'departamento_id'
        String sql = "INSERT INTO equipamentos (id_produto, id_sistema, patrimonio, numero_serie, nome_identificador, origem_filial, ip_atual, status_atual, usuario_atual, departamento_id, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        // Tenta inserir até 3 vezes caso ocorra colisão simultânea de id_sistema
        int tentativas = 3;
        for (int i = 0; i < tentativas; i++) {
            try {
                conn = Conexao.conectar();
                stmt = conn.prepareStatement(sql);
                
                stmt.setInt(1, eq.getIdProduto());
                stmt.setString(2, eq.getIdSistema());
                stmt.setString(3, eq.getPatrimonio());
                stmt.setString(4, eq.getNumeroSerie());
                stmt.setString(5, eq.getNomeIdentificador());
                stmt.setString(6, eq.getOrigemFilial());
                stmt.setString(7, eq.getIpAtual());
                stmt.setString(8, eq.getStatusAtual());
                stmt.setString(9, eq.getUsuarioAtual());
                
                // Tratando o departamento_id como Integer (aceita null se não selecionado)
                if (eq.getDepartamentoId() != null) {
                    stmt.setInt(10, eq.getDepartamentoId());
                } else {
                    stmt.setNull(10, Types.INTEGER);
                }
                
                stmt.setString(11, eq.getObservacoes());
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException e) {
                // Código SQLSTATE '23505' indica violação de Unique Constraint (chave duplicada no Postgres)
                if ("23505".equals(e.getSQLState()) && i < tentativas - 1) {
                    // Alguém pegou esse ID no mesmo milissegundo. Gera o próximo e tenta de novo.
                    eq.setIdSistema(gerarProximoIdSistema());
                    Conexao.fechar(null, stmt, conn);
                } else {
                    throw e; // Outro tipo de erro, repassa a exceção
                }
            } finally {
                Conexao.fechar(null, stmt, conn);
            }
        }
        return false;
    }

    public List<Equipamento> listar() throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        // Expandido o JOIN para trazer também Marca, Modelo, Tipo e Fabricante do produto associado
        String sql = "SELECT e.*, p.codigo_catalogo, p.modelo, m.nome_marca, t.nome as nome_tipo " +
                     "FROM equipamentos e " +
                     "INNER JOIN produtos p ON e.id_produto = p.id " +
                     "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                     "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
                     "ORDER BY e.id_equipamento DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Equipamento eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                
                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                eq.setOrigemFilial(rs.getString("origem_filial"));
                eq.setIpAtual(rs.getString("ip_atual"));
                eq.setStatusAtual(rs.getString("status_atual"));
                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                
                // Mapeando o ID do departamento corretamente
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);

                eq.setObservacoes(rs.getString("observacoes"));
                eq.setDataCadastro(rs.getString("data_cadastro"));
                
                lista.add(eq);
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return lista;
    }
    
    public String gerarProximoIdSistema() throws SQLException {
        String sql = "SELECT id_sistema FROM equipamentos ORDER BY id_equipamento DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        long proximoNumero = 1;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String ultimoId = rs.getString("id_sistema"); // Ex: EQ0000000001
                if (ultimoId != null && ultimoId.startsWith("EQ")) {
                    String apenasNumeros = ultimoId.replaceAll("\\D+", "");
                    proximoNumero = Long.parseLong(apenasNumeros) + 1;
                }
            }
        } catch (Exception e) {
            // Se tabela estiver vazia ou ocorrer erro, começa do 1
            proximoNumero = 1;
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        
        // Retorna formatado com 10 dígitos: EQ0000000001
        return String.format("EQ%010d", proximoNumero);
    }
}