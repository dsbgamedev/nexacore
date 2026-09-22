package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import dao.EquipamentoDAO;
import dao.FilialDAO;
import dao.MovimentacaoEnvioDAO;
import model.MovimentacaoEnvio;
import model.Usuario;
import util.ValidadorContextoUtil;
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
    private EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
    private FilialDAO filialDAO = new FilialDAO();
    
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
        public String numeroNota; 
        public String dataPrevisaoEntrega;
        public String observacoes;
        public Long statusId; 
        public List<Long> equipamentosIds;
    }
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoEspecifica) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Sessão expirada ou acesso negado.\"}");
            return false;
        }

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
        boolean temPermissao = isAdmin || usuario.temPermissao("movimentacao_envio", acaoEspecifica);

        if (!temPermissao) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão de " + acaoEspecifica + " no módulo de envios.\"}");
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (!validarPermissao(req, resp, "CONSULTAR")) {
            return;
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession(false);
            Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

            String idEnvioParam = req.getParameter("idEnvio");
            String tipoParam = req.getParameter("tipo");
            boolean ehDevolucao = "devolucao".equals(tipoParam);
            
            if (idEnvioParam != null && !idEnvioParam.isEmpty()) {
                Long idEnvio = Long.parseLong(idEnvioParam);
                
                List<MovimentacaoEnvio> lista = dao.listarComFiltrosPorUsuario(null, null, null, usuarioLogado);
                if (ehDevolucao) {
                    lista.removeIf(e -> e.getStatusId() != null && e.getStatusId().equals(5L));
                }
                
                MovimentacaoEnvio envioEncontrado = lista.stream()
                    .filter(e -> e.getIdEnvio().equals(idEnvio))
                    .findFirst()
                    .orElse(null);
                
                if (envioEncontrado == null) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(gson.toJson(Map.of("sucesso", false, "mensagem", "Acesso negado: Este envio pertence a uma filial diferente da sua filial ativa.")));
                    return;
                }
                    
                out.print(gson.toJson(envioEncontrado));
                return;
            }

            String statusFiltro = req.getParameter("status");
            String dataInicioStr = req.getParameter("dataInicio");
            String dataFimStr = req.getParameter("dataFim");

            List<MovimentacaoEnvio> listaFiltrada = dao.listarComFiltrosPorUsuario(statusFiltro, dataInicioStr, dataFimStr, usuarioLogado);

            if (ehDevolucao) {
                listaFiltrada.removeIf(e -> e.getStatusId() != null && e.getStatusId().equals(5L));
            }

            out.print(gson.toJson(listaFiltrada));
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar envios: " + e.getMessage());
            out.print(gson.toJson(erro));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String acao = req.getParameter("acao");
        String permissaoNecessaria = "INSERIR";
        if ("iniciarDevolucao".equals(acao) || "cancelarDevolucao".equals(acao)) {
            permissaoNecessaria = "EDITAR";
        }
        
        if (!validarPermissao(req, resp, permissaoNecessaria)) {
            return;
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        if ("iniciarDevolucao".equals(acao) || "cancelarDevolucao".equals(acao)) {
            EnvioPayload payload = gson.fromJson(req.getReader(), EnvioPayload.class);
            
            if (payload == null || payload.equipamentosIds == null || payload.equipamentosIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Nenhum equipamento selecionado.\"}");
                return;
            }

            try {
                for (Long idEqp : payload.equipamentosIds) {
                    if ("iniciarDevolucao".equals(acao)) {
                        equipamentoDAO.atualizarStatusParaDevolucao(idEqp);
                    } else {
                        equipamentoDAO.reverterStatusDevolucao(idEqp);
                    }
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Operação realizada com sucesso.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"sucesso\": false, \"mensagem\": \"Erro: " + e.getMessage() + "\"}");
            }
            return;
        }
        
        try {
            BufferedReader reader = req.getReader();
            EnvioPayload payload = gson.fromJson(reader, EnvioPayload.class);
            
            if (payload == null || payload.equipamentosIds == null || payload.equipamentosIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Dados do envio não informados ou nenhum equipamento selecionado.\"}");
                return;
            }
            
            if (payload.origemId != null) {
                Long idFilialReal = filialDAO.buscarIdFilialPorOrigemCodigo(payload.origemId.intValue());
                if (idFilialReal == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Filial de origem não encontrada para o código informado.\"}");
                    return;
                }
                payload.origemId = idFilialReal;
            }

            if (payload.destinoId != null) {
                Long idFilialRealDestino = filialDAO.buscarIdFilialPorOrigemCodigo(payload.destinoId.intValue());
                if (idFilialRealDestino == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Filial de destino não encontrada para o código informado.\"}");
                    return;
                }
                payload.destinoId = idFilialRealDestino;
            }
            
            if (payload.origemId != null && payload.origemId.equals(payload.destinoId)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"A operação não pode ser realizada: A unidade de origem e a unidade de destino não podem ser iguais.\"}");
                return;
            }

            if (payload.origemId != null) {
                try {
                    ValidadorContextoUtil.validarFilialAtiva(req, payload.origemId.intValue());
                } catch (SecurityException se) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"sucesso\": false, \"mensagem\": \"" + se.getMessage() + "\"}");
                    return;
                }
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

            Long idGerado;
            if (ehDevolucao) {
                idGerado = dao.inserirDevolucao(envio, payload.equipamentosIds);
            } else {
                idGerado = dao.inserir(envio, payload.equipamentosIds);
            }

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
        if (!validarPermissao(req, resp, "EDITAR")) {
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
            
            List<MovimentacaoEnvio> listaPermitida = dao.listarComFiltrosPorUsuario(null, null, null, usuario);
            MovimentacaoEnvio envioAlvo = listaPermitida.stream()
                .filter(e -> e.getIdEnvio().equals(idEnvio))
                .findFirst()
                .orElse(null);

            if (envioAlvo == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"sucesso\": false, \"mensagem\": \"Ação negada: Você não possui permissão ou este envio pertence a uma filial diferente da sua filial ativa na sessão.\"}");
                return;
            }

            // =========================================================================
            // TRAVA DE SEGURANÇA ATIVADA: Valida se a filial ativa no menu bate com a origem
            // =========================================================================
            if (envioAlvo.getOrigemId() != null) {
                try {
                    // Usando o nome correto do método presente no FilialDAO:
                    Integer codigoOrigem = filialDAO.buscarOrigemCodigoPorId(envioAlvo.getOrigemId().intValue());
                    if (codigoOrigem != null) {
                        ValidadorContextoUtil.validarFilialAtiva(req, codigoOrigem);
                    }
                } catch (SecurityException se) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"sucesso\": false, \"mensagem\": \"" + se.getMessage().replace("\"", "'") + "\"}");
                    return;
                }
            }
            // =========================================================================

            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            if ("efetivar".equals(acao)) {
                String nomeResponsavelEnvio = (usuario != null && usuario.getNomeCompleto() != null && !usuario.getNomeCompleto().isEmpty()) 
                    ? usuario.getNomeCompleto() 
                    : (usuario != null && usuario.getUsername() != null ? usuario.getUsername() : "Sistema");

                dao.efetivarEnvio(idEnvio, nomeResponsavelEnvio);

                if (usuario != null) {
                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Movimentação de Envio",
                        "EDITAR",
                        "movimentacao_envio",
                        idEnvio,
                        "Efetivação de envio por " + nomeResponsavelEnvio + " (Equipamentos em trânsito)",
                        "{\"statusId\": 1}",
                        "{\"statusId\": 2, \"status\": \"Em Trânsito\", \"responsavelEnvio\": \"" + nomeResponsavelEnvio + "\"}",
                        ipCliente
                    );
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Envio efetivado com sucesso por " + nomeResponsavelEnvio + "! Os equipamentos estão em trânsito.\"}");
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
        if (!validarPermissao(req, resp, "EXCLUIR")) {
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

            HttpSession session = req.getSession(false);
            Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

            // Busca segura por usuário para evitar erros de sintaxe e validar escopo
            List<MovimentacaoEnvio> listaPermitida = dao.listarComFiltrosPorUsuario(null, null, null, usuario);
            MovimentacaoEnvio envioEncontrado = listaPermitida.stream()
                .filter(e -> e.getIdEnvio().equals(idEnvio))
                .findFirst()
                .orElse(null);

            if (envioEncontrado == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"sucesso\": false, \"mensagem\": \"Ação negada: Você não possui permissão ou este envio pertence a uma filial diferente.\"}");
                return;
            }

            // =========================================================================
            // TRAVA DE SEGURANÇA ATIVADA: Valida se a filial ativa no menu bate com a origem
            // =========================================================================
            if (envioEncontrado.getOrigemId() != null) {
                try {
                    Integer codigoOrigem = filialDAO.buscarOrigemCodigoPorId(envioEncontrado.getOrigemId().intValue());
                    if (codigoOrigem != null) {
                        ValidadorContextoUtil.validarFilialAtiva(req, codigoOrigem);
                    }
                } catch (SecurityException se) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"sucesso\": false, \"mensagem\": \"" + se.getMessage().replace("\"", "'") + "\"}");
                    return;
                }
            }
            // =========================================================================

            boolean ehDevolucao = envioEncontrado.getCodigoRastreio() != null && 
                                  envioEncontrado.getCodigoRastreio().startsWith("DEV-");

            if (ehDevolucao) {
                dao.cancelarDevolucao(idEnvio);
            } else {
                dao.cancelarEnvio(idEnvio);
            }

            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            if (usuario != null) {
                String descAcao = ehDevolucao ? "Cancelamento de devolução de equipamentos" : "Cancelamento de envio de equipamentos";
                String msgRetorno = ehDevolucao ? "{\"status\": \"Cancelado / Retornado ao status ativo\"}" : "{\"status\": \"Cancelado / Retornado à origem\"}";
                
                util.AuditoriaService.registrar(
                    Long.valueOf(usuario.getId()),
                    usuario.getUsername(),
                    "Movimentacao de Envio",
                    "EXCLUIR",
                    "movimentacao_envio",
                    idEnvio,
                    descAcao,
                    "{\"idEnvio\": " + idEnvio + "}",
                    msgRetorno,
                    ipCliente
                );
            }

            String mensagemSucesso = ehDevolucao ? "Devolução cancelada com sucesso! Os equipamentos retornaram ao status ativo." : "Envio cancelado com sucesso! Os equipamentos retornaram à filial de origem.";
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"mensagem\": \"" + mensagemSucesso + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_OK);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro ao cancelar movimentação";
            out.print("{\"sucesso\": false, \"mensagem\": \"" + msg + "\"}");
        }
    }
}