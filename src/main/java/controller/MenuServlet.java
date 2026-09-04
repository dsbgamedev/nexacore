package controller;

import java.io.IOException;
import java.util.List;

import dao.MovimentacaoEnvioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.MovimentacaoEnvio;
import model.Usuario;

@WebServlet("/MenuServlet")
public class MenuServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        
     // Busca as 5 movimentações recentes com status pendentes/em trânsito para o dashboard
        try {
            MovimentacaoEnvioDAO dao = new MovimentacaoEnvioDAO();
            List<MovimentacaoEnvio> listaRecentes = dao.listarRecentesPendentes(5);
            request.setAttribute("listaMovimentacoesRecentes", listaRecentes);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboard", "Não foi possível carregar as movimentações recentes.");
        }
        
        // Encaminha de forma segura para o menu.jsp protegendo o layout
        request.getRequestDispatcher("WEB-INF/jsp/menu.jsp").forward(request, response);
    }
        
        

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
