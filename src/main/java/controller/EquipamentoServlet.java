package controller;

import com.google.gson.Gson;
import dao.EquipamentoDAO;
import model.Equipamento;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "EquipamentoServlet", urlPatterns = {"/api/equipamentos/*"})
public class EquipamentoServlet extends HttpServlet {

    private EquipamentoDAO dao = new EquipamentoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = request.getParameter("acao");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Trata a requisição do ID automático
        if ("proximo-id".equals(acao)) {
            try {
                String novoId = dao.gerarProximoIdSistema();
                response.getWriter().print("{\"proximoId\": \"" + novoId + "\"}");
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"erro\": \"Erro ao gerar próximo ID: " + e.getMessage() + "\"}");
            }
            return;
        }

        // 2. Trata a listagem geral ou com filtros para a tela de consulta
        PrintWriter out = response.getWriter();
        try {
            String pesquisaGlobal = request.getParameter("pesquisaGlobal"); // <- Captura a busca global
            String produto = request.getParameter("produto");
            String idSistema = request.getParameter("idSistema");
            String patrimonio = request.getParameter("patrimonio");
            String serial = request.getParameter("serial");
            String origem = request.getParameter("origem");
            String departamento = request.getParameter("departamento");
            String usuario = request.getParameter("usuario");
            String status = request.getParameter("status");

            List<Equipamento> lista;

            // Se a pesquisa global ou qualquer outro filtro foi preenchido, usa o método com filtros
            if ((pesquisaGlobal != null && !pesquisaGlobal.trim().isEmpty()) ||
                (produto != null && !produto.trim().isEmpty()) ||
                (idSistema != null && !idSistema.trim().isEmpty()) ||
                (patrimonio != null && !patrimonio.trim().isEmpty()) ||
                (serial != null && !serial.trim().isEmpty()) ||
                (origem != null && !origem.trim().isEmpty()) ||
                (departamento != null && !departamento.trim().isEmpty()) ||
                (usuario != null && !usuario.trim().isEmpty()) ||
                (status != null && !status.trim().isEmpty())) {
                
                lista = dao.listarComFiltros(pesquisaGlobal, idSistema, patrimonio, serial, origem, departamento, status, produto, usuario);
            } else {
                lista = dao.listar();
            }

            out.print(gson.toJson(lista));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar equipamentos: " + e.getMessage());
            out.print(gson.toJson(erro));
        }
        
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            BufferedReader reader = request.getReader();
            Equipamento eq = gson.fromJson(reader, Equipamento.class);

            boolean sucesso = dao.inserir(eq);

            Map<String, Object> resp = new HashMap<>();
            if (sucesso) {
                resp.put("sucesso", true);
                resp.put("mensagem", "Equipamento cadastrado com sucesso!");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.put("sucesso", false);
                resp.put("erro", "Não foi possível salvar o equipamento.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            out.print(gson.toJson(resp));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> resp = new HashMap<>();
            resp.put("sucesso", false);
            resp.put("erro", "Erro técnico ao salvar: " + e.getMessage());
            out.print(gson.toJson(resp));
        }
    }
}