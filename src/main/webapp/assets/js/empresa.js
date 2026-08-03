document.addEventListener("DOMContentLoaded", function () {
    const formEmpresa = document.getElementById("formEmpresa");
    const btnNovo = document.getElementById("btnNovo");
    const btnCancelar = document.getElementById("btnCancelar");
    const btnExcluir = document.getElementById("btnExcluir");
    const inputCep = document.getElementById("cep");
    
    // Seletores dos campos para máscaras
    const inputCnpj = document.getElementById("cnpj");
    const inputInscEstadual = document.getElementById("inscricaoEstadual");
    const inputSufixo = document.getElementById("sufixo");
    const inputDddTel1 = document.getElementById("dddTelefone1");
    const inputTel1 = document.getElementById("telefone1");
    const inputDddTel2 = document.getElementById("dddTelefone2");
    const inputTel2 = document.getElementById("telefone2");

    // --- Comportamento de ENTER funcionando como TAB ---
    if (formEmpresa) {
        const inputsForm = formEmpresa.querySelectorAll("input, select, textarea");
        inputsForm.forEach((input, index) => {
            input.addEventListener("keydown", function(event) {
                if (event.key === "Enter") {
                    event.preventDefault(); // Impede o envio do form ao pressionar Enter
                    const nextInput = inputsForm[index + 1];
                    if (nextInput) {
                        nextInput.focus();
                        if (nextInput.tagName === "INPUT" && nextInput.type === "text") {
                            nextInput.select();
                        }
                    }
                }
            });
        });
    }

    // --- Máscara de CNPJ (00.000.000/0000-00) ---
    if (inputCnpj) {
        inputCnpj.setAttribute('maxlength', '18');
        inputCnpj.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 14) value = value.slice(0, 14);
            
            value = value.replace(/^(\d{2})(\d)/, '$1.$2');
            value = value.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
            value = value.replace(/\.(\d{3})(\d)/, '.$1/$2');
            value = value.replace(/(\d{4})(\d)$/, '$1-$2');
            
            e.target.value = value;
        });
    }

    // --- Inscrição Estadual ---
    if (inputInscEstadual) {
        inputInscEstadual.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase().replace(/[^0-9A-Z.\-\/]/g, '');
        });
    }

    // --- Sufixo ---
    if (inputSufixo) {
        inputSufixo.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase().replace(/[^A-Z]/g, '');
        });
    }

    // --- Máscaras de DDD ---
    [inputDddTel1, inputDddTel2].forEach(input => {
        if (input) {
            input.setAttribute('maxlength', '2');
            input.addEventListener('input', function(e) {
                e.target.value = e.target.value.replace(/\D/g, '').slice(0, 2);
            });
        }
    });

    // --- Máscara de Telefone Fixo (XXXX-XXXX) ---
    if (inputTel1) {
        inputTel1.setAttribute('maxlength', '9');
        inputTel1.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 8) value = value.slice(0, 8);
            
            if (value.length > 4) {
                value = value.replace(/^(\d{4})(\d{1,4})$/, '$1-$2');
            }
            e.target.value = value;
        });
    }

    // --- Máscara de Celular (XXXXX-XXXX) ---
    if (inputTel2) {
        inputTel2.setAttribute('maxlength', '10');
        inputTel2.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 9) value = value.slice(0, 9);
            
            if (value.length > 5) {
                value = value.replace(/^(\d{5})(\d{1,4})$/, '$1-$2');
            }
            e.target.value = value;
        });
    }

    // --- Máscara e Consulta Automática de CEP ---
    if (inputCep) {
        inputCep.setAttribute('maxlength', '9');
        inputCep.addEventListener("input", function() {
            let valor = inputCep.value.replace(/\D/g, '');
            if (valor.length > 8) valor = valor.slice(0, 8);
            
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
                        const inputEndereco = document.getElementById("endereco");
                        const inputBairro = document.getElementById("bairro");
                        const inputMunicipio = document.getElementById("municipio");
                        const selectUf = document.getElementById("uf");
                        const inputNumero = document.getElementById("numero");

                        if (inputEndereco) inputEndereco.value = data.logradouro || "";
                        if (inputBairro) inputBairro.value = data.bairro || "";
                        if (inputMunicipio) inputMunicipio.value = data.localidade || "";
                        
                        if (selectUf) {
                            for (let option of selectUf.options) {
                                if (option.value.includes(data.uf) || option.text.includes(data.uf)) {
                                    selectUf.value = option.value;
                                    break;
                                }
                            }
                        }

                        if (inputNumero) {
                            inputNumero.focus();
                        }
                    } else {
                        if (typeof ModalService !== 'undefined' && typeof ModalService.error === 'function') {
                            await ModalService.error("CEP não encontrado", "O CEP informado não foi localizado na base nacional.");
                        } else {
                            alert("O CEP informado não foi localizado na base nacional.");
                        }
                    }
                } catch (error) {
                    console.error("Erro ao consultar CEP:", error);
                }
            }
        });
    }

    // --- INTEGRAÇÃO DA LUPA / MODAL DE BUSCA DE EMPRESAS ---
    const btnBuscarEmpresaModal = document.getElementById("btnBuscarEmpresaModal");
    const filtroTextoBusca = document.getElementById("filtroTextoBusca");

    if (btnBuscarEmpresaModal) {
        btnBuscarEmpresaModal.addEventListener("click", abrirModalBusca);
    }

    if (filtroTextoBusca) {
        filtroTextoBusca.addEventListener("input", function () {
            const termo = this.value.toLowerCase();
            const linhas = document.querySelectorAll("#tabelaResultadosBuscaEmpresa tbody tr");
            
            linhas.forEach(linha => {
                const textoLinha = linha.textContent.toLowerCase();
                linha.style.display = textoLinha.includes(termo) ? "" : "none";
            });
        });
    }

    // Evento de envio do formulário (Salvar / Atualizar)
    if (formEmpresa) {
        formEmpresa.addEventListener("submit", async function (event) {
            event.preventDefault();
            
            const origemCodigoVal = document.getElementById("origemCodigo").value.trim();
            const sufixoVal = document.getElementById("sufixo").value.trim();
            const nomeEmpresaVal = document.getElementById("nomeEmpresa").value.trim();
            const cnpjVal = document.getElementById("cnpj").value.trim();
            const inscricaoEstadualVal = document.getElementById("inscricaoEstadual").value.trim();
            const cepVal = document.getElementById("cep").value.trim();
            const enderecoVal = document.getElementById("endereco").value.trim();
            const numeroVal = document.getElementById("numero").value.trim();
            const bairroVal = document.getElementById("bairro").value.trim();
            const municipioVal = document.getElementById("municipio").value.trim();
            const dddTel1Val = document.getElementById("dddTelefone1").value.trim();
            const tel1Val = document.getElementById("telefone1").value.trim();

            // Validação de campos obrigatórios (apenas com asterisco vermelho na tela)
            if (!origemCodigoVal || !sufixoVal || !nomeEmpresaVal || !cnpjVal || !inscricaoEstadualVal || 
                !cepVal || !enderecoVal || !numeroVal || !bairroVal || !municipioVal || !dddTel1Val || !tel1Val) {
                
                const msgErro = "Por favor, preencha todos os campos obrigatórios marcados com asterisco (*), com destaque para a Origem e o Sufixo.";
                if (typeof ModalService !== 'undefined') {
                    await ModalService.error("Campos Obrigatórios", msgErro);
                } else {
                    alert(msgErro);
                }
                return; // Interrompe o envio
            }

            const idFilialVal = document.getElementById("idFilial") && document.getElementById("idFilial").value ? document.getElementById("idFilial").value : "";
            
            const empresaData = {
                idFilial: idFilialVal ? parseInt(idFilialVal) : null,
                origemCodigo: parseInt(origemCodigoVal) || 0,
                sufixo: sufixoVal,
                nomeEmpresa: nomeEmpresaVal,
                cnpj: cnpjVal,
                inscricaoEstadual: inscricaoEstadualVal,
                endereco: enderecoVal,
                numero: numeroVal,
                bairro: bairroVal,
                municipio: municipioVal,
                uf: document.getElementById("uf").value,
                cep: cepVal,
                dddTelefone1: dddTel1Val,
                telefone1: tel1Val,
                dddTelefone2: document.getElementById("dddTelefone2").value.trim(),
                telefone2: document.getElementById("telefone2").value.trim(),
                email: document.getElementById("email").value.trim()
            };

            const basePath = typeof contextPath !== 'undefined' ? contextPath : '';

            // Se idFilial estiver preenchido, envia com ?acao=atualizar. Caso contrário, faz o cadastro novo.
            const isEdicao = Boolean(idFilialVal);
            const urlEndpoint = isEdicao 
                ? `${basePath}/api/empresas/?acao=atualizar` 
                : `${basePath}/api/empresas/`;

            fetch(urlEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(empresaData)
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(async ({ status, body }) => {
                if (status === 200 && body.sucesso) {
                    if (typeof ModalService !== 'undefined') {
                        await ModalService.success("Sucesso", body.mensagem);
                    } else {
                        alert(body.mensagem);
                    }
                    limparFormularioCompleto();
                } else {
                    if (typeof ModalService !== 'undefined') {
                        await ModalService.error("Erro", body.erro || "Não foi possível salvar os dados.");
                    } else {
                        alert("Erro: " + (body.erro || "Não foi possível salvar os dados."));
                    }
                }
            })
            .catch(error => {
                console.error('Erro ao salvar:', error);
                if (typeof ModalService !== 'undefined') {
                    ModalService.error("Erro de Conexão", "Ocorreu um erro de comunicação com o servidor.");
                } else {
                    alert("Ocorreu um erro de comunicação com o servidor.");
                }
            });
        });
    }

    // Função unificada para resetar/limpar o formulário e liberar os campos para Novo Cadastro
    function limparFormularioCompleto() {
        if (formEmpresa) {
            formEmpresa.reset();
        }
        const inputIdFilial = document.getElementById("idFilial");
        if (inputIdFilial) inputIdFilial.value = "";

        const inputOrigem = document.getElementById("origemCodigo");
        if (inputOrigem) {
            inputOrigem.readOnly = false; // Libera o campo de código de origem para cadastro novo
            inputOrigem.focus();
        }
    }

    // Ação do Botão Novo
    if (btnNovo) {
        btnNovo.addEventListener("click", limparFormularioCompleto);
    }

    // Ação do Botão Limpar / Cancelar
    if (btnCancelar) {
        btnCancelar.addEventListener("click", limparFormularioCompleto);
    }

    // Ação do Botão Excluir
    if (btnExcluir) {
        btnExcluir.addEventListener("click", async function () {
            const origemCodigo = document.getElementById("origemCodigo").value;
            
            if (!origemCodigo) {
                if (typeof ModalService !== 'undefined') {
                    ModalService.error("Atenção", "Informe o código de origem para realizar a exclusão.");
                } else {
                    alert("Informe o código de origem para realizar a exclusão.");
                }
                return;
            }

            let confirmar = false;
            if (typeof ModalService !== 'undefined') {
                confirmar = await ModalService.confirm("Excluir Registro", `Deseja realmente excluir o cadastro da origem ID: ${origemCodigo}?`, 'error');
            } else {
                confirmar = confirm(`Deseja realmente excluir o cadastro da origem ID: ${origemCodigo}?`);
            }

            if (confirmar) {
                const basePath = typeof contextPath !== 'undefined' ? contextPath : '';

                fetch(`${basePath}/api/empresas/?acao=excluir&origemCodigo=${encodeURIComponent(origemCodigo)}`, {
                    method: 'POST'
                })
                .then(response => response.json().then(data => ({ status: response.status, body: data })))
                .then(({ status, body }) => {
                    if (status === 200 && body.sucesso) {
                        if (typeof ModalService !== 'undefined') {
                            ModalService.error("Excluído", body.mensagem);
                        } else {
                            alert(body.mensagem);
                        }
                        limparFormularioCompleto();
                    } else {
                        if (typeof ModalService !== 'undefined') {
                            ModalService.error("Erro", body.erro || "Registro não encontrado.");
                        } else {
                            alert("Erro ao excluir: " + (body.erro || "Registro não encontrado."));
                        }
                    }
                })
                .catch(error => {
                    console.error('Erro ao excluir:', error);
                    if (typeof ModalService !== 'undefined') {
                        ModalService.error("Erro", "Erro de comunicação ao tentar excluir.");
                    } else {
                        alert("Erro de comunicação ao tentar excluir.");
                    }
                });
            }
        });
    }
});

