package controller;

import dao.TransportadoraDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Transportadora;
import model.Usuario;
import service.TransportadoraService;

import java.io.IOException;
import java.util.List;
import java.util.Map; // <-- NOVO IMPORT NECESSÁRIO

@WebServlet("/TransportadoraServlet")
public class TransportadoraServlet extends HttpServlet {

    /**
     * Validação granular integrada com o método inteligente do objeto Usuario.
     * Verifica se o utilizador possui a permissão específica (CONSULTAR, INSERIR, EDITAR) no módulo "transportadoras".
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
        boolean temPermissao = usuario.temPermissao("transportadoras", acaoEspecifica);

        if (!temPermissao) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Não possui permissão de " + acaoEspecifica + " neste módulo.\"}");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Bloqueia qualquer tentativa de passar parâmetros explícitos na URL (ex: ?acao=novo ou ?acao=listar)
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso direto por parâmetros na URL não é permitido.");
            return;
        }

        // Valida se o utilizador tem permissão para consultar/aceder ao módulo
        if (!validarPermissao(request, response, "CONSULTAR")) {
            response.sendRedirect(request.getContextPath() + "/menu.jsp?erro=sem_permissao");
            return;
        }

        // Como o acesso foi feito de forma limpa, executa o fluxo padrão
        listar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String acao = request.getParameter("acao");

        if ("salvar".equalsIgnoreCase(acao)) {
            // Valida permissão de inserção antes de processar o salvamento
            if (!validarPermissao(request, response, "INSERIR")) {
                return; // A resposta de erro JSON já foi tratada dentro do validarPermissao
            }
            salvar(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação inválida.");
        }
    }

    private void salvar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        try {
            // Delega todo o trabalho de mapeamento, conversão e chamada do DAO para uma camada de Serviço
            TransportadoraService service = new TransportadoraService();
            boolean salvo = service.salvarDaRequisicao(request);

            if (salvo) {
                response.getWriter().write("{\"sucesso\": true, \"mensagem\": \"Transportadora cadastrada com sucesso!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Não foi possível salvar a transportadora.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            TransportadoraDAO dao = new TransportadoraDAO();
            
            // 1. Carrega as transportadoras (caso precise listar na mesma página)
            List<Transportadora> lista = dao.listar();
            request.setAttribute("transportadoras", lista);
            
            // 2. NOVO: Carrega os Tipos de Endereço da base de dados
            List<Map<String, Object>> listaTipos = dao.listarTiposEndereco();
            request.setAttribute("listaTiposEndereco", listaTipos);
            
            // Encaminha para a JSP correspondente
            request.getRequestDispatcher("/WEB-INF/jsp/cadastro-transportadora.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao carregar dados do ecrã.");
        }
    }
}