// Armazena os equipamentos selecionados para envio (Chave: idEquipamento, Valor: Objeto do Equipamento)
let equipamentosSelecionadosMap = new Map();

document.addEventListener("DOMContentLoaded", function() {
    // 1. Definir a data de hoje por padrão
    const hoje = new Date().toISOString().split('T')[0];
    const inputDataEnvio = document.getElementById("dataEnvio");
    if (inputDataEnvio) {
        inputDataEnvio.value = hoje;
    }
	
    // Detecta se é o fluxo de devolução com segurança
    const urlParams = new URLSearchParams(window.location.search);
    const tipoParam = (window.isDevolucaoForcada) ? 'devolucao' : urlParams.get('tipo');

    // APENAS SE FOR DEVOLUÇÃO: Força o destino fixo para a Matriz (161) e o desativa
    if (tipoParam === 'devolucao') {
        const selectDestino = document.getElementById("destinoId");
        if (selectDestino) {
            selectDestino.value = "161"; 
            selectDestino.disabled = true;  
            selectDestino.classList.add("bg-light");
        }
    }
	
	// --- GARANTIR O CAMPO RESPONSÁVEL BLOQUEADO E PREENCHIDO ---
    const inputResponsavel = document.getElementById("responsavel");
    if (inputResponsavel) {
        inputResponsavel.readOnly = true;
        inputResponsavel.style.backgroundColor = "#e9ecef";
        inputResponsavel.style.cursor = "not-allowed";
        
        if (!inputResponsavel.value && window.usuarioLogadoNome) {
            inputResponsavel.value = window.usuarioLogadoNome;
        }
    }

	// 2. Carregar o select de Filiais e só depois processar a devolução automática
	carregarFiliais().then(() => {
	    const idEquipamentoDevolucao = (window.isDevolucaoForcada) ? window.idEquipamentoDevolucaoForçado : urlParams.get('idEquipamento');

	    if (tipoParam === 'devolucao' && idEquipamentoDevolucao) {
	        setTimeout(() => {
	            carregarEquipamentoDevolucaoAutomatico(idEquipamentoDevolucao);
	        }, 150);
	    }
	});

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

    // =========================================================================
    // NOVA VALIDAÇÃO EM TEMPO REAL: Dispara assim que altera a Origem ou Destino
    // =========================================================================
    const selectOrigemEl = document.getElementById("origemId");
    const selectDestinoEl = document.getElementById("destinoId");

    function validarOrigemDestinoIgual() {
        if (!selectOrigemEl || !selectDestinoEl) return false;
        
        let origemVal = selectOrigemEl.value;
        let destinoVal = selectDestinoEl.value;

        if (origemVal && destinoVal && origemVal === destinoVal) {
            const msg = "A unidade de origem e a unidade de destino não podem ser iguais. Selecione locais diferentes para realizar a movimentação.";
            if (typeof ModalService !== 'undefined') {
                ModalService.warning("Origem e Destino Iguais", msg);
            } else {
                alert(msg);
            }
            return true;
        }
        return false;
    }

    if (selectOrigemEl) selectOrigemEl.addEventListener("change", validarOrigemDestinoIgual);
    if (selectDestinoEl) selectDestinoEl.addEventListener("change", validarOrigemDestinoIgual);
    // =========================================================================

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
	            let origemIdValor = selectOrigem ? selectOrigem.value : null;

	            const selectDestino = document.getElementById("destinoId");
	            let destinoIdValor = selectDestino ? selectDestino.value : null;

	            // Validação final antes de enviar
	            if (validarOrigemDestinoIgual()) {
	                return; // Interrompe o envio se forem iguais
	            }

	            const payload = {
	                dataEnvio: document.getElementById("dataEnvio").value,
	                origemId: parseInt(origemIdValor),
	                destinoId: parseInt(destinoIdValor),
	                responsavel: document.getElementById("responsavel").value,
	                transportadora: document.getElementById("transportadora").value,
	                codigoRastreio: document.getElementById("codigoRastreio").value,
	                numeroNota: document.getElementById("numeroNota") ? document.getElementById("numeroNota").value : null,
	                dataPrevisaoEntrega: document.getElementById("dataPrevisao").value,
	                observacoes: document.getElementById("observacoes").value,
	                equipamentosIds: Array.from(equipamentosSelecionadosMap.keys())
	            };

	            // Recalcula o tipoParam de forma segura dentro do submit
	            const urlParamsCheck = new URLSearchParams(window.location.search);
	            const tipoAtual = (window.isDevolucaoForcada) ? 'devolucao' : urlParamsCheck.get('tipo');

	            let urlEndpoint = contextPath + '/api/envios';
	            if (tipoAtual === 'devolucao') {
	                urlEndpoint += '?tipo=devolucao';
	            }
	            const btnSubmit = formEnvio.querySelector('button[type="submit"]');
	            if (btnSubmit) btnSubmit.disabled = true;

	            fetch(urlEndpoint, {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
	                body: JSON.stringify(payload)
	            })
	            .then(res => res.json())
	            .then(resposta => {
	                if (resposta.sucesso) {
	                    equipamentosSelecionadosMap.clear();
	                    
	                    if (typeof ModalService !== 'undefined') {
	                        ModalService.success("Sucesso", resposta.mensagem).then(() => {
	                            window.location.href = contextPath + '/ConsultaEnvioServlet';
	                        });
	                    } else {
	                        alert(resposta.mensagem);
	                        window.location.href = contextPath + '/ConsultaEnvioServlet';
	                    }
	                } else {
	                    if (btnSubmit) btnSubmit.disabled = false;
	                    if (typeof ModalService !== 'undefined') {
	                        ModalService.error("Erro", resposta.mensagem);
	                    } else {
	                        alert(resposta.mensagem);
	                    }
	                }
	            })
	            .catch(err => {
	                console.error("Erro:", err);
	                if (btnSubmit) btnSubmit.disabled = false;
	                if (typeof ModalService !== 'undefined') {
	                    ModalService.error("Erro", "Erro de comunicação ao efetuar o envio.");
	                } else {
	                    alert("Erro de comunicação ao efetuar o envio.");
	                }
	            });
	        });
	    }
});

