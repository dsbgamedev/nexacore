package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Fornecedor implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idFornecedor;
    private String tipoPessoa = "PJ";
    private String razaoSocial;
    private String nomeFantasia;
    private String documento;
    private String inscricaoEstadual;
    private String inscricaoMunicipal;
    private Integer idCategoria;
    private String contatoPrincipal;
    private String telefone;
    private String celular;
    private String email;
    private String site;
    private String status = "ATIVO";
    private String observacao;
    private Timestamp dataCadastro;
    private Timestamp dataAtualizacao;
    private int quantidadeEnderecos;
    private int quantidadeFiliais;
    private List<FornecedorEndereco> enderecos = new ArrayList<>();
    private List<Integer> filiaisIds = new ArrayList<>();

    public Long getIdFornecedor(){return idFornecedor;} public void setIdFornecedor(Long v){idFornecedor=v;}
    public String getTipoPessoa(){return tipoPessoa;} public void setTipoPessoa(String v){tipoPessoa=v;}
    public String getRazaoSocial(){return razaoSocial;} public void setRazaoSocial(String v){razaoSocial=v;}
    public String getNomeFantasia(){return nomeFantasia;} public void setNomeFantasia(String v){nomeFantasia=v;}
    public String getDocumento(){return documento;} public void setDocumento(String v){documento=v;}
    public String getInscricaoEstadual(){return inscricaoEstadual;} public void setInscricaoEstadual(String v){inscricaoEstadual=v;}
    public String getInscricaoMunicipal(){return inscricaoMunicipal;} public void setInscricaoMunicipal(String v){inscricaoMunicipal=v;}
    public Integer getIdCategoria() {return idCategoria;}public void setIdCategoria(Integer idCategoria) {this.idCategoria = idCategoria;}
	public String getContatoPrincipal(){return contatoPrincipal;} public void setContatoPrincipal(String v){contatoPrincipal=v;}
    public String getTelefone(){return telefone;} public void setTelefone(String v){telefone=v;}
    public String getCelular(){return celular;} public void setCelular(String v){celular=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getSite(){return site;} public void setSite(String v){site=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public Timestamp getDataCadastro(){return dataCadastro;} public void setDataCadastro(Timestamp v){dataCadastro=v;}
    public Timestamp getDataAtualizacao(){return dataAtualizacao;} public void setDataAtualizacao(Timestamp v){dataAtualizacao=v;}
    public int getQuantidadeEnderecos(){return quantidadeEnderecos;} public void setQuantidadeEnderecos(int v){quantidadeEnderecos=v;}
    public int getQuantidadeFiliais(){return quantidadeFiliais;} public void setQuantidadeFiliais(int v){quantidadeFiliais=v;}
    public List<FornecedorEndereco> getEnderecos(){return enderecos;} public void setEnderecos(List<FornecedorEndereco> v){enderecos=v==null?new ArrayList<>():v;}
    public List<Integer> getFiliaisIds(){return filiaisIds;} public void setFiliaisIds(List<Integer> v){filiaisIds=v==null?new ArrayList<>():v;}
}
