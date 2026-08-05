package model;

import java.time.LocalDateTime;

public class MovimentacaoHistorico {
    private Long idHistorico;
    private Long idEnvio;
    private Long statusId;
    private String statusNome; // Nome do status (ex: Enviado, Cancelado)
    private LocalDateTime dataHora;
    private String observacao;

    // Getters e Setters
    public Long getIdHistorico() { return idHistorico; }
    public void setIdHistorico(Long idHistorico) { this.idHistorico = idHistorico; }

    public Long getIdEnvio() { return idEnvio; }
    public void setIdEnvio(Long idEnvio) { this.idEnvio = idEnvio; }

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getStatusNome() { return statusNome; }
    public void setStatusNome(String statusNome) { this.statusNome = statusNome; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
