/**
 * Arquivo: atributo.js
 * Descrição: Gerenciamento completo da Engenharia de Atributos (Nexacore).
 * Responsabilidades: CRUD de Tipos de Produto, Vinculação de Atributos, 
 *                    Ordenação via Drag-and-Drop e Edição Inline.
 */

// ==========================================================
// 1. INICIALIZAÇÃO E EVENTOS GLOBAIS
// ==========================================================
document.addEventListener("DOMContentLoaded", function() {
    console.log("Nexacore: JavaScript de Atributos carregado com sucesso!");

    // Captura a lista de tipos de produto
    const listaTipos = document.querySelectorAll("#lista-tipos .list-group-item");
    const tituloSelecionado = document.getElementById("nome-tipo-selecionado");
	

    // Adiciona o evento de clique para alternar entre os tipos de produto
    listaTipos.forEach(item => {
        item.addEventListener("click", function(e) {
            e.preventDefault();

            // Remove a classe ativa de todos
            listaTipos.forEach(i => i.classList.remove("active"));

            // Adiciona a classe ativa no item clicado
            this.classList.add("active");

            // Atualiza o título do painel dinamicamente
            const nomeTipo = this.querySelector("span").textContent;
            tituloSelecionado.textContent = nomeTipo;

            // Pega o ID do tipo para enviar para a Servlet futuramente
            const tipoId = this.getAttribute("data-tipo-id");
            console.log("Buscando atributos para o Tipo ID: " + tipoId);
            
            // Aqui faremos a mágica do Fetch API (AJAX) para atualizar a tabela sem atualizar a página
             carregarAtributosDoTipo(tipoId);
        });
    });
});

// ==========================================================
// 2. COMUNICAÇÃO COM SERVIDOR (API/FETCH)
// ==========================================================

/**
 * Busca e renderiza os atributos vinculados ao tipo de produto selecionado.
 * @param {string} tipoId - ID do tipo de produto.
 */
function carregarAtributosDoTipo(tipoId) {
    const tabelaBody = document.querySelector("#tabela-atributos tbody");
    
    tabelaBody.innerHTML = '<tr><td colspan="6" class="text-center">Carregando atributos...</td></tr>';
    
    fetch(`/nexacore/api/atributos?tipoId=${tipoId}`)
        .then(response => {
            if (!response.ok) throw new Error("Erro na rede");
            return response.json();
        })
        .then(data => {
            // 1. Limpa o corpo da tabela antes de começar
            tabelaBody.innerHTML = ""; 

            // 2. Verifica se está vazio
            if (data.length === 0) {
                tabelaBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Nenhum atributo vinculado.</td></tr>';
                return;
            }

            // 3. Itera sobre os dados e renderiza as linhas
            data.forEach((attr) => {
				//log no console
		        console.log("Renderizando linha com DB_ID:", attr.id, "Tipo_ID:", attr.tipoId);
				// Renderização dinâmica da linha da tabela
				tabelaBody.innerHTML += `
				<tr data-id="${attr.id}" data-attr-id="${attr.idAtributoOriginal}" class="drag-item" style="cursor: grab;">
				            <td>${attr.ordem}</td>
				            <td class="nome-cell fw-bold">${attr.nome}</td>
				            <td class="grupo-cell" data-grupo-id="${attr.grupoId || 0}">
				                <span class="badge bg-light text-dark border">${attr.nomeGrupo || 'Sem Grupo'}</span>
				            </td>
				            <td class="tipo-cell"><code>${attr.tipoDado}</code></td>
				            <td class="obrigatorio-cell" data-bool="${attr.obrigatorio}">
				                ${attr.obrigatorio ? '<span class="text-success"><i class="fa-solid fa-circle-check"></i> Sim</span>' : '<span class="text-muted"><i class="fa-solid fa-circle-xmark"></i> Não</span>'}
				            </td>
				            <td class="text-end">
				                <div class="acoes-container"> 
				                    <button class="btn btn-sm btn-link" onclick="habilitarEdicao(this)" title="Editar">
				                        <i class="fa-solid fa-pen-to-square"></i>
				                    </button>
									<button class="btn btn-sm btn-nexacore-delete" onclick="excluirVinculo(this)" title="Desvincular">
									    <i class="fa-solid fa-trash"></i>
									</button>
      			                </div>
				            </td>
				        </tr>
                `;
            });

            // 4. Inicializa o Sortable APÓS criar as linhas
            inicializarSortable();
        })
        .catch(error => {
            console.error("Erro:", error);
            tabelaBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Erro ao carregar dados.</td></tr>';
        });
}

