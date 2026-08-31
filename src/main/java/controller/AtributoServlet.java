package controller;

import com.google.gson.Gson;
import dao.AtributoDAO;
import model.Atributo;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AtributoServlet
 * Controladora principal do módulo de atributos. 
 * Gerencia as requisições HTTP, realiza o parsing dos dados e coordena 
 * as chamadas para a camada de persistência (DAO).
 */
@WebServlet(value = {"/AtributosServlet", "/api/atributos/*"})
public class AtributoServlet extends HttpServlet {
    private AtributoDAO dao = new AtributoDAO();
    
    /**
     * Gerencia requisições de consulta (GET).
     * Mapeia caminhos como: /todos, /listar-tipos, /listar-grupos.
     */
    
   // Método de validação igual ao que você usou no CadastrarUsuarioServlet
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("atributos"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão para o módulo de atributos.\"}");
            return false;
        }
        return true;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
       String servletPath = request.getServletPath();
        
        // Se acessar a rota principal do Servlet, encaminha para a página JSP protegida
        if ("/AtributosServlet".equals(servletPath)) {
        	// Valida permissão antes de encaminhar para a JSP do módulo
            if (!validarPermissao(request, response)) {
                return;
            }
            request.getRequestDispatcher("/WEB-INF/jsp/gerenciar-atributos.jsp").forward(request, response);
            return;
        }
        
        String path = request.getPathInfo();
        
        
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // Roteamento baseado no PATH
        if (path == null || path.equals("/")) {
        	// Lógica para buscar atributos de um tipo específico
            String tipoIdStr = request.getParameter("tipoId");
            if (tipoIdStr != null && !tipoIdStr.isEmpty()) {
                response.getWriter().write(new Gson().toJson(dao.buscarAtributosPorTipo(Integer.parseInt(tipoIdStr))));
            }
        } else if (path.equals("/todos")) {
            response.getWriter().write(new Gson().toJson(dao.listarTodos())); 
        } else if (path.equals("/listar-tipos")) {
            response.getWriter().write(new Gson().toJson(dao.listarTodosOsTipos()));
        } else if (path.equals("/listar-grupos")) {
        	// ... lógica de listagem de grupos com tratamento de erro
            try {
                var grupos = dao.listarGrupos();
                String json = new Gson().toJson(grupos);
                System.out.println("JSON gerado para grupos: " + json); // Veja no console do Eclipse/Tomcat
                response.getWriter().write(json);
            } catch (Exception e) {
                e.printStackTrace(); // O erro real aparecerá no console do servidor
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro no DAO: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gerencia operações de escrita/edição (POST).
     * Inclui: vincular, salvar, atualizar ordem, editar e exclusão de massa.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	// Valida permissão antes de encaminhar para a JSP do módulo
        if (!validarPermissao(request, response)) {
            return;
        }
    	// Implementação robusta que utiliza Gson para parsear JSON e aciona o DAO	
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
       // 1. VINCULAR: Cria uma nova associação entre Tipo de Produto e Atributo
        if ("/vincular".equals(path)) {
            String tipoIdParam = request.getParameter("tipoId");
            String atributoIdParam = request.getParameter("atributoId");
            String grupoIdStr = request.getParameter("grupoId"); 
            String tipoDado = request.getParameter("tipoDado");
            String tamanhoStr = request.getParameter("tamanho");
            // Removido: String ordemStr = request.getParameter("ordem");
            String obrigatorioStr = request.getParameter("obrigatorio");

            if (tipoIdParam != null && !tipoIdParam.isEmpty() && atributoIdParam != null && !atributoIdParam.isEmpty()) {
                try {
                    int tipoId = Integer.parseInt(tipoIdParam);
                    int atributoId = Integer.parseInt(atributoIdParam);
                    
                    // Tratamento para valores nulos ou "undefined" vindos do JS
                    int grupoId = 0;
                    if (grupoIdStr != null && !grupoIdStr.isEmpty() && !"null".equalsIgnoreCase(grupoIdStr) && !"undefined".equalsIgnoreCase(grupoIdStr)) {
                        grupoId = Integer.parseInt(grupoIdStr);
                    }
                    
                    int tamanho = (tamanhoStr != null && !tamanhoStr.isEmpty()) ? Integer.parseInt(tamanhoStr) : 255;
                    boolean obrigatorio = "true".equals(obrigatorioStr);
                    
                    // Verifica duplicidade antes de inserir
                    if (dao.existeVinculo(tipoId, atributoId)) {
                        response.sendError(HttpServletResponse.SC_CONFLICT, "Este atributo já está vinculado!");
                    } else {
                        // Chamada simplificada: O DAO calculará a ordem sozinho
                        dao.vincularAtributoAoTipo(tipoId, atributoId, grupoId, tipoDado, tamanho, obrigatorio);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro de formato nos parâmetros numéricos.");
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gravar no banco: " + e.getMessage());
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "IDs obrigatórios para vínculo.");
            }
            
         // 2. SALVAR TIPO: Cria um novo Tipo de Produto  
        } else if ("/salvar-tipo".equals(path)) {
            String nome = request.getParameter("nome");
            if (nome != null && !nome.trim().isEmpty()) {
                try {
                    dao.salvarTipo(nome);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (Exception e) {
                    // Verifica se a mensagem contém o erro de duplicidade do PostgreSQL
                    if (e.getMessage() != null && e.getMessage().contains("tipos_produto_nome_key")) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // Código 409: Conflito
                        response.getWriter().write("Já existe um tipo de produto com o nome '" + nome + "'.");
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao salvar: " + e.getMessage());
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome do tipo é obrigatório.");
            }
            
         // 3. SALVAR ATRIBUTO: Cria um novo Atributo global  
        } else if ("/salvar".equals(path)) {
            String nome = request.getParameter("nome");
            if (nome != null && !nome.trim().isEmpty()) {
                try {
                    // Normaliza o nome para remover acentos e deixar minúsculo (Ex: "Márca" -> "marca")
                    String nomeNormalizado = java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                        .trim()
                        .toLowerCase();

                    // Trava de segurança para impedir palavras reservadas nativas do produto
                    if (nomeNormalizado.equals("marca") || nomeNormalizado.equals("marcas")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("O atributo 'Marca' é nativo do sistema e não pode ser criado.");
                        return;
                    }

                    dao.salvarAtributo(nome);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao salvar: " + e.getMessage());
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome do atributo é obrigatório.");
            }
            
         // 4. ATUALIZAR ORDEM: Processa JSON enviado pelo SortableJS para reordenar atributos  
        } else if ("/atualizar-ordem".equals(path)) {
            try {
                String json = request.getReader().lines().reduce("", (acc, cur) -> acc + cur);
                com.google.gson.JsonArray array = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
                
                for (int i = 0; i < array.size(); i++) {
                    com.google.gson.JsonObject obj = array.get(i).getAsJsonObject();
                    
                    // Validação de segurança para garantir que os campos necessários existem. Verificação de segurança robusta
                    if (obj.has("id") && !obj.get("id").isJsonNull() && 
                        obj.has("ordem") && !obj.get("ordem").isJsonNull()) {
                        
                        int id = obj.get("id").getAsInt();
                        int ordem = obj.get("ordem").getAsInt();
                        
                        dao.atualizarOrdemAtributo(id, ordem);
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace(); // Isso ajudará a ver o erro real no console do Eclipse
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao processar ordem: " + e.getMessage());
            }
            
         // 5. EDITAR: Atualiza as propriedades de um vínculo existente  
        } else if ("/editar".equals(path)) {
            try {
                String json = request.getReader().lines().reduce("", (acc, cur) -> acc + cur);
                System.out.println("DEBUG JSON RECEBIDO: " + json); // Adicione isso
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                
                int id = (obj.has("id") && !obj.get("id").isJsonNull()) ? obj.get("id").getAsInt() : 0;
             // --- CHAME AQUI PARA DIAGNOSTICAR ---
                dao.listarTodosOsDadosDoBanco();
                // CAPTURA O ATRIBUTO ID DO NOVO SELECT AQUI:
                int atributoId = (obj.has("atributoId") && !obj.get("atributoId").isJsonNull()) ? obj.get("atributoId").getAsInt() : 0; 
                int grupoId = (obj.has("grupoId") && !obj.get("grupoId").isJsonNull()) ? obj.get("grupoId").getAsInt() : 0;
                String tipoDado = obj.has("tipoDado") ? obj.get("tipoDado").getAsString() : "TEXT";
                int tamanho = (obj.has("tamanho") && !obj.get("tamanho").isJsonNull()) ? obj.get("tamanho").getAsInt() : 255;
                boolean obrigatorio = obj.has("obrigatorio") ? obj.get("obrigatorio").getAsBoolean() : false;
                
                // Passa o atributoId como segundo parâmetro para o DAO
               
                dao.atualizarAtributo(id, atributoId, grupoId, tipoDado, tamanho, obrigatorio); 
                
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace(); 
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro: " + e.getMessage());
            }
         
         // 6. EXCLUIR EM MASSA: Verifica uso antes de deletar múltiplos registros
        }else if ("/excluir-massa".equals(path)) {
            try {
                // Lê o JSON enviado pelo JS
                String json = request.getReader().lines().reduce("", (acc, cur) -> acc + cur);
                com.google.gson.JsonArray array = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
                
               // Validação de integridade: Checa se algum atributo está vinculado antes da exclusão Processa cada ID
                for (int i = 0; i < array.size(); i++) {
                    int atributoId = array.get(i).getAsInt();
                    
                    // Validação: chama o método que criamos no DAO
                    // CÓDIGO MELHORADO:
                    if (dao.atributoEstaEmUso(atributoId)) {
                        // Buscamos o nome do atributo para a mensagem ficar amigável
                        String nome = dao.buscarNomeAtributo(atributoId);
                        
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getWriter().write("O atributo '" + nome + "' não pode ser excluído pois possui vínculos.");
                        return;
                    }
                }
                
                // Exclusão segura após validação. Se chegou aqui, nenhum está em uso, pode excluir tudo.
                for (int i = 0; i < array.size(); i++) {
                    dao.excluirAtributo(array.get(i).getAsInt());
                }
                
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao excluir: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gerencia operações de remoção (DELETE).
     * Protege contra exclusões indevidas via códigos de status HTTP (409 Conflict).
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	// Lógica de exclusão com validação isValido() e tratamento de FK (foreign keys)
        String path = request.getPathInfo();
        response.setContentType("application/json");

        try {
        	// Rota para remover o vínculo entre um atributo e um tipo de produto
            if ("/excluir-vinculo".equals(path)) {
                // Aqui você recebe o parâmetro 'id' enviado pelo seu atributo.js
                String idVinculoStr = request.getParameter("id");

                if (isValido(idVinculoStr)) {
                    // Chama o DAO usando apenas o ID único da linha
                    dao.excluirVinculo(Integer.parseInt(idVinculoStr));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de vínculo inválido.");
                }
             // Rota para remover um tipo de produto inteiro do sistema 
            } else if ("/excluir-tipo".equals(path)) {
                String tipoIdStr = request.getParameter("tipoId");
                
                if (isValido(tipoIdStr)) {
                    try {
                        dao.excluirTipoProduto(Integer.parseInt(tipoIdStr));
                        response.setStatus(HttpServletResponse.SC_OK);
                    } catch (Exception e) {
                        // Verifica se a causa é violação de chave estrangeira (PostgreSQL erro 23503)
                    	// Isso ocorre se o tipo ainda tiver atributos ligados a ele
                        String msgErro = e.getMessage();
                        if (msgErro != null && msgErro.contains("23503")) {
                            response.sendError(HttpServletResponse.SC_CONFLICT, "Não é possível excluir: este tipo possui atributos vinculados.");
                        } else {
                        	// Erro genérico de banco de dados
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao excluir: " + msgErro);
                        }
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de tipo inválido.");
                }
            }
        
        } catch (NumberFormatException e) {
        	// Captura erros quando o ID não for um número válido
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Erro: Parâmetros numéricos mal formatados.");
        } catch (Exception e) {
        	// Captura qualquer outra falha inesperada no processamento da requisição
            e.printStackTrace();
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
        }
    }
    // Método auxiliar de validação para limpar o código
    // Método auxiliar de validação
    private boolean isValido(String valor) {
        return valor != null && !valor.isEmpty() && !"undefined".equals(valor) && !"null".equals(valor);
    }
}