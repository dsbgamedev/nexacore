package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

import java.io.IOException;

@WebServlet(name = "CadastrarEquipamentoServlet", urlPatterns = {"/CadastrarEquipamentoServlet"})
public class CadastrarEquipamentoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Método auxiliar para validar permissões granulares do usuário no módulo
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoEspecifica) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Sessão expirada ou acesso negado.\"}");
            return false;
        }

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

        // Verifica se o usuário tem privilégio de escrita (Admin ou permissões de Inserir/Editar)
        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        
        boolean podeEscrever = usuario != null && (
            isAdmin ||
            usuario.temPermissao("equipamentos", "INSERIR") || 
            usuario.temPermissao("equipamentos", "EDITAR")
        );

        if (!podeEscrever) {
            response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao");
            return;
        }

        String idStr = request.getParameter("id");
        String autorizado = request.getParameter("autorizado");
        String tokenOk = request.getParameter("token_ok");
        HttpSession sessaoAtual = request.getSession();

        // =========================================================================
        // PASSO 1: NOVO CADASTRO (Sem ID na URL)
        // Se a página for aberta para criar um novo registro, limpamos o ID da sessão
        // =========================================================================
        if (idStr == null || idStr.isEmpty()) {
            sessaoAtual.removeAttribute("id_equipamento_clicado");
            request.getRequestDispatcher("/WEB-INF/jsp/cadastro-equipamento.jsp").forward(request, response);
            return;
        }

        long id = Long.parseLong(idStr);

        // =========================================================================
        // PASSO 2: CLIQUE LEGÍTIMO DA LISTAGEM (&autorizado=true)
        // Quando o usuário clica em "Editar" na tela de consulta, o link envia o token.
        // Gravamos esse ID na sessão e redirecionamos adicionando '&token_ok=true'
        // =========================================================================
        if ("true".equals(autorizado)) {
            sessaoAtual.setAttribute("id_equipamento_clicado", id);
            response.sendRedirect(request.getContextPath() + "/CadastrarEquipamentoServlet?id=" + id + "&token_ok=true");
            return;
        }

        // =========================================================================
        // PASSO 3: CONSUMO DO TOKEN DE SEGURANÇA (Redirect interno limpo)
        // O servidor valida se o token veio correto, valida com a sessão e faz um
        // redirect final removendo os parâmetros e deixando a URL limpa (?id=X)
        // =========================================================================
        if ("true".equals(tokenOk)) {
            Long idPermitidoSession = (Long) sessaoAtual.getAttribute("id_equipamento_clicado");
            if (idPermitidoSession != null && idPermitidoSession.equals(id)) {
                response.sendRedirect(request.getContextPath() + "/CadastrarEquipamentoServlet?id=" + id);
                return;
            }
        }

        // =========================================================================
        // PASSO 4: A TRAVA ANTI-IDOR (Tentativa de alteração manual na URL)
        // Se a requisição chegou com um ID, mas SEM o fluxo autorizado e SEM o token,
        // significa que a pessoa digitou outro número diretamente na barra de endereços!
        // =========================================================================
        Long idPermitidoSession = (Long) sessaoAtual.getAttribute("id_equipamento_clicado");

        if (idPermitidoSession == null || !idPermitidoSession.equals(id)) {
            // Limpa o vestígio da sessão por segurança
            sessaoAtual.removeAttribute("id_equipamento_clicado");
            
            // Ativa a flag de erro IDOR que a JSP vai ler para disparar o ModalService.error
            request.setAttribute("erroIdor", true);
            request.getRequestDispatcher("/WEB-INF/jsp/cadastro-equipamento.jsp").forward(request, response);
            return;
        }

        // Se passou por todas as validações de segurança, renderiza a tela normalmente
        request.getRequestDispatcher("/WEB-INF/jsp/cadastro-equipamento.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = request.getParameter("action");
        String acaoPermissao = ("editar".equalsIgnoreCase(acao) || (request.getParameter("id") != null && !request.getParameter("id").isEmpty())) ? "EDITAR" : "INSERIR";

        if (!validarPermissao(request, response, acaoPermissao)) {
            return;
        }
    }
}