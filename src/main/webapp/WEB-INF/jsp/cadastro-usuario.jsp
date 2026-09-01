<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Usuario" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    String ctx = request.getContextPath();
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
    String nomeUsuario = (usuario != null) ? usuario.getUsername() : "Usuário";
    String unidadeAtiva = (usuario != null && usuario.getUnidadeAtivaNome() != null) ? usuario.getUnidadeAtivaNome() : "Filial Padrão";
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexaCore - ${title != null ? title : "Cadastro de Usuário"}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="<%=ctx%>/assets/css/cadastro-usuario.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>

<body class="nexacore-cadastro-usuario">
<div class="container-fluid px-4 py-4">
    <!-- CABEÇALHO -->
    <div class="mb-4">
        <div class="page-title">
            <i class="bi bi-person-gear me-2"></i> ${title != null ? title : "CADASTRO DE USUÁRIO"}
        </div>
        <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/GerenciarUsuariosServlet">Consulta Usuarios</a></li>
            <li class="breadcrumb-item active" aria-current="page">${title != null ? title : "Cadastrar Usuarios"}</li>
        </ol>
        </nav>
    </div>

    <!-- O formulário agora envia a ação via JS ou input hidden para diferenciar cadastro/edição -->
    <form id="formCadastroUsuario">
        <input type="hidden" id="usuarioId" name="id" value="${usuarioEdicao != null ? usuarioEdicao.id : (usuario != null ? usuario.id : '')}">
        <input type="hidden" id="isEditing" value="${isEditing}">

        <!-- DADOS DO USUÁRIO -->
        <div class="card-nexa p-4 mb-4">
            <div class="section-title">
                <i class="bi bi-person-vcard me-2"></i> DADOS DO USUÁRIO
            </div>
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label"> Usuário * </label>
                    <input type="text" id="username" name="usuario" class="form-control" required placeholder="Ex.: joao.silva" value="${usuarioEdicao != null ? usuarioEdicao.username : (usuario != null ? usuario.username : '')}">
                </div>

                <div class="col-md-3">
                    <label class="form-label"> Nome Completo * </label>
                    <input type="text" id="nomeCompleto" name="nome" class="form-control" required placeholder="Nome do usuário" value="${usuarioEdicao != null ? usuarioEdicao.nomeCompleto : (usuario != null ? usuario.nomeCompleto : '')}">
                </div>

                <div class="col-md-3">
                    <label class="form-label"> E-mail * </label>
                    <input type="email" id="email" name="email" class="form-control" required placeholder="usuario@empresa.com.br" value="${usuarioEdicao != null ? usuarioEdicao.email : (usuario != null ? usuario.email : '')}">
                </div>

                <div class="col-md-3">
                    <label class="form-label"> Perfil </label>
                    <select name="perfil" id="perfil" class="form-select" ${disablePerfilField ? 'disabled' : ''}>
                        <option value="">Selecione...</option>
                        <c:forEach var="p" items="${perfisDisponiveisParaSelecao}">
                            <option value="${fn:toUpperCase(p)}" ${(usuarioEdicao != null && fn:toUpperCase(usuarioEdicao.perfil) == fn:toUpperCase(p)) || (usuario != null && fn:toUpperCase(usuario.perfil) == fn:toUpperCase(p)) ? 'selected' : ''}>
                                ${p}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label"> Senha * </label>
                    <input type="password" id="senha" name="senha" class="form-control" placeholder="${isEditing ? 'Deixe em branco para manter' : 'Digite a senha'}" ${isEditing ? '' : 'required'}>
                </div>
                <div class="col-md-4">
                    <label class="form-label"> Confirmar Senha * </label>
                    <input type="password" id="confirmarSenha" name="confirmarSenha" class="form-control" placeholder="Confirme a senha" ${isEditing ? '' : 'required'}>
                </div>
                               
               <!-- FILIAL PRINCIPAL DINÂMICA -->
				<div class="col-md-4">
				    <label class="form-label"> Filial Principal </label>
				    <select name="filial" id="unidadePadrao" class="form-select" required>
				        <option value="">Selecione a filial...</option>
				    </select>
				</div>

                <div class="col-md-12">
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="ativo" id="ativo" ${usuarioEdicao != null ? (usuarioEdicao.ativo ? 'checked' : '') : (usuario == null || usuario.ativo ? 'checked' : '')}>
                        <label class="form-check-label" for="ativo"> Usuário ativo </label>
                    </div>
                </div>
            </div>
        </div>

       <!-- PERMISSÕES DINÂMICAS (MATRIZ GRANULAR) -->
       <div class="table-responsive">
		    <table class="table align-middle text-center">
		        <thead>
		            <tr class="permission-header">
		                <th class="text-start">
		                    <input type="checkbox" class="form-check-input me-2" id="checkAllModulos" onclick="toggleTodosModulos(this)"> 
		                    Módulo
		                </th>
		                <th><input type="checkbox" class="form-check-input" onclick="toggleColunaPermissao('check-consultar', this)"> Consultar</th>
		                <th><input type="checkbox" class="form-check-input" onclick="toggleColunaPermissao('check-inserir', this)"> Inserir</th>
		                <th><input type="checkbox" class="form-check-input" onclick="toggleColunaPermissao('check-editar', this)"> Editar</th>
		                <th><input type="checkbox" class="form-check-input" onclick="toggleColunaPermissao('check-excluir', this)"> Excluir</th>
		                <th><input type="checkbox" class="form-check-input" onclick="toggleColunaPermissao('check-cancelar', this)"> Cancelar</th>
		            </tr>
		        </thead>
		        <tbody>
		        <c:forEach var="modulo" items="${todosModulosDisponiveis}">
		            <tr class="modulo-row">
		                <input type="hidden" class="modulo-id" value="${modulo.id}">
		                <td class="text-start module-name">
		                    <i class="bi bi-box-seam module-icon"></i> ${modulo.nomeModulo}
		                </td>
		                <td><input type="checkbox" class="form-check-input check-consultar"></td>
		                <td><input type="checkbox" class="form-check-input check-inserir"></td>
		                <td><input type="checkbox" class="form-check-input check-editar"></td>
		                <td><input type="checkbox" class="form-check-input check-excluir"></td>
		                <td><input type="checkbox" class="form-check-input check-cancelar"></td>
		            </tr>
		        </c:forEach>
		        </tbody>
		    </table>
		</div>

       <!-- UNIDADES PERMITIDAS DINÂMICAS -->
		<div class="card-nexa p-4 mb-4">
		    <div class="d-flex justify-content-between align-items-center mb-3">
		        <div class="section-title mb-0">
		            <i class="bi bi-building me-2"></i> UNIDADES PERMITIDAS
		        </div>
		        <span class="badge bg-info text-dark p-2">Controle por filial</span>
		    </div>
		
		    <p class="text-muted small mb-3">
		        Selecione as unidades/filiais às quais este usuário terá acesso.
		    </p>
		
		    <div class="d-flex justify-content-between align-items-center bg-light border rounded p-3 mb-3">
		        <div>
		            <strong><i class="bi bi-shield-check me-1"></i> Unidades com acesso</strong>
		            <span id="contadorUnidades" class="badge bg-primary ms-2">0 selecionada(s)</span>
		        </div>
		        <div>
		            <button type="button" class="btn btn-sm btn-outline-primary me-2" onclick="selecionarTodasUnidades()">
		                <i class="bi bi-check-all"></i> Selecionar todas
		            </button>
		            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="limparUnidades()">
		                <i class="bi bi-x-lg"></i> Limpar
		            </button>
		        </div>
		    </div>

    <div class="input-group mb-3">
        <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
        <input type="text" id="pesquisaUnidade" class="form-control" placeholder="Pesquisar unidade, código ou nome..." onkeyup="filtrarUnidades()">
    </div>

   <div class="row g-3" id="listaUnidades">
        <c:forEach var="unidade" items="${todasUnidadesDisponiveis}">
            <div class="col-md-4 unidade-item">
                <label class="unidade-card" style="display: flex; align-items: center; gap: 10px; cursor: pointer; border: 1px solid #dee2e6; padding: 10px; border-radius: 6px;">
                    <input type="checkbox" name="unidadesPermitidas" value="${unidade[0]}" class="form-check-input unidade-checkbox" style="margin-top: 0;">
                    <div class="unidade-info">
                        <div class="unidade-header">
                            <span class="unidade-codigo fw-bold text-primary">${unidade[1]} ${unidade[2]}</span>
                            <c:choose>
                                <c:when test="${unidade[1] == '161'}">
                                    <span class="badge bg-success text-white">Matriz</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark">Filial</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="unidade-nome text-secondary">Unidade Operacional</div>
                    </div>
                </label>
            </div>
        </c:forEach>
    </div>
