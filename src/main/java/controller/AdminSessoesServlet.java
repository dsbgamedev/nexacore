package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import util.SessaoListener;
import dto.SessaoInfoDTO;

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
                        
                        long tempoAtivoMin = (System.currentTimeMillis() - session.getCreationTime()) / (1000 * 60);
                        info.setTempoAtivo(tempoAtivoMin + " min");

                        // Estimativa de consumo de memória da sessão corrigida (* 2)
                        int qtdModulos = (usuario.getModulosPermitidos() != null) ? usuario.getModulosPermitidos().size() : 0;
                        long memoriaEstimadaKB = 4 + (qtdModulos * 2); 
                        info.setConsumoMemoria(memoriaEstimadaKB + " KB");

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
              .append("\"sessionId\":\"").append(escapeJson(u.getSessionId())).append("\",")
              .append("\"nomeUsuario\":\"").append(escapeJson(u.getNomeUsuario())).append("\",")
              .append("\"emailUsuario\":\"").append(escapeJson(u.getEmailUsuario())).append("\",")
              .append("\"dataCriacao\":\"").append(escapeJson(u.getDataCriacao())).append("\",")
              .append("\"ultimoAcesso\":\"").append(escapeJson(u.getUltimoAcesso())).append("\",")
              .append("\"tempoAtivo\":\"").append(escapeJson(u.getTempoAtivo())).append("\",") // <--- Vírgula adicionada aqui
              .append("\"consumoMemoria\":\"").append(escapeJson(u.getConsumoMemoria())).append("\"")
              .append("}");
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}