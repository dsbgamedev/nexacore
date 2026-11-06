<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nexacore | Consulta de Equipamentos</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="p-4 bg-light">

<div class="container-fluid py-4" style="max-width: 1300px; margin: auto;">
    
    <!-- Cabeçalho Padrão com Tema Escuro Unificado -->
    <div class="card-cabecalho-padrao tema-escuro">
        <!-- Linha Superior: Título e Breadcrumb -->
        <div class="mb-3">
            <h4 class="page-title fw-bold text-white mb-1">CONSULTA DE EQUIPAMENTOS (UNIDADE FÍSICA)</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0" style="font-size: 0.85rem;">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet" class="text-light text-decoration-none">Home</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/CadastrarEquipamentoServlet" class="text-light text-decoration-none">Equipamentos</a></li>
                    <li class="breadcrumb-item active text-secondary" aria-current="page">Consulta de Equipamentos</li>
                </ol>
            </nav>
        </div>
           
        <!-- Linha Inferior: Grupo de Botões de Atalho Modernos -->
        <div class="cabecalho-botoes-atalho">
            <a href="${pageContext.request.contextPath}/CadastrarEquipamentoServlet" class="btn btn-success btn-moderno px-3 py-2 text-white">
                <i class="fa fa-plus me-1"></i> Novo Equipamento
            </a>
            <a href="${pageContext.request.contextPath}/EnvioEquipamentoServlet" class="btn btn-primary btn-moderno px-3 py-2 text-white">
		        <i class="fas fa-external-link-alt me-1"></i> Envio Equipamentos
		    </a>
		    <!-- Atalho para Devolução -->
		    <a href="${pageContext.request.contextPath}/DevolucaoEquipamentoServlet" class="btn btn-warning btn-moderno px-3 py-2 text-dark fw-semibold">
		        <i class="fas fa-undo me-1"></i> Envio Equip. Devolução
		    </a>
		    <!-- Atalho para Recebimento -->
		    <a href="${pageContext.request.contextPath}/RecebimentoServlet" class="btn btn-info btn-moderno px-3 py-2 text-white">
		        <i class="fas fa-box-open me-1"></i> Recebimento / Devolução
		    </a>
		     <a href="${pageContext.request.contextPath}/ConsultaEnvioServlet" class="btn btn-dark border border-secondary px-3 py-2 text-white">
		        <i class="fas fa-search me-1"></i> Consulta Envio
		    </a>
        </div>
    </div>

    <!-- Área de Filtros de Pesquisa -->
    <div class="card p-4 mb-4 shadow-sm">
        <div class="d-flex align-items-center mb-3 text-primary">
            <i class="fa fa-filter me-2"></i>
            <h5 class="mb-0 fw-bold">FILTROS DE PESQUISA</h5>
        </div>
        
        <form id="formFiltroEquipamento">
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Produto (Catálogo)</label>
                    <input type="text" class="form-control form-control-sm" id="filtroProduto" placeholder="Selecione o produto...">
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold small">ID Sistema</label>
                    <input type="text" class="form-control form-control-sm" id="filtroIdSistema" placeholder="Ex: EQ000154">
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Patrimônio / Etiqueta</label>
                    <input type="text" class="form-control form-control-sm" id="filtroPatrimonio" placeholder="Digite patrimônio...">
                </div>
                <div class="col-md-4">
                    <label class="form-label fw-bold small">Número de Série</label>
                    <input type="text" class="form-control form-control-sm" id="filtroSerial" placeholder="Digite o número de série...">
                </div>
                
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Origem (Filial)</label>
                    <select class="form-select form-select-sm" id="filtroOrigem">
                        <option value="">Selecione...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Departamento / Setor</label>
                    <select class="form-select form-select-sm" id="filtroDepartamento">
                        <option value="">Todos os setores...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Usuário Atual</label>
                    <input type="text" class="form-control form-control-sm" id="filtroUsuario" placeholder="Nome do usuário...">
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Status</label>
                    <select class="form-select form-select-sm" id="filtroStatus">
                        <option value="">Selecione...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Situação</label>
                    <select class="form-select form-select-sm" id="filtroSituacao">
                        <option value="">Selecione...</option>
                    </select>
                </div>
                <div class="col-md-3">
				    <label class="form-label fw-bold small">Tipo de Produto</label>
				    <select class="form-select form-select-sm" id="filtroTipoProduto">
				        <option value="">Todos os tipos...</option>
				    </select>
				</div>
                <div class="col-12 d-flex justify-content-between align-items-center mt-3 pt-3 border-top px-0">
				    <!-- Barra de Pesquisa Global alinhada com a margem dos campos de cima -->
				    <div class="input-group search-container" style="max-width: 700px;">
				        <input type="text" id="busca-global" class="form-control form-control-sm" placeholder="Pesquisar no sistema...">
				        <button id="btn-busca-global" class="btn btn-outline-secondary btn-sm" type="button">
				            <i class="fas fa-search"></i>
				        </button>
				    </div>
				
				    <!-- Botões de Ação (Direita) -->
				    <div class="d-flex gap-2">
				        <button type="button" class="btn btn-outline-secondary btn-sm" onclick="limparFiltros()">
				            <i class="fa fa-sync-alt me-1"></i> Limpar Filtros
				        </button>
				        <button type="submit" class="btn btn-primary btn-sm px-4" onclick="pesquisarEquipamentos(); return false;">
				            <i class="fa fa-search me-1"></i> Pesquisar
				        </button>
				    </div>
				</div>
            </div>
        </form>
    </div>

    <!-- Tabela de Resultados -->
    <div class="card p-4 shadow-sm">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0 fw-bold text-primary-dark">
                <i class="fa fa-list me-2"></i> EQUIPAMENTOS CADASTRADOS 
                <span class="badge bg-light text-primary border ms-2" id="totalRegistros" style="font-size: 0.75rem;">0 registros encontrados</span>
            </h5>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="font-size: 0.85rem;">
                <thead class="table-light text-secondary">
                    <tr>
                        <th class="sortable" onclick="ordenarTabela('idSistema')">ID Sistema <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('patrimonio')">Patrimônio / Etiqueta <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('produtoNome')">Produto (Catálogo) <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('nome')">Nome / CPU <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('origem')">Origem (Filial) <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('usuarioAtual')">Usuário Atual <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('departamento')">Departamento <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('status')">Status <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('situacao')">Situação <i class="fas fa-sort text-muted small"></i></th>
                        <th class="sortable" onclick="ordenarTabela('numeroSerie')">Nº de Série <i class="fas fa-sort text-muted small"></i></th>
                        <th class="text-center" style="width: 140px;">Ações</th>
                    </tr>
                </thead>
                <tbody id="tabelaEquipamentosBody">
                    <tr>
                        <td colspan="11" class="text-center text-muted py-4">Utilize os filtros acima para consultar os equipamentos.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Rodapé da Paginação Integrado ao Layout.css -->
        <div class="card-footer d-flex justify-content-between align-items-center bg-white border-top">
            <div class="text-muted small" id="infoPaginacao">
                Mostrando 0 registros
            </div>
            <nav>
                <ul class="pagination pagination-sm mb-0" id="paginacaoContainer">
                    <li class="page-item" id="btn-pagina-anterior">
                        <a class="page-link" href="#" onclick="mudarPagina(-1); return false;">Anterior</a>
                    </li>
                    <li class="page-item active">
                        <span class="page-link" id="span-pagina-atual">1</span>
                    </li>
                    <li class="page-item" id="btn-pagina-proximo">
                        <a class="page-link" href="#" onclick="mudarPagina(1); return false;">Próximo</a>
                    </li>
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

