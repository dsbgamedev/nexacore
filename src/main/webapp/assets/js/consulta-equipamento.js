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

    try {
        const [resOrigens, resDepartamentos] = await Promise.all([
            fetch(`${contextPath}/api/empresas`),
            fetch(`${contextPath}/api/departamentos`)
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
        'filtroUsuario', 'filtroStatus'
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

    tbody.innerHTML = '<tr><td colspan="10" class="text-center py-4">Buscando...</td></tr>';

    const params = new URLSearchParams({
        pesquisaGlobal: getVal('busca-global'),
        produto: getVal('filtroProduto'),
        idSistema: getVal('filtroIdSistema'),
        patrimonio: getVal('filtroPatrimonio'),
        serial: getVal('filtroSerial'),
        origem: getVal('filtroOrigem'),
        departamento: getVal('filtroDepartamento'),
        usuario: getVal('filtroUsuario'),
        status: getVal('filtroStatus')
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
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted py-4">Nenhum equipamento encontrado.</td></tr>';
            if (badgeTotal) badgeTotal.textContent = "0 registros encontrados";
            return;
        }

        if (badgeTotal) badgeTotal.textContent = `${data.length} registro(s) encontrado(s)`;

        data.forEach(eq => {
            let badgeClass = "bg-success";
            if (eq.statusAtual === "Em Manutencao" || eq.statusAtual === "Em Manutenção") badgeClass = "bg-warning text-dark";
            if (eq.statusAtual === "Inativo") badgeClass = "bg-danger";

            const selOrigem = document.getElementById('filtroOrigem');
            const selDepto = document.getElementById('filtroDepartamento');

            let origemTexto = eq.origemCodigo || '-';
            if (selOrigem && eq.origemCodigo) {
                const opt = Array.from(selOrigem.options).find(o => o.value == eq.origemCodigo);
                if (opt) origemTexto = opt.text;
            }

            let deptoTexto = eq.departamentoId || '-';
            if (selDepto && eq.departamentoId) {
                const opt = Array.from(selDepto.options).find(o => o.value == eq.departamentoId);
                if (opt) deptoTexto = opt.text;
            }

            const row = `<tr>
                <td class="fw-bold text-primary">${eq.idSistema || '-'}</td>
                <td>${eq.patrimonio || '-'}</td>
                <td>${eq.codigoCatalogo || eq.idProduto || '-'}</td>
                <td>${eq.nomeIdentificador || '-'}</td>
                <td>${origemTexto}</td>
                <td>${eq.usuarioAtual || '-'}</td>
                <td>${deptoTexto}</td>
                <td><span class="badge ${badgeClass}">${eq.statusAtual || 'Ativo'}</span></td>
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
        tbody.innerHTML = '<tr><td colspan="10" class="text-center text-danger py-4">Erro de comunicação com o servidor.</td></tr>';
    }
}

function limparFiltros() {
    const inputGlobal = document.getElementById('busca-global');
    if (inputGlobal) inputGlobal.value = '';
    
    document.querySelectorAll('#formFiltroEquipamento input').forEach(el => el.value = '');
    document.querySelectorAll('#formFiltroEquipamento select').forEach(el => el.selectedIndex = 0);
    
    const tbody = document.getElementById('tabelaEquipamentosBody');
    const badgeTotal = document.getElementById('totalRegistros');
    
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted py-4">Utilize os filtros acima para consultar os equipamentos.</td></tr>';
    }
    if (badgeTotal) {
        badgeTotal.textContent = "0 registros encontrados";
    }
}

async function confirmarExclusaoEquipamento(idEquipamento) {
    const confirmado = await ModalService.confirm(
        "Confirmar Exclusão", 
        "Deseja realmente excluir este equipamento? Esta ação não poderá ser desfeita."
    );
    
    if (confirmado) {
        try {
            const response = await fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`, {
                method: 'DELETE' // Ou POST dependendo de como seu Servlet trata exclusões
            });

            if (!response.ok) {
                throw new Error("Erro ao excluir o equipamento no servidor.");
            }

            // Se deu certo, atualiza a listagem da tela
            pesquisarEquipamentos();
            
            if (typeof ModalService.success === 'function') {
                ModalService.success("Sucesso", "Equipamento excluído com sucesso.");
            }
        } catch (error) {
            console.error("Erro na exclusão:", error);
            if (typeof ModalService !== 'undefined' && ModalService.error) {
                ModalService.error("Erro", "Não foi possível excluir o equipamento. Verifique se ele possui registros vinculados.");
            } else {
                alert("Não foi possível excluir o equipamento.");
            }
        }
    }
}

async function visualizarDetalhesEquipamento(idEquipamento) {
    try {
        const response = await fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`);
        if (!response.ok) throw new Error("Erro ao buscar detalhes no servidor.");

        const eq = await response.json();

        const setText = (id, valor) => {
            const el = document.getElementById(id);
            if (el) el.textContent = valor !== null && valor !== undefined ? valor : '-';
        };

        setText('det-eq-idsistema', eq.idSistema);
        setText('det-eq-patrimonio', eq.patrimonio);
        setText('det-eq-serie', eq.numeroSerie);
        setText('det-eq-nome', eq.nomeIdentificador);
        setText('det-eq-origem', eq.origemCodigo);
        setText('det-eq-departamento', eq.departamentoId);
        setText('det-eq-ip', eq.ipAtual || 'Não possui IP');
        setText('det-eq-usuario', eq.usuarioAtual);
        setText('det-eq-status', eq.statusAtual);
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
        if (typeof ModalService !== 'undefined') {
            ModalService.error("Erro", "Não foi possível carregar os detalhes do equipamento.");
        } else {
            alert("Não foi possível carregar os detalhes do equipamento.");
        }
    }
}