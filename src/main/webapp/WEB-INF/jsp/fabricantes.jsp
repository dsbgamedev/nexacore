<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Fabricantes</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cadastro-produto.css">
</head>
<body class="p-4 bg-light">

    <!-- ================= VIEW 1: LISTAGEM E FILTROS ================= -->
    <div id="view-listagem">
        <div class="container-fluid mb-4">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h4 class="page-title text-uppercase fw-bold">Fabricantes</h4>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MarcaServlet">Marca</a></li>
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ProdutoServlet">Cadastro Produto</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Fabricantes</li>
                        </ol>
                    </nav>
                </div>
                <button class="btn btn-success btn-sm" id="btn-novo">
                    <i class="fa fa-plus me-1"></i> Novo Fabricante
                </button>
            </div>
        </div>

        <div class="container-fluid">
            <!-- Filtros de Pesquisa -->
            <div class="card p-4 mb-4 shadow-sm">
                <div class="card-title fw-bold text-primary mb-3"><i class="fa fa-filter me-1"></i> Filtros de Pesquisa</div>
                <div class="row g-3 align-items-end">
                    <div class="col-md-5">
                        <label class="form-label">Razão Social / CNPJ</label>
                        <input type="text" id="filtro-nome" class="form-control form-control-sm" placeholder="Digite a razão social ou CNPJ...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Situação</label>
                        <select id="filtro-situacao" class="form-select">
					        <option value="">Selecione...</option>
					        <option value="todos">Todos</option>
					        <option value="true">Ativo</option>
					        <option value="false">Inativo</option>
					    </select>
                    </div>
                    <div class="col-md-4 d-flex gap-2">
                        <button class="btn btn-secondary btn-sm flex-fill" id="btn-limpar-filtros">
                            <i class="fa fa-rotate-left me-1"></i> Limpar Filtros
                        </button>
                        <button class="btn btn-primary btn-sm flex-fill" id="btn-pesquisar">
                            <i class="fa fa-search me-1"></i> Pesquisar
                        </button>
                    </div>
                </div>
            </div>

            <!-- Tabela de Resultados -->
            <div class="card p-4 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="card-title fw-bold text-primary m-0"><i class="fa fa-list me-1"></i> Fabricantes Cadastrados</div>
                    <span class="badge bg-primary px-3 py-2" id="contador-registros">0 registros encontrados</span>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 80px;">ID</th>
                                <th>Razão Social</th>
                                <th>CNPJ</th>
                                <th>País de Origem</th>
                                <th style="width: 120px;">Situação</th>
                                <th style="width: 160px;">Data de Cadastro</th>
                                <th style="width: 130px;" class="text-center">Ações</th>
                            </tr>
                        </thead>
                        <tbody id="tabela-corpo">
                            <!-- Preenchido via JavaScript -->
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- ================= VIEW 2: CADASTRO / EDIÇÃO ================= -->
    <div id="view-formulario" style="display: none;">
        <div class="container-fluid mb-4">
            <h4 class="page-title text-uppercase fw-bold" id="form-titulo">Editar Cadastro de Fabricante</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/ProdutoServlet">Cadastro Produto</a></li>
                    <li class="breadcrumb-item"><a href="#" id="link-voltar-breadcrumb">Fabricantes</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Gerenciamento</li>
                </ol>
            </nav>
        </div>

        <div class="container-fluid">
            <div class="card p-4 shadow-sm">
                <div class="card-title fw-bold text-primary mb-3"><i class="fa fa-info-circle me-1"></i> Dados do Fabricante</div>
                
                <input type="hidden" id="input-id">

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label class="form-label">Razão Social *</label>
                        <input type="text" id="input-razaosocial" class="form-control form-control-sm" placeholder="Ex: Dell Computadores do Brasil Ltda">
                    </div>
                    <div class="col-md-3">
					    <label class="form-label">CNPJ *</label>
					    <!-- Aumentado o maxlength para 18 para caber a pontuação: 00.000.000/0000-00 -->
					    <input type="text" id="input-cnpj" class="form-control form-control-sm" maxlength="18" placeholder="00.000.000/0000-00">
					</div>
                    <div class="col-md-3">
					    <label class="form-label">Inscrição Estadual</label>
					    <!-- Aumentado para 30 para permitir pontos, traços ou letras dependendo do estado -->
					    <input type="text" id="input-ie" class="form-control form-control-sm" maxlength="30" placeholder="Ex: 000.000.000.000">
					</div>
                    
                    <!-- Bloco de Endereço e Logística -->
                    <div class="col-md-2">
					    <label class="form-label">CEP</label>
					    <input type="text" id="input-cep" class="form-control form-control-sm" maxlength="9" placeholder="00000-000">
					</div>
                    <div class="col-md-6">
                        <label class="form-label">Logradouro</label>
                        <input type="text" id="input-logradouro" class="form-control form-control-sm" placeholder="Ex: Av. Industrial">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">Número</label>
                        <input type="text" id="input-numero" class="form-control form-control-sm" placeholder="Ex: 1000">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">Complemento</label>
                        <input type="text" id="input-complemento" class="form-control form-control-sm" placeholder="Galpão 2">
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Bairro</label>
                        <input type="text" id="input-bairro" class="form-control form-control-sm" placeholder="Ex: Centro">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Cidade</label>
                        <input type="text" id="input-cidade" class="form-control form-control-sm" placeholder="Ex: São Paulo">
                    </div>
                    <div class="col-md-2">
					    <label class="form-label">Estado (UF)</label>
					    <select id="input-estado" class="form-select form-select-sm">
					        <option value="">Selecione...</option>
					        <option value="AC">AC</option>
					        <option value="AL">AL</option>
					        <option value="AP">AP</option>
					        <option value="AM">AM</option>
					        <option value="BA">BA</option>
					        <option value="CE">CE</option>
					        <option value="DF">DF</option>
					        <option value="ES">ES</option>
					        <option value="GO">GO</option>
					        <option value="MA">MA</option>
					        <option value="MT">MT</option>
					        <option value="MS">MS</option>
					        <option value="MG">MG</option>
					        <option value="PA">PA</option>
					        <option value="PB">PB</option>
					        <option value="PR">PR</option>
					        <option value="PE">PE</option>
					        <option value="PI">PI</option>
					        <option value="RJ">RJ</option>
					        <option value="RN">RN</option>
					        <option value="RS">RS</option>
					        <option value="RO">RO</option>
					        <option value="RR">RR</option>
					        <option value="SC">SC</option>
					        <option value="SP">SP</option>
					        <option value="SE">SE</option>
					        <option value="TO">TO</option>
					    </select>
					</div>
                    <div class="col-md-2">
                        <label class="form-label">País de Origem</label>
                        <input type="text" id="input-pais" class="form-control form-control-sm" value="Brasil" placeholder="Brasil">
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">Situação *</label>
                        <select class="form-select form-select-sm" id="input-ativo">
                            <option value="true">Ativo</option>
                            <option value="false">Inativo</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Data de Cadastro</label>
                        <input type="text" id="input-datacadastro" class="form-control form-control-sm" readonly placeholder="Gerada automaticamente">
                    </div>
                </div>

                <div class="d-flex justify-content-between pt-3 border-top">
                    <button class="btn btn-outline-secondary btn-sm px-4" id="btn-voltar">
                        <i class="fa fa-arrow-left me-1"></i> Voltar
                    </button>
                    <div class="d-flex gap-2">
                        <button class="btn btn-danger btn-sm px-4" id="btn-excluir" style="display: none;">
                            <i class="fa fa-trash me-1"></i> Excluir
                        </button>
                        <button class="btn btn-primary btn-sm px-4" id="btn-salvar-novo">
                            <i class="fa fa-plus-circle me-1"></i> Salvar e Novo
                        </button>
                        <button class="btn btn-success btn-sm px-4" id="btn-salvar">
                            <i class="fa fa-save me-1"></i> Salvar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modais do Sistema -->
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
    <script src="${pageContext.request.contextPath}/assets/js/fabricante.js"></script>
</body>
</html>