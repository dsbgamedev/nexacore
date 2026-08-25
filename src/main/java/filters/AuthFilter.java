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
        "/cadastroEntidade.jsp",
        "/cadastroProdutos.jsp",
        "/cadastroUsuario.jsp",
        "/consulta.jsp",
        "/tiposFornecimento.jsp",
        "/tiposProduto.jsp",
        // Nomes das JSPs para o módulo de Manutenção
        "/manutencaoEquipamentos.jsp", // JSP do Formulário de Chamado
        "/historicoManutencao.jsp", // JSP do Histórico de Manutenções
        "/forgotPassword.jsp",
        "/resetPassword.jsp",
        "/gerenciarEstoque.jsp",
        "/cadastroTransportadora.jsp", 
        "/lancarFrete.jsp" ,// JSP do novo módulo de frete
        "/consultaFrete.jsp/",
        "/consultarLogs.jsp",
        "/gerarEtiquetaVisual.jsp", // Adicione esta linha
        "/gerarEtiquetaVisual.jsp"
        //"/TrocarUnidadeServlet" // <--- ADICIONE ESTA LINHA
        // NOVO: Adicionado JSP de transportadoras
        /*"/backup.jsp" Adicionado para proteger o acesso direto*/
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
        // Módulos para Produtos
        PROTECTED_RESOURCES_MODULES.put("/cadastroProdutos.jsp", "produtos");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarProdutoServlet", "produtos");

        // Módulos para Usuários
        PROTECTED_RESOURCES_MODULES.put("/cadastroUsuario.jsp", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/listarUsuarios.jsp", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarUsuarioServlet", "usuarios");
        PROTECTED_RESOURCES_MODULES.put("/ExcluirUsuarioServlet", "usuarios"); 
        PROTECTED_RESOURCES_MODULES.put("/GerenciarUsuariosServlet", "usuarios"); 

        // Módulos para Entidades (Clientes e Fornecedores)
        PROTECTED_RESOURCES_MODULES.put("/cadastroEntidade.jsp", "entidades");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarEntidadeServlet", "entidades");

        // Módulos de Consulta Geral (ConsultaDadosServlet)
        PROTECTED_RESOURCES_MODULES.put("/consulta.jsp", "consulta"); // JSP geral de consulta
        PROTECTED_RESOURCES_MODULES.put("/ConsultaDadosServlet", "consulta"); 

        // Módulos para Relatórios
        PROTECTED_RESOURCES_MODULES.put("/relatorios.jsp", "relatorios");

        // Módulos para Manutenção de Equipamentos (agora usando 'manutencoes' como módulo)
        PROTECTED_RESOURCES_MODULES.put("/manutencaoEquipamentos.jsp", "manutencoes"); // JSP do formulário (Chamado)
        PROTECTED_RESOURCES_MODULES.put("/ManutencaoEquipamentoServlet", "manutencoes"); // Servlet do formulário
        PROTECTED_RESOURCES_MODULES.put("/historicoManutencao.jsp", "manutencoes"); // Nova JSP do Histórico

        // Módulos para Gerenciar Tipos de Fornecimento
        PROTECTED_RESOURCES_MODULES.put("/tiposFornecimento.jsp", "gerenciar_tipos_fornecimento");
        PROTECTED_RESOURCES_MODULES.put("/TipoFornecimentoServlet", "gerenciar_tipos_fornecimento");
        
        // Módulos para Gerenciar Tipos de Produto
        PROTECTED_RESOURCES_MODULES.put("/tiposProduto.jsp", "gerenciar_tipos_produto");
        PROTECTED_RESOURCES_MODULES.put("/TipoProdutoServlet", "gerenciar_tipos_produto");
        
        // NOVO MÓDULO PARA ESTOQUE
        PROTECTED_RESOURCES_MODULES.put("/gerenciarEstoque.jsp", "estoque");
        PROTECTED_RESOURCES_MODULES.put("/EstoqueServlet", "estoque");
        
        // NOVO MÓDULO PARA TRANSPORTADORAS
        PROTECTED_RESOURCES_MODULES.put("/cadastroTransportadora.jsp", "transportadoras");
        PROTECTED_RESOURCES_MODULES.put("/CadastrarTransportadoraServlet", "transportadoras");
        
        // CORREÇÃO CRÍTICA: MÓDULO LANÇAMENTO DE FRETES, USANDO O NOME CORRETO 'frete' (no singular) DO BANCO DE DADOS
        PROTECTED_RESOURCES_MODULES.put("/lancarFrete.jsp", "frete"); 
        PROTECTED_RESOURCES_MODULES.put("/LancarFreteServlet", "frete"); 
        PROTECTED_RESOURCES_MODULES.put("/ImprimirEtiquetaServlet", "frete"); // <--- ADICIONE ESTA LINHA AQUI
        PROTECTED_RESOURCES_MODULES.put("/consultaFrete.jsp", "consulta_frete"); 
        PROTECTED_RESOURCES_MODULES.put("/ConsultaFreteServlet", "consulta_frete");
        
        //TROCAR FRETE
        //PROTECTED_RESOURCES_MODULES.put("/TrocarUnidadeServlet", "frete");
        
     // Módulos para Auditoria de Logs
        PROTECTED_RESOURCES_MODULES.put("/consultarLogs.jsp", "auditoria"); 
        PROTECTED_RESOURCES_MODULES.put("/ConsultarLogsServlet", "auditoria");
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