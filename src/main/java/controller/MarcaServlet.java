package controller;

import com.google.gson.Gson;
import dao.MarcaDAO;
import model.Marca;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import dao.MarcaDAO;
import model.Marca;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/marcas/*")
public class MarcaServlet extends HttpServlet {
    private MarcaDAO dao = new MarcaDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/marcas/listar
        if ("/listar".equals(path) || path == null) {
            try {
                List<Marca> marcas = dao.listarTodas();
                response.getWriter().write(gson.toJson(marcas));
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro ao listar marcas.\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/marcas/salvar
        if ("/salvar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro ao ler dados da marca.");
                return;
            }

            Marca marca = gson.fromJson(sb.toString(), Marca.class);

            try {
                dao.salvar(marca);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Marca salva com sucesso!\"}");
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
                response.getWriter().write("{\"mensagem\": \"Registro excluído com sucesso!\"}");
                
            } catch (Exception e) {
                e.printStackTrace();
                String mensagemErro = e.getMessage() != null ? e.getMessage() : "";
                
                // Trata a violação de chave estrangeira do PostgreSQL (23503)
                if (mensagemErro.contains("23503") || mensagemErro.contains("viola restrição de chave estrangeira")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Retorna HTTP 400
                    response.getWriter().write("{\"erro\": \"Não é possível excluir esta marca pois ela está vinculada a um ou mais produtos.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Retorna HTTP 500 limpo
                    String erroLimpo = mensagemErro.replace("\"", "\\\"").replaceAll("[\\r\\n]+", " ");
                    response.getWriter().write("{\"erro\": \"Erro ao excluir registro: " + erroLimpo + "\"}");
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID inválido ou não informado para exclusão.\"}");
        }
    }
}