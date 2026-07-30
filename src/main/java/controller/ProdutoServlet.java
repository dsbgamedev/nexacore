package controller;

import com.google.gson.Gson;
import dao.ProdutoDAO;
import dto.ConfiguracaoCampoDTO;
import model.TipoProduto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import dto.ProdutoDTO;

@WebServlet("/api/produtos/*")
public class ProdutoServlet extends HttpServlet {
    private ProdutoDAO dao = new ProdutoDAO();
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/produtos/listar-tipos
        if ("/listar-tipos".equals(path)) {
            List<TipoProduto> tipos = dao.listarTiposAtivos();
            response.getWriter().write(gson.toJson(tipos));
        } 
        // Rota: /api/produtos/buscar-campos?tipoId=X
        else if ("/buscar-campos".equals(path)) {
            String tipoIdStr = request.getParameter("tipoId");
            if (tipoIdStr != null && !tipoIdStr.isEmpty()) {
                int tipoId = Integer.parseInt(tipoIdStr);
                List<ConfiguracaoCampoDTO> campos = dao.buscarCamposPorTipo(tipoId);
                response.getWriter().write(gson.toJson(campos));
            }
        }
        // Rota: /api/produtos/buscar?id=X
        else if ("/buscar".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    ProdutoDTO produto = dao.buscarPorId(id);
                    if (produto != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(gson.toJson(produto));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"erro\": \"Produto não encontrado.\"}");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"erro\": \"Erro ao buscar o produto.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"ID não informado.\"}");
            }
        }
        // Rota: /api/produtos/listar-marcas
        else if ("/listar-marcas".equals(path)) {
            try {
                response.getWriter().write(gson.toJson(dao.listarMarcas()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Rota: /api/produtos/listar-fabricantes
        else if ("/listar-fabricantes".equals(path)) {
            try {
                response.getWriter().write(gson.toJson(dao.listarFabricantes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
     // Rota: /api/produtos/consultar
        else if ("/consultar".equals(path)) {
            String sku = request.getParameter("sku");
            String marcaIdStr = request.getParameter("marcaId");
            String tipoIdStr = request.getParameter("tipoId");
            String modelo = request.getParameter("modelo");
            String ativoStr = request.getParameter("ativo");
            String busca = request.getParameter("busca"); // Captura a busca global

            int marcaId = 0;
            if (marcaIdStr != null && !marcaIdStr.trim().isEmpty() && !marcaIdStr.equals("Selecione...") && !marcaIdStr.equalsIgnoreCase("TODOS")) {
                try {
                    marcaId = Integer.parseInt(marcaIdStr);
                } catch (NumberFormatException e) {
                    marcaId = 0;
                }
            }

            int tipoId = 0;
            if (tipoIdStr != null && !tipoIdStr.trim().isEmpty() && !tipoIdStr.equals("Selecione...") && !tipoIdStr.equalsIgnoreCase("TODOS")) {
                try {
                    tipoId = Integer.parseInt(tipoIdStr);
                } catch (NumberFormatException e) {
                    tipoId = 0;
                }
            }

            Boolean ativo = (ativoStr != null && !ativoStr.isEmpty() && !ativoStr.equals("Selecione...")) ? Boolean.parseBoolean(ativoStr) : null;

            // Passa todos os parâmetros incluindo a busca global para o DAO
            List<ProdutoDTO> resultados = dao.consultarProdutos(sku, marcaId, tipoId, modelo, ativo, busca);
            
            response.getWriter().write(gson.toJson(resultados));
        }
   }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/produtos/salvar
        if ("/salvar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro ao ler dados do produto.");
                return;
            }

            dto.ProdutoDTO produto = gson.fromJson(sb.toString(), dto.ProdutoDTO.class);
            System.out.println("JSON recebido para salvar: " + sb.toString());
            
            if (produto.getImagens() != null && !produto.getImagens().isEmpty()) {
                java.util.List<String> caminhosSalvos = new java.util.ArrayList<>();
                String uploadPath = "C:\\uploads_nexacore\\";
                java.io.File uploadDir = new java.io.File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                for (String base64 : produto.getImagens()) {
                    String data = base64.contains(",") ? base64.split(",")[1] : base64;
                    String fileName = "prod_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                    
                    java.io.File file = new java.io.File(uploadPath + java.io.File.separator + fileName);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(java.util.Base64.getDecoder().decode(data));
                        caminhosSalvos.add(fileName);
                    }
                }
                produto.setCaminhosImagens(caminhosSalvos);
            }
            
            try {
                dao.salvarProdutoCompleto(produto);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Produto salvo com sucesso!\", \"sku\": \"" + produto.getSku() + "\"}");
                
            } catch (java.sql.SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("já está cadastrado")) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                response.getWriter().write("{\"erro\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Ocorreu um erro inesperado ao salvar o produto.\"}");
            }
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/produtos/atualizar
        if ("/atualizar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro ao ler dados para atualização.");
                return;
            }

            dto.ProdutoDTO produto = gson.fromJson(sb.toString(), dto.ProdutoDTO.class);

            // PROCESSAMENTO DAS IMAGENS NO MODO EDIÇÃO (Igual ao doPost)
            if (produto.getImagens() != null && !produto.getImagens().isEmpty()) {
                java.util.List<String> caminhosSalvos = new java.util.ArrayList<>();
                String uploadPath = "C:\\uploads_nexacore\\";
                java.io.File uploadDir = new java.io.File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                for (String base64 : produto.getImagens()) {
                    // Se a imagem já for um nome de arquivo salvo anteriormente (string simples sem base64), mantém ela
                    if (!base64.startsWith("data:image") && !base64.contains(",")) {
                        caminhosSalvos.add(base64);
                        continue;
                    }

                    String data = base64.contains(",") ? base64.split(",")[1] : base64;
                    String fileName = "prod_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                    
                    java.io.File file = new java.io.File(uploadPath + java.io.File.separator + fileName);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(java.util.Base64.getDecoder().decode(data));
                        caminhosSalvos.add(fileName);
                    }
                }
                produto.setCaminhosImagens(caminhosSalvos);
            }

            try {
                dao.atualizarProdutoCompleto(produto);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Produto atualizado com sucesso!\"}");
                
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"erro\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"erro\": \"Erro inesperado ao atualizar o produto.\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Rota: /api/produtos/excluir?id=X
        if ("/excluir".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    dao.excluirProduto(id);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"mensagem\": \"Produto excluído com sucesso!\"}");
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"erro\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"erro\": \"Erro inesperado ao excluir o produto.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"ID do produto não informado para exclusão.\"}");
            }
        }
    }
}