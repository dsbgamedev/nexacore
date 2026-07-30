package model;

public class Equipamento {
    private int idEquipamento;
    private int idProduto;
    
    // Dados do produto relacionados para exibição
    private String codigoCatalogo;
    private String nomeProduto;
    
    // Dados da Unidade Física (Genéricos)
    private String idSistema;
    private String patrimonio;
    private String numeroSerie;
    private String nomeIdentificador;
    
    // Substituído o texto antigo pelo Código numérico da origem/filial
    private Integer origemCodigo; 
    
    private String ipAtual;
    private String statusAtual;
    private String usuarioAtual;
    
    // Substituído o texto antigo pelo ID do departamento
    private Integer departamentoId; 
    
    private String observacoes;
    private String dataCadastro;
    
    private String nomeOrigem;
    private String nomeDepartamento;

    /**
     * @return the idEquipamento
     */
    public int getIdEquipamento() {
        return idEquipamento;
    }
    /**
     * @param idEquipamento the idEquipamento to set
     */
    public void setIdEquipamento(int idEquipamento) {
        this.idEquipamento = idEquipamento;
    }
    /**
     * @return the idProduto
     */
    public int getIdProduto() {
        return idProduto;
    }
    /**
     * @param idProduto the idProduto to set
     */
    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }
    /**
     * @return the codigoCatalogo
     */
    public String getCodigoCatalogo() {
        return codigoCatalogo;
    }
    /**
     * @param codigoCatalogo the codigoCatalogo to set
     */
    public void setCodigoCatalogo(String codigoCatalogo) {
        this.codigoCatalogo = codigoCatalogo;
    }
    /**
     * @return the nomeProduto
     */
    public String getNomeProduto() {
        return nomeProduto;
    }
    /**
     * @param nomeProduto the nomeProduto to set
     */
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }
    /**
     * @return the idSistema
     */
    public String getIdSistema() {
        return idSistema;
    }
    /**
     * @param idSistema the idSistema to set
     */
    public void setIdSistema(String idSistema) {
        this.idSistema = idSistema;
    }
    /**
     * @return the patrimonio
     */
    public String getPatrimonio() {
        return patrimonio;
    }
    /**
     * @param patrimonio the patrimonio to set
     */
    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }
    /**
     * @return the numeroSerie
     */
    public String getNumeroSerie() {
        return numeroSerie;
    }
    /**
     * @param numeroSerie the numeroSerie to set
     */
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    /**
     * @return the nomeIdentificador
     */
    public String getNomeIdentificador() {
        return nomeIdentificador;
    }
    /**
     * @param nomeIdentificador the nomeIdentificador to set
     */
    public void setNomeIdentificador(String nomeIdentificador) {
        this.nomeIdentificador = nomeIdentificador;
    }
    
    /**
     * @return the origemCodigo
     */
    public Integer getOrigemCodigo() {
        return origemCodigo;
    }
    /**
     * @param origemCodigo the origemCodigo to set
     */
    public void setOrigemCodigo(Integer origemCodigo) {
        this.origemCodigo = origemCodigo;
    }

    /**
     * @return the ipAtual
     */
    public String getIpAtual() {
        return ipAtual;
    }
    /**
     * @param ipAtual the ipAtual to set
     */
    public void setIpAtual(String ipAtual) {
        this.ipAtual = ipAtual;
    }
    /**
     * @return the statusAtual
     */
    public String getStatusAtual() {
        return statusAtual;
    }
    /**
     * @param statusAtual the statusAtual to set
     */
    public void setStatusAtual(String statusAtual) {
        this.statusAtual = statusAtual;
    }
    /**
     * @return the usuarioAtual
     */
    public String getUsuarioAtual() {
        return usuarioAtual;
    }
    /**
     * @param usuarioAtual the usuarioAtual to set
     */
    public void setUsuarioAtual(String usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
    }
    
    /**
     * @return the departamentoId
     */
    public Integer getDepartamentoId() {
        return departamentoId;
    }
    /**
     * @param departamentoId the departamentoId to set
     */
    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }

    /**
     * @return the observacoes
     */
    public String getObservacoes() {
        return observacoes;
    }
    /**
     * @param observacoes the observacoes to set
     */
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    /**
     * @return the dataCadastro
     */
    public String getDataCadastro() {
        return dataCadastro;
    }
    /**
     * @param dataCadastro the dataCadastro to set
     */
    public void setDataCadastro(String dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
	/**
	 * @return the nomeOrigem
	 */
	public String getNomeOrigem() {
		return nomeOrigem;
	}
	/**
	 * @param nomeOrigem the nomeOrigem to set
	 */
	public void setNomeOrigem(String nomeOrigem) {
		this.nomeOrigem = nomeOrigem;
	}
	/**
	 * @return the nomeDepartamento
	 */
	public String getNomeDepartamento() {
		return nomeDepartamento;
	}
	/**
	 * @param nomeDepartamento the nomeDepartamento to set
	 */
	public void setNomeDepartamento(String nomeDepartamento) {
		this.nomeDepartamento = nomeDepartamento;
	}
    
}