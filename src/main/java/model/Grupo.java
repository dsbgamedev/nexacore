package model;

/**
 * Representa os grupos visuais ou lógicos aos quais um atributo pode pertencer
 * dentro de um formulário dinâmico.
 * 
 * Esta classe atua como um catálogo de opções para a interface, padronizando 
 * a seleção de grupos no modal de vinculação e permitindo a expansão futura 
 * de propriedades (como cores, ícones ou descrições detalhadas).
 */
public class Grupo {
    
    // Identificador único do grupo no banco de dados
    private int id;
    
    // Nome do grupo exibido para o usuário na interface
    private String nome;

    /**
     * Construtor padrão necessário para instanciar a classe 
     * durante a conversão JSON ou mapeamento de banco de dados.
     */
    public Grupo() {}

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    /**
     * Retorna o ID do grupo.
     * @return int identificador do grupo
     */
    public int getId() { return id; }

    /**
     * Define o ID do grupo.
     * @param id identificador a ser definido
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna o nome do grupo.
     * @return String nome do grupo
     */
    public String getNome() { return nome; }

    /**
     * Define o nome do grupo.
     * @param nome nome a ser atribuído ao grupo
     */
    public void setNome(String nome) { this.nome = nome; }
}