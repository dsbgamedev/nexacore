package controller;

import com.google.gson.Gson;
import dao.SituacaoEquipamentoDAO;
import model.SituacaoEquipamento;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
