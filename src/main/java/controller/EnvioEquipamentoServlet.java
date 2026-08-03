package controller;

import com.google.gson.Gson; // Ou Jackson, dependendo do que usa no projeto
import dao.MovimentacaoEnvioDAO;
import model.MovimentacaoEnvio;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/envios")
public class EnvioEquipamentoServlet extends HttpServlet {

    private static class EnvioPayload {
        public String dataEnvio;
        public Long origemId;
        public Long destinoId;
        public String responsavel;
        public String transportadora;
        public String codigoRastreio;
        public String dataPrevisaoEntrega;
        public String observacoes;
        public List<Long> equipamentosIds;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            // Lendo o JSON enviado pelo Front-end
            BufferedReader reader = req.getReader();
            Gson gson = new Gson();
            EnvioPayload payload = gson.fromJson(reader, EnvioPayload.class);

            // Montando o Model
            MovimentacaoEnvio envio = new MovimentacaoEnvio();
            envio.setDataEnvio(LocalDate.parse(payload.dataEnvio));
            envio.setOrigemId(payload.origemId);
            envio.setDestinoId(payload.destinoId);
            envio.setResponsavel(payload.responsavel);
            envio.setTransportadora(payload.transportadora);
            envio.setCodigoRastreio(payload.codigoRastreio);
            if (payload.dataPrevisaoEntrega != null && !payload.dataPrevisaoEntrega.isEmpty()) {
                envio.setDataPrevisaoEntrega(LocalDate.parse(payload.dataPrevisaoEntrega));
            }
            envio.setObservacoes(payload.observacoes);

            // Salvando no Banco via DAO
            MovimentacaoEnvioDAO dao = new MovimentacaoEnvioDAO();
            Long idGerado = dao.inserir(envio, payload.equipamentosIds);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"sucesso\": true, \"idEnvio\": " + idGerado + ", \"mensagem\": \"Envio efetuado com sucesso!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Erro ao efetuar envio: " + e.getMessage() + "\"}");
        }
    }
}