<!-- Modal de Detalhes Completo do Equipamento e Produto -->
<div class="modal fade" id="modalDetalhesEquipamento" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title"><i class="fas fa-info-circle me-2"></i>Dossiê do Equipamento</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <!-- Coluna da Esquerda -->
                    <div class="col-md-5 border-end text-center">
                        <h6 class="text-secondary fw-bold mb-3">Produto (Catálogo)</h6>
                        <div class="mb-3" id="det-container-img" style="display: none;">
                            <img id="det-img-produto" src="" alt="Produto" class="img-fluid rounded border p-1" style="max-height: 150px; object-fit: contain;">
                        </div>
                        <div class="text-start small bg-light p-3 rounded">
                            <p class="mb-1"><strong>SKU:</strong> <span id="det-prod-sku">-</span></p>
                            <p class="mb-1"><strong>Tipo:</strong> <span id="det-prod-tipo">-</span></p>
                            <p class="mb-1"><strong>Marca:</strong> <span id="det-prod-marca">-</span></p>
                            <p class="mb-1"><strong>Modelo:</strong> <span id="det-prod-modelo">-</span></p>
                            <p class="mb-0"><strong>Detalhes:</strong> <span id="det-prod-detalhes">-</span></p>
                        </div>
                    </div>

                    <!-- Coluna da Direita -->
                    <div class="col-md-7">
                        <h6 class="text-secondary fw-bold mb-3">Unidade Física</h6>
                        <div class="small">
                            <p class="mb-1"><strong>ID Sistema:</strong> <span id="det-eq-idsistema" class="text-primary fw-bold">-</span></p>
                            <p class="mb-1"><strong>Patrimônio / Etiqueta:</strong> <span id="det-eq-patrimonio">-</span></p>
                            <p class="mb-1"><strong>Número de Série:</strong> <span id="det-eq-serie">-</span></p>
                            <p class="mb-1"><strong>Identificador / CPU:</strong> <span id="det-eq-nome">-</span></p>
                            <p class="mb-1"><strong>Origem (Filial):</strong> <span id="det-eq-origem">-</span></p>
                            <p class="mb-1"><strong>Departamento:</strong> <span id="det-eq-departamento">-</span></p>
                            <p class="mb-1"><strong>IP Atual:</strong> <span id="det-eq-ip">-</span></p>
                            <p class="mb-1"><strong>Usuário Atual:</strong> <span id="det-eq-usuario">-</span></p>
                            <p class="mb-1"><strong>Status:</strong> <span id="det-eq-status">-</span></p>
                            <p class="mb-1"><strong>Situação:</strong> <span id="det-eq-situacao">-</span></p>
                            <!-- NOVO: Linha integrada de Especificações Dinâmicas -->
					       <p class="mb-1"> <strong>Especificações:</strong> <span id="det-eq-especificacoes" class="text-muted">Nenhuma</span>
					       </p>
                        </div>
                    </div>

                    <!-- Observações -->
                    <div class="col-12 mt-3">
                        <hr>
                        <p class="mb-1 small fw-bold text-secondary">Observações:</p>
                        <p class="text-muted border p-2 rounded bg-light small mb-0" id="det-eq-observacoes" style="white-space: pre-wrap;">-</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>
