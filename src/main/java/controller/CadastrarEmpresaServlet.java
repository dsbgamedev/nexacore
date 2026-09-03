package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

@WebServlet("/CadastrarEmpresaServlet")
public class CadastrarEmpresaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        // Regra restrita: Apenas SUPER_ADMINISTRADOR pode acessar empresas
        boolean isSuperAdmin = usuario != null && usuario.getPerfil() != null && 
                               usuario.getPerfil().toUpperCase().contains("SUPER");

        if (!isSuperAdmin) {
            // Verifica se é uma requisição AJAX/API ou carregamento de página normal
            String acceptHeader = request.getHeader("Accept");
            String requestedWith = request.getHeader("X-Requested-With");
            
            if ((acceptHeader != null && acceptHeader.contains("application/json")) || 
                "XMLHttpRequest".equals(requestedWith)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado! Apenas Super Administradores podem acessar o módulo de empresas.\"}");
            } else {
                // Redireciona para o Menu com parâmetro de erro para disparar o ModalService na página principal
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao_empresa");
            }
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarPermissao(request, response)) {
            return;
        }
        request.getRequestDispatcher("/WEB-INF/jsp/cadastro-empresa.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // A validação deve vir sempre antes de qualquer processamento
        if (!validarPermissao(request, response)) {
            return;
        }
        
        // Insira aqui a sua lógica de cadastro/processamento do POST da empresa
    }
}