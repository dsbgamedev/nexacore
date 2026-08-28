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

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        // Substituído "nome_do_modulo" pelo correto do módulo: "equipamentos"
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("equipamentos"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão para executar esta ação.\"}");
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Aqui você pode carregar dados iniciais se necessário antes de exibir a tela
        // Redireciona de forma segura para o JSP que está protegido no WEB-INF ou na pasta jsp
    	// Adicione esta validação no início para bloquear acessos negados via GET
        if (!validarPermissao(request, response)) {
            return;
        }
        request.getRequestDispatcher("/jsp/consulta-equipamento.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
        
     // Validação de segurança de acesso ao módulo
        if (!validarPermissao(request, response)) {
            return;
        }
    }
}