// ==========================================================
// 3. INTERAÇÃO E UI (MODAIS E SORTABLE)
// ==========================================================

/**
 * Configura o SortableJS para permitir reordenamento manual.
 * Atualiza a ordem no banco via POST logo após o drop.
 */
function inicializarSortable() {
    const tbody = document.querySelector("#tabela-atributos tbody");
    
    if (tbody._sortable) {
        tbody._sortable.destroy();
    }

    tbody._sortable = new Sortable(tbody, { // Armazena a instância corretamente
        animation: 150,
        ghostClass: 'bg-light',
        cursor: 'grabbing',
        onEnd: function (evt) {
            const linhas = tbody.querySelectorAll("tr");
            const novaSequencia = [];
            
            linhas.forEach((tr, index) => {
                const id = tr.getAttribute("data-id");
                
                // Apenas adiciona se o ID for válido (não nulo)
                if (id) {
                    novaSequencia.push({
                        id: parseInt(id), // Garante que seja um número para o JSON
                        ordem: index + 1
                    });
                }
            });

            if (novaSequencia.length > 0) {
                fetch('/nexacore/api/atributos/atualizar-ordem', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(novaSequencia)
                })
                .then(response => {
                    if (response.ok) {
                        console.log("Ordem atualizada com sucesso!");
                        const tipoAtivo = document.querySelector("#lista-tipos .active");
                        if (tipoAtivo) carregarAtributosDoTipo(tipoAtivo.getAttribute("data-tipo-id"));
                    } else {
                        // Captura erro do servidor para debug
                        response.text().then(text => console.error("Erro do servidor:", text));
                        alert("Erro ao salvar nova ordem.");
                    }
                })
                .catch(err => console.error("Erro no Sortable:", err));
            }
        }
    });
}
	
function salvarNovaOrdem(sequencia) {
    fetch('/nexacore/api/atributos/atualizar-ordem', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(sequencia)
    })
    .then(response => {
        if (!response.ok) alert("Erro ao salvar nova ordem!");
    });
}

/**
 * Prepara o modal de vínculo. 
 * Bloqueia a abertura caso nenhum tipo esteja selecionado.
 */
const modalVincular = document.getElementById('modalVincularCampo');
if (modalVincular) {
    modalVincular.addEventListener('show.bs.modal', async function (event) {
		// Lógica de validação de estado inicial
        const itemAtivo = document.querySelector("#lista-tipos .active");
        const tipoId = itemAtivo ? itemAtivo.getAttribute("data-tipo-id") : null;
        
        if (!tipoId) {
            // 1. Impede que o modal abra
            event.preventDefault(); 
            
            // 2. Fecha o modal (caso ele tenha tentado abrir)
            const modalInstance = bootstrap.Modal.getInstance(modalVincular);
            if (modalInstance) modalInstance.hide();
            
            // 3. Exibe o aviso padronizado
            await ModalService.alert("Atenção", "Selecione um tipo de produto primeiro!", "warning");
            return;
        }
        
        document.getElementById("modalTipoId").value = tipoId;

	    // Carregamento individual para facilitar a depuração
	    try {
	        // Busca Atributos
	        const resAttr = await fetch('/nexacore/api/atributos/todos');
	        const listaAtributos = await resAttr.json();
	        const selectAttr = document.getElementById("selectAtributos");
	        selectAttr.innerHTML = '<option value="">Escolha um atributo...</option>';
	        listaAtributos.forEach(attr => {
	            selectAttr.innerHTML += `<option value="${attr.id}">${attr.nome}</option>`;
	        });

	        // Busca Grupos
	        const resGrupos = await fetch('/nexacore/api/atributos/listar-grupos');
	        const listaGrupos = await resGrupos.json();
	        const selectGrupo = document.getElementById("grupoSelect");
	        selectGrupo.innerHTML = '<option value="">Escolha um grupo...</option>';
	        listaGrupos.forEach(grupo => {
	            selectGrupo.innerHTML += `<option value="${grupo.id}">${grupo.nome}</option>`;
	        });

	    } catch (err) {
	        console.error("Erro ao carregar dados do modal:", err);
	        alert("Erro ao carregar opções. Verifique o console.");
	    }
	});
}

