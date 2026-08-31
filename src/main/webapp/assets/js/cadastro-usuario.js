document.addEventListener("DOMContentLoaded", function () {
    console.log("NexaCore: Inicializando script de cadastro de usuário...");

	// 1. Tenta carregar as unidades dinamicamente caso venham via JSON do Servlet
	    carregarUnidadesDinamicas();

	    // 1.1 Preenche os dados caso seja edição
	    preencherDadosEdicao();

	    // 2. Inicializa contadores e eventos
	    atualizarContadorUnidades();

    const checkboxes = document.querySelectorAll('.unidade-checkbox');
    checkboxes.forEach(cb => {
        cb.addEventListener('change', atualizarContadorUnidades);
    });

	// ==========================================
    // 3. INTERCEPTAÇÃO DO SUBMIT DO FORMULÁRIO
    // ==========================================
    const form = document.querySelector("form"); // Ajuste o seletor se necessário (ex: #formCadastroUsuario)
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault(); // Impede o envio tradicional (recarregar a página)

            // Captura os dados básicos dos inputs do formulário
            const idInput = form.querySelector("input[name='id']");
            const id = idInput ? idInput.value : "";
            
            const actionInput = form.querySelector("input[name='action']");
            const action = actionInput ? actionInput.value : (id ? "editar" : "cadastrar");

            const username = form.querySelector("input[name='usuario']") ? form.querySelector("input[name='usuario']").value : "";
			const nomeCompleto = form.querySelector("input[name='nome']") ? form.querySelector("input[name='nome']").value : ""; // <-- AJUSTADO AQUI
			const email = form.querySelector("input[name='email']") ? form.querySelector("input[name='email']").value : "";
            const senha = form.querySelector("input[name='senha']") ? form.querySelector("input[name='senha']").value : "";
            
            const selectPerfil = form.querySelector("select[name='perfil']");
            const perfil = selectPerfil ? selectPerfil.value : "";
            
            const selectFilial = form.querySelector("select[name='filial']");
            const unidadePadrao = selectFilial ? selectFilial.value : "";
            
            const ativoCheckbox = form.querySelector("input[name='ativo']");
            const ativo = ativoCheckbox ? ativoCheckbox.checked : false;

			// Captura a matriz granular de módulos da tela
            const modulosPermitidos = [];
            const linhasModulos = form.querySelectorAll(".modulo-row");
            
            linhasModulos.forEach(linha => {
                const moduloIdInput = linha.querySelector(".modulo-id");
                if (moduloIdInput) {
                    modulosPermitidos.push({
                        id: parseInt(moduloIdInput.value),
                        consultar: linha.querySelector(".check-consultar").checked,
                        inserir: linha.querySelector(".check-inserir").checked,
                        editar: linha.querySelector(".check-editar").checked,
                        excluir: linha.querySelector(".check-excluir").checked,
                        cancelar: linha.querySelector(".check-cancelar").checked
                    });
                }
            });
            // Captura as Unidades Permitidas
            const unidadesPermitidas = [];
            form.querySelectorAll("input[name='unidadesPermitidas']:checked").forEach(cb => {
                unidadesPermitidas.push(cb.value);
            });

            // Monta o objeto JSON exatamente como o Java espera
            const dadosEnvio = {
                action: action,
                id: id ? parseInt(id) : 0,
                username: username,
				nomeCompleto: nomeCompleto, // <-- ADICIONADO AQUI
                email: email,
                senha: senha,
                perfil: perfil,
                ativo: ativo,
                unidadePadrao: unidadePadrao,
                modulosPermitidos: modulosPermitidos,
                unidadesPermitidas: unidadesPermitidas
            };

            console.log("Enviando dados via JSON para o Servlet:", dadosEnvio);

            // Realiza o envio via Fetch para o Servlet
            fetch(form.action, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=UTF-8"
                },
                body: JSON.stringify(dadosEnvio)
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(async resultado => {
                if (resultado.body.success) {
                    // Dispara o Modal de Sucesso customizado e aguarda o usuário clicar em OK
                    await ModalService.success("Sucesso", resultado.body.message);
                    
                    // Redireciona para a tela correta após o fechamento do modal
                    // (Se o seu GerenciarUsuariosServlet chamar o jsp gerenciar-usuarios.jsp, pode manter o servlet ou apontar para o .jsp)
                    window.location.href = window.contextPath + "/GerenciarUsuariosServlet"; 
                } else {
                    // Dispara o Modal de Erro/Aviso customizado
                    await ModalService.error("Atenção", resultado.body.message);
                }
            })
            .catch(async erro => {
                console.error("Erro na requisição AJAX:", erro);
                await ModalService.error("Erro no Servidor", "Ocorreu um erro inesperado ao comunicar com o servidor.");
            });
        });
    }
 });
