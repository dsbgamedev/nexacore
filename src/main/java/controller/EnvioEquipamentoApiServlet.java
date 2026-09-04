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

            // Captura os filtros enviados pela interface
            String statusFiltro = req.getParameter("status");
            String dataInicioStr = req.getParameter("dataInicio");
            String dataFimStr = req.getParameter("dataFim");

            // Executa a busca filtrada diretamente no Banco de Dados via DAO
            List<MovimentacaoEnvio> listaFiltrada = dao.listarComFiltros(statusFiltro, dataInicioStr, dataFimStr);

            out.print(gson.toJson(listaFiltrada));
            
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
            
            if (payload == null || payload.equipamentosIds == null || payload.equipamentosIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Dados do envio não informados ou nenhum equipamento selecionado.\"}");
                return;
            }
            
            for (Long idEquipamento : payload.equipamentosIds) {
                boolean jaPossuiEnvioPendente = dao.existeEnvioPendenteParaEquipamento(idEquipamento);
                
                if (jaPossuiEnvioPendente) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"sucesso\": false, \"mensagem\": \"O equipamento com ID " + idEquipamento + " já possui um envio ou devolução pendente. Não é permitido duplicar envios para o mesmo item.\"}");
                    return;
                }
            }

            String tipoParam = req.getParameter("tipo");
            boolean ehDevolucao = "devolucao".equals(tipoParam);

            MovimentacaoEnvio envio = new MovimentacaoEnvio();
            envio.setDataEnvio(LocalDate.parse(payload.dataEnvio));
            envio.setOrigemId(payload.origemId);
            envio.setDestinoId(payload.destinoId);
            envio.setResponsavel(payload.responsavel);
            envio.setTransportadora(payload.transportadora);
            
            String codigoRastreioFinal = payload.codigoRastreio;
            if (ehDevolucao) {
                if (codigoRastreioFinal == null || codigoRastreioFinal.trim().isEmpty()) {
                    codigoRastreioFinal = "DEV-" + System.currentTimeMillis();
                } else if (!codigoRastreioFinal.startsWith("DEV-")) {
                    codigoRastreioFinal = "DEV-" + codigoRastreioFinal;
                }
            }
            envio.setCodigoRastreio(codigoRastreioFinal);

            envio.setNumeroNota(payload.numeroNota); 
            
            if (payload.dataPrevisaoEntrega != null && !payload.dataPrevisaoEntrega.isEmpty()) {
                envio.setDataPrevisaoEntrega(LocalDate.parse(payload.dataPrevisaoEntrega));
            }
            envio.setObservacoes(payload.observacoes);
            
            if (ehDevolucao) {
                envio.setStatusId(1L); 
            } else {
                envio.setStatusId(payload.statusId != null ? payload.statusId : 1L);
            }

            Long idGerado = dao.inserir(envio, payload.equipamentosIds);

            // --- REGISTRO DE AUDITORIA (CRIAR ENVIO/DEVOLUÇÃO) ---
            HttpSession session = req.getSession(false);
            Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            if (usuario != null) {
                String acaoDesc = ehDevolucao ? "Cadastro de nova devolução de equipamentos" : "Cadastro de novo envio de equipamentos";
                util.AuditoriaService.registrar(
                    Long.valueOf(usuario.getId()),
                    usuario.getUsername(),
                    "Movimentação de Envio",
                    "CRIAR",
                    "movimentacao_envio",
                    idGerado,
                    acaoDesc,
                    "{}",
                    gson.toJson(envio),
                    ipCliente
                );
            }
            // ------------------------------------------------------

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

            HttpSession session = req.getSession(false);
            Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            if ("efetivar".equals(acao)) {
                dao.efetivarEnvio(idEnvio);

                // --- REGISTRO DE AUDITORIA (EFETIVAR ENVIO) ---
                if (usuario != null) {
                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Movimentação de Envio",
                        "EDITAR",
                        "movimentacao_envio",
                        idEnvio,
                        "Efetivação de envio (Equipamentos em trânsito)",
                        "{\"statusId\": 1}",
                        "{\"statusId\": 2, \"status\": \"Em Trânsito\"}",
                        ipCliente
                    );
                }
                // ----------------------------------------------

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Envio efetivado com sucesso! Os equipamentos estão em trânsito.\"}");
                return;
            }

            String destinoIdStr = req.getParameter("destinoId");
            if (destinoIdStr == null || destinoIdStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Parâmetros inválidos para baixa. Destino não informado.\"}");
                return;
            }

            Long destinoId = Long.parseLong(destinoIdStr);
            dao.confirmarRecebimento(idEnvio, destinoId);

            // --- REGISTRO DE AUDITORIA (CONFIRMAR RECEBIMENTO) ---
            if (usuario != null) {
                util.AuditoriaService.registrar(
                    Long.valueOf(usuario.getId()),
                    usuario.getUsername(),
                    "Movimentação de Envio",
                    "EDITAR",
                    "movimentacao_envio",
                    idEnvio,
                    "Confirmação de recebimento / baixa final na filial destino",
                    "{\"statusId\": 2}",
                    "{\"statusId\": 3, \"status\": \"Entregue / Concluído\", \"destinoId\": " + destinoId + "}",
                    ipCliente
                );
            }
            // ----------------------------------------------------

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"mensagem\": \"Recebimento confirmado com sucesso! Equipamentos atualizados para a nova filial.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro na operação";
            out.print("{\"sucesso\": false, \"mensagem\": \"" + msg + "\"}");
        }
    }

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

            // --- REGISTRO DE AUDITORIA (CANCELAR ENVIO) ---
            HttpSession session = req.getSession(false);
            Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            if (usuario != null) {
                util.AuditoriaService.registrar(
                    Long.valueOf(usuario.getId()),
                    usuario.getUsername(),
                    "Movimentação de Envio",
                    "EXCLUIR",
                    "movimentacao_envio",
                    idEnvio,
                    "Cancelamento de envio de equipamentos",
                    "{\"idEnvio\": " + idEnvio + "}",
                    "{\"status\": \"Cancelado / Retornado à origem\"}",
                    ipCliente
                );
            }
            // ----------------------------------------------

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