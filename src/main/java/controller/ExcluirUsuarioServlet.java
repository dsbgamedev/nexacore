package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import model.enums.PerfilUsuario; // Certifique-se de que este enum existe

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@WebServlet("/ExcluirUsuarioServlet")
public class ExcluirUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = null;
        if (session != null) {
            usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        }

        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=session_expired");
            return;
        }

        String messageType = "error";
        String customMessage = URLEncoder.encode("Ocorreu um erro desconhecido ao excluir usuário(s).", StandardCharsets.UTF_8.toString());

        boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());
        boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());

        if (!isUsuarioLogadoSuperAdmin && !isUsuarioLogadoAdmin) {
            customMessage = URLEncoder.encode("Você não tem permissão para excluir usuários.", StandardCharsets.UTF_8.toString());
            response.sendRedirect(request.getContextPath() + "/listarUsuarios.jsp?message=" + messageType + "&custom_message=" + customMessage);
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                int idExcluir = Integer.parseInt(idParam);

                if (idExcluir == usuarioLogado.getId()) {
                    customMessage = URLEncoder.encode("Você não pode excluir a si mesmo.", StandardCharsets.UTF_8.toString());
                    response.sendRedirect(request.getContextPath() + "/listarUsuarios.jsp?message=" + messageType + "&custom_message=" + customMessage);
                    return;
                }

                UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario usuarioSendoExcluido = usuarioDAO.buscarUsuarioPorId(idExcluir);

                if (usuarioSendoExcluido == null) {
                    customMessage = URLEncoder.encode("Usuário não encontrado para exclusão.", StandardCharsets.UTF_8.toString());
                    response.sendRedirect(request.getContextPath() + "/listarUsuarios.jsp?message=" + messageType + "&custom_message=" + customMessage);
                    return;
                }

                if (isUsuarioLogadoAdmin) {
                    if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()) || PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil())) {
                        customMessage = URLEncoder.encode("Você não tem permissão para excluir Super Administradores ou outros Administradores.", StandardCharsets.UTF_8.toString());
                        response.sendRedirect(request.getContextPath() + "/listarUsuarios.jsp?message=" + messageType + "&custom_message=" + customMessage);
                        return;
                    }
                }

                if (usuarioDAO.excluirUsuario(idExcluir)) {
                    messageType = "success";
                    customMessage = URLEncoder.encode("Usuário excluído com sucesso!", StandardCharsets.UTF_8.toString());
                } else {
                    customMessage = URLEncoder.encode("Falha ao excluir usuário. Verifique.", StandardCharsets.UTF_8.toString());
                }

            } catch (NumberFormatException e) {
                customMessage = URLEncoder.encode("ID de usuário inválido para exclusão.", StandardCharsets.UTF_8.toString());
            } catch (SQLException e) {
                System.err.println("Erro SQL ao excluir usuário: " + e.getMessage());
                customMessage = URLEncoder.encode("Erro no banco de dados ao excluir usuário: " + e.getMessage(), StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                System.err.println("Erro inesperado ao excluir usuário: " + e.getMessage());
                customMessage = URLEncoder.encode("Ocorreu um erro inesperado ao excluir usuário: " + e.getMessage(), StandardCharsets.UTF_8.toString());
            }
        }
        else { // Lógica para exclusão de múltiplos via GET (embora POST seja preferível para exclusão)
            String[] idsParam = request.getParameterValues("ids");
            if (idsParam != null && idsParam.length > 0) {
                List<Integer> idsExcluir = new ArrayList<>();
                List<Integer> idsNaoPermitidos = new ArrayList<>();

                try {
                    for (String idStr : idsParam) {
                        int currentId = Integer.parseInt(idStr);
                        if (currentId == usuarioLogado.getId()) {
                            idsNaoPermitidos.add(currentId);
                            continue;
                        }

                        UsuarioDAO usuarioDAO = new UsuarioDAO();
                        Usuario usuarioSendoExcluido = usuarioDAO.buscarUsuarioPorId(currentId);

                        if (usuarioSendoExcluido == null) {
                            continue;
                        }

                        if (isUsuarioLogadoAdmin) {
                            if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()) || PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil())) {
                                idsNaoPermitidos.add(currentId);
                                continue;
                            }
                        }
                        idsExcluir.add(currentId);
                    }

                    if (idsExcluir.isEmpty()) {
                        if (!idsNaoPermitidos.isEmpty()) {
                            customMessage = URLEncoder.encode("Você não tem permissão para excluir os usuários selecionados (perfis Super Administrador/Administrador ou você mesmo).", StandardCharsets.UTF_8.toString());
                        } else {
                            customMessage = URLEncoder.encode("Nenhum usuário válido selecionado para exclusão.", StandardCharsets.UTF_8.toString());
                        }
                    } else {
                        UsuarioDAO usuarioDAO = new UsuarioDAO();
                        int rowsAffected = usuarioDAO.excluirMultiplosUsuarios(idsExcluir);

                        if (rowsAffected > 0) {
                            messageType = "success";
                            customMessage = URLEncoder.encode(rowsAffected + " usuário(s) excluído(s) com sucesso!", StandardCharsets.UTF_8.toString());
                            if (!idsNaoPermitidos.isEmpty()) {
                                customMessage += URLEncoder.encode(" Alguns usuários não puderam ser excluídos devido a permissões.", StandardCharsets.UTF_8.toString());
                                messageType = "warning";
                            }
                        } else {
                            customMessage = URLEncoder.encode("Nenhum usuário foi excluído. Verifique as permissões ou se os usuários existem.", StandardCharsets.UTF_8.toString());
                        }
                    }

                } catch (NumberFormatException e) {
                    customMessage = URLEncoder.encode("Um ou mais IDs de usuário são inválidos para exclusão.", StandardCharsets.UTF_8.toString());
                } catch (SQLException e) {
                    System.err.println("Erro SQL ao excluir múltiplos usuários: " + e.getMessage());
                    customMessage = URLEncoder.encode("Erro no banco de dados ao excluir múltiplos usuários: " + e.getMessage(), StandardCharsets.UTF_8.toString());
                } catch (Exception e) {
                    System.err.println("Erro inesperado ao excluir múltiplos usuários: " + e.getMessage());
                    customMessage = URLEncoder.encode("Ocorreu um erro inesperado ao excluir múltiplos usuários: " + e.getMessage(), StandardCharsets.UTF_8.toString());
                }
            } else {
                customMessage = URLEncoder.encode("Nenhum usuário selecionado para exclusão.", StandardCharsets.UTF_8.toString());
            }
        }

        response.sendRedirect(request.getContextPath() + "/listarUsuarios.jsp?message=" + messageType + "&custom_message=" + customMessage);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = null;
        if (session != null) {
            usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        }

        if (usuarioLogado == null) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Sessão expirada. Faça login novamente.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(jsonResponse));
            return;
        }

        boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());
        boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());

        if (!isUsuarioLogadoSuperAdmin && !isUsuarioLogadoAdmin) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Você não tem permissão para excluir usuários.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write(gson.toJson(jsonResponse));
            return;
        }

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            String action = jsonRequest.get("action").getAsString(); // Lendo a ação do JSON

            UsuarioDAO usuarioDAO = new UsuarioDAO();

            if ("delete_single".equals(action)) { // Ação esperada do JSP
                int idExcluir = jsonRequest.get("id").getAsInt();

                if (idExcluir == usuarioLogado.getId()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Você não pode excluir a si mesmo.");
                    out.write(gson.toJson(jsonResponse));
                    return;
                }

                Usuario usuarioSendoExcluido = usuarioDAO.buscarUsuarioPorId(idExcluir);

                if (usuarioSendoExcluido == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Usuário não encontrado para exclusão.");
                    out.write(gson.toJson(jsonResponse));
                    return;
                }

                if (isUsuarioLogadoAdmin) {
                    if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()) || PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil())) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Você não tem permissão para excluir Super Administradores ou outros Administradores.");
                        out.write(gson.toJson(jsonResponse));
                        return;
                    }
                }

                if (usuarioDAO.excluirUsuario(idExcluir)) {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Usuário excluído com sucesso!");
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Falha ao excluir usuário. Verifique.");
                }

            } else if ("delete_multiple".equals(action)) { // Ação esperada do JSP
                JsonArray idsJsonArray = jsonRequest.getAsJsonArray("ids");
                List<Integer> idsExcluir = new ArrayList<>();
                List<Integer> idsNaoPermitidos = new ArrayList<>();

                for (int i = 0; i < idsJsonArray.size(); i++) {
                    int currentId = idsJsonArray.get(i).getAsInt();
                    if (currentId == usuarioLogado.getId()) {
                        idsNaoPermitidos.add(currentId);
                        continue;
                    }

                    Usuario usuarioSendoExcluido = usuarioDAO.buscarUsuarioPorId(currentId);

                    if (usuarioSendoExcluido == null) {
                        continue;
                    }

                    if (isUsuarioLogadoAdmin) {
                        if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil()) || PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioSendoExcluido.getPerfil())) {
                            idsNaoPermitidos.add(currentId);
                            continue;
                        }
                    }
                    idsExcluir.add(currentId);
                }

                if (idsExcluir.isEmpty()) {
                    if (!idsNaoPermitidos.isEmpty()) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Você não tem permissão para excluir os usuários selecionados (perfis Super Administrador/Administrador ou você mesmo).");
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Nenhum usuário válido selecionado para exclusão.");
                    }
                } else {
                    int rowsAffected = usuarioDAO.excluirMultiplosUsuarios(idsExcluir);

                    if (rowsAffected > 0) {
                        jsonResponse.addProperty("success", true);
                        String msg = rowsAffected + " usuário(s) excluído(s) com sucesso!";
                        if (!idsNaoPermitidos.isEmpty()) {
                            msg += " Alguns usuários não puderam ser excluídos devido a permissões.";
                        }
                        jsonResponse.addProperty("message", msg);
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Nenhum usuário foi excluído. Verifique as permissões ou se os usuários existem.");
                    }
                }

            } else {
                // Este é o caso que está gerando a mensagem de erro atual
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Ação inválida ou IDs ausentes para exclusão.");
            }

        } catch (SQLException e) {
            System.err.println("Erro SQL no ExcluirUsuarioServlet (POST): " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Erro no banco de dados ao excluir usuário(s).");
        } catch (Exception e) {
            System.err.println("Erro inesperado no ExcluirUsuarioServlet (POST): " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Erro interno do servidor ao processar a exclusão.");
        } finally {
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
}
