/**
 * Arquivo: modal-service.js
 * Descrição: Serviço global para padronização de alertas e confirmações via Bootstrap.
 * Responsabilidades: Criar, estilizar e gerenciar o ciclo de vida de modais de feedback.
 */

const ModalService = {
	
	/**
     * Exibe um modal de alerta simples.
     * @param {string} title - Título do alerta.
     * @param {string} message - Mensagem detalhada.
     * @param {string} type - Tipo de estilo (success, error, warning).
     */
	alert: (title, message, type = '') => {
	    const modalElement = document.getElementById('alertModal');
	    const box = document.getElementById('alertBox');
	    
	    // Limpa estilos anteriores e aplica o novo tipo
	    box.classList.remove('success', 'error', 'warning');
	    
	    // Se um tipo for enviado, adiciona ele
	    if (type) box.classList.add(type);
	    
	    // Atualiza conteúdo agora o título é dinâmico
	    document.getElementById('alertTitle').textContent = title;
	    document.getElementById('alertMessage').innerHTML = message;
        
		// Garante instância limpa do Bootstrap
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        
        return new Promise(resolve => {
            const btn = document.getElementById('alertOkBtn');
            // Handler para fechar e limpar o listener (evita duplicação de eventos)
            const handler = () => { 
                modal.hide(); 
                btn.removeEventListener('click', handler);
                resolve(); 
            };
            btn.addEventListener('click', handler);
        });
    },
	// --- Adicione este atalho dentro do ModalService.js ---
	    info: (title, message) => ModalService.alert(title, message, 'info'),
		
    // --- Atalhos de Conveniência ---
    success: (title, message) => ModalService.alert(title, message, 'success'),
    error: (title, message) => ModalService.alert(title, message, 'error'),

	/**
     * Exibe um modal de confirmação com opções de Sim/Não.
     * @returns {Promise<boolean>} - Retorna true se confirmado, false caso contrário.
     */
	confirm: (title, message, type = '') => {
	    // Busca pelo ID que está no seu HTML
	    const modalElement = document.getElementById('confirmModal');
	    const box = document.getElementById('confirmBox'); // Busca o ID direto da sua div customizada
	    
	    box.classList.remove('success', 'error');
	    if (type) box.classList.add(type);
	    
	    document.getElementById('confirmTitle').textContent = title;
	    document.getElementById('confirmMessage').textContent = message;
	    
	    const modal = new bootstrap.Modal(modalElement);
	    modal.show();

	    return new Promise(resolve => {
	        const btnOk = document.getElementById('confirmOkBtn');
	        const btnCancel = document.getElementById('confirmCancelBtn');

	        const handleOk = () => { modal.hide(); btnOk.removeEventListener('click', handleOk); btnCancel.removeEventListener('click', handleCancel); resolve(true); };
	        const handleCancel = () => { modal.hide(); btnOk.removeEventListener('click', handleOk); btnCancel.removeEventListener('click', handleCancel); resolve(false); };

	        btnOk.addEventListener('click', handleOk);
	        btnCancel.addEventListener('click', handleCancel);
	    });
	}
};
