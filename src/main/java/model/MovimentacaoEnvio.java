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
    
    // Novos campos para exibir os nomes na tela de consulta
    private String nomeOrigem;
    private String nomeDestino;

    // Novos campos para o controle do status da movimentação
    private Long statusId;
    private String statusNome;
    private String statusCor;

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

    // Getters e Setters para o nome das filiais
    public String getNomeOrigem() { return nomeOrigem; }
    public void setNomeOrigem(String nomeOrigem) { this.nomeOrigem = nomeOrigem; }

    public String getNomeDestino() { return nomeDestino; }
    public void setNomeDestino(String nomeDestino) { this.nomeDestino = nomeDestino; }

    // Getters e Setters para o Status da Movimentação
    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getStatusNome() { return statusNome; }
    public void setStatusNome(String statusNome) { this.statusNome = statusNome; }

    public String getStatusCor() { return statusCor; }
    public void setStatusCor(String statusCor) { this.statusCor = statusCor; }
}