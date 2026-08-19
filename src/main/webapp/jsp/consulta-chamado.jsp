<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Chamados de Manutenção</title>
    <script>
        var contextPath = "<%= request.getContextPath() %>";
    </script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <style>
        .filter-card { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 20px; margin-bottom: 25px; }
        .section-title { font-size: 1.05rem; font-weight: 600; color: #1e293b; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; margin-bottom: 20px; }
    </style>
</head>
<body class="p-4 bg-light">

    <!-- CABEÇALHO / BREADCRUMB -->
    <div class="container-fluid mb-4 d-flex justify-content-between align-items-center">
        <div>
            <h4 class="page-title text-uppercase fw-bold"><i class="fa-solid fa-clipboard-list me-2"></i> Consulta de Chamados de Manutenção</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="#">Home</a></li>
                    <li class="breadcrumb-item"><a href="#">Manutenções</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Consulta de Chamados</li>
                </ol>
            </nav>
        </div>
        <a href="${pageContext.request.contextPath}/jsp/manutencao-abertura.jsp" class="btn btn-primary btn-sm">
            <i class="fa-solid fa-plus me-1"></i> Novo Chamado
        </a>
    </div>

    <div class="container-fluid">
        
        <!-- FILTROS DE PESQUISA -->
        <div class="filter-card shadow-sm">
            <div class="section-title"><i class="fa-solid fa-filter me-1"></i> Filtros de Pesquisa</div>
            <form id="formFiltroChamados">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <label class="form-label small fw-bold">Pesquisa Geral</label>
                        <input type="text" class="form-control form-control-sm" id="filtroBusca" placeholder="ID, patrimônio, descrição...">
                    </div>
                    <div class="col-md-2 mb-3">
                        <label class="form-label small fw-bold">Status do Chamado</label>
                        <select class="form-select form-select-sm" id="filtroStatus">
                            <option value="">Todos</option>
                            <option value="1">Aberto / Pendente</option>
                            <option value="2">Em Análise</option>
                            <option value="3">Em Atendimento</option>
                            <option value="4">Aguardando Peça</option>
                            <option value="5">Aguardando Terceiro</option>
                            <option value="6">Finalizado</option>
                            <option value="7">Cancelado</option>
                        </select>
                    </div>
                    <div class="col-md-2 mb-3">
                        <label class="form-label small fw-bold">Tipo de Manutenção</label>
                        <select class="form-select form-select-sm" id="filtroTipo">
                            <option value="">Todas</option>
                            <option value="Corretiva">Corretiva</option>
                            <option value="Preventiva">Preventiva</option>
                        </select>
                    </div>
                    <div class="col-md-2 mb-3">
                        <label class="form-label small fw-bold">Prioridade</label>
                        <select class="form-select form-select-sm" id="filtroPrioridade">
                            <option value="">Todas</option>
                            <option value="Baixa">Baixa</option>
                            <option value="Média">Média</option>
                            <option value="Alta">Alta</option>
                            <option value="Urgente">Urgente</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3 d-flex align-items-end justify-content-end">
                        <button type="button" class="btn btn-outline-secondary btn-sm me-2 px-3" onclick="limparFiltros()">
                            <i class="fa fa-eraser me-1"></i> Limpar
                        </button>
                        <button type="submit" class="btn btn-primary btn-sm px-4">
                            <i class="fa fa-search me-1"></i> Pesquisar
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- TABELA DE CHAMADOS -->
        <div class="card p-4 shadow-sm mb-4">
            <div class="section-title d-flex justify-content-between align-items-center">
                <span><i class="fa-solid fa-table-list me-1"></i> Chamados Registrados</span>
                <span class="badge bg-secondary small" id="contadorRegistros">0 registros encontrados</span>
            </div>
            
            <div class="table-responsive">
                <table class="table table-sm table-striped table-hover align-middle small">
                    <thead class="table-dark">
                        <tr>
                            <th>ID</th>
                            <th>Abertura</th>
                            <th>Equipamento</th>
                            <th>Problema / Descrição</th>
                            <th>Tipo</th>
                            <th>Prioridade</th>
                            <th>Status</th>
                            <th>Responsável</th>
                            <th class="text-center">Ações</th>
                        </tr>
                    </thead>
                    <tbody id="tabelaChamados">
                        <tr>
                            <td colspan="9" class="text-center text-muted py-3">Carregando chamados...</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- MODAL DE VISUALIZAÇÃO / ENCERRAMENTO DO CHAMADO -->
    <div class="modal fade" id="modalGerenciarChamado" tabindex="-1" data-bs-backdrop="static">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white py-2">
                    <h5 class="modal-title fs-6"><i class="fa-solid fa-screwdriver-wrench me-1"></i> Gerenciar Chamado de Manutenção</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body bg-light">
                    <form id="formGerenciarChamado">
                        <input type="hidden" id="modalIdChamado">
                        
                        <!-- Informações Básicas (Somente Leitura) -->
                        <div class="row g-2 mb-3 bg-white p-3 rounded border">
                            <div class="col-md-4"><span class="text-muted small d-block">Equipamento:</span> <strong id="detalheEquip">---</strong></div>
                            <div class="col-md-4"><span class="text-muted small d-block">Solicitante:</span> <strong id="detalheSolicitante">---</strong></div>
                            <div class="col-md-4"><span class="text-muted small d-block">Data Abertura:</span> <strong id="detalheDataAbertura">---</strong></div>
                            <div class="col-12 mt-2"><span class="text-muted small d-block">Descrição do Problema Relatado:</span> <p class="text-dark mb-0 bg-light p-2 rounded small" id="detalheDescricao">---</p></div>
                        </div>

                        <!-- Campos de Intervenção Técnica -->
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-bold">Status Atual do Chamado *</label>
                                <select class="form-select form-select-sm" id="modalStatusChamado" required>
                                    <option value="1">Aberto</option>
                                    <option value="2">Em Análise</option>
                                    <option value="3">Em Atendimento</option>
                                    <option value="4">Aguardando Peça</option>
                                    <option value="5">Aguardando Terceiro</option>
                                    <option value="6">Finalizado</option>
                                    <option value="7">Cancelado</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-bold">Técnico Responsável</label>
                                <input type="text" class="form-control form-control-sm" id="modalResponsavelTecnico" placeholder="Nome do técnico...">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-bold">Diagnóstico Técnico</label>
                                <textarea class="form-control form-control-sm" id="modalDiagnostico" rows="2" placeholder="O que foi constatado..."></textarea>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-bold">Solução Realizada</label>
                                <textarea class="form-control form-control-sm" id="modalSolucao" rows="2" placeholder="O que foi feito para resolver..."></textarea>
                            </div>
                            <div class="col-12" id="blocoReparoCheck" style="display: none;">
                                <div class="form-check bg-white p-2 rounded border">
                                    <input class="form-check-input ms-1" type="checkbox" id="modalFoiReparado" checked>
                                    <label class="form-check-label fw-bold small text-success ms-2" for="modalFoiReparado">
                                        O equipamento foi totalmente reparado e pode retornar ao status normal?
                                    </label>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer py-2">
                    <button type="button" class="btn btn-secondary btn-sm px-3" data-bs-dismiss="modal">Fechar</button>
                    <button type="button" class="btn btn-primary btn-sm px-4" onclick="salvarAtualizacaoChamado()">
                        <i class="fa fa-save me-1"></i> Salvar Alterações
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- MODAIS PADRÃO -->
    <div class="modal fade" id="alertModal" tabindex="-1" data-bs-backdrop="static"><div class="modal-dialog modal-dialog-centered"><div class="modal-content-custom" id="alertBox"><h3 id="alertTitle"></h3><p id="alertMessage"></p><div class="modal-buttons"><button type="button" class="btn-confirmar" id="alertOkBtn">OK</button></div></div></div></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/consulta-chamado.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>