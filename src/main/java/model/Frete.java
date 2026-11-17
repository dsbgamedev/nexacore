package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Frete {
    
    private Integer id; 
       
    private LocalDate dataLancamento;
    private LocalTime horaLancamento;
 // --- CAMPOS AUXILIARES PARA USO EXCLUSIVO DO JSP (JSTL) ---
    // Estes campos serão preenchidos no Servlet com java.util.Date, 
    // permitindo o uso do <fmt:formatDate> sem erro de conversão.
    private Date dataLancamentoJsp;
    private Date horaLancamentoJsp;
    private int idTransportadora; // FK - Obrigatória
    private String notaFiscal;
    // CORREÇÃO: volume mudou para Integer (Wrapper) para ser opcional/aceitar null
    private Integer volume; 
    
    private Double pesoLiquido; 
    private Double valorNf;     
    private String observacao;
    
    private String nomeEmpresa; 
    private String nomeCliente;
    private Double valorFrete;      
   
    private String nomeTransportadora; 
    
 // --- ADICIONE ESTE CAMPO AQUI ---
    private Integer idFilial;
    
 // --- NOVO CAMPO PARA A PORCENTAGEM ---
    private Double porcentagemFrete;
    
    public Frete() {
    }
    
    public Frete(Integer id, LocalDate dataLancamento, LocalTime horaLancamento, int idTransportadora,
                 String notaFiscal, Integer volume, Double pesoLiquido, Double valorNf, String observacao, // Volume alterado aqui
                 String nomeEmpresa, String nomeCliente, Double valorFrete, Integer idFilial, Double porcentagemFrete) { 
        this.id = id;
        this.dataLancamento = dataLancamento;
        this.horaLancamento = horaLancamento;
        this.idTransportadora = idTransportadora;
        this.notaFiscal = notaFiscal;
        this.volume = volume;
        this.pesoLiquido = pesoLiquido;
        this.valorNf = valorNf;
        this.observacao = observacao;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeCliente = nomeCliente;   
        this.valorFrete = valorFrete;
        this.idFilial = idFilial; // Adicionado
        this.porcentagemFrete = porcentagemFrete; // Adicionado
    }
    
	// --- Getters e Setters ---
    
    
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	
	public LocalDate getDataLancamento() { return dataLancamento; }
	public void setDataLancamento(LocalDate dataLancamento) { this.dataLancamento = dataLancamento; }
	
	 // --- GETTERS & SETTERS PARA OS NOVOS CAMPOS JSP ---
	public Integer getIdFilial() {
        return idFilial;
    }

    public void setIdFilial(Integer idFilial) {
        this.idFilial = idFilial;
    }
    
    public Date getDataLancamentoJsp() {
        return dataLancamentoJsp;
    }

    public void setDataLancamentoJsp(Date dataLancamentoJsp) {
        this.dataLancamentoJsp = dataLancamentoJsp;
    }

    public Date getHoraLancamentoJsp() {
        return horaLancamentoJsp;
    }

    public void setHoraLancamentoJsp(Date horaLancamentoJsp) {
        this.horaLancamentoJsp = horaLancamentoJsp;
    }
    
    public Double getPorcentagemFrete() {
        return porcentagemFrete;
    }

    public void setPorcentagemFrete(Double porcentagemFrete) {
        this.porcentagemFrete = porcentagemFrete;
    }
	
	public LocalTime getHoraLancamento() { return horaLancamento; }
	public void setHoraLancamento(LocalTime horaLancamento) { this.horaLancamento = horaLancamento; }
	
	public int getIdTransportadora() { return idTransportadora; }
	public void setIdTransportadora(int idTransportadora) { this.idTransportadora = idTransportadora; }
	
	public String getNotaFiscal() { return notaFiscal; }
	public void setNotaFiscal(String notaFiscal) { this.notaFiscal = notaFiscal; }
	
	// GETTER E SETTER CORRIGIDOS PARA Integer
	public Integer getVolume() { return volume; }
	public void setVolume(Integer volume) { this.volume = volume; }
	
	public Double getPesoLiquido() { return pesoLiquido; }
	public void setPesoLiquido(Double pesoLiquido) { this.pesoLiquido = pesoLiquido; }
	
	public Double getValorNf() { return valorNf; }
	public void setValorNf(Double valorNf) { this.valorNf = valorNf; }
	
	public String getObservacao() { return observacao; }
	public void setObservacao(String observacao) { this.observacao = observacao; }
	
    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }
    
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
        
    public Double getValorFrete() { return valorFrete; }
    public void setValorFrete(Double valorFrete) { this.valorFrete = valorFrete; }
    
    public String getNomeTransportadora() { return nomeTransportadora; }
    public void setNomeTransportadora(String nomeTransportadora) { this.nomeTransportadora = nomeTransportadora; }

	@Override
	public String toString() {
		return "Frete [id=" + id + ", idFilial=" + idFilial +", dataLancamento=" + dataLancamento + ", horaLancamento=" + horaLancamento
				+ ", idTransportadora=" + idTransportadora + ", notaFiscal=" + notaFiscal + ", volume=" + volume
				+ ", pesoLiquido=" + pesoLiquido + ", valorNf=" + valorNf + ", observacao=" + observacao 
                + ", nomeEmpresa=" + nomeEmpresa + ", nomeCliente=" + nomeCliente + ", valorFrete=" + valorFrete
                + ", nomeTransportadora=" + nomeTransportadora + ", porcentagemFrete=" + porcentagemFrete + "]";
	} 
}