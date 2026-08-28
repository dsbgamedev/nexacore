document.addEventListener('DOMContentLoaded', function() {
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    const tokenInput = document.getElementById('token');
    const novaSenhaInput = document.getElementById('novaSenha');
    const confirmarNovaSenhaInput = document.getElementById('confirmarNovaSenha');

    // Lê o APP_CONTEXT_PATH do data attribute no body
    const APP_CONTEXT_PATH = document.body.dataset.appContextPath || '';

    resetPasswordForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        const token = tokenInput.value.trim();
        const novaSenha = novaSenhaInput.value;
        const confirmarNovaSenha = confirmarNovaSenhaInput.value;

        if (!token) {
            // AGORA CHAMA showPasswordModal
            showPasswordModal('Erro', 'Token de redefinição ausente. Por favor, use o link completo do e-mail.', 'error');
            return;
        }
        if (!novaSenha || !confirmarNovaSenha) {
            // AGORA CHAMA showPasswordModal
            showPasswordModal('Atenção', 'Por favor, preencha todos os campos de senha.', 'warning');
            return;
        }
        if (novaSenha !== confirmarNovaSenha) {
            // AGORA CHAMA showPasswordModal
            showPasswordModal('Erro', 'As senhas não coincidem.', 'error');
            return;
        }
        // A validação de complexidade da senha agora será feita principalmente no backend (PasswordResetServlet)
        // mas é bom ter uma validação básica aqui para feedback imediato.
        if (novaSenha.length < 8 || !/[A-Z]/.test(novaSenha) || !/[a-z]/.test(novaSenha) || !/\d/.test(novaSenha)) {
            // AGORA CHAMA showPasswordModal
            showPasswordModal('Erro', 'A nova senha deve ter no mínimo 8 caracteres, incluindo letras maiúsculas, minúsculas e números.', 'error');
            return;
        }

        // AGORA CHAMA showPasswordModal
        showPasswordModal('Aguarde', 'Redefinindo senha...', 'info');

        try {
            const response = await fetch(`${APP_CONTEXT_PATH}/PasswordResetServlet`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ 
                    action: 'resetPassword', 
                    token: token, 
                    newPassword: novaSenha, 
                    confirmPassword: confirmarNovaSenha 
                })
            });

            let result;
            try {
                // Sempre tenta parsear a resposta como JSON
                result = await response.json();
            } catch (jsonError) {
                console.error('Erro JS: Falha ao parsear JSON da resposta do servidor:', jsonError);
                const rawResponseText = await response.text();
                console.error('Erro JS: Resposta bruta do servidor (não JSON):', rawResponseText);
                showPasswordModal('Erro de Resposta', 'O servidor enviou uma resposta em formato inesperado. Por favor, contate o suporte.', 'error');
                return; // Aborta o processamento
            }

            if (result.success) {
                showPasswordModal('Sucesso', result.message + ' Você será redirecionado para a tela de login.', 'success', () => {
                    window.location.href = `${APP_CONTEXT_PATH}/LoginServlet`; 
                });
            } else {
                showPasswordModal('Erro', result.message, 'error');
            }
        } catch (error) {
            console.error('Erro na requisição de redefinição de senha:', error);
            showPasswordModal('Erro de Conexão', 'Ocorreu um erro ao conectar com o servidor. Tente novamente.', 'error');
        }
    });

    // Lógica para exibir mensagens da URL (se o token for inválido, por exemplo)
    const urlParams = new URLSearchParams(window.location.search);
    const messageType = urlParams.get('message');
    const customMessage = urlParams.get('custom_message');

    if (messageType && customMessage) {
        // AGORA CHAMA showPasswordModal
        showPasswordModal('Atenção', decodeURIComponent(customMessage), messageType);
        // Limpa os parâmetros da URL para evitar que o modal reapareça ao recarregar a página
        const newUrl = new URL(window.location.href);
        newUrl.searchParams.delete('message');
        newUrl.searchParams.delete('custom_message');
        history.replaceState({}, document.title, newUrl.toString());
    }
});
