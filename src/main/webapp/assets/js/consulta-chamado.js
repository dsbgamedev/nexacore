// Variável global para armazenar todos os chamados carregados
let listaChamadosGlobal = [];

document.addEventListener("DOMContentLoaded", function() {
    // Define a data de hoje nos inputs de data ao carregar a tela
    definirDataAtualNosFiltros();
    
    carregarChamados();

    const formFiltro = document.getElementById('formFiltroChamados');
    if (formFiltro) {
        formFiltro.addEventListener('submit', function(e) {
            e.preventDefault();
            aplicarFiltrosNaTabela();
        });
    }

    // Monitora alterações e digitações nos filtros para atualizar em tempo real
    const camposFiltro = ['filtroBusca', 'filtroStatus', 'filtroTipo', 'filtroPrioridade', 'filtroDataInicio', 'filtroDataFim'];
    camposFiltro.forEach(id => {
        const elemento = document.getElementById(id);
        if (elemento) {
            elemento.addEventListener('input', aplicarFiltrosNaTabela);
            elemento.addEventListener('change', aplicarFiltrosNaTabela);
        }
    });

    // Monitora mudança de status no modal para exibir a opção de reparo se for finalizado
    const selectStatusModal = document.getElementById('modalStatusChamado');
    const blocoReparo = document.getElementById('blocoReparoCheck');
    if (selectStatusModal) {
        selectStatusModal.addEventListener('change', function() {
            if (this.value === '6' || this.value === 'Finalizado') {
                blocoReparo.style.display = 'block';
            } else {
                blocoReparo.style.display = 'none';
            }
        });
    }
});

// Coloca a data atual (ano-mês-dia) nos filtros de período
function definirDataAtualNosFiltros() {
    const hoje = new Date().toISOString().split('T')[0];
    const inputInicio = document.getElementById('filtroDataInicio');
    const inputFim = document.getElementById('filtroDataFim');
    
    if (inputInicio && !inputInicio.value) inputInicio.value = hoje;
    if (inputFim && !inputFim.value) inputFim.value = hoje;
}

function carregarChamados() {
    // Busca todos os chamados do servidor uma única vez
    fetch(contextPath + '/api/manutencoes/listar')
        .then(res => res.json())
        .then(lista => {
            if (Array.isArray(lista)) {
                listaChamadosGlobal = lista;
                aplicarFiltrosNaTabela();
            } else {
                listaChamadosGlobal = [];
                renderizarTabela([]);
            }
        })
        .catch(err => {
            console.error("Erro:", err);
            document.getElementById('tabelaChamados').innerHTML = '<tr><td colspan="9" class="text-center text-danger py-3">Erro ao buscar dados. Verifique o servidor.</td></tr>';
        });
}

// Função que filtra os dados direto na memória do navegador
function aplicarFiltrosNaTabela() {
    const busca = (document.getElementById('filtroBusca').value || '').toLowerCase().trim();
    const statusFiltro = document.getElementById('filtroStatus').value;
    const tipoFiltro = document.getElementById('filtroTipo').value;
    const prioridadeFiltro = document.getElementById('filtroPrioridade').value;
    const dataInicio = document.getElementById('filtroDataInicio').value;
    const dataFim = document.getElementById('filtroDataFim').value;

    const listaFiltrada = listaChamadosGlobal.filter(chamado => {
        let idFmt = 'man-' + String(chamado.idChamado || 0).padStart(6, '0');
        let equip = (chamado.nomeEquipamento || '').toLowerCase();
        let desc = (chamado.descricaoProblema || '').toLowerCase();
        let solicitante = (chamado.solicitante || '').toLowerCase();
        let tecnico = (chamado.responsavelTecnico || '').toLowerCase();

        // Pesquisa geral
        let matchBusca = !busca || 
            idFmt.includes(busca) || 
            String(chamado.idChamado).includes(busca) ||
            equip.includes(busca) || 
            desc.includes(busca) ||
            solicitante.includes(busca) ||
            tecnico.includes(busca);

        // Status
        let statusVal = String(chamado.idStatusChamado || '');
        let matchStatus = !statusFiltro || statusVal === statusFiltro;

        // Tipo
        let tipoVal = chamado.tipoProblema || '';
        let matchTipo = !tipoFiltro || tipoVal.toLowerCase() === tipoFiltro.toLowerCase();

        // Prioridade
        let prioVal = chamado.prioridade || '';
        let matchPrioridade = !prioridadeFiltro || prioVal.toLowerCase() === prioridadeFiltro.toLowerCase();

        // Período de Data: SÓ É APLICADO se o usuário NÃO estiver digitando na Pesquisa Geral
        let matchData = true;
        if (!busca) {
            let dataChamado = chamado.dataAbertura; // Formato esperado vindo do Java: "AAAA-MM-DD"
            if (dataChamado) {
                if (dataInicio && dataChamado < dataInicio) matchData = false;
                if (dataFim && dataChamado > dataFim) matchData = false;
            } else if (dataInicio || dataFim) {
                matchData = false;
            }
        }

        return matchBusca && matchStatus && matchTipo && matchPrioridade && matchData;
    });

    renderizarTabela(listaFiltrada);
}

