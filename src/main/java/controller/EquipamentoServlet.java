package controller;

import com.google.gson.Gson;
import dao.EquipamentoDAO;
import model.Equipamento;
import model.Usuario;
import conexao.Conexao;
import util.ValidadorContextoUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        
        boolean temPermissaoModulo = false;
        if (usuario != null && usuario.getModulosPermitidos() != null) {
            for (String mod : usuario.getModulosPermitidos()) {
                if ("equipamentos".equalsIgnoreCase(mod != null ? mod.trim() : "")) {
                    temPermissaoModulo = true;
                    break;
                }
            }
        } 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão para o módulo de equipamentos.\"}");
            return false;
        }
        return true;
    }

    private List<Integer> obterUnidadesPermitidas(Usuario usuario) {
        List<Integer> unidadesPermitidas = new ArrayList<>();
        if (usuario == null) return unidadesPermitidas;

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || 
                          "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());

        if (isAdmin) {
            String sqlTodasOrigens = "SELECT origem_codigo FROM filiais";
            try (Connection conn = Conexao.conectar();
                 PreparedStatement stmt = conn.prepareStatement(sqlTodasOrigens);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unidadesPermitidas.add(rs.getInt("origem_codigo"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (unidadesPermitidas.isEmpty() && usuario.getUnidadesPermitidas() != null) {
            for (String s : usuario.getUnidadesPermitidas()) {
                try {
                    int valorS = Integer.parseInt(s.trim());
                    
                    String sqlCheck = "SELECT origem_codigo FROM filiais WHERE origem_codigo = ?";
                    try (Connection conn = Conexao.conectar();
                         PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                        stmt.setInt(1, valorS);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                unidadesPermitidas.add(rs.getInt("origem_codigo"));
                            } else {
                                String sqlTraduz = "SELECT origem_codigo FROM filiais WHERE id_filial = ?";
                                try (PreparedStatement stmtTraduz = conn.prepareStatement(sqlTraduz)) {
                                    stmtTraduz.setInt(1, valorS);
                                    try (ResultSet rsTraduz = stmtTraduz.executeQuery()) {
                                        if (rsTraduz.next()) {
                                            unidadesPermitidas.add(rsTraduz.getInt("origem_codigo"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignora valores inválidos
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return unidadesPermitidas;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarPermissao(request, response)) {
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão expirada.");
            return;
        }
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        List<Integer> unidadesPermitidas = obterUnidadesPermitidas(usuarioLogado);
        String acao = request.getParameter("acao");

        if ("disponiveis-origem".equals(acao)) {
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

        String acaoOrigens = request.getParameter("acaoOrigens");
        if ("listar-origens".equals(acaoOrigens)) {
            try {
                List<Map<String, Object>> filiaisPermitidas = new ArrayList<>();
                String sqlFiliais;
                
                boolean isAdmin = usuarioLogado != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuarioLogado.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuarioLogado.getPerfil()));

                if (isAdmin) {
                    sqlFiliais = "SELECT origem_codigo, sufixo FROM filiais ORDER BY sufixo";
                } else if (unidadesPermitidas != null && !unidadesPermitidas.isEmpty()) {
                    StringBuilder sb = new StringBuilder("SELECT origem_codigo, sufixo FROM filiais WHERE origem_codigo IN (");
                    for (int i = 0; i < unidadesPermitidas.size(); i++) {
                        sb.append(i == 0 ? "?" : ", ?");
                    }
                    sb.append(") ORDER BY sufixo");
                    sqlFiliais = sb.toString();
                } else {
                    out.print(gson.toJson(new ArrayList<>()));
                    return;
                }

                try (Connection conn = Conexao.conectar();
                     PreparedStatement stmt = conn.prepareStatement(sqlFiliais)) {
                    
                    if (!isAdmin && unidadesPermitidas != null) {
                        for (int i = 0; i < unidadesPermitidas.size(); i++) {
                            stmt.setInt(i + 1, unidadesPermitidas.get(i));
                        }
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> filial = new HashMap<>();
                            filial.put("origemCodigo", rs.getInt("origem_codigo"));
                            filial.put("sufixo", rs.getString("sufixo"));
                            filiaisPermitidas.add(filial);
                        }
                    }
                }
                out.print(gson.toJson(filiaisPermitidas));
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"erro\": \"Erro ao listar origens: " + e.getMessage() + "\"}");
            }
            return;
        }

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

        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int idEq = Integer.parseInt(idParam);
                Equipamento eq = dao.buscarPorId(idEq, unidadesPermitidas);
                if (eq != null) {
                    String jsonEquipamento = gson.toJson(eq);
                    com.google.gson.JsonObject jsonObj = gson.fromJson(jsonEquipamento, com.google.gson.JsonObject.class);
                    
                    boolean bloquear = (eq.getSituacaoId() != null && (eq.getSituacaoId() == 3 || eq.getSituacaoId() == 8));
                    jsonObj.addProperty("bloquearOrigem", bloquear);
                    jsonObj.addProperty("origemBloqueada", bloquear);
                    jsonObj.addProperty("permiteDisponivel", !bloquear);
                    
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

        try {
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

            if (pesquisaGlobal != null || produto != null || idSistema != null || 
                patrimonio != null || serial != null || origem != null || 
                departamento != null || usuario != null || status != null || situacao != null) {
                
                lista = dao.listarComFiltros(pesquisaGlobal, idSistema, patrimonio, serial, origem, departamento, status, situacao, produto, usuario, unidadesPermitidas);
            } else {
                lista = dao.listarComFiltros(null, null, null, null, null, null, null, null, null, null, unidadesPermitidas);
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
        if (!validarPermissao(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        String ipCliente = request.getHeader("X-Forwarded-For");
        if (ipCliente == null || ipCliente.isEmpty()) {
            ipCliente = request.getRemoteAddr();
        }
     
        String pathInfo = request.getPathInfo(); 
        List<Integer> unidadesPermitidas = obterUnidadesPermitidas(usuario);

        try {
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

                Equipamento eq = dao.buscarPorId(idEquipamento, unidadesPermitidas);
                if (eq == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Equipamento não encontrado.\"}");
                    return;
                }

                // --- VALIDAÇÃO DE CONTEXTO DE FILIAL (DEVOLUÇÃO) ---
                ValidadorContextoUtil.validarFilialAtiva(request, eq.getOrigemCodigo());

                if (eq.getSituacaoId() != null && eq.getSituacaoId() == 1) { 
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"Este equipamento está no estoque local e nunca foi enviado para outra filial. A devolução não é permitida.\"}");
                    return;
                }

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
                    
                    try (Connection conn = Conexao.conectar();
                         PreparedStatement stmt = conn.prepareStatement(sqlBuscaFilial)) {
                        stmt.setInt(1, origemCodigo);
                        try (ResultSet rs = stmt.executeQuery()) {
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
                    devolucao.setStatusId(1L); 
                    devolucao.setNumeroNota("DEV-AUTO");
                    devolucao.setObservacoes("Devolução iniciada automaticamente pelo sistema.");

                    List<Long> idsEquipamentos = new ArrayList<>();
                    idsEquipamentos.add((long) idEquipamento);

                    envioDao.inserir(devolucao, idsEquipamentos);

                    String dadosAnterioresJson = gson.toJson(eq);

                    String sqlAtualizaSituacao = "UPDATE equipamentos SET situacao_id = 8 WHERE id_equipamento = ?";
                    try (Connection conn = Conexao.conectar();
                         PreparedStatement stmt = conn.prepareStatement(sqlAtualizaSituacao)) {
                        stmt.setInt(1, idEquipamento);
                        stmt.executeUpdate();
                    }

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

            BufferedReader reader = request.getReader();
            Equipamento eq = gson.fromJson(reader, Equipamento.class);

            // --- VALIDAÇÃO DE CONTEXTO DE FILIAL (CADASTRO / ATUALIZAÇÃO) ---
            if (eq != null && eq.getOrigemCodigo() > 0) {
                ValidadorContextoUtil.validarFilialAtiva(request, eq.getOrigemCodigo());
            }

            boolean sucesso = false;
            String mensagem = "";

            if (eq.getIdEquipamento() > 0) {
                Equipamento equipamentoOriginal = dao.buscarPorId(eq.getIdEquipamento(), unidadesPermitidas);

                sucesso = dao.atualizar(eq);
                mensagem = "Equipamento atualizado com sucesso!";

                if (sucesso && usuario != null) {
                    Equipamento equipamentoAtualizado = dao.buscarPorId(eq.getIdEquipamento(), unidadesPermitidas);

                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Equipamentos",
                        "EDITAR",
                        "equipamentos",
                        (long) eq.getIdEquipamento(),
                        "Atualização de dados do equipamento",
                        gson.toJson(equipamentoOriginal),
                        gson.toJson(equipamentoAtualizado),
                        ipCliente
                    );
                }
            } else {
                int idGerado = dao.inserir(eq);
                sucesso = (idGerado > 0);
                mensagem = "Equipamento cadastrado com sucesso!";

                if (sucesso && usuario != null) {
                    int idNovo = (idGerado > 0) ? idGerado : eq.getIdEquipamento();
                    Equipamento equipamentoCadastrado = dao.buscarPorId(idNovo, unidadesPermitidas);

                    util.AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Equipamentos",
                        "CRIAR",
                        "equipamentos",
                        (long) idNovo,
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

        } catch (SecurityException e) {
            // Captura o bloqueio de segurança disparado pelo ValidadorContextoUtil (HTTP 403)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> resp = new HashMap<>();
            resp.put("sucesso", false);
            resp.put("mensagem", e.getMessage());
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
        if (!validarPermissao(request, response)) {
            return;
        }

        String idStr = request.getParameter("id");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        List<Integer> unidadesPermitidas = obterUnidadesPermitidas(usuario);

        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                
                Equipamento eq = dao.buscarPorId(id, unidadesPermitidas);
                if (eq == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"sucesso\": false, \"mensagem\": \"Equipamento não encontrado.\"}");
                    return;
                }

                // --- VALIDAÇÃO DE CONTEXTO DE FILIAL (EXCLUSÃO / INATIVAÇÃO) ---
                ValidadorContextoUtil.validarFilialAtiva(request, eq.getOrigemCodigo());

                if (eq.getSituacaoId() != null && eq.getSituacaoId() == 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"sucesso\": false, \"mensagem\": \"Operação negada! Equipamentos em trânsito não podem ser inativados.\"}");
                    return;
                }

                dao.excluirEquipamento(id); 

                String ipCliente = request.getHeader("X-Forwarded-For");
                if (ipCliente == null || ipCliente.isEmpty()) {
                    ipCliente = request.getRemoteAddr();
                }

                if (usuario != null) {
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
                
            } catch (SecurityException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
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