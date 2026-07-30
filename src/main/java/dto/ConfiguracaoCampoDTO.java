package dto;

/**
 * DTO para transportar a configuração de um campo dinâmico
 * para a interface de cadastro de produto.
 */
public class ConfiguracaoCampoDTO {
	private int id; // <--- ADICIONE ESTE CAMPO
    private String nomeAtributo;
    private String tipoDado; // (text, select, etc)
    private int ordem;
    private boolean obrigatorio;
    private String placeholder;
    private String tooltip; // Adicionado
    private String mascara;
    private int tamanho;
    private String valorPadrao;

    public ConfiguracaoCampoDTO(int id, String nomeAtributo, String tipoDado, int ordem, 
                                boolean obrigatorio, String placeholder, String toolTip,String mascara, int tamanho, String valorPadrao) {
    	this.id = id; // <--- ATUALIZE O CONSTRUTOR
    	this.nomeAtributo = nomeAtributo;
        this.tipoDado = tipoDado;
        this.ordem = ordem;
        this.obrigatorio = obrigatorio;
        this.placeholder = placeholder;
        this.mascara = mascara;
        this.tooltip = toolTip;
        this.tamanho = tamanho;
        this.valorPadrao = valorPadrao;
    }
    

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
	/**
	 * @return the nomeAtributo
	 */
	public String getNomeAtributo() {
		return nomeAtributo;
	}

	/**
	 * @param nomeAtributo the nomeAtributo to set
	 */
	public void setNomeAtributo(String nomeAtributo) {
		this.nomeAtributo = nomeAtributo;
	}

	/**
	 * @return the tipoDado
	 */
	public String getTipoDado() {
		return tipoDado;
	}

	/**
	 * @param tipoDado the tipoDado to set
	 */
	public void setTipoDado(String tipoDado) {
		this.tipoDado = tipoDado;
	}

	/**
	 * @return the ordem
	 */
	public int getOrdem() {
		return ordem;
	}

	/**
	 * @param ordem the ordem to set
	 */
	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	/**
	 * @return the obrigatorio
	 */
	public boolean isObrigatorio() {
		return obrigatorio;
	}

	/**
	 * @param obrigatorio the obrigatorio to set
	 */
	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	/**
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * @param placeholder the placeholder to set
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * @return the mascara
	 */
	public String getMascara() {
		return mascara;
	}

	/**
	 * @param mascara the mascara to set
	 */
	public void setMascara(String mascara) {
		this.mascara = mascara;
	}


	/**
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}


	/**
	 * @param tooltip the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}


	/**
	 * @return the tamanho
	 */
	public int getTamanho() {
		return tamanho;
	}


	/**
	 * @param tamanho the tamanho to set
	 */
	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}


	/**
	 * @return the valorPadrao
	 */
	public String getValorPadrao() {
		return valorPadrao;
	}


	/**
	 * @param valorPadrao the valorPadrao to set
	 */
	public void setValorPadrao(String valorPadrao) {
		this.valorPadrao = valorPadrao;
	}

    
}
