let tipoOperacaoAtual = 'envio'; 

document.addEventListener("DOMContentLoaded", function () {
    const hoje = new Date().toISOString().split('T')[0];
    const inputData = document.getElementById('dataRecebimento');
    if (inputData) inputData.value = hoje;

    carregarMovimentacoesPorTipo('envio');

    const selectEnvio = document.getElementById('selectEnvio');
    if (selectEnvio) {
        selectEnvio.addEventListener('change', function () {
            if (this.value) {
                buscarDetalhesMovimentacao(this.value);
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
                if (data.sucesso) {
                    ModalService.success("Sucesso", data.mensagem || "Operação realizada com sucesso!").then(() => {
                        marcarItensComoRecebidosNaTabela();
                        // Opcional: recarregar a lista para remover o item recebido
                        carregarMovimentacoesPorTipo(tipoOperacaoAtual);
                        limparCampos();
                    });
                } else {
                    ModalService.error("Atenção", data.mensagem || "Erro ao processar.");
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                ModalService.error("Erro Técnico", "Falha de conexão.");
            });
        });
    }
});

function selecionarTipoOperacao(tipo) {
    tipoOperacaoAtual = tipo;
    
    // Atualização visual dos cards
    const cardEnvio = document.getElementById('cardEnvio');
    const cardDevolucao = document.getElementById('cardDevolucao');
    
    if (cardEnvio) cardEnvio.classList.toggle('border-success', tipo === 'envio');
    if (cardDevolucao) cardDevolucao.classList.toggle('border-success', tipo === 'devolucao');

    // Resetar campos e carregar nova lista
    limparCampos();
    carregarMovimentacoesPorTipo(tipo);
}

function carregarMovimentacoesPorTipo(tipo) {
    const endpoint = tipo === 'devolucao' ? '/api/devolucoes/transito' : '/api/envios/transito';

    fetch(contextPath + endpoint)
        .then(response => response.json())
        .then(itens => {
            const select = document.getElementById('selectEnvio');
            if (!select) return;

            select.innerHTML = `<option value="">Selecione ${tipo === 'devolucao' ? 'uma devolução' : 'um envio'}...</option>`;
            itens.forEach(item => {
                const option = document.createElement('option');
                option.value = item.idEnvio || item.id;
                option.textContent = tipo === 'devolucao' 
                    ? `Devolução #${option.value} - Origem: ${item.origemNome} (${item.codigoRastreio || 'Em Trânsito'})`
                    : `Envio #${option.value} - Destino: ${item.destinoNome} (${item.codigoRastreio || 'Sem Rastreio'})`;
                select.appendChild(option);
            });
        })
        .catch(err => console.error("Erro ao carregar lista:", err));
}

function buscarDetalhesMovimentacao(idMovimentacao) {
    const endpoint = tipoOperacaoAtual === 'devolucao' ? `/api/devolucoes/detalhes?id=${idMovimentacao}` : `/api/envios/detalhes?id=${idMovimentacao}`;

    fetch(contextPath + endpoint)
        .then(response => response.json())
        .then(data => {
            document.getElementById('origem').value = data.origemNome || '';
            document.getElementById('transportadora').value = data.transportadora || '';
            document.getElementById('codigoRastreio').value = data.codigoRastreio || '';

            const tbody = document.querySelector('#tabelaItensRecebimento tbody');
            tbody.innerHTML = data.itens?.map(item => `
                <tr>
                    <td>${item.idSistema}</td>
                    <td>${item.patrimonio || '-'}</td>
                    <td>${item.nomeCpu || '-'}</td>
                    <td>${item.produto || '-'}</td>
                    <td>${item.numeroSerie || '-'}</td>
                    <td><span class="badge rounded-pill px-3 py-2 text-warning bg-warning-subtle">Em Trânsito</span></td>
                </tr>
            `).join('') || '<tr><td colspan="6" class="text-center">Nenhum item encontrado.</td></tr>';
        });
}

function limparCampos() {
    document.getElementById('origem').value = '';
    document.getElementById('transportadora').value = '';
    document.getElementById('codigoRastreio').value = '';
    document.getElementById('selectEnvio').selectedIndex = 0;
    
    const tbody = document.querySelector('#tabelaItensRecebimento tbody');
    if (tbody) tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Selecione uma opção para carregar os equipamentos.</td></tr>`;
}

function marcarItensComoRecebidosNaTabela() {
    document.querySelectorAll('.badge').forEach(b => {
        b.className = "badge rounded-pill px-3 py-2 text-success bg-success-subtle";
        b.innerText = "Recebido";
    });
}