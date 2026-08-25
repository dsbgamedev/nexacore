<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String ctx = request.getContextPath();
String erro = request.getParameter("erro");
String usuario = request.getParameter("usuario");
String mensagemErro = (String) request.getAttribute("erroMensagem");
if (mensagemErro == null) {
    mensagemErro = (String) request.getAttribute("erro");
}
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>NexaCore - Acesso ao Sistema</title>

<!-- Bootstrap -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<!-- Bootstrap Icons -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

<!-- CSS Externos Padronizados com ctx -->
<link rel="stylesheet" href="<%=ctx%>/assets/css/login.css">
<link rel="stylesheet" href="<%=ctx%>/assets/css/global.css">
<link rel="stylesheet" href="<%=ctx%>/assets/css/layout.css">

</head>

<body>
	<div class="login-page">
		<!-- LADO ESQUERDO -->
		<section class="brand-panel">
			<div>
				<div class="brand-logo">
					<div class="logo-symbol">N</div>
					<div class="logo-name">NEXACORE</div>
				</div>
				<div class="brand-subtitle">Sistema de Gestão de Equipamentos</div>
			</div>
			<img src="<%=ctx%>/img/login-equipamentos.png" class="equipment-image" alt="NexaCore - Gestão de Equipamentos">
			<div class="copyright">© 2026 NexaCore. Todos os direitos reservados.</div>
		</section>

		<!-- LADO DIREITO -->
		<main class="form-panel">
			<div class="login-wrapper">
				<div class="login-card">
					<div class="lock-icon">
						<i class="bi bi-lock-fill"></i>
					</div>
					<h1 class="login-title">Acesso ao Sistema</h1>
					<p class="login-description">Entre com suas credenciais para continuar</p>

					<!-- CONTAINER DE ALERTA DINÂMICO PARA O JS -->
					<div id="alertContainer">
						<% if ("1".equals(erro)) { %>
						<div class="alert alert-danger login-alert" role="alert">
							<i class="bi bi-exclamation-circle-fill me-2"></i> Usuário ou senha inválidos.
						</div>
						<% } else if ("inativo".equals(erro)) { %>
						<div class="alert alert-warning login-alert" role="alert">
							<i class="bi bi-person-x-fill me-2"></i> Este usuário está inativo.
						</div>
						<% } else if (mensagemErro != null && !mensagemErro.isEmpty()) { %>
						<div class="alert alert-danger login-alert" role="alert">
							<i class="bi bi-exclamation-circle-fill me-2"></i> <%= mensagemErro %>
						</div>
						<% } %>
					</div>

					<!-- FORMULÁRIO -->
					<form id="formLogin" action="<%=ctx%>/LoginServlet" method="post" autocomplete="on">
						<div class="mb-3">
							<label for="usuario" class="form-label">Usuário ou E-mail</label>
							<div class="input-wrapper">
								<i class="bi bi-person"></i> 
								<input type="text" id="usuario" name="usuario" class="form-control login-input"
									placeholder="Digite seu usuário ou e-mail"
									value="<%=usuario != null ? usuario : ""%>" autocomplete="username" required>
							</div>
						</div>

						<div class="mb-1">
							<label for="senha" class="form-label">Senha</label>
							<div class="input-wrapper">
								<i class="bi bi-lock"></i> 
								<input type="password" id="senha" name="senha" class="form-control login-input"
									placeholder="Digite sua senha" autocomplete="current-password" required>
								<button type="button" class="password-button" onclick="mostrarSenha()">
									<i id="iconeSenha" class="bi bi-eye"></i>
								</button>
							</div>
						</div>

						<div class="options-row">
							<label class="remember-label"> 
								<input type="checkbox" name="lembrar" value="true"> Lembrar acesso
							</label> 
							<a href="<%=ctx%>/jsp/esqueci-senha.jsp" class="forgot-link">Esqueci minha senha</a>
						</div>

						<button type="submit" id="btnEntrar" class="btn btn-login">
							<i class="bi bi-box-arrow-in-right me-2"></i> Entrar
						</button>
					</form>

					<div class="divider">
						<span>ou</span>
					</div>

					<button type="button" class="btn btn-sso" onclick="alert('Login corporativo ainda não configurado.');">
						<i class="bi bi-shield-check me-2"></i> Entrar com SSO (Corporativo)
					</button>

					<div class="version">Versão 1.0.0</div>

					<div class="support-box">
						<div class="support-icon">
							<i class="bi bi-question-lg"></i>
						</div>
						<div>
							<strong>Precisa de ajuda?</strong> Entre em contato com o suporte: 
							<a href="mailto:suporte@nexacore.com.br">suporte@nexacore.com.br</a>
						</div>
					</div>
				</div>
			</div>
		</main>
	</div>

	<!-- Passa o contextPath global para o arquivo JS externo utilizar -->
	<script>
		const contextPath = "<%=ctx%>";
	</script>
	<!-- SCRIPT EXTERNO PADRONIZADO -->
	<script src="<%=ctx%>/assets/js/login.js"></script>
</body>
</html>