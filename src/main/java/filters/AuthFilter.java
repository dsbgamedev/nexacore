package filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.Usuario;
import model.enums.PerfilUsuario;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // Lista de JSPs que são acessíveis diretamente (sem a necessidade de um Servlet intermediário)
    private static final List<String> PUBLIC_PAGES = Arrays.asList(
        "/index.jsp",
        "/menu.jsp" // Acessível para usuários logados após login
    );
    
    // Lista de JSPs que DEVEM ser explicitamente bloqueados para acesso direto
    // Acesso a estes JSPs deve ser feito via Servlet que lida com a lógica e permissões.
    private static final List<String> PROTECTED_JSP_PAGES = Arrays.asList(
    		"/WEB-INF/jsp/cadastro-empresa.jsp",
            "/WEB-INF/jsp/cadastro-equipamento.jsp",
            "/WEB-INF/jsp/cadastro-produto.jsp",
            "/WEB-INF/jsp/cadastro-usuario.jsp",
            "/WEB-INF/jsp/consulta-chamado.jsp",
            "/WEB-INF/jsp/consulta-envios.jsp",
            "/WEB-INF/jsp/consulta-equipamento.jsp",
            "/WEB-INF/jsp/consulta-produto.jsp",
            "/WEB-INF/jsp/envio-equipamento.jsp",
            "/WEB-INF/jsp/fabricantes.jsp",
            "/WEB-INF/jsp/gerarEtiquetaVisual.jsp",
            "/WEB-INF/jsp/gerenciar-atributos.jsp",
            "/WEB-INF/jsp/gerenciar-usuarios.jsp",
            "/WEB-INF/jsp/manutencao-abertura.jsp",
            "/WEB-INF/jsp/marcas.jsp",
            "/WEB-INF/jsp/recebimento-equipamento.jsp"
    	     // Protegendo o acesso direto		
    );

    // Lista de Servlets públicos (ex: Login, Reset de Senha, Logout)
    private static final List<String> PUBLIC_SERVLETS = Arrays.asList(
        "/LoginServlet",
        "/PasswordResetServlet",
        "/LogoutServlet" // Logout é sempre público, permite sair
    );

    // Lista de recursos públicos (CSS, JS, imagens, etc.)
    private static final List<String> PUBLIC_RESOURCES_AND_INCLUDES = Arrays.asList(
		"/assets/",  // <--- ADICIONADO AQUI! Libera toda a pasta de CSS, JS e imagens modernas
        "/css/",
        "/js/",
        "/images/",
        "/fonts/",
        "/jsp/" // Inclui JSPs que são apenas includes (ex: head.jsp, modal.jsp)
    );
    
    // Mapeamento de recursos protegidos para os módulos de permissão
    private static final Map<String, String> PROTECTED_RESOURCES_MODULES = new HashMap<>();
    static {
    	
    	// 1. empresas
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/cadastro-empresa.jsp", "filiais");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarEmpresaServlet", "filiais");
        PROTECTED_RESOURCES_MODULES.put("/api/filiais", "filiais");
        PROTECTED_RESOURCES_MODULES.put("/api/filiais/", "filiais");
        
        // 2. equipamentos
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/cadastro-equipamento.jsp/", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/consulta-equipamento.jsp", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/EquipamentosServlet", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarEquipamentoServlet", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/ConsultaEquipamentosServlet", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/api/equipamentos", "equipamentos");
        PROTECTED_RESOURCES_MODULES.put("/api/equipamentos/", "equipamentos");
            
        // 3. produtos
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/cadastro-produto.jsp", "produtos");
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/consulta-produto.jsp", "produtos");
        PROTECTED_RESOURCES_MODULES.put("/ProdutoServlet", "produtos");
        PROTECTED_RESOURCES_MODULES.put("/ConsultaProdutoServlet", "produtos"); // <--- Adicionado
        PROTECTED_RESOURCES_MODULES.put("/api/produtos", "produtos");
        PROTECTED_RESOURCES_MODULES.put("/api/produtos/", "produtos");
              
        // 4. usuarios
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/cadastro-usuario.jsp", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/gerenciar-usuarios.jsp", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/GerenciarUsuariosServlet", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarUsuarioServlet", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/api/usuarios", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/api/usuarios/", "usuarios");
        
        // 5. manutencao_chamados
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/consulta-chamados.jsp", "manutencao_chamados");
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/manutencao-abertura.jsp", "manutencao_chamados");
        PROTECTED_RESOURCES_MODULES.put("/ManutencaoServlet", "manutencao_chamados");
        PROTECTED_RESOURCES_MODULES.put("/api/manutencao_chamados", "manutencao_chamados");
        PROTECTED_RESOURCES_MODULES.put("/api/manutencao_chamados/", "manutencao_chamados");
        
        // 6. movimentacao_envio
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/consulta-envios.jsp", "movimentacao_envio");
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/envio-equipamento.jsp", "movimentacao_envio");
        PROTECTED_RESOURCES_MODULES.put("/EnvioEquipamentoServlet", "movimentacao_envio");
        PROTECTED_RESOURCES_MODULES.put("/ConsultaEnvioServlet", "movimentacao_envio"); 
        PROTECTED_RESOURCES_MODULES.put("/api/envios", "movimentacao_envio");
        PROTECTED_RESOURCES_MODULES.put("/api/envios/", "movimentacao_envio");
      
        // 7. fabricantes
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/fabricantes.jsp", "fabricantes");
        PROTECTED_RESOURCES_MODULES.put("/FabricanteServlet", "fabricantes");  
        PROTECTED_RESOURCES_MODULES.put("/api/fabricantes", "fabricantes"); 
        PROTECTED_RESOURCES_MODULES.put("/api/fabricantes/", "fabricantes");

        // 8. atributos (Duplicidade removida)
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/gerenciar-atributos.jsp", "atributos");
        PROTECTED_RESOURCES_MODULES.put("/Atributos", "atributos");
        PROTECTED_RESOURCES_MODULES.put("/AtributoServlet", "atributos");
        PROTECTED_RESOURCES_MODULES.put("/api/atributos", "atributos"); 
        PROTECTED_RESOURCES_MODULES.put("/api/atributos/", "atributos"); 
        
        // 9. marcas
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/marcas.jsp", "marcas");
        PROTECTED_RESOURCES_MODULES.put("/MarcaServlet", "marcas");
        PROTECTED_RESOURCES_MODULES.put("/api/marcas", "marcas"); 
        PROTECTED_RESOURCES_MODULES.put("/api/marcas/", "marcas"); 
        
        // 10. movimentacao_recebimento (Com as rotas reais de API chamadas pelo recebimento.js)
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/recebimento-equipamento.jsp", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/RecebimentoServlet", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/DevolucaoServlet", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/MovimentacaoRecebimentoServlet", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/movimentacao_recebimento", "movimentacao_recebimento"); 
        PROTECTED_RESOURCES_MODULES.put("/api/movimentacao_recebimento/", "movimentacao_recebimento"); 
        PROTECTED_RESOURCES_MODULES.put("/api/envios/transito", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/envios/detalhes", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/envios/receber", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/devolucoes/transito", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/devolucoes/detalhes", "movimentacao_recebimento");
        PROTECTED_RESOURCES_MODULES.put("/api/devolucoes/receber", "movimentacao_recebimento");
        // 11. password_reset_tokens
        PROTECTED_RESOURCES_MODULES.put("/WEB-INF/jsp/resetPassword.jsp", "password_reset_tokens");
        PROTECTED_RESOURCES_MODULES.put("/PasswordResetServlet", "password_reset_tokens");
        PROTECTED_RESOURCES_MODULES.put("/api/password_reset_tokens", "password_reset_tokens"); 
        PROTECTED_RESOURCES_MODULES.put("/api/password_reset_tokens/", "password_reset_tokens");

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthFilter inicializado.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        // Obtém o path relativo à raiz da aplicação (ex: /LoginServlet, /css/estilo.css)
        String path = requestURI.substring(contextPath.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        System.out.println("\n--- AuthFilter Início ---");
        System.out.println("AuthFilter: Context Path: " + contextPath);
        System.out.println("AuthFilter: Request URI: " + requestURI);
        System.out.println("AuthFilter: Path processado: " + path);

        HttpSession session = httpRequest.getSession(false); // Não cria nova sessão se não existir
        Usuario usuarioLogadoObj = null;
        if (session != null) {
            usuarioLogadoObj = (Usuario) session.getAttribute("usuarioLogado");
        }
        boolean loggedIn = (usuarioLogadoObj != null);

        String userProfile = (loggedIn) ? usuarioLogadoObj.getPerfil() : null; 
        List<String> modulosPermitidos = (loggedIn) ? usuarioLogadoObj.getModulosPermitidos() : null; 

        boolean isAjaxRequest = "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"));

        // Regra 1: Permitir acesso a recursos públicos (JSPs de entrada, Servlets públicos, CSS/JS/Imagens)
        boolean isPublicPage = PUBLIC_PAGES.contains(path);
        boolean isPublicResource = PUBLIC_RESOURCES_AND_INCLUDES.stream().anyMatch(path::startsWith);
        boolean isPublicServlet = PUBLIC_SERVLETS.contains(path);
        boolean isMaintenancePage = requestURI.endsWith("/maintenance.jsp");

        if (isPublicPage || isPublicResource || isPublicServlet || isMaintenancePage) {
            System.out.println("AuthFilter: Recurso público (" + path + "). Permitindo acesso.");
            chain.doFilter(request, response);
            return;
        }
        
        // --- NOVO: Regra para Modo de Manutenção ---
        boolean isMaintenanceMode = isMaintenanceMode(httpRequest);
        boolean isBackupUser = false;
        
        if (loggedIn) {
            if (session != null) {
                String backupUserId = (String) session.getAttribute("backupUserId");
                if (backupUserId != null && String.valueOf(usuarioLogadoObj.getId()).equals(backupUserId)) {
                    isBackupUser = true;
                }
            }
        }

        if (isMaintenanceMode && !isBackupUser) {
            httpResponse.sendRedirect(contextPath + "/maintenance.jsp");
            return;
        }
        // --- FIM DA NOVA REGRA ---

        // Regra 2: Bloquear acesso direto a login.jsp
        if (path.equals("/login.jsp") && request.getAttribute("forwardedFromLoginServlet") == null) {
            System.out.println("AuthFilter: Acesso direto a login.jsp sem passar pelo Servlet. Redirecionando.");
            httpResponse.sendRedirect(contextPath + "/LoginServlet");
            return;
        }
        
        // Regra 3: Se o usuário não está logado, redireciona para a página de login.
        if (!loggedIn) {
            System.out.println("AuthFilter: Usuário NÃO logado. Negando acesso a " + path + " e redirecionando para login.");
            httpResponse.sendRedirect(contextPath + "/LoginServlet"); 
            return;
        }

     // --- REGRA DE UNIDADES EVOLUÍDA ---
        if (loggedIn) {
            Integer unidadeAtivaId = usuarioLogadoObj.getUnidadeAtivaId();
            List<String> unidadesPermitidasIds = usuarioLogadoObj.getUnidadesPermitidas();
            
            // Se por algum motivo o usuário logado não tem uma unidade ativa definida
            if (unidadeAtivaId == null && (unidadesPermitidasIds != null && !unidadesPermitidasIds.isEmpty())) {
                // Fallback de segurança: define a primeira permitida como ativa
                usuarioLogadoObj.setUnidadeAtivaId(Integer.parseInt(unidadesPermitidasIds.get(0)));
                unidadeAtivaId = usuarioLogadoObj.getUnidadeAtivaId();
            }

            // Validação de Segurança Transversal:
            // Se o usuário tentar forçar um parâmetro de unidade diferente da que ele está "logado" no menu
            String unidadeNaUrl = httpRequest.getParameter("unidade_origem");
            if (unidadeNaUrl != null && !unidadeNaUrl.equals(String.valueOf(unidadeAtivaId))) {
                boolean ehAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(userProfile) || 
                                  PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(userProfile);
                
                // Se não for admin e tentar acessar unidade que não é a ativa ou não tem permissão
                if (!ehAdmin && !unidadesPermitidasIds.contains(unidadeNaUrl)) {
                    System.out.println("AuthFilter: Bloqueio de Unidade. Tentativa de violação de contexto.");
                    sendAccessDeniedResponse(httpRequest, httpResponse, isAjaxRequest, contextPath, "Acesso negado. Unidade fora de contexto.");
                    return;
                }
            }
        }
        // --- FIM DA ALTERAÇÃO ---

        // Regra 4: Bloquear acesso direto aos JSPs listados na lista de proteção.
        if (PROTECTED_JSP_PAGES.contains(path)) {
            System.out.println("AuthFilter: Tentativa de acesso direto a JSP protegido: " + path + ". Negando acesso.");
            String errorMessage = "A página solicitada não pode ser acessada diretamente.";
            sendAccessDeniedResponse(httpRequest, httpResponse, isAjaxRequest, contextPath, errorMessage);
            return;
        }
        
        // Regra 5: Verificar permissões baseadas no mapeamento de recursos para módulos.
        String requiredModule = PROTECTED_RESOURCES_MODULES.get(path);

        if (requiredModule != null) {
            if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(userProfile) || 
                PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(userProfile)) {
                System.out.println("AuthFilter: Usuário é " + userProfile.toUpperCase() + ". Permitindo acesso a " + path + ".");
                chain.doFilter(request, response);
            } else {
                boolean hasPermission = false;
                if (modulosPermitidos != null) {
                    for (String module : modulosPermitidos) {
                        if (module.equalsIgnoreCase(requiredModule)) {
                            hasPermission = true;
                            break;
                        }
                    }
                }
                
                if (hasPermission) {
                    System.out.println("AuthFilter: Usuário '" + usuarioLogadoObj.getUsername() + "' tem permissão para o módulo '" + requiredModule + "'. Permitindo acesso a " + path + ".");
                    chain.doFilter(request, response);
                } else {
                    System.out.println("AuthFilter: Usuário '" + usuarioLogadoObj.getUsername() + "' NÃO tem permissão para o módulo '" + requiredModule + "'. Negando acesso a " + path + ".");
                    String errorMessage = "Acesso negado. Você não possui permissão para o módulo de '" + requiredModule + "'.";
                    sendAccessDeniedResponse(httpRequest, httpResponse, isAjaxRequest, contextPath, errorMessage);
                }
            }
            return; 
        }
        
        // Regra 6: Permite acesso a qualquer outro recurso
        System.out.println("AuthFilter: Recurso " + path + " não explicitamente protegido por módulo, mas usuário logado. Permitindo acesso.");
        chain.doFilter(request, response);
    }

    private void sendAccessDeniedResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean isAjaxRequest, String contextPath, String message) throws IOException {
        if (isAjaxRequest) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"error\":\"" + message + "\"}");
        } else {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.setAttribute("toastMessage", message);
            }
            httpResponse.sendRedirect(contextPath + "/menu.jsp");
        }
    }

    private boolean isMaintenanceMode(HttpServletRequest request) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getServletContext().getResourceAsStream("/WEB-INF/classes/maintenance_mode.txt")))) {
            String status = reader.readLine();
            return "true".equalsIgnoreCase(status.trim());
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo de modo de manutenção no filtro: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("AuthFilter destruído.");
    }
}