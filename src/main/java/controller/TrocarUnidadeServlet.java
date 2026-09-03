package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dto.UnidadeDTO;
import model.Usuario;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TrocarUnidadeServlet", urlPatterns = {"/TrocarUnidadeServlet"})
public class TrocarUnidadeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String unidadeIdStr = request.getParameter("id");
            if (unidadeIdStr == null || unidadeIdStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"sucesso\": false, \"erro\": \"ID da unidade não fornecido.\"}");
                return;
            }

            int unidadeId = Integer.parseInt(unidadeIdStr);
            HttpSession session = request.getSession(false);
            Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"sucesso\": false, \"erro\": \"Sessão expirada.\"}");
                return;
            }

            // Atualiza a unidade ativa no objeto do usuário logado
            usuario.setUnidadeAtivaId(unidadeId);

            // Varre a lista de UnidadeDTO permitidas para atualizar também o nome da unidade ativa
            if (usuario.getUnidadesPermitidasObjetos() != null) {
                for (UnidadeDTO u : usuario.getUnidadesPermitidasObjetos()) {
                    if (u.getId() == unidadeId) {
                        usuario.setUnidadeAtivaNome(u.getNome());
                        break;
                    }
                }
            }

            // Salva o usuário atualizado de volta na sessão
            session.setAttribute("usuarioLogado", usuario);

            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"sucesso\": true}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"sucesso\": false, \"erro\": \"Erro técnico ao trocar unidade: " + e.getMessage() + "\"}");
        }
    }
}