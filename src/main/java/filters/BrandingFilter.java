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

// Mapeia o filtro para todas as URLs, entao ele sera executado para cada requisicao
@WebFilter("/*") 
public class BrandingFilter implements Filter {

    private Properties brandingProperties;
    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> BrandingFilter inicializado!");
        this.servletContext = filterConfig.getServletContext();
        brandingProperties = new Properties();
        try (InputStream input = servletContext.getResourceAsStream("/WEB-INF/classes/branding.properties")) {
            if (input == null) {
                System.err.println("WARN: branding.properties nao encontrado em /WEB-INF/classes/. Tentando via ClassLoader.");
                try (InputStream clInput = getClass().getClassLoader().getResourceAsStream("branding.properties")) {
                     if (clInput != null) {
                         brandingProperties.load(clInput);
                         System.out.println("DEBUG: branding.properties carregado via ClassLoader.");
                     } else {
                         System.err.println("ERROR: branding.properties nao encontrado em nenhum local esperado.");
                     }
                }
            } else {
                brandingProperties.load(input);
                System.out.println("DEBUG: branding.properties carregado de /WEB-INF/classes/.");
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

        // Le o companyId do context.xml
        String companyId = servletContext.getInitParameter("companyId");
        if (companyId == null || companyId.isEmpty()) {
            companyId = "default";
        }
        // System.out.println("DEBUG BrandingFilter: companyId lido: " + companyId);

        // Recupera as propriedades de branding
        String nomeEmpresaDisplay = brandingProperties.getProperty(companyId + ".name", brandingProperties.getProperty("default.name", "Sistema"));
        String faviconFileName = brandingProperties.getProperty(companyId + ".favicon", brandingProperties.getProperty("default.favicon", ""));
        String logoImageFileName = brandingProperties.getProperty(companyId + ".logo", brandingProperties.getProperty("default.logo", ""));

        // Define os atributos no request, que estarao disponiveis para QUALQUER JSP
        httpRequest.setAttribute("nomeEmpresa", nomeEmpresaDisplay);

        String faviconPath = "";
        String faviconMimeType = "";
        if (faviconFileName != null && !faviconFileName.isEmpty()) {
            faviconPath = httpRequest.getContextPath() + "/images/favicons/" + faviconFileName;
            faviconMimeType = faviconFileName.toLowerCase().endsWith(".png") ? "image/png" : "image/x-icon";
        }
        httpRequest.setAttribute("faviconPath", faviconPath);
        httpRequest.setAttribute("faviconMimeType", faviconMimeType);

        // O logo é específico da tela de login, mas o path precisa ser conhecido pelo login.js
        String logoImagePath = "";
        if (logoImageFileName != null && !logoImageFileName.isEmpty()) {
            logoImagePath = httpRequest.getContextPath() + "/images/" + logoImageFileName;
        }
        // Este atributo será lido pelo login.js no body do login.jsp
        httpRequest.setAttribute("logoImagePath", logoImagePath); 
        
        // Passa a requisicao para o proximo recurso na cadeia (Servlet ou JSP)
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println(">>> BrandingFilter destruido!");
    }
}
