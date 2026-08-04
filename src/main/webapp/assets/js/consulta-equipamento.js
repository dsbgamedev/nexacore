document.addEventListener("DOMContentLoaded", function() {
    carregarFiltrosEquipamento();
    
    const btnPesquisar = document.querySelector('.btn-primary');
    if (btnPesquisar) {
        btnPesquisar.addEventListener('click', (e) => {
            e.preventDefault();
            pesquisarEquipamentos();
        });
    }

    // Evento específico para o botão da lupa da busca global
    const btnBuscaGlobal = document.getElementById('btn-busca-global');
    if (btnBuscaGlobal) {
        btnBuscaGlobal.addEventListener('click', (e) => {
            e.preventDefault();
            pesquisarEquipamentos();
        });
    }

    configurarEventosDinamicosEquipamentos();
});

async function carregarFiltrosEquipamento() {
    const selOrigem = document.getElementById('filtroOrigem');
    const selDepartamento = document.getElementById('filtroDepartamento');
    const selStatus = document.getElementById('filtroStatus');
    const selSituacao = document.getElementById('filtroSituacao'); // Novo select de Situação

    try {
        const [resOrigens, resDepartamentos, resStatus, resSituacao] = await Promise.all([
            fetch(`${contextPath}/api/empresas`),
            fetch(`${contextPath}/api/departamentos`),
            fetch(`${contextPath}/api/status-equipamento`),
            fetch(`${contextPath}/api/situacao-equipamento`) // Endpoint que busca da tabela situacao_equipamento
        ]);

        if (resOrigens.ok && selOrigem) {
            const origens = await resOrigens.json();
            selOrigem.innerHTML = `<option value="">Selecione...</option>`;
            if (Array.isArray(origens)) {
                origens.forEach(o => {
                    const id = o.origemCodigo;
                    const sufixo = o.sufixo;
                    if (id !== undefined && sufixo) {
                        selOrigem.innerHTML += `<option value="${id}">${id} - ${sufixo}</option>`;
                    }
                });
            }
        }

        if (resDepartamentos.ok && selDepartamento) {
            const departamentos = await resDepartamentos.json();
            selDepartamento.innerHTML = `<option value="">Selecione...</option>`;
            if (Array.isArray(departamentos)) {
                departamentos.forEach(d => {
                    const id = d.idDepartamento;
                    const nome = d.nomeDepartamento;
                    if (id !== undefined && nome) {
                        selDepartamento.innerHTML += `<option value="${id}">${nome}</option>`;
                    }
                });
            }
        }

        // Preenchendo o select de Status dinamicamente
        if (resStatus.ok && selStatus) {
            const listaStatus = await resStatus.json();
            selStatus.innerHTML = `<option value="">Selecione...</option>`;
            if (Array.isArray(listaStatus)) {
                listaStatus.forEach(s => {
                    if (s.id !== undefined && s.nome) {
                        selStatus.innerHTML += `<option value="${s.id}">${s.nome}</option>`;
                    }
                });
            }
        }

        // Preenchendo o select de Situação dinamicamente
        if (resSituacao && resSituacao.ok && selSituacao) {
            const listaSituacao = await resSituacao.json();
            selSituacao.innerHTML = `<option value="">Selecione...</option>`;
            if (Array.isArray(listaSituacao)) {
                listaSituacao.forEach(sit => {
                    if (sit.id !== undefined && sit.nome) {
                        selSituacao.innerHTML += `<option value="${sit.id}">${sit.nome}</option>`;
                    }
                });
            }
        }

    } catch (error) {
        console.warn("Aviso: Falha ao carregar selects auxiliares.", error);
    }
}