// ==========================================================
// 4. FUNÇÕES DE CRUD E GERENCIAMENTO DE ATRIBUTOS
// ==========================================================

// --- Vínculos (Atributo <-> Tipo) ---
async function salvarVinculo() {
	const tipoId = document.getElementById("modalTipoId").value;
	    const atributoId = document.getElementById("selectAtributos").value;
	    
	    if (!atributoId) {
	        // Alerta de atenção continua sendo um alerta genérico ou você pode usar um .error
	        await ModalService.error("Atenção", "Por favor, selecione um atributo!");
	        return;
	    }

    let grupoId = document.getElementById("grupoSelect").value || 0;
    const tipoDado = document.getElementById("tipoDado").value;
    const tamanho = document.getElementById("tamanho").value;
    const obrigatorio = document.getElementById("obrigatorio").value;

    const url = `/nexacore/api/atributos/vincular?tipoId=${tipoId}&atributoId=${atributoId}&grupoId=${grupoId}&tipoDado=${tipoDado}&tamanho=${tamanho}&obrigatorio=${obrigatorio}`;
    
	try {
	        const response = await fetch(url, { method: 'POST' });
	        
	        if (response.ok) {
	            // AQUI VOCÊ PODE ESCOLHER: 
	            // Ou mantém o feedback no botão, ou usa o modal de sucesso:
	            ModalService.success("Sucesso", "Atributo vinculado com sucesso!");

	            // Limpa o select e atualiza a tabela
	            document.getElementById("selectAtributos").value = "";
	            carregarAtributosDoTipo(tipoId);
	            
	            // Se quiser fechar o modal automaticamente após o sucesso:
	            const modalVincular = bootstrap.Modal.getInstance(document.getElementById('modalVincularCampo'));
	            if (modalVincular) modalVincular.hide();

	        } else {
	            // Tratamento Profissional de Erros
				if (response.status === 409) {
				    const modalVincular = bootstrap.Modal.getInstance(document.getElementById('modalVincularCampo'));
				    if (modalVincular) modalVincular.hide();
				    
				    const backdrops = document.querySelectorAll('.modal-backdrop');
				    backdrops.forEach(b => b.remove());

				    setTimeout(() => {
				        // AQUI ESTÁ A MUDANÇA PARA O NOVO MÉTODO
				        ModalService.error("Conflito", "Este atributo já está vinculado.");
				    }, 300);
				} else {
	                ModalService.error("Erro", "Ocorreu um erro ao processar o vínculo.");
	            }
	        }
	    } catch (error) {
	        console.error("Erro no fetch:", error);
	        // AQUI TAMBÉM É UM ERRO
	        await ModalService.error("Erro de Conexão", "Não foi possível conectar ao servidor.");
	    }
	}	
	//Função para excluir um vínculo de atributo 
	async function excluirVinculo(botao) {
	    const linha = botao.closest("tr");
	    const idVinculo = linha.getAttribute("data-id");

	    if (!idVinculo || idVinculo === 'undefined' || idVinculo === 'null') {
	        ModalService.error("Erro", "ID do registro não encontrado.");
	        return;
	    }

	    // Chama o modal de confirmação
	    const confirmado = await ModalService.confirm(
	        "Confirmar Exclusão", 
	        "Tem certeza que deseja remover este vínculo?",
	        "error"
	    );

	    // Se o usuário clicar em Confirmar
	    if (confirmado) {
	        try {
	            const response = await fetch(`/nexacore/api/atributos/excluir-vinculo?id=${idVinculo}`, {
	                method: 'DELETE'
	            });

	            if (response.ok) {
	                // Sucesso: recarrega a lista
	                const tipoAtivo = document.querySelector("#lista-tipos .active");
	                if (tipoAtivo) {
	                    carregarAtributosDoTipo(tipoAtivo.getAttribute("data-tipo-id"));
	                }
	                ModalService.success("Sucesso", "Vínculo removido!");
	            } else {
	                ModalService.error("Erro", "Não foi possível remover o vínculo.");
	            }
	        } catch (error) {
	            console.error("Erro:", error);
	            ModalService.error("Erro", "Falha na conexão.");
	        }
	    }
	}
	async function habilitarEdicao(btn) {
	    const tr = btn.closest('tr');
	    // Armazena o HTML atual para poder restaurar depois com precisão
	    const originalHtml = tr.innerHTML;
	    tr.dataset.originalHtml = originalHtml; 

	    // 1. Busca dados (mantido igual)
	    const [resGrupos, resAtribs] = await Promise.all([
	        fetch('/nexacore/api/atributos/listar-grupos').then(r => r.json()),
	        fetch('/nexacore/api/atributos/todos').then(r => r.json())
	    ]);

		// 2. Renderiza os selects
		const tdNome = tr.querySelector('.nome-cell');
		// AQUI ESTÁ O QUE FALTAVA: a criação do select com a classe .edit-attr
		const attrIdAtual = tr.dataset.attrId; // Use o atributo de dados que guarda o ID real
		tdNome.innerHTML = `<select class="form-select form-select-sm edit-attr">` + 
		    resAtribs.map(a => `<option value="${a.id}" ${a.id == attrIdAtual ? 'selected' : ''}>${a.nome}</option>`).join('') + 
		    `</select>`;
	    
	    // Simplificando: vamos focar em editar apenas o Grupo, Tipo e Obrigatório
	    // Editar o nome do atributo via select em uma tabela de vínculo é perigoso para a integridade
	    const tdGrupo = tr.querySelector('.grupo-cell');
	    const grupoIdAtual = tdGrupo.dataset.grupoId;
	    tdGrupo.innerHTML = `<select class="form-select form-select-sm edit-grupo">${resGrupos.map(g => `<option value="${g.id}" ${g.id == grupoIdAtual ? 'selected' : ''}>${g.nome}</option>`).join('')}</select>`;

	    const tdTipo = tr.querySelector('.tipo-cell');
	    const tipoAtual = tdTipo.textContent.trim();
	    tdTipo.innerHTML = `<select class="form-select form-select-sm edit-tipo"><option value="TEXT" ${tipoAtual === 'TEXT' ? 'selected' : ''}>TEXT</option><option value="NUMBER" ${tipoAtual === 'NUMBER' ? 'selected' : ''}>NUMBER</option></select>`;

	    const tdObrig = tr.querySelector('.obrigatorio-cell');
	    const obrigatorioAtual = tdObrig.dataset.bool;
	    tdObrig.innerHTML = `<select class="form-select form-select-sm edit-obrigatorio"><option value="true" ${obrigatorioAtual === 'true' ? 'selected' : ''}>Sim</option><option value="false" ${obrigatorioAtual !== 'true' ? 'selected' : ''}>Não</option></select>`;

	    // 3. Container de Ações
	    const container = tr.querySelector('.acoes-container');
	    container.innerHTML = `
	        <button type="button" class="btn btn-sm btn-link text-success" onclick="salvarEdicao(this)"><i class="fa-solid fa-check"></i></button>
	        <button type="button" class="btn btn-sm btn-link text-danger" onclick="cancelarEdicao(this)"><i class="fa-solid fa-xmark"></i></button>
	    `;
	}
	function cancelarEdicao(btn) {
	    const tr = btn.closest('tr');
	    tr.innerHTML = tr.dataset.originalHtml;
	}

	async function salvarEdicao(btn) {
	    const tr = btn.closest('tr');
	    
	    const attrSelect = tr.querySelector('.edit-attr');
	    const grupoSelect = tr.querySelector('.edit-grupo');
	    const tipoSelect = tr.querySelector('.edit-tipo');
	    const obrigSelect = tr.querySelector('.edit-obrigatorio');

	    const dados = {
	        id: parseInt(tr.getAttribute('data-id')),
	        atributoId: parseInt(attrSelect.value),
	        grupoId: parseInt(grupoSelect.value),
	        tipoDado: tipoSelect.value,
	        tamanho: 255,
	        obrigatorio: obrigSelect.value === 'true'
	    };
		
	    if (!dados.id || isNaN(dados.id)) {
	        await ModalService.error("Erro", "ID do registro inválido.");
	        return;
	    }

	    try {
	        const response = await fetch('/nexacore/api/atributos/editar', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify(dados)
	        });

	        if (response.ok) {
	            // Sucesso padronizado
	            await ModalService.success("Sucesso", "Alteração salva com sucesso!");
	            
	            // Atualiza apenas a tabela atual, sem recarregar a página inteira
	            const tipoAtivo = document.querySelector("#lista-tipos .active");
	            if (tipoAtivo) carregarAtributosDoTipo(tipoAtivo.getAttribute("data-tipo-id"));
	            
	        } else {
	            const msg = await response.text();
	            await ModalService.alert("Erro ao editar", msg, "error");
	        }
	    } catch (error) {
	        console.error("Erro:", error);
	        await ModalService.error("Erro", "Falha de conexão com o servidor.");
	    }
	}
	// Lógica para o botão de exclusão de Tipo de Produto //
	const btnExcluirTipo = document.getElementById("btn-excluir-tipo");

	// Habilita o botão ao clicar em um tipo na lista
	document.addEventListener('click', function(e) {
	    if (e.target.closest('.list-group-item')) {
	        btnExcluirTipo.disabled = false;
	    }
	});

	// Ação de excluir
	btnExcluirTipo.addEventListener("click", async function() {
	    const tipoAtivo = document.querySelector("#lista-tipos .active");
	    if (!tipoAtivo) return;

	    const tipoId = tipoAtivo.getAttribute("data-tipo-id");
	    const nomeTipo = tipoAtivo.querySelector("span").textContent.trim();

	    // 1. Usa o modal de confirmação do seu ModalService
	    const confirmado = await ModalService.confirm(
	        "Confirmar Exclusão", 
	        `Tem certeza que deseja excluir o tipo "${nomeTipo}"? Esta ação não poderá ser desfeita.`,
	        "error"
	    );

	    if (confirmado) {
	        try {
	            const response = await fetch(`/nexacore/api/atributos/excluir-tipo?tipoId=${tipoId}`, { 
	                method: 'DELETE' 
	            });

	            if (response.ok) {
	                // 2. Feedback de sucesso padronizado
	                await ModalService.success("Sucesso", "Tipo excluído com sucesso!");
	                
	                carregarMenuTipos(); 
	                document.getElementById("nome-tipo-selecionado").textContent = "Selecione um tipo";
	                btnExcluirTipo.disabled = true;
	                document.querySelector("#tabela-atributos tbody").innerHTML = "";
	            } else {
	                // 3. Feedback de erro vindo do servidor
	                const msg = await response.text();
	                await ModalService.alert("Erro ao excluir", msg || "Verifique se existem atributos vinculados.", "error");
	            }
	        } catch (err) {
	            console.error("Erro na exclusão:", err);
	            await ModalService.error("Erro", "Falha de conexão com o servidor.");
	        }
	    }
	});

