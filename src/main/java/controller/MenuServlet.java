package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.EquipamentoDAO;
import dao.FilialDAO;
import dao.ManutencaoDAO;
import dao.MovimentacaoEnvioDAO;
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
        
        // 1. Declaração global da filial atual com fallback seguro para a Matriz (161)
        Integer filialAtualId = 161; 
        if (usuario.getUnidadeAtivaId() != null && usuario.getUnidadeAtivaId() > 0) {
            try {
                FilialDAO filialDao = new FilialDAO();
                Integer origemCodigoMapeado = filialDao.buscarOrigemCodigoPorId(usuario.getUnidadeAtivaId());
                if (origemCodigoMapeado != null) {
                    filialAtualId = origemCodigoMapeado;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 2. Busca as 5 movimentações recentes com status pendentes/em trânsito para o dashboard
        try {
            MovimentacaoEnvioDAO dao = new MovimentacaoEnvioDAO();
            List<MovimentacaoEnvio> listaRecentes = dao.listarRecentesPendentes(5);
            request.setAttribute("listaMovimentacoesRecentes", listaRecentes);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboard", "Não foi possível carregar as movimentações recentes.");
        }
        
        // 3. Busca os chamados abertos recentes para o dashboard
        try {
            ManutencaoDAO manutencaoDao = new ManutencaoDAO();
            List<ManutencaoChamado> listaChamadosRecentes = manutencaoDao.listarRecentesAbertos(5);
            request.setAttribute("listaChamadosRecentes", listaChamadosRecentes);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erroDashboardManutencao", "Não foi possível carregar os chamados recentes.");
        }
        
        // 4. Busca os equipamentos com status "Em Manutenção" utilizando a regra da Filial Atual
        try {
            EquipamentoDAO eqDao = new EquipamentoDAO();
            System.out.println("=== [DEBUG] INICIO BUSCA MANUTENCAO ===");
            System.out.println("Filial Atual ID enviado ao DAO: " + filialAtualId);
            
            // Alterado de "Em Manutenção" para o novo status de intenção/alerta
            List<Equipamento> listaEquipamentosEmManutencao = eqDao.listarPorStatusEUnidade(filialAtualId, "Encaminhado p/ Chamado");
            
            System.out.println("Tamanho da lista retornado pelo DAO: " + (listaEquipamentosEmManutencao != null ? listaEquipamentosEmManutencao.size() : "NULL"));
            if (listaEquipamentosEmManutencao != null) {
                for (Equipamento eq : listaEquipamentosEmManutencao) {
                    System.out.println(" -> Encontrado ID: " + eq.getIdEquipamento() + " | Nome: " + eq.getNomeProduto() + " | Status: " + eq.getStatusNome());
                }
            }
            System.out.println("=== [DEBUG] FIM BUSCA MANUTENCAO ===");
            
            request.setAttribute("listaEquipamentosEmManutencao", listaEquipamentosEmManutencao);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("listaEquipamentosEmManutencao", new ArrayList<>());
        }
        
        // 5. Busca as contagens de equipamentos considerando a regra da Filial Atual
        try {
            EquipamentoDAO eqDao = new EquipamentoDAO();
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