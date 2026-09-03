package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Usuario;
import java.io.IOException;

@WebServlet("/ConsultaEnvioServlet")
public class ConsultaEnvioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        // Aceita as diferentes variações que podem vir do banco/sessão
        boolean temPermissaoModulo = false;
        if (usuario != null && usuario.getModulosPermitidos() != null) {
            for (String m : usuario.getModulosPermitidos()) {
                if (m.equalsIgnoreCase("movimentacao") || 
                    m.equalsIgnoreCase("movimentacoes") || 
                    m.equalsIgnoreCase("movimentacao_envio")) {
                    temPermissaoModulo = true;
                    break;
                }
            }
        }
        /*boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("movimentacao_envio");
        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }*/
       
        // Encaminha de forma segura para o JSP protegido dentro de WEB-INF
        request.getRequestDispatcher("/WEB-INF/jsp/consulta-envios.jsp").forward(request, response);
    }
}
