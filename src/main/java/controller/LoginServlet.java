package controller;

import dao.UsuarioDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletContext;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;
import dao.LogDAO;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private Properties brandingProperties;
    private ServletContext servletContext;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println(">>> NexaCore LoginServlet inicializado com sucesso!");
        
        this.servletContext = getServletContext();
        brandingProperties = new Properties();
        try (InputStream input = servletContext.getResourceAsStream("/WEB-INF/classes/branding.properties")) {
            if (input == null) {
                try (InputStream clInput = getClass().getClassLoader().getResourceAsStream("branding.properties")) {
                     if (clInput != null) { brandingProperties.load(clInput); }
                }
            } else { brandingProperties.load(input); }
        } catch (IOException e) {
            System.err.println("Erro ao carregar branding.properties no LoginServlet init: " + e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("DEBUG NexaCore LoginServlet: doGet chamado.");

        String action = request.getParameter("action");

        // --- Endpoint para Branding JSON (usado por login.js) ---
        if ("getBranding".equals(action)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            JsonObject brandingJson = new JsonObject();

            String companyId = servletContext.getInitParameter("companyId");
            if (companyId == null || companyId.isEmpty()) {
                companyId = "default";
            }

            // Fallback alterado para NexaCore
            String nomeEmpresaDisplay = brandingProperties.getProperty(companyId + ".name", brandingProperties.getProperty("default.name", "NexaCore"));
            String faviconFileName = brandingProperties.getProperty(companyId + ".favicon", brandingProperties.getProperty("default.favicon", ""));
            String logoImageFileName = brandingProperties.getProperty(companyId + ".logo", brandingProperties.getProperty("default.logo", ""));

            brandingJson.addProperty("nomeEmpresa", nomeEmpresaDisplay);
            
            if (faviconFileName != null && !faviconFileName.isEmpty()) {
                brandingJson.addProperty("faviconPath", request.getContextPath() + "/images/favicons/" + faviconFileName);
                brandingJson.addProperty("faviconMimeType", faviconFileName.toLowerCase().endsWith(".png") ? "image/png" : "image/x-icon");
            } else {
                brandingJson.addProperty("faviconPath", "");
                brandingJson.addProperty("faviconMimeType", "");
            }
            
            if (logoImageFileName != null && !logoImageFileName.isEmpty()) {
                brandingJson.addProperty("logoImagePath", request.getContextPath() + "/images/" + logoImageFileName);
            } else {
                brandingJson.addProperty("logoImagePath", "");
            }

            out.print(gson.toJson(brandingJson));
            out.close();
            return;
        }

        // --- Lógica de Limpeza de URL ---
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null && (queryString.contains("message=") || queryString.contains("custom_message="))) {
            StringBuilder newQueryString = new StringBuilder();
            boolean firstParam = true;
            for (String param : request.getParameterMap().keySet()) {
                if (!param.equals("message") && !param.equals("custom_message")) {
                    for (String value : request.getParameterValues(param)) {
                        if (!firstParam) {
                            newQueryString.append("&");
                        }
                        newQueryString.append(URLEncoder.encode(param, StandardCharsets.UTF_8.toString()))
                                      .append("=")
                                      .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
                        firstParam = false;
                    }
                }
            }
            String newRedirectUrl = requestURL;
            if (newQueryString.length() > 0) {
                newRedirectUrl += "?" + newQueryString.toString();
            }
            response.sendRedirect(newRedirectUrl);
            return;
        }

        request.setAttribute("forwardedFromLoginServlet", true);
        
        // Direciona para a view de login padrão
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("DEBUG NexaCore LoginServlet: doPost chamado.");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        JsonObject jsonResponse = new JsonObject();

        String xRequestedWith = request.getHeader("X-Requested-With");
        String username;
        String password;

        if ("XMLHttpRequest".equals(xRequestedWith)) {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
            username = jsonRequest.get("usuario").getAsString();
            password = jsonRequest.get("senha").getAsString();
        } else {
            username = request.getParameter("usuario");
            password = request.getParameter("senha");
        }

        if (username != null) username = username.trim();
        if (password != null) password.trim(); 

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Por favor, preencha todos os campos.");
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuarioDoBanco = usuarioDAO.validarLogin(username);

            if (usuarioDoBanco == null) {
                System.out.println("NexaCore Login DEBUG: Usuário '" + username + "' NÃO encontrado.");
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Usuário ou senha inválidos.");
                out.print(gson.toJson(jsonResponse));
                return;
            }

            if (!usuarioDoBanco.isAtivo()) {
                System.out.println("NexaCore Login DEBUG: Usuário '" + username + "' está INATIVO.");
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Sua conta está inativa. Por favor, entre em contato com o administrador.");
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            // if (usuarioDoBanco.getSenha() != null && usuarioDoBanco.getSenha().equals(password)) {//Sem criptografia
            if (usuarioDoBanco.getSenha() != null && BCrypt.checkpw(password, usuarioDoBanco.getSenha())) {
                Usuario usuarioCompleto = usuarioDAO.buscarUsuarioPorId(usuarioDoBanco.getId());
             // === ADICIONE ESTAS DUAS LINHAS AQUI ===
                usuarioDAO.atualizarUltimoAcesso(usuarioDoBanco.getId());
                System.out.println("NexaCore Login: Último acesso atualizado para o usuário ID: " + usuarioDoBanco.getId());
            	
                // --- TRAVA DE SEGURANÇA: FILIAIS VINCULADAS ---
                boolean ehAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuarioCompleto.getPerfil()) || 
                                  "ADMINISTRADOR".equalsIgnoreCase(usuarioCompleto.getPerfil());
                
                if (!ehAdmin && (usuarioCompleto.getUnidadesPermitidas() == null || usuarioCompleto.getUnidadesPermitidas().isEmpty())) {
                    System.out.println("NexaCore Login bloqueado: Usuário '" + username + "' não possui filiais vinculadas.");
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Acesso negado: Seu usuário não possui nenhuma filial vinculada. Procure o administrador.");
                    out.print(gson.toJson(jsonResponse));
                    return;
                }
                
                HttpSession session = request.getSession();
                session.setAttribute("usuarioLogado", usuarioCompleto);
                
                // --- REGISTRO DE LOG ---
                try {
                    LogDAO.registrar(
                        usuarioDoBanco.getId(), 
                        usuarioDoBanco.getUsername(), 
                        "LOGIN", 
                        "usuarios", 
                        "Acesso realizado ao NexaCore. Unidade Ativa: " + usuarioCompleto.getUnidadeAtivaNome(), 
                        request.getRemoteAddr()
                    );
                } catch (Exception e) {
                    System.err.println("Erro ao registrar log de login no NexaCore: " + e.getMessage());
                }
                
                System.out.println("NexaCore Login bem-sucedido para o usuário: " + usuarioDoBanco.getUsername());
                
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Login bem-sucedido!");
                
                // Nota: Quando criarmos o MenuServlet, podemos alterar este destino para request.getContextPath() + "/MenuServlet"
                jsonResponse.addProperty("redirect", request.getContextPath() + "/MenuServlet");
                out.print(gson.toJson(jsonResponse));
            } else {
                System.out.println("NexaCore Login DEBUG: Senha INCORRETA para o usuário: " + username);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Usuário ou senha inválidos.");
                out.print(gson.toJson(jsonResponse));
            }

        } catch (Exception e) {
            System.err.println("Erro interno ao tentar fazer login no NexaCore LoginServlet: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Ocorreu um erro interno. Tente novamente mais tarde.");
            out.print(gson.toJson(jsonResponse));
        } finally {
            out.close();
        }
    }
}