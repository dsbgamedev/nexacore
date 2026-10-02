let listaEnderecos = [];

// ==========================================================================
// MÁSCARAS E LIMITAÇÕES (CNPJ, Inscrição Estadual, Telefone e Celular)
// ==========================================================================
document.getElementById("cnpj")?.addEventListener("input", function(e) {
    let value = e.target.value.replace(/\D/g, ''); 
    if (value.length > 14) value = value.substring(0, 14); 
    
    if (value.length > 12) {
        value = value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2}).*$/, "$1.$2.$3/$4-$5");
    } else if (value.length > 8) {
        value = value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4}).*$/, "$1.$2.$3/$4");
    } else if (value.length > 5) {
        value = value.replace(/^(\d{2})(\d{3})(\d{3}).*$/, "$1.$2.$3");
    } else if (value.length > 2) {
        value = value.replace(/^(\d{2})(\d{3}).*$/, "$1.$2");
    }
    e.target.value = value;
});

document.getElementById("inscricaoEstadual")?.addEventListener("input", function(e) {
    let value = e.target.value.replace(/\D/g, ''); 
    if (value.length > 14) value = value.substring(0, 14); 
    e.target.value = value;
});

// Máscara e limite para Telefone Fixo (Até 10 dígitos: (00) 0000-0000)
document.getElementById("telefone")?.addEventListener("input", function(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 10) value = value.substring(0, 10);

    if (value.length > 6) {
        value = value.replace(/^(\d{2})(\d{4})(\d{0,4}).*$/, "($1) $2-$3");
    } else if (value.length > 2) {
        value = value.replace(/^(\d{2})(\d{0,4}).*$/, "($1) $2");
    } else if (value.length > 0) {
        value = value.replace(/^(\d{0,2})/, "($1");
    }
    e.target.value = value;
});

// Máscara e limite para Celular (Até 11 dígitos, ex: 11962332015 -> (11) 96233-2015)
document.getElementById("celular")?.addEventListener("input", function(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 11) value = value.substring(0, 11);

    if (value.length > 7) {
        value = value.replace(/^(\d{2})(\d{5})(\d{0,4}).*$/, "($1) $2-$3");
    } else if (value.length > 2) {
        value = value.replace(/^(\d{2})(\d{0,5}).*$/, "($1) $2");
    } else if (value.length > 0) {
        value = value.replace(/^(\d{0,2})/, "($1");
    }
    e.target.value = value;
});

// ==========================================================================
// BUSCA DE CEP AUTOMÁTICA (ViaCEP)
// ==========================================================================
document.getElementById("btn-buscar-cep")?.addEventListener("click", async function() {
    let cep = document.getElementById("input-cep").value.replace(/\D/g, '');
    if (cep.length !== 8) {
        await ModalService.warning("Atenção", "CEP inválido. Digite 8 dígitos.");
        return;
    }
    try {
        const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
        const data = await response.json();
        if (data.erro) {
            await ModalService.warning("Atenção", "CEP não encontrado.");
            return;
        }
        document.getElementById("input-logradouro").value = data.logradouro || "";
        document.getElementById("input-bairro").value = data.bairro || "";
        document.getElementById("input-cidade").value = data.localidade || "";
        document.getElementById("input-uf").value = data.uf || "";
        document.getElementById("input-numero").focus();
    } catch (error) {
        console.error("Erro ao buscar CEP:", error);
        await ModalService.error("Erro", "Erro ao consultar o CEP.");
    }
});

// ==========================================================================
// CONTROLE DE FILIAIS ATENDIDAS E SEUS ENDEREÇOS
// ==========================================================================
document.querySelectorAll('.filial-checkbox').forEach(checkbox => {
    checkbox.addEventListener('change', function () {
        const filialId = this.value;
        const selectEndereco = document.getElementById(`end_filial_${filialId}`);
        
        if (this.checked) {
            selectEndereco.removeAttribute('disabled');
            selectEndereco.required = true;
            atualizarOpcoesEnderecosFiliais();
        } else {
            selectEndereco.setAttribute('disabled', 'true');
            selectEndereco.value = "";
            selectEndereco.required = false;
        }
    });
});

