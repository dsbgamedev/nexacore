let listaGlobalEnvios = [];
let paginaAtual = 1;
const itensPorPagina = 20;

document.addEventListener("DOMContentLoaded", function () {
    carregarEnvios();

    const inputPesquisa = document.getElementById("inputPesquisaGlobal");
    if (inputPesquisa) {
        inputPesquisa.addEventListener("input", function () {
            paginaAtual = 1; // Reseta para a primeira página ao pesquisar
            filtrarEnvios(this.value);
        });
    }
});

// Botão de Atualizar Tela (Listener duplicado removido)
document.getElementById('btnAtualizarTela').addEventListener('click', function() {
    const icone = this.querySelector('i');
    if (icone) icone.classList.add('fa-spin');
    
    // Limpa a busca
    const inputPesquisa = document.getElementById("inputPesquisaGlobal");
    if (inputPesquisa) {
        inputPesquisa.value = '';
    }
    
    // Reseta a paginação e busca dados do servidor
    paginaAtual = 1;
    carregarEnvios(); 
    
    setTimeout(() => {
        if (icone) icone.classList.remove('fa-spin');
    }, 1000);
});

function carregarEnvios() {
    const url = contextPath + '/api/envios';
    console.log("Tentando carregar envios de:", url);

    fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(async response => {
        if (!response.ok) {
            let textoErro = await response.text();
            throw new Error(`Erro HTTP ${response.status}: ${textoErro}`);
        }
        return response.json();
    })
    .then(data => {
        listaGlobalEnvios = data || [];
        paginaAtual = 1;
        renderizarTabela(listaGlobalEnvios);
    })
    .catch(error => {
        console.error("Erro detalhado ao carregar envios:", error);
        
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

// Formatar a data de AAAA-MM-DD para DD/MM/AAAA
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
        atualizarControlesPaginacao(0, 0, 0, 0);
        return;
    }

    // LÓGICA DE PAGINAÇÃO DE 20 EM 20
    const totalItens = dados.length;
    const totalPaginas = Math.ceil(totalItens / itensPorPagina) || 1;
    
    if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
    if (paginaAtual < 1) paginaAtual = 1;

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const dadosPaginados = dados.slice(inicio, fim);

    dadosPaginados.forEach(envio => {
        if (!envio) return;

        let idEnvio = envio.idEnvio || envio.id || 0;
        let origemTexto = envio.nomeOrigem ? envio.nomeOrigem : ('ID: ' + (envio.origemId || '-'));
        let destinoTexto = envio.nomeDestino ? envio.nomeDestino : ('ID: ' + (envio.destinoId || '-'));

        let nomeStatus = envio.statusNome || envio.status || 'Enviado';
        let corStatus = envio.statusCor || '#0d6efd'; 
        
        let badgeHtml = `<span class="badge rounded-pill px-3 py-2" style="background-color: ${corStatus}20; color: ${corStatus}; font-weight: 600;">${nomeStatus}</span>`;

        let listaProdutosEnvio = envio.produtos || envio.equipamentos || envio.itens || envio.listaEquipamentos || [];
        let qtdProdutos = listaProdutosEnvio.length;
        let textoProdutos = qtdProdutos === 1 ? "1 produto" : `${qtdProdutos} produtos`;

        // Botões de Ação
        let acoesHtml = `
            <div class="d-flex justify-content-center gap-1">
                <button class="btn btn-light btn-sm text-secondary" title="Visualizar Detalhes" onclick="visualizarEnvio(${idEnvio})">
                    <i class="fa fa-eye"></i>
                </button>
        `;

        if (envio.statusId === 1 || String(nomeStatus).toLowerCase().includes('aguardando')) {
            acoesHtml += `
                <button class="btn btn-light btn-sm text-success" title="Efetivar Envio (Colocar em Trânsito)" onclick="efetivarEnvio(${idEnvio})">
                    <i class="fa fa-truck-fast"></i>
                </button>
            `;
        }

        if (envio.statusId === 2 || String(nomeStatus).toLowerCase().includes('andamento') || String(nomeStatus).toLowerCase().includes('enviado')) {
            acoesHtml += `
                <button class="btn btn-light btn-sm text-danger" title="Cancelar Envio" onclick="cancelarEnvio(${idEnvio})">
                    <i class="fa fa-xmark"></i>
                </button>
            `;
        }
        acoesHtml += `</div>`;

        let rastreioHtml = envio.codigoRastreio && envio.codigoRastreio !== '-' 
            ? `<a href="#" class="text-decoration-none text-primary fw-semibold">${envio.codigoRastreio} <i class="fa fa-arrow-up-right-from-square small"></i></a>` 
            : '-';

        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td class="ps-3 fw-bold text-dark">#${idEnvio}</td>
            <td>${formatarDataBR(envio.dataEnvio || envio.data)}</td>
            <td class="text-truncate" style="max-width: 180px;" title="${origemTexto}">${origemTexto}</td>
            <td class="text-truncate" style="max-width: 180px;" title="${destinoTexto}">${destinoTexto}</td>
            <td><span class="badge bg-light text-dark border">${textoProdutos}</span></td>
            <td class="font-monospace">${envio.numeroNota || envio.notaFiscal || '-'}</td>
            <td>${envio.transportadora || '-'}</td>
            <td>${rastreioHtml}</td>
            <td>${badgeHtml}</td>
            <td>${envio.responsavel || '-'}</td>
            <td class="text-center pe-3">${acoesHtml}</td>
        `;
        tbody.appendChild(tr);
    });

    // Atualiza os botões e texto da paginação no rodapé
    atualizarControlesPaginacao(totalItens, totalPaginas, inicio, dadosPaginados.length);
}

function atualizarControlesPaginacao(totalItens, totalPaginas, inicio, qtdAtualNaTela) {
    const infoPaginacao = document.getElementById("infoPaginacao");
    const botoesPaginacao = document.getElementById("botoesPaginacao");
    
    if (!infoPaginacao || !botoesPaginacao) return;

    if (totalItens === 0) {
        infoPaginacao.innerText = "Nenhum registro encontrado";
        botoesPaginacao.innerHTML = "";
        return;
    }

    const primeiroExibido = inicio + 1;
    const ultimoExibido = inicio + qtdAtualNaTela;
    infoPaginacao.innerText = `Mostrando de ${primeiroExibido} até ${ultimoExibido} de ${totalItens} registros`;

    let htmlPaginacao = '';
    
    // Botão Anterior
    htmlPaginacao += `
        <li class="page-item ${paginaAtual === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="mudarPagina(${paginaAtual - 1}); return false;">Anterior</a>
        </li>
    `;

    // Botões Numéricos
    for (let i = 1; i <= totalPaginas; i++) {
        htmlPaginacao += `
            <li class="page-item ${paginaAtual === i ? 'active' : ''}">
                <a class="page-link" href="#" onclick="mudarPagina(${i}); return false;">${i}</a>
            </li>
        `;
    }

    // Botão Próximo
    htmlPaginacao += `
        <li class="page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="mudarPagina(${paginaAtual + 1}); return false;">Próximo</a>
        </li>
    `;

    botoesPaginacao.innerHTML = htmlPaginacao;
}

function mudarPagina(novaPagina) {
    paginaAtual = novaPagina;
    const termo = document.getElementById("inputPesquisaGlobal").value;
    if (termo) {
        filtrarEnvios(termo);
    } else {
        renderizarTabela(listaGlobalEnvios);
    }
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
    const envio = listaGlobalEnvios.find(e => e.idEnvio === idEnvio || e.id === idEnvio);

    document.getElementById("tituloModalDetalhes").innerHTML = `<i class="fa fa-info-circle me-2"></i>Detalhes do Envio #${idEnvio}`;
    document.getElementById("detalheOrigem").innerText = envio ? (envio.nomeOrigem || envio.origem || '-') : '-';
    document.getElementById("detalheDestino").innerText = envio ? (envio.nomeDestino || envio.destino || '-') : '-';
    document.getElementById("detalheTransportadora").innerText = envio ? (envio.transportadora || '-') : '-';
    document.getElementById("detalheNota").innerText = envio ? (envio.numeroNota || envio.notaFiscal || '-') : '-';
    
    let rastreio = envio && envio.codigoRastreio && envio.codigoRastreio !== '-' ? envio.codigoRastreio : '-';
    document.getElementById("detalheRastreio").innerHTML = rastreio !== '-' ? `<a href="#" class="text-decoration-none text-primary fw-semibold">${rastreio} <i class="fa fa-arrow-up-right-from-square small"></i></a>` : '-';

    const tbodyProdutos = document.getElementById("tabelaProdutosDetalhe");
    const listaHistorico = document.getElementById("listaHistoricoEnvio");

    const modalElement = document.getElementById('modalDetalhesEnvio');
    if (modalElement) {
        const modalInstance = new bootstrap.Modal(modalElement, { backdrop: 'static', keyboard: true });
        modalInstance.show();
    }

    const produtosEmbutidos = envio ? (envio.produtos || envio.equipamentos || envio.itens || envio.listaEquipamentos) : null;

    if (produtosEmbutidos && produtosEmbutidos.length > 0) {
        preencherTabelaProdutosDetalhe(produtosEmbutidos);
    } else {
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

    if (envio && envio.historico && envio.historico.length > 0) {
        listaHistorico.innerHTML = "";
        envio.historico.forEach(hist => {
            let dataHoraFormatada = hist.dataHora ? hist.dataHora.replace('T', ' ') : '-';
            let nomeStatusHist = hist.statusNome || 'Atualizado';
            let descricaoHist = hist.observacao || '';
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
        let dataFormatada = formatarDataBR(envio ? envio.dataEnvio : null) || '-';
        let nomeStatus = envio ? (envio.statusNome || 'Enviado') : 'Criado';
        let corStatusAtual = envio && envio.statusCor ? envio.statusCor : '#0d6efd';

        listaHistorico.innerHTML = `
            <li class="mb-2 pb-2 border-bottom d-flex align-items-center">
                <span class="text-muted fw-semibold me-2">${dataFormatada}</span> 
                <span class="badge me-2" style="background-color: ${corStatusAtual}; color: #fff;">${nomeStatus}</span> 
                <span>Envio ID #${idEnvio} registrado no sistema.</span>
            </li>
        `;
    }
}

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
            // Como o cancelamento deve ser vermelho, usamos ModalService.error
            ModalService.error("Atenção", resultado.mensagem).then(() => {
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

async function efetivarEnvio(idEnvio) {
    const confirmado = await ModalService.confirm(
        "Efetivar Envio", 
        "Deseja realmente despachar este envio? Os equipamentos passarão para o status 'Em Trânsito'."
    );

    if (!confirmado) return;

    fetch(`${contextPath}/api/envios?idEnvio=${idEnvio}&acao=efetivar`, {
        method: 'PUT'
    })
    .then(res => res.json())
    .then(resposta => {
        if (resposta.sucesso) {
            ModalService.success("Sucesso", resposta.mensagem).then(() => {
                carregarEnvios();
            });
        } else {
            ModalService.error("Erro", resposta.mensagem);
        }
    })
    .catch(err => {
        console.error("Erro:", err);
        ModalService.error("Erro", "Erro de comunicação ao efetivar o envio.");
    });
}