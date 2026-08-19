<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Abertura de Chamado</title>
    <script>
        var contextPath = "<%= request.getContextPath() %>";
    </script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <style>
        .card-preview { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 20px; }
        .section-title { font-size: 1.05rem; font-weight: 600; color: #1e293b; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; margin-bottom: 20px; }
        .card-resumo { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; }
    </style>
</head>
<body class="p-4 bg-light">

    <!-- CABEÇALHO / BREADCRUMB -->
    <div class="container-fluid mb-4 d-flex justify-content-between align-items-center">
        <div>
            <h4 class="page-title text-uppercase fw-bold"><i class="fa-solid fa-screwdriver-wrench me-2"></i> Abertura de Chamado de Manutenção</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="#">Home</a></li>
                    <li class="breadcrumb-item"><a href="#">Manutenções</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Abertura de Chamado</li>
                </ol>
            </nav>
        </div>
        <button class="btn btn-outline-secondary btn-sm" id="btnHistoricoEquipamento" disabled>
            <i class="fa-solid fa-clock-rotate-left me-1"></i> Histórico do Equipamento
        </button>
    </div>

    <div class="container-fluid">
        <div class="card p-4 shadow-sm mb-4">
            <form id="formChamado">
                
                <!-- SEÇÃO 1: EQUIPAMENTO -->
                <div class="section-title"><i class="fa-solid fa-laptop me-1"></i> 1. Equipamento</div>
                <div class="row mb-4">
                    <div class="col-md-6 mb-3">
                        <label for="selectEquipamento" class="form-label fw-bold">Selecionar Equipamento *</label>
                        <select class="form-select form-select-sm" id="selectEquipamento" name="idEquipamento" required>
                            <option value="">Carregando equipamentos...</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Número de Patrimônio / Etiqueta</label>
                        <input type="text" class="form-control form-control-sm" id="patrimonio" readonly placeholder="---">
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">ID Sistema</label>
                        <input type="text" class="form-control form-control-sm" id="idSistema" readonly placeholder="---">
                    </div>

                    <!-- Card de Pré-visualização Detalhada do Equipamento -->
                    <div class="col-12">
                        <div class="card-preview">
                            <div class="row align-items-center">
                                <div class="col-md-3 border-end">
                                    <h6 class="fw-bold text-primary mb-1" id="lblNomeEquipamento">Selecione um equipamento</h6>
                                    <span class="text-muted d-block small">Nº de Série: <strong id="lblSerie">---</strong></span>
                                    <span class="text-muted d-block small">Local Atual: <strong id="lblLocal">---</strong></span>
                                </div>
                                <div class="col-md-3">
                                    <span class="text-muted d-block small">Usuário Atual</span>
                                    <strong id="lblUsuario">---</strong>
                                    <span class="text-muted d-block small mt-1">Departamento: <strong id="lblDepto">---</strong></span>
                                </div>
                                <div class="col-md-3">
                                    <span class="text-muted d-block small">Situação Atual</span>
                                    <span id="lblSituacao" class="badge bg-secondary">---</span>
                                </div>
                                <div class="col-md-3">
                                    <span class="text-muted d-block small">Status</span>
                                    <span id="lblStatus" class="badge bg-secondary">---</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- SEÇÃO 2: DADOS DA MANUTENÇÃO -->
                <div class="section-title"><i class="fa-solid fa-file-lines me-1"></i> 2. Dados da Manutenção</div>
                <div class="row mb-4">
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Tipo de Manutenção *</label>
                        <select class="form-select form-select-sm" name="tipoManutencao" required>
                            <option value="Corretiva">Corretiva</option>
                            <option value="Preventiva">Preventiva</option>
                        </select>
                    </div>
                    <!-- CAMPO ADICIONADO PARA EVITAR O ERRO DE NULO NO BANCO -->
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Tipo de Problema *</label>
                        <select class="form-select form-select-sm" name="tipoProblema" required>
                            <option value="">Selecione...</option>
                            <option value="Hardware">Hardware</option>
                            <option value="Software">Software</option>
                            <option value="Rede">Rede</option>
                            <option value="Outros">Outros</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Prioridade *</label>
                        <select class="form-select form-select-sm" name="prioridade" required>
                            <option value="Baixa">Baixa</option>
                            <option value="Média" selected>Média</option>
                            <option value="Alta">Alta</option>
                            <option value="Urgente">Urgente</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Data da Abertura *</label>
                        <input type="date" class="form-control form-control-sm" id="dataAbertura" name="dataAbertura" required>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Previsão de Atendimento</label>
                        <input type="date" class="form-control form-control-sm" name="previsaoAtendimento">
                    </div>

                    <div class="col-md-4 mb-3">
                        <label class="form-label">Responsável pela Abertura *</label>
                        <input type="text" class="form-control form-control-sm" name="solicitante" required placeholder="Ex: Maria Souza">
                    </div>
                    <div class="col-md-4 mb-3">
					    <label class="form-label">Departamento Solicitante *</label>
					    <select class="form-select form-select-sm" name="idDepartamento" id="selectDepartamento" required disabled>
					        <option value="">Selecione um equipamento...</option>
					    </select>
					</div>
                    <div class="col-md-4 mb-3">
					    <label class="form-label">Local para Atendimento *</label>
					    <select class="form-select form-select-sm" name="filialOrigemId" id="selectFilial" required disabled>
					        <option value="">Selecione um equipamento...</option>
					    </select>
					</div>

                    <div class="col-md-6 mb-3">
                        <label class="form-label">Descrição do Problema *</label>
                        <textarea class="form-control form-control-sm" name="descricaoProblema" rows="3" required placeholder="Descreva a falha apresentada..."></textarea>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Observações Adicionais</label>
                        <textarea class="form-control form-control-sm" name="observacoes" rows="3" placeholder="Informações extras..."></textarea>
                    </div>
                </div>

                <!-- SEÇÃO 3: HISTÓRICO DO EQUIPAMENTO (COM LAYOUT DE 2 COLUNAS IGUAL AO PRINT) -->
                <div class="section-title"><i class="fa-solid fa-clock-rotate-left me-1"></i> 3. Histórico do Equipamento</div>
                <div class="row mb-4">
                    <!-- Coluna da Esquerda: Abas de Tabelas (Manutenções e Movimentações) -->
                    <div class="col-md-9">
                        <ul class="nav nav-tabs mb-3" id="historicoTabs" role="tablist">
                            <li class="nav-item"><button class="nav-link active small py-1" data-bs-toggle="tab" data-bs-target="#tabManutencoes" type="button">Histórico de Manutenções</button></li>
                            <li class="nav-item"><button class="nav-link small py-1" data-bs-toggle="tab" data-bs-target="#tabMovimentacoes" type="button">Histórico de Movimentações</button></li>
                        </ul>
                        <div class="tab-content" id="historicoContent">
                            <!-- Aba Manutenções -->
                            <div class="tab-pane fade show active" id="tabManutencoes">
                                <div class="table-responsive">
                                    <table class="table table-sm table-bordered align-middle small mb-2">
                                        <thead class="table-light">
                                            <tr>
                                                <th>ID</th>
                                                <th>Data Abertura</th>
                                                <th>Tipo</th>
                                                <th>Problema</th>
                                                <th>Responsável Técnico</th>
                                                <th>Status</th>
                                                <th class="text-center">Ações</th>
                                            </tr>
                                        </thead>
                                        <tbody id="tabelaHistoricoManutencoes">
                                            <tr><td colspan="7" class="text-center text-muted">Selecione um equipamento para carregar o histórico.</td></tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="text-start">
                                    <button type="button" class="btn btn-link btn-sm p-0 text-decoration-none small" id="btnVerMaisManutencoes" style="display: none;">
                                        Ver mais histórico <i class="fa fa-arrow-right ms-1"></i>
                                    </button>
                                </div>
                            </div>
                            <!-- Aba Movimentações -->
                            <div class="tab-pane fade" id="tabMovimentacoes">
                                <div class="table-responsive">
                                    <table class="table table-sm table-bordered align-middle small mb-2">
                                        <thead class="table-light">
                                            <tr>
                                                <th>Data</th>
                                                <th>Tipo</th>
                                                <th>Origem</th>
                                                <th>Destino</th>
                                                <th>Situação</th>
                                                <th>Responsável</th>
                                            </tr>
                                        </thead>
                                        <tbody id="tabelaHistoricoMovimentacoes">
                                            <tr><td colspan="6" class="text-center text-muted">Selecione um equipamento para carregar as movimentações.</td></tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="text-start">
                                    <button type="button" class="btn btn-link btn-sm p-0 text-decoration-none small" id="btnVerMaisMovimentacoes" style="display: none;">
                                        Ver mais histórico <i class="fa fa-arrow-right ms-1"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Coluna da Direita: Card de Resumo do Equipamento -->
                    <div class="col-md-3">
                        <div class="card-resumo h-100 d-flex flex-column justify-content-between">
                            <div>
                                <h6 class="fw-bold text-dark small mb-3 border-bottom pb-2">Resumo do Equipamento</h6>
                                <div class="d-flex justify-content-between small mb-2">
                                    <span class="text-muted"><i class="fa fa-wrench me-1"></i> Total de Manutenções</span>
                                    <strong id="resumoTotalManutencoes">0</strong>
                                </div>
                                <div class="d-flex justify-content-between small mb-2">
                                    <span class="text-muted"><i class="fa fa-calendar me-1"></i> Última Manutenção</span>
                                    <strong id="resumoUltimaManutencao">---</strong>
                                </div>
                                <div class="d-flex justify-content-between small mb-2">
                                    <span class="text-muted"><i class="fa fa-clock me-1"></i> Tempo Médio Atend.</span>
                                    <strong id="resumoTempoMedio">---</strong>
                                </div>
                                <div class="d-flex justify-content-between small mb-2">
                                    <span class="text-muted"><i class="fa fa-check-circle me-1"></i> Taxa de Conclusão</span>
                                    <strong id="resumoTaxaConclusao">---</strong>
                                </div>
                            </div>
                            <div class="pt-3 border-top mt-2">
                                <a href="#" id="linkHistoricoCompleto" class="text-decoration-none small fw-bold d-flex justify-content-between align-items-center text-primary" style="pointer-events: none; opacity: 0.5;">
                                    <span>Ver histórico completo</span> <i class="fa fa-arrow-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- BOTÕES DE AÇÃO -->
                <div class="d-flex justify-content-between pt-3 border-top">
                    <button type="button" class="btn btn-outline-secondary btn-sm px-4" onclick="window.history.back();">
                        <i class="fa fa-arrow-left me-1"></i> Voltar
                    </button>
                    <div>
                        <button type="button" class="btn btn-outline-danger btn-sm px-3 me-2" onclick="document.getElementById('formChamado').reset();">
                            <i class="fa fa-eraser me-1"></i> Limpar
                        </button>
                        <button type="submit" class="btn btn-primary btn-sm px-4">
                            <i class="fa fa-save me-1"></i> Abrir Chamado
                        </button>
                    </div>
                </div>

            </form>
        </div>
    </div>
    <!-- HTML DOS MODAIS PADRÃO DO SISTEMA -->
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
    <!-- SCRIPTS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/manutencao-abertura.js"></script>
    
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>