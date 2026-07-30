package model;

import java.sql.Timestamp;

public class Marca {
    private int idMarca;
    private int idFabricante;
    private String nomeMarca;
    private String logoUrl;
    private boolean ativo;
    private Timestamp dataCadastro;
    
    // Campo auxiliar apenas para exibir o nome do fabricante na tabela de listagem (via JOIN)
    private String nomeFabricante;

    public Marca() {}

    // Getters e Setters
    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }

    public int getIdFabricante() { return idFabricante; }
    public void setIdFabricante(int idFabricante) { this.idFabricante = idFabricante; }

    public String getNomeMarca() { return nomeMarca; }
    public void setNomeMarca(String nomeMarca) { this.nomeMarca = nomeMarca; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Timestamp getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Timestamp dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getNomeFabricante() { return nomeFabricante; }
    public void setNomeFabricante(String nomeFabricante) { this.nomeFabricante = nomeFabricante; }
}