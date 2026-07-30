package model;

import java.sql.Timestamp;
import com.google.gson.annotations.SerializedName;

/**
 * Classe de Modelo (Model) que representa um Atributo técnico no sistema Nexacore.
 * Esta classe atua como um DTO (Data Transfer Object) para mapear os dados da 
 * tabela 'atributos' e a configuração de campos dinâmicos ('campos_tipo_produto').
 */
public class Atributo {
    
    // Identificador primário do atributo
    private int id;
    // Nome descritivo do atributo
    private String nome;
    // Detalhes adicionais sobre o atributo
    private String descricao;
    // Status lógico de ativação do atributo no sistema
    private boolean ativo;
    // Data de registro no banco de dados
    private Timestamp dataCadastro;
    
    // CAMPOS PARA O VÍNCULO DINÂMICO
    // Nome do grupo visual associado
    private String grupo;
    // ID da categoria/grupo de visualização
    private int grupoId;
    
    // Mapeamento para JSON com nome amigável para o frontend
    @SerializedName("nomeGrupo")
    private String nomeGrupo;
    
    // Define o formato do dado (ex: TEXT, NUMBER, DATE)
    private String tipoDado;
    // Limite de caracteres ou precisão do campo
    private int tamanho;
    // Posição de exibição na interface (usado pelo Sortable)
    private int ordem;
    // Define se o preenchimento é mandatário
    private boolean obrigatorio;

    /**
     * Construtor padrão vazio para instanciar via reflexão ou frameworks.
     */
    public Atributo() {
    }

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Timestamp getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Timestamp dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    
    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }
    
    public String getNomeGrupo() { return nomeGrupo; }
    public void setNomeGrupo(String nomeGrupo) { this.nomeGrupo = nomeGrupo; }

    public String getTipoDado() { return tipoDado; }
    public void setTipoDado(String tipoDado) { this.tipoDado = tipoDado; }

    public int getTamanho() { return tamanho; }
    public void setTamanho(int tamanho) { this.tamanho = tamanho; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }

    public boolean isObrigatorio() { return obrigatorio; }
    public void setObrigatorio(boolean obrigatorio) { this.obrigatorio = obrigatorio; }
}