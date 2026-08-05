let listaGlobalEnvios = [];

document.addEventListener("DOMContentLoaded", function () {
    carregarEnvios();

    const inputPesquisa = document.getElementById("inputPesquisaGlobal");
    if (inputPesquisa) {
        inputPesquisa.addEventListener("input", function () {
            filtrarEnvios(this.value);
        });
    }
});

function carregarEnvios() {
    const url = contextPath + '/api/envios';
    console.log("Tentando carregar envios de:", url);

    fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(async response => {
        console.log("Status da resposta:", response.status);
        if (!response.ok) {
            let textoErro = await response.text();
            throw new Error(`Erro HTTP ${response.status}: ${textoErro}`);
        }
        return response.json();
    })
    .then(data => {
        console.log("Dados recebidos da API:", data);
        listaGlobalEnvios = data || [];
        renderizarTabela(listaGlobalEnvios);
    })
    .catch(error => {
        console.error("Erro detalhado ao carregar envios:", error);
        
        // Mostra o erro exato no corpo da tabela para facilitar a visualização sem precisar abrir o console
        const tbody = document.querySelector("#tabelaEnvios tbody");
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="11" class="text-center text-danger py-4"><strong>Erro ao carregar dados:</strong> ${error.message}</td></tr>`;
        }

        if (typeof ModalService !== 'undefined') {
            ModalService.error("Erro", "Erro ao carregar a lista de envios. Veja os detalhes na tabela.");
        } else {
            alert("Erro ao carregar a lista de envios: " + error.message);
        }
    });
}

// Função auxiliar para formatar a data de AAAA-MM-DD para DD/MM/AAAA
function formatarDataBR(dataStr) {
    if (!dataStr) return '';
    let partes = dataStr.split('-');
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataStr;
}

function renderizarTabela(dados) {
    const tbody = document.querySelector("#tabelaEnvios tbody");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!dados || dados.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" class="text-center text-muted py-4">Nenhum envio encontrado.</td></tr>';
        return;
    }

    dados.forEach(envio => {
        let origemTexto = envio.nomeOrigem ? envio.nomeOrigem : ('ID: ' + envio.origemId);
        let destinoTexto = envio.nomeDestino ? envio.nomeDestino : ('ID: ' + envio.destinoId);

        let nomeStatus = envio.statusNome || 'Enviado';
        let corStatus = envio.statusCor || '#0d6efd'; 
        
        // Badge em formato pílula com Soft UI
        let badgeHtml = `<span class="badge rounded-pill px-3 py-2" style="background-color: ${corStatus}20; color: ${corStatus}; font-weight: 600;">${nomeStatus}</span>`;

        // Botões de Ação Modernos (Visualizar + Cancelar se status permitir, sem três pontos)
        let acoesHtml = `
            <div class="d-flex justify-content-center gap-1">
                <button class="btn btn-light btn-sm text-secondary" title="Visualizar Detalhes" onclick="visualizarEnvio(${envio.idEnvio})">
                    <i class="bi bi-eye"></i>
                </button>
        `;

        if (envio.statusId === 2) {
            acoesHtml += `
                <button class="btn btn-light btn-sm text-danger" title="Cancelar Envio" onclick="cancelarEnvio(${envio.idEnvio})">
                    <i class="bi bi-x-circle"></i>
                </button>
            `;
        }
        acoesHtml += `</div>`;

        // Rastreio com link externo estilizado
        let rastreioHtml = envio.codigoRastreio && envio.codigoRastreio !== '-' 
            ? `<a href="#" class="text-decoration-none text-primary fw-semibold">${envio.codigoRastreio} <i class="bi bi-box-arrow-up-right small"></i></a>` 
            : '-';

        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td class="ps-3 fw-bold text-dark">#${envio.idEnvio}</td>
            <td>${formatarDataBR(envio.dataEnvio)}</td>
            <td class="text-truncate" style="max-width: 180px;" title="${origemTexto}">${origemTexto}</td>
            <td class="text-truncate" style="max-width: 180px;" title="${destinoTexto}">${destinoTexto}</td>
            <td><span class="badge bg-light text-dark border">1 produto</span></td>
            <td class="font-monospace">${envio.numeroNota || '-'}</td>
            <td>${envio.transportadora || '-'}</td>
            <td>${rastreioHtml}</td>
            <td>${badgeHtml}</td>
            <td>${envio.responsavel || '-'}</td>
            <td class="text-center pe-3">${acoesHtml}</td>
        `;
        tbody.appendChild(tr);
    });
}

