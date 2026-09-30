<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Cadastro de Transportadora</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/transportadora.css">
   </head>
<body>

    <div class="container-fluid py-4 px-4">
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb mb-1" style="font-size: 0.85rem;">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ConsultaTransportadoraServlet">Consulta Transportadora</a></li>
                <li class="breadcrumb-item active" aria-current="page">Transportadoras</li>
            </ol>
        </nav>

        <div class="d-flex align-items-center mb-4">
            <div class="page-header-icon me-3"><i class="fa-solid fa-truck-fast"></i></div>
            <div>
                <h4 class="mb-0 fw-bold text-dark">Cadastro de Transportadora</h4>
                <small class="text-muted">Dados cadastrais e múltiplos endereços</small>
            </div>
        </div>

        <form id="formTransportadora">
            <div class="row">
                <!-- DADOS DA TRANSPORTADORA -->
                <div class="col-lg-7">
                    <div class="card card-custom p-4">
                        <h5 class="fw-bold mb-3 text-secondary" style="font-size: 1rem;"><i class="fa-solid fa-building me-2"></i> Dados da transportadora</h5>
                        <hr class="mt-0 mb-3">
                        <div class="row g-3">
                            <div class="col-md-7">
                                <label class="form-label">Razão Social *</label>
                                <input type="text" class="form-control" id="razaoSocial" required>
                            </div>
                            <div class="col-md-5">
                                <label class="form-label">Nome Fantasia</label>
                                <input type="text" class="form-control" id="nomeFantasia">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">CNPJ *</label>
                                <input type="text" class="form-control" id="cnpj" placeholder="00.000.000/0000-00" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Inscrição Estadual</label>
                                <input type="text" class="form-control" id="inscricaoEstadual">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">RNTRC</label>
                                <input type="text" class="form-control" id="rntrc">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Telefone</label>
                                <input type="text" class="form-control" id="telefone">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Celular</label>
                                <input type="text" class="form-control" id="celular">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">E-mail *</label>
                                <input type="email" class="form-control" id="email" required>
                            </div>
                            <div class="col-md-8">
                                <label class="form-label">Site</label>
                                <input type="text" class="form-control" id="site">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Status *</label>
                                <select class="form-select" id="status">
                                    <option value="ATIVA" selected>Ativa</option>
                                    <option value="INATIVA">Inativa</option>
                                    <option value="BLOQUEADA">Bloqueada</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="form-label">Observações</label>
                                <textarea class="form-control" id="observacao" rows="3" maxlength="500"></textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ENDEREÇO INDIVIDUAL PARA ADICIONAR -->
                <div class="col-lg-5">
                    <div class="card card-custom p-4">
                        <h5 class="fw-bold mb-3 text-secondary" style="font-size: 1rem;"><i class="fa-solid fa-location-dot me-2"></i> Adicionar Endereço</h5>
                        <hr class="mt-0 mb-3">
                        <div class="mb-3">
						    <label class="form-label">Tipo de endereço *</label>
						    <select class="form-select" id="input-tipo-endereco" required>
						        <option value="" disabled selected>Selecione o tipo...</option>
						        <c:forEach var="tipo" items="${listaTiposEndereco}">
						            <option value="${tipo.id}">${tipo.nome}</option>
						        </c:forEach>
						    </select>
						</div>
                        <div class="row g-3">
                            <div class="col-md-7">
                                <label class="form-label">CEP *</label>
                                <input type="text" class="form-control" id="input-cep">
                            </div>
                            <div class="col-md-5 d-flex align-items-end">
                                <button type="button" class="btn btn-outline-secondary btn-sm w-100" id="btn-buscar-cep">Buscar CEP</button>
                            </div>
                            <div class="col-md-9">
                                <label class="form-label">Logradouro *</label>
                                <input type="text" class="form-control" id="input-logradouro">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Número *</label>
                                <input type="text" class="form-control" id="input-numero">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Complemento</label>
                                <input type="text" class="form-control" id="input-complemento">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Bairro *</label>
                                <input type="text" class="form-control" id="input-bairro">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Cidade *</label>
                                <input type="text" class="form-control" id="input-cidade">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">UF *</label>
                                <input type="text" class="form-control" id="input-uf" maxlength="2">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">País *</label>
                                <input type="text" class="form-control" id="input-pais" value="Brasil">
                            </div>
                            <div class="col-12">
                                <label class="form-label">Referência</label>
                                <input type="text" class="form-control" id="input-referencia">
                            </div>
                            <div class="col-12">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="check-principal">
                                    <label class="form-check-label fw-bold" for="check-principal" style="font-size: 0.875rem;">Endereço principal</label>
                                </div>
                            </div>
                            <div class="col-12 mt-2">
                                <button type="button" class="btn btn-secondary w-100 btn-sm" id="btn-adicionar-endereco">
                                    <i class="fa fa-plus me-1"></i> Incluir este endereço na lista
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- TABELA DE ENDEREÇOS CADASTRADOS -->
            <div class="card card-custom p-4 mt-2">
                <h5 class="fw-bold mb-3 text-secondary" style="font-size: 1rem;"><i class="fa-solid fa-map-location-dot me-2"></i> Endereços cadastrados para esta transportadora</h5>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" style="font-size: 0.875rem;">
                        <thead class="table-light text-muted">
                            <tr>
                                <th>Tipo ID</th>
                                <th>Endereço</th>
                                <th>Cidade/UF</th>
                                <th>Principal</th>
                                <th class="text-end">Ações</th>
                            </tr>
                        </thead>
                        <tbody id="tabela-enderecos-corpo">
                            <tr><td colspan="5" class="text-center text-muted py-3">Nenhum endereço adicionado ainda.</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="d-flex justify-content-end gap-2 mt-4 mb-5">
                <button type="button" class="btn btn-light border px-4" onclick="history.back()">Cancelar</button>
                <button type="submit" class="btn btn-primary px-4"><i class="fa fa-save me-1"></i> Salvar transportadora</button>
            </div>
        </form>
    </div>
    <!-- JavaScript para manipulação da tabela e envio AJAX -->
    <script>
   
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/transportadora.js"></script>   
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>