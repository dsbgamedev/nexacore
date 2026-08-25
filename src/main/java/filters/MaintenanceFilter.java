package filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(filterName = "MaintenanceFilter", urlPatterns = {"/*"})
public class MaintenanceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Verifica se o sistema está em modo de manutenção
        boolean isMaintenanceMode = isMaintenanceMode(httpRequest);
        
        // Permite o acesso à página de backup e ao servlet para quem já está logado
        String requestURI = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);

        if (isMaintenanceMode) {
            boolean isBackupPage = requestURI.endsWith("backup.jsp") || requestURI.endsWith("BackupServlet");
            boolean isLoggedIn = (session != null && session.getAttribute("usuario") != null);
            
            if (!isBackupPage && !isLoggedIn) {
                // Redireciona para a página de manutenção se não for a página de backup e o usuário não estiver logado
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/maintenance.jsp");
                return;
            }
        }

        // Continua a cadeia de filtros e servlets
        chain.doFilter(request, response);
    }
    
    private boolean isMaintenanceMode(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getServletContext().getResourceAsStream("/WEB-INF/classes/maintenance_mode.txt")))) {
            String status = reader.readLine();
            return "true".equalsIgnoreCase(status.trim());
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo de modo de manutenção no filtro: " + e.getMessage());
            return false;
        }
    }
}

