package filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Mapeia o filtro para todas as URLs, então ele será executado para cada requisição
@WebFilter("/*") 
public class BrandingFilter implements Filter {

    private Properties brandingProperties;
    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> BrandingFilter inicializado!");
        this.servletContext = filterConfig.getServletContext();
        brandingProperties = new Properties();
        
        // Carrega o arquivo branding.properties diretamente do classpath (src/main/resources)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("branding.properties")) {
            if (input != null) {
                brandingProperties.load(input);
                System.out.println("DEBUG: branding.properties carregado com sucesso do classpath.");
            } else {
                System.err.println("ERROR: branding.properties não encontrado na pasta resources.");
                throw new ServletException("Arquivo branding.properties não encontrado.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar branding.properties no BrandingFilter: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Falha ao carregar branding.properties no filtro", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 1. Tenta ler o companyId da sessão (se o utilizador já estiver logado)
        jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
        String companyId = (session != null) ? (String) session.getAttribute("companyId") : null;

        // 2. Se não estiver na sessão, tenta ler pelo parâmetro global do servidor (context.xml)
        if (companyId == null || companyId.isEmpty()) {
            companyId = servletContext.getInitParameter("companyId");
        }

        // 3. Se ainda assim estiver vazio, assume o padrão "default"
        if (companyId == null || companyId.isEmpty()) {
            companyId = "default";
        }

        // Recupera as propriedades de branding com base no companyId descoberto
        String nomeEmpresaDisplay = brandingProperties.getProperty(companyId + ".name", brandingProperties.getProperty("default.name", "Sistema"));
        String faviconFileName = brandingProperties.getProperty(companyId + ".favicon", brandingProperties.getProperty("default.favicon", ""));
        String logoImageFileName = brandingProperties.getProperty(companyId + ".logo", brandingProperties.getProperty("default.logo", ""));

        // Define os atributos no request, que estarão disponíveis para QUALQUER JSP
        httpRequest.setAttribute("nomeEmpresa", nomeEmpresaDisplay);

        String faviconPath = "";
        String faviconMimeType = "";
        if (faviconFileName != null && !faviconFileName.isEmpty()) {
            faviconPath = httpRequest.getContextPath() + "/images/favicons/" + faviconFileName;
            faviconMimeType = faviconFileName.toLowerCase().endsWith(".png") ? "image/png" : "image/x-icon";
        }
        httpRequest.setAttribute("faviconPath", faviconPath);
        httpRequest.setAttribute("faviconMimeType", faviconMimeType);

        // O logo é específico da tela de login, mas o path precisa ser conhecido pelo script
        String logoImagePath = "";
        if (logoImageFileName != null && !logoImageFileName.isEmpty()) {
            logoImagePath = httpRequest.getContextPath() + "/images/" + logoImageFileName;
        }
        httpRequest.setAttribute("logoImagePath", logoImagePath); 
        
        // Passa a requisição para o próximo recurso na cadeia (Servlet ou JSP)
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println(">>> BrandingFilter destruído!");
    }
}