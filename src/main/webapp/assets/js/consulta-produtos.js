let produtosGlobais = [];
let colunaAtual = '';
let direcaoAsc = true;

document.addEventListener("DOMContentLoaded", function() {
    carregarFiltros().then(() => {
        // Define o filtro de situação como "Ativo" por padrão ao abrir a página
        const selectAtivo = document.getElementById('filtro-ativo');
        if (selectAtivo) {
            selectAtivo.value = "true";
        }
        
        // Carrega os produtos ativos automaticamente logo na abertura
        pesquisarProdutos();
    });
    
    const btnPesquisar = document.querySelector('.btn-primary');
    if (btnPesquisar) {
        btnPesquisar.addEventListener('click', pesquisarProdutos);
    }

    configurarEventosDinamicos();
});

async function carregarFiltros() {
    const selTipo = document.getElementById('filtro-tipo');
    const selMarca = document.getElementById('filtro-marca');

    if (selTipo) selTipo.innerHTML = '<option value="">Selecione...</option><option value="TODOS">Todos</option>';
    if (selMarca) selMarca.innerHTML = '<option value="">Selecione...</option><option value="TODOS">Todos</option>';

    try {
        const [resTipos, resMarcas] = await Promise.all([
            fetch('/nexacore/api/produtos/listar-tipos'),
            fetch('/nexacore/api/produtos/listar-marcas')
        ]);
        
        const tipos = await resTipos.json();
        const marcas = await resMarcas.json();

        if (selTipo && Array.isArray(tipos)) {
            tipos.forEach(t => {
                const id = t.id || t.tipoId;
                const nome = t.nome || t.descricao;
                if (id !== undefined && nome) {
                    selTipo.innerHTML += `<option value="${id}">${nome}</option>`;
                }
            });
        }
        
        if (selMarca && Array.isArray(marcas)) {
            marcas.forEach(m => {
                const id = m.marcaId || m.idMarca || m.id_marca || m.id;
                const nome = m.nomeMarca || m.nome_marca || m.nome || m.descricao;
                
                if (id !== undefined && nome) {
                    selMarca.innerHTML += `<option value="${id}">${nome}</option>`;
                }
            });
        }
    } catch (error) {
        console.error("Erro ao carregar filtros:", error);
    }
}

function configurarEventosDinamicos() {
    let timeoutBusca = null;
    const idsElementos = ['busca-global', 'filtro-sku', 'filtro-modelo', 'filtro-marca', 'filtro-tipo', 'filtro-ativo'];

    idsElementos.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;

        if (el.tagName === 'INPUT') {
            el.addEventListener('input', () => {
                clearTimeout(timeoutBusca);
                timeoutBusca = setTimeout(() => {
                    pesquisarProdutos();
                }, 400);
            });

            el.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(timeoutBusca);
                    pesquisarProdutos();
                }
            });
        } else if (el.tagName === 'SELECT') {
            el.addEventListener('change', () => {
                pesquisarProdutos();
            });
        }
    });
}

async function pesquisarProdutos() {
    const tbody = document.getElementById('tabela-produtos');
    if (!tbody) return;

    const getVal = (id) => {
        const el = document.getElementById(id);
        return el ? el.value.trim() : '';
    };

    const sku = getVal('filtro-sku');
    const marcaId = getVal('filtro-marca');
    const tipoId = getVal('filtro-tipo');
    const modelo = getVal('filtro-modelo');
    const ativo = getVal('filtro-ativo');
    const buscaGlobal = document.getElementById('busca-global') ? document.getElementById('busca-global').value.trim() : '';

    // Verifica se absolutamente nenhum filtro ou busca global foi preenchido/selecionado
    const algumFiltroPreenchido = sku !== '' || marcaId !== '' || tipoId !== '' || modelo !== '' || ativo !== '' || buscaGlobal !== '';
    
    if (!algumFiltroPreenchido) {
        produtosGlobais = [];
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">Utilize os filtros acima ou a busca global para consultar os produtos.</td></tr>';
        return; // Interrompe a execução para não buscar nada no banco
    }

    tbody.innerHTML = '<tr><td colspan="9" class="text-center">Buscando...</td></tr>';

    const params = new URLSearchParams({
        sku: sku,
        marcaId: marcaId === 'TODOS' ? '' : marcaId,
        tipoId: tipoId === 'TODOS' ? '' : tipoId,
        modelo: modelo,
        ativo: ativo,
        busca: buscaGlobal,
        limite: '15'
    });

    try {
        const response = await fetch(`/nexacore/api/produtos/consultar?${params.toString()}`);
        if (!response.ok) throw new Error(`Erro no servidor: ${response.status}`);

        const data = await response.json();
        produtosGlobais = Array.isArray(data) ? data : [];

        renderizarTabela();
    } catch (error) {
        console.error("Erro na pesquisa:", error);
        ModalService.error("Erro", "Falha ao consultar produtos.");
        tbody.innerHTML = '';
    }
}

