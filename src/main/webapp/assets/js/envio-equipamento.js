// Armazena os equipamentos selecionados para envio (Chave: idEquipamento, Valor: Objeto do Equipamento)
let equipamentosSelecionadosMap = new Map();

document.addEventListener("DOMContentLoaded", function() {
    // 1. Definir a data de hoje por padrão
    const hoje = new Date().toISOString().split('T')[0];
    const inputDataEnvio = document.getElementById("dataEnvio");
    if (inputDataEnvio) {
        inputDataEnvio.value = hoje;
    }

    // 2. Carregar o select de Filiais (Origem e Destino)
    carregarFiliais();

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
	            
	            // CAPTURA DIRETA DA CHAVE CORRETA ENCONTRADA NO CONSOLE: origemCodigo
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

	        // AUTOMATIZAÇÃO E PREENCHIMENTO DA ORIGEM
	        const selectOrigem = document.getElementById("origemId");
	        if (selectOrigem && primeiraFilialEquipamento) {
	            // Percorre as opções do select para encontrar a que corresponde ao código da origem (ex: "161")
	            for (let i = 0; i < selectOrigem.options.length; i++) {
	                let optText = selectOrigem.options[i].text;
	                let optVal = selectOrigem.options[i].value;
	                
	                if (optVal == primeiraFilialEquipamento || optText.startsWith(primeiraFilialEquipamento + " -") || optText.includes(primeiraFilialEquipamento)) {
	                    selectOrigem.selectedIndex = i;
	                    break;
	                }
	            }
	            
	            if (selectOrigem.value) {
	                selectOrigem.disabled = true; // TRAVA O CAMPO PARA NINGUÉM MEXER
	                selectOrigem.dispatchEvent(new Event('change'));
	            }
	        }
	        
	        // Fechar modal
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

// Função para buscar filiais e popular os selects de origem e destino
function carregarFiliais() {
    fetch(contextPath + '/api/empresas')
        .then(res => res.json())
        .then(data => {
            const selectOrigem = document.getElementById("origemId");
            const selectDestino = document.getElementById("destinoId");
            
            if (!selectOrigem || !selectDestino) return;

            data.forEach(filial => {
                let texto = filial.origemCodigo + " - " + filial.nomeEmpresa;
                selectOrigem.add(new Option(texto, filial.idFilial));
                selectDestino.add(new Option(texto, filial.idFilial));
            });
        })
        .catch(err => console.error("Erro ao carregar filiais:", err));
}

// Função para buscar equipamentos disponíveis para o modal
function carregarEquipamentosDisponiveis() {
    fetch(contextPath + '/api/equipamentos')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("tabelaModalEquipamentosBody");
            if (!tbody) return;
            tbody.innerHTML = "";

            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento disponível encontrado.</td></tr>';
                return;
            }

            const equipamentosValidos = data.filter(eq => {
                const situacao = eq.situacaoId !== undefined ? eq.situacaoId : eq.idSituacao;
                return situacao !== 3 && situacao !== 7;
            });

            if (equipamentosValidos.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento disponível para envio no momento.</td></tr>';
                return;
            }

            equipamentosValidos.forEach(eq => {
                if (!equipamentosSelecionadosMap.has(eq.idEquipamento)) {
                    let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
                    
                    let produto = eq.produtoNome || eq.nomeProduto || eq.descricaoProduto || eq.nome 
                                || (eq.produto ? (eq.produto.nome || eq.produto.descricao || eq.produto.nomeProduto) : null) 
                                || (eq.idProduto ? "Produto #" + eq.idProduto : '-');

                    let statusBadge = eq.statusAtual || eq.status || 'Ativo';

                    // ATRIBUIÇÃO DA PROPRIEDADE CORRETA CAPTURADA DO LOG (`origemCodigo`)
                    eq.filialIdPadrao = eq.origemCodigo || eq.idFilialOrigem || eq.filialId || eq.empresaId;

                    let tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="text-center"><input type="checkbox" class="form-check-input check-equip" value="${eq.idEquipamento}" data-json='${JSON.stringify(eq)}'></td>
                        <td>${eq.idSistema || '-'}</td>
                        <td>${eq.patrimonio || '-'}</td>
                        <td>${nomeCpu}</td>
                        <td>${produto}</td>
                        <td>${eq.numeroSerie || '-'}</td>
                        <td><span class="badge bg-success">${statusBadge}</span></td>
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
            <td><span class="badge bg-warning text-dark">Em Trânsito (Previsto)</span></td>
            <td class="text-center">
                <button type="button" class="btn btn-outline-danger btn-sm" onclick="removerItemEnvio(${id})">
                    <i class="fa fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Remove item da lista de envio
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