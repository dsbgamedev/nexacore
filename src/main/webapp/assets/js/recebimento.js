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
	
	// =================================================================
    // CORRIGIDO: Evento do Botão Cancelar (Limpa a tela, data e atualiza os selects)
    // =================================================================
    const btnCancelar = document.getElementById('btnCancelar');
    if (btnCancelar) {
        btnCancelar.addEventListener('click', function(e) {
            e.preventDefault(); // Evita qualquer comportamento padrão
            limparCampos();
            
            // Restaura a data de hoje ao limpar
            const hoje = new Date().toISOString().split('T')[0];
            const inputData = document.getElementById('dataRecebimento');
            if (inputData) inputData.value = hoje;

            carregarMovimentacoesPorTipo(tipoOperacaoAtual); // Atualiza a lista do select sem refresh (F5)
        });
    }

	const formRecebimento = document.getElementById('formRecebimento');
    if (formRecebimento) {
        formRecebimento.addEventListener('submit', function (e) {
            e.preventDefault();
            
            // =================================================================
            // TRAVA DE SEGURANÇA: Evita submeter se não houver itens listados
            // =================================================================
            const linhasTabela = document.querySelectorAll('#tabelaItensRecebimento tbody tr');
            let temItensValidos = false;
            linhasTabela.forEach(tr => {
                const texto = tr.innerText.toLowerCase();
                if (!texto.includes("selecione um item") && !texto.includes("nenhum equipamento")) {
                    temItensValidos = true;
                }
            });

            if (!temItensValidos) {
                ModalService.error("Atenção", "Não há equipamentos para receber. Selecione uma movimentação válida.");
                return; // Para a execução aqui mesmo e não chama a API
            }

            const formData = new URLSearchParams(new FormData(this));
            formData.append('tipoOperacao', tipoOperacaoAtual);

            const endpointRecebimento = tipoOperacaoAtual === 'devolucao' 
                ? '/api/devolucoes/receber' 
                : '/api/envios/receber';

			fetch(contextPath + endpointRecebimento, {
	                method: 'POST',
	                body: formData
	            })
	            .then(async response => {
	                const data = await response.json();
	                if (!response.ok) {
	                    // Se deu erro no Java, lança com a mensagem
	                    throw new Error(data.mensagem || "Erro ao processar a solicitação.");
	                }
	                return data;
	            })
				.then(data => {
                    // VERIFICAÇÃO DE SEGURANÇA EXTRA: 
                    // Se o Java retornou um JSON indicando falha/erro interno (ex: data.sucesso === false)
                    if (data && data.sucesso === false) {
                        throw new Error(data.mensagem || "Ação negada pelo servidor.");
                    }

	                const mensagemSucesso = tipoOperacaoAtual === 'devolucao' 
	                    ? "Devolução efetuada com sucesso e estoque atualizado!" 
	                    : (data.mensagem || "Recebimento confirmado e estoque atualizado com sucesso!");
	
	                // Aqui sim é o sucesso real (verde)
	                ModalService.success("Sucesso", mensagemSucesso);
	
	                marcarItensComoRecebidosNaTabela();
	                
	                setTimeout(() => {
	                    limparCampos();
	                    carregarMovimentacoesPorTipo(tipoOperacaoAtual); 
	                }, 2000);
	            })
	            .catch(error => {
	                console.error('Erro:', error);
	                
	                // GARANTA QUE AQUI É USADO O ERROR (Vermelho) E NÃO SUCCESS!
	                ModalService.error("Atenção", error.message);
	                
	                carregarMovimentacoesPorTipo(tipoOperacaoAtual);
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

// Atualiza o visual dos cards e o rótulo dinâmico (Origem vs Destino)
function atualizarVisualCards(tipo) {
    const cardEnvio = document.getElementById('cardEnvio');
    const cardDevolucao = document.getElementById('cardDevolucao');
    const radioEnvio = document.getElementById('radioEnvio');
    const radioDevolucao = document.getElementById('radioDevolucao');
    const labelSelect = document.getElementById('labelSelectMovimentacao');
    const labelCampoOrigemDestino = document.getElementById('labelCampoOrigemDestino'); 
    const btnSubmit = document.querySelector("button[type='submit']");

    if (tipo === 'envio') {
        if (radioEnvio) radioEnvio.checked = true;
        if (cardEnvio) cardEnvio.classList.add('border-success');
        if (cardDevolucao) cardDevolucao.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Envio (Trânsito) *";
        if (labelCampoOrigemDestino) labelCampoOrigemDestino.textContent = "Origem"; 
        if (btnSubmit) btnSubmit.textContent = "Confirmar Recebimento";
    } else {
        if (radioDevolucao) radioDevolucao.checked = true;
        if (cardDevolucao) cardDevolucao.classList.add('border-success');
        if (cardEnvio) cardEnvio.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Devolução (Trânsito) *";
        if (labelCampoOrigemDestino) labelCampoOrigemDestino.textContent = "Destino"; // Agora o rótulo vira Destino perfeitamente
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
            if (origemInput) {
                // Se for devolução, preenche com o destino; se for envio, preenche com a origem
                if (tipoOperacaoAtual === 'devolucao') {
                    origemInput.value = data.destinoNome || data.filialDestinoNome || data.destino || '';
                } else {
                    origemInput.value = data.origemNome || data.filialOrigemNome || data.origem || '';
                }
            }

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
    if (select) select.value = ''; 

    const origemInput = document.getElementById('origem');
    if (origemInput) origemInput.value = '';

    const transpInput = document.getElementById('transportadora');
    if (transpInput) transpInput.value = '';

    const rastreioInput = document.getElementById('codigoRastreio');
    if (rastreioInput) rastreioInput.value = '';
	
    const responsavelInput = document.getElementById('responsavel');
    if (responsavelInput) responsavelInput.value = '';

    // Limpa também o campo "Condição Geral" que aparece na sua tela
    const condicaoGeralInput = document.getElementById('condicaoGeral');
    if (condicaoGeralInput) condicaoGeralInput.value = 'Todos os itens em perfeito estado'; // Volta para o padrão ou vazio ('')

    const tbody = document.querySelector('#tabelaItensRecebimento tbody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Selecione um item acima para carregar os equipamentos.</td></tr>`;
    }
}