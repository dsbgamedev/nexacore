package model;

/**
 * Classe de Modelo (Model) que representa o 'Tipo de Produto'.
 * 
 * Esta classe é utilizada para categorizar os produtos no sistema, servindo 
 * como a entidade raiz para a associação de atributos dinâmicos.
 */
public class TipoProduto {
    
    // Identificador único do tipo de produto
    private int id;
    
    // Nome descritivo do tipo (ex: Computador, Impressora)
    private String nome;

    /**
     * Construtor padrão vazio para instanciar a classe 
     * via frameworks ou bibliotecas como o GSON.
     */
    public TipoProduto() {}

    /**
     * Construtor com parâmetros para criação rápida de instâncias.
     * @param id Identificador numérico do tipo
     * @param nome Nome do tipo de produto
     */
    public TipoProduto(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    /**
     * Retorna o ID do tipo de produto.
     * @return int identificador
     */
    public int getId() {
        return id;
    }

    /**
     * Define o ID do tipo de produto.
     * @param id identificador a ser definido
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retorna o nome do tipo de produto.
     * @return String nome do tipo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do tipo de produto.
     * @param nome nome a ser atribuído
     */
    public void setNome(String nome) {
        this.nome = nome;
    }
}