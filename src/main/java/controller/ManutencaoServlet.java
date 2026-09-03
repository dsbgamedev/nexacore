package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dao.ManutencaoDAO;
import model.ManutencaoChamado;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "ManutencaoServlet", urlPatterns = {"/ManutencaoServlet", "/ConsultaChamadosServlet", "/api/manutencoes/*", "/api/filiais", "/api/movimentacoes"})
public class ManutencaoServlet extends HttpServlet {

private ManutencaoDAO dao = new ManutencaoDAO();
    
    // GSON CONFIGURADO COM SUPORTE A LOCALDATE
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
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                String dateStr = in.nextString();
                if (dateStr == null || dateStr.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateStr, formatter);
            }
        })
        .create();
    
    // Validação estrita apenas para ações de escrita (Inserir, Editar, Excluir)
    private boolean validarPermissaoEscrita(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        // Exige estritamente Admin, Inserir ou Editar (removemos a checagem genérica do módulo)
        boolean temEscrita = usuario != null && (
            isAdmin ||
            usuario.temPermissao("manutencao_chamados", "INSERIR") ||
            usuario.temPermissao("manutencao_chamados", "EDITAR")
        );

        if (!temEscrita) {
            String path = request.getRequestURI();
            if (path != null && path.contains("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão de escrita para este módulo.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao&modulo=manutencao_chamados");
            }
            return false;
        }
        return true;
    }
    
    // Validação branda para leitura/consulta
    private boolean validarPermissaoLeitura(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
     // Mantém apenas a checagem explícita de permissões de leitura/gestão
        boolean temLeitura = usuario != null && (
            isAdmin ||
            usuario.temPermissao("manutencao_chamados", "CONSULTAR") || 
            usuario.temPermissao("manutencao_chamados", "EDITAR") ||
            usuario.temPermissao("manutencao_chamados", "EXCLUIR")
        );

        return temLeitura;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Usuário não autenticado.\"}");
            return;
        }

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        // 1. TRATAMENTO PRIORITÁRIO NO TOPO: /api/filiais (Exige apenas estar logado)
        if ("/api/filiais".equals(servletPath) || (pathInfo != null && pathInfo.equals("/filiais"))) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter outFilial = resp.getWriter();
            try {
                if (usuario.getUnidadesPermitidasObjetos() != null) {
                    outFilial.print(gson.toJson(usuario.getUnidadesPermitidasObjetos()));
                } else {
                    outFilial.print("[]");
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                outFilial.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
            }
            return;
        }
        
        // 2. Trata a rota /api/movimentacoes de forma exata
        if ("/api/movimentacoes".equals(servletPath) || (pathInfo != null && pathInfo.equals("/movimentacoes"))) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter outMov = resp.getWriter();
            try {
                String idEquipParam = req.getParameter("idEquipamento");
                if (idEquipParam != null && !idEquipParam.isEmpty()) {
                    Long idEquipamento = Long.parseLong(idEquipParam);
                    // Troque pelo DAO/Método correto de movimentações se houver um específico:
                    // Ex: MovimentacaoEnvioDAO movDao = new MovimentacaoEnvioDAO();
                    // outMov.print(gson.toJson(movDao.listarPorEquipamento(idEquipamento)));
                    
                    outMov.print("[]"); 
                } else {
                    outMov.print("[]");
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                outMov.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
            }
            return;
        }

        boolean isAdmin = ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
     // Direciona para a tela de Abertura de Chamado (Exige Escrita)
        if ("/ManutencaoServlet".equals(servletPath)) {
            if (!validarPermissaoEscrita(req, resp)) {
                return; // Apenas retorna, pois validarPermissaoEscrita já redirecionou ou enviou o erro
            }

            if (usuario != null) {
                req.setAttribute("filiais", usuario.getUnidadesPermitidasObjetos());
                req.setAttribute("unidades", usuario.getUnidadesPermitidasObjetos());
            }

            boolean podeEditar = isAdmin || (usuario.temPermissao("manutencao_chamados", "EDITAR") || usuario.getModulosPermitidos().contains("manutencao_chamados"));
            req.setAttribute("podeEditar", podeEditar);
            req.getRequestDispatcher("/WEB-INF/jsp/manutencao-abertura.jsp").forward(req, resp);
            return;
        }
        
        // Direciona para a tela de Consulta de Chamados
        if ("/ConsultaChamadosServlet".equals(servletPath)) {
            if (!validarPermissaoLeitura(req, resp)) {
                resp.sendRedirect(req.getContextPath() + "/MenuServlet?erro=sem_permissao");
                return;
            }
            
            boolean podeEditar = isAdmin || usuario.temPermissao("manutencao_chamados", "EDITAR");
            boolean podeExcluir = isAdmin || usuario.temPermissao("manutencao_chamados", "EXCLUIR");
            
            req.setAttribute("podeEditar", podeEditar);
            req.setAttribute("podeExcluir", podeExcluir);
            req.getRequestDispatcher("/WEB-INF/jsp/consulta-chamado.jsp").forward(req, resp);
            return;
        }

        // Requisições para a API de Manutenções (/api/manutencoes/*)
        if (!validarPermissaoLeitura(req, resp)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado para consulta.\"}");
            return;
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idEquipParam = req.getParameter("idEquipamento");

            if (idEquipParam != null && !idEquipParam.isEmpty()) {
                Long idEquipamento = Long.parseLong(idEquipParam);
                List<ManutencaoChamado> historico = dao.listarPorEquipamento(idEquipamento);
                out.print(gson.toJson(historico));
            } else if (pathInfo != null && pathInfo.equals("/listar")) {
                String busca = req.getParameter("busca");
                String status = req.getParameter("status");
                String tipo = req.getParameter("tipo");
                String prioridade = req.getParameter("prioridade");

                if ((busca != null && !busca.isEmpty()) || (status != null && !status.isEmpty()) || 
                    (tipo != null && !tipo.isEmpty()) || (prioridade != null && !prioridade.isEmpty())) {
                    List<ManutencaoChamado> filtrados = dao.listarComFiltros(busca, status, tipo, prioridade);
                    out.print(gson.toJson(filtrados));
                } else {
                    List<ManutencaoChamado> todos = dao.listarTodos();
                    out.print(gson.toJson(todos));
                }
            } else {
                List<ManutencaoChamado> todos = dao.listarTodos();
                out.print(gson.toJson(todos));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validarPermissaoEscrita(req, resp)) {
            return;
        }
        
        // ADIÇÃO NECESSÁRIA: Recupera o usuário logado da sessão
        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
        	if ("/excluir".equals(pathInfo)) {
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado.\"}");
                    return;
                }

                Long idChamado = Long.parseLong(idParam);
                
                // 1. Busca o chamado no banco para saber quem é o criador/responsável
                ManutencaoChamado chamado = dao.buscarPorId(idChamado); // Certifique-se de que seu DAO possui este método
                if (chamado == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Chamado não encontrado.\"}");
                    return;
                }
                
             // 2. Valida regras de permissão e propriedade
             // Valida regras de permissão e propriedade utilizando o solicitante ou responsável
                boolean isSuperAdmin = usuario != null && "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
                boolean isAdmin = usuario != null && "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
                
                boolean isDono = usuario != null && (
                    (chamado.getSolicitante() != null && chamado.getSolicitante().equalsIgnoreCase(usuario.getUsername())) ||
                    (chamado.getResponsavelTecnico() != null && chamado.getResponsavelTecnico().equalsIgnoreCase(usuario.getUsername()))
                );

                if (!isSuperAdmin && !isAdmin && !isDono) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Acesso negado: Você só pode excluir chamados abertos ou geridos por você.\"}");
                    return;
                }

                dao.excluir(idChamado);

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado cancelado e equipamento liberado com sucesso!\"}");
                return;
            }

        	if ("/atualizar".equals(pathInfo)) {
                BufferedReader reader = req.getReader();
                ManutencaoChamado chamadoRecebido = gson.fromJson(reader, ManutencaoChamado.class);
                
                if (chamadoRecebido == null || chamadoRecebido.getIdChamado() == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado para atualização.\"}");
                    return;
                }

                // 1. Busca o chamado original no banco para validar a autoria/responsabilidade
                ManutencaoChamado chamadoOriginal = dao.buscarPorId(chamadoRecebido.getIdChamado());
                if (chamadoOriginal == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Chamado não encontrado.\"}");
                    return;
                }

                // 2. Valida regras de permissão para edição
                boolean isSuperAdmin = usuario != null && "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
                boolean isAdmin = usuario != null && "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
                
                boolean isDonoOuResponsavel = usuario != null && (
                    (chamadoOriginal.getSolicitante() != null && chamadoOriginal.getSolicitante().equalsIgnoreCase(usuario.getUsername())) ||
                    (chamadoOriginal.getResponsavelTecnico() != null && chamadoOriginal.getResponsavelTecnico().equalsIgnoreCase(usuario.getUsername()))
                );

                if (!isSuperAdmin && !isAdmin && !isDonoOuResponsavel) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Acesso negado: Você não tem permissão para editar chamados de outros usuários.\"}");
                    return;
                }

                dao.atualizar(chamadoRecebido, chamadoRecebido.isReparado());

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado atualizado com sucesso!\"}");
                return;
            }

            BufferedReader reader = req.getReader();
            ManutencaoChamado chamado = gson.fromJson(reader, ManutencaoChamado.class);

            if (chamado == null || chamado.getIdEquipamento() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso\": false, \"mensagem\": \"Dados incompletos para abertura do chamado.\"}");
                return;
            }

            if (chamado.getDataAbertura() == null) {
                chamado.setDataAbertura(LocalDate.now());
            }

            // ATRIBUIÇÃO AUTOMÁTICA: Define o usuário logado como responsável se estiver vazio
         // ATRIBUIÇÃO AUTOMÁTICA: Define o usuário logado como responsável técnico se estiver vazio
            if (usuario != null) {
                if (chamado.getResponsavelTecnico() == null || chamado.getResponsavelTecnico().trim().isEmpty()) {
                    chamado.setResponsavelTecnico(usuario.getUsername());
                }
            }

            Long idGerado = dao.inserir(chamado);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"idChamado\": " + idGerado + ", \"mensagem\": \"Chamado aberto com sucesso!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}