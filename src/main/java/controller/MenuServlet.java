package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.EquipamentoDAO;
import dao.FilialDAO;
import dao.ManutencaoDAO;
import dao.MovimentacaoEnvioDAO;
import dao.MovimentacaoRecebimentoDAO;
import dto.DashboardManutencaoDTO;
import dto.UnidadeDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

@WebServlet("/MenuServlet")
public class MenuServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Executor pool dedicado para paralelizar as consultas ao banco no Dashboard
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        
        long tempoInicioGeral = System.currentTimeMillis();

        // --- 1. TRADUÇÃO DAS UNIDADES (Otimizada em lote/uma única vez) ---
        long tempoEtapa = System.currentTimeMillis();
        FilialDAO filialDao = new FilialDAO();
        List<Integer> unidadesPermitidas = new ArrayList<>();
        
        try {
            unidadesPermitidas = filialDao.traduzirUnidadesParaCodigoOrigem(usuario.getUnidadesPermitidas());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[PERFORMANCE] Tradução de Filiais: " + (System.currentTimeMillis() - tempoEtapa) + "ms");
        
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
        
        final List<Integer> unidadesFinais = unidadesPermitidas != null ? unidadesPermitidas : new ArrayList<>();

        // --- 3. CARREGAMENTO PARALELO DOS DADOS DO DASHBOARD ---
        CompletableFuture<DashboardData> futureData = CompletableFuture.supplyAsync(() -> {
            DashboardData data = new DashboardData();
            
            try {
                MovimentacaoEnvioDAO movDao = new MovimentacaoEnvioDAO();
                data.listaMovimentacoesRecentes = movDao.listarRecentesPendentes(5);
                data.totalEmTransito = movDao.contarEmTransitoPorUnidades(unidadesFinais);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                MovimentacaoRecebimentoDAO recebimentoDao = new MovimentacaoRecebimentoDAO();
                data.totalRecebidosHoje = recebimentoDao.contarRecebidosHojePorUnidades(unidadesFinais);
                data.totalAguardandoRecebimento = recebimentoDao.contarPendentesRecebimentoPorUnidades(unidadesFinais);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Otimização aplicada: Substituição de 3 chamadas separadas por 1 método unificado em lote
            try {
                ManutencaoDAO manutencaoDao = new ManutencaoDAO();
                DashboardManutencaoDTO manDto = new DashboardManutencaoDTO();
                manutencaoDao.carregarDadosDashboard(unidadesFinais, manDto);
                
                data.listaChamadosRecentes = manDto.listaChamadosRecentes;
                data.totalEmManutencao = manDto.totalEmManutencao;
                data.totalChamadosVencidos = manDto.totalChamadosVencidos;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                EquipamentoDAO eqDao = new EquipamentoDAO();
                data.listaEquipamentosEmManutencao = eqDao.listarPorStatusEUnidades(5, unidadesFinais);
                data.totalEquipamentos = eqDao.contarTotalEquipamentos(unidadesFinais);
                data.totalAtivos = eqDao.contarEquipamentosAtivos(unidadesFinais);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }, executor);

        // Aguarda a conclusão e resgata os dados consolidados
        DashboardData dados = new DashboardData();
        try {
            dados = futureData.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Popula os atributos no request de uma só vez na thread principal
        request.setAttribute("listaMovimentacoesRecentes", dados.listaMovimentacoesRecentes);
        request.setAttribute("totalEmTransito", dados.totalEmTransito);
        request.setAttribute("totalRecebidosHoje", dados.totalRecebidosHoje);
        request.setAttribute("totalAguardandoRecebimento", dados.totalAguardandoRecebimento);
        request.setAttribute("listaChamadosRecentes", dados.listaChamadosRecentes);
        request.setAttribute("totalEmManutencao", dados.totalEmManutencao);
        request.setAttribute("totalChamadosVencidos", dados.totalChamadosVencidos);
        request.setAttribute("listaEquipamentosEmManutencao", dados.listaEquipamentosEmManutencao);
        request.setAttribute("totalEquipamentos", dados.totalEquipamentos);
        request.setAttribute("totalAtivos", dados.totalAtivos);

        System.out.println("[PERFORMANCE] TEMPO TOTAL GERAL DO SERVLET (OTIMIZADO): " + (System.currentTimeMillis() - tempoInicioGeral) + "ms");
        
        // Encaminha para a View
        request.getRequestDispatcher("WEB-INF/jsp/menu.jsp").forward(request, response);
    }
        
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    // Classe auxiliar interna para carregar os dados de forma segura sem travar o request do Servlet
    private static class DashboardData {
        List<?> listaMovimentacoesRecentes = new ArrayList<>();
        int totalEmTransito = 0;
        int totalRecebidosHoje = 0;
        int totalAguardandoRecebimento = 0;
        List<?> listaChamadosRecentes = new ArrayList<>();
        int totalEmManutencao = 0;
        int totalChamadosVencidos = 0;
        List<?> listaEquipamentosEmManutencao = new ArrayList<>();
        int totalEquipamentos = 0;
        int totalAtivos = 0;
    }
}