<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Envios</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Adicionado Bootstrap Icons para combinar com a nova UI moderna -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="bg-light">

<div class="container-fluid px-4 py-4">
 	<div>
      <h4 class="page-title fw-bold text-primary-dark">CONSULTA DE EQUIPAMENTOS (UNIDADE FÍSICA)</h4>
      <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0">
              <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
              <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/EnvioEquipamentoServlet">Enivos Equipamentos</a></li>
              <li class="breadcrumb-item active" aria-current="page">Consulta Enivos Equipamentos</li>
          </ol>
      </nav>     
     </div>

    <!-- Barra Superior de Pesquisa e Ações (Filtros, Exportar, Novo Envio) -->
    <div class="card border-0 p-3 mb-4 shadow-sm">
        <div class="row g-3 align-items-center">
            <!-- Barra de Pesquisa -->
            <div class="col-md-6">
                <div class="input-group">
                    <span class="input-group-text bg-white border-end-0 text-muted">
                        <i class="bi bi-search"></i>
                    </span>
                    <input type="text" id="inputPesquisaGlobal" class="form-control border-start-0 ps-0" placeholder="Pesquisar por ID, origem, destino, status, produto, nota ou rastreio...">
                </div>
            </div>

            <!-- Botões Superiores de Ação -->
            <div class="col-md-6 text-md-end">
            <!-- Botão de Atualizar dados sem precisar de F5 -->
			    <button type="button" class="btn btn-outline-secondary btn-sm me-2" id="btnAtualizarTela" title="Atualizar Lista">
			        <i class="fas fa-sync-alt"></i> Atualizar
			    </button>
                <button class="btn btn-outline-secondary btn-sm me-2" type="button">
                    <i class="bi bi-funnel"></i> Filtros
                </button>
                <button class="btn btn-outline-secondary btn-sm me-2" type="button">
                    <i class="bi bi-download"></i> Exportar
                </button>
                <a href="${pageContext.request.contextPath}/EnvioEquipamentoServlet" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg"></i> Novo Envio
                </a>
            </div>
        </div>
    </div>

    <!-- TABELA DE ENVIOS -->
    <div class="card border-0 shadow-sm">
        <div class="card-header bg-white py-3">
            <h6 class="m-0 font-weight-bold text-primary">
                <i class="bi bi-list-ul me-2"></i> Envios Realizados
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0" id="tabelaEnvios">
                    <thead class="table-light text-secondary small text-uppercase">
                        <tr>
                            <th class="ps-3"><i class="bi bi-hash me-1"></i> ID</th>
                            <th><i class="bi bi-calendar-event me-1"></i> Data</th>
                            <th><i class="bi bi-box-arrow-up-right me-1"></i> Origem</th>
                            <th><i class="bi bi-box-arrow-in-down me-1"></i> Destino</th>
                            <th><i class="bi bi-box-seam me-1"></i> Produtos</th>
                            <th><i class="bi bi-receipt me-1"></i> Nota Fiscal</th>
                            <th><i class="bi bi-truck me-1"></i> Transportadora</th>
                            <th><i class="bi bi-link-45deg me-1"></i> Rastreio</th>
                            <th><i class="bi bi-patch-check me-1"></i> Status</th>
                            <th><i class="bi bi-person me-1"></i> Responsável</th>
                            <th class="text-center pe-3"><i class="bi bi-gear me-1"></i> Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Preenchido via JavaScript -->
                        <tr>
                            <td colspan="11" class="text-center text-muted py-4">Nenhum envio encontrado.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- 👇 ADICIONE ESTE BLOCO AQUI (Rodapé da paginação) 👇 -->
        <div class="card-footer d-flex justify-content-between align-items-center">
            <div class="text-muted small" id="infoPaginacao">
                Mostrando 0 de 0 registros
            </div>
            <nav aria-label="Navegação de páginas">
                <ul class="pagination pagination-sm mb-0" id="botoesPaginacao">
                    <!-- Preenchido dinamicamente via JavaScript -->
                </ul>
            </nav>
        </div>
        <!-- 👆 FIM DO BLOCO DE PAGINAÇÃO 👆 -->
    </div>
</div>

<!-- Modais Padronizados do Sistema -->
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

<!-- Modal de Detalhes do Envio -->
<div class="modal fade" id="modalDetalhesEnvio" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title fw-bold" id="tituloModalDetalhes"><i class="bi bi-info-circle me-2"></i>ENVIO #</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4">
                <!-- Informações Principais -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <span class="text-muted small d-block fw-semibold">ORIGEM</span>
                        <span id="detalheOrigem" class="text-dark fw-bold">-</span>
                    </div>
                    <div class="col-md-6">
                        <span class="text-muted small d-block fw-semibold">DESTINO</span>
                        <span id="detalheDestino" class="text-dark fw-bold">-</span>
                    </div>
                    <div class="col-md-4">
                        <span class="text-muted small d-block fw-semibold">TRANSPORTADORA</span>
                        <span id="detalheTransportadora" class="text-dark">-</span>
                    </div>
                    <div class="col-md-4">
                        <span class="text-muted small d-block fw-semibold">NOTA FISCAL</span>
                        <span id="detalheNota" class="text-dark font-monospace">-</span>
                    </div>
                    <div class="col-md-4">
                        <span class="text-muted small d-block fw-semibold">CÓDIGO DE RASTREIO</span>
                        <span id="detalheRastreio" class="text-primary">-</span>
                    </div>
                </div>

                <hr class="text-muted opacity-25">

                <!-- Produtos Enviados -->
                <h6 class="fw-bold text-dark mb-3"><i class="bi bi-boxes me-2"></i>Produtos enviados</h6>
                <div class="table-responsive border rounded mb-4">
                    <table class="table table-sm align-middle mb-0">
                        <thead class="table-light text-secondary" style="font-size: 0.75rem;">
                            <tr>
                                <th class="ps-3">ID Sistema</th>
                                <th>Patrimônio</th>
                                <th>Produto</th>
                                <th>Nº de Série</th>
                            </tr>
                        </thead>
                        <tbody id="tabelaProdutosDetalhe" style="font-size: 0.85rem;">
                            <tr><td colspan="4" class="text-center text-muted py-3">Carregando produtos...</td></tr>
                        </tbody>
                    </table>
                </div>

                <!-- Histórico de Movimentação -->
                <h6 class="fw-bold text-dark mb-3"><i class="bi bi-clock-history me-2"></i>Histórico</h6>
                <div class="border rounded p-3 bg-light" style="max-height: 150px; overflow-y: auto;">
                    <ul class="list-unstyled mb-0 small" id="listaHistoricoEnvio">
                        <!-- Preenchido via JS -->
                    </ul>
                </div>
            </div>
            <div class="modal-footer bg-light">
                <button type="button" class="btn btn-secondary btn-sm px-4" data-bs-dismiss="modal">Fechar</button>
            </div>
        </div>
    </div>
</div>

<!-- SCRIPTS JS -->
<script>
    const contextPath = '${pageContext.request.contextPath}';
</script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/consulta-envios.js"></script>
</body>
</html>