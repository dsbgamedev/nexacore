document.addEventListener('DOMContentLoaded', function() {
    console.log("DEBUG JS: DOMContentLoaded disparado para usuarios.js");

    // --- Variáveis Globais lidas dos data-attributes do body ---
    const bodyElement = document.body;
    const APP_CONTEXT_PATH = bodyElement.dataset.appContextPath || '';
    const USER_ID_LOGADO = parseInt(bodyElement.dataset.userIdLogado || '0');
    const PERFIL_USUARIO_LOGADO = bodyElement.dataset.perfilUsuarioLogado || '';

    // ADICIONADO DEBUG: Loga as variáveis do usuário logado
    console.log("DEBUG JS: USER_ID_LOGADO:", USER_ID_LOGADO);
    console.log("DEBUG JS: PERFIL_USUARIO_LOGADO:", PERFIL_USUARIO_LOGADO);

    // Variáveis específicas para cadastroUsuario.jsp, inicializadas a partir de data-attributes
    const IS_EDITING = bodyElement.dataset.isEditing === 'true'; // Converte para booleano
    const USUARIO_SENDO_EDITADO_ID = parseInt(bodyElement.dataset.usuarioSendoEditadoId || '0');
    const DISABLE_PERFIL_FIELD_SERVER_SIDE = bodyElement.dataset.disablePerfilFieldServerSide === 'true';
    const DISABLE_MODULE_CHECKBOXES_SERVER_SIDE = bodyElement.dataset.disableModuleCheckboxesServerSide === 'true';
    
    // CORREÇÃO AQUI: Lendo de variáveis globais definidas diretamente no JSP
    let USUARIO_MODULOS_PERMITIDOS = window.USUARIO_MODULOS_PERMITIDOS || [];
    let TODOS_MODULOS_DISPONIVEIS = window.TODOS_MODULOS_DISPONIVEIS || [];
	let USUARIO_UNIDADES_PERMITIDAS = window.USUARIO_UNIDADES_PERMITIDAS || [];
	let TODAS_UNIDADES_DISPONIVEIS = window.TODAS_UNIDADES_DISPONIVEIS || [];
	
    console.log("DEBUG JS: USUARIO_MODULOS_PERMITIDOS (lido de window):", USUARIO_MODULOS_PERMITIDOS);
    console.log("DEBUG JS: TODOS_MODULOS_DISPONIVEIS (lido de window):", TODOS_MODULOS_DISPONIVEIS);

	// Adicione ao seu log de DEBUG para conferir no console:
	console.log("DEBUG JS: USUARIO_UNIDADES_PERMITIDAS:", USUARIO_UNIDADES_PERMITIDAS);
	console.log("DEBUG JS: TODAS_UNIDADES_DISPONIVEIS:", TODAS_UNIDADES_DISPONIVEIS);

    // --- Elementos do DOM (para listarUsuarios.jsp) ---
    const tabelaUsuariosBody = document.getElementById('tableBody');
    const selecionarTodosUsuariosCheckbox = document.getElementById('selectAllItems');
    const btnNovoUsuario = document.getElementById('newCadastro');
    const botaoEditarSelecionadosUsuarios = document.getElementById('editSelected');
    const botaoExcluirSelecionadosUsuarios = document.getElementById('deleteSelected');
    const mensagemVazio = document.getElementById('mensagemVazio');

    // --- Hierarquia de perfis (do mais alto para o mais baixo) ---
    const PERFIL_HIERARQUIA = [
        "super_administrador",
        "administrador",
        "gerente",
        "tecnico",
        "usuario"
    ];

    /**
     * Retorna o índice do perfil na hierarquia.
     * @param {string} perfil O nome do perfil.
     * @returns {number} O índice do perfil, ou -1 se não encontrado.
     */
    function getPerfilIndex(perfil) {
        return PERFIL_HIERARQUIA.indexOf(perfil);
    }

    /**
     * Verifica se o usuário logado tem permissão para operar (editar/excluir) um usuário alvo.
     * Esta função é a fonte da verdade para permissões no frontend.
     * @param {number} targetUserId O ID do usuário alvo.
     * @param {string} targetUserPerfil O perfil do usuário alvo.
     * @param {string} operation 'edit' ou 'delete'.
     * @returns {boolean} True se tiver permissão, false caso contrário.
     */
    function canUserOperateOnTarget(targetUserId, targetUserPerfil, operation) {
        const perfilLogadoIndex = getPerfilIndex(PERFIL_USUARIO_LOGADO);
        const perfilTargetIndex = getPerfilIndex(targetUserPerfil);

        // Se o perfil logado ou o perfil alvo não são reconhecidos, assume sem permissão
        if (perfilLogadoIndex === -1 || perfilTargetIndex === -1) {
            return false;
        }

        // Regra 1: Usuário não pode operar em si mesmo (para evitar auto-exclusão/edição via lista)
        if (targetUserId === USER_ID_LOGADO) {
            return false;
        }

        // Regra 2: Super Administrador pode operar em qualquer um (exceto a si mesmo, já barrado acima)
        if (PERFIL_USUARIO_LOGADO === 'super_administrador') {
            return true;
        }

        // Regra 3: Administrador
        if (PERFIL_USUARIO_LOGADO === 'administrador') {
            // Admin NÃO pode operar em Super Administrador
            if (targetUserPerfil === 'super_administrador') {
                return false;
            }
            // Admin NÃO pode operar em outros Administradores (mesmo nível)
            if (targetUserPerfil === 'administrador') {
                return false;
            }
            // Admin PODE operar em perfis inferiores (gerente, tecnico, usuario)
            if (perfilTargetIndex > perfilLogadoIndex) {
                return true;
            }
        }

        // Regra 4: Gerente, Tecnico, Usuario (perfis inferiores)
        // Eles não podem operar em nenhum outro usuário (já barrado pela Regra 1 para si mesmos)
        return false;
    }


    /**
     * Atualiza o estado dos botões de ação (Editar, Excluir) com base nos checkboxes selecionados.
     */
    function updateButtonStates() {
        console.log("DEBUG JS: updateButtonStates chamado.");
        const selectedCheckboxes = document.querySelectorAll('.rowCheckbox:checked');
        const numSelected = selectedCheckboxes.length;

        if (btnNovoUsuario) {
            btnNovoUsuario.disabled = false;
        }

        if (botaoEditarSelecionadosUsuarios) {
            if (numSelected === 1) {
                const selectedCheckbox = selectedCheckboxes[0];
                const userId = parseInt(selectedCheckbox.dataset.id);
                const userPerfil = selectedCheckbox.dataset.perfil;
                // Habilita se 1 selecionado E o usuário logado pode editar o usuário selecionado
                if (canUserOperateOnTarget(userId, userPerfil, 'edit')) {
                    botaoEditarSelecionadosUsuarios.removeAttribute('disabled');
                } else {
                    botaoEditarSelecionadosUsuarios.setAttribute('disabled', 'true');
                }
            } else {
                botaoEditarSelecionadosUsuarios.setAttribute('disabled', 'true');
            }
        }

        if (botaoExcluirSelecionadosUsuarios) {
            if (numSelected > 0) {
                // Habilita se pelo menos 1 selecionado E TODOS os selecionados podem ser excluídos pelo usuário logado
                const allSelectedCanBeDeleted = Array.from(selectedCheckboxes).every(cb => 
                    canUserOperateOnTarget(parseInt(cb.dataset.id), cb.dataset.perfil, 'delete')
                );
                if (allSelectedCanBeDeleted) {
                    botaoExcluirSelecionadosUsuarios.removeAttribute('disabled');
                } else {
                    botaoExcluirSelecionadosUsuarios.setAttribute('disabled', 'true');
                }
            } else {
                botaoExcluirSelecionadosUsuarios.setAttribute('disabled', 'true');
            }
        }

        if (selecionarTodosUsuariosCheckbox) {
            const allCheckboxes = document.querySelectorAll('.rowCheckbox');
            const selectableCheckboxes = Array.from(allCheckboxes).filter(cb => !cb.disabled);
            const numSelectableChecked = Array.from(selectableCheckboxes).filter(cb => cb.checked).length;

            if (selectableCheckboxes.length > 0) {
                selecionarTodosUsuariosCheckbox.checked = numSelectableChecked === selectableCheckboxes.length;
                selecionarTodosUsuariosCheckbox.removeAttribute('disabled'); // Garante que o checkbox principal não esteja desabilitado
            } else {
                selecionarTodosUsuariosCheckbox.checked = false;
                selecionarTodosUsuariosCheckbox.setAttribute('disabled', 'true');
            }
        }
    }

    /**
     * Carrega os dados dos usuários do servidor via AJAX.
     */
    async function loadUsersData() {
        console.log("DEBUG JS: loadUsersData iniciada.");

        if (tabelaUsuariosBody) tabelaUsuariosBody.innerHTML = '';
        if (mensagemVazio) mensagemVazio.style.display = 'none';

        try {
            const response = await fetch(`${APP_CONTEXT_PATH}/GerenciarUsuariosServlet?action=list`, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest' // Indica que é uma requisição AJAX
                }
            });

            console.log("DEBUG JS: Resposta recebida. Status:", response.status);

            if (response.status === 302 || response.status === 401 || response.status === 403) {
                let errorMessage = "Sessão expirou ou acesso não autorizado. Por favor, faça login novamente.";
                try {
                    const errorJson = await response.json();
                    if (errorJson && errorJson.error) {
                        errorMessage = errorJson.error;
                    }
                } catch (e) {
                    console.warn("DEBUG JS: Resposta não é JSON para erro de autenticação/autorização. Redirecionando.");
                }
                await showAlertModal("Erro de Acesso", errorMessage, "error");
                setTimeout(() => {
                    window.location.href = APP_CONTEXT_PATH + "/login.jsp?message=session_expired";
                }, 1500);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: `Erro do servidor: ${response.status} ${response.statusText}` }));
                console.error("DEBUG JS: Erro na resposta da rede:", response.status, response.statusText, errorData);
                throw new Error(errorData.message || "Erro desconhecido ao carregar usuários.");
            }

            const data = await response.json();
            console.log("DEBUG JS: Dados de usuários recebidos (JSON):", data);

            if (data && data.success && Array.isArray(data.users)) {
                renderUsersTable(data.users);
            } else {
                console.warn("DEBUG JS: Formato de dados inesperado ou 'users' ausente/inválido:", data);
                // Colspan para 8 (ID, Usuário, Email, Perfil, Ativo, Módulos Permitidos, Ações)
                if (tabelaUsuariosBody) tabelaUsuariosBody.innerHTML = `<tr><td colspan="8" class="mensagem-tabela-inicial">Erro ao carregar dados de usuários.</td></tr>`;
            }

        } catch (error) {
            console.error('DEBUG JS: Erro no bloco catch de loadUsersData:', error);
            // Colspan para 8
            if (tabelaUsuariosBody) tabelaUsuariosBody.innerHTML = `<tr><td colspan="8" class="mensagem-tabela-inicial">Erro ao carregar usuários: ${error.message}.</td></tr>`;
        } finally {
            updateButtonStates();
            console.log("DEBUG JS: loadUsersData finalizada.");
        }
    }

	/**
	 * Renderiza a tabela de usuários com os dados fornecidos.
	 * @param {Array<Object>} users - Lista de objetos de usuário.
	 */
	function renderUsersTable(users) {
	    console.log("DEBUG JS: renderUsersTable chamado com", users.length, "usuários.");
	    if (tabelaUsuariosBody) tabelaUsuariosBody.innerHTML = '';

	    if (users.length === 0) {
	        if (mensagemVazio) mensagemVazio.style.display = 'block';
	        if (tabelaUsuariosBody) tabelaUsuariosBody.innerHTML = `<tr><td colspan="8" class="mensagem-tabela-inicial">Nenhum usuário encontrado.</td></tr>`;
	        return;
	    } else {
	        if (mensagemVazio) mensagemVazio.style.display = 'none';
	    }

	    users.forEach(user => {
	        const row = tabelaUsuariosBody.insertRow();
	        const userId = user.id || '';

	        if (!user.ativo) {
	            row.classList.add('inactive-user', 'inativo');
	        }

	        const checkboxCell = row.insertCell();
	        const checkbox = document.createElement('input');
	        checkbox.type = 'checkbox';
	        checkbox.className = 'rowCheckbox';
	        checkbox.dataset.id = userId;
	        checkbox.dataset.perfil = user.perfil;
	        checkbox.dataset.ativo = user.ativo;
	        checkboxCell.appendChild(checkbox);
	        checkbox.addEventListener('change', updateButtonStates);

	        row.insertCell().textContent = userId;
	        row.insertCell().textContent = user.username || '';
	        row.insertCell().textContent = user.email || '';
	        row.insertCell().textContent = formatarPerfil(user.perfil);
	        row.insertCell().textContent = user.ativo ? 'Sim' : 'Não';

	        // Modificação: Adiciona a célula para Módulos Permitidos com um div e scrollbar
	        const modulosCell = row.insertCell();
	        const modulosDiv = document.createElement('div');
	        modulosDiv.classList.add('modulos-cell-content');
	        if (user.modulosPermitidos && Array.isArray(user.modulosPermitidos) && user.modulosPermitidos.length > 0) {
	            modulosDiv.textContent = user.modulosPermitidos.join(', ');
	        } else {
	            modulosDiv.textContent = '-';
	        }
	        modulosCell.appendChild(modulosDiv);

	        const actionsCell = row.insertCell();
	        actionsCell.classList.add('tabela-acao-btns');

	        const editBtn = document.createElement('button');
	        editBtn.textContent = 'Editar';
	        editBtn.classList.add('btn-edit');
	        editBtn.dataset.id = userId;
	        editBtn.dataset.perfil = user.perfil;
	        editBtn.addEventListener('click', handleEditUser);
	        actionsCell.appendChild(editBtn);

	        const deleteBtn = document.createElement('button');
	        deleteBtn.textContent = 'Excluir';
	        deleteBtn.classList.add('btn-delete');
	        deleteBtn.dataset.id = userId;
	        deleteBtn.dataset.perfil = user.perfil;
	        deleteBtn.addEventListener('click', handleDeleteUser);
	        actionsCell.appendChild(deleteBtn);

	        const canEdit = canUserOperateOnTarget(userId, user.perfil, 'edit');
	        const canDelete = canUserOperateOnTarget(userId, user.perfil, 'delete');
	        console.log(`DEBUG JS: Usuário ID ${userId} (${user.perfil}): Pode Editar? ${canEdit}, Pode Excluir? ${canDelete}`);

	        if (canEdit) {
	            editBtn.removeAttribute('disabled');
	        } else {
	            editBtn.setAttribute('disabled', 'true');
	        }

	        if (canDelete) {
	            deleteBtn.removeAttribute('disabled');
	        } else {
	            deleteBtn.setAttribute('disabled', 'true');
	        }

	        if (canEdit || canDelete) {
	            checkbox.removeAttribute('disabled');
	        } else {
	            checkbox.setAttribute('disabled', 'true');
	        }
	    });
	}
    /**
     * Lida com a edição de um único usuário.
     * @param {Event} event - O evento de clique.
     */
    function handleEditUser(event) {
        const userId = event.target.dataset.id;
        const userPerfil = event.target.dataset.perfil;

        if (!canUserOperateOnTarget(parseInt(userId), userPerfil, 'edit')) {
            showAlertModal("Permissão Negada", "Você não tem permissão para editar este usuário.", "warning");
            return;
        }
        console.log("DEBUG JS: Redirecionando para edição de usuário com ID:", userId);
        window.location.href = `${APP_CONTEXT_PATH}/GerenciarUsuariosServlet?action=edit&id=${userId}`;
    }

    /**
     * Lida com a exclusão de um único usuário.
     * @param {Event} event - O evento de clique.
     */
    async function handleDeleteUser(event) {
        const userId = parseInt(event.target.dataset.id);
        const userPerfil = event.target.dataset.perfil;
        console.log("DEBUG JS: Tentando excluir usuário com ID:", userId);

        if (!canUserOperateOnTarget(userId, userPerfil, 'delete')) {
            await showAlertModal("Permissão Negada", "Você não tem permissão para excluir este usuário.", "warning");
            return;
        }

        const confirmDelete = await showConfirmModal("Confirmação de Exclusão", `Tem certeza que deseja excluir o usuário com ID ${userId}?`);

        if (confirmDelete) {
            try {
                const response = await fetch(`${APP_CONTEXT_PATH}/GerenciarUsuariosServlet`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: JSON.stringify({ action: 'delete', id: userId })
                });

                if (response.status === 302 || response.status === 401 || response.status === 403) {
                    let errorMessage = "Sessão expirou ou acesso não autorizado. Por favor, faça login novamente.";
                    try {
                        const errorJson = await response.json();
                        if (errorJson && errorJson.error) { errorMessage = errorJson.error; }
                    } catch (e) { console.warn("DEBUG JS: Resposta não é JSON para erro de autenticação/autorização."); }
                    await showAlertModal("Erro de Acesso", errorMessage, "error");
                    setTimeout(() => { window.location.href = APP_CONTEXT_PATH + "/login.jsp?message=session_expired"; }, 1500);
                    return;
                }

                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({ message: `Erro do servidor: ${response.status} ${response.statusText}` }));
                    throw new Error(errorData.message || "Erro desconhecido ao excluir usuário.");
                }

                const result = await response.json();
                if (result.success) {
                    await showAlertModal("Sucesso!", result.message, "success");
                    loadUsersData(); // Recarrega os dados após a exclusão
                } else {
                    await showAlertModal("Erro!", result.message, "error");
                }
            } catch (error) {
                console.error('DEBUG JS: Erro no bloco catch de exclusão de usuário:', error);
                await showAlertModal("Erro de Conexão", "Não foi possível conectar ao servidor ou houve um erro: " + error.message, "error");
            }
        }
    }

    /**
     * Lida com a edição de múltiplos usuários (apenas um é permitido).
     */
    function handleEditarSelecionadosUsuarios() {
        const selectedCheckboxes = document.querySelectorAll('.rowCheckbox:checked');
        if (selectedCheckboxes.length !== 1) {
            showAlertModal("Atenção", "Selecione exatamente um usuário para editar.", "warning");
            return;
        }

        const userId = parseInt(selectedCheckboxes[0].dataset.id);
        const userPerfil = selectedCheckboxes[0].dataset.perfil;

        if (!canUserOperateOnTarget(userId, userPerfil, 'edit')) {
            showAlertModal("Permissão Negada", "Você não tem permissão para editar este usuário.", "warning");
            return;
        }

        window.location.href = `${APP_CONTEXT_PATH}/GerenciarUsuariosServlet?action=edit&id=${userId}`;
    }

    /**
     * Lida com a exclusão de múltiplos usuários.
     */
    async function handleExcluirSelecionadosUsuarios() {
        const selecionados = Array.from(document.querySelectorAll('.rowCheckbox:checked'));
        if (selecionados.length === 0) {
            await showAlertModal("Atenção", "Nenhum usuário selecionado para exclusão.", "warning");
            return;
        }

        const idsParaExcluir = [];
        const idsNaoPermitidos = [];

        for (const cb of selecionados) {
            const usuarioId = parseInt(cb.dataset.id);
            const usuarioPerfil = cb.dataset.perfil;

            if (canUserOperateOnTarget(usuarioId, usuarioPerfil, 'delete')) {
                idsParaExcluir.push(usuarioId);
            } else {
                idsNaoPermitidos.push(usuarioId);
            }
        }

        if (idsParaExcluir.length === 0) {
            let message = "Nenhum usuário válido selecionado para exclusão.";
            if (idsNaoPermitidos.length > 0) {
                message = "Você não tem permissão para excluir os usuários selecionados (perfis Super Administrador/Administrador ou você mesmo).";
            }
            await showAlertModal("Atenção", message, "warning");
            return;
        }

        const confirmMsg = `Tem certeza que deseja excluir ${idsParaExcluir.length} usuário(s) selecionado(s)?` +
                           (idsNaoPermitidos.length > 0 ? "\n(Alguns usuários não puderam ser excluídos devido a permissões.)" : "");
        
        const confirmDelete = await showConfirmModal("Confirmação de Exclusão", confirmMsg);

        if (confirmDelete) {
            try {
                const response = await fetch(`${APP_CONTEXT_PATH}/GerenciarUsuariosServlet`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: JSON.stringify({ action: 'deleteMultiple', ids: idsParaExcluir })
                });

                if (response.status === 302 || response.status === 401 || response.status === 403) {
                    let errorMessage = "Sessão expirou ou acesso não autorizado. Por favor, faça login novamente.";
                    try {
                        const errorJson = await response.json();
                        if (errorJson && errorJson.error) { errorMessage = errorJson.error; }
                    } catch (e) { console.warn("DEBUG JS: Resposta não é JSON para erro de autenticação/autorização."); }
                    await showAlertModal("Erro de Acesso", errorMessage, "error");
                    setTimeout(() => { window.location.href = APP_CONTEXT_PATH + "/login.jsp?message=session_expired"; }, 1500);
                    return;
                }

                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({ message: `Erro do servidor: ${response.status} ${response.statusText}` }));
                    throw new Error(errorData.message || "Erro desconhecido ao excluir usuários.");
                }

                const result = await response.json();
                if (result.success) {
                    let finalMessage = result.message;
                    if (idsNaoPermitidos.length > 0) {
                        finalMessage += ` (IDs ${idsNaoPermitidos.join(', ')} não puderam ser excluídos devido a permissões).`;
                    }
                    await showAlertModal("Sucesso!", finalMessage, "success");
                    loadUsersData(); // Recarrega os dados após a exclusão
                } else {
                    await showAlertModal("Erro!", result.message, "error");
                }
            } catch (error) {
                console.error('DEBUG JS: Erro no bloco catch de exclusão múltipla:', error);
                await showAlertModal("Erro de Conexão", "Não foi possível conectar ao servidor ou houve um erro: " + error.message, "error");
            }
        }
    }

    /**
     * Configura o formulário de cadastro/edição de usuário.
     */
    function setupCadastroUsuarioForm() {
        console.log("DEBUG JS: setupCadastroUsuarioForm iniciado.");
		
		// 1. MAPEAMENTO DOS ELEMENTOS (Declare todos aqui no topo)
        const perfilSelect = document.getElementById('perfil');
        const usuarioForm = document.getElementById('usuarioForm');
        const btnLimparUsuario = document.getElementById('btnLimparUsuario');
        const usernameInput = document.getElementById('username');
        const emailInput = document.getElementById('email');
        const senhaInput = document.getElementById('senha');
        const confirmarSenhaInput = document.getElementById('confirmarSenha');
        const ativoCheckbox = document.getElementById('ativo');
        const modulosCheckboxesContainer = document.getElementById('modulosCheckboxes');
		const unidadesCheckboxesContainer = document.getElementById('unidadesCheckboxes');
		const selectPadrao = document.getElementById('unidadePadrao');
		
		// 1. PRIMEIRO: Renderiza os Checkboxes de Unidades (O código que você postou)
		if (unidadesCheckboxesContainer) {
		    unidadesCheckboxesContainer.innerHTML = '';
		    const USUARIO_UNIDADES_PERMITIDAS = window.USUARIO_UNIDADES_PERMITIDAS || [];
		    const TODAS_UNIDADES_DISPONIVEIS = window.TODAS_UNIDADES_DISPONIVEIS || [];

		    TODAS_UNIDADES_DISPONIVEIS.forEach(unidade => {
		        const div = document.createElement('div');
		        div.classList.add('permissoes-grid-item');
		        const checkbox = document.createElement('input');
		        checkbox.type = 'checkbox';
		        checkbox.id = `unidade-${unidade[0]}`;
		        checkbox.name = 'unidadesPermitidas';
		        checkbox.value = unidade[0];

		        if (USUARIO_UNIDADES_PERMITIDAS.includes(unidade[0].toString())) {
		            checkbox.checked = true;
		        }

		        if (DISABLE_MODULE_CHECKBOXES_SERVER_SIDE) {
		            checkbox.setAttribute('disabled', 'true');
		        }
		        
		        const label = document.createElement('label');
		        label.htmlFor = `unidade-${unidade[0]}`;
		        label.textContent = unidade[1];

		        div.appendChild(checkbox);
		        div.appendChild(label);
		        unidadesCheckboxesContainer.appendChild(div);
		    });
		}
		
		// 2. SEGUNDO: Define a função de sincronização (se ela já não estiver definida acima)
		// Função para sincronizar o Select com os Checkboxes
		function sincronizarUnidadePrincipal() {
		    const idPrincipal = selectPadrao.value;

		    // 1. Desbloqueia todos os checkboxes de unidades primeiro
		    document.querySelectorAll('#unidadesCheckboxes input').forEach(cb => {
		        cb.disabled = false;
		        // Se for o Admin logado e o servidor mandou travar tudo, respeita
		        if (DISABLE_MODULE_CHECKBOXES_SERVER_SIDE) cb.disabled = true;
		    });

		    // 2. Se houver algo selecionado no Select
		    if (idPrincipal) {
		        const checkboxCorrespondente = document.getElementById(`unidade-${idPrincipal}`);
		        if (checkboxCorrespondente) {
		            checkboxCorrespondente.checked = true; // Marca
		            checkboxCorrespondente.disabled = true; // Trava
		        }
		    }
		} 
		
		// --- NOVO: Configurar Select de Unidade Principal 12.03.20206---
		// 3. TERCEIRO: Aplica os eventos e a execução inicial	
		if (selectPadrao) {
		    selectPadrao.innerHTML = '<option value="">-- Selecione a Unidade Principal --</option>';
		    TODAS_UNIDADES_DISPONIVEIS.forEach(unidade => {
		        const option = document.createElement('option');
		        option.value = unidade[0]; // ID
		        option.textContent = unidade[1]; // Nome
		        selectPadrao.appendChild(option);
		    });

		    // Se estiver editando, define o valor inicial (pega do primeiro item da lista de permitidas)
		    if (IS_EDITING && USUARIO_UNIDADES_PERMITIDAS.length > 0) {
		        selectPadrao.value = USUARIO_UNIDADES_PERMITIDAS[0];
		    }
		}

		if(selectPadrao){
			// Escuta a mudança no Select
			selectPadrao.addEventListener('change', sincronizarUnidadePrincipal);
			// Chama uma vez no carregamento para aplicar a trava inicial (se for edição)
			sincronizarUnidadePrincipal();
		}
		
        // Obter o perfil atual do usuário sendo editado, se for o caso
        const usuarioPerfilAtual = IS_EDITING ? (document.getElementById('id') && document.getElementById('id').value !== '' ? document.querySelector('#perfil option[selected]')?.value : '') : '';
        
        // Obter a lista de perfis permitidos para seleção (assumindo que o JSP já a define)
        const perfisPermitidosParaSelecao = getPerfisPermitidosParaSelecao(PERFIL_USUARIO_LOGADO, IS_EDITING, usuarioPerfilAtual);

        // Popular o select de perfis
        perfilSelect.innerHTML = '<option value="">-- Selecione o Perfil --</option>';
        perfisPermitidosParaSelecao.forEach(perfil => {
            const option = document.createElement('option');
            option.value = perfil;
            option.textContent = formatarPerfil(perfil);
            perfilSelect.appendChild(option);
        });

        // Definir o valor selecionado no modo de edição
        if (IS_EDITING && usuarioPerfilAtual) {
            perfilSelect.value = usuarioPerfilAtual;
        } else {
            perfilSelect.value = '';
        }

        // Desabilitar o campo de perfil se determinado pelo servidor
        if (DISABLE_PERFIL_FIELD_SERVER_SIDE) {
            perfilSelect.setAttribute('disabled', 'true');
        } else {
            perfilSelect.removeAttribute('disabled');
        }

        // Popular os checkboxes de módulos
        if (modulosCheckboxesContainer) {
            modulosCheckboxesContainer.innerHTML = '';
            // AQUI: TODOS_MODULOS_DISPONIVEIS e USUARIO_MODULOS_PERMITIDOS são lidos de variáveis globais
            TODOS_MODULOS_DISPONIVEIS.forEach(moduloNome => {
                const div = document.createElement('div');
                div.classList.add('permissoes-grid-item');
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.id = `modulo-${moduloNome}`;
                checkbox.name = 'modulosPermitidos';
                checkbox.value = moduloNome;
                
                // Formata o nome do módulo para exibição (ex: "produtos" -> "Produtos")
                const displayModuloNome = moduloNome.split('_')
                                                    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                                                    .join(' ');

                if (USUARIO_MODULOS_PERMITIDOS.includes(moduloNome)) {
                    checkbox.checked = true;
                }

                if (DISABLE_MODULE_CHECKBOXES_SERVER_SIDE) {
                    checkbox.setAttribute('disabled', 'true');
                } else {
                    checkbox.removeAttribute('disabled');
                }
                
                const label = document.createElement('label');
                label.htmlFor = `modulo-${moduloNome}`;
                label.textContent = displayModuloNome;

                div.appendChild(checkbox);
                div.appendChild(label);
                modulosCheckboxesContainer.appendChild(div);
            });
        }
		
		
        // Desabilitar o checkbox 'ativo' se determinado pelo servidor
        if (ativoCheckbox) {
            if (DISABLE_MODULE_CHECKBOXES_SERVER_SIDE) { // Reutilizando a flag para módulos, pode ser uma flag separada se a lógica for diferente
                ativoCheckbox.setAttribute('disabled', 'true');
            } else {
                ativoCheckbox.removeAttribute('disabled');
            }
        }

        // Event listener para o formulário de submissão
        if (usuarioForm) {
            usuarioForm.addEventListener('submit', async function(event) {
                event.preventDefault();

                const senha = senhaInput.value;
                const confirmarSenha = confirmarSenhaInput.value;
                const isNewUser = document.getElementById('id').value === '';
                const username = usernameInput.value; // Necessário para a nova validação

                // --- NOVO: REGRA DE SEGURANÇA (FRONTEND) ---
                if (senha && username.toLowerCase() === senha.toLowerCase()) {
                    await showAlertModal("Segurança", "A senha não pode ser idêntica ao nome de usuário (login). Por favor, escolha uma senha diferente.", "warning");
                    return;
                }
                // --- FIM NOVO ---

                if (senha !== confirmarSenha) {
                    await showAlertModal("Erro de Validação", "As senhas não coincidem.", "error");
                    return;
                }

                if (isNewUser && (senha === '' || confirmarSenha === '')) {
                    await showAlertModal("Erro de Validação", "Para novos usuários, a senha e a confirmação de senha são obrigatórias.", "error");
                    return;
                }
                
                if (perfilSelect.value === "") {
                    await showAlertModal("Erro de Validação", "Por favor, selecione um perfil para o usuário.", "error");
                    return;
                }

                // Validação de permissão para alterar perfil (lógica do lado do cliente)
                if (!DISABLE_PERFIL_FIELD_SERVER_SIDE) {
                    const perfilSelecionado = perfilSelect.value;
                    const perfilLogadoIndex = PERFIL_HIERARQUIA.indexOf(PERFIL_USUARIO_LOGADO);
                    const perfilSelecionadoIndex = PERFIL_HIERARQUIA.indexOf(perfilSelecionado);
                    // perfilUsuarioSendoEditadoIndex não é usado diretamente aqui, mas a lógica de comparação é com o perfil original
                    // que o servidor já validou no doGet. Aqui é uma validação extra no cliente.

                    // Não pode atribuir um perfil superior ao seu próprio
                    if (perfilLogadoIndex > -1 && perfilSelecionadoIndex > -1 && perfilSelecionadoIndex < perfilLogadoIndex) {
                        await showAlertModal("Permissão Negada", "Você não pode atribuir um perfil superior ao seu próprio.", "warning");
                        return;
                    }
                    
                    // Administradores não podem atribuir o perfil de Super Administrador
                    if (PERFIL_USUARIO_LOGADO === 'administrador' && perfilSelecionado === 'super_administrador') {
                        await showAlertModal("Permissão Negada", "Administradores não podem atribuir o perfil de Super Administrador.", "warning");
                        return;
                    }

                    // Regras específicas de edição para Administradores
                    if (PERFIL_USUARIO_LOGADO === 'administrador' && IS_EDITING) {
                        const usuarioOriginalPerfil = document.getElementById('perfilOriginal')?.value; // Assumindo que o JSP pode passar o perfil original
                        if (usuarioOriginalPerfil === 'super_administrador') {
                             await showAlertModal("Permissão Negada", "Administradores não podem alterar o perfil de Super Administradores.", "warning");
                             return;
                        }
                        if (usuarioOriginalPerfil === 'administrador' && perfilSelecionado !== 'administrador') {
                            await showAlertModal("Permissão Negada", "Administradores não podem alterar o perfil de outros Administradores.", "warning");
                            return;
                        }
                    }
                }
				
				// --- LÓGICA PARA PRESERVAR O PADRÃO NO TOPO DA LISTA ---
				            // 1. Captura todos os selecionados
				            let unidadesSelecionadas = Array.from(document.querySelectorAll('#unidadesCheckboxes input[type="checkbox"]:checked'))
				                                            .map(cb => cb.value);

				            // 2. Identifica qual era o padrão atual (lido no carregamento da página)
				            const idPadraoAtual = (window.USUARIO_UNIDADES_PERMITIDAS && window.USUARIO_UNIDADES_PERMITIDAS.length > 0) 
				                                  ? window.USUARIO_UNIDADES_PERMITIDAS[0].toString() 
				                                  : null;

				            // 3. Se o padrão antigo ainda está selecionado, movemos ele para o topo (índice 0)
				            if (idPadraoAtual && unidadesSelecionadas.includes(idPadraoAtual)) {
				                unidadesSelecionadas = [idPadraoAtual, ...unidadesSelecionadas.filter(id => id !== idPadraoAtual)];
				            }

				            const formData = {
				                id: document.getElementById('id').value,
				                action: document.getElementById('action').value,
				                username: usernameInput.value,
				                email: emailInput.value,
				                senha: senhaInput.value,
				                perfil: perfilSelect.value,
				                ativo: ativoCheckbox.checked,
				                modulosPermitidos: Array.from(document.querySelectorAll('#modulosCheckboxes input[type="checkbox"]:checked')).map(cb => cb.value),
				                
								// NOVO: Enviamos a Unidade Principal e a lista de todas as marcadas
								unidadePadrao: document.getElementById('unidadePadrao').value,
								unidadesPermitidas: Array.from(document.querySelectorAll('#unidadesCheckboxes input[type="checkbox"]:checked')).map(cb => cb.value)
				            };
                console.log("DEBUG JS: Dados do formulário para envio (Objeto JS):", formData);

                try {
                    const response = await fetch(`${APP_CONTEXT_PATH}/CadastrarUsuarioServlet`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest'
                        },
                        body: JSON.stringify(formData)
                    });

                    if (response.status === 302 || response.status === 401 || response.status === 403) {
                        let errorMessage = "Sessão expirou ou acesso não autorizado. Por favor, faça login novamente.";
                        try {
                            const errorJson = await response.json();
                            if (errorJson && errorJson.error) { errorMessage = errorJson.error; }
                        } catch (e) { console.warn("DEBUG JS: Resposta não é JSON para erro de autenticação/autorização."); }
                        await showAlertModal("Erro de Acesso", errorMessage, "error");
                        setTimeout(() => { window.location.href = APP_CONTEXT_PATH + "/login.jsp?message=session_expired"; }, 1500);
                        return;
                    }

                    if (!response.ok) {
                        // Tenta ler o JSON de erro, se falhar, usa a mensagem HTTP
                        const errorData = await response.json().catch(() => ({ message: `Erro do servidor: ${response.status} ${response.statusText}` }));
                        throw new Error(errorData.message || "Erro desconhecido ao processar usuário.");
                    }

                    const result = await response.json(); // <-- Se o Servidor retornar um JSON limpo, esta linha funciona.
                    if (result.success) {
                        await showAlertModal("Sucesso!", result.message, "success");
                        if (IS_EDITING && USUARIO_SENDO_EDITADO_ID === USER_ID_LOGADO) {
                            window.location.reload(); // Recarrega a página para atualizar a sessão do próprio usuário
                        } else {
                            window.location.href = `${APP_CONTEXT_PATH}/GerenciarUsuariosServlet`; // Redireciona para a lista
                        }
                    } else {
                        // Aqui o feedback de email duplicado é exibido
                        await showAlertModal("Erro!", result.message, "error"); 
                    }
                } catch (error) {
                    console.error('DEBUG JS: Erro no bloco catch de submissão do formulário:', error);
                    // Esta mensagem deve aparecer apenas se houver falha de rede ou SyntaxError (que já corrigimos no Servidor)
                    await showAlertModal("Erro de Conexão", "Não foi possível conectar ao servidor ou houve um erro: " + error.message, "error");
                }
            });
        }

        // Event listener para o botão Limpar
        if (btnLimparUsuario) {
            btnLimparUsuario.addEventListener('click', () => {
                usuarioForm.reset();
                if (IS_EDITING) {
                     window.location.reload(); // Recarrega para restaurar o estado original na edição
                } else {
                    perfilSelect.value = ""; // Limpa o select para novo cadastro
                }
                // Desmarca todos os checkboxes de módulos
                document.querySelectorAll('#modulosCheckboxes input[type="checkbox"]').forEach(cb => {
                    cb.checked = false;
                });
				// Desmarque também as unidades
				document.querySelectorAll('#unidadesCheckboxes input[type="checkbox"]').forEach(cb => {
				    cb.checked = false;
				});
            });
        }
    }

    /**
     * Retorna a lista de perfis que o usuário logado pode selecionar para outro usuário.
     * @param {string} perfilLogado O perfil do usuário logado.
     * @param {boolean} isEditing Indica se a operação é de edição.
     * @param {string} perfilUsuarioSendoEditado O perfil do usuário que está sendo editado (se isEditing for true).
     * @returns {Array<string>} Uma lista de perfis permitidos.
     */
    function getPerfisPermitidosParaSelecao(perfilLogado, isEditing, perfilUsuarioSendoEditado) {
        const perfisPermitidos = [];
        const perfilLogadoIndex = PERFIL_HIERARQUIA.indexOf(perfilLogado);
        // perfilSelecionadoIndex não é usado aqui, mas perfilUsuarioSendoEditado é o que importa para a lógica de edição
        
        if (perfilLogadoIndex === -1) {
            console.warn("Perfil do usuário logado não reconhecido na hierarquia.");
            return [];
        }

        PERFIL_HIERARQUIA.forEach((perfil, index) => {
            if (perfilLogado === 'super_administrador') {
                perfisPermitidos.push(perfil);
            } else if (perfilLogado === 'administrador') {
                // Admin pode selecionar perfis do mesmo nível ou inferiores, mas não Super Admin
                if (perfil !== 'super_administrador' && index >= perfilLogadoIndex) {
                    perfisPermitidos.push(perfil);
                }
            } else { // Gerente, Tecnico, Usuario
                // Só podem selecionar o próprio perfil
                if (perfil === perfilLogado) {
                    perfisPermitidos.push(perfil);
                }
            }
        });
        
        return [...new Set(perfisPermitidos)].sort((a, b) => PERFIL_HIERARQUIA.indexOf(a) - PERFIL_HIERARQUIA.indexOf(b));
    }


    // Função auxiliar para formatar o nome do perfil para exibição
    function formatarPerfil(perfil) {
        switch (perfil) {
            case 'super_administrador': return 'Super Administrador';
            case 'administrador': return 'Administrador';
            case 'gerente': return 'Gerente';
            case 'tecnico': return 'Técnico';
            case 'usuario': return 'Usuário Comum';
            default: return perfil;
        }
    }

    // --- Funções para o modal de alerta/confirmação (assumindo que são globais ou definidas aqui) ---
    // Estes elementos devem estar presentes no HTML (modal.jsp ou similar)
    const alertModal = document.getElementById('alertModal');
    const alertModalTitle = document.getElementById('alertModalTitle');
    const alertModalMessage = document.getElementById('alertModalMessage');
    const alertModalOkButton = document.getElementById('alertModalOkButton');

    const confirmModal = document.getElementById('confirmModal');
    const confirmModalTitle = document.getElementById('confirmModalTitle');
    const confirmModalMessage = document.getElementById('confirmModalMessage');
    const confirmModalConfirmButton = document.getElementById('confirmModalConfirmButton');
    const confirmModalCancelButton = document.getElementById('confirmModalCancelButton');

    const requiredFieldsModal = document.getElementById('requiredFieldsModal'); // Adicionado
    const requiredFieldsList = document.getElementById('requiredFieldsList');   // Adicionado
    const requiredFieldsModalCloseButton = document.getElementById('requiredFieldsModalCloseButton'); // Adicionado

    let confirmResolve; // Variável para resolver a Promise do modal de confirmação

    /**
     * Exibe um modal de alerta customizado.
     * @param {string} title Título do modal.
     * @param {string} message Mensagem a ser exibida.
     * @param {string} type Tipo de alerta ('info', 'success', 'warning', 'error').
     * @returns {Promise<void>} Uma Promise que resolve quando o modal é fechado.
     */
    async function showAlertModal(title, message, type = 'info') {
        return new Promise(resolve => {
            if (!alertModal || !alertModalTitle || !alertModalMessage || !alertModalOkButton) {
                console.error("Elementos do alertModal não encontrados. Exibindo alert padrão.");
                alert(`${title}\n${message}`);
                resolve();
                return;
            }

            alertModalTitle.textContent = title;
            alertModalMessage.innerHTML = message;

            // Remove classes de tipo anteriores e adiciona a nova
            alertModal.querySelector('.modal-content').classList.remove('success', 'error', 'warning', 'info');
            alertModal.querySelector('.modal-content').classList.add(type);

            alertModal.classList.add('active'); // Exibe o modal

            const closeHandler = () => {
                alertModal.classList.remove('active'); // Esconde o modal
                alertModalOkButton.removeEventListener('click', closeHandler); // Remove o listener para evitar múltiplos
                resolve(); // Resolve a Promise
            };

            alertModalOkButton.addEventListener('click', closeHandler);
        });
    }

    /**
     * Exibe um modal de confirmação customizado.
     * @param {string} title Título do modal.
     * @param {string} message Mensagem a ser exibida.
     * @returns {Promise<boolean>} Uma Promise que resolve com true se confirmado, false se cancelado.
     */
    async function showConfirmModal(title, message) {
        return new Promise(resolve => {
            if (!confirmModal || !confirmModalTitle || !confirmModalMessage || !confirmModalConfirmButton || !confirmModalCancelButton) {
                console.error("Elementos do confirmModal não encontrados. Exibindo confirm padrão.");
                resolve(confirm(`${title}\n${message}`)); // Fallback para confirm() do navegador
                return;
            }

            confirmResolve = resolve; // Armazena a função resolve para ser chamada pelos handlers dos botões

            confirmModalTitle.textContent = title;
            confirmModalMessage.textContent = message;

            confirmModal.querySelector('.modal-content').classList.remove('success', 'error', 'warning', 'info'); // Limpa classes de tipo
            confirmModal.classList.add('active'); // Exibe o modal

            const confirmHandler = () => {
                confirmModal.classList.remove('active');
                confirmModalConfirmButton.removeEventListener('click', confirmHandler);
                confirmModalCancelButton.removeEventListener('click', cancelHandler);
                confirmResolve(true); // Confirma
            };

            const cancelHandler = () => {
                confirmModal.classList.remove('active');
                confirmModalConfirmButton.removeEventListener('click', confirmHandler);
                confirmModalCancelButton.removeEventListener('click', cancelHandler);
                confirmResolve(false); // Cancela
            };

            confirmModalConfirmButton.addEventListener('click', confirmHandler);
            confirmModalCancelButton.addEventListener('click', cancelHandler);
        });
    }

    // NOVO: Função para exibir o modal de campos obrigatórios (se houver)
    /**
     * Exibe um modal listando campos obrigatórios que estão faltando.
     * @param {Array<string>} missingFields Lista de nomes dos campos faltando.
     * @returns {Promise<void>} Uma Promise que resolve quando o modal é fechado.
     */
    async function showRequiredFieldsModal(missingFields) {
        return new Promise(resolve => {
            if (!requiredFieldsModal || !requiredFieldsList || !requiredFieldsModalCloseButton) {
                console.error("Elementos do requiredFieldsModal não encontrados. Exibindo alert padrão.");
                alert("Campos obrigatórios faltando:\n" + missingFields.join('\n'));
                resolve();
                return;
            }

            requiredFieldsList.innerHTML = ''; // Limpa a lista anterior
            missingFields.forEach(field => {
                const li = document.createElement('li');
                li.textContent = field;
                requiredFieldsList.appendChild(li);
            });

            requiredFieldsModal.classList.add('active'); // Exibe o modal

            const closeHandler = () => {
                requiredFieldsModal.classList.remove('active');
                requiredFieldsModalCloseButton.removeEventListener('click', closeHandler);
                resolve();
            };

            requiredFieldsModalCloseButton.addEventListener('click', closeHandler);
        });
    }


    // Fechar modais clicando fora do conteúdo (no overlay)
    window.addEventListener('click', function(event) {
        if (alertModal && event.target === alertModal) {
            alertModal.classList.remove('active');
        }
        if (confirmModal && event.target === confirmModal) {
            confirmModal.classList.remove('active');
            if (confirmResolve) {
                confirmResolve(false); // Resolve como false se o modal de confirmação for fechado clicando fora
            }
        }
        if (requiredFieldsModal && event.target === requiredFieldsModal) { // Adicionado
            requiredFieldsModal.classList.remove('active');
        }
    });

    // Fechar modais com a tecla ESC
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            if (alertModal && alertModal.classList.contains('active')) {
                alertModal.classList.remove('active');
            }
            if (confirmModal && confirmModal.classList.contains('active')) {
                confirmModal.classList.remove('active');
                if (confirmResolve) {
                    confirmResolve(false); // Resolve como false se o modal de confirmação for fechado com ESC
                }
            }
            if (requiredFieldsModal && requiredFieldsModal.classList.contains('active')) { // Adicionado
                requiredFieldsModal.classList.remove('active');
            }
        }
    });


    // --- Roteamento de páginas e inicialização ---
    // A ordem é importante: verificar se é página de cadastro/edição primeiro
    // para garantir que as variáveis globais de módulos sejam populadas antes de setupCadastroUsuarioForm ser chamado.

	const currentPath = window.location.pathname;
	    const isCadastroPage = document.getElementById('usuarioForm') !== null;
	    const isListPage = document.getElementById('tableBody') !== null;

	    if (isCadastroPage) {
	        console.log("DEBUG JS: Iniciando setup do formulário de cadastro.");
	        setupCadastroUsuarioForm(); 
	    } 
	    
	    if (isListPage) {
	        console.log("DEBUG JS: Iniciando carga de dados da listagem.");
	        loadUsersData();
	        
	        if (selecionarTodosUsuariosCheckbox) {
	            selecionarTodosUsuariosCheckbox.addEventListener('change', function() {
	                const checkboxes = document.querySelectorAll('.rowCheckbox:not([disabled])');
	                checkboxes.forEach(cb => cb.checked = this.checked);
	                updateButtonStates();
	            });
	        }

	        if (btnNovoUsuario) {
	            btnNovoUsuario.addEventListener('click', () => {
	                window.location.href = `${APP_CONTEXT_PATH}/CadastrarUsuarioServlet`;
	            });
	        }

	        if (botaoEditarSelecionadosUsuarios) {
	            botaoEditarSelecionadosUsuarios.addEventListener('click', handleEditarSelecionadosUsuarios);
	        }

	        if (botaoExcluirSelecionadosUsuarios) {
	            botaoExcluirSelecionadosUsuarios.addEventListener('click', handleExcluirSelecionadosUsuarios);
	        }
	        updateButtonStates();
	    }
    // Para outras páginas, o script não fará nada ou pode ter lógica adicional se necessário.
});