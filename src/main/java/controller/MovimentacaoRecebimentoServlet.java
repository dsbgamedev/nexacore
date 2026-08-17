package controller;

import com.google.gson.Gson;

import dao.MovimentacaoRecebimentoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.List;

@WebServlet(urlPatterns = {
    "/api/envios/transito", 
    "/api/envios/detalhes", 
    "/api/envios/receber",
    "/api/devolucoes/transito",
    "/api/devolucoes/detalhes",
    "/api/devolucoes/receber" // <--- Adicionado aqui para mapear a URL de recebimento de devolução
})
public class MovimentacaoRecebimentoServlet extends HttpServlet {

    private final MovimentacaoRecebimentoDAO dao = new MovimentacaoRecebimentoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if ("/api/envios/transito".equals(path)) {
            List<Map<String, Object>> lista = dao.listarEnviosEmTransito();
            out.write(gson.toJson(lista));
            
        } else if ("/api/envios/detalhes".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                int idEnvio = Integer.parseInt(idStr);
                Map<String, Object> detalhes = dao.buscarDetalhesEnvio(idEnvio);
                out.write(gson.toJson(detalhes));
            } else {
                out.write("{}");
            }
            
        } else if ("/api/devolucoes/transito".equals(path)) {
            List<Map<String, Object>> lista = dao.listarDevolucoesEmTransito();
            out.write(gson.toJson(lista));
            
        } else if ("/api/devolucoes/detalhes".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                int idDevolucao = Integer.parseInt(idStr);
                Map<String, Object> detalhes = dao.buscarDetalhesDevolucao(idDevolucao);
                out.write(gson.toJson(detalhes));
            } else {
                out.write("{}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Trata tanto o endpoint de envios quanto o de devoluções no POST
        if ("/api/envios/receber".equals(path) || "/api/devolucoes/receber".equals(path)) {
            try {
                // Se a URL for explicitamente de devolução, força o tipoOperacao para 'devolucao'
                String tipoOperacao = request.getParameter("tipoOperacao");
                if ("/api/devolucoes/receber".equals(path)) {
                    tipoOperacao = "devolucao";
                }

                String idEnvioStr = request.getParameter("idMovimentacao"); 
                
                if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                    idEnvioStr = request.getParameter("idEnvio");
                }

                String dataRecebimento = request.getParameter("dataRecebimento");
                String responsavel = request.getParameter("responsavel");
                String condicaoGeral = request.getParameter("condicaoGeral");

                if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                    out.write("{\"sucesso\": false, \"mensagem\": \"ID da movimentação não informado.\"}");
                    return;
                }

                int idMovimentacao = Integer.parseInt(idEnvioStr);
                boolean sucesso = false;

                if ("devolucao".equals(tipoOperacao)) {
                    sucesso = dao.registrarRecebimentoDevolucao(idMovimentacao, dataRecebimento, responsavel, condicaoGeral);
                } else {
                    sucesso = dao.registrarRecebimento(idMovimentacao, dataRecebimento, responsavel, condicaoGeral);
                }

                if (sucesso) {
                    out.write("{\"sucesso\": true, \"mensagem\": \"Recebimento confirmado e estoque atualizado com sucesso!\"}");
                } else {
                    out.write("{\"sucesso\": false, \"mensagem\": \"Erro ao processar o recebimento no banco de dados.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.write("{\"sucesso\": false, \"mensagem\": \"Erro técnico: " + e.getMessage() + "\"}");
            }
        }
    }

}