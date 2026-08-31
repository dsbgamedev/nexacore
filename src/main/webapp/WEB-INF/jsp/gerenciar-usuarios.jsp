<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexaCore - Gerenciamento de Usuários</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/gerenciar-usuario.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>
<div class="container-fluid px-4 py-4">
    <!-- CABEÇALHO -->
    <div class="d-flex justify-content-between align-items-center mb-2">
        <div>
            <h4 class="page-title text-uppercase fw-bold">Gerenciamento de Usuários</h4>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Gerenciamento de Usuários</li>
                </ol>
            </nav>
        </div>

        <a href="${pageContext.request.contextPath}/CadastrarUsuarioServlet?action=new" class="btn btn-success btn-nexa">
            <i class="bi bi-person-plus-fill me-1"></i> Novo Usuário
        </a>
    </div>

    <!-- FILTROS -->
    <div class="card-nexa mb-4">
        <div class="card-header-nexa">
            <i class="bi bi-funnel me-2"></i> FILTROS DE PESQUISA
        </div>
        <div class="p-4">
            <form id="formFiltroUsuarios">
                <div class="row g-3">
                    <div class="col-md-5">
                        <label class="filter-label">Pesquisar</label>
                        <input type="text" id="filtroPesquisa" class="form-control search-box" placeholder="Nome, usuário ou e-mail...">
                    </div>

                    <div class="col-md-3">
                        <label class="filter-label">Perfil</label>
                        <select id="filtroPerfil" class="form-select search-box">
                            <option value="">Todos</option>
                            <option value="SUPER_ADMINISTRADOR">Super Administrador</option>
                            <option value="ADMINISTRADOR">Administrador</option>
                            <option value="GESTOR">Gestor</option>
                            <option value="USUARIO">Usuário</option>
                            <option value="CONSULTA">Consulta</option>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="filter-label">Situação</label>
                        <select id="filtroAtivo" class="form-select search-box">
                            <option value="">Todos</option>
                            <option value="true">Ativos</option>
                            <option value="false">Inativos</option>
                        </select>
                    </div>

                    <div class="col-md-2 d-flex align-items-end">
                        <button type="button" onclick="carregarUsuarios()" class="btn btn-primary w-100 btn-nexa">
                            <i class="bi bi-search me-1"></i> Pesquisar
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- LISTA -->
    <div class="card-nexa">
        <div class="card-header-nexa d-flex justify-content-between align-items-center">
            <div>
                <i class="bi bi-list-ul me-2"></i> USUÁRIOS CADASTRADOS
                <span id="contadorRegistros" class="badge bg-light text-dark ms-2">0 registros</span>
            </div>
        </div>

        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuário</th>
                        <th>Nome</th>
                        <th>E-mail</th>
                        <th>Perfil</th>
                        <th>Filial Principal</th>
                        <th>Último Acesso</th>
                        <th>Situação</th>
                        <th class="text-center">Ações</th>
                    </tr>
                </thead>
                <tbody id="tabelaUsuariosBody">
                    <!-- Dinâmico via JavaScript -->
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Configura o ContextPath global para uso nos arquivos JS externos -->
<script>
    const contextPath = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/modal-service.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/gerenciar-usuarios.js"></script>
</body>
</html>