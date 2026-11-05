document.addEventListener('DOMContentLoaded', function() {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    const emailInput = document.getElementById('email');

    // Lê o APP_CONTEXT_PATH do data attribute no body
    const APP_CONTEXT_PATH = document.body.dataset.appContextPath || '';
	
	// Função global para disparar o modal do Bootstrap 5 de forma dinâmica
	function showPasswordModal(title, message, type = 'info') {
	    const modalTitle = document.getElementById('passwordModalLabel');
	    const modalBody = document.getElementById('passwordModalBody');
	    const modalHeader = document.getElementById('passwordModalHeader');
	    
	    if (!modalTitle || !modalBody || !modalHeader) return;

	    modalTitle.textContent = title;
	    modalBody.textContent = message;
	    
	    // Remove classes de cor anteriores do cabeçalho
	    modalHeader.className = 'modal-header text-white';
	    
	    // Aplica a cor do cabeçalho consoante o tipo de alerta
	    if (type === 'success') {
	        modalHeader.classList.add('bg-success');
	    } else if (type === 'warning') {
	        modalHeader.classList.add('bg-warning', 'text-dark');
	    } else if (type === 'error' || type === 'danger') {
	        modalHeader.classList.add('bg-danger');
	    } else {
	        modalHeader.classList.add('bg-primary');
	    }

	    // Instancia e abre o modal utilizando a API do Bootstrap 5
	    const modalElement = document.getElementById('passwordModal');
	    let modalInstance = bootstrap.Modal.getInstance(modalElement);
	    if (!modalInstance) {
	        modalInstance = new bootstrap.Modal(modalElement);
	    }
	    modalInstance.show();
	}

    forgotPasswordForm.addEventListener('submit', async function(event) {
        event.preventDefault(); // Previne o envio padrão do formulário

        const email = emailInput.value.trim();
        if (!email) {
            // AGORA CHAMA showPasswordModal
            showPasswordModal('Atenção', 'Por favor, digite seu e-mail.', 'warning');
            return;
        }

        // AGORA CHAMA showPasswordModal
        showPasswordModal('Aguarde', 'Enviando link de redefinição...', 'info');

        try {
            const response = await fetch(`${APP_CONTEXT_PATH}/PasswordResetServlet`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ action: 'requestReset', email: email })
            });

            let result;
            try {
                // Sempre tenta parsear a resposta como JSON
                result = await response.json();
            } catch (jsonError) {
                console.error('Erro JS: Falha ao parsear JSON da resposta do servidor:', jsonError);
                // Se não for JSON, tenta ler como texto para depuração
                const rawResponseText = await response.text();
                console.error('Erro JS: Resposta bruta do servidor (não JSON):', rawResponseText);
                showPasswordModal('Erro de Resposta', 'O servidor enviou uma resposta em formato inesperado. Por favor, contate o suporte.', 'error');
                return; // Aborta o processamento
            }

            if (result.success) {
                showPasswordModal('Sucesso', result.message, 'success');
            } else {
                showPasswordModal('Erro', result.message, 'error');
            }
        } catch (error) {
            console.error('Erro na requisição de recuperação de senha:', error);
            // Este catch é para erros de rede ou outros erros inesperados antes de receber uma resposta
            showPasswordModal('Erro de Conexão', 'Ocorreu um erro ao conectar com o servidor. Tente novamente.', 'error');
        }
    });
});
