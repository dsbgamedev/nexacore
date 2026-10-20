package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import util.SessaoListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/AdminSessoesServlet", "/api/sessoes/*"})
public class AdminSessoesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        
        // Se a requisição for para a API de listagem JSON
        if (uri.contains("/api/sessoes/listar")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            List<SessaoInfoDTO> listaUsuariosOnline = new ArrayList<>();
            Map<String, HttpSession> sessoes = SessaoListener.getSessoesAtivas();

            synchronized (sessoes) {
                for (Map.Entry<String, HttpSession> entry : sessoes.entrySet()) {
                    HttpSession session = entry.getValue();
                    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

                    if (usuario != null) {
                        SessaoInfoDTO info = new SessaoInfoDTO();
                        info.setSessionId(session.getId());
                        
                        String nomeExibicao = (usuario.getNomeCompleto() != null && !usuario.getNomeCompleto().trim().isEmpty()) 
                                              ? usuario.getNomeCompleto() 
                                              : usuario.getUsername();
                        
                        info.setNomeUsuario(nomeExibicao);
                        info.setEmailUsuario(usuario.getEmail());
                        info.setDataCriacao(String.valueOf(session.getCreationTime()));
                        info.setUltimoAcesso(String.valueOf(session.getLastAccessedTime()));
                        listaUsuariosOnline.add(info);
                    }
                }
            }

            PrintWriter out = response.getWriter();
            out.write(converterParaJson(listaUsuariosOnline));
            out.flush();
            return;
        }

        // Caso contrário, direciona para a página JSP visual de gerenciamento de sessões
        request.getRequestDispatcher("/WEB-INF/jsp/admin-sessoes.jsp").forward(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String sessionId = request.getParameter("id");
        PrintWriter out = response.getWriter();

        if (sessionId != null && SessaoListener.invalidarSessaoPorId(sessionId)) {
            out.write("{\"sucesso\": true, \"mensagem\": \"Sessão encerrada com sucesso.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"erro\": \"Não foi possível encerrar a sessão ou ela já expirou.\"}");
        }
        out.flush();
    }

    private String converterParaJson(List<SessaoInfoDTO> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            SessaoInfoDTO u = lista.get(i);
            sb.append("{")
              .append("\"sessionId\":\"").append(u.getSessionId()).append("\",")
              .append("\"nomeUsuario\":\"").append(u.getNomeUsuario()).append("\",")
              .append("\"emailUsuario\":\"").append(u.getEmailUsuario() != null ? u.getEmailUsuario() : "").append("\",")
              .append("\"dataCriacao\":\"").append(u.getDataCriacao()).append("\",")
              .append("\"ultimoAcesso\":\"").append(u.getUltimoAcesso()).append("\"")
              .append("}");
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static class SessaoInfoDTO {
        private String sessionId;
        private String nomeUsuario;
        private String emailUsuario;
        private String dataCriacao;
        private String ultimoAcesso;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getNomeUsuario() { return nomeUsuario; }
        public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
        public String getEmailUsuario() { return emailUsuario; }
        public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
        public String getDataCriacao() { return dataCriacao; }
        public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }
        public String getUltimoAcesso() { return ultimoAcesso; }
        public void setUltimoAcesso(String ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }
    }
}