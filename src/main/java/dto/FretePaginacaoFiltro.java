package dto;

import java.io.Serializable;
import java.time.LocalDate;

public class FretePaginacaoFiltro implements Serializable {

    private String sortColumn;
    private String sortDirection;
    private int limite;
    private int offset;
    private String termoPesquisa;
    private LocalDate dataConsulta; // Para filtrar fretes do dia

    // Construtor vazio
    public FretePaginacaoFiltro() {}

    // Getters e Setters
    
    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getTermoPesquisa() {
        return termoPesquisa;
    }

    public void setTermoPesquisa(String termoPesquisa) {
        this.termoPesquisa = termoPesquisa;
    }

    public LocalDate getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDate dataConsulta) {
        this.dataConsulta = dataConsulta;
    }
}