// --- Gerenciamento de Tipos de Produto ---
// Função botao para salvar um novo tipo produto //	
async function salvarTipo() {
    const form = document.getElementById("formNovoTipo");
    // Seleciona o campo especificamente para validar
    const inputNome = form.querySelector('input[name="nome"]'); // Ajuste o 'name' caso o seu seja diferente

    // 1. Validação simples: Bloqueia se estiver vazio ou apenas com espaços
    if (!inputNome.value.trim()) {
        await ModalService.error("Atenção", "O nome do tipo de produto é obrigatório!");
        inputNome.focus(); // Coloca o cursor no campo para facilitar para o usuário
        return; // Para a execução aqui
    }

    const formData = new FormData(form);

    try {
        const response = await fetch('/nexacore/api/atributos/salvar-tipo', {
            method: 'POST',
            body: new URLSearchParams(formData)
        });

        if (response.ok) {
            await ModalService.success("Sucesso", "Tipo de produto cadastrado com sucesso!");
            
            const modalElement = document.getElementById('modalNovoTipo');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) modal.hide();
            
            form.reset();
            carregarMenuTipos(); 
        } else {
            const msg = await response.text();
            await ModalService.alert("Erro", msg || "Erro ao cadastrar tipo.", "error");
        }
    } catch (error) {
        console.error("Erro:", error);
        await ModalService.error("Erro", "Falha de conexão com o servidor.");
    }
}
	
