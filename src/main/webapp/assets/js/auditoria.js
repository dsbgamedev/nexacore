document.addEventListener("DOMContentLoaded", function () {
    // Inicialização de componentes ou ouvintes globais se necessário
    console.log("Módulo de Auditoria carregado com sucesso.");
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
            alert('Não foi possível carregar os detalhes do registro.');
        });
}