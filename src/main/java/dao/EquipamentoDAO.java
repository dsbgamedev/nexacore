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

        // Alterado de 'origem_filial' para 'origem_codigo' (Integer)
        String sql = "INSERT INTO equipamentos (id_produto, id_sistema, patrimonio, numero_serie, nome_identificador, origem_codigo, ip_atual, status_atual, usuario_atual, departamento_id, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
                
                
                // Tratando o origem_codigo como Integer (aceita null se não selecionado)
                if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
                    stmt.setInt(6, eq.getOrigemCodigo());
                } else {
                    stmt.setNull(6, Types.INTEGER);
                }
                
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
                
                // Mapeando o código da origem numérico corretamente
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);

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
    
    public List<Equipamento> listarComFiltros(String pesquisaGlobal, String idSistema, String patrimonio, String serial, String origem, String departamento, String status, String produto, String usuario) throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT e.*, p.codigo_catalogo, p.modelo, " +
            "f.nome_empresa as nome_origem, d.nome_departamento as nome_departamento " +
            "FROM equipamentos e " +
            "INNER JOIN produtos p ON e.id_produto = p.id " +
            "LEFT JOIN filiais f ON e.origem_codigo = f.origem_codigo " +
            "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
            "WHERE 1=1"
        );
        
        List<Object> parametros = new ArrayList<>();

        if (pesquisaGlobal != null && !pesquisaGlobal.trim().isEmpty()) {
            sql.append(" AND (e.id_sistema ILIKE ? OR e.patrimonio ILIKE ? OR e.numero_serie ILIKE ? OR e.usuario_atual ILIKE ? OR e.nome_identificador ILIKE ? OR p.modelo ILIKE ? OR p.codigo_catalogo ILIKE ? OR f.nome_empresa ILIKE ? OR d.nome_departamento ILIKE ?)");
            String termoGlobal = "%" + pesquisaGlobal.trim() + "%";
            for (int i = 0; i < 9; i++) {
                parametros.add(termoGlobal);
            }
        }

        if (idSistema != null && !idSistema.trim().isEmpty()) {
            sql.append(" AND e.id_sistema ILIKE ?");
            parametros.add("%" + idSistema.trim() + "%");
        }
        if (patrimonio != null && !patrimonio.trim().isEmpty()) {
            sql.append(" AND e.patrimonio ILIKE ?");
            parametros.add("%" + patrimonio.trim() + "%");
        }
        if (serial != null && !serial.trim().isEmpty()) {
            sql.append(" AND e.numero_serie ILIKE ?");
            parametros.add("%" + serial.trim() + "%");
        }
        if (origem != null && !origem.trim().isEmpty()) {
            sql.append(" AND e.origem_codigo = ?");
            parametros.add(Integer.parseInt(origem));
        }
        if (departamento != null && !departamento.trim().isEmpty()) {
            sql.append(" AND e.departamento_id = ?");
            parametros.add(Integer.parseInt(departamento));
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND e.status_atual = ?");
            parametros.add(status.trim());
        }
        if (produto != null && !produto.trim().isEmpty()) {
            sql.append(" AND (p.modelo ILIKE ? OR p.codigo_catalogo ILIKE ?)");
            parametros.add("%" + produto.trim() + "%");
            parametros.add("%" + produto.trim() + "%");
        }
        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append(" AND e.usuario_atual ILIKE ?");
            parametros.add("%" + usuario.trim() + "%");
        }

        sql.append(" ORDER BY e.id_equipamento DESC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Equipamento eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                eq.setNomeProduto(rs.getString("modelo")); 
                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);
                
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);

                eq.setIpAtual(rs.getString("ip_atual"));
                eq.setStatusAtual(rs.getString("status_atual"));
                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                eq.setObservacoes(rs.getString("observacoes"));
                
                try { eq.getClass().getMethod("setNomeOrigem", String.class).invoke(eq, rs.getString("nome_origem")); } catch (Exception ignored) {}
                try { eq.getClass().getMethod("setNomeDepartamento", String.class).invoke(eq, rs.getString("nome_departamento")); } catch (Exception ignored) {}

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
    
    public Equipamento buscarPorId(int idEquipamento) throws SQLException {
        String sql = "SELECT e.*, p.codigo_catalogo, p.modelo, p.descricao_catalogo, " +
                     "m.nome_marca, t.nome as nome_tipo, d.nome_departamento " +
                     "FROM equipamentos e " +
                     "INNER JOIN produtos p ON e.id_produto = p.id " +
                     "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                     "LEFT JOIN tipos_produto t ON p.tipo_id = t.id " +
                     "LEFT JOIN departamentos d ON e.departamento_id = d.id_departamento " +
                     "WHERE e.id_equipamento = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Equipamento eq = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idEquipamento);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                eq = new Equipamento();
                eq.setIdEquipamento(rs.getInt("id_equipamento"));
                eq.setIdProduto(rs.getInt("id_produto"));
                eq.setCodigoCatalogo(rs.getString("codigo_catalogo"));
                eq.setNomeProduto(rs.getString("modelo"));
                
                eq.setModelo(rs.getString("modelo"));
                eq.setNomeMarca(rs.getString("nome_marca"));
                eq.setNomeTipo(rs.getString("nome_tipo"));
                
                // Usando a coluna correta que existe na tabela produtos:
                eq.setDescricaoDetalhada(rs.getString("descricao_catalogo")); 

                eq.setIdSistema(rs.getString("id_sistema"));
                eq.setPatrimonio(rs.getString("patrimonio"));
                eq.setNumeroSerie(rs.getString("numero_serie"));
                eq.setNomeIdentificador(rs.getString("nome_identificador"));
                
                int origemCod = rs.getInt("origem_codigo");
                eq.setOrigemCodigo(rs.wasNull() ? null : origemCod);
                
                eq.setIpAtual(rs.getString("ip_atual"));
                eq.setStatusAtual(rs.getString("status_atual"));
                eq.setUsuarioAtual(rs.getString("usuario_atual"));
                
                int depId = rs.getInt("departamento_id");
                eq.setDepartamentoId(rs.wasNull() ? null : depId);
                
                eq.setNomeDepartamento(rs.getString("nome_departamento"));
                eq.setObservacoes(rs.getString("observacoes"));
            }
        } finally {
            Conexao.fechar(rs, stmt, conn);
        }
        return eq;
    }
    public boolean atualizar(Equipamento eq) throws SQLException {
        String sql = "UPDATE equipamentos SET id_produto = ?, patrimonio = ?, numero_serie = ?, nome_identificador = ?, origem_codigo = ?, ip_atual = ?, status_atual = ?, usuario_atual = ?, departamento_id = ?, observacoes = ? WHERE id_equipamento = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Conexao.conectar();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, eq.getIdProduto());
            stmt.setString(2, eq.getPatrimonio());
            stmt.setString(3, eq.getNumeroSerie());
            stmt.setString(4, eq.getNomeIdentificador());
            
            if (eq.getOrigemCodigo() != null && eq.getOrigemCodigo() > 0) {
                stmt.setInt(5, eq.getOrigemCodigo());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setString(6, eq.getIpAtual());
            stmt.setString(7, eq.getStatusAtual());
            stmt.setString(8, eq.getUsuarioAtual());
            
            if (eq.getDepartamentoId() != null) {
                stmt.setInt(9, eq.getDepartamentoId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            stmt.setString(10, eq.getObservacoes());
            stmt.setInt(11, eq.getIdEquipamento());
            
            return stmt.executeUpdate() > 0;
        } finally {
            Conexao.fechar(null, stmt, conn);
        }
    }
    
    /**
     * Exclui um equipamento e suas dependências utilizando transação.
     */
    public void excluirEquipamento(int idEquipamento) throws SQLException {
        // Se houver tabelas filhas vinculadas ao equipamento (ex: histórico, manutenções, etc), adicione aqui:
        // String sqlHistorico = "DELETE FROM equipamentos_historico WHERE equipamento_id = ?";
        
        String sqlEquipamento = "DELETE FROM equipamentos WHERE id_equipamento = ?";

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false); // Inicia a transação

            try (PreparedStatement stmtEquipamento = conn.prepareStatement(sqlEquipamento)) {
                
                // Se tiver outras tabelas para limpar antes, faça o PreparedStatement delas aqui.

                stmtEquipamento.setInt(1, idEquipamento);
                int linhas = stmtEquipamento.executeUpdate();

                if (linhas == 0) {
                    throw new SQLException("Equipamento não encontrado para exclusão.");
                }

                conn.commit(); // Confirma a transação se tudo deu certo
            } catch (SQLException e) {
                conn.rollback(); // Desfaz alterações caso ocorra qualquer erro
                throw e;
            }
        }
    }
}