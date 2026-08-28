// Este script é responsável por injetar o favicon e o título da aba dinamicamente
// em todas as páginas do sistema que o incluem.
// Ele é auto-executável quando o DOM estiver carregado.

document.addEventListener('DOMContentLoaded', async () => {

    const APP_CONTEXT_PATH = document.body.dataset.appContextPath || document.body.dataset.contextPath || '';

    try {
        const response = await fetch(`${APP_CONTEXT_PATH}/LoginServlet?action=getBranding`);

        if (!response.ok) {
            console.error('Erro ao buscar dados de branding globais:', response.status, response.statusText);
            return;
        }

        const brandingData = await response.json();

        console.log('Dados de branding globais recebidos:', brandingData);

        // Título
        const originalTitleElement = document.querySelector('head title');
        let originalPageTitle = originalTitleElement ? originalTitleElement.textContent : 'Sistema';

        if (brandingData.nomeEmpresa) {
            document.title = `${originalPageTitle.split(' - ')[0] || originalPageTitle} - ${brandingData.nomeEmpresa}`;
        } else {
            document.title = `${originalPageTitle.split(' - ')[0] || originalPageTitle} - CadastroWeb`;
        }

        // Favicon
        document.querySelectorAll('link[rel="icon"], link[rel="shortcut icon"]').forEach(link => link.remove());

        if (brandingData.faviconPath) {
            const link = document.createElement('link');
            link.rel = 'icon';
            link.type = brandingData.faviconMimeType || 'image/x-icon';
            link.href = brandingData.faviconPath;
            document.head.appendChild(link);
        }

    } catch (error) {
        console.error('Erro ao buscar e injetar branding globais:', error);
    }

    // MAIÚSCULAS AUTOMÁTICAS
    const camposMaiusculos = document.querySelectorAll('.maiusculo');

    camposMaiusculos.forEach(function (campo) {
        campo.addEventListener('input', function () {
            this.value = this.value.toUpperCase();
        });
    });

});