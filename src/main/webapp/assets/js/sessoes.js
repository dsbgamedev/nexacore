document.addEventListener("DOMContentLoaded", function() {
    const contextPath = document.body.getAttribute('data-context-path') || '';
    const tabelaCorpo = document.getElementById("tabela-sessoes-corpo");
    const contadorRegistros = document.getElementById("contador-sessoes");

    let dadosGlobais = [];
    let colunaAtual = 'nomeUsuario';
    let ordemAscendente = true;

    carregarSessoesAtivas();
    
    // Atualiza as sessões ativas automaticamente em tempo real a cada 10 segundos
    setInterval(carregarSessoesAtivas, 10000);

    async function carregarSessoesAtivas() {
        try {
            const response = await fetch(`${contextPath}/api/sessoes/listar`);
            dadosGlobais = await response.json();
            ordenarEExibir();
        } catch (error) {
            console.error("Erro ao carregar sessões ativas:", error);
        }
    }

    function ordenarEExibir() {
        dadosGlobais.sort((a, b) => {
            let valA = a[colunaAtual] || '';
            let valB = b[colunaAtual] || '';
            
            // Tratamento especial para valores numéricos ou strings
            if (colunaAtual === 'ultimoAcesso' || colunaAtual === 'dataCriacao') {
                valA = parseInt(valA) || 0;
                valB = parseInt(valB) || 0;
            }

            if (valA < valB) return ordemAscendente ? -1 : 1;
            if (valA > valB) return ordemAscendente ? 1 : -1;
            return 0;
        });
        renderizarTabela(dadosGlobais);
    }

    function renderizarTabela(dados) {
        tabelaCorpo.innerHTML = "";
        contadorRegistros.textContent = `${dados.length} sessão(ões) ativa(s)`;

        if (dados.length === 0) {
            tabelaCorpo.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Nenhuma sessão ativa encontrada no momento.</td></tr>`;
            return;
        }

		dados.forEach(u => {
            const tr = document.createElement("tr");
			
			// Cria uma versão encurtada do ID da sessão para exibição limpa
			const idCurto = u.sessionId ? u.sessionId.substring(0, 8) + '...' : '';
            tr.innerHTML = `
			<td><code title="${u.sessionId}">${idCurto}</code></td>
	            <td class="fw-bold">${u.nomeUsuario}</td>
	            <td>${u.emailUsuario || '-'}</td>
	            <td><span class="badge bg-info-subtle text-info"><i class="fa fa-clock me-1"></i>${u.tempoAtivo || '-'}</span></td>
	            <td><span class="badge bg-warning-subtle text-dark"><i class="fa fa-microchip me-1"></i>${u.consumoMemoria || '0 KB'}</span></td>
	            <td>${formatarData(u.ultimoAcesso)}</td>
	            <td class="text-center">
	                <button class="btn btn-sm btn-outline-danger btn-derrubar" title="Derrubar Sessão" data-id="${u.sessionId}">
	                    <i class="fa fa-power-off me-1"></i> Derrubar
	                </button>
	            </td>
	        `;
            tabelaCorpo.appendChild(tr);
        });
        // Eventos para os botões de derrubar sessão
        document.querySelectorAll('.btn-derrubar').forEach(btn => {
            btn.addEventListener('click', async function() {
                const sessionId = this.getAttribute('data-id');
                await derrubarSessao(sessionId);
            });
        });
    }

    // Configura eventos de clique para ordenação nos cabeçalhos da tabela
    document.querySelectorAll('th.sortable').forEach(th => {
        th.style.cursor = 'pointer';
        th.addEventListener('click', function() {
            const coluna = this.getAttribute('data-column');
            if (colunaAtual === coluna) {
                ordemAscendente = !ordemAscendente;
            } else {
                colunaAtual = coluna;
                ordemAscendente = true;
            }
            ordenarEExibir();
        });
    });

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
        if (!timestampStr) return '-';
        const data = new Date(parseInt(timestampStr));
        if (isNaN(data.getTime())) return '-';
        return data.toLocaleDateString('pt-BR') + ' ' + data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }
});