function carregarUnidadesDinamicas() {
    const container = document.getElementById('listaUnidades');
    const selectFilial = document.querySelector('select[name="filial"]');

    if (typeof unidadesGlobais !== 'undefined' && unidadesGlobais.length > 0) {
        
        // 1. Preenche os Checkboxes
        if (container) {
            container.innerHTML = ''; 
            unidadesGlobais.forEach(unidade => {
                const idFilial = unidade[0];
                const origemCodigo = unidade[1];
                const sufixo = unidade[2];

                // Identifica se é a Matriz (161)
                const isMatriz = (String(origemCodigo) === '161');
                const badgeClass = isMatriz ? 'bg-success text-white' : 'bg-light text-dark';
                const tipoTexto = isMatriz ? 'Matriz' : 'Filial';

                const htmlItem = `
                    <div class="col-md-4 unidade-item">
                        <label class="unidade-card" style="display: flex; align-items: center; gap: 10px; cursor: pointer; border: 1px solid #dee2e6; padding: 10px; border-radius: 6px;">
                            <input type="checkbox" name="unidadesPermitidas" value="${idFilial}" class="form-check-input unidade-checkbox" style="margin-top: 0;">
                            <div class="unidade-info">
                                <div class="unidade-header">
                                    <!-- Exibe "151 CAR" sem traço -->
                                    <span class="unidade-codigo fw-bold text-primary">${origemCodigo} ${sufixo}</span>
                                    <span class="badge ${badgeClass}">${tipoTexto}</span>
                                </div>
                                <div class="unidade-nome text-secondary">Unidade Operacional</div>
                            </div>
                        </label>
                    </div>
                `;
                container.innerHTML += htmlItem;
            });

            document.querySelectorAll('.unidade-checkbox').forEach(cb => {
                cb.addEventListener('change', atualizarContadorUnidades);
            });
        }

        // 2. Preenche o Select de Filial Principal
        if (selectFilial) {
            selectFilial.innerHTML = '<option value="">Selecione a filial...</option>';
            unidadesGlobais.forEach(unidade => {
                const idFilial = unidade[0];
                const origemCodigo = unidade[1];
                const sufixo = unidade[2];

                const isMatriz = (String(origemCodigo) === '161');
                const tipoTexto = isMatriz ? 'Matriz' : 'Filial';

                const option = document.createElement('option');
                option.value = idFilial; 
                option.textContent = `${origemCodigo} ${sufixo} (${tipoTexto})`; 
                selectFilial.appendChild(option);
            });
        }

        console.log("Unidades carregadas com destaque para Matriz via JS.");
    }
}

