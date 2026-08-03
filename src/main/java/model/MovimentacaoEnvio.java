package model;

import java.time.LocalDate;

public class MovimentacaoEnvio {
    private Long idEnvio;
    private LocalDate dataEnvio;
    private Long origemId;
    private Long destinoId;
    private String responsavel;
    private String transportadora;
    private String codigoRastreio;
    private LocalDate dataPrevisaoEntrega;
    private String observacoes;

    // Getters e Setters
    public Long getIdEnvio() { return idEnvio; }
    public void setIdEnvio(Long idEnvio) { this.idEnvio = idEnvio; }
    public LocalDate getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDate dataEnvio) { this.dataEnvio = dataEnvio; }
    public Long getOrigemId() { return origemId; }
    public void setOrigemId(Long origemId) { this.origemId = origemId; }
    public Long getDestinoId() { return destinoId; }
    public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getTransportadora() { return transportadora; }
    public void setTransportadora(String transportadora) { this.transportadora = transportadora; }
    public String getCodigoRastreio() { return codigoRastreio; }
    public void setCodigoRastreio(String codigoRastreio) { this.codigoRastreio = codigoRastreio; }
    public LocalDate getDataPrevisaoEntrega() { return dataPrevisaoEntrega; }
    public void setDataPrevisaoEntrega(LocalDate dataPrevisaoEntrega) { this.dataPrevisaoEntrega = dataPrevisaoEntrega; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}