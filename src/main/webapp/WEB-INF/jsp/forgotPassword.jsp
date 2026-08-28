<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- REMOVIDO: Todos os scriptlets Java foram movidos para o Servlet --%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <%-- <%@ include file="jsp/head.jsp" %> --%>
    <%-- O título agora vem do request attribute definido pelo Servlet --%>
    <title>${title}</title>
    <%-- O APP_CONTEXT_PATH agora vem do request attribute definido pelo Servlet --%>
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/css/estilo.css">
    <link rel="stylesheet" href="${APP_CONTEXT_PATH}/css/estiloPassword.css">
</head>
<%-- O APP_CONTEXT_PATH também é passado para o JavaScript via data attribute --%>
<body class="login-page" data-app-context-path="${APP_CONTEXT_PATH}">
    <main class="content-wrapper">
    <div class="login-container">
        <h2>Recuperar Senha</h2>
        <form id="forgotPasswordForm">
            <div class="form-group">
                <label for="email">E-mail Cadastrado:</label>
                <input type="email" id="email" name="email" required placeholder="Digite seu e-mail">
            </div>
            <button type="submit" class="btn-submit">Enviar Link de Redefinição</button>
        </form>
        <div class="back-to-login">
            <a href="${APP_CONTEXT_PATH}/LoginServlet">Voltar para o Login</a>
        </div>
    </div>
    </main>
	<%-- <jsp:include page="/footer.jsp" /> --%>
    <%-- IMPORTANTE: Inclui o modal DEDICADO para páginas de senha ANTES do script que o utiliza --%>
    <%-- Caminho absoluto para o modal.jsp --%>
    <%-- <%@ include file="/jsp/passwordModal.jsp" %> --%>

	<%-- NOVO: Inclui o script global de branding. Ele é auto-executável. --%>
    <script src="${pageContext.request.contextPath}/js/global-branding.js" defer></script>
    <%-- Inclui o script JavaScript externo APÓS o modal --%>
    <script src="${APP_CONTEXT_PATH}/js/forgotPassword.js"></script>
</body>
</html>