function configurarEventosDinamicosEquipamentos() {
    let timeoutBusca = null;
    const idsElementos = [
        'busca-global',
        'filtroProduto', 'filtroIdSistema', 'filtroPatrimonio', 
        'filtroSerial', 'filtroOrigem', 'filtroDepartamento', 
        'filtroUsuario', 'filtroStatus', 'filtroSituacao' // Incluído filtroSituacao
    ];

    idsElementos.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;

        if (el.tagName === 'INPUT') {
            el.addEventListener('input', () => {
                clearTimeout(timeoutBusca);
                timeoutBusca = setTimeout(() => {
                    pesquisarEquipamentos();
                }, 400);
            });

            el.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(timeoutBusca);
                    pesquisarEquipamentos();
                }
            });
        } else if (el.tagName === 'SELECT') {
            el.addEventListener('change', () => {
                pesquisarEquipamentos();
            });
        }
    });
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
            // Resolução flexível para o Status
            const statusTexto = eq.statusAtual || eq.statusNome || eq.statusDescricao || (eq.status && eq.status.nome) || 'Ativo';

            // Tratamento de badge para o Status (lendo cor do banco ou mapeando pelo nome)
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
                    else if (statusTexto === "Inativo") badgeStatusClass = "bg-secondary text-white"; // Ajustado para refletir a cor cinza do banco (gray)
                    else if (statusTexto === "Baixado") badgeStatusClass = "bg-dark";
                    else if (statusTexto === "Ativo" || statusTexto === "Disponível") badgeStatusClass = "bg-success";
                    break;
            }

            // Tratamento de badge para a Situação
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

            // Resolução robusta para Origem
            let origemTexto = eq.origemNome || eq.nomeOrigem || eq.filialNome || eq.origemCodigo || '-';
            if (selOrigem && eq.origemCodigo && (!eq.origemNome && !eq.nomeOrigem && !eq.filialNome)) {
                const opt = Array.from(selOrigem.options).find(o => o.value == eq.origemCodigo);
                if (opt && opt.value !== "") origemTexto = opt.text;
            }

            // Resolução robusta para Departamento
            let deptoTexto = eq.nomeDepartamento || eq.departamentoNome || eq.departamentoId || '-';
            if (selDepto && eq.departamentoId && (!eq.nomeDepartamento && !eq.departamentoNome)) {
                const opt = Array.from(selDepto.options).find(o => o.value == eq.departamentoId);
                if (opt && opt.value !== "") deptoTexto = opt.text;
            }

            // Resolução flexível para Situação
            const situacaoTexto = eq.situacaoAtual || eq.situacaoNome || eq.situacaoDescricao || (eq.situacao && eq.situacao.nome) || '-';

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
                    <button type="button" class="btn btn-sm btn-outline-secondary me-1" title="Visualizar" onclick="visualizarDetalhesEquipamento(${eq.idEquipamento})">
                        <i class="fas fa-search"></i>
                    </button>
                    <a href="${contextPath}/jsp/cadastro-equipamento.jsp?id=${eq.idEquipamento}" class="btn btn-sm btn-outline-primary me-1" title="Editar">
                        <i class="fas fa-pen"></i>
                    </a>
                    <button type="button" class="btn btn-sm btn-outline-danger" title="Excluir" onclick="confirmarExclusaoEquipamento(${eq.idEquipamento})">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            </tr>`;
            tbody.innerHTML += row;
        });
    } catch (error) {
        console.error("Erro na pesquisa:", error);
        tbody.innerHTML = '<tr><td colspan="11" class="text-center text-danger py-4">Erro de comunicação com o servidor.</td></tr>';
    }
}
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
        
        // Resolvendo Origem amigável no Modal de Detalhes
        let origemDetalhe = eq.origemNome || eq.nomeOrigem || eq.filialNome || eq.origemCodigo || '-';
        const selOrigem = document.getElementById('filtroOrigem');
        if (selOrigem && eq.origemCodigo && (!eq.origemNome && !eq.nomeOrigem && !eq.filialNome)) {
            const opt = Array.from(selOrigem.options).find(o => o.value == eq.origemCodigo);
            if (opt && opt.value !== "") origemDetalhe = opt.text;
        }
        setText('det-eq-origem', origemDetalhe);

        // Resolvendo Departamento amigável no Modal de Detalhes
        let deptoDetalhe = eq.nomeDepartamento || eq.departamentoNome || eq.departamentoId || '-';
        const selDepto = document.getElementById('filtroDepartamento');
        if (selDepto && eq.departamentoId && (!eq.nomeDepartamento && !eq.departamentoNome)) {
            const opt = Array.from(selDepto.options).find(o => o.value == eq.departamentoId);
            if (opt && opt.value !== "") deptoDetalhe = opt.text;
        }
        setText('det-eq-departamento', deptoDetalhe);

        setText('det-eq-ip', eq.ipAtual || 'Não possui IP');
        setText('det-eq-usuario', eq.usuarioAtual);

        // Tratamento flexível para Status (vê se veio como texto, objeto ou outra propriedade)
        const statusTexto = eq.statusAtual || eq.statusNome || eq.statusDescricao || (eq.status && eq.status.nome) || '-';
        setText('det-eq-status', statusTexto);

        // Tratamento flexível para Situação
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