package controller;

import com.google.gson.Gson; // ou a biblioteca JSON que você já usa no projeto

import dao.DepartamentoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Departamento;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/departamentos")
public class DepartamentoServlet extends HttpServlet {

    private DepartamentoDAO departamentoDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        departamentoDAO = new DepartamentoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Departamento> lista = departamentoDAO.listarTodos();
            
            Gson gson = new Gson();
            String json = gson.toJson(lista);

            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\": \"Erro ao buscar departamentos: " + e.getMessage() + "\"}");
        }
    }
}
