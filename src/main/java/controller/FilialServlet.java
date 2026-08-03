package controller;

import com.google.gson.Gson;
import dao.FilialDAO;
import model.Filial;

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

@WebServlet(name = "FilialServlet", urlPatterns = {"/api/empresas/*"})
public class FilialServlet extends HttpServlet {

    private FilialDAO dao = new FilialDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Filial> lista = dao.listar();
            out.print(gson.toJson(lista));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao listar empresas/filiais: " + e.getMessage());
            out.print(gson.toJson(erro));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> resp = new HashMap<>();

        try {
            String acao = request.getParameter("acao");

            // 1. Ação de Excluir
            if ("excluir".equals(acao)) {
                String origemStr = request.getParameter("origemCodigo");
                if (origemStr != null && !origemStr.isEmpty()) {
                    int origemCodigo = Integer.parseInt(origemStr);
                    boolean excluido = dao.excluir(origemCodigo);

                    if (excluido) {
                        resp.put("sucesso", true);
                        resp.put("mensagem", "Empresa/Filial excluída com sucesso!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        resp.put("sucesso", false);
                        resp.put("erro", "Nenhum registro encontrado com a origem informada para exclusão.");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    resp.put("sucesso", false);
                    resp.put("erro", "Código de origem não fornecido para exclusão.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } 
            // 2. Ação de Atualizar / Editar
            else if ("atualizar".equals(acao)) {
                BufferedReader reader = request.getReader();
                Filial filial = gson.fromJson(reader, Filial.class);

                // Opcional: Validar se o sufixo pertence a outro registro (ignorando o próprio código)
                boolean sucesso = dao.atualizar(filial);

                if (sucesso) {
                    resp.put("sucesso", true);
                    resp.put("mensagem", "Empresa/Filial atualizada com sucesso!");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.put("sucesso", false);
                    resp.put("erro", "Não foi possível atualizar o registro no banco de dados.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } 
         // 3. Fluxo Padrão: Cadastrar Novo Registro (INSERT)
            else {
            BufferedReader reader = request.getReader();
            Filial filial = gson.fromJson(reader, Filial.class);

            // Validação de campos obrigatórios no servidor
            if (filial.getOrigemCodigo() <= 0 || filial.getSufixo() == null || filial.getSufixo().trim().isEmpty() ||
                filial.getNomeEmpresa() == null || filial.getNomeEmpresa().trim().isEmpty() ||
                filial.getCnpj() == null || filial.getCnpj().trim().isEmpty()) {
                
                resp.put("sucesso", false);
                resp.put("erro", "Preencha todos os campos obrigatórios, incluindo Origem e Sufixo.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                boolean origemExiste = dao.existePorOrigemCodigo(filial.getOrigemCodigo());
                boolean sufixoExiste = dao.existePorSufixo(filial.getSufixo());

                if (origemExiste) {
                    resp.put("sucesso", false);
                    resp.put("erro", "Já existe um cadastro com o código de origem (" + filial.getOrigemCodigo() + ") no banco de dados.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else if (sufixoExiste) {
                    resp.put("sucesso", false);
                    resp.put("erro", "Já existe um cadastro com o sufixo (" + filial.getSufixo() + ") no banco de dados.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    boolean sucesso = dao.inserir(filial);

                    if (sucesso) {
                        resp.put("sucesso", true);
                        resp.put("mensagem", "Empresa/Filial cadastrada com sucesso!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        resp.put("sucesso", false);
                        resp.put("erro", "Não foi possível salvar a empresa no banco de dados.");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
         }
            out.print(gson.toJson(resp));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.put("sucesso", false);
            resp.put("erro", "Erro técnico ao processar requisição: " + e.getMessage());
            out.print(gson.toJson(resp));
        }
    }
}