function ordenarTabela(coluna) {
    if (colunaAtual === coluna) {
        direcaoAsc = !direcaoAsc; 
    } else {
        colunaAtual = coluna;
        direcaoAsc = true; 
    }

    document.querySelectorAll('thead th i[id^="sort-"]').forEach(icon => {
        icon.className = "fas fa-sort text-muted ms-1";
    });

    const iconeAtual = document.getElementById(`sort-${coluna}`);
    if (iconeAtual) {
        iconeAtual.className = direcaoAsc ? "fas fa-sort-up text-primary ms-1" : "fas fa-sort-down text-primary ms-1";
    }

    produtosGlobais.sort((a, b) => {
        let valA = a[coluna];
        let valB = b[coluna];

        if (valA === undefined || valA === null) valA = '';
        if (valB === undefined || valB === null) valB = '';

        if (typeof valA === 'boolean' || typeof valB === 'boolean') {
            return direcaoAsc ? (valA === valB ? 0 : valA ? -1 : 1) : (valA === valB ? 0 : valA ? 1 : -1);
        }

        let strA = valA.toString().trim();
        let strB = valB.toString().trim();

        // Tratamento inteligente para valores numéricos (como Modelos: 780, 3020, etc.)
        let numA = Number(strA);
        let numB = Number(strB);

        if (!isNaN(numA) && !isNaN(numB)) {
            return direcaoAsc ? numA - numB : numB - numA;
        }

        strA = strA.toLowerCase();
        strB = strB.toLowerCase();

        if (strA < strB) return direcaoAsc ? -1 : 1;
        if (strA > strB) return direcaoAsc ? 1 : -1;
        return 0;
    });

    renderizarTabela();
}

