package dto;

public class ModuloDTO {
    private int id;
    private String nomeModulo;

    // Construtor público que recebe (int, String)
    public ModuloDTO(int id, String nomeModulo) {
        this.id = id;
        this.nomeModulo = nomeModulo;
    }

    public int getId() {
        return id;
    }

    public String getNomeModulo() {
        return nomeModulo;
    }
}