document.addEventListener("DOMContentLoaded", function() {
    carregarFiltrosEquipamento();
    
    const btnPesquisar = document.querySelector('.btn-primary');
    if (btnPesquisar) {
        btnPesquisar.addEventListener('click', (e) => {
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
            if (Array.isArray(origens)) {
                origens.forEach(o => {
                    const id = o.origemCodigo;
                    const sufixo = o.sufixo; // Alterado de nomeEmpresa para sufixo
                    if (id !== undefined && sufixo) {
                        // O texto da opção agora será "161 - ssa"
                        selOrigem.innerHTML += `<option value="${id}">${id} - ${sufixo}</option>`;
                    }
                });
            }
        }

        if (resDepartamentos.ok && selDepartamento) {
            const departamentos = await resDepartamentos.json();
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

    const produto = getVal('filtroProduto');
    const idSistema = getVal('filtroIdSistema');
    const patrimonio = getVal('filtroPatrimonio');
    const serial = getVal('filtroSerial');
    const origem = getVal('filtroOrigem');
    const departamento = getVal('filtroDepartamento');
    const usuario = getVal('filtroUsuario');
    const status = getVal('filtroStatus');

    tbody.innerHTML = '<tr><td colspan="10" class="text-center py-4">Buscando...</td></tr>';

    const params = new URLSearchParams({
        produto: produto,
        idSistema: idSistema,
        patrimonio: patrimonio,
        serial: serial,
        origem: origem,
        departamento: departamento,
        usuario: usuario,
        status: status
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

            // Busca o texto correspondente direto nos selects que já foram populados na tela
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
    document.querySelectorAll('#formFiltroEquipamento input').forEach(el => el.value = '');
    document.querySelectorAll('#formFiltroEquipamento select').forEach(el => el.selectedIndex = 0);
    pesquisarEquipamentos();
}

async function confirmarExclusaoEquipamento(id) {
    const confirmado = await ModalService.confirm(
        "Confirmar Exclusão", 
        "Deseja realmente excluir este equipamento? Esta ação não poderá ser desfeita."
    );
    if (confirmado) {
        // Implementar chamada de exclusão se necessário
    }
}