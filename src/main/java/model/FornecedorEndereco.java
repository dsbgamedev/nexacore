package model;

import java.io.Serializable;

public class FornecedorEndereco implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idEndereco;
    private String tipoEndereco = "COMERCIAL";
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String pais = "Brasil";
    private String referencia;
    private boolean principal;

    public Long getIdEndereco() { return idEndereco; }
    public void setIdEndereco(Long v) { idEndereco = v; }
    public String getTipoEndereco() { return tipoEndereco; }
    public void setTipoEndereco(String v) { tipoEndereco = v; }
    public String getCep() { return cep; }
    public void setCep(String v) { cep = v; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String v) { logradouro = v; }
    public String getNumero() { return numero; }
    public void setNumero(String v) { numero = v; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String v) { complemento = v; }
    public String getBairro() { return bairro; }
    public void setBairro(String v) { bairro = v; }
    public String getCidade() { return cidade; }
    public void setCidade(String v) { cidade = v; }
    public String getUf() { return uf; }
    public void setUf(String v) { uf = v; }
    public String getPais() { return pais; }
    public void setPais(String v) { pais = v; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { referencia = v; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean v) { principal = v; }
}
