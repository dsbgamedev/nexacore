document.addEventListener("DOMContentLoaded", function () {
    // 1. Define a data atual no campo de recebimento
    const hoje = new Date().toISOString().split('T')[0];
    const inputData = document.getElementById('dataRecebimento');
    if (inputData) inputData.value = hoje;

    // 2. Carrega a lista de envios "Em Trânsito" (status_id = 2) ao abrir a página
    carregarEnviosEmTransito();

    // 3. Evento ao mudar o envio selecionado
    const selectEnvio = document.getElementById('selectEnvio');
    if (selectEnvio) {
        selectEnvio.addEventListener('change', function () {
            const idEnvio = this.value;
            if (idEnvio) {
                buscarDetalhesEnvio(idEnvio);
            } else {
                limparCampos();
            }
        });
    }

    // 4. Envio do formulário para confirmar a baixa usando o ModalService
    const formRecebimento = document.getElementById('formRecebimento');
    if (formRecebimento) {
        formRecebimento.addEventListener('submit', function (e) {
            e.preventDefault();
            
            const formData = new URLSearchParams(new FormData(this));

            fetch(contextPath + '/api/envios/receber', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.sucesso) {
                    ModalService.success("Sucesso", data.mensagem || "Operação realizada com sucesso!").then(() => {
                        marcarItensComoRecebidosNaTabela();
                        carregarEnviosEmTransito();
                        limparCampos();
                    });
                } else {
                    ModalService.error("Atenção", "Erro: " + data.mensagem);
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                ModalService.error("Erro Técnico", "Erro de conexão ao processar a operação.");
            });
        });
    }
});

function carregarEnviosEmTransito() {
    fetch(contextPath + '/api/envios/transito')
        .then(response => response.json())
        .then(envios => {
            const select = document.getElementById('selectEnvio');
            if (!select) return;

            select.innerHTML = '<option value="">Selecione um envio...</option>';

            envios.forEach(envio => {
                const option = document.createElement('option');
                option.value = envio.idEnvio;
                option.textContent = `Envio #${envio.idEnvio} - Destino: ${envio.destinoNome} (${envio.codigoRastreio || 'Sem Rastreio'})`;
                select.appendChild(option);
            });
        })
        .catch(err => console.error("Erro ao carregar envios:", err));
}

function buscarDetalhesEnvio(idEnvio) {
    fetch(contextPath + `/api/envios/detalhes?id=${idEnvio}`)
        .then(response => response.json())
        .then(data => {
            const origemInput = document.getElementById('origem');
            if (origemInput) origemInput.value = data.origemNome || '';

            const transpInput = document.getElementById('transportadora');
            if (transpInput) transpInput.value = data.transportadora || '';

            const rastreioInput = document.getElementById('codigoRastreio');
            if (rastreioInput) rastreioInput.value = data.codigoRastreio || '';

            // --- ALTERAÇÃO DINÂMICA (DEVOLUÇÃO VS ENVIO) ---
            const tituloPagina = document.querySelector('.recebimento-titulo, h2, h1'); // Ajuste conforme a tag do título da sua página
            const botaoSubmit = document.querySelector('#formRecebimento button[type="submit"], .btn-success');
            
            // Verifica se é uma devolução pelo código de rastreio (ex: começa com DEV-) ou observação
            const ehDevolucao = (data.codigoRastreio && data.codigoRastreio.startsWith('DEV-')) || 
                                (data.observacoes && data.observacoes.includes('Devolução'));

            if (ehDevolucao) {
                if (tituloPagina) tituloPagina.innerText = "Recebimento de Devolução";
                if (botaoSubmit) botaoSubmit.innerText = "Confirmar Devolução";
            } else {
                if (tituloPagina) tituloPagina.innerText = "Recebimento de Equipamentos";
                if (botaoSubmit) botaoSubmit.innerText = "Confirmar Recebimento";
            }
            // ----------------------------------------------

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
                tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Nenhum equipamento encontrado neste envio.</td></tr>`;
            }
        })
        .catch(err => console.error("Erro ao buscar detalhes:", err));
}

// Atualiza visualmente o status na tabela para "Recebido" ao confirmar
function marcarItensComoRecebidosNaTabela() {
    const badges = document.querySelectorAll('#tabelaItensRecebimento tbody tr td .badge');
    badges.forEach(badge => {
        badge.className = "badge rounded-pill px-3 py-2";
        badge.style.backgroundColor = "#19875420"; // Verde suave
        badge.style.color = "#198754"; // Verde forte
        badge.innerText = "Recebido";
    });
}

function limparCampos() {
    const origemInput = document.getElementById('origem');
    if (origemInput) origemInput.value = '';

    const transpInput = document.getElementById('transportadora');
    if (transpInput) transpInput.value = '';

    const rastreioInput = document.getElementById('codigoRastreio');
    if (rastreioInput) rastreioInput.value = '';

    // Reseta o título e botão para o padrão caso limpe a seleção
    const tituloPagina = document.querySelector('.recebimento-titulo, h2, h1');
    const botaoSubmit = document.querySelector('#formRecebimento button[type="submit"], .btn-success');
    if (tituloPagina) tituloPagina.innerText = "Recebimento de Equipamentos";
    if (botaoSubmit) botaoSubmit.innerText = "Confirmar Recebimento";

    const tbody = document.querySelector('#tabelaItensRecebimento tbody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Selecione um envio acima para carregar os equipamentos.</td></tr>`;
    }
}