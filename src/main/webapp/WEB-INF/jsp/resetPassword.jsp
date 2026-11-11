<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Estilos Globais e do Sistema -->
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/assets/css/global.css">
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/assets/css/layout.css">
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/assets/css/login.css">
</head>
<body class="login-page bg-light" data-app-context-path="${APP_CONTEXT_PATH}">

    <!-- Contentor centralizado no ecrã inteiro -->
    <div class="container d-flex justify-content-center align-items-center min-vh-100">
        <div class="login-card shadow-sm p-4 bg-white rounded w-100" style="max-width: 420px;">
            
            <div class="text-center mb-4">
                <div class="lock-icon mb-2 text-primary fs-3">
                    <i class="fa-solid fa-lock-open"></i>
                </div>
                <h3 class="login-title fw-bold">Redefinir Senha</h3>
                <p class="login-description text-muted small">Insira a sua nova senha abaixo.</p>
            </div>

            <form id="resetPasswordForm">
                <!-- O token é passado do Servlet para o JSP via request attribute -->
                <input type="hidden" id="token" name="token" value="${token}">

                <div class="mb-3">
                    <label for="novaSenha" class="form-label fw-bold small">Nova Senha</label>
                    <div class="input-group input-group-sm">
                        <span class="input-group-text bg-light"><i class="fa fa-lock"></i></span>
                        <input type="password" id="novaSenha" name="novaSenha" class="form-control" required minlength="6" placeholder="Mínimo 6 caracteres">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="confirmarNovaSenha" class="form-label fw-bold small">Confirmar Nova Senha</label>
                    <div class="input-group input-group-sm">
                        <span class="input-group-text bg-light"><i class="fa fa-lock"></i></span>
                        <input type="password" id="confirmarNovaSenha" name="confirmarNovaSenha" class="form-control" required placeholder="Confirme sua nova senha">
                    </div>
                </div>

                <div class="d-grid mb-3">
                    <button type="submit" class="btn btn-primary btn-sm py-2 fw-bold">
                        <i class="fa fa-check-circle me-1"></i> Redefinir Senha
                    </button>
                </div>
            </form>

            <div class="text-center text-muted small pt-3 border-top">
                <a href="${APP_CONTEXT_PATH}/LoginServlet" class="text-decoration-none fw-bold">
                    <i class="fa fa-arrow-left me-1"></i> Voltar para o Login
                </a>
            </div>
        </div>
    </div>

    <!-- Modal Dinâmico Próprio -->
    <div class="modal fade" id="passwordModal" tabindex="-1" aria-labelledby="passwordModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content shadow">
                <div class="modal-header text-white" id="passwordModalHeader">
                    <h5 class="modal-title fw-bold" id="passwordModalLabel">Aviso</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body fs-6" id="passwordModalBody">
                    <!-- Mensagem dinâmica -->
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm px-4" data-bs-dismiss="modal" id="modalOkBtn">OK</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Função local do Modal (Garante funcionamento autónomo sem dependências externas) -->
    <script>
        function showPasswordModal(title, message, type = 'info', callback = null) {
            const modalTitle = document.getElementById('passwordModalLabel');
            const modalBody = document.getElementById('passwordModalBody');
            const modalHeader = document.getElementById('passwordModalHeader');
            const okBtn = document.getElementById('modalOkBtn');
            
            if (!modalTitle || !modalBody || !modalHeader) return;

            modalTitle.textContent = title;
            modalBody.textContent = message;
            
            modalHeader.className = 'modal-header text-white';
            if (type === 'success') {
                modalHeader.classList.add('bg-success');
            } else if (type === 'warning') {
                modalHeader.classList.add('bg-warning', 'text-dark');
            } else if (type === 'error' || type === 'danger') {
                modalHeader.classList.add('bg-danger');
            } else {
                modalHeader.classList.add('bg-primary');
            }

            const modalElement = document.getElementById('passwordModal');
            let modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (!modalInstance) {
                modalInstance = new bootstrap.Modal(modalElement);
            }

            // Remove event listeners antigos do botão OK para evitar duplicações de callbacks
            const newOkBtn = okBtn.cloneNode(true);
            okBtn.parentNode.replaceChild(newOkBtn, okBtn);

            newOkBtn.addEventListener('click', function() {
                modalInstance.hide();
                if (callback) callback();
            });

            // Se fechar no X ou clique fora
            modalElement.addEventListener('hidden.bs.modal', function handler() {
                modalElement.removeEventListener('hidden.bs.modal', handler);
                if (callback) callback();
            }, { once: true });

            modalInstance.show();
        }
    </script>
	<!-- RODAPÉ DINÂMICO UNIFICADO -->
    <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
    <!-- LOGO EMPRESA NO LINK-->
    <script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>
    
    <!-- Script externo de redefinição -->
    <script src="${APP_CONTEXT_PATH}/assets/js/resetPassword.js"></script>
</body>
</html>