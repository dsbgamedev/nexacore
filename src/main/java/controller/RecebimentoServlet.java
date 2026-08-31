package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Usuario;
import java.io.IOException;

@WebServlet(urlPatterns = {"/RecebimentoServlet", "/DevolucaoServlet"})
public class RecebimentoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("movimentacao"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }

        // Se o usuário acessou via /DevolucaoServlet, podemos passar um atributo opcional ou deixar o JS ler a URL
        request.getRequestDispatcher("/WEB-INF/jsp/recebimento-equipamento.jsp").forward(request, response);
    }
}