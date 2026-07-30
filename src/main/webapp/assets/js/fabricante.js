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

    // Campos do Formulário
    const inputId = document.getElementById("input-id");
    const inputRazaoSocial = document.getElementById("input-razaosocial");
    const inputCnpj = document.getElementById("input-cnpj");
    const inputIe = document.getElementById("input-ie");
    const inputCep = document.getElementById("input-cep");
    const inputLogradouro = document.getElementById("input-logradouro");
    const inputNumero = document.getElementById("input-numero");
    const inputComplemento = document.getElementById("input-complemento");
    const inputBairro = document.getElementById("input-bairro");
    const inputCidade = document.getElementById("input-cidade");
    const inputEstado = document.getElementById("input-estado");
    const inputPais = document.getElementById("input-pais");
    const inputAtivo = document.getElementById("input-ativo");
    const inputDataCadastro = document.getElementById("input-datacadastro");
    const formTitulo = document.getElementById("form-titulo");

    const btnSalvar = document.getElementById("btn-salvar");
    const btnSalvarNovo = document.getElementById("btn-salvar-novo");
    const btnExcluir = document.getElementById("btn-excluir");

    let listaGlobalFabricantes = [];

    carregarFabricantes();

    btnNovo.addEventListener("click", () => abrirFormulario());
    btnVoltar.addEventListener("click", (e) => { e.preventDefault(); fecharFormulario(); });
    linkVoltarBreadcrumb.addEventListener("click", (e) => { e.preventDefault(); fecharFormulario(); });

    // Máscara dinâmica para o CNPJ
    if (inputCnpj) {
        inputCnpj.addEventListener("input", function(e) {
            let value = e.target.value.replace(/\D/g, ""); 
            if (value.length > 14) value = value.slice(0, 14); 

            value = value.replace(/^(\d{2})(\d)/, "$1.$2");
            value = value.replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3");
            value = value.replace(/\.(\d{3})(\d)/, ".$1/$2");
            value = value.replace(/(\d{4})(\d)/, "$1-$2");

            e.target.value = value;
        });
    }

    if (inputIe) {
        inputIe.addEventListener("input", function(e) {
            let value = e.target.value.replace(/[^\d.\-\/]/g, ""); 
            e.target.value = value;
        });
    }

    inputCep.addEventListener("input", function() {
        let valor = inputCep.value.replace(/\D/g, '');
        if (valor.length > 5) {
            inputCep.value = valor.replace(/^(\d{5})(\d{1,3})?$/, "$1-$2");
        } else {
            inputCep.value = valor;
        }
    });

    inputCep.addEventListener("blur", async function() {
        let cep = inputCep.value.replace(/\D/g, '');
        if (cep.length === 8) {
            try {
                const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
                const data = await response.json();
                
                if (!data.erro) {
                    inputLogradouro.value = data.logradouro || "";
                    inputBairro.value = data.bairro || "";
                    inputCidade.value = data.localidade || "";
                    inputEstado.value = data.uf || "";
                    inputNumero.focus();
                } else {
                    await ModalService.error("CEP não encontrado", "O CEP informado não foi localizado na base nacional.");
                }
            } catch (error) {
                console.error("Erro ao consultar CEP:", error);
            }
        }
    });

    function abrirFormulario(fab = null) {
        viewListagem.style.display = "none";
        viewFormulario.style.display = "block";

        if (fab) {
            formTitulo.textContent = "Editar Fabricante";
            inputId.value = fab.idFabricante;
            inputRazaoSocial.value = fab.razaoSocial || "";
            inputCnpj.value = fab.cnpj || "";
            inputIe.value = fab.inscricaoEstadual || "";
            inputCep.value = fab.cep || "";
            inputLogradouro.value = fab.logradouro || "";
            inputNumero.value = fab.numero || "";
            inputComplemento.value = fab.complemento || "";
            inputBairro.value = fab.bairro || "";
            inputCidade.value = fab.cidade || "";
            inputEstado.value = fab.estado || "";
            inputPais.value = fab.paisOrigem || "Brasil";
            inputAtivo.value = fab.ativo ? "true" : "false";
            inputDataCadastro.value = fab.dataCadastro ? formatarData(fab.dataCadastro) : "";
            btnExcluir.style.display = "inline-block";
        } else {
            formTitulo.textContent = "Novo Fabricante";
            inputId.value = "";
            inputRazaoSocial.value = "";
            inputCnpj.value = "";
            inputIe.value = "";
            inputCep.value = "";
            inputLogradouro.value = "";
            inputNumero.value = "";
            inputComplemento.value = "";
            inputBairro.value = "";
            inputCidade.value = "";
            inputEstado.value = "";
            inputPais.value = "Brasil";
            inputAtivo.value = "true";
            inputDataCadastro.value = "Gerada automaticamente ao salvar";
            btnExcluir.style.display = "none";
        }
    }

    function fecharFormulario() {
        viewFormulario.style.display = "none";
        viewListagem.style.display = "block";
        
        // Limpa os filtros ao retornar para a listagem
        filtroNome.value = "";
        filtroSituacao.value = "";
        
        carregarFabricantes();
    }

    async function carregarFabricantes() {
        try {
            const response = await fetch('/nexacore/api/fabricantes/listar');
            listaGlobalFabricantes = await response.json();
            
            // Inicia a tabela vazia aguardando o usuário pesquisar ou selecionar
            tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Utilize os filtros acima para realizar uma pesquisa.</td></tr>`;
            contadorRegistros.textContent = `0 registro(s) encontrado(s)`;
        } catch (error) {
            console.error("Erro ao carregar fabricantes:", error);
            ModalService.error("Erro", "Não foi possível carregar a lista de fabricantes.");
        }
    }

    function renderizarTabela(dados) {
        tabelaCorpo.innerHTML = "";
        contadorRegistros.textContent = `${dados.length} registro(s) encontrado(s)`;

        if (dados.length === 0) {
            tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Nenhum registro encontrado.</td></tr>`;
            return;
        }

        dados.forEach(f => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${f.idFabricante}</td>
                <td class="fw-bold">${f.razaoSocial}</td>
                <td>${f.cnpj || '-'}</td>
                <td>${f.paisOrigem || '-'}</td>
                <td><span class="badge ${f.ativo ? 'bg-success' : 'bg-secondary'}">${f.ativo ? 'Ativo' : 'Inativo'}</span></td>
                <td>${f.dataCadastro ? formatarData(f.dataCadastro) : '-'}</td>
                <td class="text-center">
                    <div class="d-flex justify-content-center gap-1">
                        <button class="btn btn-sm btn-outline-secondary btn-detalhes" title="Visualizar Detalhes" data-id="${f.idFabricante}"><i class="fa fa-search"></i></button>
                        <button class="btn btn-sm btn-outline-primary btn-editar" title="Editar" data-id="${f.idFabricante}"><i class="fa fa-pen"></i></button>
                        <button class="btn btn-sm btn-outline-danger btn-excluir-linha" title="Excluir" data-id="${f.idFabricante}"><i class="fa fa-trash"></i></button>
                    </div>
                </td>
            `;
            tabelaCorpo.appendChild(tr);
        });

        document.querySelectorAll('.btn-detalhes').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = parseInt(this.getAttribute('data-id'));
                const fab = listaGlobalFabricantes.find(item => item.idFabricante === id);
                if (fab) mostrarDetalhesFabricante(fab);
            });
        });

        document.querySelectorAll('.btn-editar').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = parseInt(this.getAttribute('data-id'));
                const fab = listaGlobalFabricantes.find(item => item.idFabricante === id);
                if (fab) abrirFormulario(fab);
            });
        });

        document.querySelectorAll('.btn-excluir-linha').forEach(btn => {
            btn.addEventListener('click', async function() {
                const id = parseInt(this.getAttribute('data-id'));
                await excluirFabricante(id);
            });
        });
    }

    async function mostrarDetalhesFabricante(f) {
        const enderecoCompleto = f.logradouro 
            ? `${f.logradouro}, ${f.numero || 'S/N'} ${f.complemento ? '(' + f.complemento + ')' : ''} - ${f.bairro || ''}, ${f.cidade || ''} / ${f.estado || ''} (CEP: ${f.cep || ''})`
            : 'Não informado';

        const conteudoHtml = `
            <div style="text-align: left; font-size: 14px;">
                <p class="mb-2"><strong>Razão Social:</strong> ${f.razaoSocial}</p>
                <p class="mb-2"><strong>CNPJ:</strong> ${f.cnpj || '-'}</p>
                <p class="mb-2"><strong>Inscrição Estadual:</strong> ${f.inscricaoEstadual || '-'}</p>
                <p class="mb-2"><strong>Endereço:</strong> ${enderecoCompleto}</p>
                <p class="mb-2"><strong>País de Origem:</strong> ${f.paisOrigem || 'Brasil'}</p>
                <p class="mb-2"><strong>Situação:</strong> ${f.ativo ? 'Ativo' : 'Inativo'}</p>
                <p class="mb-0"><strong>Data de Cadastro:</strong> ${f.dataCadastro ? formatarData(f.dataCadastro) : '-'}</p>
            </div>
        `;

        if (typeof ModalService.info === 'function') {
            await ModalService.info("Detalhes do Fabricante", conteudoHtml);
        } else {
            await ModalService.alert("Detalhes do Fabricante", conteudoHtml);
        }
    }

    function realizarPesquisa() {
        const termo = filtroNome.value.toLowerCase().trim();
        const termoSituacao = filtroSituacao.value;

        // Só bloqueia e limpa a tabela se AMBOS os campos estiverem vazios/não selecionados
        if (termoSituacao === "" && termo === "") {
            tabelaCorpo.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Utilize os filtros acima para realizar uma pesquisa.</td></tr>`;
            contadorRegistros.textContent = `0 registro(s) encontrado(s)`;
            return;
        }

        const filtrados = listaGlobalFabricantes.filter(f => {
            const matchNome = termo === "" || f.razaoSocial.toLowerCase().includes(termo) || (f.cnpj && f.cnpj.toLowerCase().includes(termo));
            
            // Se o select estiver em "Selecione..." ("") ou "Todos" ("todos"), não restringe a situação
            let matchSit = true;
            if (termoSituacao === "true") {
                matchSit = f.ativo === true;
            } else if (termoSituacao === "false") {
                matchSit = f.ativo === false;
            }

            return matchNome && matchSit;
        });

        renderizarTabela(filtrados);
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
        contadorRegistros.textContent = `0 registro(s) encontrado(s)`;
    });

    btnSalvar.addEventListener("click", () => executarSalvar(false));
    btnSalvarNovo.addEventListener("click", () => executarSalvar(true));

    async function executarSalvar(continuarNovo) {
        const razaoSocial = inputRazaoSocial.value.trim();
        const cnpjFormatado = inputCnpj.value.trim();
        const cnpjApenasNumeros = cnpjFormatado.replace(/\D/g, ''); 

        if (!razaoSocial || !cnpjFormatado) {
            await ModalService.error("Atenção", "Os campos Razão Social e CNPJ são obrigatórios.");
            return;
        }

        if (cnpjApenasNumeros.length !== 14) {
            await ModalService.error("CNPJ Inválido", "O CNPJ deve conter exatamente 14 dígitos numéricos.");
            return;
        }

        const payload = {
            idFabricante: inputId.value ? parseInt(inputId.value) : 0,
            razaoSocial: razaoSocial,
            cnpj: cnpjFormatado, 
            inscricaoEstadual: inputIe.value.trim(),
            cep: inputCep.value.trim().replace(/\D/g, ''),
            logradouro: inputLogradouro.value.trim(),
            numero: inputNumero.value.trim(),
            complemento: inputComplemento.value.trim(),
            bairro: inputBairro.value.trim(),
            cidade: inputCidade.value.trim(),
            estado: inputEstado.value,
            paisOrigem: inputPais.value.trim(),
            ativo: inputAtivo.value === "true"
        };

        try {
            const response = await fetch('/nexacore/api/fabricantes/salvar', {
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
            await excluirFabricante(id);
        }
    });

    async function excluirFabricante(id) {
        const confirmado = await ModalService.confirm("Confirmação", "Deseja realmente excluir este fabricante?");
        if (!confirmado) return;

        try {
            const response = await fetch(`/nexacore/api/fabricantes/excluir?id=${id}`, { method: 'DELETE' });
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