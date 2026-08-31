package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dao.ManutencaoDAO;
import model.ManutencaoChamado;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "ManutencaoServlet", urlPatterns = {"/ManutencaoServlet", "/ConsultaChamadosServlet", "/api/manutencoes/*"})
public class ManutencaoServlet extends HttpServlet {

    private ManutencaoDAO dao = new ManutencaoDAO();
    
    // GSON CONFIGURADO COM SUPORTE A LOCALDATE (Resolve o erro)
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(formatter.format(value));
                }
            }

            @Override
            public LocalDate read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                String dateStr = in.nextString();
                if (dateStr == null || dateStr.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateStr, formatter);
            }
        })
        .create();
    
 // Método de validação igual ao que você usou no CadastrarUsuarioServlet
    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        // Valida se possui o módulo "manutencao_chamados" liberado
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("manutencao_chamados"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Acesso negado. Você não possui permissão para o módulo de manutenção de chamados.\"}");
            return false;
        }
        return true;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String servletPath = req.getServletPath();
    	
    	// Direciona para a tela de Abertura de Chamado
        if ("/ManutencaoServlet".equals(servletPath)) {
            if (!validarPermissao(req, resp)) return;
            req.getRequestDispatcher("/WEB-INF/jsp/manutencao-abertura.jsp").forward(req, resp);
            return;
        }
        
        // Direciona para a tela de Consulta de Chamados
        if ("/ConsultaChamadosServlet".equals(servletPath)) {
            if (!validarPermissao(req, resp)) return;
            req.getRequestDispatcher("/WEB-INF/jsp/consulta-chamado.jsp").forward(req, resp);
            return;
        }

           	
    	// Valida a permissão antes de executar qualquer lógica
        if (!validarPermissao(req, resp)) {
            return;
        }
        
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String pathInfo = req.getPathInfo(); // Captura o que vem depois de /api/manutencoes
            String idEquipParam = req.getParameter("idEquipamento");

            if (idEquipParam != null && !idEquipParam.isEmpty()) {
                // Caso venha filtrado por equipamento específico (Histórico do Equipamento)
                Long idEquipamento = Long.parseLong(idEquipParam);
                List<ManutencaoChamado> historico = dao.listarPorEquipamento(idEquipamento);
                out.print(gson.toJson(historico));
            } else if (pathInfo != null && pathInfo.equals("/listar")) {
                // ADICIONADO: Atende a rota /api/manutencoes/listar da nova tela de consulta
                List<ManutencaoChamado> todos = dao.listarTodos();
                out.print(gson.toJson(todos));
            } else if (pathInfo != null && pathInfo.equals("/listar")) {
                String busca = req.getParameter("busca");
                String status = req.getParameter("status");
                String tipo = req.getParameter("tipo");
                String prioridade = req.getParameter("prioridade");

                List<ManutencaoChamado> todos = dao.listarComFiltros(busca, status, tipo, prioridade);
                out.print(gson.toJson(todos));
            } else {
                // Se chamado sem parâmetros específicos, tenta retornar todos por segurança
                List<ManutencaoChamado> todos = dao.listarTodos();
                out.print(gson.toJson(todos));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	// Valida a permissão antes de executar qualquer lógica
        if (!validarPermissao(req, resp)) {
            return;
        }
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            // Rota para excluir/cancelar o chamado
            if ("/excluir".equals(pathInfo)) {
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado.\"}");
                    return;
                }

                Long idChamado = Long.parseLong(idParam);
                dao.excluir(idChamado); // Altera o status para Cancelado e libera o equipamento

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado cancelado e equipamento liberado com sucesso!\"}");
                return;
            }

            // Rota para atualizar o chamado (vindo do modal de gerenciar)
            if ("/atualizar".equals(pathInfo)) {
                BufferedReader reader = req.getReader();
                ManutencaoChamado chamado = gson.fromJson(reader, ManutencaoChamado.class);
                
                if (chamado == null || chamado.getIdChamado() == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"sucesso\": false, \"mensagem\": \"ID do chamado não informado para atualização.\"}");
                    return;
                }

                dao.atualizar(chamado, chamado.isReparado());

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"sucesso\": true, \"mensagem\": \"Chamado atualizado com sucesso!\"}");
                return;
            }

            // Fluxo normal de Abertura de Chamado (caso POST seja feito diretamente na raiz)
            BufferedReader reader = req.getReader();
            ManutencaoChamado chamado = gson.fromJson(reader, ManutencaoChamado.class);

            if (chamado == null || chamado.getIdEquipamento() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"sucesso: false, \"mensagem\": \"Dados incompletos para abertura do chamado.\"}");
                return;
            }

            if (chamado.getDataAbertura() == null) {
                chamado.setDataAbertura(LocalDate.now());
            }

            Long idGerado = dao.inserir(chamado);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"sucesso\": true, \"idChamado\": " + idGerado + ", \"mensagem\": \"Chamado aberto com sucesso!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"sucesso\": false, \"mensagem\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}