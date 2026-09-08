package controller;

import java.io.IOException;
import java.util.List;

import dao.EquipamentoDAO;
import dao.ManutencaoDAO;
import dao.MovimentacaoEnvioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ManutencaoChamado;
import model.MovimentacaoEnvio;
import model.Usuario;

@WebServlet("/MenuServlet")
public class MenuServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        
       //1. Busca as 5 movimentações recentes com status pendentes/em trânsito para o dashboard
        try {
            MovimentacaoEnvioDAO dao = new MovimentacaoEnvioDAO();
            List<MovimentacaoEnvio> listaRecentes = dao.listarRecentesPendentes(5);
            request.setAttribute("listaMovimentacoesRecentes", listaRecentes);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboard", "Não foi possível carregar as movimentações recentes.");
        }
        
       // 2. Busca os chamados abertos recentes para o dashboard
        try {
            ManutencaoDAO manutencaoDao = new ManutencaoDAO();
            List<ManutencaoChamado> listaChamadosRecentes = manutencaoDao.listarRecentesAbertos(5);
            request.setAttribute("listaChamadosRecentes", listaChamadosRecentes);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboardManutencao", "Não foi possível carregar os chamados recentes.");
        }
        
     // 3. Busca as contagens de equipamentos considerando a regra da Filial Atual
        try {
            EquipamentoDAO eqDao = new EquipamentoDAO();
            
            // Pega o ID da unidade ativa diretamente do objeto usuário logado na sessão
            Integer filialAtualId = 161; // Valor padrão de segurança (Matriz)
            
            if (usuario != null && usuario.getUnidadeAtivaId() != null && usuario.getUnidadeAtivaId() > 0) {
                filialAtualId = usuario.getUnidadeAtivaId();
            }
            
            // Passa o ID correto para os métodos realizarem o filtro inteligente
            request.setAttribute("totalEquipamentos", eqDao.contarTotalEquipamentos(filialAtualId));
            request.setAttribute("totalAtivos", eqDao.contarEquipamentosAtivos(filialAtualId));
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("totalEquipamentos", 0);
            request.setAttribute("totalAtivos", 0);
        }
        // Encaminha de forma segura para o menu.jsp protegendo o layout
        request.getRequestDispatcher("WEB-INF/jsp/menu.jsp").forward(request, response);
    }
        
        

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
