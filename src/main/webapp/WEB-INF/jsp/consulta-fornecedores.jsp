<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="model.Fornecedor,com.google.gson.Gson" %>
<%
    Fornecedor fornecedor = (Fornecedor) request.getAttribute("fornecedor");
    if (fornecedor == null) fornecedor = new Fornecedor();

    String enderecosJson = (String) request.getAttribute("enderecosJson");
    if (enderecosJson == null) enderecosJson = "[]";

    String filiaisIdsJson = (String) request.getAttribute("filiaisIdsJson");
    if (filiaisIdsJson == null) filiaisIdsJson = "[]";
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>NexaCore | Fornecedores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link href="${pageContext.request.contextPath}/assets/css/fornecedores.css" rel="stylesheet">
</head>
<body>
    <div class="nx-layout">
       
        <!-- Main Content -->
        <main class="nx-main">
        
            <div class="nx-content">
                <!-- Breadcrumb e Título Principal -->
                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb mb-0">
                       <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                       <li class="breadcrumb-item active" aria-current="page">Fornecedores</li>
                    </ol>           
                </nav>

                <div class="page-heading mb-4">
                    <div class="page-icon">
                        <i class="fa-solid fa-building"></i>
                    </div>
                    <div>
                        <h1>Fornecedores</h1>
                        <p class="text-muted mb-0">Consulte, cadastre e defina em quais filiais cada fornecedor pode ser utilizado.</p>
                    </div>
                    <div class="heading-actions ms-auto">
                        <a class="btn btn-primary nx-btn" href="${pageContext.request.contextPath}/FornecedorServlet">
                            <i class="fa-solid fa-plus me-2"></i>Novo fornecedor
                        </a>
                    </div>
                </div>

                <c:if test="${not empty erro}">
                    <div class="alert alert-danger">
                        <i class="fa-solid fa-circle-exclamation me-2"></i>
                        <c:out value="${erro}"/>
                    </div>
                </c:if>

                <!-- Cards de Estatísticas -->
                <div class="stats-grid mb-4">
                    <div class="stat-card">
                        <span class="stat-icon blue"><i class="fa-solid fa-building"></i></span>
                        <div>
                            <small>Fornecedores listados</small>
                            <strong>${empty fornecedores ? 0 : fornecedores.size()}</strong>
                        </div>
                    </div>
                    <div class="stat-card">
                        <span class="stat-icon green"><i class="fa-solid fa-circle-check"></i></span>
                        <div>
                            <small>Cadastros ativos</small>
                            <strong>
                                <c:set var="ativos" value="0"/>
                                <c:forEach items="${fornecedores}" var="f">
                                    <c:if test="${f.status == 'ATIVO'}">
                                        <c:set var="ativos" value="${ativos + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${ativos}
                            </strong>
                        </div>
                    </div>
                    <div class="stat-card">
                        <span class="stat-icon purple"><i class="fa-solid fa-code-branch"></i></span>
                        <div>
                            <small>Filiais vinculadas</small>
                            <strong><i class="fa-solid fa-filter"></i></strong>
                        </div>
                    </div>
                </div>

                <!-- Painel de Filtros -->
                <section class="panel mb-4">
                    <div class="panel-title">
                        <div>
                            <h2><i class="fa-solid fa-magnifying-glass me-2"></i>Consulta de fornecedores</h2>
                            <p>Use os filtros para localizar um cadastro.</p>
                        </div>
                    </div>
                    <form method="get" action="${pageContext.request.contextPath}/ConsultaFornecedorServlet" class="filter-grid" id="formFiltros">
                        <div class="field">
                            <label>Nome / Razão social / Contato</label>
                            <input class="form-control" name="busca" value="${filtroBusca}" placeholder="Digite para pesquisar...">
                        </div>
                        <div class="field">
                            <label>CPF / CNPJ</label>
                            <input class="form-control" name="documento" value="${filtroDocumento}" placeholder="Somente números ou formatado">
                        </div>
                        <div class="field">
                            <label>Status</label>
                            <select class="form-select" name="status">
                                <option value="">Todos</option>
                                <option value="ATIVO" ${filtroStatus == 'ATIVO' ? 'selected' : ''}>Ativo</option>
                                <option value="INATIVO" ${filtroStatus == 'INATIVO' ? 'selected' : ''}>Inativo</option>
                                <option value="BLOQUEADO" ${filtroStatus == 'BLOQUEADO' ? 'selected' : ''}>Bloqueado</option>
                            </select>
                        </div>
                        <div class="field">
                            <label>Filial atendida</label>
                            <select class="form-select" name="filial">
                                <option value="">Todas</option>
                                <c:forEach items="${filiais}" var="fl">
                                    <option value="${fl.idFilial}" ${filtroFilial == fl.idFilial ? 'selected' : ''}>
                                        <c:out value="${fl.sufixo}"/> — <c:out value="${fl.nomeEmpresa}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-actions">
                            <button class="btn btn-primary nx-btn">
                                <i class="fa-solid fa-filter me-2"></i>Filtrar
                            </button>
                            <button type="button" class="btn btn-light" onclick="limparFiltrosFornecedores()">Limpar</button>
                        </div>
                    </form>
                </section>

                <!-- Tabela de Resultados -->
                <section class="panel table-panel mb-4">
                    <div class="panel-title">
                        <div>
                            <h2>Fornecedores cadastrados</h2>
                            <p>Os vínculos de filiais controlam onde o fornecedor aparece nos módulos.</p>
                        </div>
                        <span class="count-pill">${empty fornecedores ? 0 : fornecedores.size()} registro(s)</span>
                    </div>
                    <div class="table-responsive">
                        <table class="table nx-table align-middle">
                            <thead>
                                <tr>
                                    <th>Fornecedor</th>
                                    <th>CPF / CNPJ</th>
                                    <th>Contato</th>
                                    <th>Filiais liberadas</th>
                                    <th>Endereços</th>
                                    <th>Status</th>
                                    <th class="text-end">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${fornecedores}" var="f">
    <tr class="${f.status == 'INATIVO' ? 'row-inactive' : ''}">
        <!-- Coluna Fornecedor -->
        <td title="Razão Social: ${f.razaoSocial} | Nome Fantasia: ${f.nomeFantasia}">
            <div class="company-cell">
                <span class="company-avatar"><i class="fa-solid fa-building"></i></span>
                <div>
                    <strong title="${f.razaoSocial}"><c:out value="${f.razaoSocial}"/></strong>
                    <small title="${f.nomeFantasia}"><c:out value="${f.nomeFantasia}"/></small>
                </div>
            </div>
        </td>

        <!-- Coluna CPF / CNPJ -->
        <td title="Documento: ${f.documento} (${f.tipoPessoa == 'PF' ? 'Pessoa física' : 'Pessoa jurídica'})">
            <span class="doc-value" title="${f.documento}"><c:out value="${f.documento}"/></span>
            <small class="d-block text-muted" title="${f.tipoPessoa == 'PF' ? 'Pessoa física' : 'Pessoa jurídica'}">${f.tipoPessoa == 'PF' ? 'Pessoa física' : 'Pessoa jurídica'}</small>
        </td>

        <!-- Coluna Contato -->
        <td title="Contato: ${f.contatoPrincipal} | E-mail: ${f.email}">
            <strong title="${f.contatoPrincipal}"><c:out value="${f.contatoPrincipal}"/></strong>
            <small class="d-block text-muted" title="${f.email}"><c:out value="${f.email}"/></small>
        </td>

        <!-- Coluna Filiais liberadas -->
        <td title="<c:forEach var='fn' items='${f.listaFiliaisNomes}' varStatus='vs'>${fn}${!vs.last ? ', ' : ''}</c:forEach>">
            <c:choose>
                <c:when test="${not empty f.listaFiliaisNomes}">
                    <c:forEach var="filialNome" items="${f.listaFiliaisNomes}">
                        <span class="badge bg-light text-dark border me-1 mb-1" title="${filialNome}">
                            <i class="fa-solid fa-code-branch me-1"></i>${filialNome}
                        </span>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <span class="text-muted small">Nenhuma filial</span>
                </c:otherwise>
            </c:choose>
        </td>

        <!-- Coluna Endereços -->
        <td title="Total de endereços cadastrados: ${f.quantidadeEnderecos}">
            ${f.quantidadeEnderecos}
        </td>

        <!-- Coluna Status -->
        <td title="Status atual: ${f.status}">
            <span class="status-badge ${f.status == 'ATIVO' ? 'status-active' : (f.status == 'BLOQUEADO' ? 'status-blocked' : 'status-inactive')}" title="${f.status}">
                <c:out value="${f.status}"/>
            </span>
        </td>

        <!-- Coluna Ações -->
        <td class="text-end text-nowrap" title="Ações disponíveis">
            <button type="button" class="btn-icon" title="Visualizar" onclick="visualizarFornecedor(${f.idFornecedor}, '${pageContext.request.contextPath}')">
                <i class="fa-solid fa-eye"></i>
            </button>
            <a class="btn-icon" title="Editar" href="${pageContext.request.contextPath}/FornecedorServlet?acao=editar&id=${f.idFornecedor}">
                <i class="fa-solid fa-pen"></i>
            </a>
            <button class="btn-icon danger" title="Excluir" onclick="excluirFornecedor(${f.idFornecedor}, '${pageContext.request.contextPath}')">
                <i class="fa-regular fa-trash-can"></i>
            </button>
        </td>
    </tr>
