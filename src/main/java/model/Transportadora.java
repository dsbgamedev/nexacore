package model;

import java.util.List;

public class Transportadora {
    private Long idTransportadora;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoEstadual;
    private String rntrc;
    private String telefone;
    private String celular;
    private String email;
    private String site;
    private String status;
    private String observacao;
    private List<TransportadoraEndereco> enderecos;

    // Getters e Setters
    public Long getIdTransportadora() { return idTransportadora; }
    public void setIdTransportadora(Long idTransportadora) { this.idTransportadora = idTransportadora; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }
    public String getRntrc() { return rntrc; }
    public void setRntrc(String rntrc) { this.rntrc = rntrc; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public List<TransportadoraEndereco> getEnderecos() { return enderecos; }
    public void setEnderecos(List<TransportadoraEndereco> enderecos) { this.enderecos = enderecos; }
}