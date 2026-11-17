package dto;

import java.io.Serializable;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder; 
import com.google.gson.JsonSyntaxException; 
import util.LocalDateAdapter; 
import util.LocalTimeAdapter; 
import java.time.LocalDate; 
import java.time.LocalTime; 


/**
 * Data Transfer Object (DTO) para encapsular os critérios de filtro da consulta de fretes.
 */
public class FreteConsultaFiltro implements Serializable {

    private static final long serialVersionUID = 1L; // Boa prática para Serializable

    private String dataInicio;
    private String dataFim;
    private Integer idTransportadora;
    private String termoPesquisa;
    
    // --- NOVO CAMPO ADICIONADO PARA CORRIGIR O ERRO ---
    private String campoPesquisa; 
    
    private String sortColumn;
    private String sortDirection;
    private Integer idFilial;

    // Construtor
    public FreteConsultaFiltro() {
    }

    // --- GETTER E SETTER PARA campoPesquisa (CRÍTICO) ---
    public String getCampoPesquisa() {
        return campoPesquisa;
    }

    public void setCampoPesquisa(String campoPesquisa) {
        this.campoPesquisa = campoPesquisa;
    }

    // --- Getters e Setters existentes ---
    public Integer getIdFilial() {
        return idFilial;
    }

    public void setIdFilial(Integer idFilial) {
        this.idFilial = idFilial;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataFim() {
        return dataFim;
    }

    public void setDataFim(String dataFim) {
        this.dataFim = dataFim;
    }

    public Integer getIdTransportadora() {
        return idTransportadora;
    }

    public void setIdTransportadora(Integer idTransportadora) {
        this.idTransportadora = idTransportadora;
    }

    public String getTermoPesquisa() {
        return termoPesquisa;
    }

    public void setTermoPesquisa(String termoPesquisa) {
        this.termoPesquisa = termoPesquisa;
    }
    
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
    
    @Override
    public String toString() {
        return "FreteConsultaFiltro{" +
               "dataInicio='" + dataInicio + '\'' +
               ", dataFim='" + dataFim + '\'' +
               ", idTransportadora=" + idTransportadora +
               ", termoPesquisa='" + termoPesquisa + '\'' +
               ", campoPesquisa='" + campoPesquisa + '\'' + // Adicionado no toString
               ", sortColumn='" + sortColumn + '\'' +
               ", sortDirection='" + sortDirection + '\'' +
               ", idUnidade=" + idFilial +
               '}';
    }
}