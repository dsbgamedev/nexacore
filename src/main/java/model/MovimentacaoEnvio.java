package model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MovimentacaoEnvio {
    private Long idEnvio;
    private LocalDate dataEnvio;
    private Long origemId;
    private Long destinoId;
    private String responsavel; // Quem criou / iniciou (ex: devolução/envio comum)
    private String responsavelEnvio;   // Novo: Quem efetivou o envio físico
    private String transportadora;
    private String codigoRastreio;
    private LocalDate dataPrevisaoEntrega;
    private String observacoes;
    
    // Campo para o Número da Nota Fiscal
    private String numeroNota;
    
    // 👇 ADICIONE ESTE CAMPO PARA O COMPROVANTE 👇
    private String comprovante;
    
    // Campos para exibir os nomes na tela de consulta
    private String nomeOrigem;
    private String nomeDestino;
 // Campos para Código e Sufixo da Origem e Destino
    private Long origemCodigo;
    private String origemSufixo;
    private Long destinoCodigo;
    private String destinoSufixo;

    // Campos para o controle do status da movimentação
    private Long statusId;
    private String statusNome;
    private String statusCor;

    // Novo campo para listar os produtos vinculados no modal de consulta
    private List<Map<String, Object>> produtos;
    private List<MovimentacaoHistorico> historico;

    // Getters e Setters para os produtos vinculados
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
    
    // Novos Getters e Setters para o Responsável pelo Envio
    public String getResponsavelEnvio() { return responsavelEnvio; }
    public void setResponsavelEnvio(String responsavelEnvio) { this.responsavelEnvio = responsavelEnvio; }

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
         
    //Getters e Setter para Origem Codigo e Sufixo filiais e matriz
    public Long getOrigemCodigo() { return origemCodigo;}
    public void setOrigemCodigo(Long origemCodigo) { this.origemCodigo = origemCodigo;}
    
    public String getOrigemSufixo() { return origemSufixo;}
    public void setOrigemSufixo(String origemSufixo) { this.origemSufixo = origemSufixo;}
    
    public Long getDestinoCodigo() { return destinoCodigo; }
    public void setDestinoCodigo(Long destinoCodigo) { this.destinoCodigo = destinoCodigo;}
    
    public String getDestinoSufixo() { return destinoSufixo;}
    public void setDestinoSufixo(String destinoSufixo) { this.destinoSufixo = destinoSufixo;}

    // Getters e Setters para o Status da Movimentação
    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getStatusNome() { return statusNome; }
    public void setStatusNome(String statusNome) { this.statusNome = statusNome; }

    public String getStatusCor() { return statusCor; }
    public void setStatusCor(String statusCor) { this.statusCor = statusCor; }
    
    public String getNumeroNota() { return numeroNota; }
    public void setNumeroNota(String numeroNota) { this.numeroNota = numeroNota; }
    
    // 👇 ADICIONE OS GETTER E SETTER DO COMPROVANTE 👇
    public String getComprovante() { return comprovante; }
    public void setComprovante(String comprovante) { this.comprovante = comprovante; }

    public List<Map<String, Object>> getProdutos() { return produtos; }
    public void setProdutos(List<Map<String, Object>> produtos) { this.produtos = produtos; }
    
    public List<MovimentacaoHistorico> getHistorico() { return historico; }
    public void setHistorico(List<MovimentacaoHistorico> historico) { this.historico = historico; }
}