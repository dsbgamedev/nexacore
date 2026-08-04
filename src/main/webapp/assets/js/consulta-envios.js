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

    fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Erro HTTP: " + response.status);
            }
            return response.json();
        })
        .then(data => {
            listaGlobalEnvios = data || [];
            renderizarTabela(listaGlobalEnvios);
        })
        .catch(error => {
            console.error("Erro ao carregar envios:", error);
            if (typeof ModalService !== 'undefined') {
                ModalService.error("Erro", "Erro ao carregar a lista de envios. Verifique o console.");
            } else {
                alert("Erro ao carregar a lista de envios. Verifique o console.");
            }
        });
}

// Função auxiliar para formatar a data de AAAA-MM-DD para DD/MM/AAAA
function formatarDataBR(dataStr) {
    if (!dataStr) return '';
    // Se a data vier no formato "YYYY-MM-DD"
    let partes = dataStr.split('-');
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataStr; // Retorna original caso venha em outro formato
}

function renderizarTabela(dados) {
    const tbody = document.querySelector("#tabelaEnvios tbody");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!dados || dados.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted py-4">Nenhum envio encontrado.</td></tr>';
        return;
    }

    dados.forEach(envio => {
        let origemTexto = envio.nomeOrigem ? envio.nomeOrigem : ('ID: ' + envio.origemId);
        let destinoTexto = envio.nomeDestino ? envio.nomeDestino : ('ID: ' + envio.destinoId);

        let nomeStatus = envio.statusNome || 'Enviado';
        let corStatus = envio.statusCor || '#0d6efd'; 
        
        let badgeHtml = `<span class="badge" style="background-color: ${corStatus};">${nomeStatus}</span>`;

        let acoesHtml = '';
        if (envio.statusId === 2) {
            acoesHtml = `
                <button class="btn btn-sm btn-danger" title="Cancelar Envio" onclick="cancelarEnvio(${envio.idEnvio})">
                    <i class="fas fa-trash-alt"></i> Cancelar
                </button>
            `;
        } else {
            acoesHtml = `<span class="text-muted small fst-italic">Finalizado / Cancelado</span>`;
        }

        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td>#${envio.idEnvio}</td>
            <td>${formatarDataBR(envio.dataEnvio)}</td> <!-- Formatando a data aqui -->
            <td>${origemTexto}</td>
            <td>${destinoTexto}</td>
            <td>${badgeHtml}</td>
            <td>${envio.transportadora || '-'}</td>
            <td>${envio.codigoRastreio || '-'}</td>
            <td>${envio.responsavel || '-'}</td>
            <td class="text-center">
                ${acoesHtml}
            </td>
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
            (envio.codigoRastreio && envio.codigoRastreio.toLowerCase().includes(termoLower)) ||
            (envio.responsavel && envio.responsavel.toLowerCase().includes(termoLower)) ||
            (envio.statusNome && envio.statusNome.toLowerCase().includes(termoLower))
        );
    });
    renderizarTabela(filtrados);
}

async function confirmarBaixa(idEnvio, destinoId) {
    const confirmado = await ModalService.confirm(
        "Confirmar Baixa", 
        "Confirma o recebimento deste envio? Os equipamentos serão atualizados para a nova filial e voltarão a ficar disponíveis."
    );

    if (!confirmado) return;

    const url = `${contextPath}/api/envios?idEnvio=${idEnvio}&destinoId=${destinoId}`;

    fetch(url, {
        method: 'PUT'
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
        console.error("Erro na requisição de baixa:", error);
        ModalService.error("Erro Técnico", "Erro ao processar a baixa: " + error.message);
    });
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