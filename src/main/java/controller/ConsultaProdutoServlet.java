package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

@WebServlet("/ConsultaProdutoServlet")
public class ConsultaProdutoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        boolean podeVer = usuario != null && (
            isAdmin ||
            usuario.temPermissao("produtos", "CONSULTAR") || 
            usuario.temPermissao("produtos", "EDITAR") ||
            usuario.temPermissao("produtos", "EXCLUIR") ||
            usuario.temPermissao("produto", "CONSULTAR")
        );

        if (!podeVer) {
            response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao");
            return;
        }

        // DISPONIBILIZA AS PERMISSÕES PARA A JSP
        boolean podeEditar = isAdmin || usuario.temPermissao("produtos", "EDITAR");
        boolean podeExcluir = isAdmin || usuario.temPermissao("produtos", "EXCLUIR");

        request.setAttribute("podeEditar", podeEditar);
        request.setAttribute("podeExcluir", podeExcluir);

        request.getRequestDispatcher("/WEB-INF/jsp/consulta-produto.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}