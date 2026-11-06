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
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/assets/css/estiloPassword.css">
</head>
<body class="login-page bg-light" data-app-context-path="${APP_CONTEXT_PATH}">

    <!-- Utilização de d-flex, justify-content-center e align-items-center para centralizar no ecrã inteiro -->
    <div class="container d-flex justify-content-center align-items-center min-vh-100">
        <div class="login-card shadow-sm p-4 bg-white rounded w-100" style="max-width: 420px;">
            
            <div class="text-center mb-4">
                <div class="lock-icon mb-2 text-primary fs-3">
                    <i class="fa-solid fa-key"></i>
                </div>
                <h3 class="login-title fw-bold">Recuperar Senha</h3>
                <p class="login-description text-muted small">Digite seu e-mail cadastrado para receber o link de redefinição.</p>
            </div>

            <form id="forgotPasswordForm">
                <div class="mb-3">
                    <label for="email" class="form-label fw-bold small">E-mail Cadastrado</label>
                    <div class="input-group input-group-sm">
                        <span class="input-group-text bg-light"><i class="fa fa-envelope"></i></span>
                        <input type="email" id="email" name="email" class="form-control" required placeholder="Digite seu e-mail...">
                    </div>
                </div>

                <div class="d-grid mb-3">
                    <button type="submit" class="btn btn-primary btn-sm py-2 fw-bold">
                        <i class="fa fa-paper-plane me-1"></i> Enviar Link de Redefinição
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
    
    <!-- Modal Dinâmico de Feedback -->
<div class="modal fade" id="passwordModal" tabindex="-1" aria-labelledby="passwordModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content shadow">
            <div class="modal-header text-white" id="passwordModalHeader">
                <h5 class="modal-title fw-bold" id="passwordModalLabel">Aviso</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body fs-6" id="passwordModalBody">
                <!-- A mensagem será inserida aqui dinamicamente -->
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm px-4" data-bs-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>
<!-- RODAPÉ DINÂMICO UNIFICADO -->
<jsp:include page="/WEB-INF/jsp/footer.jsp" />
<!-- LOGO EMPRESA NO LINK-->
<script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>

 <!-- Bootstrap JS Bundle -->
 <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
 <!-- Script externo de recuperação -->

 <script src="${APP_CONTEXT_PATH}/assets/js/forgotPassword.js"></script>
</body>
</html>