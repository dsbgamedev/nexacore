package model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Representa um registro de auditoria do sistema.
 * Mapeia a tabela 'logs_sistema' do PostgreSQL.
 */
public class Log {

    private int id;
    private Integer usuarioId; // Pode ser null para ações do sistema/anônimas
    private String usuarioNome;
    private String acao;
    private String modulo;
    private String detalhes;
    private String ipMaquina;
    private Timestamp dataHora;

    // Construtor padrão
    public Log() {}

    // Construtor completo para uso no DAO
    public Log(int id, Integer usuarioId, String usuarioNome, String acao, String modulo, 
               String detalhes, String ipMaquina, Timestamp dataHora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.acao = acao;
        this.modulo = modulo;
        this.detalhes = detalhes;
        this.ipMaquina = ipMaquina;
        this.dataHora = dataHora;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }

    public String getIpMaquina() { return ipMaquina; }
    public void setIpMaquina(String ipMaquina) { this.ipMaquina = ipMaquina; }

    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }

    /**
     * Formata a data para exibição amigável no JSP (dd/MM/yyyy HH:mm:ss).
     */
    public String getDataHoraFormatada() {
        if (this.dataHora != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this.dataHora);
        }
        return "";
    }
}
