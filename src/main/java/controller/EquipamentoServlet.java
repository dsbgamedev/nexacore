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

    private static final long serialVersionUID = 1L;
    
    private final EquipamentoDAO dao = new EquipamentoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = request.getParameter("acao");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
     // 0. Trata a listagem de situações permitidas para edição direta
        String acaoSituacoes = request.getParameter("acaoSituacoes");
        if ("edicao-direta".equals(acaoSituacoes)) {
            try {
                List<Map<String, Object>> listaSituacoes = dao.listarSituacoesEdicaoDireta();
                out.print(gson.toJson(listaSituacoes));
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"erro\": \"Erro ao listar situações: " + e.getMessage() + "\"}");
            }
            return;
        }       
        // 1. Trata a requisição do ID automático para cadastro
        if ("proximo-id".equals(acao)) {
            try {
                String novoId = dao.gerarProximoIdSistema();
                out.print("{\"proximoId\": \"" + novoId + "\"}");
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"erro\": \"Erro ao gerar próximo ID: " + e.getMessage() + "\"}");
            }
            return;
        }

        // 1.1. Trata a busca de um equipamento específico por ID para edição
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int idEq = Integer.parseInt(idParam);
                Equipamento eq = dao.buscarPorId(idEq);
                if (eq != null) {
                    out.print(gson.toJson(eq));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"erro\": \"Equipamento não encontrado\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"erro\": \"" + e.getMessage() + "\"}");
            }
            return;
        }

        // 2. Trata a listagem geral ou com filtros para a tela de consulta
        try {
            String pesquisaGlobal = request.getParameter("pesquisaGlobal"); 
            String produto = request.getParameter("produto");
            String idSistema = request.getParameter("idSistema");
            String patrimonio = request.getParameter("patrimonio");
            String serial = request.getParameter("serial");
            String origem = request.getParameter("origem");
            String departamento = request.getParameter("departamento");
            String usuario = request.getParameter("usuario");
            String status = request.getParameter("status"); // Passa o ID do status selecionado no filtro
            String situacao = request.getParameter("situacao"); // <-- Adicionado parâmetro de situação

            List<Equipamento> lista;

            if ((pesquisaGlobal != null && !pesquisaGlobal.trim().isEmpty()) ||
            	    (produto != null && !produto.trim().isEmpty()) ||
            	    (idSistema != null && !idSistema.trim().isEmpty()) ||
            	    (patrimonio != null && !patrimonio.trim().isEmpty()) ||
            	    (serial != null && !serial.trim().isEmpty()) ||
            	    (origem != null && !origem.trim().isEmpty()) ||
            	    (departamento != null && !departamento.trim().isEmpty()) ||
            	    (usuario != null && !usuario.trim().isEmpty()) ||
            	    (status != null && !status.trim().isEmpty()) ||
            	    (situacao != null && !situacao.trim().isEmpty())) { 
            		
            	    // CHAMADA CORRIGIDA (seguindo a ordem exata do DAO):
            	    // Ordem: pesquisaGlobal, idSistema, patrimonio, serial, origem, departamento, statusIdFiltro, situacaoIdFiltro, produto, usuario
            	    lista = dao.listarComFiltros(pesquisaGlobal, idSistema, patrimonio, serial, origem, departamento, status, situacao, produto, usuario);
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

            boolean sucesso;
            String mensagem;

            // Se o objeto possui ID maior que 0, significa edição; caso contrário, novo cadastro
            if (eq.getIdEquipamento() > 0) {
                sucesso = dao.atualizar(eq);
                mensagem = "Equipamento atualizado com sucesso!";
            } else {
                sucesso = dao.inserir(eq);
                mensagem = "Equipamento cadastrado com sucesso!";
            }

            Map<String, Object> resp = new HashMap<>();
            if (sucesso) {
                resp.put("sucesso", true);
                resp.put("mensagem", mensagem);
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
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                
                // Validação opcional: impede inativar se estiver em trânsito
                Equipamento eq = dao.buscarPorId(id);
                if (eq != null && eq.getSituacaoId() != null && eq.getSituacaoId() == 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"sucesso\": false, \"mensagem\": \"Operação negada! Equipamentos em trânsito não podem ser inativados.\"}");
                    return;
                }

                // Executa a inativação no DAO
                dao.excluirEquipamento(id); // (Ou renomeie o método do DAO para inativarEquipamento)
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"sucesso\": true, \"mensagem\": \"Equipamento inativado com sucesso!\"}");
                
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String mensagemErro = e.getMessage() != null && !e.getMessage().isEmpty() 
                    ? e.getMessage() 
                    : "Não foi possível inativar o equipamento.";
                
                e.printStackTrace();
                out.write("{\"sucesso\": false, \"mensagem\": \"" + mensagemErro + "\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"sucesso\": false, \"mensagem\": \"ID do equipamento é obrigatório.\"}");
        }
    }
}