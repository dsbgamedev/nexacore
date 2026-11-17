<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consulta de Fretes - Nexacore</title>
    
    <!-- Bootstrap 5 e Ícones (Padrão NexaCore) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- CSS Padrão do Layout NexaCore -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/estiloFrete.css"> 
</head>
<body class="p-4 bg-light" data-app-context-path="${pageContext.request.contextPath}">

    <!-- 1. INCLUI O MENU LATERAL DA PASTA DE LAYOUT -->
    <jsp:include page="/WEB-INF/jsp/layout/sidebar.jsp" />

    <!-- CONTEÚDO PRINCIPAL DENTRO DO LAYOUT -->
    <main id="main-content" class="nexacore-main">
        
        <!-- 2. INCLUI A TOPBAR DA PASTA DE LAYOUT -->
        <jsp:include page="/WEB-INF/jsp/layout/topbar.jsp" />

        <div class="container-fluid py-4" style="max-width: 1400px; margin: auto;"
             data-context-path="${pageContext.request.contextPath}"
             data-initial-sort-col="${param.colunaOrdenacao != null ? param.colunaOrdenacao : 'dataLancamento'}"
             data-initial-sort-dir="${param.direcaoOrdenacao != null ? param.direcaoOrdenacao : 'desc'}">

            <!-- Cabeçalho Padrão com Tema Escuro Unificado -->
            <div class="card-cabecalho-padrao tema-escuro p-4 mb-4 shadow-sm border-0">
                <div class="mb-3">
                    <h4 class="page-title fw-bold text-white mb-1">
                        <i class="fa-solid fa-truck-fast me-2"></i> CONSULTA DE LANÇAMENTOS DE FRETES
                    </h4>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0" style="font-size: 0.85rem;">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet" class="text-light text-decoration-none">Home</a></li>
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/LancarFreteServlet" class="text-light text-decoration-none">Frete</a></li>
                            <li class="breadcrumb-item active text-secondary" aria-current="page">Consulta de Fretes</li>
                        </ol>
                    </nav>
                </div>
                
                <!-- Botões de Atalho Padronizados -->
                <div class="cabecalho-botoes-atalho border-secondary pt-2 border-top d-flex flex-wrap gap-2">
                    <a href="${pageContext.request.contextPath}/LancarFreteServlet" class="btn btn-success btn-moderno px-3 py-2 text-white">
                        <i class="fa fa-plus me-1"></i> Lançar Novo Frete
                    </a>
                    <a href="${pageContext.request.contextPath}/MenuServlet" class="btn btn-dark border border-secondary px-3 py-2 text-white">
                        <i class="fa fa-home me-1"></i> Menu
                    </a>
                </div>
            </div>

            <!-- Card de Filtros de Pesquisa -->
            <div class="card p-4 mb-4 shadow-sm">
                <div class="d-flex align-items-center mb-3 text-primary">
                    <i class="fa fa-filter me-2"></i>
                    <h5 class="mb-0 fw-bold">FILTROS DE PESQUISA</h5>
                </div>

                <form id="formConsultaFrete" class="form-filtros" action="${pageContext.request.contextPath}/ConsultaFreteServlet" method="post">
                    <input type="hidden" id="exportType" name="exportType" value="">
                    <input type="hidden" id="dataType" name="dataType" value="fretes">
                    <input type="hidden" id="colunaOrdenacao" name="colunaOrdenacao" value="${param.colunaOrdenacao != null ? param.colunaOrdenacao : 'dataLancamento'}">
                    <input type="hidden" id="direcaoOrdenacao" name="direcaoOrdenacao" value="${param.direcaoOrdenacao != null ? param.direcaoOrdenacao : 'desc'}">
                    <input type="hidden" id="pagina" name="pagina" value="${param.pagina != null ? param.pagina : '1'}">

                    <div class="row g-3 mb-3">
                        <div class="col-md-3">
                            <label for="dataInicio" class="form-label">Data Início</label>
                            <input type="date" class="form-control form-control-sm" id="dataInicio" name="dataInicio" value="${param.dataInicio != null ? param.dataInicio : '2026-01-01'}">
                        </div>
                        
                        <div class="col-md-3">
                            <label for="dataFim" class="form-label">Data Fim</label>
                            <input type="date" class="form-control form-control-sm" id="dataFim" name="dataFim" value="${param.dataFim != null ? param.dataFim : '2026-12-31'}">
                        </div>
                        
                        <div class="col-md-3">
                            <label for="status" class="form-label">Status do Frete</label>
                            <select class="form-select form-select-sm" id="status" name="status">
                                <option value="" ${param.status == '' ? 'selected' : ''}>Todos</option>
                                <option value="PENDENTE" ${param.status == 'PENDENTE' ? 'selected' : ''}>Pendente</option>
                                <option value="PAGO" ${param.status == 'PAGO' ? 'selected' : ''}>Pago</option>
                                <option value="CANCELADO" ${param.status == 'CANCELADO' ? 'selected' : ''}>Cancelado</option>
                            </select>
                        </div>

                        <!-- Transportadora com Digitação e Autocomplete -->
                        <div class="col-md-3">
                            <label for="transportadoraBusca" class="form-label">Transportadora</label>
                            <input type="text" class="form-control form-control-sm" id="transportadoraBusca" 
                                   list="listaTransportadorasDatalist" placeholder="Digite para buscar..." autocomplete="off"
                                   value="${param.transportadoraNome != null ? param.transportadoraNome : ''}">
                            <input type="hidden" id="transportadoraId" name="transportadoraId" value="${param.transportadoraId != null ? param.transportadoraId : '0'}">
                            
                            <datalist id="listaTransportadorasDatalist">
                                <c:forEach var="t" items="${transportadoras}">
                                    <option data-id="${t.idTransportadora}" value="${t.nomeFantasia}"></option>
                                </c:forEach>
                            </datalist>
                        </div>
                    </div>
                    
                    <div class="row g-3 align-items-end">
                        <div class="col-md-6">
                            <label for="termoPesquisa" class="form-label">Pesquisa Rápida</label>
                            <div class="input-group input-group-sm">
                                <select id="campoPesquisa" name="campoPesquisa" class="form-select" style="max-width: 180px;">
                                    <option value="todos" ${param.campoPesquisa == 'todos' ? 'selected' : ''}>Todos os Campos</option>
                                    <option value="nomeCliente" ${param.campoPesquisa == 'nomeCliente' ? 'selected' : ''}>Cliente</option>
                                    <option value="notaFiscal" ${param.campoPesquisa == 'notaFiscal' ? 'selected' : ''}>Nota Fiscal</option>
                                    <option value="nomeTransportadora" ${param.campoPesquisa == 'nomeTransportadora' ? 'selected' : ''}>Transportadora</option>
                                    <option value="observacao" ${param.campoPesquisa == 'observacao' ? 'selected' : ''}>Observação</option>
                                </select>
                                <input type="text" id="termoPesquisa" name="termoPesquisa" class="form-control maiusculo" 
                                       placeholder="Digite o termo para buscar..." 
                                       value="${param.termoPesquisa != null ? param.termoPesquisa : ''}">
                            </div>
                        </div>

                        <div class="col-md-6 d-flex justify-content-end gap-2">
                            <button type="button" id="btnLimparFiltros" class="btn btn-outline-secondary btn-sm px-3">
                                <i class="fas fa-eraser me-1"></i> Limpar Filtros
                            </button>
                            <button type="submit" class="btn btn-primary btn-sm px-4">
                                <i class="fas fa-search me-1"></i> Consultar
                            </button>
                        </div>
                    </div>
                </form>
            </div>

            <!-- Card da Tabela de Resultados -->
            <div class="card p-4 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <h5 class="mb-0 fw-bold text-primary-dark">
                        <i class="fa fa-list me-2"></i> FRETES ENCONTRADOS
                    </h5>
                    
                    <!-- Botões de Exportação Padronizados -->
                    <div class="d-flex gap-2">
                        <button type="button" id="exportExcelBtn" class="btn btn-outline-success btn-sm" title="Exportar para Excel" ${empty listaFretes ? 'disabled' : ''}>
                            <i class="fa-solid fa-file-excel me-1"></i> Excel
                        </button>
                        <button type="button" id="exportPdfBtn" class="btn btn-outline-danger btn-sm" title="Exportar para PDF" 
                            ${(empty listaFretes || usuarioLogado.perfil != 'administrador') ? 'disabled' : ''}>
                            <i class="fa-solid fa-file-pdf me-1"></i> PDF
                        </button>
                    </div>
                </div>
                
                <div class="table-responsive">
                    <table id="tabelaFretes" class="table table-striped table-hover align-middle mb-0" style="font-size: 0.85rem;">
                        <thead style="background-color: #0f172a; color: #ffffff;">
                            <tr>
                                <th class="sortable text-white" data-sort-field="id" style="cursor: pointer;">ID</th>
                                <th class="sortable text-white" data-sort-field="dataLancamento" style="cursor: pointer;">Data</th>
                                <th class="sortable text-white" data-sort-field="horaLancamento" style="cursor: pointer;">Hora</th>
                                <th class="sortable text-white" data-sort-field="nomeEmpresa" style="cursor: pointer;">Empresa</th>
                                <th class="sortable text-white" data-sort-field="nomeTransportadora" style="cursor: pointer;">Transportadora</th>
                                <th class="sortable text-white" data-sort-field="nomeCliente" style="cursor: pointer;">Cliente</th>
                                <th class="sortable text-white" data-sort-field="notaFiscal" style="cursor: pointer;">Nota Fiscal</th>
                                <th class="sortable text-white text-end" data-sort-field="volume" style="cursor: pointer;">Volume</th>
                                <th class="sortable text-white text-end" data-sort-field="pesoLiquido" style="cursor: pointer;">Peso Líq. (kg)</th>
                                <th class="sortable text-white text-end" data-sort-field="valorNf" style="cursor: pointer;">Valor NF (R$)</th>
                                <th class="sortable text-white" data-sort-field="observacao" style="cursor: pointer;">Obs</th>
                                <th class="sortable text-white text-end" data-sort-field="porcentagemFrete" style="cursor: pointer;">%</th>
                                <th class="sortable text-white text-end" data-sort-field="valorFrete" style="cursor: pointer;">Valor Frete (R$)</th>
                            </tr>
                        </thead>
                        
                        <tbody id="tbodyFretesSessao">
                            <c:choose>
                                <c:when test="${not empty listaFretes}"> 
                                    <c:forEach var="frete" items="${listaFretes}">
                                        <tr>
                                            <td><c:out value="${frete.id}"/></td>
                                            <td><fmt:formatDate value="${frete.dataLancamentoJsp}" pattern="dd/MM/yyyy"/></td>
                                            <td><fmt:formatDate value="${frete.horaLancamentoJsp}" pattern="HH:mm"/></td>
                                            <td><c:out value="${frete.nomeEmpresa}"/></td>
                                            <td><c:out value="${frete.nomeTransportadora}"/></td>
                                            <td><c:out value="${frete.nomeCliente}"/></td>
                                            <td><c:out value="${frete.notaFiscal}"/></td>
                                            <td class="text-end"><c:out value="${frete.volume}"/></td>
                                            <td class="text-end"><fmt:formatNumber value="${frete.pesoLiquido}" pattern="#,##0.00"/></td>
                                            <td class="text-end"><fmt:formatNumber value="${frete.valorNf}" type="currency" currencySymbol="R$"/></td>
                                            <td><c:out value="${frete.observacao}"/></td>
                                            <td class="text-end"><fmt:formatNumber value="${frete.porcentagemFrete}" pattern="#,##0.00"/>%</td>
                                            <td class="text-end"><fmt:formatNumber value="${frete.valorFrete}" type="currency" currencySymbol="R$"/></td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="13" class="text-center text-muted py-4">
                                            Nenhum frete encontrado. Utilize os filtros acima e clique em "Consultar".
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                        
                        <tfoot>
                            <tr class="table-primary">
                                <td colspan="7" class="text-end fw-bold">TOTAIS DA CONSULTA:</td>
                                <td id="totalVolume" class="fw-bold text-end">
                                    <c:choose>
                                        <c:when test="${not empty listaFretes}"><fmt:formatNumber value="${totalVolume}" pattern="#,##0"/></c:when>
                                        <c:otherwise>0</c:otherwise>
                                    </c:choose>
                                </td> 
                                <td id="totalPesoLiquido" class="fw-bold text-end">
                                    <c:choose>
                                        <c:when test="${not empty listaFretes}"><fmt:formatNumber value="${totalPesoLiquido}" pattern="#,##0.00"/></c:when>
                                        <c:otherwise>0,00</c:otherwise>
                                    </c:choose>
                                </td>
                                <td id="totalValorNf" class="fw-bold text-end">
                                    <c:choose>
                                        <c:when test="${not empty listaFretes}"><fmt:formatNumber value="${totalValorNf}" type="currency" currencySymbol="R$"/></c:when>
                                        <c:otherwise>R$ 0,00</c:otherwise>
                                    </c:choose>
                                </td>
                                <td></td>
                                <td></td>
                                <td id="totalValorFrete" class="fw-bold text-end">
                                    <c:choose>
                                        <c:when test="${not empty listaFretes}"><fmt:formatNumber value="${totalValorFrete}" type="currency" currencySymbol="R$"/></c:when>
                                        <c:otherwise>R$ 0,00</c:otherwise>
                                    </c:choose>
                                </td>              
                            </tr>
                        </tfoot>
                    </table>
                </div>

                <!-- Paginação e Mensagens -->
                <div id="paginacaoContainer" class="d-flex justify-content-center mt-3"></div>
                <div id="statusMensagem" class="alert d-none mt-3" role="alert"></div>
            </div>
        </div>

        <!-- 3. INCLUI O RODAPÉ DA PASTA DE LAYOUT -->
        <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />

    </main>

    <!-- Scripts Globais (Bootstrap 5 + Scripts NexaCore) -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/consultaFrete.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
</body>
</html>