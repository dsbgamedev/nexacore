let listaEnderecos = [];

// ==========================================================================
// MÁSCARAS E LIMITAÇÕES (CNPJ e Inscrição Estadual)
// ==========================================================================
document.getElementById("cnpj")?.addEventListener("input", function(e) {
    let value = e.target.value.replace(/\D/g, ''); // Remove tudo que não for dígito
    if (value.length > 14) {
        value = value.substring(0, 14); // Limita estritamente a 14 números do CNPJ real
    }
    
    // Aplica a máscara do CNPJ: 00.000.000/0000-00
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
    let value = e.target.value.replace(/\D/g, ''); // Inscrição Estadual costuma ser apenas numérica
    if (value.length > 14) {
        value = value.substring(0, 14); // Limite padrão seguro para IE no Brasil
    }
    e.target.value = value;
});

// ==========================================================================
// BUSCA DE CEP AUTOMÁTICA (ViaCEP)
// ==========================================================================
document.getElementById("btn-buscar-cep")?.addEventListener("click", async function() {
    let cep = document.getElementById("input-cep").value.replace(/\D/g, '');
    if (cep.length !== 8) {
        alert("CEP inválido. Digite 8 dígitos.");
        return;
    }
    try {
        const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
        const data = await response.json();
        if (data.erro) {
            alert("CEP não encontrado.");
            return;
        }
        document.getElementById("input-logradouro").value = data.logradouro || "";
        document.getElementById("input-bairro").value = data.bairro || "";
        document.getElementById("input-cidade").value = data.localidade || "";
        document.getElementById("input-uf").value = data.uf || "";
        document.getElementById("input-numero").focus();
    } catch (error) {
        console.error("Erro ao buscar CEP:", error);
        alert("Erro ao consultar o CEP.");
    }
});

// ==========================================================================
// ADICIONAR ENDEREÇO À LISTA TEMPORÁRIA
// ==========================================================================
document.getElementById("btn-adicionar-endereco")?.addEventListener("click", function() {
    const tipoSelect = document.getElementById("input-tipo-endereco");
    const cep = document.getElementById("input-cep").value;
    const logradouro = document.getElementById("input-logradouro").value;
    const numero = document.getElementById("input-numero").value;
    const bairro = document.getElementById("input-bairro").value;
    const cidade = document.getElementById("input-cidade").value;
    const uf = document.getElementById("input-uf").value;

    if (!tipoSelect.value || !cep || !logradouro || !numero || !cidade || !uf) {
        alert("Selecione o tipo de endereço e preencha todos os campos obrigatórios (*).");
        return;
    }

    const principal = document.getElementById("check-principal").checked;
    if (principal) {
        listaEnderecos.forEach(e => e.principal = false);
    }

    listaEnderecos.push({
        tipoEnderecoId: parseInt(tipoSelect.value),
        tipoNome: tipoSelect.options[tipoSelect.selectedIndex].text,
        cep, 
        logradouro, 
        numero,
        complemento: document.getElementById("input-complemento").value,
        bairro, 
        cidade, 
        uf,
        pais: document.getElementById("input-pais").value || "Brasil",
        referencia: document.getElementById("input-referencia").value,
        principal
    });

    renderizarTabela();
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
}

function limparCamposEndereco() {
    document.getElementById("input-tipo-endereco").selectedIndex = 0;
    document.getElementById("input-cep").value = "";
    document.getElementById("input-logradouro").value = "";
    document.getElementById("input-numero").value = "";
    document.getElementById("input-complemento").value = "";
    document.getElementById("input-bairro").value = "";
    document.getElementById("input-cidade").value = "";
    document.getElementById("input-uf").value = "";
    document.getElementById("input-referencia").value = "";
    document.getElementById("check-principal").checked = false;
}

// ==========================================================================
// ENVIO DO FORMULÁRIO PRINCIPAL VIA AJAX
// ==========================================================================
document.getElementById("formTransportadora")?.addEventListener("submit", async function(e) {
    e.preventDefault();
    
    const cnpjLimpo = document.getElementById("cnpj").value.replace(/\D/g, '');
    if (cnpjLimpo.length !== 14) {
        alert("O CNPJ deve conter exatamente 14 dígitos válidos.");
        document.getElementById("cnpj").focus();
        return;
    }

    if (listaEnderecos.length === 0) {
        alert("Adicione pelo menos um endereço para a transportadora.");
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
    formData.append("email", document.getElementById("email").value);
    formData.append("site", document.getElementById("site").value);
    formData.append("status", document.getElementById("status").value);
    formData.append("observacao", document.getElementById("observacao").value);
    formData.append("enderecosJson", JSON.stringify(listaEnderecos));

    try {
        const response = await fetch('TransportadoraServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: formData.toString()
        });
        const res = await response.json();
        if (res.sucesso) {
            alert(res.mensagem);
            window.location.href = 'TransportadoraServlet'; 
        } else {
            alert("Erro: " + res.mensagem);
        }
    } catch (err) {
        console.error(err);
        alert("Erro ao comunicar com o servidor.");
    }
});