<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Cadastro de Equipamento</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="p-4 bg-light">

    <div class="container-fluid mb-4">
        <h4 class="page-title text-uppercase fw-bold">Cadastro de Equipamento (Unidade Física)</h4>
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="#">Home</a></li>
                <li class="breadcrumb-item"><a href="#">Equipamentos</a></li>
                <li class="breadcrumb-item active" aria-current="page">Novo Equipamento</li>
            </ol>
        </nav>
    </div>

    <div class="container-fluid">
        <div class="row">
            <!-- COLUNA ESQUERDA: Busca de Produto, Imagem e Especificações -->
            <div class="col-md-4">
                <div class="card p-4 shadow-sm mb-4">
                    <h5 class="fw-bold text-primary mb-3">Selecione o Produto (Catálogo)</h5>
                    
                    <div class="mb-3 position-relative">
                        <label class="form-label">Pesquisar Produto *</label>
                        <div class="input-group input-group-sm">
                            <input type="text" id="input-busca-produto" class="form-control" placeholder="Digite o código ou modelo...">
                            <!-- Botão de Lupa -->
                            <button class="btn btn-outline-secondary" type="button" id="btn-abrir-modal-produto" title="Pesquisar no Catálogo">
                                <i class="fa fa-search"></i>
                            </button>
                            <!-- Botão de Limpar/Refresh da Pesquisa -->
                            <button class="btn btn-outline-danger" type="button" id="btn-limpar-busca" title="Limpar Seleção do Produto">
                                <i class="fa fa-sync-alt"></i>
                            </button>
                        </div>
                        <!-- Lista flutuante de autocomplete -->
                        <div id="lista-autocomplete" class="list-group position-absolute w-100 shadow-sm" style="z-index: 1000; max-height: 200px; overflow-y: auto; display: none;"></div>
                        <input type="hidden" id="input-id-produto">
                    </div>

                    <!-- Container para exibir a Imagem do Produto com Carrossel e Zoom -->
                    <div class="text-center mb-3 p-2 bg-light rounded border" id="container-img-produto" style="display: none;">
                        <div class="position-relative px-4">
                            <!-- Botão Anterior -->
                            <button class="position-absolute top-50 start-0 translate-middle-y btn btn-dark btn-sm rounded-circle shadow-sm" id="btn-prev-img" type="button" style="z-index: 5; width: 28px; height: 28px; display: none;">
                                <i class="fa fa-chevron-left fa-xs"></i>
                            </button>

                            <!-- Imagem Principal -->
                            <img id="img-produto" src="" alt="Foto do Produto" class="img-fluid rounded" style="max-height: 120px; object-fit: contain; cursor: zoom-in;" title="Clique para ampliar">

                            <!-- Botão Próximo -->
                            <button class="position-absolute top-50 end-0 translate-middle-y btn btn-dark btn-sm rounded-circle shadow-sm" id="btn-next-img" type="button" style="z-index: 5; width: 28px; height: 28px; display: none;">
                                <i class="fa fa-chevron-right fa-xs"></i>
                            </button>
                        </div>
                        <!-- Indicador / Contador de Imagens -->
                        <div id="indicador-imagens" class="badge bg-secondary text-white mt-2 small" style="display: none;">1 / 1</div>
                    </div>

                    <div class="p-3 bg-light rounded border">
                        <h6 class="fw-bold text-success small mb-3"><i class="fa fa-info-circle me-1"></i> Informações do Produto</h6>
                        <p class="mb-1 small"><strong>Tipo:</strong> <span id="info-tipo">-</span></p>
                        <p class="mb-1 small"><strong>Marca:</strong> <span id="info-marca">-</span></p>
                        <p class="mb-1 small"><strong>Modelo:</strong> <span id="info-modelo">-</span></p>
                        <p class="mb-0 small"><strong>Detalhes:</strong> <span id="info-detalhes">-</span></p>
                    </div>
                </div>
            </div>

            <!-- COLUNA DIREITA: Dados da Unidade Física -->
            <div class="col-md-8">
                <div class="card p-4 shadow-sm mb-4">
                    <h5 class="fw-bold text-primary mb-3">Dados da Unidade (Equipamento)</h5>
                    
                    <input type="hidden" id="input-id">

                    <div class="row g-3 mb-3" id="form-dados-equipamento">
                        <div class="col-md-4">
                            <label class="form-label">ID Sistema *</label>
                            <input type="text" id="input-idsistema" class="form-control form-control-sm bg-white" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Patrimônio / Etiqueta *</label>
                            <input type="text" id="input-patrimonio" class="form-control form-control-sm" placeholder="Ex: 001548">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Número de Série</label>
                            <input type="text" id="input-numeroserie" class="form-control form-control-sm" placeholder="Ex: ABC123456789">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Nome / Identificador do Ativo *</label>
                            <input type="text" id="input-nomeidentificador" class="form-control form-control-sm" placeholder="Ex: DESK253 ou IMP-HP-01">
                            <div class="form-text text-muted" style="font-size: 0.75rem;">Identificador único na rede/empresa</div>
                        </div>
						                        <div class="col-md-6">
						    <label class="form-label">Origem (Filial) *</label>
						    <select class="form-select form-select-sm" id="input-origem" required>
						        <option value="">Carregando origens...</option>
						    </select>
						</div>

                        <div class="col-md-4">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <label class="form-label mb-0">IP Atual</label>
                                <div class="form-check form-switch m-0" style="min-height: auto;">
                                    <input class="form-check-input" type="checkbox" id="check-possui-ip" checked title="Marque se o equipamento possui IP">
                                    <label class="form-check-label small text-muted" for="check-possui-ip" style="font-size: 0.7rem;">Possui IP</label>
                                </div>
                            </div>
                            <input type="text" id="input-ip" class="form-control form-control-sm" placeholder="Ex: 192.168.0.50">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Status Atual *</label>
                            <select class="form-select form-select-sm" id="input-status">
                                <option value="Ativo">Ativo</option>
                                <option value="Em Manutenção">Em Manutenção</option>
                                <option value="Inativo">Inativo</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Usuário Atual</label>
                            <input type="text" id="input-usuario" class="form-control form-control-sm" placeholder="Ex: Carlos Alberto">
                        </div>

                        <div class="col-md-12">
                            <label class="form-label">Departamento / Setor</label>
                            <select id="input-departamento" class="form-select form-select-sm">
                                <option value="">Selecione o setor...</option>
                                <!-- Preenchido dinamicamente via JavaScript -->
                            </select>
                        </div>

                        <div class="col-md-12">
                            <label class="form-label">Observações</label>
                            <textarea id="input-observacoes" class="form-control form-control-sm" rows="2" placeholder="Detalhes adicionais sobre o equipamento..."></textarea>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between pt-3 border-top">
                        <button class="btn btn-outline-secondary btn-sm px-4" id="btn-voltar">
                            <i class="fa fa-arrow-left me-1"></i> Voltar
                        </button>
                        <div>
                            <!-- Botão Limpar Formulário -->
                            <button class="btn btn-outline-danger btn-sm px-3 me-2" id="btn-limpar-form">
                                <i class="fa fa-eraser me-1"></i> Limpar
                            </button>
                            <!-- Botão Salvar Equipamento -->
                            <button class="btn btn-primary btn-sm px-4" id="btn-salvar">
                                <i class="fa fa-save me-1"></i> Salvar Equipamento
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ==========================================================
         MODAL DE SELEÇÃO DE PRODUTOS (VIA LUPA)
    ========================================================== -->
    <div class="modal fade" id="modalBuscaProduto" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title fs-6"><i class="fa fa-boxes me-2"></i> Selecionar Produto do Catálogo</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fechar"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <input type="text" id="modal-input-filtro" class="form-control form-control-sm" placeholder="Filtrar por código, marca, modelo ou tipo...">
                    </div>
                    <div class="table-responsive" style="max-height: 350px; overflow-y: auto;">
                        <table class="table table-hover table-sm align-middle small">
                            <thead class="table-light sticky-top">
                                <tr>
                                    <th>Código / SKU</th>
                                    <th>Tipo</th>
                                    <th>Marca</th>
                                    <th>Modelo</th>
                                    <th class="text-center">Ação</th>
                                </tr>
                            </thead>
                            <tbody id="modal-tabela-produtos">
                                <!-- Preenchido dinamicamente via JS -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ==========================================================
         MODAL DE ZOOM DA IMAGEM
    ========================================================== -->
    <div class="modal fade" id="modalZoomImagem" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-transparent border-0 shadow-none">
                <div class="modal-header border-0 pb-0 justify-content-end">
				    <button type="button" class="btn-close bg-white p-2 rounded-circle shadow" data-bs-dismiss="modal" aria-label="Fechar" style="opacity: 1;"></button>
				</div>
                <div class="modal-body text-center p-0">
                    <img id="img-zoom-modal" src="" class="img-fluid rounded shadow-lg" style="max-height: 80vh; object-fit: contain;">
                </div>
            </div>
        </div>
    </div>

    <!-- ==========================================================
         MODAIS PADRÕES DO MODAL SERVICE (ALERTA E CONFIRMAÇÃO)
    ========================================================== -->
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
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/equipamento.js"></script>
</body>
</html>