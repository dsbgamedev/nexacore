// Armazena os equipamentos selecionados para envio (Chave: idEquipamento, Valor: Objeto do Equipamento)
let equipamentosSelecionadosMap = new Map();

document.addEventListener("DOMContentLoaded", function() {
    // 1. Definir a data de hoje por padrão (caso exista na tela)
    const hoje = new Date().toISOString().split('T')[0];
    const inputDataEnvio = document.getElementById("dataEnvio");
    if (inputDataEnvio) {
        inputDataEnvio.value = hoje;
    }
	
    // 2. Carregar os selects de Filtros da Tela de Consulta dinamicamente
    carregarFiltroFiliais();
    carregarFiltroDepartamentos();
    carregarFiltroStatus();
    carregarFiltroSituacao();

    // Executa a pesquisa inicial para popular a tabela com todos os registros
    if (typeof pesquisarEquipamentos === 'function') {
        pesquisarEquipamentos();
    }
	
    // 3. Evento do botão que abre o modal de seleção de equipamentos
    const btnAbrirModal = document.getElementById("btnAbrirModalEquipamentos");
    if (btnAbrirModal) {
        btnAbrirModal.addEventListener("click", function() {
            carregarEquipamentosDisponiveis();
            const modalEl = document.getElementById('modalSelecionarEquipamento');
            if (modalEl) {
                new bootstrap.Modal(modalEl).show();
            }
        });
    }

	// 4. Evento do botão de confirmar seleção dentro do modal (Validando pelo idSistema)
	const btnConfirmarSelecao = document.getElementById("btnConfirmarSelecao");
	if (btnConfirmarSelecao) {
	    btnConfirmarSelecao.addEventListener("click", function() {
	        const checks = document.querySelectorAll(".check-equip:checked");
	        let itensDuplicados = [];
	        let primeiraFilialEquipamento = null;

	        if (equipamentosSelecionadosMap.size > 0) {
	            const primeiroItem = Array.from(equipamentosSelecionadosMap.values())[0];
	            primeiraFilialEquipamento = primeiroItem.filialIdPadrao;
	        }

	        checks.forEach(chk => {
	            const eq = JSON.parse(chk.getAttribute("data-json"));
	            
	            let filialEq = eq.origemCodigo || eq.idFilialOrigem || eq.filialId || eq.empresaId || eq.idFilial || eq.idEmpresa;
	            eq.filialIdPadrao = filialEq;

	            if (primeiraFilialEquipamento && filialEq && primeiraFilialEquipamento !== filialEq) {
	                return; 
	            }

	            if (!primeiraFilialEquipamento) {
	                primeiraFilialEquipamento = filialEq;
	            }

	            let jaExiste = Array.from(equipamentosSelecionadosMap.values()).some(
	                item => item.idSistema === eq.idSistema
	            );

	            if (jaExiste) {
	                itensDuplicados.push(eq.idSistema);
	            } else {
	                equipamentosSelecionadosMap.set(eq.idEquipamento, eq);
	            }
	        });

	        if (itensDuplicados.length > 0) {
	            let msg = `O(s) equipamento(s) com o ID de Sistema abaixo já foi(ram) lançado(s) nesta lista de envio:\n\n• ${itensDuplicados.join("\n• ")}`;
	            if (typeof ModalService !== 'undefined') {
	                ModalService.warning("Equipamento já adicionado", msg);
	            } else {
	                alert(msg);
	            }
	        }

	        atualizarTabelaPrincipalItens();

	        const selectOrigem = document.getElementById("origemId");
	        if (selectOrigem && primeiraFilialEquipamento) {
	            for (let i = 0; i < selectOrigem.options.length; i++) {
	                let optText = selectOrigem.options[i].text;
	                let optVal = selectOrigem.options[i].value;
	                
	                if (optVal == primeiraFilialEquipamento || optText.startsWith(primeiraFilialEquipamento + " -") || optText.includes(primeiraFilialEquipamento)) {
	                    selectOrigem.selectedIndex = i;
	                    break;
	                }
	            }
	            
	            if (selectOrigem.value) {
	                selectOrigem.disabled = true;
	                selectOrigem.dispatchEvent(new Event('change'));
	            }
	        }
	        
	        const modalEl = document.getElementById('modalSelecionarEquipamento');
	        const modal = bootstrap.Modal.getInstance(modalEl);
	        if (modal) modal.hide();
	    });
	}

    // 5. Evento de submissão do formulário principal de Envio
	const formEnvio = document.getElementById("formEnvio");
    if (formEnvio) {
        formEnvio.addEventListener("submit", function(e) {
            e.preventDefault();

            if (equipamentosSelecionadosMap.size === 0) {
                if (typeof ModalService !== 'undefined') {
                    ModalService.warning("Atenção", "Adicione pelo menos um equipamento ao envio.");
                } else {
                    alert("Adicione pelo menos um equipamento ao envio.");
                }
                return;
            }

            const selectOrigem = document.getElementById("origemId");
            let origemIdValor = null;
            if (selectOrigem) {
                origemIdValor = selectOrigem.value;
                
                if (!origemIdValor && equipamentosSelecionadosMap.size > 0) {
                    const primeiroEq = Array.from(equipamentosSelecionadosMap.values())[0];
                    origemIdValor = primeiroEq.origemCodigo || primeiroEq.filialId || primeiroEq.idFilial;
                }
            }

            const payload = {
                dataEnvio: document.getElementById("dataEnvio").value,
                origemId: parseInt(origemIdValor),
                destinoId: parseInt(document.getElementById("destinoId").value),
                responsavel: document.getElementById("responsavel").value,
                transportadora: document.getElementById("transportadora").value,
                codigoRastreio: document.getElementById("codigoRastreio").value,
                numeroNota: document.getElementById("numeroNota") ? document.getElementById("numeroNota").value : null,
                dataPrevisaoEntrega: document.getElementById("dataPrevisao").value,
                observacoes: document.getElementById("observacoes").value,
                equipamentosIds: Array.from(equipamentosSelecionadosMap.keys())
            };

            fetch(contextPath + '/api/envios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                body: JSON.stringify(payload)
            })
            .then(res => res.json())
            .then(resposta => {
                if (resposta.sucesso) {
                    if (typeof ModalService !== 'undefined') {
                        ModalService.success("Sucesso", resposta.mensagem).then(() => {
                            window.location.reload();
                        });
                    } else {
                        alert(resposta.mensagem);
                        window.location.reload();
                    }
                } else {
                    if (typeof ModalService !== 'undefined') {
                        ModalService.error("Erro", resposta.mensagem);
                    } else {
                        alert(resposta.mensagem);
                    }
                }
            })
            .catch(err => {
                console.error("Erro:", err);
                if (typeof ModalService !== 'undefined') {
                    ModalService.error("Erro", "Erro de comunicação ao efetuar o envio.");
                } else {
                    alert("Erro de comunicação ao efetuar o envio.");
                }
            });
        });
    }
});

