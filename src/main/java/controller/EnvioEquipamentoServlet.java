package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dao.MovimentacaoEnvioDAO;
import model.MovimentacaoEnvio;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "EnvioEquipamentoServlet", urlPatterns = {"/api/envios/*"})
public class EnvioEquipamentoServlet extends HttpServlet {

    private MovimentacaoEnvioDAO dao = new MovimentacaoEnvioDAO();
    
    // Gson blindado para o Java 17+ utilizando um TypeAdapter seguro para LocalDate
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(formatter.format(value));
                }
            }

            @Override
            public LocalDate read(JsonReader in) throws IOException {
                String str = in.nextString();
                return str != null ? LocalDate.parse(str, formatter) : null;
            }
        })
        // Adicionado TypeAdapter para LocalDateTime para suportar a classe MovimentacaoHistorico
        .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(formatter.format(value));
                }
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                String str = in.nextString();
                return str != null ? LocalDateTime.parse(str, formatter) : null;
            }
        })
        .disableHtmlEscaping()
        .create();

    private static class EnvioPayload {
        public String dataEnvio;
        public Long origemId;
        public Long destinoId;
        public String responsavel;
        public String transportadora;
        public String codigoRastreio;
        public String numeroNota; // Adicionado para receber a Nota Fiscal do Front-end
        public String dataPrevisaoEntrega;
        public String observacoes;
        public Long statusId; 
        public List<Long> equipamentosIds;
    }

    // 1. GET: Lista todos os envios para a tela de consulta
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idEnvioParam = req.getParameter("idEnvio");
            
            // Se vier o parâmetro idEnvio, podemos retornar apenas os detalhes/produtos daquele envio específico
            if (idEnvioParam != null && !idEnvioParam.isEmpty()) {
                Long idEnvio = Long.parseLong(idEnvioParam);
                // Opcional: crie um método no DAO que busca o envio por ID com seus produtos, 
                // ou filtre da lista geral. Para simplificar, vamos buscar na lista geral:
                List<MovimentacaoEnvio> lista = dao.listarTodos();
                MovimentacaoEnvio envioEncontrado = lista.stream()
                    .filter(e -> e.getIdEnvio().equals(idEnvio))
                    .findFirst()
                    .orElse(null);
                    
                out.print(gson.toJson(envioEncontrado));
                return;
            }

            // Comportamento padrão: lista todos
            List<MovimentacaoEnvio> lista = dao.listarTodos();
            out.print(gson.toJson(lista));
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar envios: " + e.getMessage());
            out.print(gson.toJson(erro));
        }
    }

    // 2. POST: Cadastra o novo envio
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            BufferedReader reader = req.getReader();
            EnvioPayload payload = gson.fromJson(reader, EnvioPayload.class);
            if (payload == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Dados do envio não informados (Payload vazio).\"}");
                return;
            }

            // Verifica se a requisição veio indicando devolução via parâmetro na URL
            String tipoParam = req.getParameter("tipo");
            boolean ehDevolucao = "devolucao".equals(tipoParam);

            MovimentacaoEnvio envio = new MovimentacaoEnvio();
            envio.setDataEnvio(LocalDate.parse(payload.dataEnvio));
            envio.setOrigemId(payload.origemId);
            envio.setDestinoId(payload.destinoId);
            envio.setResponsavel(payload.responsavel);
            envio.setTransportadora(payload.transportadora);
            
            // --- REGRA DE OURO PARA DEVOLUÇÃO: FORÇA O PREFIXO DEV- ---
            String codigoRastreioFinal = payload.codigoRastreio;
            if (ehDevolucao) {
                if (codigoRastreioFinal == null || codigoRastreioFinal.trim().isEmpty()) {
                    codigoRastreioFinal = "DEV-" + System.currentTimeMillis();
                } else if (!codigoRastreioFinal.startsWith("DEV-")) {
                    codigoRastreioFinal = "DEV-" + codigoRastreioFinal;
                }
            }
            envio.setCodigoRastreio(codigoRastreioFinal);
            // ----------------------------------------------------------

            envio.setNumeroNota(payload.numeroNota); 
            
            if (payload.dataPrevisaoEntrega != null && !payload.dataPrevisaoEntrega.isEmpty()) {
                envio.setDataPrevisaoEntrega(LocalDate.parse(payload.dataPrevisaoEntrega));
            }
            envio.setObservacoes(payload.observacoes);
            
            // --- REGRA DE OURO PARA STATUS: SE FOR DEVOLUÇÃO, TRAVA OBRIGATORIAMENTE EM 1L (Aguardando Envio) ---
            if (ehDevolucao) {
                envio.setStatusId(1L); // Força Aguardando Envio para devoluções
            } else {
                envio.setStatusId(payload.statusId != null ? payload.statusId : 1L);
            }
            // --------------------------------------------------------------------------------------------------

            Long idGerado = dao.inserir(envio, payload.equipamentosIds);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"idEnvio\": " + idGerado + ", \"mensagem\": \"Envio efetuado com sucesso!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK); 
            
            String mensagemErro = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
            mensagemErro = mensagemErro.replace("\"", "'").replace("\n", " ");

            out.print("{\"sucesso\": false, \"mensagem\": \"" + mensagemErro + "\"}");
        }
    }

 // 3. PUT: Realiza a efetivação do envio (em trânsito) ou baixa/confirmação de recebimento
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String acao = req.getParameter("acao");
            String idEnvioStr = req.getParameter("idEnvio");

            if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"ID do envio não informado.\"}");
                return;
            }

            Long idEnvio = Long.parseLong(idEnvioStr);

            // Cenário 1: Efetivar Envio (Coloca em trânsito e muda status dos equipamentos)
            if ("efetivar".equals(acao)) {
                dao.efetivarEnvio(idEnvio);

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Envio efetivado com sucesso! Os equipamentos estão em trânsito.\"}");
                return;
            }

            // Cenário 2: Confirmar Recebimento / Baixa final na filial de destino
            String destinoIdStr = req.getParameter("destinoId");
            if (destinoIdStr == null || destinoIdStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Parâmetros inválidos para baixa. Destino não informado.\"}");
                return;
            }

            Long destinoId = Long.parseLong(destinoIdStr);
            dao.confirmarRecebimento(idEnvio, destinoId);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"mensagem\": \"Recebimento confirmado com sucesso! Equipamentos atualizados para a nova filial.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro na operação";
            out.print("{\"sucesso\": false, \"mensagem\": \"" + msg + "\"}");
        }
    }

    // 4. DELETE: Cancela/Exclui o envio e retorna os equipamentos para a filial de origem
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idEnvioStr = req.getParameter("idEnvio");

            if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"ID do envio não informado para cancelamento.\"}");
                return;
            }

            Long idEnvio = Long.parseLong(idEnvioStr);
            dao.cancelarEnvio(idEnvio);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"mensagem\": \"Envio cancelado com sucesso! Os equipamentos retornaram à filial de origem.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro ao cancelar envio";
            out.print("{\"sucesso\": false, \"mensagem\": \"" + msg + "\"}");
        }
    }
}