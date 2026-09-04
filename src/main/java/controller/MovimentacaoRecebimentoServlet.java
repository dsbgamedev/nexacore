package controller;

import com.google.gson.Gson;

import dao.MovimentacaoRecebimentoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

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
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("movimentacao_recebimento"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão para o módulo de recebimento de movimentações.\"}");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if (!validarPermissao(request, response)) {
            return;
        }
    	
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
    	if (!validarPermissao(request, response)) {
            return;
        }
    	
    	String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if ("/api/envios/receber".equals(path) || "/api/devolucoes/receber".equals(path)) {
            try {
                String tipoOperacao = request.getParameter("tipoOperacao");
                boolean ehDevolucao = "/api/devolucoes/receber".equals(path);
                if (ehDevolucao) {
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
                    // --- REGISTRO DE AUDITORIA (RECEBIMENTO DE ENVIO OU DEVOLUÇÃO) ---
                    HttpSession session = request.getSession(false);
                    Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
                    String ipCliente = request.getHeader("X-Forwarded-For");
                    if (ipCliente == null || ipCliente.isEmpty()) {
                        ipCliente = request.getRemoteAddr();
                    }

                    if (usuario != null) {
                        String moduloNome = ehDevolucao ? "Recebimento de Devoluções" : "Recebimento de Envios";
                        String descricao = ehDevolucao ? "Confirmação de recebimento de devolução de equipamentos" : "Confirmação de recebimento de envio de equipamentos";
                        
                        Map<String, Object> dadosNovos = new java.util.HashMap<>();
                        dadosNovos.put("dataRecebimento", dataRecebimento);
                        dadosNovos.put("responsavelRecebimento", responsavel);
                        dadosNovos.put("condicaoGeral", condicaoGeral);
                        dadosNovos.put("status", "Concluído / Recebido");

                        util.AuditoriaService.registrar(
                            Long.valueOf(usuario.getId()),
                            usuario.getUsername(),
                            moduloNome,
                            "EDITAR",
                            "movimentacao_envio",
                            (long) idMovimentacao,
                            descricao,
                            "{\"status\": \"Em Trânsito / Pendente\"}",
                            gson.toJson(dadosNovos),
                            ipCliente
                        );
                    }
                    // -----------------------------------------------------------------

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