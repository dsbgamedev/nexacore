<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Consulta de Envios</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="bg-light">

<div class="container-fluid py-4">
    <!-- Cabeçalho da Página e Breadcrumb -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="page-title fw-bold text-primary-dark">CONSULTA DE ENVIOS DE EQUIPAMENTOS</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/jsp/index.jsp">Home</a></li>
                    <li class="breadcrumb-item"><a href="#">Movimentações</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Consulta de Envios</li>
                </ol>
            </nav>
        </div>
    </div>

    <!-- Barra de Pesquisa Geral -->
    <div class="card p-3 mb-4 shadow-sm">
        <div class="input-group input-group-sm">
            <span class="input-group-text bg-white"><i class="fas fa-search text-muted"></i></span>
            <input type="text" id="inputPesquisaGlobal" class="form-control" placeholder="Pesquisar por ID, origem, destino, status, transportadora ou rastreio...">
        </div>
    </div>

    <!-- TABELA DE ENVIOS -->
    <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold text-primary-dark"><i class="fas fa-list me-2"></i> Envios Realizados</h5>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0" id="tabelaEnvios">
                    <thead class="table-light text-secondary">
                        <tr>
                            <th>ID Envio</th>
                            <th>Data Envio</th>
                            <th>Origem</th>
                            <th>Destino</th>
                            <th>Status</th>
                            <th>Transportadora</th>
                            <th>Rastreio</th>
                            <th>Responsável</th>
                            <th class="text-center">Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Preenchido via JavaScript -->
                        <tr>
                            <td colspan="9" class="text-center text-muted py-4">Nenhum envio encontrado.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modais Padronizados do Sistema (Necessários para o ModalService funcionar) -->
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