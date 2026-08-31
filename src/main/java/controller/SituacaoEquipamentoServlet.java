package controller;

import com.google.gson.Gson;
import dao.SituacaoEquipamentoDAO;
import model.SituacaoEquipamento;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/situacao-equipamento")
public class SituacaoEquipamentoServlet extends HttpServlet {

    private SituacaoEquipamentoDAO situacaoDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        situacaoDAO = new SituacaoEquipamentoDAO();
    }

    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && (usuario.getModulosPermitidos().contains("situacao_equipamento") || usuario.getModulosPermitidos().contains("equipamentos")); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Acesso negado. Você não possui permissão para o módulo de situações de equipamentos.\"}");
            return false;
        }
        return true;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if (!validarPermissao(request, response)) {
            return;
        }
    	
    	response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<SituacaoEquipamento> lista = situacaoDAO.listarTodos();
            Gson gson = new Gson();
            String json = gson.toJson(lista);

            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\": \"Erro ao buscar situações: " + e.getMessage() + "\"}");
        }
    }
}
