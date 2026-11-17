package controller;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder; 
import util.LocalDateAdapter; 
import util.LocalTimeAdapter; 
import dao.FreteDAO;
import dao.TransportadoraDAO;
import model.Frete;
import model.Transportadora;
import model.Usuario; 
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate; 
import java.time.LocalTime; 
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 

@WebServlet("/ListarFretesServlet")
public class ListarFretesServlet extends HttpServlet {
    
    private final FreteDAO freteDAO = new FreteDAO();
    private final TransportadoraDAO transportadoraDAO = new TransportadoraDAO();
    private static final String LANCAR_FRETE_PAGE = "/WEB-INF/jsp/lancar-frete.jsp"; 
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();
    
    private static class FreteListResponse {
        List<Frete> fretes;
        int totalRegistros;
        
        public FreteListResponse(List<Frete> fretes, int totalRegistros) {
            this.fretes = fretes;
            this.totalRegistros = totalRegistros;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/ListarTransportadorasServlet".equals(path)) {
            processarAutocomplete(request, response);
            return; 
        }

        // --- CARREGAMENTO INICIAL DA TELA ---
        final int REGISTROS_POR_PAGINA_DEFAULT = 30;
        final int PAGINA_INICIAL = 1;
        final LocalDate DATA_HOJE = LocalDate.now();

        // BUSCANDO A UNIDADE ATIVA DO USUÁRIO (Fallback para 7 - Matriz se nulo)
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        int idUnidade = (usuarioLogado != null && usuarioLogado.getUnidadeAtivaId() != null) ? usuarioLogado.getUnidadeAtivaId() : 7;

        try {
            int offset = (PAGINA_INICIAL - 1) * REGISTROS_POR_PAGINA_DEFAULT;
            String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy") : "data";
            String orderDir = request.getParameter("orderDir") != null ? request.getParameter("orderDir") : "DESC";

            List<Frete> fretesIniciais = freteDAO.listarFretesDoDiaPaginado(DATA_HOJE, REGISTROS_POR_PAGINA_DEFAULT, offset, orderBy, orderDir, idUnidade);
            int total = freteDAO.contarTotalFretesDoDia(DATA_HOJE, idUnidade);
            
            request.setAttribute("fretesIniciaisJsonInput", gson.toJson(fretesIniciais));
            request.setAttribute("totalRegistrosJSP", total);
            request.setAttribute("paginaAtualJSP", PAGINA_INICIAL);
            request.setAttribute("registrosPorPaginaJSP", REGISTROS_POR_PAGINA_DEFAULT);
            
            request.getRequestDispatcher(LANCAR_FRETE_PAGE).forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/MenuServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        int idUnidade = (usuarioLogado != null && usuarioLogado.getUnidadeAtivaId() != null) ? usuarioLogado.getUnidadeAtivaId() : 7;
        
        String action = request.getParameter("action");
        String termo = request.getParameter("termo") != null ? request.getParameter("termo").trim() : "";
        
        try {
            int pagina = Integer.parseInt(request.getParameter("pagina") != null ? request.getParameter("pagina") : "1");
            int registrosPorPagina = Integer.parseInt(request.getParameter("registrosPorPagina") != null ? request.getParameter("registrosPorPagina") : "30");
            int offset = (pagina - 1) * registrosPorPagina;

            String orderBy = request.getParameter("orderBy"); 
            String orderDir = request.getParameter("orderDir");
            
            List<Frete> fretes = null;
            int totalRegistros = 0;
            LocalDate dataHoje = LocalDate.now();
            
            // Se for busca geral e o termo não estiver vazio
            if ("buscaGeral".equals(action) && !termo.isEmpty()) {
                fretes = freteDAO.listarPorTermoPaginado(termo, registrosPorPagina, offset, orderBy, orderDir, idUnidade);
                totalRegistros = freteDAO.contarTotalFretesPorTermo(termo, idUnidade); 
            } else {
                // Caso contrário, traz sempre os fretes do dia da unidade
                fretes = freteDAO.listarFretesDoDiaPaginado(dataHoje, registrosPorPagina, offset, orderBy, orderDir, idUnidade);
                totalRegistros = freteDAO.contarTotalFretesDoDia(dataHoje, idUnidade);
            }
            
            response.getWriter().write(gson.toJson(new FreteListResponse(fretes, totalRegistros)));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            response.getWriter().write(gson.toJson(new FreteListResponse(null, 0)));
        }
    }

    private void processarAutocomplete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String termo = request.getParameter("termo");
        
        try {
            if (termo != null && !termo.trim().isEmpty()) {
                List<Transportadora> lista = transportadoraDAO.listarItens(null, 0, 50, termo.trim(), "nome", "ASC");
                response.getWriter().write(gson.toJson(lista));
            } else {
                response.getWriter().write("[]");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("[]");
        }
    }
}