// Função para carregar o menu de tipos de forma dinâmica //
function carregarMenuTipos() {
    fetch('/nexacore/api/atributos/listar-tipos') // Ajuste a URL conforme o seu Servlet
        .then(response => response.json())
        .then(data => {
            const listaTipos = document.getElementById("lista-tipos");
            listaTipos.innerHTML = ""; // Limpa a lista atual

            data.forEach(tipo => {
                listaTipos.innerHTML += `
                    <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" 
                       href="#" data-tipo-id="${tipo.id}">
                        <span><i class="fa-solid fa-tag me-2"></i>${tipo.nome}</span>
                    </a>
                `;
            });
            
            // Re-anexa os eventos de clique após recriar os elementos
            anexarEventosClique(); 
        })
        .catch(error => console.error("Erro ao carregar menu:", error));
}

// Extraímos a lógica de clique para uma função para poder reutilizá-la
function anexarEventosClique() {
    const listaTipos = document.querySelectorAll("#lista-tipos .list-group-item");
    const tituloSelecionado = document.getElementById("nome-tipo-selecionado");

    listaTipos.forEach(item => {
        item.addEventListener("click", function(e) {
            e.preventDefault();
            listaTipos.forEach(i => i.classList.remove("active"));
            this.classList.add("active");
            tituloSelecionado.textContent = this.querySelector("span").textContent;
            carregarAtributosDoTipo(this.getAttribute("data-tipo-id"));
        });
    });
}

