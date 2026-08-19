package model;

import java.time.LocalDate;

public class ManutencaoChamado {
    private Long idChamado;
    private Long idEquipamento;
    private Long filialOrigemId;
    private Long idDepartamento; 
    private LocalDate dataAbertura;
    private String solicitante;
    private String tipoProblema;
    private String prioridade;
    private String descricaoProblema;
    private String responsavelTecnico;
    private LocalDate previsaoAtendimento;
    private String observacoes;
    
    // ATUALIZADO PARA A NOVA ESTRUTURA NORMALIZADA
    private Long idStatusChamado;     
    private String nomeStatus;
    private String nomeEquipamento;
    private String diagnostico;
    private String solucaoRealizada;
    private boolean reparado;
    
    // Getters e Setters
    public Long getIdChamado() { return idChamado; }
    public void setIdChamado(Long idChamado) { this.idChamado = idChamado; }

    // Método auxiliar de formatação
    public String getCodigoFormatado() {
        if (this.idChamado == null) return "MAN-0000000";
        return String.format("MAN-%07d", this.idChamado);
    }
    
    public boolean isReparado() {
        return reparado;
    }

    public void setReparado(boolean reparado) {
        this.reparado = reparado;
    }

    public Long getIdEquipamento() { return idEquipamento; }
    public void setIdEquipamento(Long idEquipamento) { this.idEquipamento = idEquipamento; }

    public Long getFilialOrigemId() { return filialOrigemId; }
    public void setFilialOrigemId(Long filialOrigemId) { this.filialOrigemId = filialOrigemId; }
    
    public Long getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Long idDepartamento) { this.idDepartamento = idDepartamento; }

    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }

    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }

    public String getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(String tipoProblema) { this.tipoProblema = tipoProblema; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getDescricaoProblema() { return descricaoProblema; }
    public void setDescricaoProblema(String descricaoProblema) { this.descricaoProblema = descricaoProblema; }

    public String getResponsavelTecnico() { return responsavelTecnico; }
    public void setResponsavelTecnico(String responsavelTecnico) { this.responsavelTecnico = responsavelTecnico; }

    public LocalDate getPrevisaoAtendimento() { return previsaoAtendimento; }
    public void setPrevisaoAtendimento(LocalDate previsaoAtendimento) { this.previsaoAtendimento = previsaoAtendimento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getIdStatusChamado() { return idStatusChamado; }
    public void setIdStatusChamado(Long idStatusChamado) { this.idStatusChamado = idStatusChamado; }

    public String getNomeStatus() { return nomeStatus; }
    public void setNomeStatus(String nomeStatus) { this.nomeStatus = nomeStatus; }
    
    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String nomeEquipamento) { this.nomeEquipamento = nomeEquipamento; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getSolucaoRealizada() { return solucaoRealizada; }
    public void setSolucaoRealizada(String solucaoRealizada) { this.solucaoRealizada = solucaoRealizada; }
}