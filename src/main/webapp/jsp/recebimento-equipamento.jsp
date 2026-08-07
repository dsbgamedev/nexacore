<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Recebimento de Equipamentos</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>
    <div class="container mt-4">
        <h2>Recebimento de Equipamentos</h2>
        <p class="text-muted">Movimentações / Recebimento</p>

        <form id="formRecebimento">
            <div class="row">
                <!-- Seleção do Envio em Trânsito -->
                <div class="col-md-4 mb-3">
                    <label for="selectEnvio" class="form-label">Selecionar Envio (Trânsito) *</label>
                    <select id="selectEnvio" name="idEnvio" class="form-select" required>
                        <option value="">Selecione um envio...</option>
                        <!-- Preenchido dinamicamente via Java/AJAX com os envios com status_id = 2 -->
                    </select>
                </div>
                
                <!-- Campo Origem (Preenchido Automático) -->
			    <div class="col-md-4 mb-3">
			        <label for="origem" class="form-label">Origem</label>
			        <input type="text" id="origem" class="form-control" readonly>
			    </div>

                <div class="col-md-4 mb-3">
                    <label for="dataRecebimento" class="form-label">Data do Recebimento *</label>
                    <input type="date" id="dataRecebimento" name="dataRecebimento" class="form-control" required>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="responsavel" class="form-label">Responsável pelo Recebimento *</label>
                    <input type="text" id="responsavel" name="responsavel" class="form-control" placeholder="Ex: Maria Souza" required>
                </div>
            </div>

            <div class="row">
                <div class="col-md-4 mb-3">
                    <label for="transportadora" class="form-label">Transportadora</label>
                    <input type="text" id="transportadora" class="form-control" readonly>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="codigoRastreio" class="form-label">Código de Rastreio</label>
                    <input type="text" id="codigoRastreio" class="form-control" readonly>
                </div>

                <div class="col-md-4 mb-3">
                    <label for="condicaoGeral" class="form-label">Condição Geral</label>
                    <input type="text" id="condicaoGeral" name="condicaoGeral" class="form-control" value="Todos os itens em perfeito estado">
                </div>
            </div>

            <hr class="my-4">

            <h4>Equipamentos Recebidos</h4>
            <div class="table-responsive">
                <table class="table table-bordered table-striped" id="tabelaItensRecebimento">
                    <thead>
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
                            <td colspan="6" class="text-center text-muted">Selecione um envio acima para carregar os equipamentos.</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="mt-4">
                <button type="submit" class="btn btn-success">Confirmar Recebimento</button>
                <a href="index.jsp" class="btn btn-secondary">Cancelar</a>
            </div>
        </form>
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

<!-- Scripts -->
<script>
   const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/recebimento.js"></script>