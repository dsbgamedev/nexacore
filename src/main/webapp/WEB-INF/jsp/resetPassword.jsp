<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- REMOVIDO: Todos os scriptlets Java foram movidos para o Servlet --%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <%--<%@ include file="jsp/head.jsp" %>--%>
    <%-- O título agora vem do request attribute definido pelo Servlet --%>
    <title>${title}</title>
    <%-- O APP_CONTEXT_PATH agora vem do request attribute definido pelo Servlet --%>
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/css/estilo.css">
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/css/estiloPassword.css">
</head>
<%-- O APP_CONTEXT_PATH também é passado para o JavaScript via data attribute --%>
<body class="login-page" data-app-context-path="${APP_CONTEXT_PATH}">
    <main class="main-wrapper">
    <div class="login-container">
        <h2>Redefinir Senha</h2>
        <form id="resetPasswordForm">
            <%-- O token é passado do Servlet para o JSP via request attribute e acessado por EL --%>
            <input type="hidden" id="token" name="token" value="${token}">
            <div class="form-group">
                <label for="novaSenha">Nova Senha:</label>
                <input type="password" id="novaSenha" name="novaSenha" required minlength="6" placeholder="Mínimo 6 caracteres">
            </div>
            <div class="form-group">
                <label for="confirmarNovaSenha">Confirmar Nova Senha:</label>
                <input type="password" id="confirmarNovaSenha" name="confirmarNovaSenha" required placeholder="Confirme sua nova senha">
            </div>
            <button type="submit" class="btn-submit">Redefinir Senha</button>
        </form>
        <div class="back-to-login">
            <a href="${APP_CONTEXT_PATH}/LoginServlet">Voltar para o Login</a>
        </div>
    </div>
    </main>
	   <%-- <jsp:include page="/footer.jsp" /> <%-- Incluindo o rodapé --%>
    <%-- IMPORTANTE: Inclui o modal DEDICADO para páginas de senha ANTES do script que o utiliza --%>
    <%-- CORRIGIDO: Usando caminho absoluto para o modal.jsp --%>
    <%--  <%@ include file="/jsp/passwordModal.jsp" %> --%>
	<%-- NOVO: Inclui o script global de branding. Ele é auto-executável. --%>
    <script src="${pageContext.request.contextPath}/js/global-branding.js" defer></script>
    <%-- Inclui o script JavaScript externo APÓS o modal --%>
    <script src="${APP_CONTEXT_PATH}/js/resetPassword.js"></script>
</body>
</html>
