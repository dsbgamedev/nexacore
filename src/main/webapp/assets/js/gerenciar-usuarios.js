let listaUsuariosGlobal = [];

document.addEventListener("DOMContentLoaded", function() {
    carregarUsuarios();

    const inputPesquisa = document.getElementById("filtroPesquisa");
    const selectPerfil = document.getElementById("filtroPerfil");
    const selectAtivo = document.getElementById("filtroAtivo");

    if (inputPesquisa) inputPesquisa.addEventListener("input", filtrarUsuarios);
    if (selectPerfil) selectPerfil.addEventListener("change", filtrarUsuarios);
    if (selectAtivo) selectAtivo.addEventListener("change", filtrarUsuarios);
});

function carregarUsuarios() {
    fetch(contextPath + "/GerenciarUsuariosServlet?action=list", {
        method: "GET",
        headers: {
            "X-Requested-With": "XMLHttpRequest"
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            listaUsuariosGlobal = data.users || [];
            filtrarUsuarios(); 
        } else {
            ModalService.error("Erro", "Erro ao carregar usuários: " + (data.error || "Erro desconhecido"));
        }
    })
    .catch(error => console.error("Erro na requisição:", error));
}

function filtrarUsuarios() {
    const termo = document.getElementById("filtroPesquisa").value.toLowerCase().trim();
    const perfilFiltro = document.getElementById("filtroPerfil").value;
    const ativoFiltro = document.getElementById("filtroAtivo").value;

    const filtrados = listaUsuariosGlobal.filter(u => {
        const idStr = String(u.id || '');
        const usernameStr = (u.username || '').toLowerCase();
        const nomeStr = (u.nomeCompleto || u.nome || '').toLowerCase();
        const emailStr = (u.email || '').toLowerCase();
        const perfilStr = (u.perfil || '').toLowerCase();
        const filialStr = (u.unidadeAtivaNome || u.unidadePrincipal || u.filialPrincipal || '').toLowerCase();

        const combinaTexto = !termo || 
            idStr.includes(termo) || 
            usernameStr.includes(termo) || 
            nomeStr.includes(termo) || 
            emailStr.includes(termo) || 
            perfilStr.includes(termo) || 
            filialStr.includes(termo);

        const combinaPerfil = !perfilFiltro || u.perfil === perfilFiltro;

        let combinaAtivo = true;
        if (ativoFiltro !== "") {
            const boolAtivo = (ativoFiltro === "true");
            combinaAtivo = (u.ativo === boolAtivo);
        }

        return combinaTexto && combinaPerfil && combinaAtivo;
    });

    preencherTabela(filtrados);
}

function preencherTabela(usuarios) {
    const tbody = document.getElementById("tabelaUsuariosBody");
    const contador = document.getElementById("contadorRegistros");
    tbody.innerHTML = "";
    
    if (!usuarios || usuarios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-3 text-muted">Nenhum usuário encontrado.</td></tr>`;
        contador.textContent = "0 registros";
        return;
    }

    contador.textContent = usuarios.length + " registro(s)";

    usuarios.forEach(u => {
        let tr = document.createElement("tr");
		
		// === ESTA É A PARTE QUE FALTAVA ADICIONAR ===
        if (!u.ativo) {
            tr.classList.add("usuario-inativo");
        }

        let statusBadge = u.ativo 
            ? `<span class="badge bg-success">Ativo</span>` 
            : `<span class="badge bg-secondary">Inativo</span>`;

        // Define o ícone e o texto do botão de status dinamicamente (se está ativo, mostra opção para desativar e vice-versa)
        let iconeStatus = u.ativo ? "bi-toggle-on text-success" : "bi-toggle-off text-secondary";
        let tituloStatus = u.ativo ? "Desativar Usuário" : "Ativar Usuário";

        let nomeCompleto = u.nomeCompleto || u.nome || '-';
        let filialNome = u.unidadeAtivaNome || u.unidadePrincipal || u.filialPrincipal || '-';
        let ultimoAcessoFormatado = u.ultimoAcesso || '-'; 

        tr.innerHTML = `
            <td>${u.id}</td>
            <td><strong>${u.username || ''}</strong></td>
            <td>${nomeCompleto}</td>
            <td>${u.email || ''}</td>
            <td><span class="badge bg-primary">${u.perfil || ''}</span></td>
            <td>${filialNome}</td>
            <td>${ultimoAcessoFormatado}</td>
            <td>${statusBadge}</td>
            <td class="text-center" style="white-space: nowrap;">
                <!-- Botão Editar -->
                <a href="${contextPath}/CadastrarUsuarioServlet?action=edit&id=${u.id}" class="btn btn-outline-primary btn-sm me-1" title="Editar">
                    <i class="bi bi-pencil"></i>
                </a>
                <!-- Botão Alternar Status (Ativar / Desativar) -->
                <button class="btn btn-outline-secondary btn-sm me-1" title="${tituloStatus}" onclick="alternarStatus(${u.id})">
                    <i class="bi ${iconeStatus}"></i>
                </button>
                <!-- Botão Exclusão Definitiva -->
                <button class="btn btn-outline-danger btn-sm" title="Excluir Permanentemente" onclick="excluirUsuario(${u.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/**
 * Apenas altera o status entre Ativo e Inativo chamando a action "toggleStatus"
 */
async function alternarStatus(id) {
    const confirmado = await ModalService.confirm(
        "Alterar Situação", 
        "Deseja realmente alterar o status (ativo/inativo) deste usuário?", 
        "warning"
    );

    if (!confirmado) return;

    fetch(contextPath + "/GerenciarUsuariosServlet", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest"
        },
        body: JSON.stringify({ action: "toggleStatus", id: id })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            ModalService.success("Sucesso", data.message).then(() => {
                carregarUsuarios();
            });
        } else {
            ModalService.error("Erro", data.message || "Não foi possível alterar o status.");
        }
    })
    .catch(error => {
        console.error("Erro:", error);
        ModalService.error("Erro", "Ocorreu um erro na requisição.");
    });
}

/**
 * Remove o usuário permanentemente do banco chamando a action "delete"
 */
async function excluirUsuario(id) {
    const confirmado = await ModalService.confirm(
        "Exclusão Definitiva", 
        "Atenção: Esta ação excluirá o usuário permanentemente do sistema. Deseja continuar?", 
        "error"
    );

    if (!confirmado) return;

    fetch(contextPath + "/GerenciarUsuariosServlet", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest"
        },
        body: JSON.stringify({ action: "delete", id: id })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            ModalService.success("Sucesso", data.message).then(() => {
                carregarUsuarios();
            });
        } else {
            ModalService.error("Erro", data.message || "Não foi possível excluir o usuário.");
        }
    })
    .catch(error => {
        console.error("Erro:", error);
        ModalService.error("Erro", "Ocorreu um erro na requisição.");
    });
}