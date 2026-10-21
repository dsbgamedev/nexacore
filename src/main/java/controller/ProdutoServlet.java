package controller;

import com.google.gson.Gson;
import dao.ProdutoDAO;
import dto.ConfiguracaoCampoDTO;
import model.TipoProduto;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import dto.ProdutoDTO;

@WebServlet(urlPatterns = {"/ProdutoServlet", "/api/produtos/*"})
public class ProdutoServlet extends HttpServlet {
    private ProdutoDAO dao = new ProdutoDAO();
    private Gson gson = new Gson();
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoDesejada) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Acesso negado. Usuário não autenticado.\"}");
            return false;
        }

        boolean temPermissao = usuario.temPermissao("produtos", acaoDesejada) || usuario.temPermissao("produto", acaoDesejada);

        if (!temPermissao) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Acesso negado. Você não possui permissão para " + acaoDesejada.toLowerCase() + " produtos.\"}");
            return false;
        }
        return true;
    }

    /**
     * Método auxiliar para determinar dinamicamente a pasta de upload com base no tipo do produto.
     */
    private String obterDiretorioUpload(ProdutoDTO produto) {
        String pastaTipo = "Geral";
        
        if (produto.getNomeTipo() != null && !produto.getNomeTipo().trim().isEmpty()) {
            pastaTipo = produto.getNomeTipo().trim();
        } else if (produto.getTipoId() > 0) {
            String nomeTipoDoBanco = dao.buscarNomeTipoPorId(produto.getTipoId());
            if (nomeTipoDoBanco != null && !nomeTipoDoBanco.trim().isEmpty()) {
                pastaTipo = nomeTipoDoBanco.trim();
            }
        }
        
        // Remove caracteres especiais ou acentos inadequados para nomes de pastas Windows
        pastaTipo = pastaTipo.replaceAll("[^a-zA-Z0-9-_áàâãéèêíóôõúçÁÀÂÃÉÈÊÍÓÔÕÚÇ ]", "").trim();
        if (pastaTipo.isEmpty()) {
            pastaTipo = "Geral";
        }

        return "C:\\uploads_nexacore\\" + pastaTipo + "\\";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	String path = request.getPathInfo();
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));

        if (path == null || path.equals("/")) {
            boolean temAcessoEscrita = usuario != null && (
                isAdmin ||
                usuario.temPermissao("produtos", "INSERIR") ||
                usuario.temPermissao("produtos", "EDITAR") ||
                usuario.temPermissao("produto", "INSERIR") ||
                usuario.temPermissao("produto", "EDITAR")
            );

            if (!temAcessoEscrita) {
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao");
                return;
            }

            boolean podeEditar = isAdmin || usuario.temPermissao("produtos", "EDITAR") || usuario.temPermissao("produto", "EDITAR");
            boolean podeExcluir = isAdmin || usuario.temPermissao("produtos", "EXCLUIR") || usuario.temPermissao("produto", "EXCLUIR");

            request.setAttribute("podeEditar", podeEditar);
            request.setAttribute("podeExcluir", podeExcluir);

            request.getRequestDispatcher("/WEB-INF/jsp/cadastro-produto.jsp").forward(request, response);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("/listar-tipos".equals(path)) {
            List<TipoProduto> tipos = dao.listarTiposAtivos();
            response.getWriter().write(gson.toJson(tipos));
        } 
        else if ("/buscar-campos".equals(path)) {
            String tipoIdStr = request.getParameter("tipoId");
            if (tipoIdStr != null && !tipoIdStr.isEmpty()) {
                int tipoId = Integer.parseInt(tipoIdStr);
                List<ConfiguracaoCampoDTO> campos = dao.buscarCamposPorTipo(tipoId);
                response.getWriter().write(gson.toJson(campos));
            }
        }
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
        else if ("/listar-marcas".equals(path)) {
            try {
                response.getWriter().write(gson.toJson(dao.listarMarcas()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("/listar-fabricantes".equals(path)) {
            try {
                response.getWriter().write(gson.toJson(dao.listarFabricantes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("/consultar".equals(path)) {
            String sku = request.getParameter("sku");
            String marcaIdStr = request.getParameter("marcaId");
            String tipoIdStr = request.getParameter("tipoId");
            String modelo = request.getParameter("modelo");
            String ativoStr = request.getParameter("ativo");
            String busca = request.getParameter("busca");

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

            List<ProdutoDTO> resultados = dao.consultarProdutos(sku, marcaId, tipoId, modelo, ativo, busca);
            response.getWriter().write(gson.toJson(resultados));
        }
   }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	String acao = request.getParameter("action");
        String acaoPermissao = ("editar".equalsIgnoreCase(acao) || (request.getParameter("id") != null && !request.getParameter("id").isEmpty())) ? "EDITAR" : "INSERIR";

        if (!validarPermissao(request, response, acaoPermissao)) {
            return;
        }
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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

            ProdutoDTO produto = gson.fromJson(sb.toString(), ProdutoDTO.class);
            System.out.println("JSON recebido para salvar: " + sb.toString());
            
            if (produto.getImagens() != null && !produto.getImagens().isEmpty()) {
                List<String> caminhosSalvos = new java.util.ArrayList<>();
                
                // Define o diretório dinâmico com base no tipo do produto
                String uploadPath = obterDiretorioUpload(produto);
                java.io.File uploadDir = new java.io.File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs(); // Cria a pasta do tipo (ex: Computador, Impressora) se não existir
                }

                for (String base64 : produto.getImagens()) {
                    String data = base64.contains(",") ? base64.split(",")[1] : base64;
                    String fileName = "prod_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                    
                    java.io.File file = new java.io.File(uploadPath + java.io.File.separator + fileName);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(java.util.Base64.getDecoder().decode(data));
                        // Salva o caminho relativo incluindo o nome da pasta (ex: Computador/prod_123.jpg)
                        caminhosSalvos.add(uploadDir.getName() + "/" + fileName);
                    }
                }
                produto.setCaminhosImagens(caminhosSalvos);
            }
            
            try {
                dao.salvarProdutoCompleto(produto);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"Produto salvo com sucesso!\", \"sku\": \"" + produto.getSku() + "\"}");
                
            } catch (SQLException e) {
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
    	if (!validarPermissao(request, response, "EDITAR")) {
            return;
        }
    	
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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

            ProdutoDTO produto = gson.fromJson(sb.toString(), ProdutoDTO.class);

            if (produto.getImagens() != null && !produto.getImagens().isEmpty()) {
                List<String> caminhosSalvos = new java.util.ArrayList<>();
                
                // Define o diretório dinâmico com base no tipo do produto
                String uploadPath = obterDiretorioUpload(produto);
                java.io.File uploadDir = new java.io.File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                for (String base64 : produto.getImagens()) {
                    // Se a imagem já for um caminho/nome salvo anteriormente (sem base64), mantém ela
                    if (!base64.startsWith("data:image") && !base64.contains(",")) {
                        caminhosSalvos.add(base64);
                        continue;
                    }

                    String data = base64.contains(",") ? base64.split(",")[1] : base64;
                    String fileName = "prod_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                    
                    java.io.File file = new java.io.File(uploadPath + java.io.File.separator + fileName);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(java.util.Base64.getDecoder().decode(data));
                        caminhosSalvos.add(uploadDir.getName() + "/" + fileName);
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
    	if (!validarPermissao(request, response, "EXCLUIR")) {
            return;
        }
    	
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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