function filtrarEnvios(termo) {
    const termoLower = termo.toLowerCase();
    const filtrados = listaGlobalEnvios.filter(envio => {
        return (
            String(envio.idEnvio).includes(termoLower) ||
            String(envio.origemId).toLowerCase().includes(termoLower) ||
            String(envio.destinoId).toLowerCase().includes(termoLower) ||
            (envio.nomeOrigem && envio.nomeOrigem.toLowerCase().includes(termoLower)) ||
            (envio.nomeDestino && envio.nomeDestino.toLowerCase().includes(termoLower)) ||
            (envio.transportadora && envio.transportadora.toLowerCase().includes(termoLower)) ||
            (envio.numeroNota && envio.numeroNota.toLowerCase().includes(termoLower)) ||
            (envio.codigoRastreio && envio.codigoRastreio.toLowerCase().includes(termoLower)) ||
            (envio.responsavel && envio.responsavel.toLowerCase().includes(termoLower)) ||
            (envio.statusNome && envio.statusNome.toLowerCase().includes(termoLower))
        );
    });
    renderizarTabela(filtrados);
}

function visualizarEnvio(idEnvio) {
    // 1. Encontra o envio na lista global já carregada na tabela
    const envio = listaGlobalEnvios.find(e => e.idEnvio === idEnvio || e.id === idEnvio);

    // 2. Preenche os campos básicos no modal
    document.getElementById("tituloModalDetalhes").innerHTML = `<i class="bi bi-info-circle me-2"></i>ENVIO #${idEnvio}`;
    document.getElementById("detalheOrigem").innerText = envio ? (envio.nomeOrigem || envio.origem || '-') : '-';
    document.getElementById("detalheDestino").innerText = envio ? (envio.nomeDestino || envio.destino || '-') : '-';
    document.getElementById("detalheTransportadora").innerText = envio ? (envio.transportadora || '-') : '-';
    document.getElementById("detalheNota").innerText = envio ? (envio.numeroNota || envio.notaFiscal || '-') : '-';
    
    let rastreio = envio && envio.codigoRastreio && envio.codigoRastreio !== '-' ? envio.codigoRastreio : '-';
    document.getElementById("detalheRastreio").innerHTML = rastreio !== '-' ? `<a href="#" class="text-decoration-none text-primary fw-semibold">${rastreio} <i class="bi bi-box-arrow-up-right small"></i></a>` : '-';

    const tbodyProdutos = document.getElementById("tabelaProdutosDetalhe");
    const listaHistorico = document.getElementById("listaHistoricoEnvio");

    // Abre o modal imediatamente
    const modalElement = document.getElementById('modalDetalhesEnvio');
    if (modalElement) {
        const modalInstance = new bootstrap.Modal(modalElement, { backdrop: 'static', keyboard: true });
        modalInstance.show();
    }

    // 3. Verifica se os produtos já vieram embutidos no objeto
    const produtosEmbutidos = envio ? (envio.produtos || envio.equipamentos || envio.itens || envio.listaEquipamentos) : null;

    if (produtosEmbutidos && produtosEmbutidos.length > 0) {
        preencherTabelaProdutosDetalhe(produtosEmbutidos);
    } else {
        // Se não vieram embutidos, busca os detalhes do envio na API
        tbodyProdutos.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">Carregando produtos...</td></tr>`;

        fetch(`${contextPath}/api/envios/detalhes?idEnvio=${idEnvio}`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
        .then(response => {
            if (!response.ok) throw new Error("Erro ao buscar detalhes do envio.");
            return response.json();
        })
        .then(data => {
            const listaProdutos = data.produtos || data.equipamentos || data.itens || data.listaEquipamentos || [];
            preencherTabelaProdutosDetalhe(listaProdutos);
        })
        .catch(error => {
            console.warn("Rota de detalhes específica não encontrada.", error);
            tbodyProdutos.innerHTML = `<tr><td colspan="4" class="text-center text-danger py-3">Não foi possível carregar os itens vinculados.</td></tr>`;
        });
    }

	// Preenche o histórico dinamicamente baseado nos dados reais do envio com cores dinâmicas
		if (envio && envio.historico && envio.historico.length > 0) {
		    listaHistorico.innerHTML = "";
		    envio.historico.forEach(hist => {
		        let dataHoraFormatada = hist.dataHora ? hist.dataHora.replace('T', ' ') : '-';
		        let nomeStatusHist = hist.statusNome || 'Atualizado';
		        let descricaoHist = hist.observacao || '';
		        
		        // Pega a cor dinâmica que veio do banco (ex: red, blue, green, orange) ou usa cinza como fallback
		        let corStatusHist = hist.statusCor || '#6c757d';
		        
		        listaHistorico.innerHTML += `
		            <li class="mb-2 pb-2 border-bottom d-flex align-items-center">
		                <span class="text-muted fw-semibold me-2">${dataHoraFormatada}</span> 
		                <span class="badge me-2" style="background-color: ${corStatusHist}; color: #fff;">${nomeStatusHist}</span> 
		                <span>${descricaoHist}</span>
		            </li>
		        `;
		    });
		} else {
			    // Monta o histórico padrão para registros antigos, puxando a cor real do status atual do envio
			    let dataFormatada = formatarDataBR(envio ? envio.dataEnvio : null) || '-';
			    let nomeStatus = envio ? (envio.statusNome || 'Enviado') : 'Criado';
			    
			    // Pega a cor do status atual do objeto do envio (ex: o vermelho do cancelado)
			    let corStatusAtual = envio && envio.statusCor ? envio.statusCor : '#6c757d';

			    listaHistorico.innerHTML = `
			        <li class="mb-2 pb-2 border-bottom d-flex align-items-center">
			            <span class="text-muted fw-semibold me-2">${dataFormatada}</span> 
			            <span class="badge me-2" style="background-color: ${corStatusAtual}; color: #fff;">${nomeStatus}</span> 
			            <span>Envio ID #${idEnvio} registrado no sistema.</span>
			        </li>
			    `;
			}
}

// Função de preenchimento adaptada exatamente para as 4 colunas do seu JSP
function preencherTabelaProdutosDetalhe(lista) {
    const tbodyProdutos = document.getElementById("tabelaProdutosDetalhe");
    if (!tbodyProdutos) return;

    if (lista && lista.length > 0) {
        tbodyProdutos.innerHTML = "";
        lista.forEach(eq => {
            let produto = eq.produtoNome || eq.nomeProduto || eq.descricaoProduto || eq.nome 
                        || (eq.produto ? (eq.produto.nome || eq.produto.descricao || eq.produto.nomeProduto) : null) 
                        || (eq.idProduto ? "Produto #" + eq.idProduto : '-');

            tbodyProdutos.innerHTML += `
                <tr>
                    <td class="ps-3 fw-bold">${eq.idSistema || '-'}</td>
                    <td>${eq.patrimonio || '-'}</td>
                    <td>${produto}</td>
                    <td>${eq.numeroSerie || eq.serie || '-'}</td>
                </tr>
            `;
        });
    } else {
        tbodyProdutos.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">Nenhum produto vinculado a este envio.</td></tr>`;
    }
}

function preencherHistoricoPadrao(idEnvio) {
    const listaHistorico = document.getElementById("listaHistoricoEnvio");
    if (!listaHistorico) return;
    
    listaHistorico.innerHTML = `
        <li class="mb-2 pb-2 border-bottom">
            <span class="text-muted fw-semibold ne-2">04/08/2026</span> 
            <span class="badge bg-secondary text-white me-2">Criado</span> Envio ID #${idEnvio} registrado no sistema.
        </li>
    `;
}
async function cancelarEnvio(idEnvio) {
    const confirmado = await ModalService.confirm(
        "Cancelar Envio", 
        "Tem certeza que deseja cancelar este envio? Os equipamentos retornarão para a filial de origem e o status do envio será cancelado."
    );

    if (!confirmado) return;

    const url = `${contextPath}/api/envios?idEnvio=${idEnvio}`;

    fetch(url, {
        method: 'DELETE'
    })
    .then(async response => {
        const text = await response.text();
        try {
            return JSON.parse(text);
        } catch (e) {
            throw new Error("Resposta inválida do servidor: " + text);
        }
    })
    .then(resultado => {
        if (resultado.sucesso) {
            ModalService.success("Sucesso", resultado.mensagem).then(() => {
                carregarEnvios();
            });
        } else {
            ModalService.error("Erro", resultado.mensagem);
        }
    })
    .catch(error => {
        console.error("Erro na requisição de cancelamento:", error);
        ModalService.error("Erro Técnico", "Erro ao cancelar o envio: " + error.message);
    });
}