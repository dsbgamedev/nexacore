<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="model.Usuario" %>
<%
    String ctx = request.getContextPath();
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
    String nomeUsuario = (usuario != null) ? usuario.getUsername() : "Usuário";
%>
<c:if test="${empty sessionScope.usuarioLogado}">
    <c:redirect url="${pageContext.request.contextPath}/LoginServlet?message=session_expired"/>
</c:if>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexaCore - Sistema de Gestão de Equipamentos</title>
        
    <!-- Bootstrap 5.3 & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- CSS Globais e do Layout -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/menu.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/layout.css">
</head>
<body class="nexacore-layout" data-app-context-path="<%=ctx%>" data-session-message="${sessionScope.mensagemSessao}">
<div id="inlineMessageContainer"></div>

    <!-- 1. INCLUI A SIDEBAR MODULARIZADA -->
    <jsp:include page="/WEB-INF/jsp/layout/sidebar.jsp" />

    <!-- CONTEÚDO PRINCIPAL -->
    <main id="main-content" class="nexacore-main">
        
        <!-- 2. INCLUI A TOPBAR MODULARIZADA -->
        <jsp:include page="/WEB-INF/jsp/layout/topbar.jsp" />

        <!-- DASHBOARD CONTAINER -->
        <div class="dashboard-container container-fluid p-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="fw-bold mb-1">Olá, <%= nomeUsuario %>! 👋</h2>
                        <p class="text-muted mb-0">Bem-vindo ao NexaCore. Aqui você tem uma visão geral do sistema e acesso rápido às funcionalidades.</p>
                    </div>
                    <div class="text-end text-muted small">
                        <div><i class="bi bi-calendar-event me-1"></i> Terça-feira, 19 de Agosto de 2026</div>
                        <div><i class="bi bi-clock me-1"></i> 10:45</div>
                    </div>
                </div>
            </div>

            <!-- CARDS DE ESTATÍSTICAS -->
			<div class="row g-3 mb-4">
			    <div class="col-md">
			        <div class="stat-card">
			            <div class="stat-icon bg-blue-light text-primary"><i class="bi bi-display"></i></div>
			            <div class="stat-info">
			                <span class="stat-label">Total de Equipamentos</span>
			                <h3 class="stat-value">${totalEquipamentos}</h3>
			                <a href="<%=ctx%>/ConsultaEquipamentosServlet" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
			            </div>
			        </div>
			    </div>
			    <div class="col-md">
			        <div class="stat-card">
			            <div class="stat-icon bg-green-light text-success"><i class="bi bi-check-circle"></i></div>
			            <div class="stat-info">
			                <span class="stat-label">Equipamentos Ativos</span>
			                <h3 class="stat-value">${totalAtivos}</h3>
			                <a href="<%=ctx%>/ConsultaEquipamentosServlet" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
			            </div>
			        </div>
			    </div>
               <div class="col-md">
				<div class="stat-card">
					<div class="stat-icon bg-yellow-light text-warning"><i class="bi bi-tools"></i></div>
						<div class="stat-info">
						<span class="stat-label">Em Manutenção</span>
						<h3 class="stat-value">${empty totalEmManutencao ? 0 : totalEmManutencao}</h3>
						<a href="<%=ctx%>/ConsultaChamadosServlet" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
						</div>
					</div>
				</div>
                <div class="col-md">
					<div class="stat-card">
						<div class="stat-icon bg-purple-light text-info"><i class="bi bi-truck"></i></div>
							<div class="stat-info">
							<span class="stat-label">Total Em Trânsito</span>
							<h3 class="stat-value">${empty totalEmTransito ? 0 : totalEmTransito}</h3>
							<a href="<%=ctx%>/ConsultaEnvioServlet" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md">
				    <div class="stat-card">
				        <div class="stat-icon bg-success-light text-success"><i class="bi bi-box-seam"></i></div>
				        <div class="stat-info">
				            <span class="stat-label">Recebidos Hoje</span>
				            <h3 class="stat-value">${empty totalRecebidosHoje ? 0 : totalRecebidosHoje}</h3>
				            <a href="<%=ctx%>/ConsultaEnvioServlet" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
				        </div>
				    </div>
				</div>
            </div>

            <!-- ACESSO RÁPIDO E AVISOS -->
            <div class="row g-4 mb-4">
                <div class="col-lg-8">
                    <div class="card-section p-4 h-100">
                        <h5 class="fw-bold mb-3">Acesso Rápido</h5>
                        <div class="row g-3">
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/CadastrarEquipamentoServlet" class="quick-action-card">
                                    <div class="quick-icon text-primary"><i class="bi bi-plus-circle"></i></div>
                                    <span>Novo Equipamento</span>
                                    <small>Cadastrar novo equipamento</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/EnvioEquipamentoServlet" class="quick-action-card">
                                    <div class="quick-icon text-success"><i class="bi bi-truck"></i></div>
                                    <span>Enviar Equipamentos</span>
                                    <small>Criar novo envio</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/RecebimentoServlet" class="quick-action-card">
                                    <div class="quick-icon text-info"><i class="bi bi-box-arrow-down"></i></div>
                                    <span>Receber Equipamentos</span>
                                    <small>Confirmar recebimento</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ManutencaoServlet" class="quick-action-card">
                                    <div class="quick-icon text-warning"><i class="bi bi-tools"></i></div>
                                    <span>Abrir Chamado</span>
                                    <small>Abrir chamado de manutenção</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ConsultaEquipamentosServlet" class="quick-action-card">
                                    <div class="quick-icon text-primary"><i class="bi bi-search"></i></div>
                                    <span>Consulta de Equipamentos</span>
                                    <small>Buscar equipamentos</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ConsultaChamadosServlet" class="quick-action-card">
                                    <div class="quick-icon text-danger"><i class="bi bi-file-earmark-text"></i></div>
                                    <span>Consulta de Chamados</span>
                                    <small>Ver chamados abertos</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ConsultaEnvioServlet" class="quick-action-card">
                                    <div class="quick-icon text-success"><i class="bi bi-arrow-left-right"></i></div>
                                    <span>Histórico de Movimentações</span>
                                    <small>Ver movimentações</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ConfiguracoesServlet" class="quick-action-card">
                                    <div class="quick-icon text-secondary"><i class="bi bi-bar-chart"></i></div>
                                    <span>Relatórios</span>
                                    <small>Acessar relatórios</small>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
					
				<!-- Avisos e Pendências Dinâmicos -->
				<div class="col-lg-4">
				    <div class="card-section p-4 h-100">
				        <div class="d-flex justify-content-between align-items-center mb-3">
				            <h5 class="fw-bold mb-0">Avisos e Pendências</h5>
				            <a href="#" class="small text-decoration-none">Ver todos</a>
				        </div>
				        <ul class="list-unstyled notification-list mb-0">
				            <li class="notification-item">
				                <i class="bi bi-tools text-warning"></i>
				                <div>
				                    <strong>${empty totalEquipamentosManutencao ? 0 : totalEquipamentosManutencao} equipamentos em manutenção</strong>
				                    <span>Aguardando atendimento ou finalização</span>
				                </div>
				            </li>
				            <li class="notification-item">
				                <i class="bi bi-truck text-info"></i>
				                <div>
				                    <strong>${empty totalAguardandoRecebimento ? 0 : totalAguardandoRecebimento} movimentações aguardando recebimento</strong>
				                    <span>Verifique e confirme os recebimentos pendentes</span>
				                </div>
				            </li>
				            <li class="notification-item">
				                <i class="bi bi-info-circle text-primary"></i>
				                <div>
				                    <strong>${empty totalEmTransito ? 0 : totalEmTransito} equipamentos em trânsito</strong>
				                    <span>Acompanhe os envios em andamento</span>
				                </div>
				            </li>
				            <li class="notification-item">
				                <i class="bi bi-calendar-x text-danger"></i>
				                <div>
				                    <strong>${empty totalChamadosVencidos ? 0 : totalChamadosVencidos} manutenções vencidas</strong>
				                    <span>Chamados com prazo ultrapassado</span>
				                </div>
				            </li>
				        </ul>
				    </div>
				</div>
            </div>

            <!-- TABELAS RECENTES -->
            <div class="row g-4 mb-4">
                <!-- Equipamentos em Manutenção -->
                <div class="col-lg-6">
                    <div class="card-section p-4 h-100">
                       <div class="d-flex justify-content-between align-items-center mb-3">
						   <h5 class="fw-bold mb-0">Equipamentos em Manutenção</h5>
						   <c:choose>
						       <c:when test="${canWriteManutencao}">
						           <a href="<%=ctx%>/ManutencaoServlet" class="small text-decoration-none">Ver todos</a>
						       </c:when>
						       <c:otherwise>
						           <a href="<%=ctx%>/MenuServlet?erro=sem_permissao&modulo=manutencao_chamados" class="small text-decoration-none">Ver todos</a>
						       </c:otherwise>
						   </c:choose>
						</div>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0 small">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID Equip.</th>
                                        <th>Patrimônio</th>
                                        <th>Equipamento / Modelo</th>
                                        <th>Situação</th>
                                        <th class="text-center">Ação</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty listaEquipamentosEmManutencao}">
                                            <c:forEach var="equip" items="${listaEquipamentosEmManutencao}">
                                                <tr>
                                                    <td><strong class="text-danger">EQ-${equip.idEquipamento}</strong></td>
                                                    <td><span class="text-truncate-custom" title="${equip.patrimonio}"> ${empty equip.patrimonio ? '-' : equip.patrimonio}</span></td>
                                                    <td><span class="text-truncate-custom" title="${equip.nomeProduto}">${equip.nomeProduto}</span></td>
                                                    <td><span class="badge bg-danger-subtle text-danger px-2 py-1">Pendente Chamado</span></td>
                                                    <td class="text-center">
                                                        <a href="<%=ctx%>/ManutencaoServlet" class="btn btn-sm btn-outline-danger" title="Abrir Chamado Agora">
                                                            <i class="bi bi-tools"></i>
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" class="text-center text-muted py-3">Nenhum equipamento pendente de abertura de chamado.</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Chamados Abertos -->
                <div class="col-lg-6"> 
                    <div class="card-section p-4 h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="fw-bold mb-0">Chamados Abertos</h5>
                            <a href="<%=ctx%>/ConsultaChamadosServlet" class="small text-decoration-none">Ver todos</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0 small">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID Chamado</th>
                                        <th>Status</th>
                                        <th class="text-center">Visualizar</th>
                                        <th>Equipamento</th>
                                        <th>Problema</th>
                                        <th>Abertura</th>                 
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty listaChamadosRecentes}">
                                            <c:forEach var="chamado" items="${listaChamadosRecentes}">
                                                <tr>
                                                    <td><strong>MAN-${chamado.idChamado}</strong></td>
                                                    <td>
                                                       <c:choose>
                                                            <c:when test="${fn:contains(fn:toLowerCase(chamado.nomeStatus), 'análise') or fn:contains(fn:toLowerCase(chamado.nomeStatus), 'aberto')}">
                                                                <span class="badge bg-warning-subtle text-warning">${chamado.nomeStatus}</span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(fn:toLowerCase(chamado.nomeStatus), 'atendimento') or fn:contains(fn:toLowerCase(chamado.nomeStatus), 'andamento')}">
                                                                <span class="badge bg-primary-subtle text-primary">${chamado.nomeStatus}</span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(fn:toLowerCase(chamado.nomeStatus), 'aguardando')}">
                                                                <span class="badge bg-info-subtle text-info">${chamado.nomeStatus}</span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(fn:toLowerCase(chamado.nomeStatus), 'concluído') or fn:contains(fn:toLowerCase(chamado.nomeStatus), 'finalizado')}">
                                                                <span class="badge bg-success-subtle text-success">${chamado.nomeStatus}</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-secondary-subtle text-secondary">${chamado.nomeStatus}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-center">
                                                        <button type="button" class="btn btn-sm btn-outline-primary" onclick="abrirModalDetalhesChamado(${chamado.idChamado})" title="Ver Detalhes">
                                                            <i class="bi bi-eye"></i>
                                                        </button>
                                                    </td>
                                                    <td><span class="text-truncate-custom" title="${chamado.nomeEquipamento}">${chamado.nomeEquipamento}</span></td>                                         			     
                                                    <td><span class="text-truncate-custom" title="${chamado.tipoProblema}">${chamado.tipoProblema}</span></td>                                            
                                                    <td class="data-formatavel">${chamado.dataAbertura}</td>                      
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="6" class="text-center text-muted py-3">Nenhum chamado aberto no momento.</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- SEGUNDA LINHA DE TABELAS RECENTES -->
            <div class="row g-4">
                <!-- Movimentações Recentes -->
				<div class="col-12">
				    <div class="card-section p-4 h-100">
				        <div class="d-flex justify-content-between align-items-center mb-3">
				            <h5 class="fw-bold mb-0">Movimentações Recentes</h5>
				            <a href="<%=ctx%>/ConsultaEnvioServlet" class="small text-decoration-none">Ver todos</a>
				        </div>
				        <div class="table-responsive">
				            <table class="table table-hover align-middle mb-0 small">
				                <thead class="table-light">
				                    <tr>
				                        <th>ID</th>
				                        <th>Data</th>
				                        <th>Origem</th>
				                        <th>Destino</th>
				                        <th>Status</th>
				                    </tr>
				                </thead>
				                <tbody>
				                    <c:choose>
				                        <c:when test="${not empty listaMovimentacoesRecentes}">
				                            <c:forEach var="mov" items="${listaMovimentacoesRecentes}">
				                                <tr>
				                                    <td><strong>ENV-${mov.idEnvio}</strong></td>
				                                    <td>${mov.dataEnvio}</td>
				                                    <td>${mov.nomeOrigem}</td>
				                                    <td>${mov.nomeDestino}</td>
				                                    <td>
				                                        <span class="badge" style="background-color: ${mov.statusCor}; color: #fff;">
				                                            ${mov.statusNome}
				                                        </span>
				                                    </td>
				                                </tr>
				                            </c:forEach>
				                        </c:when>
				                        <c:otherwise>
				                            <tr>
				                                <td colspan="5" class="text-center text-muted py-3">Nenhuma movimentação em trânsito ou aguardando.</td>
				                            </tr>
				                        </c:otherwise>
				                    </c:choose>
				                </tbody>
				            </table>
				        </div>
				    </div>
				</div>            
            </div>

        </div>

        <!-- 3. INCLUI O RODAPÉ MODULARIZADO -->
        <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
    </main>
	
    <!-- MODAL DE GERENCIAR/VISUALIZAR CHAMADO -->
    <div class="modal fade" id="modalGerenciarChamado" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title"><i class="bi bi-tools me-2"></i> Gerenciar Chamado de Manutenção</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fechar"></button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <strong>Equipamento:</strong>
                            <p id="modalEquipamento" class="text-muted mb-0">-</p>
                        </div>
                        <div class="col-md-4">
                            <strong>Solicitante:</strong>
                            <p id="modalSolicitante" class="text-muted mb-0">-</p>
                        </div>
                        <div class="col-md-4">
                            <strong>Data Abertura:</strong>
                            <p id="modalDataAbertura" class="text-muted mb-0">-</p>
                        </div>
                    </div>
                    <div class="mb-3">
                        <strong>Descrição do Problema Relatado:</strong>
                        <textarea id="modalDescricao" class="form-control bg-light" rows="2" readonly></textarea>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label"><strong>Status Atual do Chamado *</strong></label>
                            <input type="text" id="modalStatus" class="form-control" readonly />
                        </div>
                        <div class="col-md-6">
                            <label class="form-label"><strong>Técnico Responsável</strong></label>
                            <input type="text" id="modalTecnico" class="form-control" readonly />
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label"><strong>Diagnóstico Técnico</strong></label>
                            <textarea id="modalDiagnostico" class="form-control" rows="3" readonly></textarea>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label"><strong>Solução Realizada</strong></label>
                            <textarea id="modalSolucao" class="form-control" rows="3" readonly></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>
                </div>
            </div>
        </div>
    </div>

    <!-- MODAL DE ALERTA GLOBAL -->
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

    <!-- SCRIPTS -->
    <script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="<%=ctx%>/assets/js/menu.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
    
</body>
</html>