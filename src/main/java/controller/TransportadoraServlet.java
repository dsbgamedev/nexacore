package controller;

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
import java.util.List;
import java.util.Map;

@WebServlet("/TransportadoraServlet")
public class TransportadoraServlet extends HttpServlet {

    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoEspecifica) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Sessão expirada ou acesso negado.\"}");
            return false;
        }

        boolean temPermissao = usuario.temPermissao("transportadoras", acaoEspecifica);
        if (!temPermissao) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Não possui permissão de " + acaoEspecifica + " neste módulo.\"}");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Permite carregar a tela de cadastro normalmente ou com parâmetro de edição se houver
        String acao = request.getParameter("acao");
        
        if (acao != null && !acao.isEmpty() && !"editar".equals(acao)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ação não permitida na URL.");
            return;
        }

        if (!validarPermissao(request, response, "CONSULTAR")) {
            response.sendRedirect(request.getContextPath() + "/menu.jsp?erro=sem_permissao");
            return;
        }

        if ("editar".equals(acao)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr);
                    TransportadoraDAO dao = new TransportadoraDAO();
                    Transportadora t = dao.buscarPorId(id);
                    request.setAttribute("transportadoraParaEdicao", t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        listar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String acao = request.getParameter("acao");

        if ("salvar".equalsIgnoreCase(acao)) {
            if (!validarPermissao(request, response, "INSERIR") && !validarPermissao(request, response, "EDITAR")) {
                return;
            }
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

    private void listar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }
}