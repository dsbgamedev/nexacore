package controller;

import com.google.gson.Gson;
import dao.FabricanteDAO;
import model.Fabricante;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/fabricantes/*")
public class FabricanteServlet extends HttpServlet {
    private FabricanteDAO dao = new FabricanteDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/listar".equals(path) || path == null) {
            try {
                List<Fabricante> lista = dao.listarTodos();
                response.getWriter().write(gson.toJson(lista));
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro ao listar fabricantes.\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/salvar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro ao ler dados.");
                return;
            }

            Fabricante fab = gson.fromJson(sb.toString(), Fabricante.class);

            try {
                dao.salvar(fab);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Fabricante salvo com sucesso!\"}");
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                dao.excluir(id);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Fabricante excluído com sucesso!\"}");
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String msg = "Erro ao excluir o registro.";
                // Verifica se é violação de chave estrangeira do Postgres
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                    msg = "Não é possível excluir este fabricante pois ele está vinculado a uma ou mais marcas cadastradas.";
                }
                // Retorna um JSON limpo e seguro
                String jsonErro = "{\"erro\": \"" + msg.replace("\"", "\\\"") + "\"}";
                response.getWriter().write(jsonErro);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String jsonErro = "{\"erro\": \"Erro inesperado ao excluir o registro.\"}";
                response.getWriter().write(jsonErro);
            }
        }
    }
}
