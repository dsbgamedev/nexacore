package dto;

import java.util.List;

public class ProdutoDTO {
    private int id; // Identificador único (Chave Primária)
    private int tipoId;
    private String sku;
    private String descricaoResumida;
    private String descricaoDetalhada;
    private boolean ativo;
    
    // Novos campos para Marca e Fabricante (IDs e Nomes para exibição)
    private int marcaId;
    private int fabricanteId;
    private String nomeMarca;
    private String nomeFabricante;

    private String modelo; 
    private String observacoes;
    private List<AtributoDTO> atributos;
    private List<String> caminhosImagens; 
    private List<String> imagens; 
    
    // Campo auxiliar para exibição do tipo na tabela
    private String nomeTipo;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTipoId() { return tipoId; }
    public void setTipoId(int tipoId) { this.tipoId = tipoId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getDescricaoResumida() { return descricaoResumida; }
    public void setDescricaoResumida(String descricaoResumida) { this.descricaoResumida = descricaoResumida; }

    public String getDescricaoDetalhada() { return descricaoDetalhada; }
    public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    public String getNomeTipo() { return nomeTipo; }
    public void setNomeTipo(String nomeTipo) { this.nomeTipo = nomeTipo; }
    
    public List<String> getCaminhosImagens() { return caminhosImagens; }
    public void setCaminhosImagens(List<String> caminhosImagens) { this.caminhosImagens = caminhosImagens; }

    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public List<AtributoDTO> getAtributos() { return atributos; }
    public void setAtributos(List<AtributoDTO> atributos) { this.atributos = atributos; }

    // --- GETTERS E SETTERS PARA MARCA E FABRICANTE ---
    
    public int getMarcaId() { return marcaId; }
    public void setMarcaId(int marcaId) { this.marcaId = marcaId; }

    public int getFabricanteId() { return fabricanteId; }
    public void setFabricanteId(int fabricanteId) { this.fabricanteId = fabricanteId; }

    public String getNomeMarca() { return nomeMarca; }
    public void setNomeMarca(String nomeMarca) { this.nomeMarca = nomeMarca; }

    public String getNomeFabricante() { return nomeFabricante; }
    public void setNomeFabricante(String nomeFabricante) { this.nomeFabricante = nomeFabricante; }
}