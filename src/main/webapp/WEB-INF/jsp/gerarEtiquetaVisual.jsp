<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Etiqueta NF - Nexacore ></title>
     <%-- Bibliotecas e Scripts Externos (Toda a lógica está no etiqueta.js) --%>
    <script src="https://cdn.jsdelivr.net/npm/jsbarcode@3.11.0/dist/JsBarcode.all.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/estiloEtiqueta.css">   
    <style>
        :root {
          --largura: ${largura}${unidade};
          --altura: ${altura}${unidade};
         }
     </style> 
</head>
<body class="p-4 bg-light" data-app-context-path="${pageContext.request.contextPath}">
<!-- 1. INCLUI O MENU LATERAL DA PASTA DE LAYOUT -->
    <jsp:include page="/WEB-INF/jsp/layout/sidebar.jsp" />

    <!-- CONTEÚDO PRINCIPAL DENTRO DO LAYOUT -->
    <main id="main-content" class="nexacore-main">
        
        <!-- 2. INCLUI A TOPBAR DA PASTA DE LAYOUT -->
        <jsp:include page="/WEB-INF/jsp/layout/topbar.jsp" />
   
    <div class="etiqueta-wrapper ${layout}">
        
        <c:choose>
            <%-- NOVO MODELO: Estilo Logística 100x149 --%>
            <c:when test="${layout == 'logistica'}">
			    <div class="secao-topo-log">
			        <div class="col-logo">
			            <span class="cba-texto">CBA</span>
			            <span class="diesel-texto">DIESEL</span>
			        </div>
			        <div class="col-codigos">
			            <div class="box-preto">SC1 - 2</div>
			            <div class="box-preto">LSC - 21</div>
			        </div>
			        <div class="col-agencia">
			            <span class="vertical">AGÊNCIA</span>
			        </div>
			    </div>
			
			    <div class="secao-meio-log">
			        <div class="dados-endereco">
			            <strong>REMETENTE:</strong> CBA DIESEL - MATRIZ SP<br>
			            <strong>DESTINATÁRIO:</strong> <c:out value="${conteudo}" />
			        </div>
			        <div class="qr-area">
			            <div class="qr-box">QR</div>
			        </div>
			    </div>
			
			    <div class="secao-nf-log">
			        <span class="nf-label">NOTA FISCAL</span>
			        <div class="nf-valor">${notaFiscal}</div>
			    </div>
			</c:when>

            <%-- MODELOS ANTIGOS (Padrao, Moderno, Compacto) --%>
            <c:otherwise>
                <header class="cabecalho-etiqueta">
                    <c:if test="${layout == 'moderno'}">
                        <span class="logo-texto">CBADIESEL</span>
                    </c:if>
                    <h2>ETIQUETA DE FRETE</h2>
                </header>
                
                <div class="conteudo-texto">
                    <strong>CONTEÚDO:</strong><br>
                    <c:out value="${conteudo}" default="Sem descrição" /><br><br>
                    
                    <div class="dados-rodape">
                        <span><strong>ORIGEM:</strong> ${sessionScope.usuarioLogado.unidadeAtivaNome}</span>
                        <span><strong>NF:</strong> ${notaFiscal}</span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <%-- 
             Área do Barcode: Note que removemos o script e usamos data-attributes.
             O arquivo etiqueta.js vai ler esses valores "data-nf" e "data-largura" sozinho.
        --%>
        <div class="barcode-area">
            <svg id="barcode" 
                 data-nf="${notaFiscal}" 
                 data-largura="${largura}"></svg>
        </div>
    </div>
 <!-- 3. INCLUI O RODAPÉ DA PASTA DE LAYOUT -->
 <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
 <script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>
 
 <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
 <script src="${pageContext.request.contextPath}/assets/js/etiqueta.js"></script>
 <script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
</body>
</html>