// FUNÇÕES DE CARREGAMENTO DOS FILTROS DA TELA DE CONSULTA
async function carregarFiltroFiliais() {
    try {
        const response = await fetch('/nexacore/api/empresas/');
        if (response.ok) {
            const filiais = await response.json();
            const select = document.getElementById("filtroOrigem");
            if (select) {
                select.innerHTML = '<option value="">Selecione...</option>';
                filiais.forEach(f => {
                    const option = document.createElement("option");
                    option.value = f.origemCodigo;
                    option.textContent = `${f.origemCodigo} - ${f.sufixo || f.nomeEmpresa || ''}`;
                    select.appendChild(option);
                });
            }
        }
    } catch (e) {
        console.error("Erro ao carregar filiais para o filtro:", e);
    }
}

async function carregarFiltroDepartamentos() {
    try {
        const response = await fetch('/nexacore/api/departamentos');
        if (response.ok) {
            const departamentos = await response.json();
            const select = document.getElementById("filtroDepartamento");
            if (select) {
                select.innerHTML = '<option value="">Todos os setores...</option>';
                departamentos.forEach(d => {
                    const option = document.createElement("option");
                    option.value = d.idDepartamento || d.id;
                    option.textContent = d.nomeDepartamento || d.nome;
                    select.appendChild(option);
                });
            }
        }
    } catch (e) {
        console.error("Erro ao carregar departamentos para o filtro:", e);
    }
}