</div>

        <!-- BOTÕES DE AÇÃO -->
        <div class="card-nexa p-3 mb-5">
            <div class="d-flex justify-content-between align-items-center">
                <a href="<%=ctx%>/GerenciarUsuariosServlet" class="btn btn-outline-secondary px-4">
                    <i class="bi bi-arrow-left me-1"></i> Voltar
                </a>
                <div>
                    <button type="reset" class="btn btn-light border px-4 me-2">
                        <i class="bi bi-eraser me-1"></i> Limpar
                    </button>
                    <button type="submit" class="btn btn-primary px-5">
                        <i class="bi bi-check-lg me-1"></i> Salvar Usuário
                    </button>
                </div>
            </div>
        </div>
    </form>
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

<!-- Scripts e Injeção de Dados do Usuário Editado -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script>
    // Variáveis globais do sistema injetadas via JSP
    const contextPath = "${pageContext.request.contextPath}";
    const unidadesGlobais = ${not empty todasUnidadesJson ? todasUnidadesJson : '[]'};
    
    // Dados do usuário para pré-preenchimento no modo de edição
    const usuarioEdicao = ${not empty usuarioJson ? usuarioJson : 'null'};
    const unidadesPermitidasUsuario = ${not empty usuarioUnidadesJson ? usuarioUnidadesJson : '[]'};
</script>
<script src="${pageContext.request.contextPath}/assets/js/cadastro-usuario.js"></script>
</body>
</html>