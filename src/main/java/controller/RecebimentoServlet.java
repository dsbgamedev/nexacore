package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Usuario;
import java.io.IOException;

@WebServlet(urlPatterns = {"/RecebimentoServlet", "/DevolucaoServlet"})
public class RecebimentoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuario == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão expirada.");
            return;
        }

        boolean isAdmin = "SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil());
        
        // 1. Validação de Permissão de Módulo
        boolean temPermissaoModulo = false;
        if (usuario.getModulosPermitidos() != null) {
            for (String m : usuario.getModulosPermitidos()) {
                if (m.equalsIgnoreCase("movimentacao") || 
                    m.equalsIgnoreCase("movimentacaoes") || 
                    m.equalsIgnoreCase("movimentacao_recebimento")) {
                    temPermissaoModulo = true;
                    break;
                }
            }
        }

        if (!isAdmin && !temPermissaoModulo) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado: Você não possui permissão para este módulo.");
            return;
        }

        // 2. Validação de Filial Ativa (Opcional por página, caso queira garantir que o usuário 
        // só abra a tela de recebimentos se estiver com uma filial válida selecionada no menu superior)
        Integer filialAtivaId = usuario.getUnidadeAtivaId();
        if (!isAdmin && filialAtivaId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado: Nenhuma filial ativa selecionada no menu superior.");
            return;
        }

        // Se passou por todas as barreiras, encaminha para a tela JSP de recebimento/devolução
        request.getRequestDispatcher("/WEB-INF/jsp/recebimento-equipamento.jsp").forward(request, response);
    }
}