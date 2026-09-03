package controller;

import com.google.gson.Gson;
import dao.AuditoriaDAO;
import model.Auditoria;
import model.Usuario;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/AuditoriaServlet", "/auditoria"})
public class AuditoriaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        // Apenas SUPER_ADMINISTRADOR pode acessar auditoria
        boolean isSuperAdmin = usuario != null && usuario.getPerfil() != null && 
                               usuario.getPerfil().toUpperCase().contains("SUPER");

        if (!isSuperAdmin) {
            String acceptHeader = request.getHeader("Accept");
            String requestedWith = request.getHeader("X-Requested-With");
            
            if ((acceptHeader != null && acceptHeader.contains("application/json")) || 
                "XMLHttpRequest".equals(requestedWith)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado! Apenas Super Administradores podem acessar o módulo de auditoria.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao_auditoria");
            }
            return false;
        }
        return true;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = request.getParameter("acao");
        
        if (!validarPermissao(request, response)) {
            return;
        }
    
        // 1. Rota para retornar detalhes em JSON para o modal da tela
        if ("detalhes".equals(acao)) {
            String idParam = request.getParameter("id");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            if (idParam != null && !idParam.isEmpty()) {
                Long id = Long.parseLong(idParam);
                AuditoriaDAO dao = new AuditoriaDAO();
                Auditoria auditoria = dao.buscarPorId(id);
                response.getWriter().write(new Gson().toJson(auditoria));
            } else {
                response.getWriter().write("{\"erro\": \"ID não informado\"}");
            }
            return;
        }

        // 2. Parâmetros de Filtro vindos da interface
        String usuario = request.getParameter("usuario");
        String modulo = request.getParameter("modulo");
        String tipoAcao = request.getParameter("tipoAcao");
        String entidade = request.getParameter("entidade");
        String dataInicio = request.getParameter("dataInicio");
        String dataFim = request.getParameter("dataFim");
        String ipOrigem = request.getParameter("ipOrigem");

        AuditoriaDAO dao = new AuditoriaDAO();
        List<Auditoria> logs = dao.listarComFiltros(usuario, modulo, tipoAcao, entidade, dataInicio, dataFim);
        int totalRegistros = logs.size();

        request.setAttribute("listaAuditoria", logs);
        request.setAttribute("totalRegistros", totalRegistros);
        
        request.getRequestDispatcher("/WEB-INF/jsp/auditoria.jsp").forward(request, response);
    }
}