package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

@WebServlet("/ConsultaEquipamentosServlet")
public class ConsultaEquipamentosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        // Valida se tem permissão de CONSULTAR no módulo "equipamentos"
        boolean temPermissao = usuario != null && usuario.temPermissao("equipamentos", "CONSULTAR");

        if (!temPermissao) {
            // Se o usuário não estiver logado ou não tiver permissão, redireciona ou avisa adequadamente
            if (usuario == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Você não possui permissão para consultar este módulo.");
            }
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarPermissao(request, response)) {
            return;
        }
        
     // Recupera o usuário logado para extrair as filiais permitidas
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
       
        
        request.getRequestDispatcher("/WEB-INF/jsp/consulta-equipamento.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarPermissao(request, response)) {
            return;
        }
        doGet(request, response);
    }
}