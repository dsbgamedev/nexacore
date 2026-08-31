<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Cadastro de Produto</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cadastro-produto.css">
</head>
<body class="p-4 bg-light">
<div class="container-fluid mb-4">
    <h4 class="page-title fw-bold text-primary-dark"> CADASTRO DE PRODUTO (CATÁLOGO)</h4>
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0">
           <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
           <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MarcaServlet">Marca</a></li>
           <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/FabricanteServlet">Fabricante</a></li>
           <li class="breadcrumb-item active" aria-current="page">Produtos</li>
            
        </ol>
    </nav>
</div>

<div class="container-fluid">
    <div class="row">
        <div class="col-lg-8">
            <div class="card p-4">
                <div class="card-title fw-bold text-primary mb-3">Dados Básicos</div>
                
                <!-- SEÇÃO FIXA SUPERIOR: Apenas Tipo, SKU e Marca -->
                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label">Tipo de Produto *</label>
                        <select class="form-select form-select-sm" id="select-tipo-produto">
                            <option value="">-- Selecione --</option>
                        </select>
                    </div>
                   
                    <div class="col-md-6">
                        <label class="form-label">Código do Produto (SKU) *</label>
                        <input type="text" id="sku" class="form-control" readonly placeholder="O SKU será gerado automaticamente">
                    </div>
                    
                    <div class="col-12">
					    <label for="marcaId" class="form-label">Marca *</label>
					    <select id="marcaId" class="form-select" required>
					        <option value="">-- Selecione --</option>
					    </select>
					</div>
                </div>

                <!-- SEÇÃO DE ATRIBUTOS DINÂMICOS -->
                <div id="div-campos-dinamicos" class="row g-3 mb-4 pt-2 border-top" style="display: none;">
                    <div class="col-12 text-muted small fw-bold mb-1"><i class="fas fa-sliders-h me-1"></i> Especificações do Tipo</div>
                    <div id="container-atributos-tecnicos" class="row g-3 m-0 p-0"></div>
                </div>

                <!-- MENU DE ABAS -->
                <ul class="nav nav-tabs mb-3">
                    <li class="nav-item"><button class="nav-link active" type="button" data-bs-toggle="tab" data-bs-target="#tab-geral">Informações Gerais</button></li>
                    <li class="nav-item"><button class="nav-link" type="button" data-bs-toggle="tab" data-bs-target="#tab-imagem">Imagem</button></li>
                    <li class="nav-item"><button class="nav-link" type="button" data-bs-toggle="tab" data-bs-target="#tab-obs">Observações</button></li>
                </ul>

                <!-- CONTEÚDO DAS ABAS -->
                <div class="tab-content">
    
                    <!-- ABA 1: Informações Gerais -->
                    <div class="tab-pane fade show active" id="tab-geral">
                        <div class="row g-3">
                            <div class="col-12">
                                <label class="form-label">Descrição Resumida</label>
                                <input type="text" id="desc-resumida" class="form-control form-control-sm" readonly>
                            </div>
                            
                            <div class="col-12">
                                <label class="form-label">Descrição Detalhada</label>
                                <textarea id="desc-detalhada" class="form-control form-control-sm" rows="2"></textarea>
                            </div>
                    
                            <div class="row g-3 m-0 p-0">
                                <div class="col-12">
                                    <label class="form-label">Ativo</label>
                                    <select class="form-select form-select-sm" id="ativo">
                                        <option value="true">Sim</option>
                                        <option value="false">Não</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- ABA 2: Imagem -->
                    <div class="tab-pane fade" id="tab-imagem">
                        <div class="row">
                            <div class="col-md-4">
                                <label class="fw-bold mb-2 text-primary">IMAGEM PRINCIPAL</label>
                                <div class="border rounded p-3 text-center d-flex flex-column align-items-center justify-content-center" style="height: 250px;">
                                    <img id="img-destaque-preview" src="" class="img-fluid mb-3" style="max-height: 150px; display: none;" onerror="this.style.display='none'">
                                    <div class="d-flex gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-primary" id="btn-alterar-destaque"><i class="fa fa-edit me-1"></i> Alterar imagem</button>
                                        <button type="button" class="btn btn-sm btn-outline-danger" id="btn-remover-destaque"><i class="fa fa-trash me-1"></i> Remover</button>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-8">
                                <label class="fw-bold mb-2 text-primary">GALERIA DE IMAGENS</label>
                                <div id="gallery-container" class="d-flex flex-wrap gap-2 border rounded p-3" style="min-height: 250px;">
                                    <div class="border border-dashed rounded p-3 text-center d-flex flex-column align-items-center justify-content-center" 
                                         style="width: 100px; height: 100px; cursor: pointer;"
                                         onclick="document.getElementById('file-upload').click()">
                                        <i class="fa fa-plus text-muted"></i>
                                        <small class="text-muted">Adicionar</small>
                                    </div>
                                    <input type="file" id="file-upload" hidden accept="image/*" multiple>
                                </div>
                                <p class="text-muted small mt-2">Formatos aceitos: JPG, PNG, WebP (Máx. 5MB por arquivo)</p>
                            </div>
                        </div>
                    </div>

                    <!-- ABA 3: Observações -->
                    <div class="tab-pane fade" id="tab-obs">
                        <div class="mt-3" id="container-observacoes">
                            <label class="form-label fw-bold">Observações Internas</label>
                            <textarea class="form-control" 
                                      id="txt-observacoes" 
                                      rows="8" 
                                      maxlength="2000" 
                                      placeholder="Digite aqui as observações internas..."></textarea>
                            <div class="text-muted mt-1" style="font-size: 0.85rem;">
                                <span id="char-count">0</span>/2000 caracteres
                            </div>
                        </div>
                    </div>

                </div><!-- Fim do tab-content -->

                <!-- BOTÕES DE AÇÃO FIXOS -->
                <div class="mt-4 pt-3 border-top">
                    <button type="button" class="btn btn-primary" id="btn-salvar">Salvar Produto</button>
                    <button type="button" class="btn btn-secondary" id="btn-cancelar">Cancelar</button>
                </div>

            </div>
        </div>
        
        <div class="col-lg-4">
		    <div class="card p-3 painel-produto">
		        <h6 class="fw-bold">Campos por Tipo de Produto</h6>
		        <p style="font-size: 11px; color: #6c757d;">Os campos exibidos abaixo são definidos para o tipo selecionado.</p>
		        <div id="container-lista-campos"></div>
		    </div>
	    </div>
    </div>
</div>

<!-- Modais -->
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/produto.js"></script>
</body>
</html>