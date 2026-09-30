package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.TransportadoraDAO;
import jakarta.servlet.http.HttpServletRequest;
import model.Transportadora;
import model.TransportadoraEndereco;

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

        // 2. Converte o JSON dos endereços usando o Gson
        String jsonEnderecos = request.getParameter("enderecosJson");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<TransportadoraEndereco>>(){}.getType();
        List<TransportadoraEndereco> enderecos = gson.fromJson(jsonEnderecos, listType);

        // 3. Validações de regra de negócio (ex: garantir que tem endereços)
        if (enderecos == null || enderecos.isEmpty()) {
            throw new IllegalArgumentException("A transportadora deve possuir pelo menos um endereço.");
        }

        // 4. Delega para o DAO realizar a persistência no banco de dados
        return transportadoraDAO.salvarComEnderecos(t, enderecos);
    }
}