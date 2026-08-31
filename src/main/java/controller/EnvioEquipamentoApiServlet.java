package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dao.MovimentacaoEnvioDAO;
import model.MovimentacaoEnvio;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "EnvioEquipamentoApiServlet", urlPatterns = {"/api/envios/*"})
public class EnvioEquipamentoApiServlet extends HttpServlet {
	
	
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
    
    // Método de validação igual ao que você usou no CadastrarUsuarioServlet
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("movimentacao_envio"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão para o módulo de atributos.\"}");
            return false;
        }
        return true;
        
    }
    
    // 1. GET: Lista todos os envios para a tela de consulta
 // 1. GET: Retorna o JSON com a lista de envios ou um envio específico
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validarPermissao(req, resp)) {
            return;
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idEnvioParam = req.getParameter("idEnvio");
            
            // Se vier o parâmetro idEnvio, retorna apenas os detalhes daquele envio específico
            if (idEnvioParam != null && !idEnvioParam.isEmpty()) {
                Long idEnvio = Long.parseLong(idEnvioParam);
                List<MovimentacaoEnvio> lista = dao.listarTodos();
                MovimentacaoEnvio envioEncontrado = lista.stream()
                    .filter(e -> e.getIdEnvio().equals(idEnvio))
                    .findFirst()
                    .orElse(null);
                    
                out.print(gson.toJson(envioEncontrado));
                return;
            }

            // Comportamento padrão: lista todos em formato JSON
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
    	if (!validarPermissao(req, resp)) {
            return;
        }
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            BufferedReader reader = req.getReader();
            EnvioPayload payload = gson.fromJson(reader, EnvioPayload.class);
            
            // 1. Valida se o payload ou a lista de equipamentos está vazia primeiro
            if (payload == null || payload.equipamentosIds == null || payload.equipamentosIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Dados do envio não informados ou nenhum equipamento selecionado.\"}");
                return;
            }
            
            // 2. --- REGRA DE SEGURANÇA: VALIDA SE JÁ EXISTE ENVIO PENDENTE PARA OS EQUIPAMENTOS ---
            for (Long idEquipamento : payload.equipamentosIds) {
                boolean jaPossuiEnvioPendente = dao.existeEnvioPendenteParaEquipamento(idEquipamento);
                
                if (jaPossuiEnvioPendente) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"sucesso\": false, \"mensagem\": \"O equipamento com ID " + idEquipamento + " já possui um envio ou devolução pendente. Não é permitido duplicar envios para o mesmo item.\"}");
                    return;
                }
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
    	if (!validarPermissao(req, resp)) {
            return;
        }
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
    	if (!validarPermissao(req, resp)) {
            return;
        }
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