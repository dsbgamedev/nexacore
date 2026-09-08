package controller;

import com.google.gson.Gson;
import dao.EquipamentoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet(name = "CampoEspecificacaoServlet", urlPatterns = {"/api/campos-especificacao"})
public class CampoEspecificacaoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final EquipamentoDAO dao = new EquipamentoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String produtoIdStr = request.getParameter("produtoId");
        String equipamentoIdStr = request.getParameter("equipamentoId");

        try {
            if (produtoIdStr != null && !produtoIdStr.trim().isEmpty()) {
                int produtoId = Integer.parseInt(produtoIdStr);
                List<Map<String, Object>> lista;

                if (equipamentoIdStr != null && !equipamentoIdStr.trim().isEmpty()) {
                    int equipamentoId = Integer.parseInt(equipamentoIdStr);
                    lista = dao.listarCamposComValoresPorEquipamento(produtoId, equipamentoId);
                } else {
                    lista = dao.listarCamposEspecificacaoPorProduto(produtoId);
                }

                out.print(gson.toJson(lista));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"erro\": \"ID do produto não informado.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }
}