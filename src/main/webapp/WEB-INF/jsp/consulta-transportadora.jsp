<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Transportadoras</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body data-app-context-path="${pageContext.request.contextPath}">

<div class="container-fluid py-4">
    <!-- Cabeçalho da Página -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="page-title fw-bold text-primary-dark">CONSULTA DE TRANSPORTADORAS</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                    <!-- Rota corrigida para o servlet centralizador -->
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/TransportadoraServlet">Transportadoras</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Consulta de Transportadoras</li>
                </ol>
            </nav>
        </div>
        
        <div>
            <!-- Botão Nova Transportadora agora aponta para a renderização de cadastro via Servlet -->
            <a href="${pageContext.request.contextPath}/TransportadoraServlet?acao=novo" class="btn btn-success">
                <i class="fa fa-plus me-1"></i> Nova Transportadora
            </a>
        </div>
    </div>

    <!-- Área de Filtros de Pesquisa -->
    <div class="card p-4 mb-4">
        <div class="d-flex align-items-center mb-3 text-primary">
            <i class="fa fa-filter me-2"></i>
            <h5 class="mb-0 fw-bold">FILTROS DE PESQUISA</h5>
        </div>
        
        <!-- O formulário de filtro agora submete para o TransportadoraServlet -->
        <form action="${pageContext.request.contextPath}/TransportadoraServlet" method="get" id="formFiltroTransportadora">
            <input type="hidden" name="acao" value="filtrar">
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">Pesquisa (Razão Social / Nome Fantasia)</label>
                    <input type="text" class="form-control form-control-sm" name="pesquisa" value="${filtroPesquisa}" placeholder="Digite a razão social ou nome fantasia...">
                </div>
                <div class="col-md-4">
                    <label class="form-label">CNPJ</label>
                    <input type="text" class="form-control form-control-sm" name="cnpj" value="${filtroCnpj}" placeholder="Digite o CNPJ...">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Status</label>
                    <select class="form-select form-select-sm" name="status">
                        <option value="">Todos...</option>
                        <option value="ATIVA" ${filtroStatus == 'ATIVA' ? 'selected' : ''}>Ativa</option>
                        <option value="INATIVA" ${filtroStatus == 'INATIVA' ? 'selected' : ''}>Inativa</option>
                    </select>
                </div>
                
                <div class="col-12 d-flex justify-content-end gap-2 mt-3 pt-3 border-top">
                    <a href="${pageContext.request.contextPath}/TransportadoraServlet" class="btn btn-outline-secondary btn-sm">
                        <i class="fa fa-sync-alt me-1"></i> Limpar Filtros
                    </a>
                    <button type="submit" class="btn btn-primary btn-sm px-4">
                        <i class="fa fa-search me-1"></i> Pesquisar
                    </button>
                </div>
            </div>
        </form>
    </div>

    <!-- Tabela de Resultados -->
    <div class="card p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0 fw-bold text-primary-dark">
                <i class="fa fa-list me-2"></i> TRANSPORTADORAS CADASTRADAS 
            </h5>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="font-size: 0.85rem;">
                <thead class="table-light text-secondary">
                    <tr>
                        <th>ID</th>
                        <th>Razão Social</th>
                        <th>Nome Fantasia</th>
                        <th>CNPJ</th>
                        <th>Telefone</th>
                        <th>E-mail</th>
                        <th>Status</th>
                        <th class="text-center" style="width: 140px;">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty transportadoras}">
                            <c:forEach var="t" items="${transportadoras}">
                                <tr>
                                    <td>${t.idTransportadora}</td>
                                    <td>${t.razaoSocial}</td>
                                    <td>${t.nomeFantasia != null ? t.nomeFantasia : '-'}</td>
                                    <td>${t.cnpj}</td>
                                    <td>${t.telefone != null ? t.telefone : '-'}</td>
                                    <td>${t.email != null ? t.email : '-'}</td>
                                    <td>
									    <span class="badge ${t.statusBadgeClass}">${t.status}</span>
									</td>
                                    <td class="text-center">
                                        <button type="button" class="btn btn-outline-secondary btn-sm border-0 btn-visualizar" data-id="${t.idTransportadora}" title="Visualizar"><i class="fa fa-eye"></i></button>
                                        <button type="button" class="btn btn-outline-primary btn-sm border-0 btn-editar" data-id="${t.idTransportadora}" title="Editar"><i class="fa fa-pen"></i></button>
                                        <button type="button" class="btn btn-outline-danger btn-sm border-0 btn-excluir" data-id="${t.idTransportadora}" title="Excluir"><i class="fa fa-trash"></i></button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center text-muted py-4">Nenhuma transportadora encontrada. Utilize os filtros acima.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal de Detalhes / Visualização -->
<div class="modal fade" id="modalDetalhesTransportadora" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title"><i class="fas fa-info-circle me-2"></i>Detalhes da Transportadora</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="conteudoDetalhesTransportadora">
                <!-- Carregado via AJAX / Servlet -->
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<!-- SCRIPTS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Única variável global de contexto necessária para os arquivos JS externos
    const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/js/consulta-transportadora.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>