// --- FUNÇÕES AUXILIARES DO MODAL DE BUSCA DE EMPRESAS ---

async function abrirModalBusca() {
    try {
        const basePath = typeof contextPath !== 'undefined' ? contextPath : '';
        const response = await fetch(`${basePath}/api/empresas`);
        if (!response.ok) throw new Error("Erro ao buscar empresas.");
        
        const empresas = await response.json();
        const tbody = document.querySelector("#tabelaResultadosBuscaEmpresa tbody");
        tbody.innerHTML = "";

        if (!empresas || empresas.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">Nenhuma empresa encontrada.</td></tr>`;
        } else {
            empresas.forEach(emp => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td class="ps-3 fw-bold">${emp.origemCodigo || emp.origem || ''}</td>
                    <td>${emp.nomeEmpresa || ''}</td>
                    <td>${emp.cnpj || ''}</td>
                    <td class="text-center">
                        <button type="button" class="btn btn-primary btn-sm px-2 py-1" title="Selecionar">
                            <i class="fa fa-check"></i> Selecionar
                        </button>
                    </td>
                `;

                tr.querySelector("button").addEventListener("click", () => {
                    preencherFormularioComEmpresa(emp);
                    const modalEl = document.getElementById("modalBuscaEmpresa");
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    modal.hide();
                });

                tbody.appendChild(tr);
            });
        }

        const modalBusca = new bootstrap.Modal(document.getElementById("modalBuscaEmpresa"));
        modalBusca.show();

    } catch (error) {
        console.error("Erro:", error);
        if (typeof ModalService !== "undefined" && ModalService.error) {
            ModalService.error("Erro", "Não foi possível carregar a lista de empresas.");
        } else {
            alert("Erro ao carregar lista de empresas.");
        }
    }
}

function preencherFormularioComEmpresa(emp) {
    // Seta o ID interno para o backend identificar que é uma atualização (UPDATE)
    const inputIdFilial = document.getElementById("idFilial");
    if (inputIdFilial) {
        inputIdFilial.value = emp.idFilial || emp.id || "";
    }

    const inputOrigem = document.getElementById("origemCodigo");
    if (inputOrigem) {
        inputOrigem.value = emp.origemCodigo || emp.origem || "";
        inputOrigem.readOnly = true; // Bloqueia para evitar duplicidade de chave no banco
    }

    document.getElementById("nomeEmpresa").value = emp.nomeEmpresa || "";
    document.getElementById("cnpj").value = emp.cnpj || "";
    document.getElementById("inscricaoEstadual").value = emp.inscricaoEstadual || "";
    document.getElementById("cep").value = emp.cep || "";
    document.getElementById("endereco").value = emp.endereco || "";
    document.getElementById("numero").value = emp.numero || "";
    document.getElementById("bairro").value = emp.bairro || "";
    document.getElementById("municipio").value = emp.municipio || "";
    document.getElementById("uf").value = emp.uf || "SP - São Paulo";
    document.getElementById("sufixo").value = emp.sufixo || "";
    document.getElementById("dddTelefone1").value = emp.dddTelefone1 || "";
    
    // Formatação ao carregar dados existentes no formulário
    let t1 = (emp.telefone1 || "").replace(/\D/g, "");
    if (t1.length > 4) t1 = t1.replace(/^(\d{4})(\d{1,4})$/, '$1-$2');
    document.getElementById("telefone1").value = t1;

    document.getElementById("dddTelefone2").value = emp.dddTelefone2 || "";

    let t2 = (emp.telefone2 || "").replace(/\D/g, "");
    if (t2.length > 5) t2 = t2.replace(/^(\d{5})(\d{1,4})$/, '$1-$2');
    document.getElementById("telefone2").value = t2;

    document.getElementById("email").value = emp.email || "";
}