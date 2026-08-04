package model;

public class StatusEquipamento {
    private int id;
    private String nome;
    private String cor;
    private boolean ativo;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
