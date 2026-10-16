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
    <link href="${pageContext.request.contextPath}/assets/css/fornecedores.css" rel="stylesheet">
</head>
<body>
    <div class="nx-layout">
        <!-- Main Content (Awan ti peke a sidebar) -->
        <main class="nx-main w-100 ms-0">
            <header class="nx-topbar">
                <a class="back-link" href="${pageContext.request.contextPath}/FornecedorServlet">
                    <i class="fa-solid fa-arrow-left"></i>
                </a>
                <div class="topbar-actions ms-auto">
                    <i class="fa-regular fa-bell"></i>
                    <i class="fa-regular fa-circle-question"></i>
                </div>
            </header>

            <div class="nx-content container-fluid p-4">
                <div class="nx-breadcrumb mb-3">
                    <i class="fa-solid fa-house"></i>
                    <span>Cadastros</span>
                    <i class="fa-solid fa-chevron-right"></i>
                    <a href="${pageContext.request.contextPath}/FornecedorServlet">Fornecedores</a>
                    <i class="fa-solid fa-chevron-right"></i>
                    <strong><%= fornecedor.getIdFornecedor() == null ? "Novo fornecedor" : "Editar fornecedor" %></strong>
                </div>

                <div class="page-heading mb-4">
                    <div class="page-icon">
                        <i class="fa-solid fa-building"></i>
                    </div>
                    <div>
                        <h1><%= fornecedor.getIdFornecedor() == null ? "Cadastro de Fornecedor" : "Editar Fornecedor" %></h1>
                        <p class="text-muted mb-0">Preencha os dados do fornecedor e selecione as filiais autorizadas a utilizá-lo.</p>
                    </div>
                    <div class="heading-actions ms-auto">
                        <a href="${pageContext.request.contextPath}/FornecedorServlet" class="btn btn-light">
                            <i class="fa-solid fa-xmark me-2"></i>Cancelar
                        </a>
                        <button form="formFornecedor" type="submit" class="btn btn-primary nx-btn">
                            <i class="fa-solid fa-floppy-disk me-2"></i>Salvar fornecedor
                        </button>
                    </div>
                </div>

                <div id="formAlert" class="alert d-none" role="alert"></div>

                <form id="formFornecedor" novalidate>
                    <input type="hidden" name="acao" value="salvar">
                    <input type="hidden" name="idFornecedor" value="<%= fornecedor.getIdFornecedor() == null ? "" : fornecedor.getIdFornecedor() %>">
                    <input type="hidden" name="enderecosJson" id="enderecosJson">

                    <div class="form-layout">
                        <div class="form-main">
                            <!-- Dados Cadastrais -->
                            <section class="panel mb-4">
                                <div class="panel-title">
                                    <div>
                                        <h2><i class="fa-regular fa-id-card me-2"></i>Dados cadastrais</h2>
                                        <p>Identificação fiscal e informações gerais.</p>
                                    </div>
                                </div>
                                <div class="form-grid">
                                    <div class="col-md-3">
									    <label for="idCategoria" class="form-label fw-semibold">Categoria *</label>
									    <select id="idCategoria" name="categoria" class="form-select" required>
									        <option value="">Selecione...</option>
									        <c:forEach var="cat" items="${categorias}">
									            <option value="${cat.nome}" ${fornecedor.categoria == cat.nome ? 'selected' : ''}>${cat.nome}</option>
									        </c:forEach>
									    </select>
									</div>
                                    <div class="field">
                                        <label>CPF / CNPJ <span class="required">*</span></label>
                                        <input class="form-control" name="documento" id="documento" required value="<%= fornecedor.getDocumento() == null ? "" : fornecedor.getDocumento() %>" placeholder="00.000.000/0000-00">
                                    </div>
                                    <div class="field span-2">
                                        <label>Razão social / Nome completo <span class="required">*</span></label>
                                        <input class="form-control" name="razaoSocial" required maxlength="200" value="<%= fornecedor.getRazaoSocial() == null ? "" : fornecedor.getRazaoSocial() %>" placeholder="Nome cadastrado oficialmente">
                                    </div>
                                    <div class="field">
                                        <label>Nome fantasia</label>
                                        <input class="form-control" name="nomeFantasia" maxlength="200" value="<%= fornecedor.getNomeFantasia() == null ? "" : fornecedor.getNomeFantasia() %>">
                                    </div>
                                    <div class="field">
                                        <label>Categoria</label>
                                        <select class="form-select" name="categoria">
                                            <option value="">Selecione...</option>
                                            <%
                                                Integer cat = fornecedor.getIdCategoria();
                                                String[] cats = {"Informática e tecnologia", "Material de escritório", "Peças e componentes", "Serviços", "Mobiliário", "Limpeza e conservação", "Outros"};
                                                for (String c : cats) {
                                            %>
                                                <option value="<%= c %>" <%= c.equals(cat) ? "selected" : "" %>><%= c %></option>
                                            <% } %>
                                        </select>
                                    </div>
                                    <div class="field">
                                        <label>Inscrição estadual</label>
                                        <input class="form-control" name="inscricaoEstadual" value="<%= fornecedor.getInscricaoEstadual() == null ? "" : fornecedor.getInscricaoEstadual() %>">
                                    </div>
                                    <div class="field">
                                        <label>Inscrição municipal</label>
                                        <input class="form-control" name="inscricaoMunicipal" value="<%= fornecedor.getInscricaoMunicipal() == null ? "" : fornecedor.getInscricaoMunicipal() %>">
                                    </div>
                                    <div class="field">
                                        <label>Status <span class="required">*</span></label>
                                        <select class="form-select" name="status">
                                            <option value="ATIVO" <%= "ATIVO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Ativo</option>
                                            <option value="INATIVO" <%= "INATIVO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Inativo</option>
                                            <option value="BLOQUEADO" <%= "BLOQUEADO".equals(fornecedor.getStatus()) ? "selected" : "" %>>Bloqueado</option>
                                        </select>
                                    </div>
                                </div>
                            </section>

                            <!-- Contato -->
                            <section class="panel mb-4">
                                <div class="panel-title">
                                    <div>
                                        <h2><i class="fa-regular fa-address-book me-2"></i>Contato</h2>
                                        <p>Dados para comunicação com o fornecedor.</p>
                                    </div>
                                </div>
                                <div class="form-grid">
                                    <div class="field">
                                        <label>Contato principal</label>
                                        <input class="form-control" name="contatoPrincipal" value="<%= fornecedor.getContatoPrincipal() == null ? "" : fornecedor.getContatoPrincipal() %>" placeholder="Nome do contato">
                                    </div>
                                    <div class="field">
                                        <label>Telefone</label>
                                        <input class="form-control" name="telefone" value="<%= fornecedor.getTelefone() == null ? "" : fornecedor.getTelefone() %>" placeholder="(00) 0000-0000">
                                    </div>
                                    <div class="field">
                                        <label>Celular</label>
                                        <input class="form-control" name="celular" value="<%= fornecedor.getCelular() == null ? "" : fornecedor.getCelular() %>" placeholder="(00) 00000-0000">
                                    </div>
                                    <div class="field">
                                        <label>E-mail</label>
                                        <input type="email" class="form-control" name="email" value="<%= fornecedor.getEmail() == null ? "" : fornecedor.getEmail() %>" placeholder="contato@empresa.com.br">
                                    </div>
                                    <div class="field span-2">
                                        <label>Site</label>
                                        <input type="url" class="form-control" name="site" value="<%= fornecedor.getSite() == null ? "" : fornecedor.getSite() %>" placeholder="https://www.empresa.com.br">
                                    </div>
                                </div>
                            </section>

                            <!-- Endereços -->
                            <section class="panel mb-4">
                                <div class="panel-title">
                                    <div>
                                        <h2><i class="fa-solid fa-location-dot me-2"></i>Endereços</h2>
                                        <p>Cadastre sede, cobrança, entrega ou outros endereços.</p>
                                    </div>
                                    <button type="button" class="btn btn-outline-primary btn-sm" id="addEndereco">
                                        <i class="fa-solid fa-plus me-1"></i>Adicionar endereço
                                    </button>
                                </div>
                                <div id="enderecosContainer"></div>
                                <div id="semEndereco" class="empty-inline">Nenhum endereço adicionado. Use “Adicionar endereço” se necessário.</div>
                            </section>

                            <!-- Observações -->
                            <section class="panel mb-4">
                                <div class="panel-title">
                                    <div>
                                        <h2><i class="fa-regular fa-note-sticky me-2"></i>Observações</h2>
                                        <p>Informações internas para consulta.</p>
                                    </div>
                                </div>
                                <textarea class="form-control" rows="4" name="observacao" maxlength="3000" placeholder="Condições comerciais, informações relevantes..."><%= fornecedor.getObservacao() == null ? "" : fornecedor.getObservacao() %></textarea>
                            </section>
                        </div>

                        <!-- Sidebar lateral do formulário (Filiais autorizadas) -->
                        <aside class="form-side">
                            <section class="panel branch-panel mb-3">
                                <div class="panel-title">
                                    <div>
                                        <h2><i class="fa-solid fa-code-branch me-2"></i>Filiais autorizadas</h2>
                                        <p>Defina em quais filiais este fornecedor estará disponível.</p>
                                    </div>
                                </div>
                                <div class="branch-callout">
                                    <i class="fa-solid fa-circle-info"></i>
                                    <span>O cadastro é único. Marque somente as filiais que podem utilizar este fornecedor.</span>
                                </div>
                                <div class="branch-actions">
                                    <button type="button" class="btn btn-sm btn-light" id="marcarTodas">Marcar todas</button>
                                    <button type="button" class="btn btn-sm btn-light" id="limparFiliais">Limpar seleção</button>
                                </div>
                                <div class="branch-list">
                                    <c:forEach items="${filiais}" var="fl">
                                        <label class="branch-option">
                                            <input class="form-check-input filial-check" type="checkbox" name="filiaisIds" value="${fl.idFilial}">
                                            <span class="branch-code"><c:out value="${fl.sufixo}"/></span>
                                            <span class="branch-info">
                                                <strong><c:out value="${fl.nomeEmpresa}"/></strong>
                                                <small>Origem: <c:out value="${fl.origemCodigo}"/></small>
                                            </span>
                                        </label>
                                    </c:forEach>
                                </div>
                                <div class="branch-footer">
                                    <i class="fa-solid fa-shield-halved"></i>
                                    <span>Se nenhuma filial for marcada, o fornecedor ficará sem liberação para uso nos módulos que aplicarem essa regra.</span>
                                </div>
                            </section>

                            <section class="panel tip-panel">
                                <i class="fa-solid fa-lightbulb"></i>
                                <div>
                                    <strong>Dica</strong>
                                    <p>Antes de salvar, confira o CPF/CNPJ. O sistema não permite duplicar o mesmo documento.</p>
                                </div>
                            </section>
                        </aside>
                    </div>

                    <div class="bottom-actions d-flex justify-content-end gap-2 mb-4">
                        <a class="btn btn-light" href="${pageContext.request.contextPath}/FornecedorServlet">Cancelar</a>
                        <button type="submit" class="btn btn-primary nx-btn">
                            <i class="fa-solid fa-floppy-disk me-2"></i>Salvar fornecedor
                        </button>
                    </div>
                </form>

                <footer class="nx-footer text-center py-3">NexaCore <span>•</span> Cadastro de fornecedores</footer>
            </div>
        </main>
    </div>

    <script>
        window.NEXACORE_CONTEXT = '${pageContext.request.contextPath}';
        window.FORNECEDOR_ENDERECOS = <%= enderecosJson %>;
        window.FORNECEDOR_FILIAIS = <%= filiaisIdsJson %>;
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/fornecedores.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
</body>
</html>