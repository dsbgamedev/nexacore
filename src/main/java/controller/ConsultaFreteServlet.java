package controller;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken; 
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Collections;
import java.sql.SQLException; 
import java.lang.Math; 

import dao.ConsultaFreteDAO;
import dao.TransportadoraDAO;
import dto.FreteConsultaFiltro;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Frete;
import model.Transportadora;
import model.Usuario;
import model.enums.PerfilUsuario; 
import util.LocalDateAdapter;
import util.LocalTimeAdapter;

/**
 * Servlet responsável por processar a consulta de fretes com base em filtros.
 * Mapeado para a URL "/ConsultaFreteServlet".
 */
@WebServlet("/ConsultaFreteServlet")
public class ConsultaFreteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ConsultaFreteDAO freteDAO = new ConsultaFreteDAO();
    private final TransportadoraDAO transportadoraDAO = new TransportadoraDAO();
    
    private Gson gson; 
    
    private static final String CADASTRO_PAGE = "/WEB-INF/jsp/consulta-frete.jsp"; 
    private static final String REQUIRED_MODULE = "frete"; 

    @Override
    public void init() throws ServletException {
        super.init();
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeAdapter());
        
        this.gson = gsonBuilder.setPrettyPrinting().create();
    }
    
    // =========================================================
    // Métodos de Segurança (Mantidos)
    // =========================================================
    
    private boolean checkPermission(Usuario usuario) {
        if (usuario == null) return false;
        String perfil = usuario.getPerfil();
        List<String> modulosPermitidos = usuario.getModulosPermitidos();
        if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil) ||
            PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(perfil)) {
            return true;
        } 
        return modulosPermitidos != null && modulosPermitidos.stream()
                .anyMatch(m -> m.equalsIgnoreCase(REQUIRED_MODULE));
    }
    
    private void handleAccessDeniedGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String errorMessage = "Acesso negado. Você não tem permissão para o módulo de Consulta de Fretes.";
        HttpSession session = request.getSession(false);
        if (session != null) {
             session.setAttribute("toastMessage", errorMessage);
        }
        response.sendRedirect(request.getContextPath() + "/MenuServlet");
    }
    
    private void handleAccessDeniedAjax(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");
        Gson localGson = (this.gson != null) ? this.gson : new Gson(); 
        response.getWriter().write(localGson.toJson(Map.of("erro", "Acesso negado. Você não tem permissão para realizar esta operação.")));
        response.getWriter().flush();
    }


    // =========================================================
    // doGet (Carregamento Inicial da Página)
    // =========================================================
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	try {
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (!checkPermission(usuarioLogado)) {
             handleAccessDeniedGet(request, response);
             return;
        }
        
        //System.out.println("--- DEBUG INÍCIO: doGet (Carregamento inicial da página) ---");
        
        try {
        	List<Transportadora> transportadoras = transportadoraDAO.listar();
            request.setAttribute("transportadoras", transportadoras);
        } catch (Exception daoEx) {
            System.err.println("ERRO: Falha ao buscar transportadoras no doGet: " + daoEx.getMessage());
        }

        request.setAttribute("listaFretes", new ArrayList<Frete>());
        request.setAttribute("filtroAtual", new FreteConsultaFiltro()); 
        
        System.out.println("--- DEBUG FIM: doGet ---");
        
        request.getRequestDispatcher(CADASTRO_PAGE).forward(request, response);
        //RequestDispatcher dispatcher = request.getRequestDispatcher("/consulta-frete.jsp");
        //dispatcher.forward(request, response);
    	} catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/MenuServlet");
        }
    }
    
    // =========================================================
    // doPost (Consulta e Exportação)
    // =========================================================
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        //System.out.println("--- DEBUG INÍCIO: doPost ---");
        
        // --- Delegação para Exportação (CORRIGIDA PARA REPASSAR FILTROS) ---
        String exportType = request.getParameter("exportType");
        String dataType = request.getParameter("dataType"); 

        if (exportType != null && !exportType.trim().isEmpty() && "fretes".equals(dataType)) {
            
            System.out.println("DEBUG (Servlet): Detectada requisição de EXPORTAÇÃO (" + exportType + "). Fazendo forward para ExportServlet.");

            // 1. EXTRAIR OS PARÂMETROS DE FILTRO DA REQUISIÇÃO ATUAL (POST)
            String dataInicio = request.getParameter("dataInicio");
            String dataFim = request.getParameter("dataFim");
            String idTransportadoraStr = request.getParameter("transportadoraId"); 
            String termoPesquisa = request.getParameter("termoPesquisa");
            
            // 2. REPASSAR O exportType (Formato) E O dataType
            // Adicionar como atributo para o ExportServlet ler via getParameter() de forma confiável
            request.setAttribute("exportType", exportType);
            request.setAttribute("dataType", dataType);

            // 3. REPASSAR OS FILTROS CRÍTICOS COMO ATRIBUTOS 
            if (dataInicio != null) request.setAttribute("dataInicio", dataInicio);
            if (dataFim != null) request.setAttribute("dataFim", dataFim);
            if (idTransportadoraStr != null) request.setAttribute("transportadoraId", idTransportadoraStr);
            if (termoPesquisa != null) request.setAttribute("termoPesquisa", termoPesquisa);
            
            // 4. Delegar para o ExportServlet
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ExportServlet");
            dispatcher.forward(request, response);
            return;
        }

        // --- LÓGICA DE CONSULTA NORMAL (Retorno JSON) ---
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        if (!checkPermission(usuarioLogado)) {
             handleAccessDeniedAjax(response);
             return;
        }

        // --- Variáveis de Paginação e Ordenação ---
        int paginaAtual = 1;
        int limite = 50; 

        try {
            // Lendo parâmetros de Paginação
            String paginaStr = request.getParameter("pagina");
            String limiteStr = request.getParameter("limite");
            
            if (paginaStr != null) paginaAtual = Integer.parseInt(paginaStr);
            if (limiteStr != null) limite = Integer.parseInt(limiteStr);
            
            if (paginaAtual < 1) paginaAtual = 1;
            if (limite < 1) limite = 50; 
            
            int offset = (paginaAtual - 1) * limite;
            
            // Lendo parâmetros de Ordenação
            String sortColumn = request.getParameter("colunaOrdenacao");
            String sortDirection = request.getParameter("direcaoOrdenacao");
            
            if (sortColumn == null || sortColumn.isEmpty()) sortColumn = "dataLancamento";
            if (sortDirection == null || sortDirection.isEmpty()) sortDirection = "desc";

            // --- Lendo e Preenchendo o DTO (FreteConsultaFiltro) ---
            
            String dataInicio = request.getParameter("dataInicio");
            String dataFim = request.getParameter("dataFim");
            String idTransportadoraStr = request.getParameter("transportadoraId"); 
            String termoPesquisa = request.getParameter("termoPesquisa");
            
         // ADICIONE ESTA LINHA AQUI:
            String campoPesquisa = request.getParameter("campoPesquisa");
            
            FreteConsultaFiltro filtro = new FreteConsultaFiltro();
            
            // --- ADICIONE ESTA LINHA: Captura a filial ativa do menu ---
            filtro.setIdFilial(usuarioLogado.getUnidadeAtivaId());
            
            if (dataInicio != null && !dataInicio.trim().isEmpty()) filtro.setDataInicio(dataInicio);
            if (dataFim != null && !dataFim.trim().isEmpty()) filtro.setDataFim(dataFim);
            
            if (idTransportadoraStr != null && !idTransportadoraStr.trim().isEmpty() && !"0".equals(idTransportadoraStr)) {
                filtro.setIdTransportadora(Integer.parseInt(idTransportadoraStr));
            }
            
            if (termoPesquisa != null && !termoPesquisa.trim().isEmpty()) filtro.setTermoPesquisa(termoPesquisa);
            
            // ADICIONE ESTA LINHA TAMBÉM:
            filtro.setCampoPesquisa(campoPesquisa);
            filtro.setSortColumn(sortColumn);
            filtro.setSortDirection(sortDirection);
            
            System.out.println("DEBUG (Servlet) - Filtro aplicado: " + filtro + ", Página: " + paginaAtual + ", Limite: " + limite);

            // 5. CHAMADA DO DAO (consulta paginada)
            Map<String, Object> resultadoConsulta = freteDAO.consultarComPaginacaoEAgregados(filtro, offset, limite);

            // 6. Extrai e Formata a Resposta
            List<Frete> fretes = (List<Frete>) resultadoConsulta.get("fretes");
            int totalRegistros = (int) resultadoConsulta.getOrDefault("totalRegistros", 0);

            // Extrai Totais Agregados
            double totalVolume = (double) resultadoConsulta.getOrDefault("totalVolume", 0.0);
            double totalPesoLiquido = (double) resultadoConsulta.getOrDefault("totalPesoLiquido", 0.0);
            double totalValorNf = (double) resultadoConsulta.getOrDefault("totalValorNf", 0.0);
            double totalValorFrete = (double) resultadoConsulta.getOrDefault("totalValorFrete", 0.0);
            
            // 7. Calcula o Total de Páginas
            int totalPaginas = (int) Math.ceil((double) totalRegistros / limite);
            if (totalRegistros == 0) totalPaginas = 0;

            // 8. Monta o Objeto de Resposta JSON
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fretes", fretes);
            responseData.put("totalPaginas", totalPaginas);
            responseData.put("paginaAtual", paginaAtual);
            responseData.put("limite", limite);
            responseData.put("totalVolume", totalVolume);
            responseData.put("totalPesoLiquido", totalPesoLiquido);
            responseData.put("totalValorNf", totalValorNf);
            responseData.put("totalValorFrete", totalValorFrete);

            // 9. Serializa e Envia
            out.print(gson.toJson(responseData));
            out.flush();

        } catch (NumberFormatException e) {
            System.err.println("ERRO (Servlet): Falha na conversão de ID/Paginação/Limite: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Erro no formato dos dados de filtro. Verifique os campos numéricos.")));
        } catch (SQLException e) {
             System.err.println("ERRO SQL (Servlet): Falha na consulta ao banco de dados: " + e.getMessage());
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             out.print(gson.toJson(Map.of("error", "Erro ao acessar o banco de dados: " + e.getMessage())));
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO (Servlet) - Ocorreu um erro interno na consulta: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Ocorreu um erro interno durante a consulta: " + e.getMessage())));
        } finally {
            if (out != null) out.close();
        }
        
        System.out.println("--- DEBUG FIM: doPost ---");
    }
}