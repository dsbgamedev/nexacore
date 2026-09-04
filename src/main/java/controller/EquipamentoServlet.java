package controller;

import com.google.gson.Gson;
import dao.EquipamentoDAO;
import model.Equipamento;
import model.Usuario;
import model.enums.PerfilUsuario;
import conexao.Conexao; // Importa a sua classe de conexão

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
    
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("equipamentos"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão para o módulo de equipamentos.\"}");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	// Adicione esta validação no início para bloquear acessos negados via GET
        if (!validarPermissao(request, response)) {
            return;
        }
    	
    	String acao = request.getParameter("acao");
        HttpSession session = request.getSession(false);
        //boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        // O AuthFilter já garantiu que a sessão existe e que o usuário está logado, 
        // mas uma checagem rápida garante que o objeto Usuario está disponível para uso:
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão expirada.");
            return;
        }
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer unidadeAtivaId = usuarioLogado.getUnidadeAtivaId();
        // A partir daqui, você foca 100% na regra de negócio (chamar o DAO, montar JSON, etc.)  
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
     // 0.1. Trata a listagem de equipamentos disponíveis por origem para o modal de envio
        String acaoDisponiveis = request.getParameter("acao"); // ou use outro parâmetro se preferir, ex: "disponiveis-origem"
        if ("disponiveis-origem".equals(request.getParameter("acao"))) {
            try {
                String origemParam = request.getParameter("origemCodigo");
                if (origemParam != null && !origemParam.trim().isEmpty()) {
                    long origemCodigo = Long.parseLong(origemParam);
                    List<Equipamento> listaDisponiveis = dao.listarDisponiveisPorOrigem(origemCodigo);
                    out.print(gson.toJson(listaDisponiveis));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"erro\": \"Código de origem não informado.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"erro\": \"Erro ao listar equipamentos disponíveis: " + e.getMessage() + "\"}");
            }
            return;
        }
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
                    // Converte o objeto Equipamento para um Map ou JsonObject para injetar a flag de bloqueio
                    String jsonEquipamento = gson.toJson(eq);
                    com.google.gson.JsonObject jsonObj = gson.fromJson(jsonEquipamento, com.google.gson.JsonObject.class);
                    
                    // Regra de Negócio: Se o equipamento estiver Em Trânsito (Situação 3) ou Em Devolução (Situação 8), bloqueia a origem
                    boolean bloquear = (eq.getSituacaoId() != null && (eq.getSituacaoId() == 3 || eq.getSituacaoId() == 8));
                    jsonObj.addProperty("bloquearOrigem", bloquear);
                    jsonObj.addProperty("origemBloqueada", bloquear); // Garante compatibilidade com as variações do JS
                    jsonObj.addProperty("permiteDisponivel", !bloquear); // Se estiver bloqueado, não permite "Disponível" (ID 1)
                    
                    out.print(gson.toJson(jsonObj));
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
            // Função auxiliar para limpar parâmetros vazios ou "todos"
            java.util.function.Function<String, String> limparParametro = (val) -> {
                if (val == null || val.trim().isEmpty() || "todos".equalsIgnoreCase(val.trim())) {
                    return null;
                }
                return val.trim();
            };

            String pesquisaGlobal = limparParametro.apply(request.getParameter("pesquisaGlobal"));
            String produto = limparParametro.apply(request.getParameter("produto"));
            String idSistema = limparParametro.apply(request.getParameter("idSistema"));
            String patrimonio = limparParametro.apply(request.getParameter("patrimonio"));
            String serial = limparParametro.apply(request.getParameter("serial"));
            String origem = limparParametro.apply(request.getParameter("origem"));
            String departamento = limparParametro.apply(request.getParameter("departamento"));
            String usuario = limparParametro.apply(request.getParameter("usuario"));
            String status = limparParametro.apply(request.getParameter("status"));
            String situacao = limparParametro.apply(request.getParameter("situacao"));

            List<Equipamento> lista;

            // Se algum dos parâmetros principais estiver preenchido (diferente de null), usa o filtro
            if (pesquisaGlobal != null || produto != null || idSistema != null || 
                patrimonio != null || serial != null || origem != null || 
                departamento != null || usuario != null || status != null || situacao != null) {
                
                lista = dao.listarComFiltros(pesquisaGlobal, idSistema, patrimonio, serial, origem, departamento, status, situacao, produto, usuario);
            } else {
                // Se tudo estiver limpo ou marcado como "Todos", traz todos os registros
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
    	// Validação de segurança de acesso ao módulo
        if (!validarPermissao(request, response)) {
            return;
        }

    	response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Recupera o usuário logado para uso na auditoria
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        // Captura o IP de origem do cliente
        String ipCliente = request.getHeader("X-Forwarded-For");
        if (ipCliente == null || ipCliente.isEmpty()) {
            ipCliente = request.getRemoteAddr();
        }
     
        String pathInfo = request.getPathInfo(); 

        try {
        	// TRATAMENTO DA ROTA DE DEVOLUÇÃO
        	if ("/devolver".equals(pathInfo)) {
        	    BufferedReader reader = request.getReader();
        	    Map<String, Object> payload = gson.fromJson(reader, Map.class);
        	    
        	    Double idDouble = (Double) payload.get("idEquipamento");
        	    if (idDouble == null) {
        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        out.print("{\"sucesso\": false, \"mensagem\": \"ID do equipamento não informado.\"}");
        	        return;
        	    }
        	    int idEquipamento = idDouble.intValue();

        	    Equipamento eq = dao.buscarPorId(idEquipamento);
        	    if (eq == null) {
        	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	        out.print("{\"sucesso\": false, \"mensagem\": \"Equipamento não encontrado.\"}");
        	        return;
        	    }

        	    // 1. VALIDAÇÃO DE SEGURANÇA: Se o equipamento está Disponível (ID 1), impede a devolução
        	    if (eq.getSituacaoId() != null && eq.getSituacaoId() == 1) { 
        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        out.print("{\"sucesso\": false, \"mensagem\": \"Este equipamento está no estoque local e nunca foi enviado para outra filial. A devolução não é permitida.\"}");
        	        return;
        	    }

        	    // 2. Validação: Se já estiver Em Trânsito (ID 3) ou Em Devolução (ID 8), impede nova ação
        	    if (eq.getSituacaoId() != null && (eq.getSituacaoId() == 3 || eq.getSituacaoId() == 8)) {
        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        out.print("{\"sucesso\": false, \"mensagem\": \"Este equipamento já está em trânsito ou em processo de devolução!\"}");
        	        return;
        	    }

        	    try {
        	        dao.MovimentacaoEnvioDAO envioDao = new dao.MovimentacaoEnvioDAO();
        	        model.MovimentacaoEnvio devolucao = new model.MovimentacaoEnvio();
        	        
        	        devolucao.setDataEnvio(java.time.LocalDate.now());
        	        
        	        Integer origemCodigo = eq.getOrigemCodigo();
        	        if (origemCodigo == null || origemCodigo <= 0) {
        	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	            out.print("{\"sucesso\": false, \"mensagem\": \"Filial de origem inválida para realizar a devolução.\"}");
        	            return;
        	        }
        	        
        	        Long idFilialReal = null;
        	        String sqlBuscaFilial = "SELECT id_filial FROM filiais WHERE origem_codigo = ?";
        	        
        	        try (java.sql.Connection conn = Conexao.conectar();
        	             java.sql.PreparedStatement stmt = conn.prepareStatement(sqlBuscaFilial)) {
        	            stmt.setInt(1, origemCodigo);
        	            try (java.sql.ResultSet rs = stmt.executeQuery()) {
        	                if (rs.next()) {
        	                    idFilialReal = rs.getLong("id_filial");
        	                }
        	            }
        	        } catch (Exception ex) {
        	            ex.printStackTrace();
        	        }

        	        if (idFilialReal == null || idFilialReal <= 0) {
        	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	            out.print("{\"sucesso\": false, \"mensagem\": \"Não foi encontrada nenhuma filial cadastrada com o código de origem: " + origemCodigo + "\"}");
        	            return;
        	        }
        	        
        	        devolucao.setOrigemId(idFilialReal);
        	        devolucao.setDestinoId(idFilialReal);
        	        
        	        devolucao.setResponsavel("Sistema (Devolução)");
        	        devolucao.setTransportadora("Interna / Própria");
        	        devolucao.setCodigoRastreio("DEV-" + eq.getIdSistema());
        	        
        	        // 3. Define o status inicial da movimentação como 1 (Aguardando Envio)
        	        devolucao.setStatusId(1L); 
        	        
        	        devolucao.setNumeroNota("DEV-AUTO");
        	        devolucao.setObservacoes("Devolução iniciada automaticamente pelo sistema.");

        	        java.util.List<Long> idsEquipamentos = new java.util.ArrayList<>();
        	        idsEquipamentos.add((long) idEquipamento);

        	        // Insere a movimentação de envio/devolução
        	        envioDao.inserir(devolucao, idsEquipamentos);

        	        // 4. Guarda o estado anterior para a auditoria
        	        String dadosAnterioresJson = gson.toJson(eq);

        	        // 5. Atualiza a situação do equipamento na tabela equipamentos para 8 (Em Devolução)
        	        String sqlAtualizaSituacao = "UPDATE equipamentos SET situacao_id = 8 WHERE id_equipamento = ?";
        	        try (java.sql.Connection conn = Conexao.conectar();
        	             java.sql.PreparedStatement stmt = conn.prepareStatement(sqlAtualizaSituacao)) {
        	            stmt.setInt(1, idEquipamento);
        	            stmt.executeUpdate();
        	        }

        	        // REGISTRO DE AUDITORIA DE DEVOLUÇÃO
        	        if (usuario != null) {
        	            util.AuditoriaService.registrar(
        	                Long.valueOf(usuario.getId()),
        	                usuario.getUsername(),
        	                "Equipamentos",
        	                "EDITAR",
        	                "equipamentos",
        	                (long) idEquipamento,
        	                "Iniciação automática de devolução do equipamento",
        	                dadosAnterioresJson,
        	                "{\"situacaoId\": 8, \"status\": \"Em Devolução\"}",
        	                ipCliente
        	            );
        	        }

        	        Map<String, Object> resp = new HashMap<>();
        	        resp.put("sucesso", true);
        	        resp.put("mensagem", "Devolução iniciada com sucesso! Aguardando envio.");
        	        response.setStatus(HttpServletResponse.SC_OK);
        	        out.print(gson.toJson(resp));

        	    } catch (Exception e) {
        	        e.printStackTrace();
        	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	        String erroMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Erro desconhecido";
        	        out.print("{\"sucesso\": false, \"mensagem\": \"Erro ao registrar devolução: " + erroMsg + "\"}");
        	    }
        	    return;
        	}

            // FLUXO NORMAL DE CADASTRO / EDIÇÃO
            BufferedReader reader = request.getReader();
            Equipamento eq = gson.fromJson(reader, Equipamento.class);

            boolean sucesso;
            String mensagem;

            if (eq.getIdEquipamento() > 0) {
                // Busca o original antes de atualizar para guardar na auditoria
                Equipamento equipamentoOriginal = dao.buscarPorId(eq.getIdEquipamento());

                sucesso = dao.atualizar(eq);
                mensagem = "Equipamento atualizado com sucesso!";

                if (sucesso && usuario != null) {
                    // RECARREGA O EQUIPAMENTO DO BANCO PARA TRAZER TODOS OS CAMPOS ENRIQUECIDOS/TEXTUAIS (MARCA, MODELO, TIPO, SITUAÇÃO, ETC.)
                    Equipamento equipamentoAtualizado = dao.buscarPorId(eq.getIdEquipamento());

                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Equipamentos",
                        "EDITAR",
                        "equipamentos",
                        (long) eq.getIdEquipamento(),
                        "Atualização de dados do equipamento",
                        gson.toJson(equipamentoOriginal),
                        gson.toJson(equipamentoAtualizado), // Usa o objeto completo vindo do banco com todas as descrições
                        ipCliente
                    );
                }
            } else {
                sucesso = dao.inserir(eq);
                mensagem = "Equipamento cadastrado com sucesso!";
                long idGerado = eq.getIdEquipamento(); 

                if (sucesso && usuario != null) {
                    // RECARREGA TAMBÉM NO CADASTRO PARA GARANTIR OS TEXTOS DO NOVO REGISTRO
                    Equipamento equipamentoCadastrado = idGerado > 0 ? dao.buscarPorId((int) idGerado) : eq;

                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Equipamentos",
                        "CRIAR",
                        "equipamentos",
                        idGerado,
                        "Cadastro de novo equipamento",
                        "{}",
                        gson.toJson(equipamentoCadastrado),
                        ipCliente
                    );
                }
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
            resp.put("erro", "Erro técnico: " + e.getMessage());
            out.print(gson.toJson(resp));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Validação de segurança de acesso ao módulo (mantida apenas uma vez)
        if (!validarPermissao(request, response)) {
            return;
        }

        String idStr = request.getParameter("id");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                
                // 1. Busca o equipamento antes de inativar para guardar no histórico anterior
                Equipamento eq = dao.buscarPorId(id);
                if (eq != null && eq.getSituacaoId() != null && eq.getSituacaoId() == 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"sucesso\": false, \"mensagem\": \"Operação negada! Equipamentos em trânsito não podem ser inativados.\"}");
                    return;
                }

                // 2. Executa a inativação no banco
                dao.excluirEquipamento(id); 

                // 3. Captura o usuário logado e o IP para a auditoria
                HttpSession session = request.getSession(false);
                Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

                String ipCliente = request.getHeader("X-Forwarded-For");
                if (ipCliente == null || ipCliente.isEmpty()) {
                    ipCliente = request.getRemoteAddr();
                }

                // 4. Registra a auditoria da exclusão/inativação
                if (usuario != null && eq != null) {
                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Equipamentos",
                        "EXCLUIR",
                        "equipamentos",
                        (long) id,
                        "Inativação / Exclusão de equipamento",
                        gson.toJson(eq),
                        "{\"status\": \"Inativo\"}",
                        ipCliente
                    );
                }
                
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