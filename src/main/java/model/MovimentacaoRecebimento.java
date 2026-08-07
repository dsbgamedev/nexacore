package model;

import java.util.Date;

public class MovimentacaoRecebimento {
    private int idRecebimento;
    private int idEnvio;
    private Date dataRecebimento;
    private String responsavelRecebimento;
    private String condicaoGeral;
    private Date dataCadastro;

    public MovimentacaoRecebimento() {}

    // Getters e Setters
    public int getIdRecebimento() { return idRecebimento; }
    public void setIdRecebimento(int idRecebimento) { this.idRecebimento = idRecebimento; }

    public int getIdEnvio() { return idEnvio; }
    public void setIdEnvio(int idEnvio) { this.idEnvio = idEnvio; }

    public Date getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(Date dataRecebimento) { this.dataRecebimento = dataRecebimento; }

    public String getResponsavelRecebimento() { return responsavelRecebimento; }
    public void setResponsavelRecebimento(String responsavelRecebimento) { this.responsavelRecebimento = responsavelRecebimento; }

    public String getCondicaoGeral() { return condicaoGeral; }
    public void setCondicaoGeral(String condicaoGeral) { this.condicaoGeral = condicaoGeral; }

    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }
}
