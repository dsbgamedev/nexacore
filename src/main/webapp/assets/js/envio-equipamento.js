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

    // 4. Evento do botão de confirmar seleção dentro do modal
    const btnConfirmarSelecao = document.getElementById("btnConfirmarSelecao");
    if (btnConfirmarSelecao) {
        btnConfirmarSelecao.addEventListener("click", function() {
            const checks = document.querySelectorAll(".check-equip:checked");
            checks.forEach(chk => {
                const eq = JSON.parse(chk.getAttribute("data-json"));
                equipamentosSelecionadosMap.set(eq.idEquipamento, eq);
            });

            atualizarTabelaPrincipalItens();
            
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
                if (typeof showAlert === 'function') {
                    showAlert("Atenção", "Adicione pelo menos um equipamento ao envio.");
                } else {
                    alert("Adicione pelo menos um equipamento ao envio.");
                }
                return;
            }

            const payload = {
                dataEnvio: document.getElementById("dataEnvio").value,
                origemId: parseInt(document.getElementById("origemId").value),
                destinoId: parseInt(document.getElementById("destinoId").value),
                responsavel: document.getElementById("responsavel").value,
                transportadora: document.getElementById("transportadora").value,
                codigoRastreio: document.getElementById("codigoRastreio").value,
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
                    if (typeof showAlert === 'function') {
                        showAlert("Sucesso", resposta.mensagem, () => {
                            window.location.reload();
                        });
                    } else {
                        alert(resposta.mensagem);
                        window.location.reload();
                    }
                } else {
                    if (typeof showAlert === 'function') {
                        showAlert("Erro", resposta.mensagem);
                    } else {
                        alert(resposta.mensagem);
                    }
                }
            })
            .catch(err => {
                console.error("Erro:", err);
                if (typeof showAlert === 'function') {
                    showAlert("Erro", "Erro de comunicação ao efetuar o envio.");
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

// Função para buscar equipamentos disponíveis (status Ativo) para o modal
function carregarEquipamentosDisponiveis() {
    fetch(contextPath + '/api/equipamentos?status=Ativo')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("tabelaModalEquipamentosBody");
            if (!tbody) return;
            tbody.innerHTML = "";

            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">Nenhum equipamento disponível encontrado.</td></tr>';
                return;
            }

            data.forEach(eq => {
                if (!equipamentosSelecionadosMap.has(eq.idEquipamento)) {
                    // Mapeia possíveis variações vindas do backend
                    let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
                    let produto = eq.produtoNome || eq.nomeProduto || (eq.produto ? eq.produto.nome : '-') || '-';

                    let tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="text-center"><input type="checkbox" class="form-check-input check-equip" value="${eq.idEquipamento}" data-json='${JSON.stringify(eq)}'></td>
                        <td>${eq.idSistema || '-'}</td>
                        <td>${eq.patrimonio || '-'}</td>
                        <td>${nomeCpu}</td>
                        <td>${produto}</td>
                        <td>${eq.numeroSerie || '-'}</td>
                        <td><span class="badge bg-success">${eq.status || 'Ativo'}</span></td>
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
        // Mapeia possíveis variações vindas do backend
        let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '-';
        let produto = eq.produtoNome || eq.nomeProduto || (eq.produto ? eq.produto.nome : '-') || '-';

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
}