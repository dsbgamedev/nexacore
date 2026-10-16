package controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * =====================================================================
 * API SERVLET: FilialApiServlet
 * =====================================================================
 * @description Fornece a lista de filiais/unidades permitidas para o usuário 
 *              logado no formato JSON.
 * @endpoint /api/filiais
 * @usage Consumido via JavaScript por múltiplos módulos do sistema que 
 *        precisam popular selects ou tabelas de filiais (ex: Transportadoras, 
 *        Manutenção, etc.).
 * @author Douglas / nexacore
 * =====================================================================
 */
@WebServlet(name = "FilialApiServlet", urlPatterns = {"/api/filiais"})
public class FilialApiServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Usuário não autenticado.\"}");
            return;
        }

        // Validação básica de leitura (ou ajuste conforme sua regra de negócio)
        boolean isAdmin = ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temLeitura = isAdmin || usuario.temPermissao("transportadoras", "CONSULTAR") || usuario.temPermissao("manutencao", "CONSULTAR");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (usuario.getUnidadesPermitidasObjetos() != null) {
                out.print(gson.toJson(usuario.getUnidadesPermitidasObjetos()));
            } else {
                out.print("[]");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
        }
    }
}
