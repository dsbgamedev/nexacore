document.addEventListener("DOMContentLoaded", function () {
    const contextPath = document.body.getAttribute("data-app-context-path") || "";
    
    // Array local para armazenar os endereços adicionados na tela antes do envio final
    let listaEnderecos = [];

    // 1. Busca de CEP (Via ViaCEP)
    const btnBuscarCep = document.getElementById("btn-buscar-cep");
    if (btnBuscarCep) {
        btnBuscarCep.addEventListener("click", function () {
            let cepInput = document.getElementById("input-cep");
            if (!cepInput) return;
            let cep = cepInput.value.replace(/\D/g, '');
            if (cep.length !== 8) {
                ModalService.warning("CEP Inválido", "O CEP deve conter exatamente 8 dígitos.");
                return;
            }

            fetch(`https://viacep.com.br/ws/${cep}/json/`)
                .then(response => response.json())
                .then(data => {
                    if (!data.erro) {
                        document.getElementById("input-logradouro").value = data.logradouro || "";
                        document.getElementById("input-bairro").value = data.bairro || "";
                        document.getElementById("input-cidade").value = data.localidade || "";
                        document.getElementById("input-uf").value = data.uf || "";
                        let numEl = document.getElementById("input-numero");
                        if (numEl) numEl.focus();
                    } else {
                        ModalService.warning("Não encontrado", "CEP não encontrado na base de dados.");
                    }
                })
                .catch(() => ModalService.error("Erro", "Erro ao consultar o CEP. Verifique sua conexão."));
        });
    }

    // 2. Botão para Incluir Endereço na Tabela Dinâmica
    const btnAdicionarEndereco = document.getElementById("btn-adicionar-endereco");
    if (btnAdicionarEndereco) {
        btnAdicionarEndereco.addEventListener("click", function () {
            let tipoSelect = document.getElementById("input-tipo-endereco");
            if (!tipoSelect) return;
            let tipoId = tipoSelect.value;
            let tipoNome = tipoSelect.options[tipoSelect.selectedIndex].text;
            
            let cep = document.getElementById("input-cep").value;
            let logradouro = document.getElementById("input-logradouro").value;
            let numero = document.getElementById("input-numero").value;
            let bairro = document.getElementById("input-bairro").value;
            let cidade = document.getElementById("input-cidade").value;
            let uf = document.getElementById("input-uf").value;
            let paisEl = document.getElementById("input-pais");
            let pais = paisEl ? paisEl.value : "Brasil";

            // Validação simples
            if (!tipoId || !cep || !logradouro || !numero || !bairro || !cidade || !uf) {
                ModalService.warning("Campos Obrigatórios", "Por favor, preencha todos os campos obrigatórios do endereço.");
                return;
            }

            // Objeto de endereço temporário
            let novoEndereco = {
                tipoEnderecoId: parseInt(tipoId),
                tipoNomeText: tipoNome,
                cep: cep,
                logradouro: logradouro,
                numero: numero,
                complemento: document.getElementById("input-complemento") ? document.getElementById("input-complemento").value : "",
                bairro: bairro,
                cidade: cidade,
                uf: uf,
                pais: pais,
                referencia: "",
                principal: listaEnderecos.length === 0 // O primeiro endereço cadastrado é o principal por padrão
            };

            listaEnderecos.push(novoEndereco);
            atualizarTabelaEnderecos();
            atualizarSelectsFiliais();
            limparCamposEndereco();
        });
    }

    // Função para renderizar a tabela de endereços
    function atualizarTabelaEnderecos() {
        let tbody = document.getElementById("tabela-enderecos-corpo");
        if (!tbody) return;
        tbody.innerHTML = "";

        if (listaEnderecos.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">Nenhum endereço adicionado ainda.</td></tr>`;
            return;
        }

        listaEnderecos.forEach((end, index) => {
            let tr = document.createElement("tr");
            tr.innerHTML = `
                <td><span class="badge bg-secondary">${end.tipoNomeText}</span></td>
                <td>${end.logradouro}, ${end.numero} - ${end.bairro}</td>
                <td>${end.cidade}/${end.uf}</td>
                <td>
                    <input type="radio" name="enderecoPrincipalRadio" ${end.principal ? 'checked' : ''} onchange="marcarPrincipal(${index})">
                </td>
                <td class="text-end">
                    <button type="button" class="btn btn-outline-danger btn-sm border-0" onclick="removerEndereco(${index})">
                        <i class="fa fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Funções globais auxiliares para a tabela
    window.removerEndereco = function (index) {
        listaEnderecos.splice(index, 1);
        if (listaEnderecos.length > 0 && !listaEnderecos.some(e => e.principal)) {
            listaEnderecos[0].principal = true;
        }
        atualizarTabelaEnderecos();
        atualizarSelectsFiliais();
    };

    window.marcarPrincipal = function (index) {
        listaEnderecos.forEach((e, i) => e.principal = (i === index));
    };

    function limparCamposEndereco() {
        document.getElementById("input-tipo-endereco").value = "";
        document.getElementById("input-cep").value = "";
        document.getElementById("input-logradouro").value = "";
        document.getElementById("input-numero").value = "";
        document.getElementById("input-complemento").value = "";
        document.getElementById("input-bairro").value = "";
        document.getElementById("input-cidade").value = "";
        document.getElementById("input-uf").value = "";
        let paisEl = document.getElementById("input-pais");
        if(paisEl) paisEl.value = "Brasil";
    }

    // Atualiza os selects das filiais com base nos endereços adicionados
    function atualizarSelectsFiliais() {
        document.querySelectorAll(".filial-endereco-select").forEach(select => {
            select.innerHTML = '<option value="" selected disabled>Selecione um endereço...</option>';
            
            listaEnderecos.forEach((end, index) => {
                let opt = document.createElement("option");
                opt.value = index; 
                opt.textContent = `${end.tipoNomeText} - ${end.logradouro}, ${end.numero} (${end.cidade}/${end.uf})`;
                select.appendChild(opt);
            });
        });
    }

    // Habilita ou desabilita o select de endereço quando o checkbox da filial é marcado
    document.querySelectorAll(".filial-checkbox").forEach(checkbox => {
        checkbox.addEventListener("change", function () {
            let filialId = this.value;
            let selectEndereco = document.getElementById(`end_filial_${filialId}`);
            if (selectEndereco) {
                selectEndereco.disabled = !this.checked;
                if (!this.checked) selectEndereco.value = "";
            }
        });
    });

    // 3. Submissão do Formulário Principal via AJAX (Integrado com o TransportadoraServlet)
    const formTransportadora = document.getElementById("formTransportadora");
    if (formTransportadora) {
        formTransportadora.addEventListener("submit", async function (e) {
            e.preventDefault();

            if (listaEnderecos.length === 0) {
                await ModalService.warning("Atenção", "Adicione pelo menos um endereço à transportadora antes de salvar.");
                return;
            }

            let transportadoraData = {
                razaoSocial: document.getElementById("razaoSocial")?.value || "",
                nomeFantasia: document.getElementById("nomeFantasia")?.value || "",
                cnpj: document.getElementById("cnpj")?.value || "",
                inscricaoEstadual: document.getElementById("inscricaoEstadual")?.value || "",
                rntrc: document.getElementById("rntrc")?.value || "",
                telefone: document.getElementById("telefone")?.value || "",
                celular: document.getElementById("celular")?.value || "",
                email: document.getElementById("email")?.value || "",
                site: document.getElementById("site")?.value || "",
                status: document.getElementById("status")?.value || "ATIVA",
                observacao: document.getElementById("observacao")?.value || ""
            };

            let filiaisSelecionadas = [];
            let erroFilial = false;
            
            for (let cb of document.querySelectorAll(".filial-checkbox:checked")) {
                let filialId = cb.value;
                let enderecoSelect = document.getElementById(`end_filial_${filialId}`);
                let enderecoIndice = enderecoSelect ? enderecoSelect.value : "";

                if (enderecoIndice === "") {
                    await ModalService.warning("Filial Pendente", "Por favor, selecione um endereço para cada filial marcada.");
                    erroFilial = true;
                    break;
                }

                filiaisSelecionadas.push({
                    idFilial: parseInt(filialId),
                    enderecoIndice: parseInt(enderecoIndice)
                });
            }

            if (erroFilial) return;

            let payload = {
                transportadora: transportadoraData,
                enderecos: listaEnderecos,
                filiais: filiaisSelecionadas
            };

            try {
                let response = await fetch(contextPath + "/TransportadoraServlet?acao=salvar", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json;charset=UTF-8"
                    },
                    body: JSON.stringify(payload)
                });

                const contentType = response.headers.get("content-type");
                let data = {};
                if (contentType && contentType.includes("application/json")) {
                    data = await response.json();
                } else {
                    let text = await response.text();
                    throw new Error(text || "Erro desconhecido no servidor.");
                }

                if (response.ok && data.sucesso) {
                    await ModalService.success("Sucesso!", data.mensagem || "Transportadora cadastrada com sucesso!");
                    window.location.href = contextPath + "/ConsultaTransportadoraServlet";
                } else {
                    await ModalService.error("Erro ao Salvar", data.mensagem || data.error || "Não foi possível concluir a operação.");
                }

            } catch (err) {
                console.error(err);
                await ModalService.error("Erro de Comunicação", "Erro ao comunicar com o servidor: " + err.message);
            }
        });
    }

    // --- 4. Controle de Seleção em Massa (Checkbox "Selecionar Todos") ---
    const selectAll = document.getElementById("selectAllCheckbox");
    if (selectAll) {
        selectAll.addEventListener("change", function () {
            document.querySelectorAll(".transportadora-checkbox").forEach(cb => {
                cb.checked = selectAll.checked;
            });
        });
    }

    // 5. Ações da Tabela de Consulta (Visualizar, Editar, Excluir)
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