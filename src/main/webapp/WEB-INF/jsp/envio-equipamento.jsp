<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Envio de Equipamentos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>

<div class="container-fluid py-4" style="max-width: 1300px; margin: auto;">
    <!-- Cabeçalho da Página -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="page-title fw-bold text-primary-dark">ENVIO DE EQUIPAMENTOS</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0" style="font-size: 0.85rem;">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/jsp/index.jsp">Home</a></li>
                    <li class="breadcrumb-item"><a href="#">Movimentações</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Envio</li>
                </ol>
            </nav>
        </div>
    </div>

    <!-- Formulário Principal de Envio -->
    <div class="card p-4 mb-4 shadow-sm">
        <form id="formEnvio">
            <!-- PRIMEIRA LINHA: 4 CAMPOS -->
            <div class="row g-3 mb-3">
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Data do Envio *</label>
                    <input type="date" class="form-control form-control-sm" id="dataEnvio" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Origem *</label>
                    <select class="form-select form-select-sm" id="origemId" required>
                        <option value="">Selecione a origem...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Destino *</label>
                    <select class="form-select form-select-sm" id="destinoId" required>
                        <option value="">Selecione o destino...</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Responsável pelo Envio *</label>
                    <input type="text" class="form-control form-control-sm" id="responsavel" placeholder="Nome do colaborador" required>
                </div>
            </div>

            <!-- SEGUNDA LINHA: 4 CAMPOS -->
            <div class="row g-3 mb-3">
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Transportadora *</label>
                    <select class="form-select form-select-sm" id="transportadora" required>
                        <option value="">Selecione...</option>
                        <option value="Correios">Correios</option>
                        <option value="Jadlog">Jadlog</option>
                        <option value="Total Express">Total Express</option>
                        <option value="Outra">Outra</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Nº da Nota Fiscal *</label>
                    <input type="text" class="form-control form-control-sm" id="numeroNota" name="numeroNota" placeholder="Ex: 000123456" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Código de Rastreio *</label>
                    <input type="text" class="form-control form-control-sm" id="codigoRastreio" placeholder="Ex: AA123456789BR" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Data Prevista de Entrega</label>
                    <input type="date" class="form-control form-control-sm" id="dataPrevisao">
                </div>
            </div>

            <!-- TERCEIRA LINHA: OBSERVAÇÕES -->
            <div class="mb-4">
                <label class="form-label fw-bold small">Observações</label>
                <textarea class="form-control form-control-sm" id="observacoes" rows="2" placeholder="Detalhes adicionais sobre o envio..."></textarea>
            </div>

            <hr class="my-4">

            <!-- Seção de Itens -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold m-0 text-primary-dark"><i class="fa fa-boxes me-2"></i>Equipamentos a Enviar</h5>
                <button type="button" class="btn btn-primary btn-sm" id="btnAbrirModalEquipamentos">
                    <i class="fa fa-plus me-1"></i> Adicionar Equipamento
                </button>
            </div>

            <div class="table-responsive">
                <table class="table table-bordered table-hover align-middle mb-0" style="font-size: 0.85rem;" id="tabelaItensEnvio">
                    <thead class="table-light text-secondary">
                        <tr>
                            <th>ID Sistema</th>
                            <th>Patrimônio</th>
                            <th>Nome da CPU</th>
                            <th>Produto</th>
                            <th>Número de Série</th>
                            <th>Status Atual</th>
                            <th class="text-center" style="width: 80px;">Ações</th>
                        </tr>
                    </thead>
                    <tbody id="corpoTabelaItens">
                        <tr id="linhaVazia">
                            <td colspan="7" class="text-center text-muted py-4">Nenhum equipamento adicionado ao envio.</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-success px-4 fw-bold">
                    <i class="fa fa-paper-plane me-1"></i> Efetuar Envio
                </button>
                <button type="button" class="btn btn-outline-secondary px-4" onclick="window.location.reload();">
                    Cancelar
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Modal de Seleção de Equipamentos -->
<div class="modal fade" id="modalSelecionarEquipamento" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title"><i class="fas fa-search me-2"></i>Selecionar Equipamentos Disponíveis</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                    <table class="table table-sm table-hover align-middle" style="font-size: 0.85rem;">
                        <thead class="table-light sticky-top">
                            <tr>
                                <th style="width: 40px;" class="text-center">
                                    <input type="checkbox" id="selecionarTodosModal" class="form-check-input">
                                </th>
                                <th>ID Sistema</th>
                                <th>Patrimônio</th>
                                <th>CPU / Identificador</th>
                                <th>Produto</th>
                                <th>Série</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody id="tabelaModalEquipamentosBody">
                            <!-- Preenchido via JS -->
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Fechar</button>
                <button type="button" class="btn btn-primary btn-sm" id="btnConfirmarSelecao">Adicionar Selecionados</button>
            </div>
        </div>
    </div>
</div>

<!-- Modais Padronizados do Sistema (Alert / Confirm) -->
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
    // Context path padrão do projeto para os arquivos JS
    const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/envio-equipamento.js"></script>
</body>
</html>