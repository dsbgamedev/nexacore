package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.FornecedorDAO;
import model.Fornecedor;
import model.FornecedorEndereco;
import model.Usuario;
import util.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(name = "FornecedorServlet", urlPatterns = {"/FornecedorServlet", "/FornecedorServlet/*"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class FornecedorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final FornecedorDAO dao = new FornecedorDAO();
    private final Gson gson = new Gson();

    // Validação estrita para ações de escrita (Inserir / Editar / Excluir)
    private boolean validarPermissaoEscrita(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet?message=session_expired");
            return false;
        }

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
        
        boolean temEscrita = isAdmin ||
            usuario.temPermissao("fornecedores", "INSERIR") ||
            usuario.temPermissao("fornecedores", "EDITAR") ||
            usuario.temPermissao("fornecedores", "EXCLUIR");

        if (!temEscrita) {
            String path = request.getRequestURI();
            if (path != null && path.contains("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão de escrita para o módulo de fornecedores.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/MenuServlet?erro=sem_permissao&modulo=fornecedores");
            }
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Valida se tem permissão de escrita para abrir a tela de cadastro/edição
        if (!validarPermissaoEscrita(req, resp)) {
            return;
        }

        String acao = req.getParameter("acao");

        try {
            if (acao == null || acao.isBlank()) {
                carregarFormulario(req, resp, new Fornecedor());
            } else if ("editar".equals(acao)) {
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.isBlank()) {
                    resp.sendError(400, "ID do fornecedor não informado.");
                    return;
                }
                long id = Long.parseLong(idParam);
                Fornecedor f = dao.buscarPorId(id);
                
                if (f == null) {
                    resp.sendError(404, "Fornecedor não encontrado.");
                    return;
                }
                carregarFormulario(req, resp, f);
            } else {
                resp.sendRedirect(req.getContextPath() + "/ConsultaFornecedorServlet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            String erroEnc = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Erro interno", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/ConsultaFornecedorServlet?erro=" + erroEnc);
        }
    }

    private void carregarFormulario(HttpServletRequest req, HttpServletResponse resp, Fornecedor f) throws Exception {
        req.setAttribute("fornecedor", f);
        req.setAttribute("filiais", dao.listarFiliais());
        req.setAttribute("categorias", dao.listarCategorias()); // NOVO
        req.setAttribute("enderecosJson", gson.toJson(f.getEnderecos()));
        req.setAttribute("filiaisIdsJson", gson.toJson(f.getFiliaisIds()));
        req.getRequestDispatcher("/WEB-INF/jsp/cadastro-fornecedor.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        if (!validarPermissaoEscrita(req, resp)) {
            return;
        }

        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        String acao = req.getParameter("acao");
        String pathInfo = req.getPathInfo();

        try {
            // Ação de Exclusão
            if ("excluir".equals(acao) || (pathInfo != null && pathInfo.contains("excluir"))) {
                // Valida permissão específica de exclusão
                boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
                if (!isAdmin && !usuario.temPermissao("fornecedores", "EXCLUIR")) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado: Você não possui permissão para excluir fornecedores.\"}");
                    return;
                }

                String idParam = req.getParameter("id");
                if (idParam == null || idParam.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"ID do fornecedor não informado.\"}");
                    return;
                }

                long id = Long.parseLong(idParam);
                Fornecedor fornecedorAnterior = dao.buscarPorId(id);
                if (fornecedorAnterior == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Fornecedor não encontrado.\"}");
                    return;
                }

                boolean excluido = dao.excluir(id);
                if (excluido) {
                    // Auditoria de Exclusão
                    String ipCliente = req.getHeader("X-Forwarded-For");
                    if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

                    AuditoriaService.registrar(
                        Long.valueOf(usuario.getId()),
                        usuario.getUsername(),
                        "Fornecedores",
                        "EXCLUIR",
                        "fornecedores",
                        id,
                        "Exclusão de fornecedor",
                        gson.toJson(fornecedorAnterior),
                        "{\"status\": \"Excluído\"}",
                        ipCliente
                    );

                    json(resp, 200, Map.of("sucesso", true, "mensagem", "Fornecedor excluído com sucesso."));
                } else {
                    json(resp, 404, Map.of("sucesso", false, "mensagem", "Não foi possível excluir o fornecedor."));
                }
                return;
            }

            // Mapeamento e montagem do objeto Fornecedor para Salvar/Atualizar
            Fornecedor f = new Fornecedor();
            String idStr = req.getParameter("idFornecedor");
            boolean isNovo = (idStr == null || idStr.isBlank());
            
            if (!isNovo) {
                f.setIdFornecedor(Long.valueOf(idStr));
            }

            f.setTipoPessoa(req.getParameter("tipoPessoa"));
            f.setRazaoSocial(req.getParameter("razaoSocial"));
            f.setNomeFantasia(req.getParameter("nomeFantasia"));
            f.setDocumento(req.getParameter("documento"));
            f.setInscricaoEstadual(req.getParameter("inscricaoEstadual"));
            f.setInscricaoMunicipal(req.getParameter("inscricaoMunicipal"));
            String idCatStr = req.getParameter("idCategoria");
            if (idCatStr != null && !idCatStr.isBlank()) {
                f.setIdCategoria(Integer.valueOf(idCatStr));
            } else {
                f.setIdCategoria(null);
            }
            f.setContatoPrincipal(req.getParameter("contatoPrincipal"));
            f.setTelefone(req.getParameter("telefone"));
            f.setCelular(req.getParameter("celular"));
            f.setEmail(req.getParameter("email"));
            f.setSite(req.getParameter("site"));
            f.setStatus(req.getParameter("status"));
            f.setObservacao(req.getParameter("observacao"));

            // Desserialização dos Endereços em JSON
            Type tipo = new TypeToken<List<FornecedorEndereco>>() {}.getType();
            List<FornecedorEndereco> enderecos = Collections.emptyList();
            String jsonEnderecos = req.getParameter("enderecosJson");
            
            if (jsonEnderecos != null && !jsonEnderecos.isBlank()) {
                List<FornecedorEndereco> l = gson.fromJson(jsonEnderecos, tipo);
                if (l != null) enderecos = l;
            }
            f.setEnderecos(enderecos);

            // Coleta dos IDs das filiais selecionadas
            List<Integer> ids = new ArrayList<>();
            String[] valores = req.getParameterValues("filiaisIds");
            if (valores != null) {
                for (String v : valores) {
                    try { ids.add(Integer.valueOf(v)); } catch (NumberFormatException ignored) {}
                }
            }
            f.setFiliaisIds(ids);

            // Validações básicas de negócio antes de salvar
            if (f.getRazaoSocial() == null || f.getRazaoSocial().trim().isEmpty()) {
                json(resp, 400, Map.of("sucesso", false, "mensagem", "Informe a razão social/nome."));
                return;
            }

            Fornecedor fornecedorAntigo = null;
            if (!isNovo) {
                fornecedorAntigo = dao.buscarPorId(f.getIdFornecedor());
            }
            
         // 1. Validação de obrigatoriedade dos dados cadastrais principais
            if (f.getRazaoSocial() == null || f.getRazaoSocial().trim().isEmpty() ||
                f.getDocumento() == null || f.getDocumento().trim().isEmpty() ||
                f.getStatus() == null || f.getStatus().trim().isEmpty()) {
                json(resp, 400, Map.of("sucesso", false, "mensagem", "Preencha todos os campos obrigatórios dos dados cadastrais."));
                return;
            }

            // 2. Validação rigorosa de CPF/CNPJ e Inscrições Estaduais/Municipais
            String docLimpo = f.getDocumento().replaceAll("\\D", "");
            boolean isPf = "PF".equalsIgnoreCase(f.getTipoPessoa());

            if (isPf) {
                if (docLimpo.length() != 11) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CPF inválido. Deve conter exatamente 11 dígitos."));
                    return;
                }
            } else {
                if (docLimpo.length() != 14) {
                    json(resp, 400, Map.of("sucesso", false, "mensagem", "CNPJ inválido. Deve conter exatamente 14 dígitos."));
                    return;
                }
            }

            // 3. Validação de obrigatoriedade de pelo menos uma filial selecionada
            if (f.getFiliaisIds() == null || f.getFiliaisIds().isEmpty()) {
                json(resp, 400, Map.of("sucesso", false, "mensagem", "Selecione pelo menos uma filial ou matriz autorizada para este fornecedor."));
                return;
            }

            long salvo = dao.salvar(f);

            // Auditoria de Criação ou Atualização
            String ipCliente = req.getHeader("X-Forwarded-For");
            if (ipCliente == null || ipCliente.isEmpty()) ipCliente = req.getRemoteAddr();

            AuditoriaService.registrar(
                Long.valueOf(usuario.getId()),
                usuario.getUsername(),
                "Fornecedores",
                isNovo ? "CRIAR" : "EDITAR",
                "fornecedores",
                salvo,
                isNovo ? "Cadastro de novo fornecedor" : "Atualização de dados de fornecedor",
                isNovo ? "{}" : gson.toJson(fornecedorAntigo),
                gson.toJson(f),
                ipCliente
            );

            json(resp, 200, Map.of(
                "sucesso", true,
                "id", salvo,
                "mensagem", isNovo ? "Fornecedor cadastrado com sucesso." : "Fornecedor atualizado com sucesso."
            ));

        } catch (Exception e) {
            e.printStackTrace();
            json(resp, 400, Map.of("sucesso", false, "mensagem", mensagem(e)));
        }
    }

    private void json(HttpServletResponse r, int status, Object o) throws IOException {
        r.setStatus(status);
        r.getWriter().write(gson.toJson(o));
    }

    private String mensagem(Exception e) {
        String m = e.getMessage();
        if (m == null || m.isBlank()) return "Não foi possível concluir a operação.";
        if (m.toLowerCase().contains("ux_fornecedores_documento") || m.toLowerCase().contains("duplicate key")) {
            return "Já existe um fornecedor cadastrado com este CPF/CNPJ.";
        }
        return m;
    }
}