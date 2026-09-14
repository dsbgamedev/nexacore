package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import java.io.IOException;

@WebServlet("/DevolucaoEquipamentoServlet")
public class DevolucaoEquipamentoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String idEqParam = req.getParameter("idEquipamento");
        String tipoParam = req.getParameter("tipo");

        if (idEqParam != null && !idEqParam.isEmpty()) {
            session.setAttribute("equipamentoDevolucaoId", idEqParam);
            
            String redirectUrl = req.getContextPath() + "/DevolucaoEquipamentoServlet";
            if ("devolucao".equals(tipoParam)) {
                redirectUrl += "?tipo=devolucao";
            }
            
            resp.sendRedirect(redirectUrl);
            return;
        }
        
        // Verifica se é devolução pela URL ou se manteve na sessão
        if ("devolucao".equals(tipoParam) || "devolucao".equals(session.getAttribute("tipoDevolucaoFlag"))) {
            session.setAttribute("tipoDevolucaoFlag", "devolucao");
            req.setAttribute("isDevolucaoForcada", true);
        }

        // Recupera o ID da sessão e limpa
        String idEquipamento = (String) session.getAttribute("equipamentoDevolucaoId");
        if (idEquipamento != null) {
            session.removeAttribute("equipamentoDevolucaoId");
            req.setAttribute("idEquipamento", idEquipamento); 
        }

        // Define o nome do usuário logado
        String nomeResponsavel = (usuario.getNomeCompleto() != null && !usuario.getNomeCompleto().isEmpty()) 
                                 ? usuario.getNomeCompleto() 
                                 : usuario.getUsername();
        req.setAttribute("nomeUsuarioLogado", nomeResponsavel);

        // Encaminha para a JSP dedicada de devolução
        req.getRequestDispatcher("/WEB-INF/jsp/devolucao-equipamento.jsp").forward(req, resp);
        
        // Limpa a flag da sessão SOMENTE APÓS o forward para garantir que a renderização useu o estado correto
        session.removeAttribute("tipoDevolucaoFlag");
    }
}