// Função para buscar filiais e popular os selects de origem e destino
function carregarFiliais() {
    return fetch(contextPath + '/api/equipamentos?acaoOrigens=listar-origens')
        .then(res => {
            if (!res.ok) {
                console.warn("A API de origens retornou status " + res.status + ". Usando fallback seguro.");
                return [];
            }
            return res.json();
        })
        .then(data => {
            const selectOrigem = document.getElementById("origemId");
            const selectDestino = document.getElementById("destinoId");
            
            if (!selectOrigem || !selectDestino) return;

            // BLINDAGEM: Garante que 'data' seja tratado como array
            let listaEmpresas = [];
            if (Array.isArray(data)) {
                listaEmpresas = data;
            } else if (data && typeof data === 'object') {
                listaEmpresas = data.content || data.empresas || data.lista || Object.values(data).find(v => Array.isArray(v)) || [];
            }

            // Limpa as opções existentes mantendo apenas a opção padrão "Selecione..."
            selectOrigem.innerHTML = '<option value="">Selecione a origem...</option>';

            // Popula os selects com as filiais permitidas
            listaEmpresas.forEach(filial => {
                let id = filial.origemCodigo || filial.idFilial || filial.id || filial.codigo;
                let codigo = filial.origemCodigo || filial.codigo || id;
                let nome = filial.sufixo || filial.nomeEmpresa || filial.nome || "Filial";
                let texto = codigo + " - " + nome;

                if (id && ![...selectOrigem.options].some(opt => opt.value == id)) {
                    selectOrigem.add(new Option(texto, id));
                }
                if (id && ![...selectDestino.options].some(opt => opt.value == id)) {
                    selectDestino.add(new Option(texto, id));
                }
            });

            // GARANTIA DE SEGURANÇA: Se a Matriz (161) não estiver no destino, injetamos manualmente
            const urlParams = new URLSearchParams(window.location.search);
            const tipoParam = (window.isDevolucaoForcada) ? 'devolucao' : urlParams.get('tipo');

            if (tipoParam === 'devolucao') {
                let matrizExiste = [...selectDestino.options].some(opt => opt.value == "161" || opt.text.includes("161"));
                if (!matrizExiste) {
                    selectDestino.add(new Option("161 - CBA DIESEL SP MATRIZ", "161"));
                }

                for (let i = 0; i < selectDestino.options.length; i++) {
                    let optVal = selectDestino.options[i].value;
                    let optText = selectDestino.options[i].text.toUpperCase();
                    
                    if (optVal == "161" || optText.startsWith("161 -") || optText.includes("161") || optText.includes("SP MATRIZ") || optText.includes("CBA DIESEL SP")) {
                        selectDestino.selectedIndex = i;
                        break;
                    }
                }
                selectDestino.disabled = true;
                selectDestino.style.backgroundColor = "#e9ecef";
                selectDestino.style.cursor = "not-allowed";
                selectDestino.classList.add("bg-light");
            }
        })
        .catch(err => {
            console.error("Erro crítico ao carregar filiais:", err);
            const selectDestino = document.getElementById("destinoId");
            if (selectDestino && selectDestino.options.length <= 1) {
                selectDestino.add(new Option("161 - CBA DIESEL SP MATRIZ", "161"));
                selectDestino.value = "161";
                selectDestino.disabled = true;
                selectDestino.style.backgroundColor = "#e9ecef";
            }
        });
}

