package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.TransportadoraDAO;
import jakarta.servlet.http.HttpServletRequest;
import model.Transportadora;
import model.TransportadoraEndereco;
import model.TransportadoraFilial; // <-- Novo Import

import java.lang.reflect.Type;
import java.util.List;

public class TransportadoraService {

    private TransportadoraDAO transportadoraDAO;

    public TransportadoraService() {
        this.transportadoraDAO = new TransportadoraDAO();
    }

    public boolean salvarDaRequisicao(HttpServletRequest request) throws Exception {
        // 1. Mapeia os dados principais da transportadora
        Transportadora t = new Transportadora();
        t.setRazaoSocial(request.getParameter("razaoSocial"));
        t.setNomeFantasia(request.getParameter("nomeFantasia"));
        t.setCnpj(request.getParameter("cnpj"));
        t.setInscricaoEstadual(request.getParameter("inscricaoEstadual"));
        t.setRntrc(request.getParameter("rntrc"));
        t.setTelefone(request.getParameter("telefone"));
        t.setCelular(request.getParameter("celular"));
        t.setEmail(request.getParameter("email"));
        t.setSite(request.getParameter("site"));
        t.setStatus(request.getParameter("status"));
        t.setObservacao(request.getParameter("observacao"));

        Gson gson = new Gson();

        // 2. Converte o JSON dos endereços
        String jsonEnderecos = request.getParameter("enderecosJson");
        Type listEnderecoType = new TypeToken<List<TransportadoraEndereco>>(){}.getType();
        List<TransportadoraEndereco> enderecos = gson.fromJson(jsonEnderecos, listEnderecoType);

        if (enderecos == null || enderecos.isEmpty()) {
            throw new IllegalArgumentException("A transportadora deve possuir pelo menos um endereço.");
        }

        // 3. Converte o JSON das filiais atendidas
        String jsonFiliais = request.getParameter("filiaisJson");
        Type listFilialType = new TypeToken<List<TransportadoraFilial>>(){}.getType();
        List<TransportadoraFilial> filiais = gson.fromJson(jsonFiliais, listFilialType);

        if (filiais == null || filiais.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos uma filial atendida.");
        }

        // 4. Delega para o DAO realizar a persistência completa (Transportadora + Endereços + Filiais)
        return transportadoraDAO.salvarComEnderecosEFiliais(t, enderecos, filiais);// *Ajuste o nome do método no seu DAO se necessário
    }
}