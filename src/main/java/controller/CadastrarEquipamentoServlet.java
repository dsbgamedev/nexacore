package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

import java.io.IOException;

@WebServlet("/CadastrarEquipamentoServlet")
public class CadastrarEquipamentoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Validação granular integrada com o método inteligente do objeto Usuario.
     * Verifica se o usuário possui a permissão específica (CONSULTAR, INSERIR, EDITAR) no módulo "equipamentos".
     */
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoEspecifica) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Sessão expirada ou acesso negado.\"}");
            return false;
        }

        // Utiliza o método inteligente do objeto Usuario para validar a permissão granular
        boolean temPermissao = usuario.temPermissao("equipamentos", acaoEspecifica);

        if (!temPermissao) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão de " + acaoEspecifica + " neste módulo.\"}");
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        boolean podeEscrever = usuario != null && (
            isAdmin ||
            usuario.temPermissao("equipamentos", "INSERIR") || 
            usuario.temPermissao("equipamentos", "EDITAR")
        );

        if (!podeEscrever) {
            // Redireciona de volta para o menu informando o bloqueio
            response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/jsp/cadastro-equipamento.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Identifica se é uma edição (ex: verificando se veio ID ou parâmetro de ação) ou um novo cadastro
        String acao = request.getParameter("action");
        String acaoPermissao = ("editar".equalsIgnoreCase(acao) || (request.getParameter("id") != null && !request.getParameter("id").isEmpty())) ? "EDITAR" : "INSERIR";

        // Validação de segurança executada ANTES de qualquer processamento
        if (!validarPermissao(request, response, acaoPermissao)) {
            return;
        }
        
        // Aqui entra a sua lógica de salvamento/atualização do equipamento...
    }

    // Auxiliar para checar permissão no doGet sem duplicar código de resposta JSON
    private boolean usuarioTemPermissaoSimples(HttpServletRequest request, String acao) {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        return usuario != null && usuario.temPermissao("equipamentos", acao);
    }
}