package controller;

import com.google.gson.Gson;
import dao.MovimentacaoRecebimentoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
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
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {
    "/api/envios/transito", 
    "/api/envios/detalhes", 
    "/api/envios/receber",
    "/api/devolucoes/transito",
    "/api/devolucoes/detalhes",
    "/api/devolucoes/receber"
})
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
	    maxFileSize = 1024 * 1024 * 50,       // 50MB (para iti video wenno dakes a ladawan)
	    maxRequestSize = 1024 * 1024 * 100    // 100MB
	)
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
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        Integer unidadeAtivaId = usuario != null ? usuario.getUnidadeAtivaId() : null;

        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if ("/api/envios/transito".equals(path)) {
            List<Map<String, Object>> lista = dao.listarEnviosEmTransito();
            
            // Se não for admin, filtra rigorosamente para exibir apenas itens onde o destino bate com a filial ativa
            if (!isAdmin && unidadeAtivaId != null) {
                lista = lista.stream().filter(envio -> {
                    Object destIdObj = envio.get("destinoId");
                    if (destIdObj != null) {
                        int destId = Integer.parseInt(destIdObj.toString());
                        return destId == unidadeAtivaId;
                    }
                    return false;
                }).collect(Collectors.toList());
            } else if (!isAdmin) {
                lista.clear(); // Se não tem filial ativa definida, não traz nada
            }
            
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
            
            // Se não for admin, filtra rigorosamente para exibir apenas devoluções destinadas à filial ativa
            if (!isAdmin && unidadeAtivaId != null) {
                lista = lista.stream().filter(dev -> {
                    Object destIdObj = dev.get("destinoId");
                    if (destIdObj != null) {
                        int destId = Integer.parseInt(destIdObj.toString());
                        return destId == unidadeAtivaId;
                    }
                    return false;
                }).collect(Collectors.toList());
            } else if (!isAdmin) {
                lista.clear();
            }
            
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
                HttpSession session = request.getSession(false);
                Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
                
                String idEnvioStr = request.getParameter("idMovimentacao"); 
                if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                    idEnvioStr = request.getParameter("idEnvio");
                }

                if (idEnvioStr == null || idEnvioStr.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"sucesso\": false, \"mensagem\": \"ID da movimentação não informado.\"}");
                    return;
                }

                int idMovimentacao = Integer.parseInt(idEnvioStr);

                // =========================================================================
                // VALIDAÇÃO RIGOROSA DE FILIAL: Apenas a filial de destino (ou Admin) pode confirmar
                // =========================================================================
                int destinoIdMovimentacao = dao.buscarDestinoIdPorMovimentacao(idMovimentacao);
                boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));

                if (!isAdmin) {
                    Integer unidadeAtivaId = usuario != null ? usuario.getUnidadeAtivaId() : null;
                    if (unidadeAtivaId == null || unidadeAtivaId != destinoIdMovimentacao) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.write("{\"sucesso\": false, \"mensagem\": \"Acesso negado: Sua filial ativa não possui permissão para confirmar operações destinadas a esta unidade.\"}");
                        return;
                    }
                }
                // =========================================================================

                String tipoOperacao = request.getParameter("tipoOperacao");
                boolean ehDevolucao = "/api/devolucoes/receber".equals(path);
                if (ehDevolucao) {
                    tipoOperacao = "devolucao";
                }

                String dataRecebimento = request.getParameter("dataRecebimento");
                String responsavel = request.getParameter("responsavel");
                String condicaoGeral = request.getParameter("condicaoGeral");
                
                // =========================================================================
                // TRATAMENTO DO UPLOAD DE COMPROVANTE (Alinhado com a pasta do ImagemServlet)
                // =========================================================================
                String caminhoArquivoSalvo = null;
                try {
                    jakarta.servlet.http.Part filePart = request.getPart("comprovanteRecebimento");
                    if (filePart != null && filePart.getSize() > 0) {
                        String fileName = java.nio.file.Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                        
                        // Caminho unificado com o do ImagemServlet       
                     // Diretório base unificado com o ImagemServlet
                        String uploadPath = "C:\\uploads_nexacore\\recebimentos"; 
                        
                        java.io.File uploadDir = new java.io.File(uploadPath);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        
                        String filePath = uploadPath + java.io.File.separator + uniqueFileName;
                        filePart.write(filePath);
                        
                        // O caminho relativo que será salvo no banco (ex: recebimentos/1711123_foto.jpg)
                        caminhoArquivoSalvo = "recebimentos/" + uniqueFileName;
                    }
                } catch (Exception e) {
                    System.err.println("Aviso: Nenhum arquivo enviado ou erro ao processar o anexo: " + e.getMessage());
                }
                // =========================================================================

                boolean sucesso = false;
               // Passando o caminhoArquivoSalvo para os métodos DAO (certifique-se de que os métodos no DAO aceitam essa String)
                if ("devolucao".equals(tipoOperacao)) {
                    sucesso = dao.registrarRecebimentoDevolucao(idMovimentacao, dataRecebimento, responsavel, condicaoGeral, caminhoArquivoSalvo);
                } else {
                    sucesso = dao.registrarRecebimento(idMovimentacao, dataRecebimento, responsavel, condicaoGeral, caminhoArquivoSalvo);
                }

                if (sucesso) {
                    // --- REGISTRO DE AUDITORIA ---
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

                    out.write("{\"sucesso\": true, \"mensagem\": \"Recebimento confirmado e estoque atualizado com sucesso!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"sucesso\": false, \"mensagem\": \"Erro ao processar o recebimento no banco de dados.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"sucesso\": false, \"mensagem\": \"Erro técnico: " + e.getMessage() + "\"}");
            }
        }
    }
}