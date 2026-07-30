package dao;

import conexao.Conexao;
import dto.AtributoDTO;
import dto.ConfiguracaoCampoDTO;
import dto.ProdutoDTO;
import model.TipoProduto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * ProdutoDAO
 * Responsável pelas operações de leitura e persistência relacionadas ao Cadastro de Produtos.
 * Alinhado ao padrão de implementação do AtributoDAO.
 */
public class ProdutoDAO {

    /**
     * Lista todos os tipos de produto ativos para popular o select no formulário.
     */
    public List<TipoProduto> listarTiposAtivos() {
        List<TipoProduto> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM tipos_produto WHERE ativo = true ORDER BY nome ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new TipoProduto(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    /**
     * Lista as marcas para popular o select no formulário.
     * (Retorno ajustado de Categoria genérica para Map/Objeto ou mantido limpo caso use DTO genérico, 
     * aqui adaptado para retornar como array genérico ou você pode criar uma classe Marca se preferir, 
     * mantendo a estrutura que você já tinha).
     */
    /**
     * Lista as marcas para popular o select no formulário.
     */
    public List<ProdutoDTO> listarMarcas() throws SQLException {
        List<ProdutoDTO> lista = new ArrayList<>();
        String sql = "SELECT id_marca, nome_marca FROM marcas ORDER BY nome_marca ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ProdutoDTO dto = new ProdutoDTO();
                dto.setMarcaId(rs.getInt("id_marca"));
                dto.setNomeMarca(rs.getString("nome_marca"));
                lista.add(dto); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    /**
     * Lista os fabricantes para popular o select no formulário.
     */
    public List<ProdutoDTO> listarFabricantes() throws SQLException {
        List<ProdutoDTO> lista = new ArrayList<>();
        String sql = "SELECT id_fabricante, razao_social FROM fabricantes ORDER BY razao_social ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ProdutoDTO dto = new ProdutoDTO();
                dto.setNomeFabricante(rs.getString("razao_social"));
                lista.add(dto); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Consulta produtos no banco de dados aplicando filtros dinâmicos.
     */
    public List<ProdutoDTO> consultarProdutos(String sku, int marcaId, int tipoId, String modelo, Boolean ativo, String busca) {
        List<ProdutoDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, t.nome as tipo_nome, m.nome_marca as marca_nome, f.razao_social as fabricante_nome " +
            "FROM produtos p " +
            "JOIN tipos_produto t ON p.tipo_id = t.id " +
            "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
            "LEFT JOIN fabricantes f ON m.id_fabricante = f.id_fabricante " +
            "WHERE 1=1 "
        );

        if (sku != null && !sku.isEmpty()) {
            sql.append(" AND p.codigo_catalogo ILIKE ? ");
        }
        if (marcaId > 0) {
            sql.append(" AND p.marca_id = ? ");
        }
        if (tipoId > 0) {
            sql.append(" AND p.tipo_id = ? ");
        }
        if (modelo != null && !modelo.isEmpty()) {
            sql.append(" AND p.modelo ILIKE ? ");
        }
        if (ativo != null) {
            sql.append(" AND p.ativo = ? ");
        }
        
        if (busca != null && !busca.isEmpty()) {
            sql.append(" AND (p.codigo_catalogo ILIKE ? OR p.modelo ILIKE ? OR p.nome_produto ILIKE ? OR m.nome_marca ILIKE ? OR t.nome ILIKE ? OR f.razao_social ILIKE ? OR p.descricao_catalogo ILIKE ? OR p.observacoes ILIKE ?) ");
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int i = 1;
            if (sku != null && !sku.isEmpty()) {
                stmt.setString(i++, "%" + sku + "%");
            }
            if (marcaId > 0) {
                stmt.setInt(i++, marcaId);
            }
            if (tipoId > 0) {
                stmt.setInt(i++, tipoId);
            }
            if (modelo != null && !modelo.isEmpty()) {
                stmt.setString(i++, "%" + modelo + "%");
            }
            if (ativo != null) {
                stmt.setBoolean(i++, ativo);
            }
            if (busca != null && !busca.isEmpty()) {
                String termoBusca = "%" + busca + "%";
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
                stmt.setString(i++, termoBusca);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ProdutoDTO p = new ProdutoDTO();
                int produtoId = rs.getInt("id");
                p.setId(produtoId);
                p.setSku(rs.getString("codigo_catalogo"));
                p.setModelo(rs.getString("modelo"));
                p.setDescricaoResumida(rs.getString("nome_produto"));
                p.setNomeTipo(rs.getString("tipo_nome"));
                p.setNomeMarca(rs.getString("marca_nome")); 
                p.setNomeFabricante(rs.getString("fabricante_nome")); 
                p.setAtivo(rs.getBoolean("ativo"));
                p.setObservacoes(rs.getString("observacoes"));

                // Monta a descrição detalhada idêntica ao cadastro/busca por ID
                StringBuilder descDetalhada = new StringBuilder();
                if (p.getNomeMarca() != null && !p.getNomeMarca().isEmpty()) {
                    descDetalhada.append("Marca: ").append(p.getNomeMarca());
                }
                if (p.getModelo() != null && !p.getModelo().isEmpty()) {
                    if (descDetalhada.length() > 0) descDetalhada.append(", ");
                    descDetalhada.append("Modelo: ").append(p.getModelo());
                }

                // Busca os atributos dinâmicos da tabela filha para cada produto listado
                String sqlAtributos = "SELECT pe.campo_id, pe.valor, a.nome AS nome_atributo " +
                                      "FROM produto_especificacoes pe " +
                                      "JOIN campos_tipo_produto ctp ON pe.campo_id = ctp.id " +
                                      "JOIN atributos a ON ctp.atributo_id = a.id " +
                                      "WHERE pe.produto_id = ?";
                
                try (PreparedStatement stmtAttr = conn.prepareStatement(sqlAtributos)) {
                    stmtAttr.setInt(1, produtoId);
                    try (ResultSet rsAttr = stmtAttr.executeQuery()) {
                        List<AtributoDTO> atributos = new ArrayList<>();
                        while (rsAttr.next()) {
                            AtributoDTO attr = new AtributoDTO();
                            attr.setIdAtributo(String.valueOf(rsAttr.getInt("campo_id")));
                            attr.setValor(rsAttr.getString("valor"));
                            attr.setNomeAtributo(rsAttr.getString("nome_atributo"));
                            atributos.add(attr);

                            // Adiciona na string de descrição detalhada
                            if (rsAttr.getString("valor") != null && !rsAttr.getString("valor").trim().isEmpty()) {
                                descDetalhada.append(", ").append(rsAttr.getString("nome_atributo")).append(": ").append(rsAttr.getString("valor"));
                            }
                        }
                        p.setAtributos(atributos);
                    }
                }

                p.setDescricaoDetalhada(descDetalhada.toString());

                // Busca as imagens associadas
                String sqlImagens = "SELECT caminho FROM produto_imagens WHERE produto_id = ? ORDER BY ordem ASC";
                try (PreparedStatement stmtImg = conn.prepareStatement(sqlImagens)) {
                    stmtImg.setInt(1, produtoId);
                    try (ResultSet rsImg = stmtImg.executeQuery()) {
                        List<String> caminhos = new ArrayList<>();
                        while (rsImg.next()) {
                            caminhos.add(rsImg.getString("caminho"));
                        }
                        p.setCaminhosImagens(caminhos);
                    }
                }

                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    /**
     * Busca a configuração dinâmica dos campos de um tipo de produto.
     */
    public List<ConfiguracaoCampoDTO> buscarCamposPorTipo(int tipoId) {
        List<ConfiguracaoCampoDTO> lista = new ArrayList<>();
        String sql = "SELECT ctp.id, ctp.tipo_dado, ctp.ordem, ctp.obrigatorio, ctp.placeholder, " +
                     "ctp.tooltip, ctp.mascara, ctp.tamanho, ctp.valor_padrao, a.nome AS nome_atributo " +
                     "FROM campos_tipo_produto ctp " +
                     "JOIN atributos a ON ctp.atributo_id = a.id " +
                     "WHERE ctp.tipo_id = ? AND ctp.ativo = true " +
                     "ORDER BY ctp.ordem ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tipoId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                lista.add(new ConfiguracaoCampoDTO(
                	rs.getInt("id"),
                    rs.getString("nome_atributo"),
                    rs.getString("tipo_dado"),
                    rs.getInt("ordem"),
                    rs.getBoolean("obrigatorio"),
                    rs.getString("placeholder"),
                    rs.getString("tooltip"),
                    rs.getString("mascara"),
                    rs.getInt("tamanho"),
                    rs.getString("valor_padrao")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    /**
     * Busca um produto específico pelo seu ID.
     */
    public ProdutoDTO buscarPorId(int id) {
        ProdutoDTO p = null;
        String sqlProduto = "SELECT p.*, t.nome as tipo_nome, m.nome_marca as marca_nome, f.razao_social as fabricante_nome " +
                            "FROM produtos p " +
                            "JOIN tipos_produto t ON p.tipo_id = t.id " +
                            "LEFT JOIN marcas m ON p.marca_id = m.id_marca " +
                            "LEFT JOIN fabricantes f ON m.id_fabricante = f.id_fabricante " +
                            "WHERE p.id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sqlProduto)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    p = new ProdutoDTO();
                    p.setId(rs.getInt("id"));
                    p.setSku(rs.getString("codigo_catalogo"));
                    p.setTipoId(rs.getInt("tipo_id"));
                    p.setMarcaId(rs.getInt("marca_id"));
                    p.setModelo(rs.getString("modelo"));
                    p.setDescricaoResumida(rs.getString("nome_produto"));
                    p.setDescricaoDetalhada(rs.getString("descricao_catalogo"));
                    p.setObservacoes(rs.getString("observacoes")); // Carrega a observação corretamente
                    p.setNomeTipo(rs.getString("tipo_nome"));
                    p.setNomeMarca(rs.getString("marca_nome"));
                    p.setNomeFabricante(rs.getString("fabricante_nome"));
                    p.setAtivo(rs.getBoolean("ativo"));
                }
            }

            if (p != null) {
                // Busca os atributos dinâmicos
                String sqlAtributos = "SELECT pe.campo_id, pe.valor, a.nome AS nome_atributo " +
                                      "FROM produto_especificacoes pe " +
                                      "JOIN campos_tipo_produto ctp ON pe.campo_id = ctp.id " +
                                      "JOIN atributos a ON ctp.atributo_id = a.id " +
                                      "WHERE pe.produto_id = ?";
                
                try (PreparedStatement stmtAttr = conn.prepareStatement(sqlAtributos)) {
                    stmtAttr.setInt(1, id);
                    try (ResultSet rsAttr = stmtAttr.executeQuery()) {
                        List<AtributoDTO> atributos = new ArrayList<>();
                        while (rsAttr.next()) {
                            AtributoDTO attr = new AtributoDTO();
                            attr.setIdAtributo(String.valueOf(rsAttr.getInt("campo_id")));
                            attr.setValor(rsAttr.getString("valor"));
                            attr.setNomeAtributo(rsAttr.getString("nome_atributo"));
                            atributos.add(attr);
                        }
                        p.setAtributos(atributos);
                    }
                }

                // Busca as imagens associadas e preenche a lista no DTO
                String sqlImagens = "SELECT caminho FROM produto_imagens WHERE produto_id = ? ORDER BY ordem ASC";
                try (PreparedStatement stmtImg = conn.prepareStatement(sqlImagens)) {
                    stmtImg.setInt(1, id);
                    try (ResultSet rsImg = stmtImg.executeQuery()) {
                        List<String> caminhos = new ArrayList<>();
                        while (rsImg.next()) {
                            caminhos.add(rsImg.getString("caminho"));
                        }
                        p.setCaminhosImagens(caminhos); // Garante que as imagens vão para o JSON da API
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }
    /**
     * Atualiza os dados completos de um produto existente.
     */
    public void atualizarProdutoCompleto(ProdutoDTO p) throws SQLException {
        String sqlProduto = "UPDATE produtos SET tipo_id = ?, marca_id = ?, modelo = ?, nome_produto = ?, " +
                            "descricao_catalogo = ?, ativo = ?, observacoes = ? WHERE id = ?";
        
        String deleteAttr = "DELETE FROM produto_especificacoes WHERE produto_id = ?";
        String insertAttr = "INSERT INTO produto_especificacoes (produto_id, campo_id, valor) VALUES (?, ?, ?)";
        
        String deleteImg = "DELETE FROM produto_imagens WHERE produto_id = ?";
        String insertImg = "INSERT INTO produto_imagens (produto_id, nome_arquivo, caminho, principal, ordem) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlProduto)) {
                    ps.setInt(1, p.getTipoId());
                    
                    if (p.getMarcaId() > 0) {
                        ps.setInt(2, p.getMarcaId());
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }

                    ps.setString(3, p.getModelo());
                    ps.setString(4, p.getDescricaoResumida());
                    ps.setString(5, p.getDescricaoDetalhada());
                    ps.setBoolean(6, p.isAtivo());
                    ps.setString(7, p.getObservacoes());
                    ps.setInt(8, p.getId());

                    int linhasAfetadas = ps.executeUpdate();
                    if (linhasAfetadas == 0) {
                        throw new SQLException("Produto não encontrado para atualização.");
                    }
                }

                try (PreparedStatement psDelAttr = conn.prepareStatement(deleteAttr);
                     PreparedStatement psInsAttr = conn.prepareStatement(insertAttr)) {
                    
                    psDelAttr.setInt(1, p.getId());
                    psDelAttr.executeUpdate();

                    if (p.getAtributos() != null) {
                        for (AtributoDTO attr : p.getAtributos()) {
                            if (attr.getValor() != null && !attr.getValor().trim().isEmpty()) {
                                psInsAttr.setInt(1, p.getId());
                                psInsAttr.setInt(2, Integer.parseInt(attr.getIdAtributo()));
                                psInsAttr.setString(3, attr.getValor().trim());
                                psInsAttr.addBatch();
                            }
                        }
                        psInsAttr.executeBatch();
                    }
                }

                try (PreparedStatement psDelImg = conn.prepareStatement(deleteImg);
                     PreparedStatement psInsImg = conn.prepareStatement(insertImg)) {
                    
                    psDelImg.setInt(1, p.getId());
                    psDelImg.executeUpdate();

                    if (p.getCaminhosImagens() != null && !p.getCaminhosImagens().isEmpty()) {
                        for (int i = 0; i < p.getCaminhosImagens().size(); i++) {
                            String fileName = p.getCaminhosImagens().get(i);
                            
                            psInsImg.setInt(1, p.getId());
                            psInsImg.setString(2, fileName); // nome_arquivo
                            psInsImg.setString(3, fileName); // caminho
                            psInsImg.setBoolean(4, i == 0);  // principal (true para a primeira)
                            psInsImg.setInt(5, i);           // ordem
                            
                            psInsImg.addBatch();
                        }
                        psInsImg.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Exclui um produto e suas dependências.
     */
    public void excluirProduto(int id) throws SQLException {
        String sqlEspecificacoes = "DELETE FROM produto_especificacoes WHERE produto_id = ?";
        String sqlImagens = "DELETE FROM produto_imagens WHERE produto_id = ?";
        String sqlProduto = "DELETE FROM produtos WHERE id = ?";

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlEspecificacoes);
                 PreparedStatement ps2 = conn.prepareStatement(sqlImagens);
                 PreparedStatement ps3 = conn.prepareStatement(sqlProduto)) {
                
                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                int linhas = ps3.executeUpdate();

                if (linhas == 0) {
                    throw new SQLException("Produto não encontrado para exclusão.");
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
   
    public boolean skuExiste(String sku) {
        String sql = "SELECT COUNT(*) FROM produtos WHERE codigo_catalogo = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String buscarProximoSequencial(String prefixo) {
        String sql = "SELECT COALESCE(MAX(CAST(NULLIF(regexp_replace(codigo_catalogo, '^.*-', ''), '') AS INTEGER)), 0) + 1 " +
                     "FROM produtos WHERE codigo_catalogo LIKE ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefixo + "-%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return String.format("%03d", rs.getInt(1)); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "001";
    }
    
    public void salvarProdutoCompleto(ProdutoDTO p) throws SQLException {
        String prefixoBase = p.getSku(); 
        String sequencial = buscarProximoSequencial(prefixoBase);
        String skuFinal = prefixoBase + "-" + sequencial;
        
        p.setSku(skuFinal);

        if (skuExiste(p.getSku())) {
            throw new SQLException("O SKU '" + p.getSku() + "' já está cadastrado no sistema.");
        }
        
        String sqlProduto = "INSERT INTO produtos (codigo_catalogo, tipo_id, marca_id, modelo, nome_produto, descricao_catalogo, ativo, observacoes) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        String sqlAtributo = "INSERT INTO produto_especificacoes (produto_id, campo_id, valor) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);

            int produtoId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlProduto)) {
                ps.setString(1, p.getSku());
                ps.setInt(2, p.getTipoId());
                
                if (p.getMarcaId() > 0) {
                    ps.setInt(3, p.getMarcaId());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }

                ps.setString(4, p.getModelo());
                ps.setString(5, p.getDescricaoResumida());
                ps.setString(6, p.getDescricaoDetalhada());
                ps.setBoolean(7, p.isAtivo());
                ps.setString(8, p.getObservacoes());
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) produtoId = rs.getInt(1);
                }
            }

            if (p.getAtributos() != null) {
                try (PreparedStatement psAttr = conn.prepareStatement(sqlAtributo)) {
                    for (AtributoDTO attr : p.getAtributos()) {
                        if (attr.getIdAtributo() != null && !attr.getIdAtributo().trim().isEmpty()) {
                            psAttr.setInt(1, produtoId);
                            psAttr.setInt(2, Integer.parseInt(attr.getIdAtributo()));
                            psAttr.setString(3, attr.getValor());
                            psAttr.addBatch();
                        }
                    }
                    psAttr.executeBatch();
                }
            }
            
            if (p.getCaminhosImagens() != null && !p.getCaminhosImagens().isEmpty()) {
                String sqlImagem = "INSERT INTO produto_imagens (produto_id, nome_arquivo, caminho, principal, ordem) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psImg = conn.prepareStatement(sqlImagem)) {
                    for (int i = 0; i < p.getCaminhosImagens().size(); i++) {
                        String fileName = p.getCaminhosImagens().get(i);
                        
                        psImg.setInt(1, produtoId);
                        psImg.setString(2, fileName); 
                        psImg.setString(3, fileName); 
                        psImg.setBoolean(4, i == 0);  
                        psImg.setInt(5, i);           
                        
                        psImg.addBatch();
                    }
                    psImg.executeBatch();
                }
            }
            
            conn.commit();
            System.out.println("Produto salvo com sucesso! ID gerado: " + produtoId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; 
        }
    }
}