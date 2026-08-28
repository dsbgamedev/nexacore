package controller;

import com.google.gson.Gson;
import dao.LogDAO;
import dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import model.enums.PerfilUsuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

@WebServlet("/GerenciarUsuariosServlet")
public class GerenciarUsuariosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseMap = new HashMap<>();

        HttpSession session = request.getSession(false);
        final Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("success", false);
            responseMap.put("error", "Sessão expirou ou não autenticado.");
            out.print(gson.toJson(responseMap));
            return;
        }

        String action = null;
        int id = 0;
        List<Integer> idsParaExcluir = new ArrayList<>();

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            if (requestBody != null && !requestBody.isEmpty()) {
                JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
                if (jsonRequest.has("action")) action = jsonRequest.get("action").getAsString();
                if (jsonRequest.has("id")) id = jsonRequest.get("id").getAsInt();
                if (jsonRequest.has("ids")) {
                    JsonArray idsJsonArray = jsonRequest.getAsJsonArray("ids");
                    for (int i = 0; i < idsJsonArray.size(); i++) {
                        idsParaExcluir.add(idsJsonArray.get(i).getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("success", false);
            responseMap.put("message", "Formato de requisição inválido.");
            out.print(gson.toJson(responseMap));
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());
        boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());

        try {
            if ("delete".equals(action)) {
                if (!isUsuarioLogadoSuperAdmin && !isUsuarioLogadoAdmin) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    responseMap.put("success", false);
                    responseMap.put("message", "Sem permissão para excluir.");
                    out.print(gson.toJson(responseMap));
                    return;
                }

                Usuario usuarioSendoExcluido = usuarioDAO.buscarUsuarioPorId(id);
                if (usuarioSendoExcluido == null) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Usuário não encontrado.");
                    out.print(gson.toJson(responseMap));
                    return;
                }

                if (id == usuarioLogado.getId()) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Você não pode excluir a si mesmo.");
                    out.print(gson.toJson(responseMap));
                    return;
                }

                if (isUsuarioLogadoAdmin && (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()) || 
                    PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()))) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Admin não pode excluir SuperAdmin ou outros Admins.");
                    out.print(gson.toJson(responseMap));
                    return;
                }

                if (usuarioDAO.excluirUsuario(id)) {
                    // --- LOG INDIVIDUAL ---
                    try {
                        LogDAO.registrar(usuarioLogado.getId(), usuarioLogado.getUsername(), "EXCLUSÃO", "usuarios", 
                            "Excluiu o utilizador: " + usuarioSendoExcluido.getUsername() + " (ID: " + id + ")", 
                            request.getRemoteAddr());
                    } catch (Exception e) { System.err.println("Erro Log: " + e.getMessage()); }

                    responseMap.put("success", true);
                    responseMap.put("message", "Usuário excluído com sucesso!");
                }
            } else if ("deleteMultiple".equals(action)) {
                if (idsParaExcluir.isEmpty()) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Nenhum usuário selecionado.");
                } else {
                    List<Integer> idsRealmenteExcluir = new ArrayList<>();
                    List<String> nomesParaLog = new ArrayList<>();

                    for (int currentId : idsParaExcluir) {
                        if (currentId == usuarioLogado.getId()) continue;
                        Usuario u = usuarioDAO.buscarUsuarioPorId(currentId);
                        if (u == null) continue;

                        if (isUsuarioLogadoAdmin && (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(u.getPerfil()) || 
                            PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(u.getPerfil()))) continue;

                        idsRealmenteExcluir.add(currentId);
                        nomesParaLog.add(u.getUsername());
                    }

                    if (!idsRealmenteExcluir.isEmpty()) {
                        int rowsAffected = usuarioDAO.excluirMultiplosUsuarios(idsRealmenteExcluir);
                        if (rowsAffected > 0) {
                            // --- LOG MÚLTIPLO ---
                            try {
                                LogDAO.registrar(usuarioLogado.getId(), usuarioLogado.getUsername(), "EXCLUSÃO MÚLTIPLA", "usuarios", 
                                    "Excluiu " + rowsAffected + " utilizadores: [" + String.join(", ", nomesParaLog) + "]", 
                                    request.getRemoteAddr());
                            } catch (Exception e) { System.err.println("Erro Log: " + e.getMessage()); }

                            responseMap.put("success", true);
                            responseMap.put("message", rowsAffected + " usuário(s) excluído(s) com sucesso!");
                        }
                    } else {
                        responseMap.put("success", false);
                        responseMap.put("message", "Nenhum usuário válido selecionado para exclusão.");
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("success", false);
            responseMap.put("message", "Erro: " + e.getMessage());
        } finally {
            out.print(gson.toJson(responseMap));
            out.flush();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        final Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (usuarioLogado == null) {
            if (isAjaxRequest) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("{\"success\":false,\"error\":\"Sessão expirada\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?message=session_expired");
            }
            return;
        }

        String action = request.getParameter("action");
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());
        boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());

        if ("list".equals(action) && isAjaxRequest) {
            response.setContentType("application/json");
            try {
                List<Usuario> usuarios = usuarioDAO.listarTodosUsuarios();
                if (!isUsuarioLogadoSuperAdmin) {
                    usuarios = usuarios.stream()
                        .filter(u -> isUsuarioLogadoAdmin ? !PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(u.getPerfil()) : u.getId() == usuarioLogado.getId())
                        .collect(Collectors.toList());
                }
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("users", usuarios);
                response.getWriter().print(gson.toJson(responseMap));
            } catch (SQLException e) {
                response.setStatus(500);
            }
        } else if ("edit".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/CadastrarUsuarioServlet?action=edit&id=" + request.getParameter("id"));
        } else if ("new".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/CadastrarUsuarioServlet");
        } else {
            request.setAttribute("title", "Gerenciamento de Usuários");
            request.setAttribute("usuarioLogadoId", usuarioLogado.getId());
            request.setAttribute("perfilUsuarioLogado", usuarioLogado.getPerfil()); 
            request.getRequestDispatcher("/listarUsuarios.jsp").forward(request, response);
        }
    }
}