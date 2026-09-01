<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Produtos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>

<div class="container-fluid py-4">
    <!-- Cabeçalho com Título (Esquerda), Busca (Centro) e Botão Novo (Direita) -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <!-- Esquerda: Título e Breadcrumb -->
        <div>
            <h4 class="page-title fw-bold text-primary-dark">CONSULTA DE PRODUTOS (CATÁLOGO)</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ProdutoServlet">Novo Produto</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Consulta de Produtos</li>
                </ol>
            </nav>
        </div>
        
        <!-- Centro: Barra de Pesquisa Global -->
        <div class="input-group search-container">
            <input type="text" id="busca-global" class="form-control" placeholder="Pesquisar no sistema...">
            <button id="btn-busca-global" class="btn btn-outline-secondary">
                <i class="fas fa-search"></i>
            </button>
        </div>

        <!-- Direita: Botão Novo Produto (Exibido apenas se tiver permissão de escrita/edição) -->
        <div>
            <c:if test="${podeEditar}">
                <a href="${pageContext.request.contextPath}/ProdutoServlet" class="btn btn-success">
                    <i class="fas fa-plus"></i> Novo Produto
                </a>
            </c:if>
        </div>
    </div>

    <!-- Área de Filtros -->
    <div class="card p-4 mb-4">
        <div class="d-flex align-items-center mb-3 text-primary">
            <i class="fas fa-filter me-2"></i>
            <h5 class="mb-0 fw-bold">FILTROS DE PESQUISA</h5>
        </div>
        
        <div class="row">
	    <div class="col-md-3 mb-3">
	        <label class="form-label">Código (SKU)</label>
	        <input type="text" id="filtro-sku" class="form-control" placeholder="Digite o código...">
	    </div>
	    <div class="col-md-3 mb-3">
	        <label class="form-label">Marca</label>
	        <select id="filtro-marca" class="form-select">
	            <option value="">Selecione...</option>
	        </select>
	    </div>
	    <div class="col-md-3 mb-3">
	        <label class="form-label">Modelo</label>
	        <input type="text" id="filtro-modelo" class="form-control" placeholder="Digite o modelo...">
	    </div>
	    <div class="col-md-3 mb-3">
	        <label class="form-label">Tipo de Produto</label>
	        <select id="filtro-tipo" class="form-select">
	            <option value="">Selecione...</option>
	        </select>
	    </div>
	</div>    
	
	<div class="row align-items-end">
	    <div class="col-md-4 mb-3">
	        <label class="form-label">Situação</label>
	        <select id="filtro-ativo" class="form-select">
	            <option value="">Selecione...</option>
	            <option value="true">Ativo</option>
	            <option value="false">Inativo</option>
	        </select>
	    </div>
    <div class="col-md-8 d-flex justify-content-end mb-3">
        <button class="btn btn-outline-secondary me-2" onclick="limparFiltros()"><i class="fas fa-sync-alt me-1"></i> Limpar Filtros</button>
        <button class="btn btn-primary" onclick="pesquisarProdutos()"><i class="fas fa-search me-1"></i> Pesquisar</button>
    </div>
</div>
    </div>

    <!-- Tabela de Resultados -->
    <div class="card p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0 fw-bold text-primary-dark"><i class="fas fa-list me-2"></i> PRODUTOS CADASTRADOS</h5>
            <div class="d-flex align-items-center">
                <span class="me-2 text-muted small">Registros por página:</span>
                <select class="form-select form-select-sm select-paginacao"><option>10</option><option>25</option></select>
            </div>
        </div>
        <table class="table table-hover align-middle">
            <thead class="table-light">
                <tr><th>Código (SKU)</th><th>Tipo</th><th>Marca</th><th>Modelo</th><th>Descrição Resumida</th><th>Status</th><th>Ações</th></tr>
            </thead>
            <tbody id="tabela-produtos"></tbody>
        </table>
    </div>
</div>

<!-- Modais -->
<div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content-custom" id="confirmBox">
            <h3 id="confirmTitle">Confirmação</h3>
            <p id="confirmMessage">Deseja realmente prosseguir?</p>
            <div class="modal-buttons">
                <button type="button" class="btn-cancelar" id="confirmCancelBtn" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn-confirmar" id="confirmOkBtn">Confirmar</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="alertModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content-custom" id="alertBox">
            <h3 id="alertTitle">Atenção</h3>
            <p id="alertMessage">Mensagem do sistema.</p>
            <div class="modal-buttons">
                <button type="button" class="btn-confirmar" id="alertOkBtn" data-bs-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="detalhesProdutoModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content-custom" id="detalhesBox">
            <h3 id="detalhesTitle">Detalhes do Produto</h3>
            <div id="detalhesMessage" style="text-align: left; margin-bottom: 20px; font-size: 0.95em;"></div>
            <div class="modal-buttons">
                <button type="button" class="btn-confirmar" data-bs-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // O servidor substitui isso pelos booleanos true/false do Java
    const usuarioPodeEditar = ${podeEditar};
    const usuarioPodeExcluir = ${podeExcluir};
</script>
<script src="${pageContext.request.contextPath}/assets/js/consulta-produtos.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>