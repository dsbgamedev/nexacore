document.addEventListener('DOMContentLoaded', () => {
        if (typeof window.initGlobalBranding === 'function') {
            window.initGlobalBranding();
        } else {
            console.warn('initGlobalBranding não encontrado.');
        }

        const APP_CONTEXT_PATH = document.body.dataset.appContextPath || '';
        const inlineMessageContainer = document.getElementById('inlineMessageContainer');

        function displayInlineMessage(message, type) {
            if (!inlineMessageContainer) return;
            inlineMessageContainer.innerHTML = ''; 
            const p = document.createElement('p');
            p.textContent = message;
            p.className = 'message ' + type; 
            inlineMessageContainer.appendChild(p);

            setTimeout(() => {
                p.style.opacity = '0';
                p.style.transition = 'opacity 1s ease-out';
                setTimeout(() => {
                    inlineMessageContainer.innerHTML = '';
                }, 1000);
            }, 5000);
        }

        const sessionMessage = document.body.dataset.sessionMessage;
        if (sessionMessage && sessionMessage.trim() !== 'null' && sessionMessage.trim() !== '') {
            displayInlineMessage(sessionMessage, 'warning');
        }

        // Lógica para expandir/recolher submenus no menu lateral
        const toggleSubmenuLinks = document.querySelectorAll('.toggle-submenu');
        toggleSubmenuLinks.forEach(link => {
            link.addEventListener('click', function(event) {
                if (this.getAttribute('href') === '#' || this.getAttribute('href') === '') {
                    event.preventDefault();
                }
                const parentLi = this.closest('.menu-item-with-submenu');
                const submenu = parentLi.querySelector('.submenu');
                if (submenu) {
                    parentLi.classList.toggle('active'); 
                    submenu.classList.toggle('active');
                }
            });
        });

        // Seletor de Unidade Ativa via AJAX
        const selectUnidade = document.getElementById('selectUnidadeAtiva');
        if (selectUnidade) {
            selectUnidade.addEventListener('change', function() {
                const unidadeId = this.value;
                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        title: 'Alterando Unidade...',
                        allowOutsideClick: false,
                        didOpen: () => { Swal.showLoading(); }
                    });
                }

                const params = new URLSearchParams();
                params.append('id', unidadeId);

                fetch(`${APP_CONTEXT_PATH}/TrocarUnidadeServlet`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params.toString()
                })
                .then(response => {
                    if (response.ok) {
                        window.location.reload(true);
                    } else {
                        if (typeof Swal !== 'undefined') {
                            Swal.fire('Erro', 'Não foi possível trocar a unidade.', 'error');
                        } else {
                            alert('Erro ao trocar unidade.');
                        }
                    }
                })
                .catch(error => {
                    console.error('Erro:', error);
                    if (typeof Swal !== 'undefined') Swal.fire('Erro', 'Erro de conexão.', 'error');
                });
            });
        }
    });
// --- Tratamento de Acesso Negado via Parâmetro na URL ---
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('erro') === 'sem_permissao') {
        if (typeof ModalService !== 'undefined') {
            ModalService.error(
                "Acesso Negado", 
                "Você não possui permissão para acessar o cadastro de equipamentos."
            ).then(() => {
                // Remove o parâmetro da URL de forma limpa para o modal não reaparecer ao atualizar
                const novaUrl = window.location.pathname;
                window.history.replaceState({}, document.title, novaUrl);
            });
        }
    }