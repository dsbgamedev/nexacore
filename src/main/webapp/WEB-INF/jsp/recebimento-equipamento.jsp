<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nexacore | Recebimento de Equipamentos</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="p-4 bg-light">

<div class="container-fluid py-4" style="max-width: 1300px; margin: auto;">
    
    <!-- Cabeçalho Padrão com Tema Escuro Unificado -->
    <div class="card-cabecalho-padrao tema-escuro p-4 mb-4 shadow-sm border-0">
        <!-- Linha Superior: Título e Breadcrumb -->
        <div class="mb-3">
            <h4 class="page-title fw-bold text-white mb-1">RECEBIMENTO DE EQUIPAMENTOS</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0" style="font-size: 0.85rem;">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet" class="text-light text-decoration-none">Home</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ConsultaEnvioServlet" class="text-light text-decoration-none">Consulta Envio Equipamentos</a></li>
                    <li class="breadcrumb-item active text-secondary" aria-current="page">Recebimento / Devolução</li>
                </ol>
            </nav>     
        </div>
        
        <!-- Linha Inferior: Grupo de Botões de Atalho Padronizados -->
        <div class="cabecalho-botoes-atalho border-secondary pt-2 border-top d-flex flex-wrap gap-2">
            <a href="${pageContext.request.contextPath}/ConsultaEquipamentosServlet" class="btn btn-success btn-moderno px-3 py-2 text-white">
                <i class="fa fa-plus me-1"></i> Consulta Equipamento
            </a>
            <a href="${pageContext.request.contextPath}/EnvioEquipamentoServlet" class="btn btn-primary btn-moderno px-3 py-2 text-white">
                <i class="fas fa-external-link-alt me-1"></i> Envio Equipamentos
            </a>
            <a href="${pageContext.request.contextPath}/DevolucaoEquipamentoServlet" class="btn btn-warning btn-moderno px-3 py-2 text-dark fw-semibold">
                <i class="fas fa-undo me-1"></i> Envio Equip. Devolução
            </a>
            <a href="${pageContext.request.contextPath}/ConsultaEnvioServlet" class="btn btn-dark border border-secondary px-3 py-2 text-white">
                <i class="fas fa-search me-1"></i> Consulta Envio
            </a>
            <a href="${pageContext.request.contextPath}/MenuServlet" class="btn btn-dark border border-secondary px-3 py-2 text-white">
                <i class="fa fa-plus me-1"></i> Menu
            </a>
        </div>
    </div>

    <!-- Formulário Principal de Recebimento -->
    <div class="card p-4 mb-4 shadow-sm">
        <form id="formRecebimento" enctype="multipart/form-data">
            <!-- Seletor de Tipo de Operação (Cards) -->
            <div class="row mb-4">
                <div class="col-md-6">
                    <div class="card tipo-operacao-card active border-success shadow-sm" id="cardEnvio" style="cursor: pointer;" onclick="selecionarTipoOperacao('envio')">
                        <div class="card-body">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipoOperacao" id="radioEnvio" value="envio" checked>
                                <label class="form-check-label fw-bold text-success" for="radioEnvio">
                                    Recebimento de Envio
                                </label>
                            </div>
                            <p class="text-muted small mb-0 mt-1">Receber equipamentos enviados de outra filial.</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card tipo-operacao-card border-secondary shadow-sm" id="cardDevolucao" style="cursor: pointer;" onclick="selecionarTipoOperacao('devolucao')">
                        <div class="card-body">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="tipoOperacao" id="radioDevolucao" value="devolucao">
                                <label class="form-check-label fw-bold text-secondary" for="radioDevolucao">
                                    Devolução de Equipamento
                                </label>
                            </div>
                            <p class="text-muted small mb-0 mt-1">Receber equipamentos devolvidos para esta filial.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Alerta Informativo de Seleção -->
            <div class="alert alert-primary d-flex align-items-center mb-4" role="alert" id="alertaInformativo">
                <i class="fas fa-info-circle me-2"></i>
                <div id="textoAlertaTopo">
                    Ao selecionar um envio, os dados da movimentação e dos equipamentos serão carregados automaticamente.
                </div>
            </div>

            <div class="row">
                <div class="col-md-4 mb-3">
                    <label id="labelSelectMovimentacao" class="form-label fw-bold small">Selecionar Envio (Trânsito) *</label>
                    <select id="selectEnvio" name="idMovimentacao" class="form-select form-select-sm" required>
                        <option value="">Selecione...</option>
                    </select>
                </div>
                
                <div class="col-md-4 mb-3">
                    <label for="origem" class="form-label fw-bold small">Origem</label>
                    <input type="text" id="origem" class="form-control form-control-sm" readonly>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="dataRecebimento" class="form-label fw-bold small">Data do Recebimento *</label>
                    <input type="date" id="dataRecebimento" name="dataRecebimento" class="form-control form-control-sm" required>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="responsavel" class="form-label fw-bold small">Responsável pelo Recebimento *</label>
                    <input type="text" id="responsavel" name="responsavel" class="form-control form-control-sm" placeholder="Ex: Maria Souza" required>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="transportadora" class="form-label fw-bold small">Transportadora</label>
                    <input type="text" id="transportadora" class="form-control form-control-sm" readonly>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="codigoRastreio" class="form-label fw-bold small">Código de Rastreio</label>
                    <input type="text" id="codigoRastreio" class="form-control form-control-sm" readonly>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="condicaoGeral" class="form-label fw-bold small">Condição Geral</label>
                    <input type="text" id="condicaoGeral" name="condicaoGeral" class="form-control form-control-sm" value="Todos os itens em perfeito estado">                    
                </div>
                <div class="col-md-6 mb-3">
                    <label for="comprovanteRecebimento" class="form-label fw-bold small">Foto/Vídeo da Condição (Opcional)</label>
                    <input type="file" id="comprovanteRecebimento" name="comprovanteRecebimento" class="form-control form-control-sm" accept="image/*,video/*">
                    <div class="form-text">Anexe uma foto ou vídeo caso o equipamento apresente avarias.</div>
                </div>
            </div>

            <hr class="my-4">

            <h5 class="fw-bold text-primary-dark mb-3"><i class="fa fa-boxes me-2"></i>Equipamentos Recebidos</h5>
            
            <!-- Alerta amarelo operacional -->
            <div class="alert alert-warning py-2 mb-3" role="alert" style="font-size: 0.9rem;">
                <i class="fas fa-exclamation-triangle me-2"></i> Após confirmar o recebimento, a movimentação será finalizada e o equipamento ficará disponível nesta filial.
            </div>

            <div class="table-responsive">
                <table class="table table-bordered table-hover align-middle mb-0" style="font-size: 0.85rem;" id="tabelaItensRecebimento">
                    <thead class="table-light text-secondary">
                        <tr>
                            <th>ID Sistema</th>
                            <th>Patrimônio</th>
                            <th>Nome da CPU</th>
                            <th>Produto</th>
                            <th>Número de Série</th>
                            <th>Status Atual</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">Selecione um item acima para carregar os equipamentos.</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-success px-4 fw-bold">Confirmar Recebimento</button>
                <button type="button" class="btn btn-outline-secondary px-4" id="btnCancelar">Limpar</button>
            </div>
        </form>
    </div>
</div>

<!-- Modais -->
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
<!-- RODAPÉ DINÂMICO UNIFICADO -->
<jsp:include page="/WEB-INF/jsp/footer.jsp" />
<!-- LOGO EMPRESA NO LINK-->
<script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>

<script>
   const contextPath = "${pageContext.request.contextPath}";
   const usuarioLogadoSessao = "${loginUsuarioLogado}";
</script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/recebimento.js"></script>
</body>
</html>