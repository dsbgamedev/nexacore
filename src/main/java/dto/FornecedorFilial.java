package dto;

import java.io.Serializable;

public class FornecedorFilial implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer idFilial;
    private String nomeFilial;
    private String observacao;

    // Construtores
    public FornecedorFilial() {}

    public FornecedorFilial(Integer idFilial, String nomeFilial, String observacao) {
        this.idFilial = idFilial;
        this.nomeFilial = nomeFilial;
        this.observacao = observacao;
    }

    // Getters e Setters
    public Integer getIdFilial() { 
        return idFilial; 
    }
    public void setIdFilial(Integer idFilial) { 
        this.idFilial = idFilial; 
    }
    
    public String getNomeFilial() { 
        return nomeFilial; 
    }
    public void setNomeFilial(String nomeFilial) { 
        this.nomeFilial = nomeFilial; 
    }
    
    public String getObservacao() { 
        return observacao; 
    }
    public void setObservacao(String observacao) { 
        this.observacao = observacao; 
    }
}