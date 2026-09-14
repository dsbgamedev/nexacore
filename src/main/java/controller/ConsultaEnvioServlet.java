package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Usuario;
import java.io.IOException;

@WebServlet("/ConsultaEnvioServlet")
public class ConsultaEnvioServlet extends HttpServlet {

    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Sessão expirada ou acesso negado.");
            return false;
        }

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
        
        // Verifica se possui permissão em alguma das variações do módulo ou via método inteligente
        boolean temPermissaoModulo = isAdmin || 
                                     usuario.temPermissao("movimentacao_envio", "CONSULTAR") || 
                                     usuario.temPermissao("movimentacao", "CONSULTAR") || 
                                     usuario.temPermissao("movimentacoes", "CONSULTAR");

        if (!temPermissaoModulo && usuario.getModulosPermitidos() != null) {
            for (String m : usuario.getModulosPermitidos()) {
                if (m.equalsIgnoreCase("movimentacao") || 
                    m.equalsIgnoreCase("movimentacoes") || 
                    m.equalsIgnoreCase("movimentacao_envio")) {
                    temPermissaoModulo = true;
                    break;
                }
            }
        }

        if (!temPermissaoModulo) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Você não possui permissão para consultar este módulo.");
            return false;
        }

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!validarPermissao(request, response)) {
            return;
        }
        
        // Encaminha de forma segura para o JSP protegido dentro de WEB-INF
        request.getRequestDispatcher("/WEB-INF/jsp/consulta-envios.jsp").forward(request, response);
    }
}