// Converte "AAAA-MM-DD" para "DD/MM/AAAA"
function formatarDataBR(dataStr) {
    if (!dataStr) return '';
    const partes = dataStr.split('-');
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataStr;
}

// Desenha a tabela com a lista informada
function renderizarTabela(lista) {
    const tbody = document.getElementById('tabelaChamados');
    tbody.innerHTML = '';

    if (!Array.isArray(lista) || lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted py-3">Nenhum chamado encontrado.</td></tr>';
        document.getElementById('contadorRegistros').innerText = '0 registros encontrados';
        return;
    }

    document.getElementById('contadorRegistros').innerText = `${lista.length} registro(s) encontrado(s)`;

    lista.forEach(chamado => {
        let idFmt = 'MAN-' + String(chamado.idChamado || 0).padStart(6, '0');
        let statusVal = chamado.nomeStatus || 'Aberto';
        
        // Verifica se o chamado está Finalizado (6) ou Cancelado (7)
        let isFinalizadoOuCancelado = (chamado.idStatusChamado === 6 || chamado.idStatusChamado === 7 || statusVal === 'Finalizado' || statusVal === 'Cancelado');
        
        let badgeClass = 'bg-secondary';
        if (statusVal === 'Aberto') badgeClass = 'bg-warning text-dark';
        else if (statusVal === 'Em Atendimento' || statusVal === 'Em Análise') badgeClass = 'bg-info text-dark';
        else if (statusVal === 'Finalizado') badgeClass = 'bg-success';
        else if (statusVal === 'Cancelado') badgeClass = 'bg-danger';

        // Oculta botão de excluir/cancelar se já estiver finalizado ou cancelado
        let btnExcluirHtml = isFinalizadoOuCancelado 
            ? '' 
            : `<button class="btn btn-sm btn-outline-danger" onclick="excluirChamado(${chamado.idChamado})" title="Cancelar Chamado"><i class="fa fa-ban"></i></button>`;

        let tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${idFmt}</strong></td>
            <td>${formatarDataBR(chamado.dataAbertura)}</td>
            <td>${chamado.nomeEquipamento || 'EQ-' + chamado.idEquipamento}</td>
            <td>${chamado.descricaoProblema || ''}</td>
            <td>${chamado.tipoProblema || 'Corretiva'}</td>
            <td>${chamado.prioridade || 'Média'}</td>
            <td><span class="badge ${badgeClass}">${statusVal}</span></td>
            <td>${chamado.responsavelTecnico || 'Não atribuído'}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-primary" onclick='abrirModalGerenciar(${JSON.stringify(chamado)})' title="Visualizar"><i class="fa fa-eye"></i></button>
                <button class="btn btn-sm btn-outline-warning" onclick="editarChamado(${chamado.idChamado})" title="Editar"><i class="fa fa-pen"></i></button>
                ${btnExcluirHtml}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function abrirModalGerenciar(chamado) {
    document.getElementById('modalIdChamado').value = chamado.idChamado;
    document.getElementById('detalheEquip').innerText = chamado.nomeEquipamento || `Equipamento ID: ${chamado.idEquipamento}`;
    document.getElementById('detalheSolicitante').innerText = chamado.solicitante || '---';
    document.getElementById('detalheDataAbertura').innerText = formatarDataBR(chamado.dataAbertura) || '---';
    document.getElementById('detalheDescricao').innerText = chamado.descricaoProblema || '---';
    
    const statusAtualId = parseInt(chamado.idStatusChamado || '1');
    document.getElementById('modalStatusChamado').value = statusAtualId;
    document.getElementById('modalResponsavelTecnico').value = chamado.responsavelTecnico || '';
    document.getElementById('modalDiagnostico').value = chamado.diagnostico || '';
    document.getElementById('modalSolucao').value = chamado.solucaoRealizada || '';

    const statusSelect = document.getElementById('modalStatusChamado');
    const tecnicoInput = document.getElementById('modalResponsavelTecnico');
    const diagInput = document.getElementById('modalDiagnostico');
    const solInput = document.getElementById('modalSolucao');
    const checkReparo = document.getElementById('modalFoiReparado');
    const btnSalvar = document.querySelector('#modalGerenciarChamado .btn-primary');

    // MÁQUINA DE ESTADOS: Bloqueia status anteriores (menores que o atual)
    for (let i = 0; i < statusSelect.options.length; i++) {
        let opt = statusSelect.options[i];
        let optVal = parseInt(opt.value);

        if (statusAtualId === 6 || statusAtualId === 7) {
            // Se já estiver finalizado ou cancelado, trava tudo
            opt.disabled = true;
        } else {
            // Bloqueia se o ID for menor que o atual, exceto o status 7 (Cancelado) que pode ser acionado a qualquer momento
            if (optVal < statusAtualId && optVal !== 7) {
                opt.disabled = true;
            } else {
                opt.disabled = false;
            }
        }
    }

    const isFinalizadoOuCancelado = (statusAtualId === 6 || statusAtualId === 7);

    if (isFinalizadoOuCancelado) {
        statusSelect.disabled = true;
        tecnicoInput.disabled = true;
        diagInput.disabled = true;
        solInput.disabled = true;
        checkReparo.disabled = true;
        if (btnSalvar) btnSalvar.style.display = 'none';
        document.getElementById('blocoReparoCheck').style.display = (statusAtualId === 6) ? 'block' : 'none';
    } else {
        statusSelect.disabled = false;
        tecnicoInput.disabled = false;
        diagInput.disabled = false;
        solInput.disabled = false;
        checkReparo.disabled = false;
        if (btnSalvar) btnSalvar.style.display = 'inline-block';
        document.getElementById('blocoReparoCheck').style.display = (statusAtualId === 6) ? 'block' : 'none';
    }

    const modal = new bootstrap.Modal(document.getElementById('modalGerenciarChamado'));
    modal.show();
}

function salvarAtualizacaoChamado() {
    const statusId = document.getElementById('modalStatusChamado').value;
    const solucao = document.getElementById('modalSolucao').value.trim();

    if (statusId === '6' && !solucao) {
        ModalService.error("Atenção", "O campo 'Solução Realizada' é obrigatório para finalizar o chamado.");
        return;
    }

    const payload = {
        idChamado: parseInt(document.getElementById('modalIdChamado').value),
        idStatusChamado: parseInt(statusId),
        responsavelTecnico: document.getElementById('modalResponsavelTecnico').value,
        diagnostico: document.getElementById('modalDiagnostico').value,
        solucaoRealizada: solucao,
        reparado: document.getElementById('modalFoiReparado').checked
    };

    fetch(contextPath + '/api/manutencoes/atualizar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(resposta => {
        if (resposta.sucesso) {
            ModalService.success("Sucesso", resposta.mensagem).then(() => {
                location.reload();
            });
        } else {
            ModalService.error("Atenção", resposta.mensagem);
        }
    })
    .catch(() => {
        ModalService.error("Erro", "Erro de comunicação ao atualizar o chamado.");
    });
}

function limparFiltros() {
    const formFiltro = document.getElementById('formFiltroChamados');
    if (formFiltro) formFiltro.reset();
    
    // Restaura as datas para o dia atual após limpar
    definirDataAtualNosFiltros();
    aplicarFiltrosNaTabela();
}

function editarChamado(idChamado) {
    fetch(contextPath + '/api/manutencoes/listar')
        .then(res => res.json())
        .then(lista => {
            const chamado = lista.find(c => c.idChamado === idChamado);
            if (chamado) {
                abrirModalGerenciar(chamado);
            } else {
                ModalService.error("Atenção", "Chamado não encontrado.");
            }
        })
        .catch(() => {
            ModalService.error("Erro", "Não foi possível carregar os dados do chamado para edição.");
        });
}

function excluirChamado(id) {
    ModalService.confirm("Confirmação", "Deseja realmente cancelar este chamado?").then((confirmado) => {
        if (confirmado) {
            fetch(contextPath + '/api/manutencoes/excluir?id=' + id, { method: 'POST' })
                .then(res => res.json())
                .then(resp => {
                    if (resp.sucesso) {
                        ModalService.success("Sucesso", resp.mensagem).then(() => carregarChamados());
                    } else {
                        ModalService.error("Atenção", resp.mensagem);
                    }
                })
                .catch(() => {
                    ModalService.error("Erro", "Erro de comunicação ao excluir o chamado.");
                });
        }
    });
}