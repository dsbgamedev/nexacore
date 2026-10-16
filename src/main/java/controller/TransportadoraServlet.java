package controller;

import com.google.gson.Gson;
import dao.TransportadoraDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Transportadora;
import model.Usuario;
import service.TransportadoraService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet(name = "TransportadoraServlet", urlPatterns = { "/TransportadoraServlet", "/api/transportadoras/*"})
public class TransportadoraServlet extends HttpServlet {

    private Gson gson = new Gson();

    // Validação estrita para ações de escrita (Inserir, Editar, Excluir)
    private boolean validarPermissaoEscrita(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        boolean temEscrita = usuario != null && (
            isAdmin ||
            usuario.temPermissao("transportadoras", "INSERIR") ||
            usuario.temPermissao("transportadoras", "EDITAR") ||
            usuario.temPermissao("transportadoras", "EXCLUIR")
        );

        if (!temEscrita) {
            String path = request.getRequestURI();
            if (path != null && path.contains("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão de escrita para este módulo.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao&modulo=transportadoras");
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
        
        boolean temLeitura = usuario != null && (
            isAdmin ||
            usuario.temPermissao("transportadoras", "CONSULTAR") || 
            usuario.temPermissao("transportadoras", "EDITAR") ||
            usuario.temPermissao("transportadoras", "EXCLUIR")
        );

        return temLeitura;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Usuário não autenticado.\"}");
            return;
        }

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        // 1. Tratamento para /api/filiais (Exige apenas estar logado e permissão de leitura)
        if ("/api/filiais".equals(servletPath) || (pathInfo != null && pathInfo.equals("/filiais"))) {
            if (!validarPermissaoLeitura(request, response)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado.\"}");
                return;
            }
            
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter outFilial = response.getWriter();
            try {
                if (usuario.getUnidadesPermitidasObjetos() != null) {
                    outFilial.print(gson.toJson(usuario.getUnidadesPermitidasObjetos()));
                } else {
                    outFilial.print("[]");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                outFilial.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
            }
            return;
        }

        boolean isAdmin = ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));

        // 2. Direciona para a tela de Cadastro/Edição
        if ("/TransportadoraServlet".equals(servletPath)) {
            if (!validarPermissaoLeitura(request, response)) {
                response.sendRedirect(request.getContextPath() + "/menu.jsp?erro=sem_permissao&modulo=transportadoras");
                return;
            }

            try {
                TransportadoraDAO dao = new TransportadoraDAO();
                String acao = request.getParameter("acao");
                
                if (acao != null && !acao.isEmpty() && !"editar".equals(acao)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ação não permitida na URL.");
                    return;
                }

                if ("editar".equals(acao)) {
                    String idStr = request.getParameter("id");
                    if (idStr != null) {
                        Long id = Long.parseLong(idStr);
                        Transportadora t = dao.buscarPorId(id);
                        
                        request.setAttribute("transportadora", t);
                        request.setAttribute("enderecosCadastrados", dao.listarEnderecosPorTransportadora(id));
                        request.setAttribute("filiaisAssociadas", dao.listarFiliaisPorTransportadora(id));
                    }
                }

                request.setAttribute("listaTiposEndereco", dao.listarTiposEndereco());
                request.setAttribute("listaFiliais", new dao.FilialDAO().listar());                   
                
                boolean podeEditar = isAdmin || usuario.temPermissao("transportadoras", "EDITAR");
                request.setAttribute("podeEditar", podeEditar);

                request.getRequestDispatcher("/WEB-INF/jsp/cadastro-transportadora.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao carregar dados do ecrã.");
            }
            return;
        }

        // 3. Direciona para a tela de Consulta de Transportadoras
        if ("/ConsultaTransportadoraServlet".equals(servletPath)) {
            if (!validarPermissaoLeitura(request, response)) {
                response.sendRedirect(request.getContextPath() + "/menu.jsp?erro=sem_permissao&modulo=transportadoras");
                return;
            }

            boolean podeEditar = isAdmin || usuario.temPermissao("transportadoras", "EDITAR");
            boolean podeExcluir = isAdmin || usuario.temPermissao("transportadoras", "EXCLUIR");

            request.setAttribute("podeEditar", podeEditar);
            request.setAttribute("podeExcluir", podeExcluir);
            request.getRequestDispatcher("/WEB-INF/jsp/consulta-transportadora.jsp").forward(request, response);
            return;
        }

        // 4. Requisições para a API de Transportadoras (/api/transportadoras/*)
        if (!validarPermissaoLeitura(request, response)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado para consulta.\"}");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            TransportadoraDAO dao = new TransportadoraDAO();
            List<Transportadora> lista = dao.listar();
            out.print(gson.toJson(lista));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarPermissaoEscrita(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String acao = request.getParameter("acao");
        String pathInfo = request.getPathInfo();

        // Tratamento caso venha uma requisição de exclusão via API ou parâmetro
        if ((pathInfo != null && pathInfo.contains("excluir")) || "excluir".equalsIgnoreCase(acao)) {
            response.setContentType("application/json; charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                String idParam = request.getParameter("id");
                if (idParam == null || idParam.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID da transportadora não informado.\"}");
                    return;
                }

                Long id = Long.parseLong(idParam);
                new TransportadoraDAO().excluir(id);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Transportadora excluída com sucesso!\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
            }
            return;
        }

        // Ação padrão de salvar
        if ("salvar".equalsIgnoreCase(acao) || (pathInfo != null && pathInfo.equals("/salvar"))) {
            salvar(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação inválida.");
        }
    }

    private void salvar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        try {
            TransportadoraService service = new TransportadoraService();
            boolean salvo = service.salvarDaRequisicao(request);

            if (salvo) {
                response.getWriter().write("{\"sucesso\": true, \"mensagem\": \"Transportadora salva com sucesso!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Não foi possível salvar a transportadora.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }
}

    /*private void listar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            TransportadoraDAO dao = new TransportadoraDAO();
            List<Transportadora> lista = dao.listar();
            request.setAttribute("transportadoras", lista);
            request.setAttribute("listaTiposEndereco", dao.listarTiposEndereco());
            request.setAttribute("listaFiliais", new dao.FilialDAO().listar());                   
            request.getRequestDispatcher("/WEB-INF/jsp/cadastro-transportadora.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao carregar dados do ecrã.");
        }
    }*/
