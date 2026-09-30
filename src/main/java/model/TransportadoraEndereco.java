package model;

public class TransportadoraEndereco {
    private Long idEndereco;
    private Long idTransportadora;
    private Integer tipoEnderecoId;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String pais;
    private String referencia;
    private boolean principal;

    // Getters e Setters
    public Long getIdEndereco() { return idEndereco; }
    public void setIdEndereco(Long idEndereco) { this.idEndereco = idEndereco; }
    public Long getIdTransportadora() { return idTransportadora; }
    public void setIdTransportadora(Long idTransportadora) { this.idTransportadora = idTransportadora; }
    public Integer getTipoEnderecoId() { return tipoEnderecoId; }
    public void setTipoEnderecoId(Integer tipoEnderecoId) { this.tipoEnderecoId = tipoEnderecoId; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}