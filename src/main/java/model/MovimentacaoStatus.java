package model;

public class MovimentacaoStatus {
    private Integer id;
    private String nome;
    private String cor;
    private boolean ativo;

    public MovimentacaoStatus() {}

    public MovimentacaoStatus(Integer id, String nome, String cor, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
        this.ativo = ativo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