function atualizarOpcoesEnderecosFiliais() {
    document.querySelectorAll('.filial-endereco-select').forEach(select => {
        const valorAtual = select.value;
        select.innerHTML = '<option value="" disabled selected>Selecione o endereço...</option>';
        
        listaEnderecos.forEach((end, idx) => {
            const option = document.createElement('option');
            option.value = idx; // Usamos o índice do array como referência temporária
            option.textContent = `[${end.tipoNome}] ${end.logradouro}, ${end.numero} - ${end.cidade}/${end.uf}`;
            select.appendChild(option);
        });

        // Restaura a seleção anterior se ainda existir
        if (valorAtual && select.querySelector(`option[value="${valorAtual}"]`)) {
            select.value = valorAtual;
        }
    });
}

// ==========================================================================
// ADICIONAR ENDEREÇO À LISTA TEMPORÁRIA
// ==========================================================================
document.getElementById("btn-adicionar-endereco")?.addEventListener("click", async function() {
    const tipoSelect = document.getElementById("input-tipo-endereco");
    const cep = document.getElementById("input-cep").value;
    const logradouro = document.getElementById("input-logradouro").value;
    const numero = document.getElementById("input-numero").value;
    const bairro = document.getElementById("input-bairro").value;
    const cidade = document.getElementById("input-cidade").value;
    const uf = document.getElementById("input-uf").value;

    if (!tipoSelect.value || !cep || !logradouro || !numero || !bairro || !cidade || !uf) {
        await ModalService.warning("Campos Obrigatórios", "Selecione o tipo de endereço e preencha todos os campos obrigatórios (*).");
        return;
    }

    // Se for o primeiro endereço ou não houver principal, define este como principal automaticamente
    const principal = listaEnderecos.length === 0;
    if (principal) {
        listaEnderecos.forEach(e => e.principal = false);
    }

    listaEnderecos.push({
        tipoEnderecoId: parseInt(tipoSelect.value),
        tipoNome: tipoSelect.options[tipoSelect.selectedIndex].text,
        cep, 
        logradouro, 
        numero,
        complemento: "", 
        bairro, 
        cidade, 
        uf,
        pais: document.getElementById("input-pais").value || "Brasil",
        referencia: "",
        principal
    });

    renderizarTabela();
    atualizarOpcoesEnderecosFiliais();
    limparCamposEndereco();
});

function renderizarTabela() {
    const tbody = document.getElementById("tabela-enderecos-corpo");
    tbody.innerHTML = "";
    if (listaEnderecos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">Nenhum endereço adicionado ainda.</td></tr>`;
        return;
    }
    listaEnderecos.forEach((end, idx) => {
        tbody.innerHTML += `
            <tr>
                <td><span class="badge bg-light text-primary border border-primary">${end.tipoNome}</span></td>
                <td>${end.logradouro}, ${end.numero} ${end.complemento ? '- ' + end.complemento : ''}, ${end.bairro}</td>
                <td>${end.cidade} / ${end.uf}</td>
                <td>${end.principal ? '<span class="text-success fw-bold"><i class="fa fa-check-circle"></i> Sim</span>' : 'Não'}</td>
                <td class="text-end"><button type="button" class="btn btn-outline-danger btn-sm border-0" onclick="removerEndereco(${idx})"><i class="fa fa-trash"></i></button></td>
            </tr>`;
    });
}

function removerEndereco(idx) {
    listaEnderecos.splice(idx, 1);
    renderizarTabela();
    atualizarOpcoesEnderecosFiliais();
}

