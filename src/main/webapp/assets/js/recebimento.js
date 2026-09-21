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
	
    const btnCancelar = document.getElementById('btnCancelar');
    if (btnCancelar) {
        btnCancelar.addEventListener('click', function(e) {
            e.preventDefault(); 
            limparCampos();
            
            const hoje = new Date().toISOString().split('T')[0];
            const inputData = document.getElementById('dataRecebimento');
            if (inputData) inputData.value = hoje;

            carregarMovimentacoesPorTipo(tipoOperacaoAtual); 
        });
    }

    const formRecebimento = document.getElementById('formRecebimento');
    if (formRecebimento) {
        formRecebimento.addEventListener('submit', function (e) {
            e.preventDefault();
            
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
                return; 
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
                    throw new Error(data.mensagem || "Erro ao processar a solicitação.");
                }
                return data;
            })
            .then(data => {
                if (data && data.sucesso === false) {
                    throw new Error(data.mensagem || "Ação negada pelo servidor.");
                }

                const mensagemSucesso = tipoOperacaoAtual === 'devolucao' 
                    ? "Devolução efetuada com sucesso e estoque atualizado!" 
                    : (data.mensagem || "Recebimento confirmado e estoque atualizado com sucesso!");

                ModalService.success("Sucesso", mensagemSucesso);

                marcarItensComoRecebidosNaTabela();
                
                setTimeout(() => {
                    limparCampos();
                    carregarMovimentacoesPorTipo(tipoOperacaoAtual); 
                }, 2000);
            })
            .catch(error => {
                console.error('Erro:', error);
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
    const origemInput = document.getElementById('origem');

    if (tipo === 'envio') {
        if (radioEnvio) radioEnvio.checked = true;
        if (cardEnvio) cardEnvio.classList.add('border-success');
        if (cardDevolucao) cardDevolucao.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Envio (Trânsito) *";
        if (labelCampoOrigemDestino) labelCampoOrigemDestino.textContent = "Origem"; 
        if (btnSubmit) btnSubmit.textContent = "Confirmar Recebimento";
        
        // Garante que o campo origem fica travado e com estilo padrão
        if (origemInput) {
            origemInput.setAttribute('readonly', true);
            origemInput.classList.add('bg-light');
        }
    } else {
        if (radioDevolucao) radioDevolucao.checked = true;
        if (cardDevolucao) cardDevolucao.classList.add('border-success');
        if (cardEnvio) cardEnvio.classList.remove('border-success');
        if (labelSelect) labelSelect.textContent = "Selecionar Devolução (Trânsito) *";
        if (labelCampoOrigemDestino) labelCampoOrigemDestino.textContent = "Destino"; 
        if (btnSubmit) btnSubmit.textContent = "Confirmar Devolução";

        // Trava rigidamente o campo de Destino na devolução para ninguém alterar
        if (origemInput) {
            origemInput.setAttribute('readonly', true);
            origemInput.classList.add('bg-light');
        }
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

// Direciona para a busca de detalhes correta e trava o campo de origem/destino
function buscarDetalhesMovimentacao(idMov) {
    const endpoint = tipoOperacaoAtual === 'devolucao' ? `/api/devolucoes/detalhes?id=${idMov}` : `/api/envios/detalhes?id=${idMov}`;

    fetch(contextPath + endpoint)
        .then(response => response.json())
        .then(data => {
            const origemInput = document.getElementById('origem');
            if (origemInput) {
                if (tipoOperacaoAtual === 'devolucao') {
                    // Preenche com o destino real da devolução (ex: filial que vai receber)
                    origemInput.value = data.destinoNome || data.filialDestinoNome || data.destino || '';
                } else {
                    // Preenche com a origem real do envio
                    origemInput.value = data.origemNome || data.filialOrigemNome || data.origem || '';
                }
                // BLINDA O CAMPO: Assegura que permaneça bloqueado para edição manual
                origemInput.setAttribute('readonly', true);
                origemInput.classList.add('bg-light');
            }

            const transpInput = document.getElementById('transportadora');
            if (transpInput) {
                transpInput.value = data.transportadora || '';
                transpInput.setAttribute('readonly', true);
                transpInput.classList.add('bg-light');
            }

            const rastreioInput = document.getElementById('codigoRastreio');
            if (rastreioInput) {
                rastreioInput.value = data.codigoRastreio || '';
                rastreioInput.setAttribute('readonly', true);
                rastreioInput.classList.add('bg-light');
            }

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
    if (origemInput) {
        origemInput.value = '';
        origemInput.removeAttribute('readonly'); // Libera temporariamente para limpeza limpa
        origemInput.classList.remove('bg-light');
    }

    const transpInput = document.getElementById('transportadora');
    if (transpInput) {
        transpInput.value = '';
        transpInput.removeAttribute('readonly');
        transpInput.classList.remove('bg-light');
    }

    const rastreioInput = document.getElementById('codigoRastreio');
    if (rastreioInput) {
        rastreioInput.value = '';
        rastreioInput.removeAttribute('readonly');
        rastreioInput.classList.remove('bg-light');
    }
	
    const responsavelInput = document.getElementById('responsavel');
    if (responsavelInput) responsavelInput.value = '';

    const condicaoGeralInput = document.getElementById('condicaoGeral');
    if (condicaoGeralInput) condicaoGeralInput.value = 'Todos os itens em perfeito estado';

    const tbody = document.querySelector('#tabelaItensRecebimento tbody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Selecione um item acima para carregar os equipamentos.</td></tr>`;
    }
}