// Carrega o equipamento de devolução definindo a Origem na filial atual e o Destino fixo na Matriz
async function carregarEquipamentoDevolucaoAutomatico(idEquipamento) {
    try {
        const resEq = await fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`);
        if (!resEq.ok) return;
        const eq = await resEq.json();

        let filialAtualId = eq.idFilialOrigem || eq.filialId || eq.origemCodigo || (eq.origem && (eq.origem.id || eq.origem.codigo)) || eq.empresaId;
        eq.filialIdPadrao = filialAtualId;
        
        equipamentosSelecionadosMap.set(eq.idEquipamento, eq);
        atualizarTabelaPrincipalItens();

        // 1. Seta e trava a Origem com verificação de segurança caso as options demorem um instante
        const selectOrigem = document.getElementById("origemId");
        if (selectOrigem && filialAtualId) {
            let tentativas = 0;
            const tentarSelecionarOrigem = () => {
                let encontrado = false;
                for (let i = 0; i < selectOrigem.options.length; i++) {
                    let optVal = selectOrigem.options[i].value;
                    let optText = selectOrigem.options[i].text;
                    if (optVal == filialAtualId || optText.startsWith(filialAtualId + " -") || optText.includes(filialAtualId)) {
                        selectOrigem.selectedIndex = i;
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado && tentativas < 5) {
                    tentativas++;
                    setTimeout(tentarSelecionarOrigem, 100);
                    return;
                }

                selectOrigem.disabled = true;
                selectOrigem.style.backgroundColor = "#e9ecef";
                selectOrigem.style.cursor = "not-allowed";
                selectOrigem.classList.add("bg-light");
            };
            tentarSelecionarOrigem();
        }

        // 2. BUSCA E TRAVA O DESTINO FIXO OBRIGATORIAMENTE EM: 161 - CBA DIESEL SP MATRIZ
        const selectDestino = document.getElementById("destinoId");
        if (selectDestino) {
            let encontrado = false;
            for (let i = 0; i < selectDestino.options.length; i++) {
                let optVal = selectDestino.options[i].value;
                let optText = selectDestino.options[i].text.toUpperCase();
                
                if (optVal == "161" || optText.startsWith("161 -") || optText.includes("161") || optText.includes("SP MATRIZ") || optText.includes("CBA DIESEL SP")) {
                    selectDestino.selectedIndex = i;
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                selectDestino.disabled = true;
                selectDestino.style.backgroundColor = "#e9ecef";
                selectDestino.style.cursor = "not-allowed";
                selectDestino.classList.add("bg-light");
            }
        }
    } catch (e) {
        console.error("Erro ao carregar devolução automática:", e);
    }
}

// Função para buscar equipamentos disponíveis para o modal com regra exata de filtragem por tipo
function carregarEquipamentosDisponiveis() {
    const urlParams = new URLSearchParams(window.location.search);
    const tipo = (window.isDevolucaoForcada) ? 'devolucao' : urlParams.get('tipo');
    const idEquipamentoDevolucao = (window.isDevolucaoForcada) ? window.idEquipamentoDevolucaoForçado : urlParams.get('idEquipamento');

    let endpoint = contextPath + '/api/equipamentos';
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
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento encontrado.</td></tr>';
                return;
            }

            const equipamentosValidos = lista.filter(eq => {
                if (tipo === 'devolucao' && idEquipamentoDevolucao) {
                    return eq.idEquipamento == idEquipamentoDevolucao;
                }

                const situacaoTexto = (eq.situacaoAtual || eq.situacaoNome || (eq.situacao && (eq.situacao.nome || eq.situacao.descricao)) || '').toLowerCase();
                const statusMov = (eq.statusMovimentacao || eq.statusAtualMovimentacao || '').toUpperCase();
                const statusTexto = (eq.statusAtual || eq.status || '').toLowerCase();

                if (
                    statusMov.includes("AGUARDANDO") || 
                    statusMov.includes("TRANSITO") || 
                    statusMov.includes("EXTERNO") ||
                    statusTexto.includes("aguardando") ||
                    situacaoTexto.includes("aguardando") ||
                    situacaoTexto.includes("trânsito")
                ) {
                    return false;
                }

                if (tipo === 'devolucao') {
                    return situacaoTexto.includes("devolução") || situacaoTexto.includes("devolucao");
                }

                if (situacaoTexto.includes("devolução") || situacaoTexto.includes("devolucao")) {
                    return false;
                }

                const situacaoId = eq.situacaoId !== undefined ? Number(eq.situacaoId) : (eq.situacao && eq.situacao.id ? Number(eq.situacao.id) : 0);
                return (situacaoId === 1) || (situacaoTexto.includes("disponível") && !situacaoTexto.includes("uso") && !situacaoTexto.includes("reservado"));
            });

            if (equipamentosValidos.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento disponível encontrado para esta operação.</td></tr>';
                return;
            }

            equipamentosValidos.forEach(eq => {
                if (!equipamentosSelecionadosMap.has(eq.idEquipamento)) {
                    let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
                    let produto = eq.produtoNome || eq.nomeProduto || eq.descricaoProduto || eq.nome 
                                || (eq.produto ? (eq.produto.nome || eq.produto.descricao || eq.produto.nomeProduto) : null) 
                                || (eq.idProduto ? "Produto #" + eq.idProduto : '-');

                    let situacaoExibida = eq.situacaoAtual || eq.situacaoNome || (eq.situacao ? (eq.situacao.nome || eq.situacao.descricao) : 'Disponível');
                    let badgeClass = (situacaoExibida.toLowerCase().includes('devolução') || situacaoExibida.toLowerCase().includes('devolucao')) ? 'bg-warning text-dark' : 'bg-success';

                    eq.filialIdPadrao = eq.origemCodigo || eq.idFilialOrigem || eq.filialId || eq.empresaId;

                    let tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="text-center"><input type="checkbox" class="form-check-input check-equip" value="${eq.idEquipamento}" data-json='${JSON.stringify(eq)}'></td>
                        <td>${eq.idSistema || '-'}</td>
                        <td>${eq.patrimonio || '-'}</td>
                        <td>${nomeCpu}</td>
                        <td>${produto}</td>
                        <td>${eq.numeroSerie || '-'}</td>
                        <td><span class="badge ${badgeClass}">${situacaoExibida}</span></td>
                    `;
                    tbody.appendChild(tr);
                }
            });
        })
        .catch(err => console.error("Erro ao carregar equipamentos para seleção:", err));
}

