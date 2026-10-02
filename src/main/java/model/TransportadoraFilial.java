package model;

public class TransportadoraFilial {
    private int idFilial;
    private int enderecoIndice; // Índice do endereço selecionado no array do front-end

    public int getIdFilial() {
        return idFilial;
    }
    public void setIdFilial(int idFilial) {
        this.idFilial = idFilial;
    }
    public int getEnderecoIndice() {
        return enderecoIndice;
    }
    public void setEnderecoIndice(int enderecoIndice) {
        this.enderecoIndice = enderecoIndice;
    }
}
