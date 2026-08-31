<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Cadastro de Empresa</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="bg-light py-4">

    <div class="container-fluid px-4">
        <!-- Cabeçalho da Página -->
        <div class="page-header mb-4">
            <h1 class="h3 fw-bold text-dark mb-1" style="color: #1e293b !important; letter-spacing: -0.5px;">CADASTRO DE EMPRESA</h1>
            <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                <li class="breadcrumb-item active" aria-current="page">Empresa</li>
             </ol>
            </nav>
        </div>

        <form id="formEmpresa">
            <!-- Campo oculto para armazenar o ID interno da filial (chave primária real para edição) -->
            <input type="hidden" id="idFilial" value="">

            <div class="row g-4">
                <!-- COLUNA ESQUERDA: Formulário Principal de Cadastro -->
                <div class="col-lg-9">
                    <div class="card border-0 shadow-sm p-4 rounded-3 bg-white mb-3">
                        <h5 class="fw-bold text-primary mb-4 pb-2 border-bottom d-flex align-items-center gap-2" style="font-size: 1rem; color: #1e3a8a !important;">
                            <i class="fa fa-file-alt text-primary"></i> DADOS DA EMPRESA
                        </h5>
                        
                        <div class="row g-3 mb-3">
                            <div class="col-md-2">
                                <label class="form-label small fw-bold text-secondary">Origem <span class="text-danger">*</span></label>
							    <div class="input-group input-group-sm">
							        <input type="text" id="origemCodigo" class="form-control" required value="" placeholder="Código">
							        <button type="button" class="btn btn-outline-secondary px-2" id="btnBuscarEmpresaModal" title="Pesquisar Empresa">
							            <i class="fa fa-search"></i>
							        </button>
							    </div>
                            </div>
                            <div class="col-md-7">
                                <label class="form-label small fw-bold text-secondary">Nome da Empresa <span class="text-danger">*</span></label>
                                <input type="text" id="nomeEmpresa" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">CNPJ <span class="text-danger">*</span></label>
                                <input type="text" id="cnpj" class="form-control form-control-sm" required value="">
                            </div>
                        </div>

                        <!-- Linha 2: Inscrição Estadual (3) + CEP (2) + Endereço (5) + Número (2) = 12 -->
                        <div class="row g-3 mb-3">
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Inscrição Estadual <span class="text-danger">*</span></label>
                                <input type="text" id="inscricaoEstadual" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-2">
                                <label class="form-label small fw-bold text-secondary">CEP <span class="text-danger">*</span></label>
                                <input type="text" id="cep" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-5">
                                <label class="form-label small fw-bold text-secondary">Endereço <span class="text-danger">*</span></label>
                                <input type="text" id="endereco" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-2">
                                <label class="form-label small fw-bold text-secondary">Número <span class="text-danger">*</span></label>
                                <input type="text" id="numero" class="form-control form-control-sm" required value="">
                            </div>
                        </div>

                        <!-- Linha 3: Bairro (3) + Município (5) + UF (3) + Sufixo (1) = 12 -->
                        <div class="row g-3 mb-3">
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Bairro <span class="text-danger">*</span></label>
                                <input type="text" id="bairro" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-5">
                                <label class="form-label small fw-bold text-secondary">Município <span class="text-danger">*</span></label>
                                <input type="text" id="municipio" class="form-control form-control-sm" required value="">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">UF <span class="text-danger">*</span></label>
                                <select id="uf" class="form-select form-select-sm" required>
                                    <option value="SP - São Paulo" selected>SP - São Paulo</option>
                                    <option value="AC - Acre">AC - Acre</option>
                                    <option value="AL - Alagoas">AL - Alagoas</option>
                                    <option value="AP - Amapá">AP - Amapá</option>
                                    <option value="AM - Amazonas">AM - Amazonas</option>
                                    <option value="BA - Bahia">BA - Bahia</option>
                                    <option value="CE - Ceará">CE - Ceará</option>
                                    <option value="DF - Distrito Federal">DF - Distrito Federal</option>
                                    <option value="ES - Espírito Santo">ES - Espírito Santo</option>
                                    <option value="GO - Goiás">GO - Goiás</option>
                                    <option value="MA - Maranhão">MA - Maranhão</option>
                                    <option value="MT - Mato Grosso">MT - Mato Grosso</option>
                                    <option value="MS - Mato Grosso do Sul">MS - Mato Grosso do Sul</option>
                                    <option value="MG - Minas Gerais">MG - Minas Gerais</option>
                                    <option value="PA - Pará">PA - Pará</option>
                                    <option value="PB - Paraíba">PB - Paraíba</option>
                                    <option value="PR - Paraná">PR - Paraná</option>
                                    <option value="PE - Pernambuco">PE - Pernambuco</option>
                                    <option value="PI - Piauí">PI - Piauí</option>
                                    <option value="RJ - Rio de Janeiro">RJ - Rio de Janeiro</option>
                                    <option value="RN - Rio Grande do Norte">RN - Rio Grande do Norte</option>
                                    <option value="RS - Rio Grande do Sul">RS - Rio Grande do Sul</option>
                                    <option value="RO - Rondônia">RO - Rondônia</option>
                                    <option value="RR - Roraima">RR - Roraima</option>
                                    <option value="SC - Santa Catarina">SC - Santa Catarina</option>
                                    <option value="SP - São Paulo">SP - São Paulo</option>
                                    <option value="SE - Sergipe">Sergipe</option>
                                    <option value="TO - Tocantins">TO - Tocantins</option>
                                </select>
                            </div>
                            <div class="col-md-1">
                                <label class="form-label small fw-bold text-secondary">Sufixo <span class="text-danger">*</span></label>
                                <input type="text" id="sufixo" class="form-control form-control-sm" value="">
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-1">
                                <label class="form-label small fw-bold text-secondary">DDD <span class="text-danger">*</span></label>
                                <input type="text" id="dddTelefone1" class="form-control form-control-sm" maxlength="2" value="">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Telefone <span class="text-danger">*</span></label>
                                <input type="text" id="telefone1" class="form-control form-control-sm" value="">
                            </div>
                            <div class="col-md-1">
                                <label class="form-label small fw-bold text-secondary">DDD</label>
                                <input type="text" id="dddTelefone2" class="form-control form-control-sm" maxlength="2" value="">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Celular</label>
                                <input type="text" id="telefone2" class="form-control form-control-sm" value="">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label small fw-bold text-secondary">Email</label>
                                <input type="email" id="email" class="form-control form-control-sm" value="">
                            </div>
                        </div>

                        <div class="row g-3 mb-4 pt-3 border-top align-items-center">
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Nº Proprietário Familiar</label>
                                <input type="text" class="form-control form-control-sm" value="0">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Atividade Empresa</label>
                                <input type="text" class="form-control form-control-sm" value="0">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small fw-bold text-secondary">Natureza Empresa</label>
                                <input type="text" class="form-control form-control-sm" value="0">
                            </div>
                            <div class="col-md-2">
                                <label class="form-label small fw-bold text-secondary">Unidade</label>
                                <select class="form-select form-select-sm">
                                    <option value="UN" selected>UN</option>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex align-items-end pt-3">
                                <button type="button" class="btn btn-outline-secondary btn-sm w-100 py-1"><i class="fa fa-print"></i></button>
                            </div>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="border rounded p-2 bg-white d-flex align-items-center justify-content-around h-100">
                                    <span class="small fw-bold text-muted">Tipo</span>
                                    <div class="form-check m-0">
                                        <input class="form-check-input" type="radio" name="tipoMovimento" id="tipoEntrada" value="Entrada">
                                        <label class="form-check-label small" for="tipoEntrada">Entrada</label>
                                    </div>
                                    <div class="form-check m-0">
                                        <input class="form-check-input" type="radio" name="tipoMovimento" id="tipoSaida" value="Saída" checked>
                                        <label class="form-check-label small" for="tipoSaida">Saída</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- COLUNA DIREITA: Painel de Informações Estilizado -->
                <div class="col-lg-3">
                    <div class="card border-0 shadow-sm p-3 rounded-3 bg-white">
                        <h6 class="fw-bold text-primary mb-2 d-flex align-items-center gap-2" style="font-size: 0.9rem;">
                            <i class="fa fa-info-circle text-primary"></i> INFORMAÇÕES DA EMPRESA
                        </h6>
                        <p class="text-muted mb-3" style="font-size: 0.75rem; line-height: 1.4;">Preencha os dados cadastrais da empresa com atenção. Essas informações serão utilizadas em todo o sistema.</p>
                        
                        <hr class="text-muted my-2">

                        <div class="d-flex align-start gap-2 py-2 border-bottom">
                            <div class="text-success fs-5 px-1"><i class="fa fa-file-invoice"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Dados Fiscais</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Informações fiscais e tributárias da empresa.</span>
                            </div>
                        </div>

                        <div class="d-flex align-start gap-2 py-2 border-bottom">
                            <div class="text-success fs-5 px-1"><i class="fa fa-map-marker-alt"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Endereço</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Endereço completo para correspondências.</span>
                            </div>
                        </div>

                        <div class="d-flex align-start gap-2 py-2 border-bottom">
                            <div class="text-success fs-5 px-1"><i class="fa fa-phone-alt"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Contatos</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Telefones e e-mail para comunicação.</span>
                            </div>
                        </div>

                        <div class="d-flex align-start gap-2 py-2 border-bottom">
                            <div class="text-success fs-5 px-1"><i class="fa fa-cogs"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Configuração Fiscal</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Definições de entrada/saída e emissão de notas.</span>
                            </div>
                        </div>

                        <div class="d-flex align-start gap-2 py-2 border-bottom">
                            <div class="text-success fs-5 px-1"><i class="fa fa-briefcase"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Dados Comerciais</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Informações comerciais e de atividades.</span>
                            </div>
                        </div>

                        <div class="d-flex align-start gap-2 pt-2">
                            <div class="text-success fs-5 px-1"><i class="fa fa-building"></i></div>
                            <div>
                                <span class="d-block fw-bold text-dark" style="font-size: 0.8rem;">Unidade</span>
                                <span class="text-muted" style="font-size: 0.7rem;">Unidade de negócio da empresa.</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- BARRA INFERIOR DE BOTÕES DE AÇÃO -->
            <div class="card border-0 shadow-sm px-4 py-3 rounded-3 bg-white mt-2 mb-4 d-flex flex-row justify-content-between align-items-center">
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-outline-secondary btn-sm px-3" id="btnCancelar">
					    <i class="fa fa-eraser me-1"></i> Limpar
					</button>
                    <button type="button" class="btn btn-outline-primary btn-sm px-4" id="btnNovo">
                        <i class="fa fa-plus me-1"></i> Novo
                    </button>
                </div>
                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-success btn-sm px-4" id="btnSalvar">
                        <i class="fa fa-save me-1"></i> Salvar
                    </button>
                    <button type="button" class="btn btn-danger btn-sm px-3" id="btnExcluir">
                        <i class="fa fa-trash me-1"></i> Excluir
                    </button>
                </div>
            </div>
        </form>
    </div>

   <!-- ==========================================================
         NEXACORE - MODAL SERVICE (Inclusão obrigatória na página)
    ========================================================== -->

    <!-- Modal de Alerta -->
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

    <!-- Modal de Confirmação -->
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

    <!-- Modal de Pesquisa / Seleção de Empresa -->
    <div class="modal fade" id="modalBuscaEmpresa" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow">
                <div class="modal-header bg-primary text-white py-2">
                    <h5 class="modal-title fs-6 fw-bold"><i class="fa fa-search me-1"></i> Pesquisar Empresa</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fechar"></button>
                </div>
                <div class="modal-body bg-light">
                    <div class="mb-3">
                        <input type="text" id="filtroTextoBusca" class="form-control form-control-sm" placeholder="Digite o nome ou código da empresa para filtrar...">
                    </div>
                    <div class="table-responsive bg-white rounded shadow-sm" style="max-height: 350px; overflow-y: auto;">
                        <table class="table table-hover table-sm align-middle mb-0" id="tabelaResultadosBuscaEmpresa">
                            <thead class="table-light sticky-top">
                                <tr>
                                    <th class="ps-3">Origem</th>
                                    <th>Nome da Empresa</th>
                                    <th>CNPJ</th>
                                    <th class="text-center" style="width: 100px;">Ação</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Preenchido via JavaScript -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Seus scripts existentes -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>   
    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/empresa.js"></script>
</body>
</html>