// Adicione esta função no seu cadastro-usuario.js
function preencherDadosEdicao() {
    if (typeof usuarioEdicao !== 'undefined' && usuarioEdicao !== null) {
        console.log("Modo de Edição detectado. Preenchendo dados:", usuarioEdicao);

        // 1. Preenche dados textuais básicos
        const usernameInput = document.getElementById("username");
        if (usernameInput) usernameInput.value = usuarioEdicao.username || '';

        const nomeCompletoInput = document.getElementById("nomeCompleto");
        if (nomeCompletoInput) nomeCompletoInput.value = usuarioEdicao.nomeCompleto || '';

        const emailInput = document.getElementById("email");
        if (emailInput) emailInput.value = usuarioEdicao.email || '';

        const perfilSelect = document.getElementById("perfil");
        if (perfilSelect && usuarioEdicao.perfil) {
            perfilSelect.value = usuarioEdicao.perfil.toUpperCase();
        }

        const ativoCheckbox = document.getElementById("ativo");
        if (ativoCheckbox) {
            ativoCheckbox.checked = usuarioEdicao.ativo;
        }

        // 2. Preenche a Filial Principal (Unidade Ativa/Padrão)
        const selectFilial = document.getElementById("unidadePadrao");
        if (selectFilial && usuarioEdicao.unidadesPermitidas) {
            // Procura qual unidade está marcada como padrão no banco ou pega a primeira associada
            // Como o DAO carrega a lista, podemos selecionar a que veio como principal ou correspondente
            if (usuarioEdicao.unidadeAtivaId) {
                selectFilial.value = usuarioEdicao.unidadeAtivaId;
            }
        }

        // 3. Marca os Checkboxes de Unidades Permitidas
        if (typeof unidadesPermitidasUsuario !== 'undefined' && Array.isArray(unidadesPermitidasUsuario)) {
            setTimeout(() => {
                unidadesPermitidasUsuario.forEach(idUnidade => {
                    const checkbox = document.querySelector(`.unidade-checkbox[value="${idUnidade}"]`);
                    if (checkbox) {
                        checkbox.checked = true;
                    }
                });
                atualizarContadorUnidades();
            }, 100); // Pequeno atraso para garantir que o DOM das unidades dinâmicas já foi renderizado
        }

        // 4. Preenche a Matriz Granular de Módulos
        if (usuarioEdicao.permissoesModulos && Array.isArray(usuarioEdicao.permissoesModulos)) {
            usuarioEdicao.permissoesModulos.forEach(perm => {
                // Procura a linha do módulo correspondente pelo ID oculto
                const linhas = document.querySelectorAll(".modulo-row");
                linhas.forEach(linha => {
                    const inputId = linha.querySelector(".modulo-id");
                    if (inputId && parseInt(inputId.value) === perm.moduloId) {
                        if (linha.querySelector(".check-consultar")) linha.querySelector(".check-consultar").checked = perm.consultar;
                        if (linha.querySelector(".check-inserir")) linha.querySelector(".check-inserir").checked = perm.inserir;
                        if (linha.querySelector(".check-editar")) linha.querySelector(".check-editar").checked = perm.editar;
                        if (linha.querySelector(".check-excluir")) linha.querySelector(".check-excluir").checked = perm.excluir;
                        if (linha.querySelector(".check-cancelar")) linha.querySelector(".check-cancelar").checked = perm.cancelar;
                    }
                });
            });
        }
    }
}

function atualizarContadorUnidades() {
    const checkboxes = document.querySelectorAll('.unidade-checkbox:checked');
    const contador = document.getElementById('contadorUnidades');
    if (contador) {
        contador.textContent = checkboxes.length + ' selecionada(s)';
    }
}

function selecionarTodasUnidades() {
    document.querySelectorAll('.unidade-item').forEach(item => {
        if (item.style.display !== 'none') {
            const cb = item.querySelector('.unidade-checkbox');
            if (cb) cb.checked = true;
        }
    });
    atualizarContadorUnidades();
}

function limparUnidades() {
    document.querySelectorAll('.unidade-checkbox').forEach(cb => {
        cb.checked = false;
    });
    atualizarContadorUnidades();
}

function filtrarUnidades() {
    const inputPesquisa = document.getElementById('pesquisaUnidade');
    if (!inputPesquisa) return;

    const termo = inputPesquisa.value.toLowerCase();
    document.querySelectorAll('.unidade-item').forEach(item => {
        const texto = item.textContent.toLowerCase();
        if (texto.includes(termo)) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}

/**
 * Marca ou desmarca absolutamente todas as caixas de permissão da tabela.
 */
function toggleTodosModulos(masterCheckbox) {
    const checkboxes = document.querySelectorAll('.modulo-row input[type="checkbox"]');
    checkboxes.forEach(cb => {
        cb.checked = masterCheckbox.checked;
    });
}

/**
 * Marca ou desmarca todos os checkboxes de uma coluna específica (ex: todas as caixas de 'Inserir').
 */
function toggleColunaPermissao(classeColuna, masterCheckbox) {
    const checkboxes = document.querySelectorAll(`.${classeColuna}`);
    checkboxes.forEach(cb => {
        cb.checked = masterCheckbox.checked;
    });
}