// Atualiza a tabela principal de itens selecionados para envio
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

// Remove item da lista de envio e restaura o status no banco para "Ativo" e situação para "Em Uso"
async function removerItemEnvio(id) {
    const urlParams = new URLSearchParams(window.location.search);
    const tipoParam = (window.isDevolucaoForcada) ? 'devolucao' : urlParams.get('tipo');

    if (tipoParam === 'devolucao') {
        try {
            const response = await fetch(`${contextPath}/api/envios?acao=cancelarDevolucao`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ equipamentosIds: [id] })
            });

            const resposta = await response.json();
            if (!response.ok || !resposta.sucesso) {
                if (typeof ModalService !== 'undefined') {
                    ModalService.error("Erro", resposta.mensagem || "Erro ao reverter status do equipamento.");
                } else {
                    alert(resposta.mensagem || "Erro ao reverter status do equipamento.");
                }
                return;
            }
        } catch (error) {
            console.error("Erro ao cancelar devolução:", error);
            if (typeof ModalService !== 'undefined') {
                ModalService.error("Erro", "Erro de comunicação ao reverter status do equipamento.");
            } else {
                alert("Erro de comunicação ao reverter status do equipamento.");
            }
            return;
        }
    }

    equipamentosSelecionadosMap.delete(id);
    atualizarTabelaPrincipalItens();

    if (equipamentosSelecionadosMap.size === 0) {
        const selectOrigem = document.getElementById("origemId");
        if (selectOrigem) {
            selectOrigem.disabled = false;
            selectOrigem.style.backgroundColor = "";
            selectOrigem.style.cursor = "";
            selectOrigem.value = "";
        }
        
        if (tipoParam !== 'devolucao') {
            const selectDestino = document.getElementById("destinoId");
            if (selectDestino) {
                selectDestino.disabled = false;
                selectDestino.style.backgroundColor = "";
                selectDestino.style.cursor = "";
                selectDestino.value = "";
            }
        }
    }
}