</c:forEach>
                                <c:if test="${empty fornecedores}">
                                    <tr>
                                        <td colspan="7">
                                            <div class="empty-state py-4">
                                                <i class="fa-solid fa-folder-open fs-2 mb-2"></i>
                                                <strong>Nenhum fornecedor encontrado</strong>
                                                <span>Altere os filtros ou cadastre um novo fornecedor.</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </section>

                <footer class="nx-footer text-center py-3">NexaCore <span>•</span> Gestão de fornecedores</footer>
            </div>
        </main>
    </div>
	<!-- Modais de Alerta e Confirmação Padronizados -->
	<div class="modal fade" id="alertModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
	    <div class="modal-dialog modal-dialog-centered">
	        <div class="modal-content modal-content-custom" id="alertBox">
	            <h3 id="alertTitle" class="fw-bold mb-2"></h3>
	            <p id="alertMessage" class="text-muted mb-4"></p>
	            <div class="modal-buttons">
	                <button type="button" class="btn btn-primary px-4" id="alertOkBtn" data-bs-dismiss="modal">OK</button>
	            </div>
	        </div>
	    </div>
	</div>
	
	<div class="modal fade" id="confirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
	    <div class="modal-dialog modal-dialog-centered">
	        <div class="modal-content modal-content-custom" id="confirmBox">
	            <h3 id="confirmTitle" class="fw-bold mb-2"></h3>
	            <p id="confirmMessage" class="text-muted mb-4"></p>
	            <div class="modal-buttons">
	                <button type="button" class="btn btn-secondary px-4" id="confirmCancelBtn" data-bs-dismiss="modal">Cancelar</button>
	                <button type="button" class="btn btn-danger px-4" id="confirmOkBtn">Confirmar</button>
	            </div>
	        </div>
	    </div>
	</div>
	
	<!-- Modal de Visualização de Fornecedor -->
	<div class="modal fade" id="viewModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
	    <div class="modal-dialog modal-dialog-centered modal-lg">
	        <div class="modal-content" style="border-radius: 12px; border: none; box-shadow: 0 10px 25px rgba(0,0,0,0.1);">
	            <div class="modal-header border-0 pb-0">
	                <h5 class="modal-title fw-bold"><i class="fa-solid fa-building me-2 text-primary"></i>Detalhes do Fornecedor</h5>
	                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
	            </div>
	            <div class="modal-body p-4" id="viewModalBody">
	                <div class="text-center py-4">
	                    <div class="spinner-border text-primary" role="status"></div>
	                    <p class="text-muted mt-2 mb-0">Carregando informações...</p>
	                </div>
	            </div>
	            <div class="modal-footer border-0 pt-0">
	                <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal">Fechar</button>
	            </div>
	        </div>
	    </div>
	</div>

    <script>
        window.NEXACORE_CONTEXT = '${pageContext.request.contextPath}';
        window.FORNECEDOR_ENDERECOS = <%= enderecosJson %>;
        window.FORNECEDOR_FILIAIS = <%= filiaisIdsJson %>;
    </script>
    <!-- Adicione o script do Bootstrap JS aqui -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/fornecedores.js"></script>
</body>
</html>