package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.EquipamentoDAO;
import dao.FilialDAO;
import dao.ManutencaoDAO;
import dao.MovimentacaoEnvioDAO;
import dto.UnidadeDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Equipamento;
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
        
        // --- 1. TRADUÇÃO DAS UNIDADES USANDO O FILIALDAO (MVC Respeitado) ---
        FilialDAO filialDao = new FilialDAO();
        List<Integer> unidadesPermitidas = new ArrayList<>();
        
        try {
            unidadesPermitidas = filialDao.traduzirUnidadesParaCodigoOrigem(usuario.getUnidadesPermitidas());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("=== [DEBUG] Unidades Permitidas Convertidas para Origem Código: " + unidadesPermitidas);
        
        // --- 2. GARANTIR UNIDADE ATIVA PADRÃO NA SESSÃO ---
        if (usuario.getUnidadeAtivaId() == null && unidadesPermitidas != null && !unidadesPermitidas.isEmpty()) {
            int primeiraFilial = unidadesPermitidas.get(0);
            usuario.setUnidadeAtivaId(primeiraFilial);
            
            if (usuario.getUnidadesPermitidasObjetos() != null) {
                for (UnidadeDTO u : usuario.getUnidadesPermitidasObjetos()) {
                    if (u.getId() == primeiraFilial) {
                        usuario.setUnidadeAtivaNome(u.getNome());
                        break;
                    }
                }
            }
            session.setAttribute("usuarioLogado", usuario);
        }
        
        // --- 3. CARREGAMENTO DOS DADOS DO DASHBOARD ---
        try {
            MovimentacaoEnvioDAO movDao = new MovimentacaoEnvioDAO();
            request.setAttribute("listaMovimentacoesRecentes", movDao.listarRecentesPendentes(5));
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboard", "Não foi possível carregar as movimentações recentes.");
        }
        
        try {
            ManutencaoDAO manutencaoDao = new ManutencaoDAO();
            request.setAttribute("listaChamadosRecentes", manutencaoDao.listarRecentesAbertos(5));
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboardManutencao", "Não foi possível carregar os chamados recentes.");
        }
        
        try {
            EquipamentoDAO eqDao = new EquipamentoDAO();
            request.setAttribute("listaEquipamentosEmManutencao", eqDao.listarPorStatusEUnidades(5, unidadesPermitidas));
            request.setAttribute("totalEquipamentos", eqDao.contarTotalEquipamentos(unidadesPermitidas));
            request.setAttribute("totalAtivos", eqDao.contarEquipamentosAtivos(unidadesPermitidas));
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("listaEquipamentosEmManutencao", new ArrayList<>());
            request.setAttribute("totalEquipamentos", 0);
            request.setAttribute("totalAtivos", 0);
        }
        
        // Encaminha para a View
        request.getRequestDispatcher("WEB-INF/jsp/menu.jsp").forward(request, response);
    }
        
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}