async function carregarFiltroStatus() {
    try {
        const response = await fetch('/nexacore/api/status-equipamento');
        if (response.ok) {
            const listaStatus = await response.json();
            const select = document.getElementById("filtroStatus");
            if (select) {
                select.innerHTML = '<option value="">Selecione...</option>';
                listaStatus.forEach(s => {
                    const option = document.createElement("option");
                    option.value = s.id;
                    option.textContent = s.nome;
                    select.appendChild(option);
                });
            }
        }
    } catch (e) {
        console.error("Erro ao carregar status para o filtro:", e);
    }
}

async function carregarFiltroSituacao() {
    try {
        const response = await fetch('/nexacore/api/equipamentos/?acaoSituacoes=edicao-direta');
        if (response.ok) {
            const listaSituacao = await response.json();
            const select = document.getElementById("filtroSituacao");
            if (select) {
                select.innerHTML = '<option value="">Selecione...</option>';
                listaSituacao.forEach(sit => {
                    const option = document.createElement("option");
                    option.value = sit.id;
                    option.textContent = sit.nome;
                    select.appendChild(option);
                });
            }
        }
    } catch (e) {
        console.error("Erro ao carregar situação para o filtro:", e);
    }
}

// FUNÇÃO DE VISUALIZAÇÃO DE DETALHES (LUPA) CORRIGIDA NO ESCOPO GLOBAL
async function visualizarDetalhesEquipamento(idEquipamento) {
    try {
        const response = await fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`);
        if (!response.ok) throw new Error("Erro ao buscar detalhes no servidor.");

        const eq = await response.json();

        const setText = (id, valor) => {
            const el = document.getElementById(id);
            if (el) el.textContent = valor !== null && valor !== undefined && valor !== '' ? valor : '-';
        };

        setText('det-eq-idsistema', eq.idSistema);
        setText('det-eq-patrimonio', eq.patrimonio);
        setText('det-eq-serie', eq.numeroSerie);
        setText('det-eq-nome', eq.nomeIdentificador);
        
        let origemDetalhe = eq.origemNome || eq.nomeOrigem || eq.filialNome || eq.origemCodigo || '-';
        const selOrigem = document.getElementById('filtroOrigem');
        if (selOrigem && eq.origemCodigo && (!eq.origemNome && !eq.nomeOrigem && !eq.filialNome)) {
            const opt = Array.from(selOrigem.options).find(o => o.value == eq.origemCodigo);
            if (opt && opt.value !== "") origemDetalhe = opt.text;
        }
        setText('det-eq-origem', origemDetalhe);

        let deptoDetalhe = eq.nomeDepartamento || eq.departamentoNome || eq.departamentoId || '-';
        const selDepto = document.getElementById('filtroDepartamento');
        if (selDepto && eq.departamentoId && (!eq.nomeDepartamento && !eq.departamentoNome)) {
            const opt = Array.from(selDepto.options).find(o => o.value == eq.departamentoId);
            if (opt && opt.value !== "") deptoDetalhe = opt.text;
        }
        setText('det-eq-departamento', deptoDetalhe);

        setText('det-eq-ip', eq.ipAtual || 'Não possui IP');
        setText('det-eq-usuario', eq.usuarioAtual);

        const statusTexto = eq.statusAtual || eq.statusNome || eq.statusDescricao || (eq.status && eq.status.nome) || '-';
        setText('det-eq-status', statusTexto);

        const situacaoTexto = eq.situacaoAtual || eq.situacaoNome || eq.situacaoDescricao || (eq.situacao && eq.situacao.nome) || '-';
        setText('det-eq-situacao', situacaoTexto);

        setText('det-eq-observacoes', eq.observacoes || 'Nenhuma observação registrada.');

        setText('det-prod-sku', eq.codigoCatalogo || eq.idProduto);
        setText('det-prod-tipo', eq.nomeTipo);
        setText('det-prod-marca', eq.nomeMarca);
        setText('det-prod-modelo', eq.modelo);
        setText('det-prod-detalhes', eq.descricaoDetalhada);

        const containerImg = document.getElementById('det-container-img');
        const imgProduto = document.getElementById('det-img-produto');
        
        if (containerImg && imgProduto) {
            if (eq.imagemUrl || eq.foto || eq.caminhoImagem) {
                let imgBruta = eq.imagemUrl || eq.foto || eq.caminhoImagem;
                let nomeArquivo = imgBruta.includes('\\') ? imgBruta.substring(imgBruta.lastIndexOf('\\') + 1) : imgBruta.split('/').pop();
                imgProduto.src = `${contextPath}/api/imagens/${nomeArquivo}`;
                containerImg.style.display = 'block';
            } else {
                containerImg.style.display = 'none';
            }
        }

        const modalElement = document.getElementById('modalDetalhesEquipamento');
        if (modalElement) {
            const modalInstance = new bootstrap.Modal(modalElement, { backdrop: 'static', keyboard: true });
            modalInstance.show();
        }
    } catch (error) {
        console.error("Erro ao buscar detalhes do equipamento:", error);
        if (typeof ModalService !== 'undefined' && ModalService.error) {
            ModalService.error("Erro", "Não foi possível carregar os detalhes do equipamento.");
        } else {
            alert("Não foi possível carregar os detalhes do equipamento.");
        }
    }
}

async function pesquisarEquipamentos() {
    const tbody = document.getElementById('tabelaEquipamentosBody');
    if (!tbody) return;

    const getVal = (id) => {
        const el = document.getElementById(id);
        return el ? el.value.trim() : '';
    };

    tbody.innerHTML = '<tr><td colspan="11" class="text-center py-4">Buscando...</td></tr>';

    const params = new URLSearchParams({
        pesquisaGlobal: getVal('busca-global'),
        produto: getVal('filtroProduto'),
        idSistema: getVal('filtroIdSistema'),
        patrimonio: getVal('filtroPatrimonio'),
        serial: getVal('filtroSerial'),
        origem: getVal('filtroOrigem'),
        departamento: getVal('filtroDepartamento'),
        usuario: getVal('filtroUsuario'),
        status: getVal('filtroStatus'),
        situacao: getVal('filtroSituacao')
    });

    try {
        const response = await fetch(`${contextPath}/api/equipamentos?${params.toString()}`);
        
        if (!response.ok) {
            throw new Error(`Erro no servidor: ${response.status}`);
        }

        const data = await response.json();

        tbody.innerHTML = '';
        const badgeTotal = document.getElementById('totalRegistros');

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="11" class="text-center text-muted py-4">Nenhum equipamento encontrado.</td></tr>';
            if (badgeTotal) badgeTotal.textContent = "0 registros encontrados";
            return;
        }

        if (badgeTotal) badgeTotal.textContent = `${data.length} registro(s) encontrado(s)`;

        const selOrigem = document.getElementById('filtroOrigem');
        const selDepto = document.getElementById('filtroDepartamento');

        data.forEach(eq => {
            const statusTexto = eq.statusAtual || eq.statusNome || eq.statusDescricao || (eq.status && eq.status.nome) || 'Ativo';

            const corStatusBanco = (eq.statusCor || (eq.status && eq.status.cor) || '').toLowerCase();
            let badgeStatusClass = "bg-secondary";
            switch (corStatusBanco) {
                case 'green': badgeStatusClass = "bg-success"; break;
                case 'orange': badgeStatusClass = "bg-warning text-dark"; break;
                case 'gray': case 'grey': badgeStatusClass = "bg-secondary text-white"; break;
                case 'black': badgeStatusClass = "bg-dark"; break;
                case 'red': badgeStatusClass = "bg-danger"; break;
                case 'blue': badgeStatusClass = "bg-primary"; break;
                default:
                    if (statusTexto === "Em Manutencao" || statusTexto === "Em Manutenção") badgeStatusClass = "bg-warning text-dark";
                    else if (statusTexto === "Inativo") badgeStatusClass = "bg-secondary text-white";
                    else if (statusTexto === "Baixado") badgeStatusClass = "bg-dark";
                    else if (statusTexto === "Ativo" || statusTexto === "Disponível") badgeStatusClass = "bg-success";
                    break;
            }

            const corSituacaoBanco = (eq.situacaoCor || (eq.situacao && eq.situacao.cor) || '').toLowerCase();
            let badgeSituacaoClass = "bg-info text-dark";
            switch (corSituacaoBanco) {
                case 'green': badgeSituacaoClass = "bg-success"; break;
                case 'orange': badgeSituacaoClass = "bg-warning text-dark"; break;
                case 'red': badgeSituacaoClass = "bg-danger"; break;
                case 'blue': badgeSituacaoClass = "bg-primary"; break;
                case 'black': badgeSituacaoClass = "bg-dark"; break;
                case 'gray': case 'grey': badgeSituacaoClass = "bg-secondary text-white"; break;
                default:
                    badgeSituacaoClass = "bg-info text-dark";
                    break;
            }

            let origemTexto = eq.origemNome || eq.nomeOrigem || eq.filialNome || eq.origemCodigo || '-';
            if (selOrigem && eq.origemCodigo && (!eq.origemNome && !eq.nomeOrigem && !eq.filialNome)) {
                const opt = Array.from(selOrigem.options).find(o => o.value == eq.origemCodigo);
                if (opt && opt.value !== "") origemTexto = opt.text;
            }

            let deptoTexto = eq.nomeDepartamento || eq.departamentoNome || eq.departamentoId || '-';
            if (selDepto && eq.departamentoId && (!eq.nomeDepartamento && !eq.departamentoNome)) {
                const opt = Array.from(selDepto.options).find(o => o.value == eq.departamentoId);
                if (opt && opt.value !== "") deptoTexto = opt.text;
            }

			const situacaoTexto = eq.situacaoAtual || eq.situacaoNome || eq.situacaoDescricao || (eq.situacao && eq.situacao.nome) || '-';
            const sitId = eq.situacaoId !== undefined ? Number(eq.situacaoId) : (eq.situacao ? Number(eq.situacao.id) : 0);

            const eAguardandoEnvio = situacaoTexto.toLowerCase().includes('aguardando envio');
            const bloqueado = (sitId === 3 || sitId === 8 || eAguardandoEnvio);
            const ehDisponivel = (sitId === 1) || (situacaoTexto.toLowerCase().includes('disponível') && !situacaoTexto.toLowerCase().includes('uso'));

            // NOVA REGRA: Verifica se está em manutenção (Situação 6) E se o chamado está aberto (Status ID 1)
            const emManutencaoAssistência = (sitId === 6 || situacaoTexto.toLowerCase().includes('assistência') || situacaoTexto.toLowerCase().includes('manutenção'));
            const chamadoAberto = (eq.idStatusChamado === 1 || eq.statusChamadoId === 1 || eq.statusChamado === 'Aberto');
            const deveOcultarEditar = (emManutencaoAssistência && chamadoAberto);

            let acoesHtml = `
                <button type="button" class="btn btn-sm btn-outline-secondary me-1" title="Visualizar" onclick="visualizarDetalhesEquipamento(${eq.idEquipamento})">
                    <i class="fas fa-search"></i>
                </button>
            `;

            if (bloqueado) {
                acoesHtml += `
                    <span class="badge bg-warning text-dark" title="Operação bloqueada para esta situação">Bloqueado</span>
                `;
            } else {
                // Só exibe o botão de editar se NÃO estiver em manutenção com chamado aberto
                if (!deveOcultarEditar) {
                    acoesHtml += `
                        <a href="${contextPath}/jsp/cadastro-equipamento.jsp?id=${eq.idEquipamento}" class="btn btn-sm btn-outline-primary me-1" title="Editar">
                            <i class="fas fa-pen"></i>
                        </a>
                    `;
                }

                if (ehDisponivel) {
                    acoesHtml += `
                        <button type="button" class="btn btn-sm btn-outline-danger me-1" title="Excluir" onclick="confirmarExclusaoEquipamento(${eq.idEquipamento})">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    `;
                }
            }

            const ID_SITUACAO_EM_USO = 2; 
            const ehEmUso = (sitId === ID_SITUACAO_EM_USO) || (situacaoTexto.toLowerCase().includes('em uso'));
            const codigoOrigemAtual = eq.origemCodigo ? parseInt(eq.origemCodigo) : 0;
            const ehMatriz161 = (codigoOrigemAtual === 161);

            if (ehEmUso && !ehMatriz161) {
                acoesHtml += `
                    <button type="button" class="btn btn-sm btn-outline-warning ms-1" title="Iniciar Devolução" onclick="iniciarDevolucao(${eq.idEquipamento})">
                        <i class="fas fa-undo"></i>
                    </button>
                `;
            }

            const row = `<tr>
                <td class="fw-bold text-primary">${eq.idSistema || '-'}</td>
                <td>${eq.patrimonio || '-'}</td>
                <td>${eq.codigoCatalogo || eq.idProduto || '-'}</td>
                <td>${eq.nomeIdentificador || '-'}</td>
                <td>${origemTexto}</td>
                <td>${eq.usuarioAtual || '-'}</td>
                <td>${deptoTexto}</td>
                <td><span class="badge ${badgeStatusClass}">${statusTexto}</span></td>
                <td><span class="badge ${badgeSituacaoClass}">${situacaoTexto}</span></td>
                <td>${eq.numeroSerie || '-'}</td>
                <td class="text-center" style="white-space: nowrap;">
                    ${acoesHtml}
                </td>
            </tr>`;
            tbody.innerHTML += row;
        });
    } catch (error) {
        console.error("Erro na pesquisa:", error);
        tbody.innerHTML = '<tr><td colspan="11" class="text-center text-danger py-4">Erro de comunicação com o servidor.</td></tr>';
    }
}

function carregarEquipamentosDisponiveis() {
    const urlParams = new URLSearchParams(window.location.search);
    const tipo = urlParams.get('tipo');
    const idEquipamentoDevolucao = urlParams.get('idEquipamento');

    const selectOrigem = document.getElementById("origemId");
    let origemCodigoSelecionada = selectOrigem ? selectOrigem.value : "";

    let endpoint = contextPath + '/api/equipamentos?acao=disponiveis-origem';
    if (origemCodigoSelecionada) {
        endpoint += `&origemCodigo=${origemCodigoSelecionada}`;
    }

    if (tipo === 'devolucao' && idEquipamentoDevolucao) {
        endpoint = `${contextPath}/api/equipamentos?id=${idEquipamentoDevolucao}`;
    }

    fetch(endpoint)
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("tabelaModalEquipamentosBody");
            if (!tbody) return;
            tbody.innerHTML = "";

            const lista = Array.isArray(data) ? data : [data];

            if (!lista || lista.length === 0 || !lista[0]) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento disponível encontrado para esta origem.</td></tr>';
                return;
            }

            const listaApenasDisponiveis = lista.filter(eq => {
                if (tipo === 'devolucao') return true; 

                const sitId = eq.situacaoId !== undefined ? eq.situacaoId : (eq.situacao ? eq.situacao.id : null);
                const sitTexto = (eq.situacaoAtual || eq.situacaoNome || (eq.situacao && eq.situacao.nome) || '').toLowerCase();

                const ehDisponivel = (sitId == 1) || sitTexto.includes("disponível");
                const naoEstaBloqueado = !eq.bloquearOrigem && !eq.origemBloqueada && eq.statusMovimentacao !== 'EM_DESTINO_EXTERNO';

                return ehDisponivel && naoEstaBloqueado;
            });

            if (listaApenasDisponiveis.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento com status Disponível encontrado.</td></tr>';
                return;
            }

            listaApenasDisponiveis.forEach(eq => {
                if (!equipamentosSelecionadosMap.has(eq.idEquipamento)) {
                    let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
                    let produto = eq.produtoNome || eq.nomeProduto || eq.descricaoProduto || eq.nome 
                                || (eq.produto ? (eq.produto.nome || eq.produto.descricao || eq.produto.nomeProduto) : null) 
                                || (eq.idProduto ? "Produto #" + eq.idProduto : '-');

                    let statusBadge = (tipo === 'devolucao') ? 'Em Devolução' : (eq.statusNome || eq.statusAtual || 'Ativo');
                    let badgeClass = (tipo === 'devolucao') ? 'bg-warning text-dark' : 'bg-success';

                    eq.filialIdPadrao = eq.origemCodigo || eq.idFilialOrigem || eq.filialId || eq.empresaId;

                    let tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="text-center"><input type="checkbox" class="form-check-input check-equip" value="${eq.idEquipamento}" data-json='${JSON.stringify(eq)}'></td>
                        <td>${eq.idSistema || '-'}</td>
                        <td>${eq.patrimonio || '-'}</td>
                        <td>${nomeCpu}</td>
                        <td>${produto}</td>
                        <td>${eq.numeroSerie || '-'}</td>
                        <td><span class="badge ${badgeClass}">${statusBadge}</span></td>
                    `;
                    tbody.appendChild(tr);
                }
            });
        })
        .catch(err => console.error("Erro ao carregar equipamentos para seleção:", err));
}

