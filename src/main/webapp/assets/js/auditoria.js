document.addEventListener("DOMContentLoaded", function () {
	console.log("Módulo de Auditoria carregado com sucesso.");
		
    // Preenche a data de hoje automaticamente se os campos estiverem vazios
    const hoje = new Date().toISOString().split('T')[0];
    const inputDataInicio = document.querySelector("input[name='dataInicio']");
    const inputDataFim = document.querySelector("input[name='dataFim']");

    if (inputDataInicio && !inputDataInicio.value) inputDataInicio.value = hoje;
    if (inputDataFim && !inputDataFim.value) inputDataFim.value = hoje;

    // Se houver parâmetros na URL, limpa a URL visualmente para mantê-la limpa
    if (window.location.search) {
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    const form = document.querySelector("form[action*='AuditoriaServlet']");
    if (form) {
        form.addEventListener("submit", function (e) {
            // Seleciona todos os inputs e selects do formulário
            const inputs = form.querySelectorAll("input, select");
            inputs.forEach(input => {
                // Se o campo estiver vazio, desativa-o temporariamente para que o navegador não o envie na URL
                if (!input.value.trim()) {
                    input.disabled = true;
                }
            });
        });
    }
});

/**
 * Função chamada ao clicar no botão de ver detalhes de um registro de auditoria.
 * Faz uma requisição assíncrona (fetch) para buscar os detalhes em JSON no Servlet.
 */
function verDetalhes(id) {
    const url = contextPath + '/AuditoriaServlet?acao=detalhes&id=' + id;

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("Erro ao buscar os detalhes da auditoria.");
            }
            return response.json();
        })
        .then(data => {
            // Preenche a descrição
            document.getElementById('modalDescricao').innerText = data.descricao || 'Sem descrição informada.';
            
            // Formata e exibe os dados anteriores (JSON)
            try {
                let antesObj = JSON.parse(data.dadosAnteriores || '{}');
                document.getElementById('modalAntes').innerText = JSON.stringify(antesObj, null, 2);
            } catch(e) {
                document.getElementById('modalAntes').innerText = data.dadosAnteriores || 'N/A';
            }

            // Formata e exibe os dados novos (JSON)
            try {
                let depoisObj = JSON.parse(data.dadosNovos || '{}');
                document.getElementById('modalDepois').innerText = JSON.stringify(depoisObj, null, 2);
            } catch(e) {
                document.getElementById('modalDepois').innerText = data.dadosNovos || 'N/A';
            }

            // Abre o modal do Bootstrap 5 de forma segura
            const modalElement = document.getElementById('modalDetalhes');
            const modalBootstrap = new bootstrap.Modal(modalElement);
            modalBootstrap.show();
        })
        .catch(err => {
            console.error(err);
            if (typeof ModalService !== 'undefined' && ModalService.error) {
                ModalService.error("Erro", "Não foi possível carregar os detalhes do registro.");
            } else {
                alert('Não foi possível carregar os detalhes do registro.');
            }
        });
}

/**
 * Função chamada ao clicar no botão de excluir um registro de auditoria (requisição HTTP DELETE).
 */
async function excluirLog(id) {
    let confirmado = false;

    if (typeof ModalService !== 'undefined' && ModalService.confirm) {
        confirmado = await ModalService.confirm(
            "Confirmar Exclusão", 
            "Tem certeza que deseja excluir permanentemente este registro de auditoria?",
            "error"
        );
    } else {
        confirmado = confirm("Tem certeza que deseja excluir permanentemente este registro de auditoria?");
    }

    if (confirmado) {
        const url = contextPath + '/AuditoriaServlet?id=' + id;

        fetch(url, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                if (typeof ModalService !== 'undefined' && ModalService.success) {
                    ModalService.success("Sucesso", "Registro de auditoria excluído com sucesso!").then(() => {
                        window.location.reload();
                    });
                } else {
                    alert("Registro excluído com sucesso!");
                    window.location.reload();
                }
            } else {
                throw new Error("Erro ao excluir o registro no servidor.");
            }
        })
        .catch(err => {
            console.error(err);
            if (typeof ModalService !== 'undefined' && ModalService.error) {
                ModalService.error("Erro", "Não foi possível excluir o registro.");
            } else {
                alert('Não foi possível excluir o registro.');
            }
        });
    }
}