<!-- RODAPÉ DINÂMICO UNIFICADO -->
<jsp:include page="/WEB-INF/jsp/footer.jsp" />
<!-- LOGO EMPRESA NO LINK-->
<script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>

<!-- SCRIPTS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Variável global de contexto padronizada para os arquivos JS externos
    const contextPath = "${pageContext.request.contextPath}";

 // Injeta as permissões e dados do usuário logado diretamente no JS
    <%
        model.Usuario uLogado = (model.Usuario) session.getAttribute("usuarioLogado");
        boolean podeEditarGlob = uLogado != null && uLogado.temPermissao("equipamentos", "EDITAR");
        boolean podeExcluirGlob = uLogado != null && uLogado.temPermissao("equipamentos", "EXCLUIR");
        
        boolean isAdmin = uLogado != null && (
            "SUPER_ADMINISTRADOR".equalsIgnoreCase(uLogado.getPerfil()) || 
            "ADMINISTRADOR".equalsIgnoreCase(uLogado.getPerfil()) ||
            (uLogado.getPerfil() != null && uLogado.getPerfil().toUpperCase().contains("SUPER"))
        );

        String filialCod = "";
        if (uLogado != null && uLogado.getUnidadesPermitidas() != null && !uLogado.getUnidadesPermitidas().isEmpty()) {
            filialCod = uLogado.getUnidadesPermitidas().get(0);
        }
    %>
    const permissaoEditarEquipamento = <%= podeEditarGlob %>;
    const permissaoExcluirEquipamento = <%= podeExcluirGlob %>;
    const usuarioAdmin = <%= isAdmin %>;
    const filialUsuarioCodigo = "<%= filialCod %>";
</script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/consulta-equipamento.js"></script>
</body>
</html>