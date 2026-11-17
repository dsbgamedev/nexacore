document.addEventListener("DOMContentLoaded", function() {
    
    const viewListagem = document.getElementById("view-listagem");
    const viewFormulario = document.getElementById("view-formulario");

    const btnNovo = document.getElementById("btn-novo");
    const btnVoltar = document.getElementById("btn-voltar");
    const linkVoltarBreadcrumb = document.getElementById("link-voltar-breadcrumb");

    const btnPesquisar = document.getElementById("btn-pesquisar");
    const btnLimparFiltros = document.getElementById("btn-limpar-filtros");
    const filtroNome = document.getElementById("filtro-nome");
    const filtroSituacao = document.getElementById("filtro-situacao");
    const tabelaCorpo = document.getElementById("tabela-corpo");
    const contadorRegistros = document.getElementById("contador-registros");

    const inputId = document.getElementById("input-id");
    const inputNomeMarca = document.getElementById("input-nomemarca");
    const inputFabricante = document.getElementById("input-fabricante");
    const inputLogoUrl = document.getElementById("input-logourl");
    const inputAtivo = document.getElementById("input-ativo");
    const inputDataCadastro = document.getElementById("input-datacadastro");
    const formTitulo = document.getElementById("form-titulo");

    const btnSalvar = document.getElementById("btn-salvar");
    const btnSalvarNovo = document.getElementById("btn-salvar-novo");
    const btnExcluir = document.getElementById("btn-excluir");

    let listaGlobalMarcas = [];
    let listaGlobalFabricantes = [];
    
    // Configurações de Paginação (15 itens por página)
    let paginaAtual = 1;
    const itensPorPagina = 15;
    let listaFiltradaAtual = [];

    carregarFabricantesSelect();
    carregarMarcas();

    btnNovo.addEventListener("click", () => abrirFormulario());
    btnVoltar.addEventListener("click", (e) => { e.preventDefault(); fecharFormulario(); });
    linkVoltarBreadcrumb.addEventListener("click", (e) => { e.preventDefault(); fecharFormulario(); });
	
    const inputsFormulario = viewFormulario.querySelectorAll("input, select, textarea");
    inputsFormulario.forEach((el, index) => {
        el.addEventListener("keydown", function(e) {
            if (e.key === "Enter") {
                e.preventDefault(); 
                const proximoElemento = inputsFormulario[index + 1];
                if (proximoElemento) {
                    proximoElemento.focus();
                    if (proximoElemento.tagName === "INPUT" && proximoElemento.type === "text") {
                        proximoElemento.select();
                    }
                }
            }
        });
    });

    async function carregarFabricantesSelect() {
        try {
            const response = await fetch('/nexacore/api/fabricantes/listar');
            listaGlobalFabricantes = await response.json();
            
            inputFabricante.innerHTML = '<option value="">Selecione um fabricante...</option>';
            listaGlobalFabricantes.forEach(f => {
                const opt = document.createElement("option");
                opt.value = f.idFabricante;
                opt.textContent = f.razaoSocial;
                inputFabricante.appendChild(opt);
            });
        } catch (error) {
            console.error("Erro ao carregar fabricantes para o select:", error);
        }
    }

    function abrirFormulario(marca = null) {
        viewListagem.style.display = "none";
        viewFormulario.style.display = "block";

        if (marca) {
            formTitulo.textContent = "Editar Marca";
            inputId.value = marca.idMarca;
            inputNomeMarca.value = marca.nomeMarca || "";
            inputFabricante.value = marca.idFabricante || "";
            inputLogoUrl.value = marca.logoUrl || "";
            inputAtivo.value = marca.ativo ? "true" : "false";
            inputDataCadastro.value = marca.dataCadastro ? formatarData(marca.dataCadastro) : "";
            btnExcluir.style.display = "inline-block";
        } else {
            formTitulo.textContent = "Nova Marca";
            inputId.value = "";
            inputNomeMarca.value = "";
            inputFabricante.value = "";
            inputLogoUrl.value = "";
            inputAtivo.value = "true";
            inputDataCadastro.value = "Gerada automaticamente ao salvar";
            btnExcluir.style.display = "none";
        }
    }

	function fecharFormulario() {
        viewFormulario.style.display = "none";
        viewListagem.style.display = "block";
        filtroNome.value = "";
        filtroSituacao.value = "";
        carregarMarcas();
    }

    async function carregarMarcas() {
        try {
            const response = await fetch('/nexacore/api/marcas/listar');
            listaGlobalMarcas = await response.json();
            
            // Inicia aguardando pesquisa do usuário, igual à estrutura anterior
            tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Utilize os filtros acima para realizar uma pesquisa.</td></tr>`;
            contadorRegistros.textContent = `0 registros encontrados`;
            const infoPaginacao = document.getElementById("contador-paginacao-info");
            if(infoPaginacao) infoPaginacao.textContent = "Nenhum registro";
            document.getElementById("paginacao-container").innerHTML = "";
        } catch (error) {
            console.error("Erro ao carregar marcas:", error);
            ModalService.error("Erro", "Não foi possível carregar a lista de marcas.");
        }
    }

    function renderizarTabelaComPaginacao(dados) {
        listaFiltradaAtual = dados;
        const totalRegistros = dados.length;
        contadorRegistros.textContent = `${totalRegistros} registros encontrados`;

        if (totalRegistros === 0) {
            tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Nenhum registro encontrado.</td></tr>`;
            document.getElementById("paginacao-container").innerHTML = "";
            const infoPaginacao = document.getElementById("contador-paginacao-info");
            if(infoPaginacao) infoPaginacao.textContent = "Nenhum registro";
            return;
        }

        const totalPaginas = Math.ceil(totalRegistros / itensPorPagina);
        if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;

        const inicio = (paginaAtual - 1) * itensPorPagina;
        const fim = Math.min(inicio + itensPorPagina, totalRegistros);
        const itensPagina = dados.slice(inicio, fim);

        const infoPaginacao = document.getElementById("contador-paginacao-info");
        if(infoPaginacao) {
            infoPaginacao.textContent = `Mostrando de ${inicio + 1} até ${fim} de ${totalRegistros} registros`;
        }

        tabelaCorpo.innerHTML = "";
        itensPagina.forEach(m => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${m.idMarca}</td>
                <td class="fw-bold">${m.nomeMarca}</td>
                <td>${m.nomeFabricante || '-'}</td>
                <td>${m.logoUrl ? `<a href="${m.logoUrl}" target="_blank" title="${m.logoUrl}">Ver Link</a>` : '-'}</td>
                <td><span class="badge ${m.ativo ? 'bg-success' : 'bg-secondary'}">${m.ativo ? 'Ativo' : 'Inativo'}</span></td>
                <td>${m.dataCadastro ? formatarData(m.dataCadastro) : '-'}</td>
                <td class="text-center">
				    <button class="btn btn-sm btn-outline-secondary btn-detalhes me-1" title="Visualizar Detalhes" data-id="${m.idMarca}"><i class="fa fa-search"></i></button>
                    <button class="btn btn-sm btn-outline-primary btn-editar me-1" title="Editar" data-id="${m.idMarca}"><i class="fa fa-pen"></i></button>
                    <button class="btn btn-sm btn-outline-danger btn-excluir-linha" title="Excluir" data-id="${m.idMarca}"><i class="fa fa-trash"></i></button>
                </td>
            `;
            tabelaCorpo.appendChild(tr);
        });

        vincularEventosBotoesTabela();
        renderizarControlesPaginacao(totalPaginas);
    }

    function renderizarControlesPaginacao(totalPaginas) {
        const container = document.getElementById("paginacao-container");
        if(!container) return;
        container.innerHTML = "";

        if (totalPaginas <= 1) return;

        // Botão Primeira
        const liPrimeira = document.createElement("li");
        liPrimeira.className = `page-item ${paginaAtual === 1 ? 'disabled' : ''}`;
        liPrimeira.innerHTML = `<a class="page-link" href="#">&laquo;&laquo; Primeira</a>`;
        liPrimeira.onclick = (e) => { e.preventDefault(); if(paginaAtual > 1) { paginaAtual = 1; renderizarTabelaComPaginacao(listaFiltradaAtual); } };
        container.appendChild(liPrimeira);

        // Botão Anterior
        const liAnterior = document.createElement("li");
        liAnterior.className = `page-item ${paginaAtual === 1 ? 'disabled' : ''}`;
        liAnterior.innerHTML = `<a class="page-link" href="#">Anterior</a>`;
        liAnterior.onclick = (e) => { e.preventDefault(); if(paginaAtual > 1) { paginaAtual--; renderizarTabelaComPaginacao(listaFiltradaAtual); } };
        container.appendChild(liAnterior);

        // Intervalo numérico inteligente
        let inicioPag = Math.max(1, paginaAtual - 2);
        let fimPag = Math.min(totalPaginas, paginaAtual + 2);

        if (inicioPag > 1) {
            container.appendChild(criarBotaoPagina(1));
            if (inicioPag > 2) container.appendChild(criarBotaoElipses());
        }

        for (let i = inicioPag; i <= fimPag; i++) {
            container.appendChild(criarBotaoPagina(i));
        }

        if (fimPag < totalPaginas) {
            if (fimPag < totalPaginas - 1) container.appendChild(criarBotaoElipses());
            container.appendChild(criarBotaoPagina(totalPaginas));
        }

        // Botão Próximo
        const liProximo = document.createElement("li");
        liProximo.className = `page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}`;
        liProximo.innerHTML = `<a class="page-link" href="#">Próximo</a>`;
        liProximo.onclick = (e) => { e.preventDefault(); if(paginaAtual < totalPaginas) { paginaAtual++; renderizarTabelaComPaginacao(listaFiltradaAtual); } };
        container.appendChild(liProximo);

        // Botão Última
        const liUltima = document.createElement("li");
        liUltima.className = `page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}`;
        liUltima.innerHTML = `<a class="page-link" href="#">Última &raquo;&raquo;</a>`;
        liUltima.onclick = (e) => { e.preventDefault(); if(paginaAtual < totalPaginas) { paginaAtual = totalPaginas; renderizarTabelaComPaginacao(listaFiltradaAtual); } };
        container.appendChild(liUltima);
    }

    function criarBotaoPagina(num) {
        const li = document.createElement("li");
        li.className = `page-item ${paginaAtual === num ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${num}</a>`;
        li.onclick = (e) => {
            e.preventDefault();
            paginaAtual = num;
            renderizarTabelaComPaginacao(listaFiltradaAtual);
        };
        return li;
    }

    function criarBotaoElipses() {
        const li = document.createElement("li");
        li.className = "page-item disabled";
        li.innerHTML = `<a class="page-link" href="#">...</a>`;
        return li;
    }

    function vincularEventosBotoesTabela() {
        document.querySelectorAll('.btn-detalhes').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = parseInt(this.getAttribute('data-id'));
                const marca = listaGlobalMarcas.find(item => item.idMarca === id);
                if (marca) mostrarDetalhesMarcas(marca);
            });
        });

        document.querySelectorAll('.btn-editar').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = parseInt(this.getAttribute('data-id'));
                const marca = listaGlobalMarcas.find(item => item.idMarca === id);
                if (marca) abrirFormulario(marca);
            });
        });

        document.querySelectorAll('.btn-excluir-linha').forEach(btn => {
            btn.addEventListener('click', async function() {
                const id = parseInt(this.getAttribute('data-id'));
                await excluirMarca(id);
            });
        });
    }
	
	async function mostrarDetalhesMarcas(m) {
	       const conteudoHtml = `
	           <div style="text-align: left; font-size: 14px;">
	               <p class="mb-2"><strong>ID da Marca:</strong> ${m.idMarca}</p>
	               <p class="mb-2"><strong>Nome da Marca:</strong> ${m.nomeMarca}</p>
	               <p class="mb-2"><strong>Fabricante Vinculado:</strong> ${m.nomeFabricante || '-'}</p>
	               <p class="mb-2"><strong>Logo URL:</strong> ${m.logoUrl ? `<a href="${m.logoUrl}" target="_blank">${m.logoUrl}</a>` : '-'}</p>
	               <p class="mb-2"><strong>Situação:</strong> ${m.ativo ? 'Ativo' : 'Inativo'}</p>
	               <p class="mb-0"><strong>Data de Cadastro:</strong> ${m.dataCadastro ? formatarData(m.dataCadastro) : '-'}</p>
	           </div>
	       `;

	       if (typeof ModalService.info === 'function') {
	           await ModalService.info("Detalhes da Marca", conteudoHtml);
	       } else {
	           await ModalService.alert("Detalhes da Marca", conteudoHtml);
	       }
	}

	function realizarPesquisa() {
        paginaAtual = 1; 
        const termo = filtroNome.value.toLowerCase().trim();
        const termoSituacao = filtroSituacao.value;

        const filtrados = listaGlobalMarcas.filter(m => {
            const matchNome = termo === "" || m.nomeMarca.toLowerCase().includes(termo) || (m.nomeFabricante && m.nomeFabricante.toLowerCase().includes(termo));
            
            let matchSit = true;
            if (termoSituacao === "true") {
                matchSit = m.ativo === true;
            } else if (termoSituacao === "false") {
                matchSit = m.ativo === false;
            }

            return matchNome && matchSit;
        });

        renderizarTabelaComPaginacao(filtrados);
    }

    btnPesquisar.addEventListener("click", realizarPesquisa);
    filtroNome.addEventListener("input", realizarPesquisa);

    filtroNome.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            realizarPesquisa();
        }
    });

    filtroSituacao.addEventListener("change", realizarPesquisa);

    btnLimparFiltros.addEventListener("click", () => {
        filtroNome.value = "";
        filtroSituacao.value = ""; 
        tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Utilize os filtros acima para realizar uma pesquisa.</td></tr>`;
        contadorRegistros.textContent = `0 registros encontrados`;
        const infoPaginacao = document.getElementById("contador-paginacao-info");
        if(infoPaginacao) infoPaginacao.textContent = "Nenhum registro";
        document.getElementById("paginacao-container").innerHTML = "";
    });

    btnSalvar.addEventListener("click", () => executarSalvar(false));
    btnSalvarNovo.addEventListener("click", () => executarSalvar(true));

    async function executarSalvar(continuarNovo) {
        const nomeMarca = inputNomeMarca.value.trim();
        const idFabricante = inputFabricante.value;

        if (!nomeMarca || !idFabricante) {
            await ModalService.error("Atenção", "Os campos Nome da Marca e Fabricante são obrigatórios.");
            return;
        }

        const payload = {
            idMarca: inputId.value ? parseInt(inputId.value) : 0,
            idFabricante: parseInt(idFabricante),
            nomeMarca: nomeMarca,
            logoUrl: inputLogoUrl.value.trim(),
            ativo: inputAtivo.value === "true"
        };

        try {
            const response = await fetch('/nexacore/api/marcas/salvar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (response.ok) {
                await ModalService.success("Sucesso", result.mensagem);
                if (continuarNovo) {
                    abrirFormulario(null);
                } else {
                    fecharFormulario();
                }
            } else {
                await ModalService.error("Erro", result.erro || "Erro ao salvar registro.");
            }
        } catch (error) {
            console.error("Erro na requisição:", error);
            await ModalService.error("Erro Técnico", "Não foi possível comunicar com o servidor.");
        }
    }

    btnExcluir.addEventListener("click", async function() {
        const id = parseInt(inputId.value);
        if (id) {
            await excluirMarca(id);
        }
    });

    async function excluirMarca(id) {
        const confirmado = await ModalService.confirm("Confirmação", "Deseja realmente excluir esta marca?");
        if (!confirmado) return;

        try {
            const response = await fetch(`/nexacore/api/marcas/excluir?id=${id}`, { method: 'DELETE' });
            const result = await response.json();

            if (response.ok) {
                await ModalService.success("Sucesso", result.mensagem || "Registro excluído com sucesso.");
                fecharFormulario();
            } else {
                await ModalService.error("Erro", result.erro || "Não foi possível excluir o registro.");
            }
        } catch (error) {
            console.error("Erro técnico na exclusão:", error);
            await ModalService.error("Erro Técnico", "Falha de comunicação com o servidor.");
        }
    }

    function formatarData(dataStr) {
        const data = new Date(dataStr);
        if (isNaN(data.getTime())) return dataStr;
        return data.toLocaleDateString('pt-BR') + ' ' + data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }
});