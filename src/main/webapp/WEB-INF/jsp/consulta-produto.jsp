<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nexacore | Consulta de Produtos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="p-4 bg-light">

<div class="container-fluid py-4" style="max-width: 1300px; margin: auto;">

    <!-- Cabeçalho Padrão com Tema Escuro Unificado -->
    <div class="card-cabecalho-padrao tema-escuro p-4 mb-4 shadow-sm border-0">
        <!-- Linha Superior: Título e Breadcrumb -->
        <div class="mb-3">
            <h4 class="page-title fw-bold text-white mb-1">CONSULTA DE PRODUTOS (CATÁLOGO)</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0" style="font-size: 0.85rem;">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet" class="text-light text-decoration-none">Home</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ProdutoServlet" class="text-light text-decoration-none">Novo Produto</a></li>
                    <li class="breadcrumb-item active text-secondary" aria-current="page">Consulta de Produtos</li>
                </ol>
            </nav>
        </div>
        
        <!-- Linha Inferior: Grupo de Botões de Atalho Padronizados e Barra de Pesquisa Global -->
        <div class="cabecalho-botoes-atalho border-secondary pt-2 border-top d-flex flex-wrap align-items-center justify-content-between gap-2">
            <div class="d-flex align-items-center gap-2">
                <c:if test="${podeEditar}">
                    <a href="${pageContext.request.contextPath}/ProdutoServlet" class="btn btn-success btn-moderno px-3 py-2 text-white">
                        <i class="fas fa-plus me-1"></i> Novo Produto
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/MenuServlet" class="btn btn-dark border border-secondary px-3 py-2 text-white">
                    <i class="fa fa-plus me-1"></i> Menu
                </a>
            </div>

            <!-- Centro: Barra de Pesquisa Global -->
            <div class="input-group search-container" style="max-width: 400px; flex-grow: 1;">
                <input type="text" id="busca-global" class="form-control form-control-sm" placeholder="Pesquisar no sistema...">
                <button id="btn-busca-global" class="btn btn-outline-secondary btn-sm bg-white text-dark">
                    <i class="fas fa-search"></i>
                </button>
            </div>
        </div>
    </div>

    <!-- Área de Filtros -->
    <div class="card p-4 mb-4 shadow-sm">
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
    <div class="card p-4 shadow-sm">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0 fw-bold text-primary-dark"><i class="fas fa-list me-2"></i> PRODUTOS CADASTRADOS</h5>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th style="cursor: pointer;" onclick="ordenarTabela('sku')">Código (SKU) <i class="fas fa-sort text-muted ms-1" id="sort-sku"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('nomeTipo')">Tipo <i class="fas fa-sort text-muted ms-1" id="sort-nomeTipo"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('nomeFabricante')">Fabricante <i class="fas fa-sort text-muted ms-1" id="sort-nomeFabricante"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('nomeMarca')">Marca <i class="fas fa-sort text-muted ms-1" id="sort-nomeMarca"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('nomeCategoria')">Categoria <i class="fas fa-sort text-muted ms-1" id="sort-nomeCategoria"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('modelo')">Modelo <i class="fas fa-sort text-muted ms-1" id="sort-modelo"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('descricaoResumida')">Descrição Resumida <i class="fas fa-sort text-muted ms-1" id="sort-descricaoResumida"></i></th>
                        <th style="cursor: pointer;" onclick="ordenarTabela('ativo')">Status <i class="fas fa-sort text-muted ms-1" id="sort-ativo"></i></th>
                        <th class="text-center" style="width: 140px;">Ações</th>
                    </tr>
                </thead>
                <tbody id="tabela-produtos"></tbody>
            </table>
        </div>
        <!-- Rodapé da Tabela / Paginação -->
	    <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top flex-wrap gap-2">
	        <div id="paginacao-info" class="text-muted small"></div>
	        <nav aria-label="Navegação de página">
	            <ul class="pagination pagination-sm mb-0" id="paginacao-numeros"></ul>
	        </nav>
	    </div>
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
 <!-- RODAPÉ DINÂMICO UNIFICADO -->
 <jsp:include page="/WEB-INF/jsp/footer.jsp" />
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