// Inicializa quando a página carrega
document.addEventListener("DOMContentLoaded", function() {
    carregarMenuTipos();
    // anexa os eventos inicialmente
    anexarEventosClique(); 
	document.addEventListener('click', function(event) {
	    const isClickInside = event.target.closest('tr');
	    const emEdicao = document.querySelector('.edit-grupo'); // Verifica se há algum select de edição aberto
	    
	    if (emEdicao && !isClickInside) {
	        // Se clicou fora, recarrega a lista do tipo ativo para resetar as linhas
	        const tipoAtivo = document.querySelector("#lista-tipos .active");
	        if (tipoAtivo) carregarAtributosDoTipo(tipoAtivo.getAttribute("data-tipo-id"));
	    }
	});
});

// --- GESTÃO DE ATRIBUTOS ---
// Função para salvar um novo Atributo (Com trava para palavras reservadas)
async function salvarNovoAtributo() {
    const inputNomeAttr = document.getElementById("nomeNovoAtributo");
    const nomeAtributo = inputNomeAttr.value.trim();

    if (!nomeAtributo) {
        await ModalService.error("Erro", "O nome do atributo é obrigatório!");
        return;
    }

    // Normaliza o texto digitado (remove acentos e converte para minúsculo)
    const nomeNormalizado = nomeAtributo.toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g, "");

    // Lista de palavras proibidas por já serem nativas/estruturais do sistema
    const palavrasProibidas = ["marca", "marcas"];

    if (palavrasProibidas.includes(nomeNormalizado)) {
        await ModalService.alert(
            "Atributo Reservado", 
            "O campo 'Marca' já é nativo do produto e gerencia-se automaticamente. Não é permitido criar atributos com este nome.", 
            "error"
        );
        inputNormAttrFocus = inputNomeAttr;
        inputNomeAttr.focus();
        return; // Interrompe o processo e bloqueia o salvamento
    }

    try {
        const response = await fetch(`/nexacore/api/atributos/salvar?nome=${encodeURIComponent(nomeAtributo)}`, {
            method: 'POST'
        });

        if (response.ok) {
            // Sucesso padronizado
            await ModalService.success("Sucesso", "Atributo cadastrado com sucesso!");
            
            const modalElement = document.getElementById('modalNovoAtributo');
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();
            
            inputNomeAttr.value = "";
            
        } else {
            // Mensagem personalizada de duplicidade
            await ModalService.alert(
                "Atributo já cadastrado", 
                "Já existe um atributo com esse nome. Informe um nome diferente.", 
                "error"
            );
        }
    } catch (error) {
        console.error("Erro:", error);
        await ModalService.error("Erro", "Falha de conexão com o servidor.");
    }
}
function abrirModalExclusao() {
    // Altere para a rota correta mapeada no @WebServlet("/api/atributos/*")
    fetch('/nexacore/api/atributos/todos') 
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("lista-atributos-exclusao");
            container.innerHTML = data.map(a => `
                <label class="list-group-item">
                    <input type="checkbox" value="${a.id}" class="me-2"> ${a.nome}
                </label>
            `).join('');
            new bootstrap.Modal(document.getElementById('modalExcluirAtributos')).show();
        })
        .catch(err => console.error("Erro ao buscar atributos:", err));
}
 //Função para confirmar a exclusão em massa (Padronizada)//
