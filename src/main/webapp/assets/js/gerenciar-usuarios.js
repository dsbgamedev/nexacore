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
            alert("Erro ao carregar usuários: " + (data.error || "Erro desconhecido"));
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
        
        // Incluído u.unidadeAtivaNome aqui para o filtro global funcionar com a filial
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

        let statusBadge = u.ativo 
            ? `<span class="badge bg-success">Ativo</span>` 
            : `<span class="badge bg-secondary">Inativo</span>`;

        // Mapeia corretamente u.unidadeAtivaNome para puxar a filial carregada pelo DAO
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
                <a href="${contextPath}/CadastrarUsuarioServlet?action=edit&id=${u.id}" class="btn btn-outline-primary btn-sm me-1" title="Editar">
                    <i class="bi bi-pencil"></i>
                </a>
                <button class="btn btn-outline-danger btn-sm" title="Excluir/Desativar" onclick="desativarUsuario(${u.id})">
                    <i class="bi bi-person-x"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function desativarUsuario(id) {
    if (!confirm("Deseja realmente excluir/desativar este usuário?")) {
        return;
    }

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
            alert(data.message);
            carregarUsuarios(); 
        } else {
            alert("Erro: " + (data.message || "Não foi possível excluir."));
        }
    })
    .catch(error => console.error("Erro:", error));
}