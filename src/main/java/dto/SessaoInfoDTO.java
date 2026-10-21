package dto;

public class SessaoInfoDTO {
    private String sessionId;
    private String nomeUsuario;
    private String emailUsuario;
    private String dataCriacao;
    private String ultimoAcesso;
    private String tempoAtivo;
    private String consumoMemoria; // Novo campo para consumo/processos

    // Getters e Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    
    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
    
    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public String getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(String ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }
    
    public String getTempoAtivo() { return tempoAtivo; }
    public void setTempoAtivo(String tempoAtivo) { this.tempoAtivo = tempoAtivo; }
    
    public String getConsumoMemoria() { return consumoMemoria; }
    public void setConsumoMemoria(String consumoMemoria) { this.consumoMemoria = consumoMemoria; }
}
