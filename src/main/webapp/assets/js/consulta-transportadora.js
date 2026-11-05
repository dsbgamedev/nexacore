document.addEventListener("DOMContentLoaded", function () {
    // 1. Variáveis de Paginação e Ordenação
    let paginaAtual = 1;
    const itensPorPagina = 15;
    let linhasOriginais = [];
    let colunaOrdenadaAtual = 0; // Começa por ID
    let ordemAscendente = true;  // Crescente por padrão

    const tbody = document.getElementById("tbodyTransportadoras");
    if (tbody) {
        const trs = Array.from(tbody.querySelectorAll("tr"));
        if (!(trs.length === 1 && trs[0].id === "linha-vazia")) {
            linhasOriginais = trs;
        }

        // Ordenação inicial padrão: ID Crescente
        if (linhasOriginais.length > 0) {
            ordenarDados(0, true);
        }
    }

    // Listener para cliques nos cabeçalhos da tabela para ordenação dinâmica
    document.querySelectorAll(".coluna-ordenavel").forEach(th => {
        th.addEventListener("click", function () {
            let colunaIndex = parseInt(this.getAttribute("data-coluna"));
            if (colunaOrdenadaAtual === colunaIndex) {
                ordemAscendente = !ordemAscendente;
            } else {
                colunaOrdenadaAtual = colunaIndex;
                ordemAscendente = true;
            }
            ordenarDados(colunaIndex, ordemAscendente);
        });
    });

    function ordenarDados(colunaIndex, ascendente) {
        linhasOriginais.sort((a, b) => {
            let valA = a.children[colunaIndex].innerText.trim();
            let valB = b.children[colunaIndex].innerText.trim();

            let numA = parseFloat(valA.replace(/\D/g, ''));
            let numB = parseFloat(valB.replace(/\D/g, ''));

            if (!isNaN(numA) && !isNaN(numB) && colunaIndex === 0) {
                return ascendente ? numA - numB : numB - numA;
            }

            return ascendente ? valA.localeCompare(valB) : valB.localeCompare(valA);
        });

        paginaAtual = 1;
        renderizarTabelaPaginada();
    }

    function renderizarTabelaPaginada() {
        if (!tbody) return;
        tbody.innerHTML = "";

        if (linhasOriginais.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">Nenhuma transportadora encontrada. Utilize os filtros acima.</td></tr>`;
            document.getElementById("ulPaginacao").innerHTML = "";
            document.getElementById("paginacao-info").innerHTML = "";
            return;
        }

        const totalItens = linhasOriginais.length;
        const totalPaginas = Math.ceil(totalItens / itensPorPagina);
        
        if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
        if (paginaAtual < 1) paginaAtual = 1;

        const inicio = (paginaAtual - 1) * itensPorPagina;
        const fim = inicio + itensPorPagina;
        const itensPagina = linhasOriginais.slice(inicio, fim);

        itensPagina.forEach(tr => tbody.appendChild(tr));

        document.getElementById("paginacao-info").innerText = 
            `Mostrando ${inicio + 1} a ${Math.min(fim, totalItens)} de ${totalItens} registros`;

        renderizarBotoesPaginacao(totalPaginas);
    }

	function renderizarBotoesPaginacao(totalPaginas) {
        const ul = document.getElementById("ulPaginacao");
        if (!ul) return;
        ul.innerHTML = "";

        if (totalPaginas <= 1) return;

        // Botão Primeira
        let liPrimeira = document.createElement("li");
        liPrimeira.className = `page-item ${paginaAtual === 1 ? 'disabled' : ''}`;
        liPrimeira.innerHTML = `<button class="page-link" type="button">Primeira</button>`;
        liPrimeira.querySelector("button").addEventListener("click", () => mudarPagina(1));
        ul.appendChild(liPrimeira);

        // Botão Anterior
        let liAnterior = document.createElement("li");
        liAnterior.className = `page-item ${paginaAtual === 1 ? 'disabled' : ''}`;
        liAnterior.innerHTML = `<button class="page-link" type="button">Anterior</button>`;
        liAnterior.querySelector("button").addEventListener("click", () => mudarPagina(paginaAtual - 1));
        ul.appendChild(liAnterior);

        // Lógica de Paginação com reticências (...) igual à imagem de referência
        let paginas = [];
        if (totalPaginas <= 7) {
            for (let i = 1; i <= totalPaginas; i++) paginas.push(i);
        } else {
            if (paginaAtual <= 4) {
                paginas = [1, 2, 3, 4, 5, '...', totalPaginas];
            } else if (paginaAtual >= totalPaginas - 3) {
                paginas = [1, '...', totalPaginas - 4, totalPaginas - 3, totalPaginas - 2, totalPaginas - 1, totalPaginas];
            } else {
                paginas = [1, '...', paginaAtual - 1, paginaAtual, paginaAtual + 1, '...', totalPaginas];
            }
        }

		paginas.forEach(p => {
            let li = document.createElement("li");
            if (p === '...') {
                li.className = "page-item disabled";
                li.innerHTML = `<span class="page-link">...</span>`;
            } else {
                let isActive = (p === paginaAtual);
                li.className = `page-item ${isActive ? 'active' : ''}`;
                
                let btn = document.createElement("button");
                btn.className = "page-link";
                btn.type = "button";
                btn.innerText = p;
                
                // Se for a página ativa, garantimos o texto branco e impedimos sobreposições
                if (isActive) {
                    btn.style.color = "#ffffff";
                    btn.style.backgroundColor = "#0d6efd";
                    btn.style.borderColor = "#0d6efd";
                }

                btn.addEventListener("click", () => mudarPagina(p));
                li.appendChild(btn);
            }
            ul.appendChild(li);
        });

        // Botão Próximo
        let liProximo = document.createElement("li");
        liProximo.className = `page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}`;
        liProximo.innerHTML = `<button class="page-link" type="button">Próximo</button>`;
        liProximo.querySelector("button").addEventListener("click", () => mudarPagina(paginaAtual + 1));
        ul.appendChild(liProximo);

        // Botão Última
        let liUltima = document.createElement("li");
        liUltima.className = `page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}`;
        liUltima.innerHTML = `<button class="page-link" type="button">Última</button>`;
        liUltima.querySelector("button").addEventListener("click", () => mudarPagina(totalPaginas));
        ul.appendChild(liUltima);
		}

	    window.mudarPagina = function(pagina) {
	        paginaAtual = pagina;
	        renderizarTabelaPaginada();
	        // Removido o window.scrollTo para a página não subir ao clicar
	    };

    // 2. Ações da Tabela (Visualizar, Editar, Excluir)
    document.addEventListener("click", async function (e) {
        let btnVisualizar = e.target.closest(".btn-visualizar");
        let btnEditar = e.target.closest(".btn-editar");
        let btnExcluir = e.target.closest(".btn-excluir");

        if (btnVisualizar) {
            let id = btnVisualizar.getAttribute("data-id");
            abrirDetalhesTransportadora(id);
        } else if (btnEditar) {
            let id = btnEditar.getAttribute("data-id");
            window.location.href = contextPath + "/TransportadoraServlet?acao=editar&id=" + id;
        } else if (btnExcluir) {
            let id = btnExcluir.getAttribute("data-id");
            let nomeTransportadora = btnExcluir.getAttribute("data-nome") || "esta transportadora";

            let confirmado = await ModalService.confirm(
                "Confirmar Exclusão",
                `Tem certeza que deseja excluir ${nomeTransportadora}? Esta ação não poderá ser desfeita.`,
                "warning"
            );

            if (confirmado) {
                excluirTransportadora(id);
            }
        }
    });

    function abrirDetalhesTransportadora(id) {
        fetch(contextPath + "/ConsultaTransportadoraServlet?acao=detalhes&id=" + id)
            .then(res => res.text())
            .then(html => {
                let container = document.getElementById("conteudoDetalhesTransportadora");
                if (container) {
                    container.innerHTML = html;
                    let modalEl = document.getElementById("modalDetalhesTransportadora");
                    if (modalEl && window.bootstrap) {
                        let modal = new bootstrap.Modal(modalEl);
                        modal.show();
                    }
                }
            })
            .catch(() => ModalService.error("Erro", "Erro ao carregar os detalhes da transportadora."));
    }

    async function excluirTransportadora(id) {
        try {
            let res = await fetch(contextPath + "/ConsultaTransportadoraServlet?acao=excluir&id=" + id, {
                method: "POST"
            });
            let data = await res.json();

            if (data.sucesso) {
                await ModalService.success("Sucesso!", "Transportadora excluída com sucesso.");
                location.reload();
            } else {
                await ModalService.error("Erro", data.mensagem || data.error || "Erro ao excluir registo.");
            }
        } catch (error) {
            await ModalService.error("Erro Interno", "Erro ao comunicar com o servidor para exclusão.");
        }
    }
});