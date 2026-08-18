let tipoOperacaoAtual = 'envio'; 

document.addEventListener("DOMContentLoaded", function () {
    const hoje = new Date().toISOString().split('T')[0];
    const inputData = document.getElementById('dataRecebimento');
    if (inputData) inputData.value = hoje;

    // Verifica se a página foi aberta via parâmetro de devolução (ex: ?tipo=devolucao)
    const urlParams = new URLSearchParams(window.location.search);
    const tipoUrl = urlParams.get('tipo');

    if (tipoUrl === 'devolucao') {
        tipoOperacaoAtual = 'devolucao';
        atualizarVisualCards('devolucao');
    }

    // Carrega a listagem correta baseada no tipo detectado
    carregarMovimentacoesPorTipo(tipoOperacaoAtual);

    const selectEnvio = document.getElementById('selectEnvio');
    if (selectEnvio) {
        selectEnvio.addEventListener('change', function () {
            const idMov = this.value;
            if (idMov) {
                buscarDetalhesMovimentacao(idMov);
            } else {
                limparCampos();
            }
        });
    }

	const formRecebimento = document.getElementById('formRecebimento');
    if (formRecebimento) {
        formRecebimento.addEventListener('submit', function (e) {
            e.preventDefault();
            
            const formData = new URLSearchParams(new FormData(this));
            formData.append('tipoOperacao', tipoOperacaoAtual);

            const endpointRecebimento = tipoOperacaoAtual === 'devolucao' 
                ? '/api/devolucoes/receber' 
                : '/api/envios/receber';

            fetch(contextPath + endpointRecebimento, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                const mensagemSucesso = tipoOperacaoAtual === 'devolucao' 
                    ? "Devolução efetuada com sucesso e estoque atualizado!" 
                    : (data.mensagem || "Recebimento confirmado e estoque atualizado com sucesso!");

                // Exibe o modal imediatamente sem travar fluxo em promessas
                ModalService.success("Sucesso", mensagemSucesso);

                // Executa a atualização visual e limpeza dos campos na hora
                marcarItensComoRecebidosNaTabela();
                limparCampos();
                carregarMovimentacoesPorTipo(tipoOperacaoAtual);
            })
            .catch(error => {
                console.error('Erro:', error);
                ModalService.error("Erro Técnico", "Falha de conexão.");
            });
        });
    }
});

// Função para alternar os cards de Envio vs Devolução via clique
function selecionarTipoOperacao(tipo) {
    tipoOperacaoAtual = tipo;
    atualizarVisualCards(tipo);
    carregarMovimentacoesPorTipo(tipo);
    limparCampos();
}

function atualizarVisualCards(tipo) {
    const cardEnvio = document.getElementById('cardEnvio');
    const cardDevolucao = document.getElementById('cardDevolucao');
    const radioEnvio = document.getElementById('radioEnvio');
    const radioDevolucao = document.getElementById('radioDevolucao');
    const labelSelect = document.getElementById('labelSelectMovimentacao');
    const btnSubmit = document.querySelector("button[type='submit']");

    if (tipo === 'envio') {
        if (radioEnvio) radioEnvio.checked = true;
        if (cardEnvio) cardEnvio.classList.add('border-success');
        if (cardDevolucao) cardDevolucao.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Envio (Trânsito) *";
        if (btnSubmit) btnSubmit.textContent = "Confirmar Recebimento";
    } else {
        if (radioDevolucao) radioDevolucao.checked = true;
        if (cardDevolucao) cardDevolucao.classList.add('border-success');
        if (cardEnvio) cardEnvio.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Devolução (Trânsito) *";
        if (btnSubmit) btnSubmit.textContent = "Confirmar Devolução";
    }
}

// Carrega os dados dependendo se é Envio ou Devolução
function carregarMovimentacoesPorTipo(tipo) {
    const endpoint = tipo === 'devolucao' ? '/api/devolucoes/transito' : '/api/envios/transito';
    
    fetch(contextPath + endpoint)
        .then(response => response.json())
        .then(envios => {
            const select = document.getElementById('selectEnvio');
            if (!select) return;

            select.innerHTML = '<option value="">Selecione...</option>';

            if (Array.isArray(envios)) {
                envios.forEach(envio => {
                    const option = document.createElement('option');
                    option.value = envio.idEnvio;
                    
                    if (tipo === 'devolucao') {
                        option.textContent = `Devolução #${envio.idEnvio} - Origem: ${envio.origemNome} (${envio.codigoRastreio || 'Sem Rastreio'})`;
                    } else {
                        option.textContent = `Envio #${envio.idEnvio} - Destino: ${envio.destinoNome} (${envio.codigoRastreio || 'Sem Rastreio'})`;
                    }
                    
                    select.appendChild(option);
                });
            }
        })
        .catch(err => console.error("Erro ao carregar movimentações:", err));
}

// Direciona para a busca de detalhes correta
function buscarDetalhesMovimentacao(idMov) {
    const endpoint = tipoOperacaoAtual === 'devolucao' ? `/api/devolucoes/detalhes?id=${idMov}` : `/api/envios/detalhes?id=${idMov}`;

    fetch(contextPath + endpoint)
        .then(response => response.json())
        .then(data => {
            const origemInput = document.getElementById('origem');
            if (origemInput) origemInput.value = data.origemNome || '';

            const transpInput = document.getElementById('transportadora');
            if (transpInput) transpInput.value = data.transportadora || '';

            const rastreioInput = document.getElementById('codigoRastreio');
            if (rastreioInput) rastreioInput.value = data.codigoRastreio || '';

            const tbody = document.querySelector('#tabelaItensRecebimento tbody');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (data.itens && data.itens.length > 0) {
                data.itens.forEach(item => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${item.idSistema}</td>
                        <td>${item.patrimonio || '-'}</td>
                        <td>${item.nomeCpu || '-'}</td>
                        <td>${item.produto || '-'}</td>
                        <td>${item.numeroSerie || '-'}</td>
                        <td><span class="badge rounded-pill px-3 py-2" style="background-color: #ffc10720; color: #ffc107; font-weight: 600;">Em Trânsito</span></td>
                    `;
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Nenhum equipamento encontrado nesta movimentação.</td></tr>`;
            }
        })
        .catch(err => console.error("Erro ao buscar detalhes:", err));
}

function marcarItensComoRecebidosNaTabela() {
    const badges = document.querySelectorAll('#tabelaItensRecebimento tbody tr td .badge');
    badges.forEach(badge => {
        badge.className = "badge rounded-pill px-3 py-2";
        badge.style.backgroundColor = "#19875420";
        badge.style.color = "#198754";
        badge.innerText = "Recebido";
    });
}

function limparCampos() {
    const select = document.getElementById('selectEnvio');
    if (select) select.value = ''; // Reseta o select para "Selecione..."

    const origemInput = document.getElementById('origem');
    if (origemInput) origemInput.value = '';

    const transpInput = document.getElementById('transportadora');
    if (transpInput) transpInput.value = '';

    const rastreioInput = document.getElementById('codigoRastreio');
    if (rastreioInput) rastreioInput.value = '';
	
	// Adicionado para limpar o campo Responsável pelo Recebimento
    const responsavelInput = document.getElementById('responsavel');
    if (responsavelInput) responsavelInput.value = '';

    const tbody = document.querySelector('#tabelaItensRecebimento tbody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Selecione um item acima para carregar os equipamentos.</td></tr>`;
    }
}