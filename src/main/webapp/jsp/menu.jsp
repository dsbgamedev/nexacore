<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Usuario" %>
<%
    String ctx = request.getContextPath();
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
    String nomeUsuario = (usuario != null) ? usuario.getUsername() : "Usuário";
    String unidadeAtiva = (usuario != null && usuario.getUnidadeAtivaNome() != null) ? usuario.getUnidadeAtivaNome() : "Filial Padrão";
%>
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
<body class="nexacore-layout">

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
                <li><a href="<%=ctx%>/jsp/menu.jsp" class="active"><i class="bi bi-speedometer2"></i> Dashboard</a></li>
            </ul>

            <span class="menu-category">EQUIPAMENTOS</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/ConsultarEquipamentosServlet"><i class="bi bi-cpu"></i> Consulta de Equipamentos</a></li>
                <li><a href="<%=ctx%>/CadastrarEquipamentoServlet"><i class="bi bi-plus-circle"></i> Novo Equipamento</a></li>
                <li><a href="<%=ctx%>/CatalogoProdutosServlet"><i class="bi bi-box-seam"></i> Produtos (Catálogo)</a></li>
            </ul>

            <span class="menu-category">MOVIMENTAÇÕES</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/NovoEnvioServlet"><i class="bi bi-arrow-up-right-circle"></i> Envios</a></li>
                <li><a href="<%=ctx%>/RecebimentoServlet"><i class="bi bi-arrow-down-left-circle"></i> Recebimentos</a></li>
                <li><a href="<%=ctx%>/TransferenciaServlet"><i class="bi bi-arrow-left-right"></i> Transferências</a></li>
                <li><a href="<%=ctx%>/DevolucaoServlet"><i class="bi bi-arrow-counterclockwise"></i> Devoluções</a></li>
                <li><a href="<%=ctx%>/HistoricoMovimentacoesServlet"><i class="bi bi-clock-history"></i> Histórico de Movimentações</a></li>
            </ul>

            <span class="menu-category">MANUTENÇÕES</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/AbrirChamadoServlet"><i class="bi bi-tools"></i> Abrir Chamado</a></li>
                <li><a href="<%=ctx%>/ConsultaChamadosServlet"><i class="bi bi-list-check"></i> Consulta de Chamados</a></li>
                <li><a href="<%=ctx%>/HistoricoManutencoesServlet"><i class="bi bi-journal-medical"></i> Histórico de Manutenções</a></li>
            </ul>

            <span class="menu-category">CADASTROS</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/FabricantesServlet"><i class="bi bi-building"></i> Fabricantes</a></li>
                <li><a href="<%=ctx%>/MarcasServlet"><i class="bi bi-tag"></i> Marcas</a></li>
                <li><a href="<%=ctx%>/TiposProdutoServlet"><i class="bi bi-grid"></i> Tipos de Produto</a></li>
                <li><a href="<%=ctx%>/AtributosServlet"><i class="bi bi-list-nested"></i> Atributos</a></li>
                <!-- Link direto para a nossa tela de cadastro de usuários recém-revisada! -->
                <li><a href="<%=ctx%>/CadastrarUsuarioServlet"><i class="bi bi-people"></i> Usuários</a></li>
            </ul>

            <span class="menu-category">CONFIGURAÇÕES</span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/ConfiguracoesServlet"><i class="bi bi-gear"></i> Configurações</a></li>
            </ul>
        </div>

        <!-- USER FOOTER NA SIDEBAR -->
        <div class="sidebar-user-footer">
            <div class="user-avatar"><%= nomeUsuario.substring(0, 2).toUpperCase() %></div>
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
                <div class="topbar-branch-selector">
                    <i class="bi bi-building-check"></i>
                    <span>Filial Atual: <strong><%= unidadeAtiva %></strong></span>
                    <i class="bi bi-chevron-down ms-1"></i>
                </div>
            </div>
        </header>

        <!-- DASHBOARD CONTAINER -->
        <div class="dashboard-container container-fluid p-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="fw-bold mb-1">Olá, <%= nomeUsuario %>! 👋</h2>
                        <p class="text-muted mb-0">Bem-vinda ao NexaCore. Aqui você tem uma visão geral do sistema e acesso rápido às funcionalidades.</p>
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
                <!-- Acesso Rápido (8 opções) -->
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
                                <a href="<%=ctx%>/NovoEnvioServlet" class="quick-action-card">
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
                                <a href="<%=ctx%>/AbrirChamadoServlet" class="quick-action-card">
                                    <div class="quick-icon text-warning"><i class="bi bi-tools"></i></div>
                                    <span>Abrir Chamado</span>
                                    <small>Abrir chamado de manutenção</small>
                                </a>
                            </div>
                            <div class="col-md-3 col-6">
                                <a href="<%=ctx%>/ConsultarEquipamentosServlet" class="quick-action-card">
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

                <!-- Avisos e Pendências Completo -->
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

            <!-- TABELAS DE MOVIMENTAÇÕES E CHAMADOS RECENTES -->
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

            <!-- RODAPÉ DA PÁGINA -->
            <footer class="mt-5 text-center text-muted small pb-3 d-flex justify-content-between border-top pt-3">
                <span>NexaCore v1.0.0</span>
                <span>© 2026 NexaCore. Todos os direitos reservados.</span>
                <span>Suporte: <a href="mailto:suporte@nexacore.com.br">suporte@nexacore.com.br</a></span>
            </footer>
        </div>

    <!-- Scripts Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>