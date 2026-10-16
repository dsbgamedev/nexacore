package controller;

import dao.FornecedorDAO;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

@WebServlet("/ConsultaFornecedorServlet")
public class ConsultaFornecedorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final FornecedorDAO dao = new FornecedorDAO();

    private boolean validarPermissaoLeitura(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet?message=session_expired");
            return false;
        }

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
        
        boolean temLeitura = isAdmin ||
            usuario.temPermissao("fornecedores", "CONSULTAR") || 
            usuario.temPermissao("fornecedores", "EDITAR") ||
            usuario.temPermissao("fornecedores", "EXCLUIR");

        if (!temLeitura) {
            response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao&modulo=fornecedores");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        if (!validarPermissaoLeitura(req, resp)) {
            return;
        }

        try {
            String busca = req.getParameter("busca");
            String documento = req.getParameter("documento");
            String status = req.getParameter("status");
            Integer filial = null;

            try {
                String filialParam = req.getParameter("filial");
                if (filialParam != null && !filialParam.isBlank()) {
                    filial = Integer.valueOf(filialParam);
                }
            } catch (NumberFormatException ignored) {}

            req.setAttribute("fornecedores", dao.listar(busca, documento, status, filial));
            req.setAttribute("filiais", dao.listarFiliais());
            req.setAttribute("filtroBusca", busca);
            req.setAttribute("filtroDocumento", documento);
            req.setAttribute("filtroStatus", status);
            req.setAttribute("filtroFilial", filial);

            req.getRequestDispatcher("/WEB-INF/jsp/consulta-fornecedores.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("erro", "Não foi possível carregar a listagem de fornecedores.");
            req.setAttribute("fornecedores", Collections.emptyList());
            try {
                req.setAttribute("filiais", dao.listarFiliais());
            } catch (Exception ignored) {
                req.setAttribute("filiais", Collections.emptyList());
            }
            req.getRequestDispatcher("/WEB-INF/jsp/consulta-fornecedores.jsp").forward(req, resp);
        }
    }
}