<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="model.Fornecedor,com.google.gson.Gson" %>
<%
    Fornecedor fornecedor = (Fornecedor) request.getAttribute("fornecedor");
    if (fornecedor == null) fornecedor = new Fornecedor();

    String enderecosJson = (String) request.getAttribute("enderecosJson");
    if (enderecosJson == null) enderecosJson = "[]";

    String filiaisIdsJson = (String) request.getAttribute("filiaisIdsJson");
    if (filiaisIdsJson == null) filiaisIdsJson = "[]";
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>NexaCore | Cadastro de Fornecedor</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/layout.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/fornecedores.css" rel="stylesheet">
</head>
<body class="p-4 bg-light">
    <div class="container-fluid mb-4">
        <h4 class="page-title fw-bold text-primary-dark mb-2">
            <%= fornecedor.getIdFornecedor() == null ? "CADASTRO DE FORNECEDOR" : "EDITAR FORNECEDOR" %>
        </h4>
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/FornecedorServlet">Fornecedores</a></li>
                <li class="breadcrumb-item active" aria-current="page"><%= fornecedor.getIdFornecedor() == null ? "Novo fornecedor" : "Editar fornecedor" %></li>
            </ol>
        </nav>
    </div>

    <div class="container-fluid">
        <div id="formAlert" class="alert d-none" role="alert"></div>

        <form id="formFornecedor" novalidate>
            <input type="hidden" name="acao" value="salvar">
            <input type="hidden" name="idFornecedor" value="<%= fornecedor.getIdFornecedor() == null ? "" : fornecedor.getIdFornecedor() %>">
            <input type="hidden" name="enderecosJson" id="enderecosJson">

            <div class="row">
                <!-- Coluna Principal (Esquerda) -->
                <div class="col-lg-8">
                    <!-- Dados Cadastrais -->
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="fw-bold text-primary mb-3">
                            <i class="fa-regular fa-id-card me-2"></i>Dados cadastrais
                        </div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Tipo de Pessoa *</label>
                                <select class="form-select" id="tipoPessoa" name="tipoPessoa">
                                    <option value="PJ" <%= "PJ".equals(fornecedor.getTipoPessoa()) ? "selected" : "" %>>Pessoa Jurídica (CNPJ)</option>
                                    <option value="PF" <%= "PF".equals(fornecedor.getTipoPessoa()) ? "selected" : "" %>>Pessoa Física (CPF)</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Categoria *</label>
                                <select class="form-select" name="idCategoria" required>
                                    <option value="">Selecione...</option>
                                    <c:forEach items="${categorias}" var="cat">
                                        <option value="${cat.idCategoria}" ${fornecedor.idCategoria == cat.idCategoria ? 'selected' : ''}>
                                            <c:out value="${cat.nome}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">CPF / CNPJ *</label>
                                <input class="form-control" name="documento" id="documento" required value="<%= fornecedor.getDocumento() == null ? "" : fornecedor.getDocumento() %>" placeholder="00.000.000/0000-00">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Nome fantasia</label>
                                <input class="form-control" name="nomeFantasia" maxlength="200" value="<%= fornecedor.getNomeFantasia() == null ? "" : fornecedor.getNomeFantasia() %>">
                            </div>
                            <div class="col-12">
                                <label class="form-label">Razão social / Nome completo *</label>
                                <input class="form-control" name="razaoSocial" required maxlength="200" value="<%= fornecedor.getRazaoSocial() == null ? "" : fornecedor.getRazaoSocial() %>" placeholder="Nome cadastrado oficialmente">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Inscrição estadual *</label>
                                <input class="form-control" name="inscricaoEstadual" id="inscricaoEstadual" required maxlength="14" value="<%= fornecedor.getInscricaoEstadual() == null ? "" : fornecedor.getInscricaoEstadual() %>" placeholder="9 a 14 dígitos">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Inscrição municipal *</label>
                                <input class="form-control" name="inscricaoMunicipal" id="inscricaoMunicipal" required maxlength="11" value="<%= fornecedor.getInscricaoMunicipal() == null ? "" : fornecedor.getInscricaoMunicipal() %>" placeholder="Até 11 dígitos">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Status *</label>
                                <select class="form-select" name="status">
                                    <option value="ATIVO" <%= "ATIVO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Ativo</option>
                                    <option value="INATIVO" <%= "INATIVO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Inativo</option>
                                    <option value="BLOQUEADO" <%= "BLOQUEADO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Bloqueado</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- Contato -->
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="fw-bold text-primary mb-3">
                            <i class="fa-regular fa-address-book me-2"></i>Contato
                        </div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Contato principal</label>
                                <input class="form-control" name="contatoPrincipal" value="<%= fornecedor.getContatoPrincipal() == null ? "" : fornecedor.getContatoPrincipal() %>" placeholder="Nome do contato">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">E-mail</label>
                                <input type="email" class="form-control" name="email" value="<%= fornecedor.getEmail() == null ? "" : fornecedor.getEmail() %>" placeholder="contato@empresa.com.br">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Telefone</label>
                                <input class="form-control" name="telefone" id="telefone" maxlength="14" value="<%= fornecedor.getTelefone() == null ? "" : fornecedor.getTelefone() %>" placeholder="(00) 0000-0000">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Celular</label>
                                <input class="form-control" name="celular" id="celular" maxlength="15" value="<%= fornecedor.getCelular() == null ? "" : fornecedor.getCelular() %>" placeholder="(00) 00000-0000">
                            </div>
                            <div class="col-12">
                                <label class="form-label">Site</label>
                                <input type="url" class="form-control" name="site" value="<%= fornecedor.getSite() == null ? "" : fornecedor.getSite() %>" placeholder="https://www.empresa.com.br">
                            </div>
                        </div>
                    </div>

                    <!-- Endereços -->
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div class="fw-bold text-primary">
                                <i class="fa-solid fa-location-dot me-2"></i>Endereços
                            </div>
                            <button type="button" class="btn btn-outline-primary btn-sm" id="addEndereco">
                                <i class="fa-solid fa-plus me-1"></i>Adicionar endereço
                            </button>
                        </div>
                        <div id="enderecosContainer"></div>
                        <div id="semEndereco" class="empty-inline text-muted text-center py-3">Nenhum endereço adicionado. Use “Adicionar endereço” se necessário.</div>
                    </div>

                    <!-- Observações -->
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="fw-bold text-primary mb-3">
                            <i class="fa-regular fa-note-sticky me-2"></i>Observações
                        </div>
                        <textarea class="form-control" rows="4" name="observacao" maxlength="3000" placeholder="Condições comerciais, informações relevantes..."><%= fornecedor.getObservacao() == null ? "" : fornecedor.getObservacao() %></textarea>
                    </div>

                    <!-- Botões de Ação Inferiores -->
                    <div class="d-flex justify-content-end gap-2 mb-4 pt-2 border-top">
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/FornecedorServlet">Cancelar</a>
                        <button type="submit" class="btn btn-primary">
                            <i class="fa-solid fa-floppy-disk me-2"></i>Salvar fornecedor
                        </button>
                    </div>
                </div>

                <!-- Coluna Lateral (Direita - Filiais Autorizadas) -->
                <div class="col-lg-4">
                    <div class="card p-3 shadow-sm mb-3">
                        <h6 class="fw-bold text-primary mb-1">
                            <i class="fa-solid fa-code-branch me-2"></i>Filiais autorizadas
                        </h6>
                        <p class="text-muted" style="font-size: 11px;">Defina em quais filiais este fornecedor estará disponível.</p>
                        
                        <div class="alert alert-info p-2 mb-2" style="font-size: 12px;">
                            <i class="fa-solid fa-circle-info me-1"></i> O cadastro é único. Marque somente as filiais que podem utilizar este fornecedor.
                        </div>

                        <div class="d-flex gap-2 mb-2">
                            <button type="button" class="btn btn-sm btn-light border w-50" id="marcarTodas">Marcar todas</button>
                            <button type="button" class="btn btn-sm btn-light border w-50" id="limparFiliais">Limpar seleção</button>
                        </div>

                        <div class="branch-list border rounded p-2 overflow-auto" style="max-height: 400px;">
                            <c:forEach items="${filiais}" var="fl">
                                <label class="branch-option d-flex align-items-center gap-2 p-1 border-bottom">
                                    <input class="form-check-input filial-check mt-0" type="checkbox" name="filiaisIds" value="${fl.idFilial}">
                                    <span class="badge bg-secondary"><c:out value="${fl.sufixo}"/></span>
                                    <div class="d-flex flex-column" style="font-size: 12px;">
                                        <strong><c:out value="${fl.nomeEmpresa}"/></strong>
                                        <small class="text-muted">Origem: <c:out value="${fl.origemCodigo}"/></small>
                                    </div>
                                </label>
                            </c:forEach>
                        </div>
                        
                        <div class="text-muted mt-2" style="font-size: 11px;">
                            <i class="fa-solid fa-shield-halved me-1"></i> Se nenhuma filial for marcada, o fornecedor ficará sem liberação.
                        </div>
                    </div>

                    <div class="card p-3 shadow-sm bg-light border-0">
                        <div class="d-flex gap-2 align-items-start">
                            <i class="fa-solid fa-lightbulb text-warning fs-5 mt-1"></i>
                            <div style="font-size: 12px;">
                                <strong>Dica</strong>
                                <p class="mb-0 text-muted">Antes de salvar, confira o CPF/CNPJ. O sistema não permite duplicar o mesmo documento.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>

        <footer class="text-center py-3 text-muted" style="font-size: 13px;">NexaCore <span>•</span> Cadastro de fornecedores</footer>
    </div>

    <script>
        window.NEXACORE_CONTEXT = '${pageContext.request.contextPath}';
        window.FORNECEDOR_ENDERECOS = <%= enderecosJson %>;
        window.FORNECEDOR_FILIAIS = <%= filiaisIdsJson %>;
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/fornecedores.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>