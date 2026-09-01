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

@WebServlet(name = "ManutencaoServlet", urlPatterns = {"/ManutencaoServlet", "/ConsultaChamadosServlet", "/api/manutencoes/*"})
public class ManutencaoServlet extends HttpServlet {

    private ManutencaoDAO dao = new ManutencaoDAO();
    
    // GSON CONFIGURADO COM SUPORTE A LOCALDATE (Resolve o erro)
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
        
        // Exige explicitamente permissão de INSERIR ou EDITAR (removendo consultas genéricas)
        boolean temEscrita = usuario != null && (
            isAdmin ||
            (usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("manutencao_chamados")) ||
            usuario.temPermissao("manutencoes", "INSERIR") ||
            usuario.temPermissao("manutencoes", "EDITAR") ||
            usuario.temPermissao("manutencao", "INSERIR") ||
            usuario.temPermissao("manutencao", "EDITAR") ||
            usuario.temPermissao("chamados", "INSERIR") ||
            usuario.temPermissao("chamados", "EDITAR")
        );

        // Se o usuário tiver apenas CONSULTAR, ele não cairá aqui, logo será bloqueado.
        if (!temEscrita) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão de escrita para este módulo.\"}");
            return false;
        }
        return true;
    }

    // Validação branda para leitura/consulta
    private boolean validarPermissaoLeitura(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        boolean temLeitura = usuario != null && (
            isAdmin ||
            (usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("manutencao_chamados")) ||
            usuario.temPermissao("manutencoes", "CONSULTAR") || 
            usuario.temPermissao("manutencoes", "EDITAR") ||
            usuario.temPermissao("manutencoes", "EXCLUIR") ||
            usuario.temPermissao("manutencao", "CONSULTAR") ||
            usuario.temPermissao("chamados", "CONSULTAR") ||
            usuario.temPermissao("chamados", "EDITAR")
        );

        return temLeitura;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String servletPath = req.getServletPath();
        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        // Direciona para a tela de Abertura de Chamado (Exige Escrita)
        if ("/ManutencaoServlet".equals(servletPath)) {
        	if (!validarPermissaoEscrita(req, resp)) {
                // Redireciona imediatamente caso não tenha escrita
                resp.sendRedirect(req.getContextPath() + "/MenuServlet?erro=sem_permissao");
                return;
            }
            boolean podeEditar = isAdmin || (usuario != null && (usuario.temPermissao("manutencoes", "EDITAR") || usuario.temPermissao("chamados", "EDITAR")));
            req.setAttribute("podeEditar", podeEditar);
            req.getRequestDispatcher("/WEB-INF/jsp/manutencao-abertura.jsp").forward(req, resp);
            return;
        }
        
        // Direciona para a tela de Consulta de Chamados (Permite apenas Consulta)
        if ("/ConsultaChamadosServlet".equals(servletPath)) {
            if (!validarPermissaoLeitura(req, resp)) {
                resp.sendRedirect(req.getContextPath() + "/MenuServlet?erro=sem_permissao");
                return;
            }
            boolean podeEditar = isAdmin || (usuario != null && (usuario.temPermissao("manutencoes", "EDITAR") || usuario.temPermissao("chamados", "EDITAR")));
            boolean podeExcluir = isAdmin || (usuario != null && (usuario.temPermissao("manutencoes", "EXCLUIR") || usuario.temPermissao("chamados", "EXCLUIR")));
            
            req.setAttribute("podeEditar", podeEditar);
            req.setAttribute("podeExcluir", podeExcluir);
            req.getRequestDispatcher("/WEB-INF/jsp/consulta-chamado.jsp").forward(req, resp);
            return;
        }

        // Requisições para a API (/api/manutencoes/*) exigem pelo menos permissão de leitura
        if (!validarPermissaoLeitura(req, resp)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado para consulta.\"}");
            return;
        }
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String pathInfo = req.getPathInfo(); // Captura o que vem depois de /api/manutencoes
            String idEquipParam = req.getParameter("idEquipamento");

            if (idEquipParam != null && !idEquipParam.isEmpty()) {
                // Caso venha filtrado por equipamento específico (Histórico do Equipamento)
                Long idEquipamento = Long.parseLong(idEquipParam);
                List<ManutencaoChamado> historico = dao.listarPorEquipamento(idEquipamento);
                out.print(gson.toJson(historico));
            } else if (pathInfo != null && pathInfo.equals("/listar")) {
                // Atende a rota /api/manutencoes/listar aplicando os filtros se existirem
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
                // Se chamado sem parâmetros específicos, tenta retornar todos por segurança
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
    	// Operações de POST (Criar, Atualizar, Excluir) exigem obrigatoriamente permissão de escrita
        if (!validarPermissaoEscrita(req, resp)) {
            return;
        }
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            // Rota para excluir/cancelar o chamado
            if ("/excluir".equals(pathInfo)) {
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado.\"}");
                    return;
                }

                Long idChamado = Long.parseLong(idParam);
                dao.excluir(idChamado); // Altera o status para Cancelado e libera o equipamento

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado cancelado e equipamento liberado com sucesso!\"}");
                return;
            }

            // Rota para atualizar o chamado (vindo do modal de gerenciar)
            if ("/atualizar".equals(pathInfo)) {
                BufferedReader reader = req.getReader();
                ManutencaoChamado chamado = gson.fromJson(reader, ManutencaoChamado.class);
                
                if (chamado == null || chamado.getIdChamado() == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado para atualização.\"}");
                    return;
                }

                dao.atualizar(chamado, chamado.isReparado());

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado atualizado com sucesso!\"}");
                return;
            }

            // Fluxo normal de Abertura de Chamado (caso POST seja feito diretamente na raiz)
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