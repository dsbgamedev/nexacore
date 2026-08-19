document.addEventListener("DOMContentLoaded", function() {
    carregarChamados();

    const formFiltro = document.getElementById('formFiltroChamados');
    if (formFiltro) {
        formFiltro.addEventListener('submit', function(e) {
            e.preventDefault();
            carregarChamados();
        });
    }

    // Monitora mudança de status no modal para exibir a opção de reparo se for finalizado
    const selectStatusModal = document.getElementById('modalStatusChamado');
    const blocoReparo = document.getElementById('blocoReparoCheck');
    if (selectStatusModal) {
        selectStatusModal.addEventListener('change', function() {
            if (this.value === 'Finalizado') {
                blocoReparo.style.display = 'block';
            } else {
                blocoReparo.style.display = 'none';
            }
        });
    }
});

function carregarChamados() {
    // Captura os valores dos filtros
    const busca = document.getElementById('filtroBusca').value;
    const status = document.getElementById('filtroStatus').value;
    const tipo = document.getElementById('filtroTipo').value;
    const prioridade = document.getElementById('filtroPrioridade').value;

    // Constrói a URL com os filtros como parâmetros
    const url = new URL(contextPath + '/api/manutencoes/listar', window.location.origin);
    if (busca) url.searchParams.append('busca', busca);
    if (status) url.searchParams.append('status', status);
    if (tipo) url.searchParams.append('tipo', tipo);
    if (prioridade) url.searchParams.append('prioridade', prioridade);

    fetch(url)
        .then(res => res.json())
        .then(lista => {
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
                
                let badgeClass = 'bg-secondary';
                if (statusVal === 'Aberto') badgeClass = 'bg-warning text-dark';
                else if (statusVal === 'Em Atendimento') badgeClass = 'bg-info text-dark';
                else if (statusVal === 'Finalizado') badgeClass = 'bg-success';
                else if (statusVal === 'Cancelado') badgeClass = 'bg-danger';

				let tr = document.createElement('tr');
				    tr.innerHTML = `
				        <td><strong>${idFmt}</strong></td>
				        <td>${chamado.dataAbertura || ''}</td>
				        <td>${chamado.nomeEquipamento || 'EQ-' + chamado.idEquipamento}</td>
				        <td>${chamado.descricaoProblema || ''}</td>
				        <td>${chamado.tipoProblema || 'Corretiva'}</td>
				        <td>${chamado.prioridade || 'Média'}</td>
				        <td><span class="badge ${badgeClass}">${statusVal}</span></td>
				        <td>${chamado.responsavelTecnico || 'Não atribuído'}</td>
				        <td class="text-center">
				            <button class="btn btn-sm btn-outline-primary" onclick='abrirModalGerenciar(${JSON.stringify(chamado)})' title="Visualizar"><i class="fa fa-eye"></i></button>
				            <button class="btn btn-sm btn-outline-warning" onclick="editarChamado(${chamado.idChamado})" title="Editar"><i class="fa fa-pen"></i></button>
				            <button class="btn btn-sm btn-outline-danger" onclick="excluirChamado(${chamado.idChamado})" title="Excluir"><i class="fa fa-trash"></i></button>
				        </td>
				    `;
				    tbody.appendChild(tr);
				});
        })
        .catch(err => {
            console.error("Erro:", err);
            document.getElementById('tabelaChamados').innerHTML = '<tr><td colspan="9" class="text-center text-danger py-3">Erro ao buscar dados. Verifique o servidor.</td></tr>';
        });
}

function abrirModalGerenciar(chamado) {
    document.getElementById('modalIdChamado').value = chamado.idChamado;
    document.getElementById('detalheEquip').innerText = chamado.nomeEquipamento || `Equipamento ID: ${chamado.idEquipamento}`;
    document.getElementById('detalheSolicitante').innerText = chamado.solicitante || '---';
    document.getElementById('detalheDataAbertura').innerText = chamado.dataAbertura || '---';
    document.getElementById('detalheDescricao').innerText = chamado.descricaoProblema || '---';
    
    document.getElementById('modalStatusChamado').value = chamado.idStatusChamado || '1';
    document.getElementById('modalResponsavelTecnico').value = chamado.responsavelTecnico || '';
    document.getElementById('modalDiagnostico').value = chamado.diagnostico || '';
    document.getElementById('modalSolucao').value = chamado.solucaoRealizada || '';

    // Verifica se o chamado já está finalizado (ID 6)
    const isFinalizado = (chamado.idStatusChamado === 6 || chamado.nomeStatus === 'Finalizado');
    
    // Seleciona os campos do modal
    const statusSelect = document.getElementById('modalStatusChamado');
    const tecnicoInput = document.getElementById('modalResponsavelTecnico');
    const diagInput = document.getElementById('modalDiagnostico');
    const solInput = document.getElementById('modalSolucao');
    const checkReparo = document.getElementById('modalFoiReparado');
    const btnSalvar = document.querySelector('#modalGerenciarChamado .btn-primary'); // Botão Salvar Alterações

    if (isFinalizado) {
        // Bloqueia os campos para edição
        statusSelect.disabled = true;
        tecnicoInput.disabled = true;
        diagInput.disabled = true;
        solInput.disabled = true;
        checkReparo.disabled = true;
        if (btnSalvar) btnSalvar.style.display = 'none'; // Oculta o botão salvar se já estiver fechado
        
        document.getElementById('blocoReparoCheck').style.display = 'block';
    } else {
        // Libera os campos caso esteja em aberto/andamento
        statusSelect.disabled = false;
        tecnicoInput.disabled = false;
        diagInput.disabled = false;
        solInput.disabled = false;
        checkReparo.disabled = false;
        if (btnSalvar) btnSalvar.style.display = 'inline-block';
        
        document.getElementById('blocoReparoCheck').style.display = 'none';
    }

    const modal = new bootstrap.Modal(document.getElementById('modalGerenciarChamado'));
    modal.show();
}

function salvarAtualizacaoChamado() {
	const statusId = document.getElementById('modalStatusChamado').value;
	    const solucao = document.getElementById('modalSolucao').value.trim();

	    // ID 6 = Finalizado (ajuste o ID se no seu banco for outro)
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
    document.getElementById('formFiltroChamados').reset();
    carregarChamados();
}

function editarChamado(idChamado) {
    // Faz uma nova busca ou reutiliza os dados para abrir o modal de gerenciamento diretamente
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
    if (confirm("Confirma o cancelamento deste chamado?")) {
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
}