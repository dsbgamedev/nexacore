package controller;

import dao.UsuarioDAO;
import model.Usuario;
import util.EmailUtil;
import util.TokenGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.net.URLEncoder; // Adicionado para URLEncoder
import java.nio.charset.StandardCharsets; // Adicionado para StandardCharsets

@WebServlet("/PasswordResetServlet")
public class PasswordResetServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            EmailUtil.initialize(getServletContext());
        } catch (RuntimeException e) {
            System.err.println("Falha ao inicializar EmailUtil no PasswordResetServlet: " + e.getMessage());
            throw new ServletException("Erro ao inicializar o serviço de e-mail.", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
            String action = jsonRequest.get("action").getAsString();

            if ("requestReset".equals(action)) {
                String email = jsonRequest.get("email").getAsString();
                // ALTERADO: Passando o objeto 'request' para handleRequestReset
                handleRequestReset(request, email, jsonResponse);
            } else if ("resetPassword".equals(action)) {
                String token = jsonRequest.get("token").getAsString();
                String newPassword = jsonRequest.get("newPassword").getAsString();
                String confirmPassword = jsonRequest.get("confirmPassword").getAsString();
                handleResetPassword(token, newPassword, confirmPassword, jsonResponse);
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Ação inválida.");
            }

        } catch (SQLException e) {
            System.err.println("Erro de SQL no PasswordResetServlet: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Erro interno do servidor: Erro no banco de dados.");
        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Erro ao enviar e-mail. Verifique as configurações do servidor de e-mail.");
        } catch (Exception e) {
            System.err.println("Erro inesperado no PasswordResetServlet: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Erro interno do servidor. Tente novamente mais tarde.");
        } finally {
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    // ALTERADO: Assinatura do método para receber HttpServletRequest
    private void handleRequestReset(HttpServletRequest request, String email, JsonObject jsonResponse) throws SQLException, MessagingException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.buscarUsuarioPorEmail(email);

        if (usuario == null) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "E-mail não encontrado.");
            return;
        }

        String token = TokenGenerator.generateToken();
        LocalDateTime expiraEm = LocalDateTime.now().plusHours(1);

        usuarioDAO.salvarTokenRedefinicao(usuario.getId(), token, expiraEm);

        // CORREÇÃO AQUI: Construindo o link dinamicamente para o Servlet
        String resetLink = request.getScheme() + "://" + // http ou https
                           request.getServerName() +   // localhost, 192.168.0.90, www.seusite.com
                           (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) + // Porta, se não for padrão
                           request.getContextPath() +     // /CadastroWeb
                           "/PasswordResetServlet?action=showResetPage&token=" + token; // Apontando para o Servlet

        String subject = "Redefinição de Senha para CadastroWeb";
        String body = "Olá " + usuario.getUsername() + ",\n\n"
                    + "Você solicitou uma redefinição de senha. Por favor, clique no link abaixo para redefinir sua senha:\n"
                    + resetLink + "\n\n" // Usando o link dinâmico completo
                    + "Este link é válido por 1 hora.\n"
                    + "Se você não solicitou esta redefinição, por favor, ignore este e-mail.\n\n"
                    + "Atenciosamente,\nEquipe CadastroWeb";

        EmailUtil.sendEmail(email, subject, body);

        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Link de redefinição enviado para o seu e-mail.");
    }

    private void handleResetPassword(String token, String newPassword, String confirmPassword, JsonObject jsonResponse) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        // Assumindo que Usuario.PasswordResetToken é uma classe interna ou um tipo importado corretamente
        Usuario.PasswordResetToken resetToken = usuarioDAO.buscarTokenRedefinicao(token);

        // VERIFICAÇÃO DE SEGURANÇA APRIMORADA
        if (resetToken == null || resetToken.isUsado() || resetToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Token inválido, expirado ou já utilizado.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "As senhas não coincidem.");
            return;
        }
        
        if (newPassword.length() < 6) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "A nova senha deve ter pelo menos 6 caracteres.");
            return;
        }

        // Atualiza a senha do usuário
        usuarioDAO.atualizarSenhaUsuario(resetToken.getIdUsuario(), newPassword);
        
        // NOVO: Marca o token como usado imediatamente após a redefinição de senha
        usuarioDAO.marcarTokenComoUsado(token);

        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Senha redefinida com sucesso!");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // Define APP_CONTEXT_PATH para o JSP, pois ele é usado no body data-attribute e links
        request.setAttribute("APP_CONTEXT_PATH", request.getContextPath());

        // Se a ação for para mostrar a página de redefinição de senha
        if ("showResetPage".equals(action)) {
            String token = request.getParameter("token");
            
            // Lógica de validação do token e redirecionamento no SERVELET
            if (token == null || token.isEmpty()) {
                String encodedMessage = URLEncoder.encode("Token de redefinição de senha inválido ou ausente.", StandardCharsets.UTF_8.name());
                response.sendRedirect(request.getContextPath() + "/LoginServlet?message=error&custom_message=" + encodedMessage);
                return; // Importante: termina o processamento aqui
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            // Assumindo que Usuario.PasswordResetToken é uma classe interna ou um tipo importado corretamente
            Usuario.PasswordResetToken storedToken = null;
            try {
                storedToken = usuarioDAO.buscarTokenRedefinicao(token);
            } catch (SQLException e) {
                System.err.println("Erro SQL ao buscar token no doGet: " + e.getMessage());
                String encodedMessage = URLEncoder.encode("Erro ao verificar token de redefinição. Tente novamente.", StandardCharsets.UTF_8.name());
                response.sendRedirect(request.getContextPath() + "/LoginServlet?message=error&custom_message=" + encodedMessage);
                return;
            }

            if (storedToken == null || storedToken.isUsado() || storedToken.getExpiraEm().isBefore(LocalDateTime.now())) { // Usando a lógica do seu código
                String errorMessage = "Token de redefinição inválido, expirado ou já utilizado. Por favor, solicite uma nova redefinição.";
                if (storedToken != null && !storedToken.isUsado()) {
                    try {
                        usuarioDAO.marcarTokenComoUsado(token);
                    } catch (SQLException e) {
                        System.err.println("Erro SQL ao marcar token como usado no doGet: " + e.getMessage());
                    }
                }
                String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.name());
                response.sendRedirect(request.getContextPath() + "/LoginServlet?message=error&custom_message=" + encodedMessage);
                return;
            }

            // Se o token é válido, define o atributo e encaminha para a página JSP
            request.setAttribute("token", token); // O JSP irá ler este atributo
            request.setAttribute("title", "Redefinir Senha"); // Define o título aqui
            request.getRequestDispatcher("/resetPassword.jsp").forward(request, response);
        } else {
            // Se a ação não for "showResetPage" (ou se não houver ação), encaminha para forgotPassword.jsp
            request.setAttribute("title", "Esqueci Minha Senha"); // Define o título para forgotPassword.jsp
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
        }
    }
}