function limparCamposEndereco() {
    document.getElementById("input-tipo-endereco").selectedIndex = 0;
    document.getElementById("input-cep").value = "";
    document.getElementById("input-logradouro").value = "";
    document.getElementById("input-numero").value = "";
    document.getElementById("input-bairro").value = "";
    document.getElementById("input-cidade").value = "";
    document.getElementById("input-uf").value = "";
    document.getElementById("input-pais").value = "Brasil";
}
// ==========================================================================
// ENVIO DO FORMULÁRIO PRINCIPAL VIA AJAX COM FILIAIS E ENDEREÇOS
// ==========================================================================
document.getElementById("formTransportadora")?.addEventListener("submit", async function(e) {
    e.preventDefault();
    
    const cnpjLimpo = document.getElementById("cnpj").value.replace(/\D/g, '');
    if (cnpjLimpo.length !== 14) {
        await ModalService.warning("CNPJ Inválido", "O CNPJ deve conter exatamente 14 dígitos válidos.");
        document.getElementById("cnpj").focus();
        return;
    }

    const emailInput = document.getElementById("email").value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailInput)) {
        await ModalService.warning("E-mail Inválido", "Por favor, insira um endereço de e-mail válido contendo '@'.");
        document.getElementById("email").focus();
        return;
    }

    // Se a tabela estiver vazia mas preencheu os campos individuais embaixo
    const tipoSelect = document.getElementById("input-tipo-endereco");
    const cepVal = document.getElementById("input-cep").value.trim();
    const logradouroVal = document.getElementById("input-logradouro").value.trim();
    const numeroVal = document.getElementById("input-numero").value.trim();
    const cidadeVal = document.getElementById("input-cidade").value.trim();
    const ufVal = document.getElementById("input-uf").value.trim();

	if (listaEnderecos.length === 0) {
        if (tipoSelect.value && cepVal && logradouroVal && numeroVal && cidadeVal && ufVal) {
            listaEnderecos.push({
                tipoEnderecoId: parseInt(tipoSelect.value),
                tipoNome: tipoSelect.options[tipoSelect.selectedIndex].text,
                cep: cepVal, 
                logradouro: logradouroVal, 
                numero: numeroVal,
                complemento: "",
                bairro: document.getElementById("input-bairro").value, 
                cidade: cidadeVal, 
                uf: ufVal,
                pais: document.getElementById("input-pais").value || "Brasil",
                referencia: "",
                principal: true
            });
            renderizarTabela();
            atualizarOpcoesEnderecosFiliais();
            limparCamposEndereco();
        } else {
            await ModalService.warning("Endereço Obrigatório", "Adicione pelo menos um endereço para a transportadora.");
            return;
        }
    }

    // Coleta as filiais selecionadas e o índice do endereço escolhido para cada uma
    const filiaisAtendidas = [];
    let erroFilialSemEndereco = false;

    document.querySelectorAll('.filial-checkbox:checked').forEach(checkbox => {
        const filialId = checkbox.value;
        const selectEndereco = document.getElementById(`end_filial_${filialId}`);
        const enderecoIdx = selectEndereco.value;

        if (enderecoIdx === "") {
            erroFilialSemEndereco = true;
        } else {
            filiaisAtendidas.push({
                idFilial: parseInt(filialId),
                enderecoIndice: parseInt(enderecoIdx) // O backend usará isso para associar ao endereço correto cadastrado
            });
        }
    });

    if (erroFilialSemEndereco) {
        await ModalService.warning("Filial sem Endereço", "Por favor, selecione o endereço correspondente para todas as filiais marcadas.");
        return;
    }

    if (filiaisAtendidas.length === 0) {
        await ModalService.warning("Filial Obrigatória", "Selecione pelo menos uma filial atendida por esta transportadora.");
        return;
    }

    const formData = new URLSearchParams();
    formData.append("acao", "salvar");
    formData.append("razaoSocial", document.getElementById("razaoSocial").value);
    formData.append("nomeFantasia", document.getElementById("nomeFantasia").value);
    formData.append("cnpj", document.getElementById("cnpj").value);
    formData.append("inscricaoEstadual", document.getElementById("inscricaoEstadual").value);
    formData.append("rntrc", document.getElementById("rntrc").value);
    formData.append("telefone", document.getElementById("telefone").value);
    formData.append("celular", document.getElementById("celular").value);
    formData.append("email", emailInput);
    formData.append("site", document.getElementById("site").value);
    formData.append("status", document.getElementById("status").value);
    formData.append("observacao", document.getElementById("observacao").value);
    formData.append("enderecosJson", JSON.stringify(listaEnderecos));
    formData.append("filiaisJson", JSON.stringify(filiaisAtendidas)); // <-- Envia as filiais associadas em formato JSON

    try {
        const response = await fetch('TransportadoraServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: formData.toString()
        });

        if (response.status === 403) {
            const errJson = await response.json().catch(() => ({}));
            await ModalService.error("Acesso Negado", errJson.error || "Você não possui permissão para realizar esta operação.");
            return;
        }

        const res = await response.json();
        
        if (res.sucesso) {
            await ModalService.success("Sucesso", res.mensagem);
            window.location.href = 'TransportadoraServlet'; 
        } else {
            await ModalService.error("Erro", res.mensagem);
        }
    } catch (err) {
        console.error(err);
        await ModalService.error("Erro de Comunicação", "Erro ao comunicar com o servidor.");
    }
});