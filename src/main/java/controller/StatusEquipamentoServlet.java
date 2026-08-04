package controller;

import com.google.gson.Gson;
import dao.StatusEquipamentoDAO;
import model.StatusEquipamento;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/status-equipamento")
public class StatusEquipamentoServlet extends HttpServlet {

    private StatusEquipamentoDAO statusDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        statusDAO = new StatusEquipamentoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<StatusEquipamento> lista = statusDAO.listarTodos();
            Gson gson = new Gson();
            String json = gson.toJson(lista);

            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\": \"Erro ao buscar status: " + e.getMessage() + "\"}");
        }
    }
}