async function confirmarExclusaoAtributos() {
    // Captura os checkboxes marcados
    const checkboxes = document.querySelectorAll("#lista-atributos-exclusao input:checked");
    const ids = Array.from(checkboxes).map(i => parseInt(i.value));

    // Validação usando ModalService
    if (ids.length === 0) {
        await ModalService.alert("Atenção", "Selecione pelo menos um atributo para excluir.", "error");
        return;
    }

    try {
        const response = await fetch('/nexacore/api/atributos/excluir-massa', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });

        if (response.ok) {
            // Fecha o modal de exclusão antes de mostrar o sucesso
            const modalElement = document.getElementById('modalExcluirAtributos');
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();

            // Mostra o sucesso e, ao fechar, recarrega a página
            await ModalService.success("Sucesso", "Atributos removidos com sucesso!");
            location.reload();
            
        } 	else {
			  // Agora o servidor envia apenas a mensagem de texto, sem sujeira!
			  const msg = await response.text(); 
			  await ModalService.alert("Atenção", msg, "error");
			}
    } catch (err) {
        console.error("Erro na exclusão:", err);
        await ModalService.alert("Erro", "Falha de conexão com o servidor.", "error");
    }
}
function exibirErroModal(mensagem) {
    document.getElementById("mensagem-erro-modal").innerText = mensagem;
    const modalErro = new bootstrap.Modal(document.getElementById('modalAlertaErro'));
    modalErro.show();
}

