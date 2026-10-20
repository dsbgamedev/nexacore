document.addEventListener("DOMContentLoaded", function() {
    const contextPath = document.body.getAttribute('data-context-path') || '';
    const tabelaCorpo = document.getElementById("tabela-sessoes-corpo");
    const contadorRegistros = document.getElementById("contador-sessoes");

    carregarSessoesAtivas();

    async function carregarSessoesAtivas() {
        try {
            const response = await fetch(`${contextPath}/api/sessoes/listar`);
            const sessoes = await response.json();
            renderizarTabela(sessoes);
        } catch (error) {
            console.error("Erro ao carregar sessões ativas:", error);
        }
    }

    function renderizarTabela(dados) {
        tabelaCorpo.innerHTML = "";
        contadorRegistros.textContent = `${dados.length} sessão(ões) ativa(s)`;

        if (dados.length === 0) {
            tabelaCorpo.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Nenhuma sessão ativa encontrada no momento.</td></tr>`;
            return;
        }

        dados.forEach(u => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td><code>${u.sessionId}</code></td>
                <td class="fw-bold">${u.nomeUsuario}</td>
                <td>${u.emailUsuario || '-'}</td>
                <td>${formatarData(u.ultimoAcesso)}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger btn-derrubar" title="Derrubar Sessão" data-id="${u.sessionId}">
                        <i class="fa fa-power-off me-1"></i> Derrubar
                    </button>
                </td>
            `;
            tabelaCorpo.appendChild(tr);
        });

        document.querySelectorAll('.btn-derrubar').forEach(btn => {
            btn.addEventListener('click', async function() {
                const sessionId = this.getAttribute('data-id');
                await derrubarSessao(sessionId);
            });
        });
    }

    async function derrubarSessao(sessionId) {
        if (!confirm("Deseja realmente derrubar a sessão deste usuário?")) return;

        try {
            const response = await fetch(`${contextPath}/api/sessoes/excluir?id=${sessionId}`, { method: 'DELETE' });
            const result = await response.json();

            if (response.ok) {
                alert(result.mensagem || "Sessão encerrada com sucesso.");
                carregarSessoesAtivas();
            } else {
                alert(result.erro || "Não foi possível encerrar a sessão.");
            }
        } catch (error) {
            console.error("Erro técnico ao encerrar sessão:", error);
            alert("Falha de comunicação com o servidor.");
        }
    }

    function formatarData(timestampStr) {
        const data = new Date(parseInt(timestampStr));
        if (isNaN(data.getTime())) return '-';
        return data.toLocaleDateString('pt-BR') + ' ' + data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }
});