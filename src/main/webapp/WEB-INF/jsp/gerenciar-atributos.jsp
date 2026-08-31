<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Engenharia de Atributos - Nexacore</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/layout.css" rel="stylesheet">
</head>
<!-- ... (cabeçalho mantido) ... -->
<body>

<div class="container-fluid py-4">
	<!-- Título e Introdução -->
    <div class="d-flex justify-content-between align-items-center mb-4">
         <div>
              <h4 class="mb-1 text-dark"><i class="fa-solid fa-sliders text-primary me-2"></i>Nexacore - Engenharia de Atributos</h4>
            <p class="text-muted small mb-0">Modelagem de formulários dinâmicos e especificações técnicas.</p>
              <nav aria-label="breadcrumb">
                  <ol class="breadcrumb mb-0">
                      <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                      <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MarcaServlet">Marca</a></li>
                      <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ProdutoServlet">Cadastro Produto</a></li>
                      <li class="breadcrumb-item active" aria-current="page">Engenharia de Atributos</li>
                  </ol>
              </nav>
          </div>
    </div>

    <div class="row g-4">
    	<!-- Sidebar: Tipos de Produto -->
        <div class="col-md-3">
            <div class="card h-100">
                <div class="card-header bg-white fw-bold text-secondary pt-3 d-flex justify-content-between align-items-center">
				    <span><i class="fa-solid fa-boxes-stacked me-2"></i>Tipos de Produto</span>
				    <div>
				        <!-- Novo botão de excluir -->
				        <button class="btn btn-sm btn-outline-danger me-1" id="btn-excluir-tipo" disabled title="Excluir tipo selecionado">
				            <i class="fa-solid fa-minus"></i>
				        </button>
				        <!-- Botão de adicionar existente -->
				        <button class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#modalNovoTipo">
				            <i class="fa-solid fa-plus"></i>
				        </button>
				    </div>
				</div>
                <div class="card-body p-2">
                    <div class="list-group list-group-flush" id="lista-tipos"></div>
                </div>
            </div>
        </div>
        
		<!-- Área Central: Atributos e Ações -->
        <div class="col-md-9">
            <div class="card">
                <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                    <div>
                        <span class="text-muted small d-block">Configurando campos para:</span>
                        <h5 class="mb-0 text-dark fw-bold" id="nome-tipo-selecionado">Selecione um tipo</h5>
                    </div>                  
                    
					    <button class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#modalVincularCampo">
					        <i class="fa-solid fa-link me-1"></i> Vincular Atributo
					    </button>
					    <button class="btn btn-success btn-sm" data-bs-toggle="modal" data-bs-target="#modalNovoAtributo">
					        <i class="fa-solid fa-plus me-1"></i> Novo Atributo
					    </button>
					    <!-- Botão para abrir o modal -->
						<button class="btn btn-danger btn-sm" onclick="abrirModalExclusao()">- Excluir Atributos</button>
					    
					
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle border-top" id="tabela-atributos">
                            <thead>
                                <tr>
                                    <th>Ordem</th>
                                    <th>Atributo / Label</th>
                                    <th>Grupo Visual</th>
                                    <th>Tipo de Componente</th>
                                    <th>Obrigatório</th>
                                    <th width="100" class="text-end">Ações</th>
                                </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Vincular -->
<div class="modal fade" id="modalVincularCampo" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Vincular Novo Atributo</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="formVincular">
                    <input type="hidden" name="tipoId" id="modalTipoId">
                    <div class="mb-3">
                        <label class="form-label">Selecione o Atributo</label>
                        <select class="form-select" id="selectAtributos" name="atributoId"></select>
                    </div>
                    <div class="row">
                        <div class="mb-3">
						    <label class="form-label">Grupo Visual</label>
						    <select class="form-select" id="grupoSelect" name="grupoId">
						        <!-- Preenchido via AJAX -->
						    </select>
						</div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Tipo de Componente</label>
                            <select class="form-select" id="tipoDado">
                                <option value="TEXT">Texto</option>
                                <option value="NUMBER">Número</option>
                            </select>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Tamanho</label>
                            <input type="number" class="form-control" id="tamanho" value="255">
                        </div>
                          <div class="col-md-6 mb-3">
                            <label class="form-label">Obrigatório</label>
                            <select class="form-select" id="obrigatorio">
                                <option value="true">Sim</option>
                                <option value="false">Não</option>
                            </select>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" onclick="salvarVinculo()">Confirmar Vínculo</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Novo Tipo -->
<div class="modal fade" id="modalNovoTipo" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Cadastrar Novo Tipo</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="formNovoTipo">
                    <div class="mb-3">
                        <label>Nome do Tipo</label>
                        <input type="text" name="nome" class="form-control" required>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="salvarTipo()">Salvar</button>
            </div>
        </div>
    </div>
</div>

<!-- O Modal -->
<div class="modal fade" id="modalExcluirAtributos" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Remover Atributos</h5>
            </div>
            <div class="modal-body">
                <div id="lista-atributos-exclusao" class="list-group">
                    <!-- Preenchido via JS -->
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>
                <button class="btn btn-danger" onclick="confirmarExclusaoAtributos()">Confirmar Exclusão</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Novo Atributo -->
<div class="modal fade" id="modalNovoAtributo" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Cadastrar Novo Atributo</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="formNovoAtributo">
                    <div class="mb-3">
                        <label class="form-label">Nome do Atributo</label>
                        <input type="text" id="nomeNovoAtributo" class="form-control" required>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" onclick="salvarNovoAtributo()">Salvar Atributo</button>
            </div>
        </div>
    </div>
</div>

<!-- Modais do Sistema (Organizados no final do body) -->
<jsp:include page="modal-service.jsp" />

<!-- Scripts de terceiros e customizados -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.min.js"></script>
<!-- Importando o seu novo serviço -->
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/atributo.js"></script>
</body>
</html>