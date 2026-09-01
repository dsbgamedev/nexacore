<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="model.Usuario" %>
<%
    String ctx = request.getContextPath();
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
    String nomeUsuario = (usuario != null) ? usuario.getUsername() : "Usuário";
    String unidadeAtiva = (usuario != null && usuario.getUnidadeAtivaNome() != null) ? usuario.getUnidadeAtivaNome() : "Filial Padrão";
%>
<%--
    Verifica se o usuário está logado. Se não, redireciona para a página de login.
    Esta lógica é crucial para a segurança e é feita antes de qualquer renderização do corpo da página.
--%>
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
    
    <!-- CSS Globais e do Layout (Apenas os corretos para o painel) -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/menu.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/global.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/layout.css">
</head>
<body class="nexacore-layout" data-app-context-path="<%=ctx%>" data-session-message="${sessionScope.mensagemSessao}">
<div id="inlineMessageContainer"></div>

    <%-- CONFIGURAÇÃO DE VARIÁVEIS DE PERMISSÃO ROBUSTA --%>
    <c:set var="perfilLogado" value="${fn:toLowerCase(sessionScope.usuarioLogado.perfil)}" />
    <c:set var="isSuperAdmin" value="${fn:contains(perfilLogado, 'super')}" />
    <c:set var="isAdmin" value="${fn:contains(perfilLogado, 'admin')}" />
    <c:set var="hasFullAccess" value="${isSuperAdmin or isAdmin}" />
    <%-- Mantém como objeto de coleção/lista vindo da sessão --%>
    <c:set var="listaModulos" value="${sessionScope.usuarioLogado.modulosPermitidos}" />
    
    <%-- Processamento seguro da lista de módulos com delimitadores de vírgula --%>
    <% 
        model.Usuario userLogado = (model.Usuario) session.getAttribute("usuarioLogado");
        java.util.List<String> listaMod = (userLogado != null) ? userLogado.getModulosPermitidos() : null;
        String modulosStr = "";
        if (listaMod != null && !listaMod.isEmpty()) {
            StringBuilder sb = new StringBuilder(",");
            for (String m : listaMod) {
                if (m != null) {
                    sb.append(m.toLowerCase().trim()).append(",");
                }
            }
            modulosStr = sb.toString();
        }
        request.setAttribute("modulosStr", modulosStr);
    %>

   <%-- Controle granular flexível --%>
    <%-- Controle granular flexível (ajustado para corresponder exatamente às chaves) --%>
    <c:set var="canSeeEquipamentos" value="${hasFullAccess or fn:contains(modulosStr, ',equipamento,') or fn:contains(modulosStr, ',equipamentos,')}" />
    <c:set var="canSeeProdutos" value="${hasFullAccess or fn:contains(modulosStr, ',produto,') or fn:contains(modulosStr, ',produtos,')}" />
    <c:set var="canSeeMovimentacao" value="${hasFullAccess or fn:contains(modulosStr, ',movimentacao,')}" />
    <c:set var="canSeeManutencao" value="${hasFullAccess or fn:contains(modulosStr, ',manutencao,') or fn:contains(modulosStr, ',manutencao_chamados,')}" />
    <c:set var="canSeeFabricantes" value="${hasFullAccess or fn:contains(modulosStr, ',fabricante,') or fn:contains(modulosStr, ',fabricantes,')}" />
    <c:set var="canSeeMarcas" value="${hasFullAccess or fn:contains(modulosStr, ',marca,') or fn:contains(modulosStr, ',marcas,')}" />
    <c:set var="canSeeEmpresas" value="${hasFullAccess or fn:contains(modulosStr, ',empresa,') or fn:contains(modulosStr, ',filial,') or fn:contains(modulosStr, ',filiais,')}" />
    <c:set var="canSeeAtributos" value="${hasFullAccess or fn:contains(modulosStr, ',atributo,') or fn:contains(modulosStr, ',atributos,')}" />
    <c:set var="canSeeUsuarios" value="${hasFullAccess or fn:contains(modulosStr, ',usuario,') or fn:contains(modulosStr, ',usuarios,')}" />
     <c:set var="canSeeGerenciarUsuarios" value="${hasFullAccess or fn:contains(modulosStr, ',usuario,') or fn:contains(modulosStr, ',usuarios,')}" />
    <!-- SIDEBAR LATERAL ESQUERDA -->
    <aside id="sidebar" class="nexacore-sidebar">
        <div class="sidebar-brand">
            <div class="logo-symbol">N</div>
            <div class="logo-info">
                <span class="logo-name">NEXACORE</span>
                <span class="logo-sub">Sistema de Gestão</span>
            </div>
        </div>

        <div class="sidebar-menu-container">
            <span class="menu-category">PRINCIPAL</span>
            <ul class="sidebar-menu">
                <%-- Alterado de /jsp/menu.jsp para o Servlet correspondente --%>
                <li><a href="<%=ctx%>/MenuServlet" class="active"><i class="bi bi-speedometer2"></i> Dashboard</a></li>
            </ul>

            <%-- EQUIPAMENTOS --%>
            <c:if test="${canSeeEquipamentos or canSeeProdutos}">
                <span class="menu-category">EQUIPAMENTOS</span>
                <ul class="sidebar-menu">
                	<c:if test="${canSeeEquipamentos}">
                    <li><a href="<%=ctx%>/ConsultaEquipamentosServlet"><i class="bi bi-cpu"></i> Consulta de Equipamentos</a></li>
                    <li><a href="<%=ctx%>/CadastrarEquipamentoServlet"><i class="bi bi-plus-circle"></i> Novo Equipamento</a></li>
                     </c:if>
                    <c:if test="${canSeeProdutos}">
                    <%-- Substitua ProdutoServlet pelo servlet correto de produtos, ex: CadastrarProdutoServlet --%>
                    <li><a href="<%=ctx%>/ProdutoServlet"><i class="bi bi-box-seam"></i> Produtos (Catálogo)</a></li>                  
                   </c:if>
                </ul>
            </c:if>

            <%-- MOVIMENTAÇÕES --%>
            <c:if test="${canSeeMovimentacao}">
                <span class="menu-category">MOVIMENTAÇÕES</span>
                <ul class="sidebar-menu">
                <c:if test="${canSeeMovimentacao}">
                    <li><a href="<%=ctx%>/EnvioEquipamentoServlet"><i class="bi bi-arrow-up-right-circle"></i> Envios</a></li>
                    <li><a href="<%=ctx%>/ConsultaEnvioServlet"><i class="bi bi-search"></i> Consulta de Envios</a></li>
                    <li><a href="<%=ctx%>/RecebimentoServlet"><i class="bi bi-arrow-down-left-circle"></i> Recebimentos</a></li>
                    <li><a href="<%=ctx%>/DevolucaoServlet"><i class="bi bi-arrow-counterclockwise"></i> Devoluções</a></li>  
                  </c:if>        
                </ul>
            </c:if>

            <%-- MANUTENÇÕES --%>
            <c:if test="${canSeeManutencao}">
                <span class="menu-category">MANUTENÇÕES</span>
                <ul class="sidebar-menu">
                	<c:if test="${canSeeManutencao}">
                    <li><a href="<%=ctx%>/ManutencaoServlet"><i class="bi bi-tools"></i> Abrir Chamado</a></li>
                    <li><a href="<%=ctx%>/ConsultaChamadosServlet"><i class="bi bi-list-check"></i> Consulta de Chamados</a></li>
                    </c:if>
                </ul>
            </c:if>

            <%-- CADASTROS E GERENCIAMENTOS--%>
            <c:if test="${canSeeFabricantes or canSeeMarcas or canSeeEmpresas or canSeeAtributos or canSeeUsuarios or canSeeGerenciarUsuarios}">
                <span class="menu-category">CADASTROS</span>
                <ul class="sidebar-menu">
                    <c:if test="${canSeeFabricantes}">
                        <li><a href="<%=ctx%>/FabricanteServlet"><i class="bi bi-building"></i> Fabricantes</a></li>
                    </c:if>
                    <c:if test="${canSeeMarcas}">
                        <li><a href="<%=ctx%>/MarcaServlet"><i class="bi bi-tag"></i> Marcas</a></li>
                    </c:if>
                    <c:if test="${canSeeEmpresas}">
                        <li><a href="<%=ctx%>/CadastrarEmpresaServlet"><i class="bi bi-grid"></i> Empresas</a></li>
                    </c:if>
                    <c:if test="${canSeeAtributos}">
                        <li><a href="<%=ctx%>/AtributosServlet"><i class="bi bi-list-nested"></i> Atributos</a></li>
                    </c:if>
                    <c:if test="${canSeeUsuarios}">
                        <li><a href="<%=ctx%>/CadastrarUsuarioServlet"><i class="bi bi-people"></i> Usuários</a></li>
                    </c:if>
                    <c:if test="${canSeeGerenciarUsuarios}">
                        <li><a href="<%=ctx%>/GerenciarUsuariosServlet"><i class="bi bi-people"></i>Gerenciar Usuários</a></li>
                    </c:if>
                </ul>
            </c:if>

            <span class="menu-category">CONFIGURAÇÕES</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/ConfiguracoesServlet"><i class="bi bi-gear"></i> Configurações</a></li>
            </ul>
        </div>

        <!-- USER FOOTER NA SIDEBAR -->
        <div class="sidebar-user-footer">
            <div class="user-avatar"><%= nomeUsuario.substring(0, Math.min(nomeUsuario.length(), 2)).toUpperCase() %></div>
            <div class="user-details">
                <span class="user-name"><%= nomeUsuario %></span>
                <span class="user-branch">Filial: <%= unidadeAtiva %></span>
            </div>
            <a href="<%=ctx%>/LogoutServlet" class="user-logout-icon" title="Sair"><i class="bi bi-box-arrow-right"></i></a>
        </div>
    </aside>

    <!-- CONTEÚDO PRINCIPAL -->
    <main id="main-content" class="nexacore-main">
        <!-- TOPBAR -->
        <header class="nexacore-topbar">
            <button id="sidebarToggle" class="btn-toggle"><i class="bi bi-list"></i></button>
            <div class="topbar-right">
                <div class="topbar-icon-badge" title="Notificações">
                    <i class="bi bi-bell"></i>
                    <span class="badge-count">3</span>
                </div>
                <div class="topbar-icon-badge" title="Ajuda">
                    <i class="bi bi-question-circle"></i>
                </div>
                <div class="topbar-branch-selector d-flex align-items-center">
                    <i class="bi bi-building-check me-1"></i>
                    <span class="me-2">Filial Atual:</span>
                    <select id="selectUnidadeAtiva" class="form-select form-select-sm w-auto d-inline-block">
                        <c:forEach var="unidade" items="${sessionScope.usuarioLogado.unidadesPermitidasObjetos}">
                            <option value="${unidade.id}" ${unidade.id == sessionScope.usuarioLogado.unidadeAtivaId ? 'selected' : ''}>
                                ${unidade.nome}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </header>

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
                            <h3 class="stat-value">1.248</h3>
                            <a href="#" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md">
                    <div class="stat-card">
                        <div class="stat-icon bg-green-light text-success"><i class="bi bi-check-circle"></i></div>
                        <div class="stat-info">
                            <span class="stat-label">Equipamentos Ativos</span>
                            <h3 class="stat-value">1.062</h3>
                            <a href="#" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md">
                    <div class="stat-card">
                        <div class="stat-icon bg-yellow-light text-warning"><i class="bi bi-tools"></i></div>
                        <div class="stat-info">
                            <span class="stat-label">Em Manutenção</span>
                            <h3 class="stat-value">28</h3>
                            <a href="#" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md">
                    <div class="stat-card">
                        <div class="stat-icon bg-purple-light text-info"><i class="bi bi-truck"></i></div>
                        <div class="stat-info">
                            <span class="stat-label">Em Trânsito</span>
                            <h3 class="stat-value">14</h3>
                            <a href="#" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
                <div class="col-md">
                    <div class="stat-card">
                        <div class="stat-icon bg-red-light text-danger"><i class="bi bi-box-arrow-in-down"></i></div>
                        <div class="stat-info">
                            <span class="stat-label">Aguardando Recebimento</span>
                            <h3 class="stat-value">9</h3>
                            <a href="#" class="stat-link">Ver detalhes <i class="bi bi-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ACESSO RÁPIDO E AVISOS -->
            <div class="row g-4 mb-4">
                <!-- Acesso Rápido -->
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
                                <a href="<%=ctx%>/MovimentacaoRecebimentoServlet" class="quick-action-card">
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
                                <a href="<%=ctx%>/HistoricoMovimentacoesServlet" class="quick-action-card">
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

                <!-- Avisos e Pendências -->
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
                                    <strong>28 equipamentos em manutenção</strong>
                                    <span>Aguardando atendimento ou finalização</span>
                                </div>
                            </li>
                            <li class="notification-item">
                                <i class="bi bi-truck text-info"></i>
                                <div>
                                    <strong>9 movimentações aguardando recebimento</strong>
                                    <span>Verifique e confirme os recebimentos pendentes</span>
                                </div>
                            </li>
                            <li class="notification-item">
                                <i class="bi bi-info-circle text-primary"></i>
                                <div>
                                    <strong>14 equipamentos em trânsito</strong>
                                    <span>Acompanhe os envios em andamento</span>
                                </div>
                            </li>
                            <li class="notification-item">
                                <i class="bi bi-calendar-x text-danger"></i>
                                <div>
                                    <strong>3 manutenções vencidas</strong>
                                    <span>Chamados com prazo ultrapassado</span>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- TABELAS RECENTES -->
            <div class="row g-4">
                <!-- Movimentações Recentes -->
                <div class="col-lg-6">
                    <div class="card-section p-4 h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="fw-bold mb-0">Movimentações Recentes</h5>
                            <a href="<%=ctx%>/HistoricoMovimentacoesServlet" class="small text-decoration-none">Ver todos</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0 small">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID Movimento</th>
                                        <th>Tipo</th>
                                        <th>Origem</th>
                                        <th>Destino</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><strong>ENV-000124</strong></td>
                                        <td>Envio</td>
                                        <td>CBA Diesel SP</td>
                                        <td>CBA Diesel GO</td>
                                        <td><span class="badge bg-primary-subtle text-primary">Em Trânsito</span></td>
                                    </tr>
                                    <tr>
                                        <td><strong>DEV-000045</strong></td>
                                        <td>Devolução</td>
                                        <td>CBA Diesel GO</td>
                                        <td>CBA Diesel SP</td>
                                        <td><span class="badge bg-primary-subtle text-primary">Em Trânsito</span></td>
                                    </tr>
                                    <tr>
                                        <td><strong>REC-000078</strong></td>
                                        <td>Recebimento</td>
                                        <td>CBA Diesel GO</td>
                                        <td>CBA Diesel SP</td>
                                        <td><span class="badge bg-success-subtle text-success">Recebido</span></td>
                                    </tr>
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
                                        <th>Equipamento</th>
                                        <th>Problema</th>
                                        <th>Abertura</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><strong>MAN-000056</strong></td>
                                        <td>EQ00000003</td>
                                        <td>Falha na impressão</td>
                                        <td>19/08/2026</td>
                                        <td><span class="badge bg-warning-subtle text-warning">Em Atendimento</span></td>
                                    </tr>
                                    <tr>
                                        <td><strong>MAN-000055</strong></td>
                                        <td>EQ00000007</td>
                                        <td>Não liga</td>
                                        <td>18/08/2026</td>
                                        <td><span class="badge bg-info-subtle text-info">Aguardando Peça</span></td>
                                    </tr>
                                    <tr>
                                        <td><strong>MAN-000054</strong></td>
                                        <td>EQ00000012</td>
                                        <td>Ruído excessivo</td>
                                        <td>18/08/2026</td>
                                        <td><span class="badge bg-warning-subtle text-warning">Em Atendimento</span></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- ==========================================================
         	NEXACORE - MODAL SERVICE (Modais Globais)
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

            <!-- RODAPÉ -->
            <footer class="mt-5 text-center text-muted small pb-3 d-flex justify-content-between border-top pt-3">
                <span>NexaCore v1.0.0</span>
                <span>© 2026 NexaCore. Todos os direitos reservados.</span>
                <span>Suporte: <a href="mailto:suporte@nexacore.com.br">suporte@nexacore.com.br</a></span>
            </footer>
        </div>
    </main>

    <!-- Scripts Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <!-- Script Externo -->
    <script src="<%=ctx%>/assets/js/menu.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('erro') === 'sem_permissao') {
                if (typeof ModalService !== 'undefined') {
                    ModalService.error(
                        "Acesso Negado", 
                        "Você não possui permissão para acessar o cadastro de equipamentos."
                    ).then(() => {
                        const novaUrl = window.location.pathname;
                        window.history.replaceState({}, document.title, novaUrl);
                    });
                }
            }
        });
    </script>
</body>
</html>