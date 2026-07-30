package dao;

import conexao.Conexao;
import model.Atributo;
import model.Grupo;
import model.TipoProduto; // IMPORTANTE: verifique se esta classe existe
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AtributoDAO (Data Access Object)
 * Responsável por toda a comunicação entre o sistema Nexacore e o banco de dados PostgreSQL.
 * Gerencia o ciclo de vida de Atributos, Grupos, Tipos de Produto e seus relacionamentos (vínculos).
 */

public class AtributoDAO {
	/**
     * Recupera todos os atributos ativos cadastrados no sistema.
     * @return List<Atributo> Lista de atributos disponíveis para seleção.
     */	
    public List<Atributo> listarTodos() {
        List<Atributo> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM atributos WHERE ativo = true ORDER BY nome ASC";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Atributo attr = new Atributo();
                attr.setId(rs.getInt("id"));
                attr.setNome(rs.getString("nome"));
                lista.add(attr);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    
	/**
	 * Lista todos os tipos de produto cadastrados.
	 * @return List<TipoProduto> Lista para popular o menu lateral.
	 */    
    public List<TipoProduto> listarTodosOsTipos() {
        List<TipoProduto> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM tipos_produto ORDER BY nome ASC";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TipoProduto tp = new TipoProduto();
                tp.setId(rs.getInt("id"));
                tp.setNome(rs.getString("nome"));
                lista.add(tp);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    
    /**
     * Busca os grupos de visualização disponíveis para os campos do formulário.
     */
    public List<Grupo> listarGrupos() {
        List<Grupo> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM grupos_produto ORDER BY nome ASC"; // Ajuste o nome da tabela se necessário
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Grupo g = new Grupo();
                g.setId(rs.getInt("id"));
                g.setNome(rs.getString("nome"));
                lista.add(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    
    /**
     * Realiza o JOIN entre Atributos e a tabela de Vínculos (campos_tipo_produto).
     * @param tipoProdutoId O tipo que está selecionado na tela.
     * @return List<Atributo> Atributos formatados com seus grupos e regras (tamanho, obrigatório).
     */
    public List<Atributo> buscarAtributosPorTipo(int tipoProdutoId) {
        List<Atributo> lista = new ArrayList<>();
        
        // 1. Buscamos o ctp.id como "vinculo_id"
        String sql = "SELECT ctp.id AS vinculo_id, a.id AS atributo_id, a.nome, ctp.grupo_id, g.nome AS nome_grupo, ctp.tipo_dado, ctp.tamanho, ctp.ordem, ctp.obrigatorio " +
                     "FROM atributos a " +
                     "JOIN campos_tipo_produto ctp ON a.id = ctp.atributo_id " +
                     "LEFT JOIN grupos_produto g ON ctp.grupo_id = g.id " +
                     "WHERE ctp.tipo_id = ? ORDER BY ctp.ordem ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tipoProdutoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Atributo attr = new Atributo();
                
                // 2. Mapeamento correto: o ID da linha é o vinculo_id
                attr.setId(rs.getInt("vinculo_id")); 
                // Agora, se a sua classe Atributo tiver um campo idAtributoOriginal, use-o:
                // attr.setIdAtributoOriginal(rs.getInt("atributo_id")); 
                
                attr.setNome(rs.getString("nome"));
                attr.setGrupoId(rs.getInt("grupo_id"));
                attr.setNomeGrupo(rs.getString("nome_grupo"));
                attr.setTipoDado(rs.getString("tipo_dado"));
                attr.setTamanho(rs.getInt("tamanho"));
                attr.setOrdem(rs.getInt("ordem"));
                attr.setObrigatorio(rs.getBoolean("obrigatorio"));
                
                lista.add(attr);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    
	/**
	 * Atualiza a posição (ordem) de um vínculo de atributo.
	 * Utilizado principalmente pela funcionalidade de Drag-and-Drop (SortableJS) no frontend.
	 * 
	 * @param id Identificador do registro na tabela 'campos_tipo_produto'.
	 * @param novaOrdem O novo valor inteiro para a posição do campo.
	 */
    public void atualizarOrdemAtributo(int id, int novaOrdem) {
        String sql = "UPDATE campos_tipo_produto SET ordem = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar(); // Ajuste conforme seu método de conexão
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, novaOrdem);
            ps.setInt(2, id);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Atualiza as propriedades de configuração de um atributo vinculado a um tipo de produto.
     * Foca na edição das características de exibição/regra, preservando a identidade do vínculo.
     * 
     * @param id O 'vinculo_id' da tabela 'campos_tipo_produto'.
     * @param atributoId ID do atributo original (mantido para referência).
     * @param grupoId ID do grupo visual ao qual o campo pertence.
     * @param tipoDado O tipo de dado (text, number, etc).
     * @param tamanho O tamanho máximo do campo.
     * @param obrigatorio Flag indicando se o campo é de preenchimento obrigatório.
     * @throws SQLException Caso o registro não seja localizado no banco.
     */        
   public void atualizarAtributo(int id, int atributoId, int grupoId, String tipoDado, int tamanho, boolean obrigatorio) throws SQLException {
	    // Agora o SQL foca em atualizar o VÍNCULO (grupo, tipo, etc), 
	    // mas mantém o atributo_id apenas se necessário. 
	    // Para simplificar e evitar o erro de unicidade, vamos remover o atributo_id do UPDATE 
	    // se você estiver apenas editando as propriedades do campo.
	    
	    String sql = "UPDATE campos_tipo_produto SET grupo_id = ?, tipo_dado = ?, tamanho = ?, obrigatorio = ? WHERE id = ?";
	    
	    try (Connection conn = Conexao.conectar();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        ps.setInt(1, grupoId);
	        ps.setString(2, tipoDado);
	        ps.setInt(3, tamanho);
	        ps.setBoolean(4, obrigatorio);
	        ps.setInt(5, id); // O 'id' aqui é o vinculo_id (70, 71, etc.)
	        
	        int afetadas = ps.executeUpdate();
	        
	        if (afetadas == 0) {
	            throw new SQLException("Erro: Registro com ID " + id + " não encontrado.");
	        }
	    }
	}
   
   /**
    * Método utilitário para depuração (log).
    * Realiza uma varredura na tabela de vínculos e imprime no console do servidor
    * a estrutura atual, ajudando a identificar inconsistências de dados.
    */
   public void listarTodosOsDadosDoBanco() {
	    String sql = "SELECT * FROM campos_tipo_produto";
	    System.out.println("--- INICIANDO AUDITORIA DE DADOS NO JAVA ---");
	    
	    try (Connection conn = Conexao.conectar();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        
	        int contagem = 0;
	        while (rs.next()) {
	            contagem++;
	            int id = rs.getInt("id");
	            int atributoId = rs.getInt("atributo_id");
	            System.out.println("Linha " + contagem + " -> ID no banco: " + id + " | atributo_id: " + atributoId);
	        }
	        
	        if (contagem == 0) {
	            System.out.println("ALERTA: A tabela está VAZIA para esta conexão!");
	        } else {
	            System.out.println("Total de registros encontrados pelo Java: " + contagem);
	        }
	        
	    } catch (SQLException e) {
	        System.out.println("Erro ao acessar a tabela: " + e.getMessage());
	        e.printStackTrace();
	    }
	    System.out.println("--- FIM DA AUDITORIA ---");
	}
   
   /**
    * Validação de integridade.
    * Verifica se um determinado atributo já foi vinculado a um tipo específico
    * para evitar duplicidade de registros na mesma configuração.
    * 
    * @param tipoId ID do tipo de produto.
    * @param atributoId ID do atributo.
    * @return true se o vínculo já existir, false caso contrário.
    */ 
    public boolean existeVinculo(int tipoId, int atributoId) {
        String sql = "SELECT COUNT(*) FROM campos_tipo_produto WHERE tipo_id = ? AND atributo_id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tipoId);
            stmt.setInt(2, atributoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    /**
     * Vincula um atributo a um tipo de produto, calculando automaticamente a ordem.
     */
    public void vincularAtributoAoTipo(int tipoId, int atributoId, int grupoId, String tipoDado, int tamanho, boolean obrigatorio) throws SQLException {
        // Buscamos a ordem automaticamente aqui dentro
        int novaOrdem = buscarProximaOrdem(tipoId);
        
        String sql = "INSERT INTO campos_tipo_produto (tipo_id, atributo_id, grupo_id, tipo_dado, tamanho, ordem, obrigatorio, ativo, data_cadastro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tipoId);
            stmt.setInt(2, atributoId);
            stmt.setInt(3, grupoId);
            stmt.setString(4, tipoDado);
            stmt.setInt(5, tamanho);
            stmt.setInt(6, novaOrdem); // Usando a ordem calculada automaticamente
            stmt.setBoolean(7, obrigatorio);
            stmt.executeUpdate();
        }
    }  
    
    //** METODO AUXILIAR RESPONSAVEL POR DESCOBRIR QUAL PROXIMO NUMETO NA ORDEM
    public int buscarProximaOrdem(int tipoId) {
        // Busca o primeiro "buraco" (número que falta na sequência)
        // Se não houver buracos, ele pega o próximo número da sequência (MAX + 1)
        String sql = "SELECT COALESCE(MIN(t1.ordem + 1), 1) " +
                     "FROM campos_tipo_produto t1 " +
                     "WHERE t1.tipo_id = ? " +
                     "AND NOT EXISTS (SELECT 1 FROM campos_tipo_produto t2 " +
                     "                WHERE t2.tipo_id = ? " +
                     "                AND t2.ordem = t1.ordem + 1)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tipoId);
            stmt.setInt(2, tipoId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        
        return 1; // Fallback: se a tabela estiver totalmente vazia
    }
    
    //Metodo para buscar o ID pelo nome
    public String buscarNomeAtributo(int id) {
        String sql = "SELECT nome FROM atributos WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Atributo desconhecido"; // Fallback caso não encontre
    }
    
    /**
     * Insere um novo tipo de produto no sistema.
     * @param nome Nome do novo tipo (ex: 'Monitor').
     */
    public void salvarTipo(String nome) {
        // Adicionamos o grupo_id na query. Usando o 1 como exemplo de grupo padrão.
        String sql = "INSERT INTO tipos_produto (nome, grupo_id) VALUES (?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.setInt(2, 1); // <--- ID do grupo padrão (ex: "Geral" ou "Hardware")
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Registra um novo atributo na base de dados.
     * @param nome Nome do atributo a ser criado.
     * @throws SQLException Repassa erros de banco para tratamento na camada superior.
     */
    public void salvarAtributo(String nome) throws SQLException {
    	if (nome != null) {
            String nomeNormalizado = java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .trim()
                .toLowerCase();

            if (nomeNormalizado.equals("marca") || nomeNormalizado.equals("marcas")) {
                throw new SQLException("O atributo 'Marca' é nativo do sistema e não pode ser cadastrado manualmente.");
            }
        }//Se tentar cadastrar a palavra Marca nao ira conseguir pois ela e uma tabela e nativa da tela cadastro produto
    	
        String sql = "INSERT INTO atributos (nome, ativo, data_cadastro) VALUES (?, true, CURRENT_TIMESTAMP)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Remove um vínculo específico entre um atributo e um tipo de produto.
     * @param idVinculo Identificador único (Primary Key) na tabela 'campos_tipo_produto'.
     */
    public void excluirVinculo(int idVinculo) { // Alterado para receber apenas o ID da linha (vinculo_id)
        // O SQL agora deleta diretamente pela chave primária (id)
        String sql = "DELETE FROM campos_tipo_produto WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVinculo); // Passa apenas o ID único da linha
            stmt.executeUpdate();
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    /**
     * Exclui um tipo de produto. 
     * Se houver atributos vinculados, o banco de dados bloqueará a exclusão (Foreign Key Constraint).
     * @param tipoId ID do tipo a ser removido.
     * @throws SQLException Caso o tipo não exista ou haja violação de integridade.
     */
    public void excluirTipoProduto(int tipoId) throws SQLException {
        // Tentamos deletar apenas o tipo. 
        // Se ele tiver vínculos, o banco de dados lançará um erro de violação de FK automaticamente.
        String sql = "DELETE FROM tipos_produto WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tipoId);
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas == 0) {
                throw new SQLException("Erro: Tipo não encontrado.");
            }
        } 
        // Não precisamos de catch aqui, pois o 'throws SQLException' 
        // vai passar o erro direto para o seu Servlet capturar.
    }
 
    /**
     * Verifica se um atributo possui vínculos ativos.
     * Essencial para impedir exclusões que quebrariam formulários existentes.
     * @param atributoId ID do atributo a ser verificado.
     * @return true se estiver em uso, false caso contrário.
     */
    public boolean atributoEstaEmUso(int atributoId) {
        String sql = "SELECT COUNT(*) FROM campos_tipo_produto WHERE atributo_id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, atributoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Se retornar > 0, ele está sendo usado
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    /**
     * Exclui um atributo do sistema com segurança.
     * Executa uma checagem prévia para evitar violação de integridade referencial.
     * @param atributoId ID do atributo a ser excluído.
     * @throws SQLException Se o atributo estiver em uso ou não for encontrado.
     */
    public void excluirAtributo(int atributoId) throws SQLException {
        // Primeiro verificamos novamente por segurança
        if (atributoEstaEmUso(atributoId)) {
            throw new SQLException("Este atributo não pode ser excluído pois está vinculado a um ou mais tipos de produto.");
        }
        
        String sql = "DELETE FROM atributos WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, atributoId);
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Atributo não encontrado.");
            }
        }
    }
}
   