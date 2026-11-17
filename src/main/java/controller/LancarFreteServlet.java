package controller;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder; 
import util.LocalDateAdapter; 
import util.LocalTimeAdapter; 
import java.time.LocalDate; 
import java.time.LocalTime; 
import dao.FreteDAO;
import dao.TransportadoraDAO;
import dao.LogDAO; 
import model.Frete;
import model.Transportadora;
import model.Usuario; 
import model.enums.PerfilUsuario;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.PrintWriter; 
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 

@WebServlet({"/LancarFreteServlet", "/ListarTransportadorasServlet"})
public class LancarFreteServlet extends HttpServlet {
    
    private final TransportadoraDAO transportadoraDAO = new TransportadoraDAO();
    private final FreteDAO freteDAO = new FreteDAO();
    private static final String CADASTRE_PAGE = "/WEB-INF/jsp/lancar-frete.jsp"; 
    private static final String REQUIRED_MODULE = "frete"; 
    private static final int REGISTROS_POR_PAGINA = 30;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

    private boolean checkPermission(Usuario usuario) {
        if (usuario == null) return false;
        String perfil = usuario.getPerfil();
        List<String> modulosPermitidos = usuario.getModulosPermitidos();
        
        if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil) ||
            PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(perfil) ||
            (perfil != null && perfil.toLowerCase().contains("admin"))) {
            return true;
        } 
        
        return modulosPermitidos != null && modulosPermitidos.stream()
                .anyMatch(m -> m.equalsIgnoreCase(REQUIRED_MODULE) || m.equalsIgnoreCase("frete"));
    }
    
    private void handleAccessDeniedGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String errorMessage = "Acesso negado. Você não tem permissão para o módulo de Lançamento de Fretes.";
        HttpSession session = request.getSession(false);
        if (session != null) {
             session.setAttribute("toastMessage", errorMessage);
        }
        response.sendRedirect(request.getContextPath() + "/MenuServlet");
    }
    
    private void handleAccessDeniedAjax(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(Map.of("error", "Acesso negado. Você não tem permissão para realizar esta operação.")));
        response.getWriter().flush();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        // Tratamento seguro para o autocomplete de transportadoras via GET
        if ("/ListarTransportadorasServlet".equals(path)) {
            HttpSession session = request.getSession(false);
            Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
            if (!checkPermission(usuarioLogado)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("[]");
                return;
            }
            processarAutocomplete(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (!checkPermission(usuarioLogado)) {
            handleAccessDeniedGet(request, response);
            return;
        }

        int idUnidadeAtiva = (usuarioLogado.getUnidadeAtivaId() != null) ? usuarioLogado.getUnidadeAtivaId() : 7;

        try {
            List<Transportadora> transportadoras = transportadoraDAO.listar();
            request.setAttribute("transportadoras", transportadoras);
        } catch (Exception e) {
            System.err.println("Aviso ao listar transportadoras: " + e.getMessage());
        }

        try {
            LocalDate hoje = LocalDate.now();
            String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy") : "data";
            String orderDir = request.getParameter("orderDir") != null ? request.getParameter("orderDir") : "DESC";
            
            List<Frete> fretesIniciais = freteDAO.listarFretesDoDiaPaginado(hoje, REGISTROS_POR_PAGINA, 0, orderBy, orderDir, idUnidadeAtiva); 
            int totalFretesIniciais = freteDAO.contarTotalFretesDoDia(hoje, idUnidadeAtiva);
            
            request.setAttribute("fretesIniciaisJsonInput", gson.toJson(fretesIniciais));
            request.setAttribute("totalRegistrosJSP", totalFretesIniciais);
        } catch (Exception e) {
            System.err.println("ERRO AO CARREGAR TELA INICIAL DE FRETES:");
            e.printStackTrace();
            request.setAttribute("fretesIniciaisJsonInput", "[]");
            request.setAttribute("totalRegistrosJSP", 0);
        }

        request.setAttribute("registrosPorPaginaJSP", REGISTROS_POR_PAGINA);
        request.setAttribute("paginaAtualJSP", 1); 

        request.getRequestDispatcher(CADASTRE_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (!checkPermission(usuarioLogado)) {
            handleAccessDeniedAjax(response);
            return;
        }

        String action = request.getParameter("action");
        
        // Se houver parâmetro de ação (buscaGeral ou fretesDoDia), processa via AJAX a listagem idêntica à consulta
        if (action != null && !action.isEmpty()) {
            processarListagemAjax(request, response);
            return;
        }

        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String jsonFrete = getJsonPayload(request);
            Frete frete = gson.fromJson(jsonFrete, Frete.class);
            
            frete.setIdFilial(usuarioLogado.getUnidadeAtivaId() != null ? usuarioLogado.getUnidadeAtivaId() : 7);

            if (freteDAO.existeNfParaTransportadora(frete.getNotaFiscal(), frete.getIdTransportadora())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); 
                out.print(gson.toJson(Map.of(
                    "success", false, 
                    "message", "Atenção: A Nota Fiscal " + frete.getNotaFiscal() + " já foi lançada para esta transportadora!"
                )));
                return; 
            }
            
            Frete freteSalvo = freteDAO.salvarERetornar(frete); 

            if (freteSalvo != null) {
                registrarLog(usuarioLogado, freteSalvo, "INSERÇÃO", request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("success", true, "message", "Frete inserido com sucesso!", "frete", freteSalvo)));
            }
        } catch (Exception e) {
            response.setStatus(500);
            out.print(gson.toJson(Map.of("success", false, "erro", e.getMessage())));
        } finally {
            out.flush();
        }
    }

    private void processarAutocomplete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String termo = request.getParameter("termo");
        try {
            if (termo != null && !termo.trim().isEmpty()) {
                // Utiliza a mesma chamada flexível do DAO de transportadoras
                List<Transportadora> lista = transportadoraDAO.listarItens("ATIVA", 0, 50, termo.trim(), "nome", "ASC");
                response.getWriter().print(gson.toJson(lista));
            } else {
                response.getWriter().print("[]");
            }
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().print("[]");
        }
    }

    private void processarListagemAjax(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
            
            if (usuarioLogado == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            int idUnidadeAtiva = usuarioLogado.getUnidadeAtivaId() != null ? usuarioLogado.getUnidadeAtivaId() : 7;
            String action = request.getParameter("action");
            String termo = request.getParameter("termo") != null ? request.getParameter("termo").trim() : "";
            int pagina = Integer.parseInt(request.getParameter("pagina") != null ? request.getParameter("pagina") : "1");
            int limite = Integer.parseInt(request.getParameter("registrosPorPagina") != null ? request.getParameter("registrosPorPagina") : "30");
            String orderBy = request.getParameter("orderBy");
            String orderDir = request.getParameter("orderDir");
            
            int offset = (pagina - 1) * limite;
            
            List<Frete> lista;
            int total;
            LocalDate hoje = LocalDate.now();

            // Se for busca geral e o termo estiver preenchido
            if ("buscaGeral".equals(action) && !termo.isEmpty()) {
                lista = freteDAO.listarPorTermoPaginado(termo, limite, offset, orderBy, orderDir, idUnidadeAtiva);
                total = freteDAO.contarTotalFretesPorTermo(termo, idUnidadeAtiva);
            } else {
                lista = freteDAO.listarFretesDoDiaPaginado(hoje, limite, offset, orderBy, orderDir, idUnidadeAtiva);
                total = freteDAO.contarTotalFretesDoDia(hoje, idUnidadeAtiva);
            }

            // Padroniza a resposta JSON idêntica ao que o front-end (lancar-frete.js) espera
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fretes", lista);
            responseData.put("totalRegistros", total);

            out.print(gson.toJson(responseData));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace(); 
            response.setStatus(500);
            out.print(gson.toJson(Map.of("success", false, "message", e.getMessage())));
        } finally {
            if (out != null) out.close();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false); 
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (!checkPermission(usuarioLogado)) {
            handleAccessDeniedAjax(response);
            return;
        }
        
        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String jsonFrete = getJsonPayload(request);
            Frete freteNovo = gson.fromJson(jsonFrete, Frete.class);
            
            freteNovo.setIdFilial(usuarioLogado.getUnidadeAtivaId() != null ? usuarioLogado.getUnidadeAtivaId() : 7);
            
            if (freteDAO.existeNfParaTransportadoraEdicao(freteNovo.getNotaFiscal(), freteNovo.getIdTransportadora(), freteNovo.getId())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); 
                out.print(gson.toJson(Map.of(
                    "success", false, 
                    "message", "Atenção: Já existe outro lançamento com a Nota Fiscal " + freteNovo.getNotaFiscal() + " para esta transportadora!"
                )));
                return; 
            }

            Frete freteAntigo = freteDAO.buscarPorIdComNomeTransportadora(freteNovo.getId());

            if (freteDAO.atualizar(freteNovo)) {
                Frete freteAtualizado = freteDAO.buscarPorIdComNomeTransportadora(freteNovo.getId());
                registrarLogEdicao(usuarioLogado, freteAntigo, freteAtualizado, request.getRemoteAddr());

                response.setStatus(HttpServletResponse.SC_OK); 
                out.print(gson.toJson(Map.of("success", true, "message", "Frete atualizado com sucesso!", "frete", freteAtualizado)));
            }
        } catch (Exception e) {
            response.setStatus(500);
            out.print(gson.toJson(Map.of("success", false, "erro", e.getMessage())));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (!checkPermission(usuarioLogado)) {
            handleAccessDeniedAjax(response);
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String idParam = request.getParameter("id"); 
            if (idParam == null) {
                response.setStatus(400);
                return;
            }

            int idFrete = Integer.parseInt(idParam);
            Frete freteExcluido = freteDAO.buscarPorIdComNomeTransportadora(idFrete);

            if (freteDAO.excluir(idFrete)) {
                String detalhe = (freteExcluido != null) 
                    ? "Excluiu frete ID: " + idFrete + " | NF: " + freteExcluido.getNotaFiscal() + " | Transp: " + freteExcluido.getNomeTransportadora()
                    : "Excluiu frete ID: " + idFrete;
                
                LogDAO.registrar(usuarioLogado.getId(), usuarioLogado.getUsername(), "EXCLUSÃO", "fretes", detalhe, request.getRemoteAddr());

                response.setStatus(HttpServletResponse.SC_OK); 
                response.getWriter().write(gson.toJson(Map.of("success", true, "message", "Frete excluído.")));
            }
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write(gson.toJson(Map.of("success", false, "erro", e.getMessage())));
        } finally {
            response.getWriter().flush();
        }
    }
    
    private void registrarLog(Usuario u, Frete f, String acao, String ip) {
        try {
            String detalhe = String.format("ID: %d | NF: %s | Valor: R$ %.2f | %% Frete: %.2f%%", 
                             f.getId(), f.getNotaFiscal(), f.getValorNf(), 
                             f.getPorcentagemFrete() != null ? f.getPorcentagemFrete() : 0.0);
            
            LogDAO.registrar(u.getId(), u.getUsername(), acao, "fretes", detalhe, ip);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void registrarLogEdicao(Usuario u, Frete antigo, Frete novo, String ip) {
        try {
            StringBuilder sb = new StringBuilder("Editou frete ID: " + novo.getId());
            
            if (!Objects.equals(antigo.getValorNf(), novo.getValorNf())) {
                sb.append(String.format(" | Valor NF: %.2f -> %.2f", antigo.getValorNf(), novo.getValorNf()));
            }
            
            if (!Objects.equals(antigo.getValorFrete(), novo.getValorFrete())) {
                sb.append(String.format(" | Valor Frete: %.2f -> %.2f", antigo.getValorFrete(), novo.getValorFrete()));
            }

            if (!Objects.equals(antigo.getPorcentagemFrete(), novo.getPorcentagemFrete())) {
                sb.append(String.format(" | %% Frete: %.2f%% -> %.2f%%", 
                    antigo.getPorcentagemFrete() != null ? antigo.getPorcentagemFrete() : 0.0, 
                    novo.getPorcentagemFrete() != null ? novo.getPorcentagemFrete() : 0.0));
            }

            LogDAO.registrar(u.getId(), u.getUsername(), "EDIÇÃO", "fretes", sb.toString(), ip);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getJsonPayload(HttpServletRequest request) throws IOException {
        StringBuilder jsonPayload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) jsonPayload.append(line);
        }
        return jsonPayload.toString();
    }
}