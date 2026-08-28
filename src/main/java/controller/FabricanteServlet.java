package controller;

import com.google.gson.Gson;
import dao.FabricanteDAO;
import model.Fabricante;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Mapeamento duplo: atende tanto a abertura da tela quanto as chamadas de API (AJAX)
@WebServlet(urlPatterns = {"/FabricanteServlet", "/api/fabricantes/*"})
public class FabricanteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FabricanteDAO dao = new FabricanteDAO();
    private Gson gson = new Gson();

    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, boolean isApi) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("fabricantes"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            if (isApi) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão para executar esta ação.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado ao módulo de fabricantes.");
            }
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        boolean isApi = servletPath.startsWith("/api/");

        if (!validarPermissao(request, response, isApi)) {
            return;
        }

        // 1. Fluxo MVC Tradicional: Renderiza a tela JSP
        if ("/FabricanteServlet".equals(servletPath)) {
            request.getRequestDispatcher("/WEB-INF/jsp/fabricantes.jsp").forward(request, response);
            return;
        }

        // 2. Fluxo API: Retorna JSON (ex: /api/fabricantes/listar)
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/listar".equals(path) || path == null) {
            try {
                List<Fabricante> lista = dao.listarTodos();
                response.getWriter().write(gson.toJson(lista));
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro ao listar fabricantes.\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!validarPermissao(request, response, true)) return;
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/salvar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro ao ler dados.");
                return;
            }

            Fabricante fab = gson.fromJson(sb.toString(), Fabricante.class);

            try {
                dao.salvar(fab);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Fabricante salvo com sucesso!\"}");
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!validarPermissao(request, response, true)) return;

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                dao.excluir(id);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Fabricante excluído com sucesso!\"}");
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String msg = (e.getSQLState() != null && e.getSQLState().startsWith("23")) 
                    ? "Não é possível excluir este fabricante pois ele está vinculado a marcas." 
                    : "Erro ao excluir o registro.";
                response.getWriter().write("{\"erro\": \"" + msg + "\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"Erro inesperado ao excluir o registro.\"}");
            }
        }
    }
}