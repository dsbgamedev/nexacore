<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexaCore - Sistema de Gestão de Equipamentos</title>
    
    <!-- Bootstrap 5.3 & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <!-- FontAwesome Icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    
    <!-- CSS Globais e do Layout -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    
    <style>
        .card-custom { border: none; border-radius: 12px; box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075); background: #fff; }
        .badge-acao { font-size: 0.75rem; padding: 0.35em 0.65em; font-weight: 600; border-radius: 4px; }
        .bg-editar { background-color: #fff3cd; color: #856404; }
        .bg-criar { background-color: #cfe2ff; color: #084298; }
        .bg-excluir { background-color: #f8d7da; color: #842029; }
        .avatar-circle { width: 32px; height: 32px; background-color: #0d6efd; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: bold; }
    </style>
</head>
<body>
<div class="container-fluid py-4 px-4">
    <h4 class="page-title fw-bold text-primary-dark">AUDITORIA NEXACORE</h4>
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-4">
           <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
           <li class="breadcrumb-item active" aria-current="page">Auditoria</li>
        </ol>
    </nav>    

    <!-- Filtros de Pesquisa -->
    <div class="card card-custom p-4 mb-4">
        <div class="d-flex align-items-center text-primary mb-3">
            <i class="fa-solid fa-filter me-2"></i>
            <span class="fw-bold small text-uppercase">Filtros de Pesquisa</span>
        </div>
        
        <form action="${pageContext.request.contextPath}/AuditoriaServlet" method="GET">
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Usuário</label>
                    <input type="text" name="usuario" class="form-control form-control-sm" value="${param.usuario}" placeholder="Nome ou login">
                </div>
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Módulo</label>
                    <input type="text" name="modulo" class="form-control form-control-sm" value="${param.modulo}" placeholder="Ex: Manutenção">
                </div>
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Ação</label>
                    <select name="tipoAcao" class="form-select form-select-sm">
                        <option value="">Todas as ações</option>
                        <option value="CRIAR" ${param.tipoAcao == 'CRIAR' ? 'selected' : ''}>CRIAR</option>
                        <option value="EDITAR" ${param.tipoAcao == 'EDITAR' ? 'selected' : ''}>EDITAR</option>
                        <option value="EXCLUIR" ${param.tipoAcao == 'EXCLUIR' ? 'selected' : ''}>EXCLUIR</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Entidade</label>
                    <input type="text" name="entidade" class="form-control form-control-sm" value="${param.entidade}" placeholder="Ex: manutencao_chamados">
                </div>

                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Data Início</label>
                    <input type="date" name="dataInicio" class="form-control form-control-sm" value="${param.dataInicio}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">Data Fim</label>
                    <input type="date" name="dataFim" class="form-control form-control-sm" value="${param.dataFim}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small text-muted fw-semibold">IP de Origem</label>
                    <input type="text" name="ipOrigem" class="form-control form-control-sm" value="${param.ipOrigem}" placeholder="Ex: 192.168.0.1">
                </div>
                <div class="col-md-3 d-flex align-items-end gap-2">
                    <button type="submit" class="btn btn-primary btn-sm w-100 shadow-sm"><i class="fa-solid fa-magnifying-glass me-1"></i> Pesquisar</button>
                    <a href="${pageContext.request.contextPath}/AuditoriaServlet" class="btn btn-outline-secondary btn-sm w-100"><i class="fa-solid fa-rotate-right me-1"></i> Limpar</a>
                </div>
            </div>
        </form>
    </div>

    <!-- Tabela de Registros -->
    <div class="card card-custom p-3">
        <div class="d-flex justify-content-between align-items-center mb-3 px-2">
            <span class="badge bg-light text-secondary border px-3 py-2 fw-normal">
                <i class="fa-solid fa-database me-1"></i> <strong>${totalRegistros != null ? totalRegistros : 0}</strong> registros encontrados
            </span>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light text-uppercase fs-7 text-muted">
                    <tr>
                        <th class="py-3">Data/Hora</th>
                        <th>Usuário</th>
                        <th>Módulo</th>
                        <th>Ação</th>
                        <th>Entidade / Registro</th>
                        <th>IP Origem</th>
                        <th>Sucesso</th>
                        <th class="text-center">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="audit" items="${listaAuditoria}">
                        <tr>
                            <td><fmt:formatDate value="${audit.dataHora}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <div class="avatar-circle">
                                        <c:choose>
                                            <c:when test="${not empty audit.usuarioNome and fn:length(audit.usuarioNome) gt 0}">
                                                ${fn:toUpperCase(fn:substring(audit.usuarioNome, 0, 1))}
                                            </c:when>
                                            <c:otherwise>U</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div>
                                        <div class="fw-semibold text-dark small">${audit.usuarioNome}</div>
                                        <div class="text-muted" style="font-size: 0.75rem;">ID: ${audit.usuarioId}</div>
                                    </div>
                                </div>
                            </td>
                            <td><i class="fa-solid fa-cube text-muted me-1"></i> ${audit.modulo}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${audit.acao == 'CRIAR'}"><span class="badge badge-acao bg-criar">CRIAR</span></c:when>
                                    <c:when test="${audit.acao == 'EDITAR'}"><span class="badge badge-acao bg-editar">EDITAR</span></c:when>
                                    <c:when test="${audit.acao == 'EXCLUIR'}"><span class="badge badge-acao bg-excluir">EXCLUIR</span></c:when>
                                    <c:otherwise><span class="badge badge-acao bg-secondary">${audit.acao}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="small fw-semibold text-dark">${audit.entidade}</div>
                                <div class="text-muted" style="font-size: 0.75rem;">ID: ${audit.registroId}</div>
                            </td>
                            <td class="small font-monospace">${audit.ipOrigem}</td>
                            <td><span class="badge bg-success-subtle text-success border border-success-subtle"><i class="fa-solid fa-check me-1"></i> Sim</span></td>
                            <td class="text-center">
                                <button class="btn btn-sm btn-light text-primary border me-1" onclick="verDetalhes(${audit.id})" title="Ver Detalhes">
                                    <i class="fa-regular fa-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-light text-danger border" onclick="excluirLog(${audit.id})" title="Excluir Registro">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty listaAuditoria}">
                        <tr>
                            <td colspan="8" class="text-center py-4 text-muted">Nenhum registro de auditoria encontrado.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal de Detalhes -->
<div class="modal fade" id="modalDetalhes" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title"><i class="fa-solid fa-file-lines me-2"></i> Detalhes da Auditoria</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p><strong>Descrição:</strong> <span id="modalDescricao"></span></p>
                <div class="row">
                    <div class="col-md-6">
                        <label class="form-label fw-bold text-muted small">Dados Anteriores (JSON):</label>
                        <pre id="modalAntes" class="bg-light p-3 border rounded small" style="max-height: 250px; overflow-y: auto;"></pre>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-bold text-muted small">Dados Novos (JSON):</label>
                        <pre id="modalDepois" class="bg-light p-3 border rounded small" style="max-height: 250px; overflow-y: auto;"></pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- ==========================================================
     MODAIS DO MODAL SERVICE (Alerta e Confirmação)
========================================================== -->
<div class="modal fade" id="alertModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content-custom" id="alertBox">
            <h3 id="alertTitle"></h3>
            <p id="alertMessage"></p>
            <div class="modal-buttons">
                <button type="button" class="btn-confirmar" id="alertOkBtn">OK</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="confirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: transparent; border: none; box-shadow: none;">
            <div class="modal-content-custom" id="confirmBox">
                <h3 id="confirmTitle"></h3>
                <p id="confirmMessage"></p>
                <div class="modal-buttons">
                    <button type="button" class="btn-cancelar" id="confirmCancelBtn">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="confirmOkBtn">Confirmar</button>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<!-- Configura o ContextPath global para uso nos arquivos JS externos -->
<script> const contextPath = '${pageContext.request.contextPath}'; </script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auditoria.js"></script>
</body>
</html>