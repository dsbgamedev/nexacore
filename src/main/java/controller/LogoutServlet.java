package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/LogoutServlet") // Mapeia este Servlet para a URL /LogoutServlet
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Obtém a sessão existente, não cria uma nova
        if (session != null) {
            session.invalidate(); // Invalida (encerra) a sessão
            System.out.println("Usuário deslogado. Sessão invalidada.");
        }
        // Redireciona para a página de login após o logout
        response.sendRedirect("LoginServlet?message=logged_out");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // CORREÇÃO: Chamar doGet com ambos os parâmetros
        doGet(request, response); 
    }
   
}

