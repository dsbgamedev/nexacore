package dto;

import java.util.ArrayList;
import java.util.List;
import model.ManutencaoChamado;

public class DashboardManutencaoDTO {
    public List<ManutencaoChamado> listaChamadosRecentes = new ArrayList<>();
    public int totalEmManutencao = 0;
    public int totalChamadosVencidos = 0;
}