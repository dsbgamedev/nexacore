package model;

public class EspecificacaoEquipamento {
    private int campoId;
    private String valor;
    
    // Opcional (caso queira exibir o nome do atributo na tela de consulta/dossiê)
    private String nomeCampo; 

    public int getCampoId() { return campoId; }
    public void setCampoId(int campoId) { this.campoId = campoId; }
    
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getNomeCampo() { return nomeCampo; }
    public void setNomeCampo(String nomeCampo) { this.nomeCampo = nomeCampo; }
}
