// Função para alternar a visibilidade da senha
function mostrarSenha() {
    const inputSenha = document.getElementById("senha");
    const iconeSenha = document.getElementById("iconeSenha");
    
    if (inputSenha.type === "password") {
        inputSenha.type = "text";
        iconeSenha.classList.replace("bi-eye", "bi-eye-slash");
    } else {
        inputSenha.type = "password";
        iconeSenha.classList.replace("bi-eye-slash", "bi-eye");
    }
}

// Intercepta o envio do formulário para processar via AJAX/JSON com o LoginServlet
document.addEventListener("DOMContentLoaded", () => {
    const formLogin = document.getElementById("formLogin");
    const alertContainer = document.getElementById("alertContainer");

    if (formLogin) {
        formLogin.addEventListener("submit", async function(event) {
            event.preventDefault(); // Evita o recarregamento padrão da página

            const usuario = document.getElementById("usuario").value;
            const senha = document.getElementById("senha").value;

            try {
                const response = await fetch(formLogin.action, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-Requested-With": "XMLHttpRequest"
                    },
                    body: JSON.stringify({ usuario, senha })
                });

                const resultado = await response.json();

                if (resultado.success) {
                    // Redireciona para o menu retornado pelo Servlet
                    window.location.href = resultado.redirect;
                } else {
                    // Exibe a mensagem de erro dinamicamente na tela
                    alertContainer.innerHTML = `
                        <div class="alert alert-danger login-alert" role="alert">
                            <i class="bi bi-exclamation-circle-fill me-2"></i> ${resultado.message}
                        </div>
                    `;
                }
            } catch (error) {
                console.error("Erro na requisição de login:", error);
                alertContainer.innerHTML = `
                    <div class="alert alert-danger login-alert" role="alert">
                        <i class="bi bi-exclamation-circle-fill me-2"></i> Ocorreu um erro de comunicação com o servidor.
                    </div>
                `;
            }
        });
    }
});