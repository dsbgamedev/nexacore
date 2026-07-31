<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Equipamentos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>

<div class="container-fluid py-4">
    <!-- Cabeçalho da Página -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="page-title fw-bold text-primary-dark">CONSULTA DE EQUIPAMENTOS (UNIDADE FÍSICA)</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/jsp/index.jsp">Home</a></li>
                    <li class="breadcrumb-item"><a href="#">Equipamentos</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Consulta de Equipamentos</li>
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
        
        
        
        <div>
            <a href="${pageContext.request.contextPath}/jsp/cadastro-equipamento.jsp" class="btn btn-success">
                <i class="fa fa-plus me-1"></i> Novo Equipamento
            </a>
        </div>
    </div>

    <!-- Área de Filtros de Pesquisa -->
    <div class="card p-4 mb-4">
        <div class="d-flex align-items-center mb-3 text-primary">
            <i class="fa fa-filter me-2"></i>
            <h5 class="mb-0 fw-bold">FILTROS DE PESQUISA</h5>
        </div>
        
        <form id="formFiltroEquipamento">
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">Produto (Catálogo)</label>
                    <input type="text" class="form-control form-control-sm" id="filtroProduto" placeholder="Selecione o produto...">
                </div>
                <div class="col-md-2">
                    <label class="form-label">ID Sistema</label>
                    <input type="text" class="form-control form-control-sm" id="filtroIdSistema" placeholder="Ex: EQ000154">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Patrimônio / Etiqueta</label>
                    <input type="text" class="form-control form-control-sm" id="filtroPatrimonio" placeholder="Digite patrimônio...">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Número de Série</label>
                    <input type="text" class="form-control form-control-sm" id="filtroSerial" placeholder="Digite o número de série...">
                </div>
                
                <div class="col-md-3">
				    <label class="form-label">Origem (Filial)</label>
				    <select class="form-select form-select-sm" id="filtroOrigem">
				        <option value="">Selecione...</option>
				    </select>
				</div>
                <div class="col-md-3">
                    <label class="form-label">Departamento / Setor</label>
                    <select class="form-select form-select-sm" id="filtroDepartamento">
                        <option value="">Todos os setores...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Usuário Atual</label>
                    <input type="text" class="form-control form-control-sm" id="filtroUsuario" placeholder="Nome do usuário...">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Status</label>
                    <select class="form-select form-select-sm" id="filtroStatus">
                    	<option value="">Selecione...</option>
                        <option value="">Todos os status</option>
                        <option value="Ativo">Ativo</option>
                        <option value="Em Manutencao">Em Manutenção</option>
                        <option value="Inativo">Inativo</option>
                    </select>
                </div>

                <div class="col-12 d-flex justify-content-end gap-2 mt-3 pt-3 border-top">
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="limparFiltros()">
                        <i class="fa fa-sync-alt me-1"></i> Limpar Filtros
                    </button>
                    <button type="submit" class="btn btn-primary btn-sm px-4" onclick="pesquisarEquipamentos(); return false;">
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
                <i class="fa fa-list me-2"></i> EQUIPAMENTOS CADASTRADOS 
                <span class="badge bg-light text-primary border ms-2" id="totalRegistros" style="font-size: 0.75rem;">0 registros encontrados</span>
            </h5>
            <div class="d-flex align-items-center gap-2">
                <span class="text-muted small">Registros por página:</span>
                <select class="form-select form-select-sm select-paginacao" style="width: 70px;" id="qtdPorPagina">
                    <option value="10" selected>10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                </select>
            </div>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="font-size: 0.85rem;">
                <thead class="table-light text-secondary">
                    <tr>
                        <th>ID Sistema</th>
                        <th>Patrimônio / Etiqueta</th>
                        <th>Produto (Catálogo)</th>
                        <th>Nome / CPU</th>
                        <th>Origem (Filial)</th>
                        <th>Usuário Atual</th>
                        <th>Departamento</th>
                        <th>Status</th>
                        <th>Nº de Série</th>
                        <th class="text-center" style="width: 140px;">Ações</th>
                    </tr>
                </thead>
                <tbody id="tabelaEquipamentosBody">
                    <tr>
                        <td colspan="10" class="text-center text-muted py-4">Utilize os filtros acima para consultar os equipamentos.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
            <div class="text-muted small" id="infoPaginacao">
                Mostrando 0 registros
            </div>
            <nav>
                <ul class="pagination pagination-sm mb-0" id="paginacaoContainer">
                    <li class="page-item disabled"><a class="page-link" href="#">Anterior</a></li>
                    <li class="page-item active"><a class="page-link" href="#">1</a></li>
                    <li class="page-item disabled"><a class="page-link" href="#">Próximo</a></li>
                </ul>
            </nav>
        </div>
    </div>
</div>

<!-- Modais Padronizados -->
<div class="modal fade" id="alertModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content-custom" id="alertBox">
            <h3 id="alertTitle">Atenção</h3>
            <p id="alertMessage"></p>
            <div class="modal-buttons">
                <button type="button" class="btn-confirmar" id="alertOkBtn" data-bs-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="confirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: transparent; border: none; box-shadow: none;">
            <div class="modal-content-custom" id="confirmBox">
                <h3 id="confirmTitle">Confirmação</h3>
                <p id="confirmMessage"></p>
                <div class="modal-buttons">
                    <button type="button" class="btn-cancelar" id="confirmCancelBtn" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="confirmOkBtn">Confirmar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- SCRIPTS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Variável global de contexto padronizada para os arquivos JS externos
    const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/consulta-equipamento.js"></script>
</body>
</html>