package controller;

import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/gerar-etiqueta") // URL amigável e profissional
public class ImprimirEtiquetaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * MÉTODO GET: Quando o usuário clica no menu lateral.
     * Ele não imprime nada, apenas "chama" o formulário dentro do menu.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redireciona para o menu, injetando a view correta
        RequestDispatcher dispatcher = request.getRequestDispatcher("/menu.jsp?view=configurarEtiqueta");
        dispatcher.forward(request, response);
    }

    /**
     * MÉTODO POST: Quando o usuário clica no botão verde "GERAR E IMPRIMIR".
     * Aqui acontece o processamento dos dados.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        // Captura de dados (idêntica ao seu código)
        String notaFiscal = request.getParameter("notaFiscal");
        String conteudo = request.getParameter("conteudo");
        String largura = request.getParameter("largura");
        String altura = request.getParameter("altura");
        String unidade = request.getParameter("unidadeMedida");
        String layout = request.getParameter("layout");

        // Validações
        if (notaFiscal == null || notaFiscal.trim().isEmpty()) notaFiscal = "0";
        if (unidade == null || unidade.trim().isEmpty()) unidade = "mm";
        if (layout == null || layout.trim().isEmpty()) layout = "padrao";
        if (largura == null || largura.trim().isEmpty()) largura = "75"; // Valor padrão seguro
        if (altura == null || altura.trim().isEmpty()) altura = "50";   // Valor padrão seguro

        // Atributos para o JSP de Impressão
        request.setAttribute("notaFiscal", notaFiscal);
        request.setAttribute("conteudo", conteudo);
        request.setAttribute("largura", largura);
        request.setAttribute("altura", altura);
        request.setAttribute("unidade", unidade);
        request.setAttribute("layout", layout);

        // Envia para a página de visualização (View de saída)
        RequestDispatcher dispatcher = request.getRequestDispatcher("/gerarEtiquetaVisual.jsp");
        dispatcher.forward(request, response);
    }
}