function atualizarTabelaPrincipalItens() {
    const tbody = document.getElementById("corpoTabelaItens");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (equipamentosSelecionadosMap.size === 0) {
        tbody.innerHTML = '<tr id="linhaVazia"><td colspan="7" class="text-center text-muted py-4">Nenhum equipamento adicionado ao envio.</td></tr>';
        return;
    }

    equipamentosSelecionadosMap.forEach((eq, id) => {
        let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
        let produto = eq.produtoNome || eq.nomeProduto || eq.descricaoProduto || eq.nome 
                    || (eq.produto ? (eq.produto.nome || eq.produto.descricao || eq.produto.nomeProduto) : null) 
                    || (eq.idProduto ? "Produto #" + eq.idProduto : '-');

        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${eq.idSistema || '-'}</td>
            <td>${eq.patrimonio || '-'}</td>
            <td>${nomeCpu}</td>
            <td>${produto}</td>
            <td>${eq.numeroSerie || '-'}</td>
            <td><span class="badge bg-warning text-dark">Aguardando Envio</span></td>
            <td class="text-center">
                <button type="button" class="btn btn-outline-danger btn-sm" onclick="removerItemEnvio(${id})">
                    <i class="fa fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function removerItemEnvio(id) {
    equipamentosSelecionadosMap.delete(id);
    atualizarTabelaPrincipalItens();

    if (equipamentosSelecionadosMap.size === 0) {
        const selectOrigem = document.getElementById("origemId");
        if (selectOrigem) {
            selectOrigem.disabled = false;
            selectOrigem.value = "";
        }
    }
}

async function confirmarExclusaoEquipamento(idEquipamento) {
    const confirmado = await ModalService.confirm(
        "Confirmar Inativação", 
        "Deseja realmente inativar este equipamento? Ele sairá da listagem ativa e o histórico será preservado."
    );
    
    if (confirmado) {
        try {
            const response = await fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`, {
                method: 'DELETE'
            });

            const textoResposta = await response.text();
            let resultado = {};
            
            try {
                resultado = textoResposta ? JSON.parse(textoResposta) : {};
            } catch (e) {
                resultado = { mensagem: textoResposta };
            }

            if (!response.ok) {
                throw new Error(resultado.mensagem || "Não foi possível inativar o equipamento.");
            }

            if (typeof pesquisarEquipamentos === 'function') {
                pesquisarEquipamentos();
            } else {
                location.reload();
            }
            
            if (typeof ModalService.success === 'function') {
                ModalService.success("Sucesso", resultado.mensagem || "Equipamento inativado com sucesso.");
            }
        } catch (error) {
            console.error("Erro na inativação:", error);
            if (typeof ModalService !== 'undefined' && ModalService.error) {
                ModalService.error("Atenção", error.message);
            } else {
                alert(error.message);
            }
        }
    }
}

function iniciarDevolucao(idEquipamento) {
    window.location.href = `${contextPath}/jsp/envio-equipamento.jsp?tipo=devolucao&idEquipamento=${idEquipamento}`;
}