<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    if (session.getAttribute("usuarioLogado") == null) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet?message=session_expired");
        return;
    }
%>

<!DOCTYPE html>
<html lang="pt-br">
<head> 
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lançamento de Fretes - NexaCore</title>
    
    <!-- Bootstrap 5 (Necessário para o grid col-md-3 e col-md-6 funcionarem) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Ícones -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- Estilos NexaCore -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <!-- <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/estilo.css"> -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/estiloFrete.css">    
</head>

<body class="bg-light" data-app-context-path="${pageContext.request.contextPath}">
    <!-- 1. MENU LATERAL -->
    <jsp:include page="/WEB-INF/jsp/layout/sidebar.jsp" />

    <!-- CONTEÚDO PRINCIPAL -->
    <main id="main-content" class="nexacore-main">
        
        <!-- 2. TOPBAR -->
        <jsp:include page="/WEB-INF/jsp/layout/topbar.jsp" />

        <div class="container-fluid py-3 px-4">
            
            <!-- BANNER ESCURO DE CABEÇALHO (Padrão Imagem 4) -->
            <div class="nexacore-header-banner">
                <h4 class="fw-bold mb-1 d-flex align-items-center gap-2">
                    <i class="fas fa-truck-loading"></i> LANÇAMENTO DE FRETES (CONTÍNUO)
                </h4>
                <div class="breadcrumb-text">
                    Home &nbsp;/&nbsp; Frete &nbsp;/&nbsp; <span class="text-white">Lançar Frete</span>
                </div>
                <hr class="border-secondary opacity-25 my-3">
                <div class="d-flex flex-wrap gap-2">
                    <a href="${pageContext.request.contextPath}/ConsultaFreteServlet" class="btn btn-primary btn-sm px-3 py-2 fw-semibold">
                        <i class="fas fa-search me-1"></i> Consultar Fretes
                    </a>
                    <a href="${pageContext.request.contextPath}/MenuServlet" class="btn btn-dark border-secondary btn-sm px-3 py-2 fw-semibold">
                        <i class="fas fa-home me-1"></i> Menu
                    </a>
                </div>
            </div>

            <div id="alertContainer"></div>

            <!-- CARD 1: FORMULÁRIO DE LANÇAMENTO (Disposição exata da Imagem 3) -->
            <div class="nexacore-card">
                <div class="nexacore-section-title">
                    <i class="fas fa-edit"></i> DADOS DO LANÇAMENTO
                </div>

                <form id="formFrete" data-action-url="${pageContext.request.contextPath}/LancarFreteServlet" method="post">
                    <input type="hidden" id="idFrete" name="idFrete" value="">
                    <input type="hidden" id="usuarioUnidadeId" value="${sessionScope.usuarioLogado.unidadeAtivaId}">
                    <input type="hidden" id="valorFreteHidden" name="valorFrete" value="0.00" />
                    
                    <!-- LINHA 1: 4 COLUNAS (Transportadora, Nota Fiscal, Volume, Observação) -->
                    <div class="row">
                        <div class="col-md-3 mb-3 position-relative">
                            <label for="transportadoraBusca" class="form-label">Transportadora:</label>
                            <input type="text" id="transportadoraBusca" class="form-control maiusculo" placeholder="Digite para buscar..." autocomplete="off" required>
                            <input type="hidden" id="transportadoraId" name="transportadoraId">
                            <div id="listaSugestoes" class="list-group"></div>
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="notaFiscal" class="form-label">Nota Fiscal:</label>
                            <input type="text" id="notaFiscal" name="notaFiscal" class="form-control maiusculo" required ondragstart="return false;">
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="volume" class="form-label">Volume (Itens):</label>
                            <input type="number" id="volume" name="volume" class="form-control maiusculo" value="1" required min="1" ondragstart="return false;">
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="observacao" class="form-label">Observação:</label>
                            <input type="text" id="observacao" name="observacao" class="form-control maiusculo" ondragstart="return false;">
                        </div>
                    </div>
                    
                    <!-- LINHA 2: 4 COLUNAS (Empresa, Cliente, Peso Líquido, Valor da NF) -->
                    <div class="row">
                        <div class="col-md-3 mb-3">
                            <label for="nomeEmpresa" class="form-label">Empresa:</label>
                            <input type="text" id="nomeEmpresa" name="nomeEmpresa" class="form-control maiusculo" value="${nomeEmpresaGlobal}" required ondragstart="return false;">
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="nomeCliente" class="form-label">Cliente:</label>
                            <input type="text" id="nomeCliente" name="nomeCliente" class="form-control maiusculo" required ondragstart="return false;">
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="pesoLiquido" class="form-label">Peso Líquido (kg):</label>
                            <input type="text" id="pesoLiquido" name="pesoLiquido" class="form-control money-mask text-end" placeholder="Ex: 15,50" required ondragstart="return false;" maxlength="11">
                        </div>

                        <div class="col-md-3 mb-3">
                            <label for="valorNf" class="form-label">Valor da NF (R$):</label>
                            <input type="text" id="valorNf" name="valorNf" class="form-control money-mask text-end" placeholder="Ex: 500,00" required ondragstart="return false;" maxlength="11">
                        </div>
                    </div>

                    <!-- TERCEIRA LINHA APENAS PARA O FRETE -->
                    <div class="row">
						    <div class="col-md-3 mb-3" id="containerPorcentagem" style="display: none;">
						        <label for="porcentagemFrete" class="form-label">
						            % Frete:
						            <span id="labelTravaMatriz" style="display: none; font-size: 0.8rem; margin-left: 10px;">
						                <input type="checkbox" id="chkAlterarPorcentagem"> 
						                <i class="fas fa-lock text-muted" id="iconeCadeado"></i> Alterar
						            </span>
						        </label>
						        <input type="text" id="porcentagemFrete" name="porcentagemFrete" class="form-control money-mask text-end" value="2,00">
						    </div>
						</div>
                    
                    <input type="hidden" id="valorFreteHidden" name="valorFrete" value="0.00" />
                    
                   <!-- LINHA 4: BOTÕES COMPACTOS ALINHADOS À DIREITA (Igual à referência) -->
					                    <!-- LINHA 4: BOTÕES COMPACTOS ALINHADOS À DIREITA -->
					<hr class="my-3 text-muted opacity-25">
					<div class="d-flex flex-wrap justify-content-end gap-2">
					    <button type="button" id="btnLimparCancelar" class="btn btn-outline-secondary btn-sm px-3 py-2 fw-semibold">
					        <i class="fas fa-eraser me-1"></i> Limpar Formulário
					    </button>
					    <button type="submit" id="btnSalvarAtualizar" class="btn btn-success btn-sm px-3 py-2 fw-semibold">
					        <i class="fas fa-check me-1"></i> <span id="btnText">Inserir na Lista e Salvar</span>
					    </button>
					</div>
                </form>
            </div>
            
            <!-- CARD 2: FRETES LANÇADOS (Disposição da Imagem 3 + Visual da Imagem 4) -->
            <div class="nexacore-card">
                <div class="nexacore-section-title">
                    <i class="fas fa-list-ul"></i> FRETES LANÇADOS
                </div>
                
                <!-- BARRA DE PESQUISA E BOTÃO LIMPAR PESQUISA LADO A LADO (50% / 50%) -->
				               <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
				    <div class="input-group search-group flex-grow-1" style="min-width: 260px;">
				        <span class="input-group-text bg-white text-secondary"><i class="fas fa-search"></i></span>
				        <input type="text" id="inputPesquisa" class="form-control maiusculo" placeholder="Pesquisar por Cliente, NF, Transportadora, etc...">
				    </div>
				    <button type="button" id="btnLimparPesquisa" class="btn btn-outline-primary btn-sm px-3 fw-semibold text-nowrap" style="height: 38px;">
				        <i class="fas fa-redo me-1"></i> Limpar Pesquisa (Fretes do Dia)
				    </button>
				</div>

                <input type="hidden" id="totalRegistrosJSP" value="${totalRegistrosJSP}" />
                <input type="hidden" id="registrosPorPaginaJSP" value="${registrosPorPaginaJSP}" />
                <input type="hidden" id="paginaAtualJSP" value="${paginaAtualJSP}" />
                <input type="hidden" id="fretesIniciaisJsonInput" value='${fretesIniciaisJsonInput}' />

                <!-- TABELA ESTILIZADA NO PADRÃO ESCURO (#0f172a) -->
                <div class="table-responsive">
                    <table class="table table-striped table-sm" id="tabelaFretesLancados">
                        <thead>
                            <tr>
                                <th data-coluna="id">ID <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="data">Data <i class="fas fa-sort sort-icon"></i></th>
                                <th>Hora</th>
                                <th data-coluna="empresa">Empresa <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="transportadora">Transportadora <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="cliente">Cliente <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="notaFiscal">NF <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="volume" class="text-end">Vol. <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="peso" class="text-end">Peso <i class="fas fa-sort sort-icon"></i></th>
                                <th data-coluna="valorNf" class="text-end">Valor NF <i class="fas fa-sort sort-icon"></i></th>
                                <th>Obs</th>
                                <th data-coluna="valorFrete" class="text-end">Vlr Frete <i class="fas fa-sort sort-icon"></i></th>
                                <th class="text-end">%</th>
                                <th class="text-center">Ações</th>
                            </tr>
                        </thead>
                        <tbody id="tbodyFretesSessao">
                        </tbody>
                        <tfoot>
                            <tr class="table-primary">
                                <td colspan="7" class="text-end fw-bold">TOTAIS DA PÁGINA:</td>
                                <td id="totalVolume" class="fw-bold text-end">0</td> 
                                <td id="totalPesoLiquido" class="fw-bold text-end">0,00</td>
                                <td id="totalValorNf" class="fw-bold text-end">R$ 0,00</td>
                                <td></td>
                                <td id="totalValorFrete" class="fw-bold text-end">R$ 0,00</td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
                    
                <div class="row mt-3">
                    <div class="col-12">
                        <div id="paginacaoContainer" class="d-flex justify-content-center"></div>
                        <div id="statusPaginacao" class="text-center mt-2 small text-muted"></div>
                    </div> 
                </div>

                <p id="mensagemFretesVazio" class="alert alert-light border text-center text-muted mt-3 mb-0" style="display:none;">
                    Selecione o botão "Limpar Pesquisa (Fretes do Dia)" para visualizar os registros de hoje.
                </p>
            </div>

        </div>

        <!-- 3. RODAPÉ -->
        <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
    </main>

    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/tablesort/5.2.1/tablesort.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/tablesort/5.2.1/sorts/tablesort.date.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script> 
    <script src="${pageContext.request.contextPath}/assets/js/global-branding.js" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/lancar-frete.js"></script>
</body>
</html>