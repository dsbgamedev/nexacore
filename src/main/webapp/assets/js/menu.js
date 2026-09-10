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

	// --- Tratamento Global e Dinâmico de Erros via URL ---
    const urlParams = new URLSearchParams(window.location.search);
    const erroParam = urlParams.get('erro');

    if (erroParam) {
        let mensagem = "Ocorreu um erro na operação.";

        // Regras dinâmicas baseadas no código do erro
        if (erroParam.startsWith('sem_permissao')) {
            mensagem = "Você não possui permissão para acessar esta funcionalidade.";
            
            // Tratamentos específicos se necessário
            const modulo = urlParams.get('modulo');
            if (modulo === 'manutencao_chamados') {
                mensagem = "Seu usuário possui apenas permissão de consulta. O acesso à abertura de chamados está restrito.";
            } else if (erroParam === 'sem_permissao_atributo') {
                mensagem = "Acesso negado! Apenas Super Administradores podem acessar o módulo de atributos.";
            } else if (erroParam === 'sem_permissao_empresa') {
                mensagem = "Acesso negado! Apenas Super Administradores podem acessar o módulo de empresas.";
            }
        } 
        // NOVO: Se o servlet mandar uma mensagem personalizada codificada na URL (ex: ?erro=custom&msg=Seu+texto)
        else if (erroParam === 'custom' && urlParams.has('msg')) {
            mensagem = decodeURIComponent(urlParams.get('msg').replace(/\+/g, ' '));
        }

        setTimeout(() => {
            if (typeof ModalService !== 'undefined') {
                ModalService.error("Atenção", mensagem).then(() => {
                    const novaUrl = window.location.pathname;
                    window.history.replaceState({}, document.title, novaUrl);
                });
            } else {
                alert(mensagem);
                const novaUrl = window.location.pathname;
                window.history.replaceState({}, document.title, novaUrl);
            }
        }, 300);
    }
});
function abrirModalDetalhesChamado(idChamado) {
    const APP_CONTEXT_PATH = document.body.dataset.appContextPath || '';

    // Se a lista global de chamados já estiver carregada no menu, busca nela
    if (typeof listaChamadosGlobal !== 'undefined' && listaChamadosGlobal.length > 0) {
        const chamado = listaChamadosGlobal.find(c => c.idChamado === idChamado);
        if (chamado) {
            abrirModalGerenciarNoDashboard(chamado);
            return;
        }
    }

    // Caso contrário, busca via API da mesma forma que a tela de consulta faz
    fetch(APP_CONTEXT_PATH + '/api/manutencoes/listar')
        .then(res => res.json())
        .then(lista => {
            if (Array.isArray(lista)) {
                const chamado = lista.find(c => c.idChamado === idChamado);
                if (chamado) {
                    abrirModalGerenciarNoDashboard(chamado);
                } else {
                    if (typeof Swal !== 'undefined') {
                        Swal.fire('Atenção', 'Chamado não encontrado.', 'warning');
                    } else {
                        alert('Chamado não encontrado.');
                    }
                }
            }
        })
        .catch(error => {
            console.error('Erro:', error);
            if (typeof Swal !== 'undefined') {
                Swal.fire('Erro', 'Não foi possível carregar os detalhes do chamado.', 'error');
            } else {
                alert('Não foi possível carregar os detalhes do chamado.');
            }
        });
}

function abrirModalGerenciarNoDashboard(chamado) {
    // Preenche os campos do modal com os IDs corretos do menu.jsp
    const modalId = document.getElementById('modalIdChamado');
    if (modalId) modalId.value = chamado.idChamado;

    const lblEquip = document.getElementById('modalEquipamento');
    if (lblEquip) lblEquip.innerText = chamado.nomeEquipamento || `Equipamento ID: ${chamado.idEquipamento}`;

    const lblSolicitante = document.getElementById('modalSolicitante');
    if (lblSolicitante) lblSolicitante.innerText = chamado.solicitante || '---';

    const lblData = document.getElementById('modalDataAbertura');
    if (lblData) lblData.innerText = chamado.dataAbertura || '---';

    const txtDesc = document.getElementById('modalDescricao');
    if (txtDesc) txtDesc.value = chamado.descricaoProblema || '';

    const statusInput = document.getElementById('modalStatus');
    if (statusInput) statusInput.value = chamado.nomeStatus || 'Aberto';

    const tecnicoInput = document.getElementById('modalTecnico');
    if (tecnicoInput) tecnicoInput.value = chamado.responsavelTecnico || 'Não atribuído';

    const diagInput = document.getElementById('modalDiagnostico');
    if (diagInput) diagInput.value = chamado.diagnostico || '';

    const solInput = document.getElementById('modalSolucao');
    if (solInput) solInput.value = chamado.solucaoRealizada || '';

    // Abre o modal do Bootstrap
    const modalElement = document.getElementById('modalGerenciarChamado');
    if (modalElement) {
        const meuModal = new bootstrap.Modal(modalElement);
        meuModal.show();
    }
}
// Formatar datas no padrão yyyy-mm-dd para dd/mm/aaaa em elementos com a classe .data-formatavel
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.data-formatavel').forEach(td => {
        const textoOriginal = td.textContent.trim();
        if (textoOriginal && textoOriginal.length >= 10) {
            const partes = textoOriginal.substring(0, 10).split('-');
            if (partes.length === 3) {
                // Mantém o restante do texto caso venha hora junto (ex: yyyy-mm-dd hh:mm:ss)
                const hora = textoOriginal.length > 10 ? textoOriginal.substring(10) : '';
                td.textContent = `${partes[2]}/${partes[1]}/${partes[0]}${hora}`;
            }
        }
    });
});