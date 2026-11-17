<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (session.getAttribute("usuarioLogado") == null) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet?message=session_expired");
        return;
    }
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Impressão de Etiquetas - Nexacore</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="nexacore-layout" data-app-context-path="${pageContext.request.contextPath}">
<!-- 1. INCLUI O MENU LATERAL DA PASTA DE LAYOUT -->
    <jsp:include page="/WEB-INF/jsp/layout/sidebar.jsp" />

    <!-- CONTEÚDO PRINCIPAL DENTRO DO LAYOUT -->
    <main id="main-content" class="nexacore-main">
        
        <!-- 2. INCLUI A TOPBAR DA PASTA DE LAYOUT -->
        <jsp:include page="/WEB-INF/jsp/layout/topbar.jsp" />



<div class="box max-w-2xl mx-auto mt-10 p-6 bg-white shadow-md rounded-lg">

    
   	<%-- Criamos a URL em uma variável separada --%>
	<c:set var="urlImpressao" value="${pageContext.request.contextPath}/gerar-etiqueta" />

	<%-- Agora usamos apenas a variável no action --%>
	<form action="${urlImpressao}" method="POST" target="_blank" class="space-y-4">
        
        <div class="bg-blue-50 p-4 rounded-t border border-blue-200">
            <label class="block text-sm font-bold text-blue-700 mb-2">Modelos Salvos</label>
            <select id="presetTamanhos" class="w-full border border-blue-300 rounded p-2" onchange="aplicarPreset()">
                <option value="">-- Escolha um padrão --</option>
                <option value="75,50,mm">Padrão Frete (75x50 mm)</option>
                <option value="100,150,mm">Logística Grande (100x150 mm)</option>
                <option value="50,30,mm">Produto Pequeno (50x30 mm)</option>
                <option value="100,149,mm">Logística Full (100x149 mm)</option>
            </select>
        </div>
        
		<div class="bg-gray-50 p-4 rounded border mt-4">
		    <label class="block text-sm font-bold text-gray-700 mb-2">Estilo Visual</label>
		    <select name="layout" class="w-full border border-gray-300 rounded p-2 bg-white">
		        <option value="padrao">Padrão (Básico)</option>
		        <option value="moderno">Moderno (Com Bordas e Destaque)</option>
		        <option value="compacto">Compacto (Para etiquetas pequenas)</option>
		        <option value="logistica">Logística Full (Estilo Zebra 100x149)</option>
		    </select>
		</div>

        <div class="grid grid-cols-3 gap-4 bg-gray-50 p-4 rounded border">
            <div>
                <label class="block text-xs font-semibold">Largura</label>
                <input type="number" id="inputLargura" name="largura" value="75" step="0.1" class="w-full border rounded p-2" required>
            </div>
            <div>
                <label class="block text-xs font-semibold">Altura</label>
                <input type="number" id="inputAltura" name="altura" value="50" step="0.1" class="w-full border rounded p-2" required>
            </div>
            <div>
                <label class="block text-xs font-semibold">Unidade</label>
                <select id="selectUnidade" name="unidadeMedida" class="w-full border rounded p-2">
                    <option value="mm" selected>mm</option> <option value="cm">cm</option>
                </select>
            </div>
        </div>

        <div class="space-y-4 pt-2">
            <input type="text" name="notaFiscal" placeholder="Número da Nota Fiscal" class="w-full border rounded p-2" required>
            <textarea name="conteudo" rows="3" class="w-full border rounded p-2" placeholder="Conteúdo do frete..."></textarea>
        </div>

        <button type="submit" class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-lg shadow-lg">
            GERAR E IMPRIMIR
        </button>
    </form>
</div>
<script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/etiqueta.js"/></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
<script src="${urlJs}"></script>
</body>
</html>