function renderizarTabela() {
    const tbody = document.getElementById('tabela-produtos');
    if (!tbody) return;

    tbody.innerHTML = '';
    if (!produtosGlobais || produtosGlobais.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">Nenhum produto encontrado.</td></tr>';
        return;
    }

    // Limita estritamente a exibição a no máximo 15 registros por página
    const produtosLimitados = produtosGlobais.slice(0, 15);

    produtosLimitados.forEach(p => {
        let botoesAcao = `
            <button type="button" class="btn btn-sm btn-outline-secondary me-1" title="Visualizar" onclick="visualizarDetalhesProduto(${p.id})">
                <i class="fas fa-search"></i>
            </button>`;

        if (typeof usuarioPodeEditar !== 'undefined' && usuarioPodeEditar) {
            botoesAcao += `
                <a href="/nexacore/ProdutoServlet?id=${p.id}" class="btn btn-sm btn-outline-primary me-1" title="Editar">
                    <i class="fas fa-pen"></i>
                </a>`;
        }

        if (typeof usuarioPodeExcluir !== 'undefined' && usuarioPodeExcluir) {
            botoesAcao += `
                <button type="button" class="btn btn-sm btn-outline-danger" title="Excluir" onclick="confirmarExclusaoProduto(${p.id})">
                    <i class="fas fa-trash-alt"></i>
                </button>`;
        }

        const row = `<tr>
            <td>${p.sku || '-'}</td>
            <td>${p.nomeTipo || '-'}</td>
            <td>${p.nomeFabricante || '-'}</td>
            <td>${p.nomeMarca || p.marca || '-'}</td>
            <td>${p.nomeCategoria || '-'}</td>
            <td>${p.modelo || '-'}</td>
            <td>${p.descricaoResumida || '-'}</td>
            <td>
                <span class="badge ${p.ativo ? 'bg-success' : 'bg-danger'}">
                    ${p.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td class="text-center">
                ${botoesAcao}
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function limparFiltros() {
    // Reseta todos os campos de texto para vazio
    document.querySelectorAll('.card .row input').forEach(el => el.value = '');
    
    // Reseta todos os selects para a primeira opção (que é o "Selecione...")
    document.querySelectorAll('.card .row select').forEach(el => el.selectedIndex = 0);
    
    // Limpa também a barra de busca global
    const buscaGlobal = document.getElementById('busca-global');
    if (buscaGlobal) buscaGlobal.value = '';
    
    // Limpa os dados globais e esvazia a tabela visualmente
    produtosGlobais = [];
    const tbody = document.getElementById('tabela-produtos');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">Utilize os filtros acima ou a busca global para consultar os produtos.</td></tr>';
    }
}

async function confirmarExclusaoProduto(id) {
    const confirmado = await ModalService.confirm(
        "Confirmar Exclusão", 
        "Deseja realmente excluir este produto? Esta ação não poderá ser desfeita."
    );
    if (confirmado) {
        executarExclusaoProduto(id);
    }
}

async function executarExclusaoProduto(id) {
    try {
        const response = await fetch(`/nexacore/api/produtos/excluir?id=${id}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        if (response.ok) {
            await ModalService.success("Sucesso", result.mensagem || "Produto excluído com sucesso!");
            pesquisarProdutos();
        } else {
            await ModalService.error("Erro", result.erro || "Não foi possível excluir o produto.");
        }
    } catch (error) {
        console.error("Erro na requisição de exclusão:", error);
        ModalService.error("Erro Técnico", "Falha de comunicação com o servidor.");
    }
}

async function visualizarDetalhesProduto(id) {
    try {
        const response = await fetch(`/nexacore/api/produtos/buscar?id=${id}`);
        if (!response.ok) throw new Error("Erro ao buscar detalhes no servidor.");

        const produto = await response.json();

        let descricaoCompleta = `Marca: ${produto.nomeMarca || produto.marca || '-'}`;
        if (produto.modelo) {
            descricaoCompleta += `, Modelo: ${produto.modelo}`;
        }
        
        if (produto.atributos && produto.atributos.length > 0) {
            produto.atributos.forEach((attr) => {
                let nomeAttr = attr.nomeAtributo || attr.nome_atributo || attr.nome || attr.descricao;
                
                if (!nomeAttr || nomeAttr.trim() === '') {
                    nomeAttr = `Atributo ${attr.idAtributo || ''}`.trim();
                }

                if (nomeAttr.toLowerCase() !== 'modelo') {
                    descricaoCompleta += `, ${nomeAttr}: ${attr.valor || '-'}`;
                }
            });
        }

        const conteudoHtml = `
            <p><strong>Código (SKU):</strong> ${produto.sku || '-'}</p>
            <p><strong>Tipo:</strong> ${produto.nomeTipo || '-'}</p>
			<p><strong>Categoria:</strong> ${produto.nomeCategoria || '-'}</p>      
            <p><strong>Fabricante:</strong> ${produto.nomeFabricante || '-'}</p>    
            <p><strong>Marca:</strong> ${produto.nomeMarca || produto.marca || '-'}</p>
            <p><strong>Modelo:</strong> ${produto.modelo || '-'}</p>
            <p><strong>Descrição Resumida:</strong> ${produto.descricaoResumida || '-'}</p>
            <div class="mb-3">
                <strong>Descrição Detalhada:</strong>
                <p class="text-muted border p-2 rounded bg-light" style="white-space: pre-wrap; word-break: break-word;">${descricaoCompleta}</p>
            </div>
            <p><strong>Situação:</strong> <span class="badge ${produto.ativo ? 'bg-success' : 'bg-danger'}">${produto.ativo ? 'Ativo' : 'Inativo'}</span></p>
        `;

        const detalhesMessage = document.getElementById('detalhesMessage');
        if (detalhesMessage) detalhesMessage.innerHTML = conteudoHtml;

        const modalElement = document.getElementById('detalhesProdutoModal');
        if (modalElement) {
            const modalInstance = new bootstrap.Modal(modalElement, { backdrop: 'static', keyboard: true });
            modalInstance.show();
        }
    } catch (error) {
        console.error("Erro ao buscar detalhes:", error);
        ModalService.error("Erro", "Não foi possível carregar os detalhes do produto.");
    }
}