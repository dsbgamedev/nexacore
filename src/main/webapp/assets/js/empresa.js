document.addEventListener("DOMContentLoaded", function () {
    const formEmpresa = document.getElementById("formEmpresa");
    const btnNovo = document.getElementById("btnNovo");
    const btnCancelar = document.getElementById("btnCancelar");
    const btnExcluir = document.getElementById("btnExcluir");
    const inputCep = document.getElementById("cep");
    
    // Seletores dos novos campos para máscaras
    const inputCnpj = document.getElementById("cnpj");
    const inputInscEstadual = document.getElementById("inscricaoEstadual");
    const inputSufixo = document.getElementById("sufixo");
    const inputDddTel1 = document.getElementById("dddTelefone1");
    const inputTel1 = document.getElementById("telefone1");
    const inputDddTel2 = document.getElementById("dddTelefone2");
    const inputTel2 = document.getElementById("telefone2");

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

    // --- Inscrição Estadual (Permite números, letras maiúsculas e separadores comuns) ---
    if (inputInscEstadual) {
        inputInscEstadual.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase().replace(/[^0-9A-Z.\-\/]/g, '');
        });
    }

    // --- Sufixo (Permite apenas letras maiúsculas) ---
    if (inputSufixo) {
        inputSufixo.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase().replace(/[^A-Z]/g, '');
        });
    }

    // --- Máscaras de DDD (Máximo 2 dígitos numéricos) ---
    [inputDddTel1, inputDddTel2].forEach(input => {
        if (input) {
            input.setAttribute('maxlength', '2');
            input.addEventListener('input', function(e) {
                e.target.value = e.target.value.replace(/\D/g, '').slice(0, 2);
            });
        }
    });

    // --- Máscaras de Telefones/Celulares (Formato 0000-0000 ou 00000-0000) ---
    [inputTel1, inputTel2].forEach(input => {
        if (input) {
            input.setAttribute('maxlength', '10');
            input.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                if (value.length > 9) value = value.slice(0, 9);
                
                // Aplica hífen dinamicamente dependendo se tem 8 ou 9 dígitos
                if (value.length > 5) {
                    value = value.replace(/^(\d{4,5})(\d{1,4})$/, '$1-$2');
                }
                e.target.value = value;
            });
        }
    });

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

    // Evento de envio do formulário (Salvar / Atualizar)
	if (formEmpresa) {
        formEmpresa.addEventListener("submit", function (event) {
            event.preventDefault();
            
            const empresaData = {
                origemCodigo: parseInt(document.getElementById("origemCodigo").value) || 0,
                sufixo: document.getElementById("sufixo").value,
                nomeEmpresa: document.getElementById("nomeEmpresa").value,
                cnpj: document.getElementById("cnpj").value,
                inscricaoEstadual: document.getElementById("inscricaoEstadual").value,
                endereco: document.getElementById("endereco").value,
                numero: document.getElementById("numero").value,
                bairro: document.getElementById("bairro").value,
                municipio: document.getElementById("municipio").value,
                uf: document.getElementById("uf").value,
                cep: document.getElementById("cep").value,
                dddTelefone1: document.getElementById("dddTelefone1").value,
                telefone1: document.getElementById("telefone1").value,
                dddTelefone2: document.getElementById("dddTelefone2").value,
                telefone2: document.getElementById("telefone2").value,
                email: document.getElementById("email").value
            };

            const basePath = typeof contextPath !== 'undefined' ? contextPath : '';

            fetch(`${basePath}/api/empresas/`, {
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
                    formEmpresa.reset();
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

    // Ação do Botão Novo
    if (btnNovo) {
        btnNovo.addEventListener("click", function () {
            formEmpresa.reset();
            document.getElementById("origemCodigo").focus();
        });
    }

	// Ação do Botão Limpar
	if (btnCancelar) {
	    btnCancelar.addEventListener("click", function () {
	        if (formEmpresa) {
	            formEmpresa.reset();
	        }
	        const inputOrigem = document.getElementById("origemCodigo");
	        if (inputOrigem) {
	            inputOrigem.focus();
	        }
	    });
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
                            ModalService.success("Excluído", body.mensagem);
                        } else {
                            alert(body.mensagem);
                        }
                        formEmpresa.reset();
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