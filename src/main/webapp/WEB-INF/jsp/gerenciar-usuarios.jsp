<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>NexaCore - Gerenciamento de Usuários</title>

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <style>

        body {
            background: #f4f6f9;
            font-family: Arial, Helvetica, sans-serif;
            color: #172033;
        }

        .page-title {
            color: #123b82;
            font-weight: 700;
            font-size: 28px;
        }

        .breadcrumb-custom {
            font-size: 14px;
            color: #6c757d;
            margin-bottom: 25px;
        }

        .breadcrumb-custom a {
            color: #1464e8;
            text-decoration: none;
        }

        .card-nexa {
            background: white;
            border: 1px solid #dde3ec;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,.04);
        }

        .card-header-nexa {
            padding: 18px 22px;
            border-bottom: 1px solid #e5e9ef;
            font-size: 18px;
            font-weight: 700;
            color: #123b82;
        }

        .btn-nexa {
            background: #1264e8;
            color: white;
            border: none;
            padding: 10px 18px;
            border-radius: 6px;
            font-weight: 600;
        }

        .btn-nexa:hover {
            background: #0d52c2;
            color: white;
        }

        .table thead th {
            background: #f7f9fc;
            color: #334155;
            font-size: 13px;
            font-weight: 700;
            border-bottom: 1px solid #dce2ea;
        }

        .table tbody td {
            vertical-align: middle;
            font-size: 14px;
        }

        .status-ativo {
            background: #0aa36c;
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: 600;
        }

        .status-inativo {
            background: #6c757d;
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: 600;
        }

        .btn-action {
            width: 36px;
            height: 36px;
            border-radius: 6px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-left: 4px;
        }

        .search-box {
            height: 42px;
        }

        .filter-label {
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
        }

    </style>

</head>

<body>

<div class="container-fluid px-4 py-4">

    <!-- CABEÇALHO -->

    <div class="d-flex justify-content-between align-items-center mb-2">

        <div>
            <div class="page-title">
                <i class="bi bi-people me-2"></i>
                GERENCIAMENTO DE USUÁRIOS
            </div>

            <div class="breadcrumb-custom">
                <a href="menu.jsp">Home</a>
                /
                Administração
                /
                Usuários
            </div>
        </div>

        <a href="cadastro-usuario.jsp"
           class="btn btn-success">

            <i class="bi bi-person-plus-fill me-1"></i>
            Novo Usuário

        </a>

    </div>


    <!-- FILTROS -->

    <div class="card-nexa mb-4">

        <div class="card-header-nexa">

            <i class="bi bi-funnel me-2"></i>
            FILTROS DE PESQUISA

        </div>

        <div class="p-4">

            <form action="../GerenciarUsuariosServlet"
                  method="get">

                <div class="row g-3">

                    <div class="col-md-5">

                        <label class="filter-label">
                            Pesquisar
                        </label>

                        <input
                            type="text"
                            name="pesquisa"
                            class="form-control search-box"
                            placeholder="Nome, usuário ou e-mail...">

                    </div>


                    <div class="col-md-3">

                        <label class="filter-label">
                            Perfil
                        </label>

                        <select name="perfil"
                                class="form-select search-box">

                            <option value="">
                                Todos
                            </option>

                            <option value="ADMINISTRADOR">
                                Administrador
                            </option>

                            <option value="GESTOR">
                                Gestor
                            </option>

                            <option value="USUARIO">
                                Usuário
                            </option>

                            <option value="CONSULTA">
                                Consulta
                            </option>

                        </select>

                    </div>


                    <div class="col-md-2">

                        <label class="filter-label">
                            Situação
                        </label>

                        <select name="ativo"
                                class="form-select search-box">

                            <option value="">
                                Todos
                            </option>

                            <option value="true">
                                Ativos
                            </option>

                            <option value="false">
                                Inativos
                            </option>

                        </select>

                    </div>


                    <div class="col-md-2 d-flex align-items-end">

                        <button
                            type="submit"
                            class="btn btn-primary w-100">

                            <i class="bi bi-search me-1"></i>
                            Pesquisar

                        </button>

                    </div>

                </div>

            </form>

        </div>

    </div>


    <!-- LISTA -->

    <div class="card-nexa">

        <div class="card-header-nexa
                    d-flex
                    justify-content-between
                    align-items-center">

            <div>

                <i class="bi bi-list-ul me-2"></i>
                USUÁRIOS CADASTRADOS

                <span class="badge bg-light text-dark ms-2">
                    5 registros
                </span>

            </div>

            <select class="form-select"
                    style="width:100px">

                <option>10</option>
                <option>25</option>
                <option>50</option>

            </select>

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

                    <th class="text-center">
                        Ações
                    </th>

                </tr>

                </thead>


                <tbody>

                <!--
                    Aqui futuramente você coloca:

                    request.getAttribute("usuarios")
                -->

                <tr>

                    <td>1</td>

                    <td>
                        <strong>douglas</strong>
                    </td>

                    <td>
                        Douglas Bocatto
                    </td>

                    <td>
                        douglas@empresa.com.br
                    </td>

                    <td>
                        <span class="badge bg-primary">
                            Administrador
                        </span>
                    </td>

                    <td>
                        161 - SSA
                    </td>

                    <td>
                        26/08/2026 09:42
                    </td>

                    <td>

                        <span class="status-ativo">
                            Ativo
                        </span>

                    </td>

                    <td class="text-center">

                        <!-- Visualizar -->

                        <a href="visualizar-usuario.jsp?id=1"
                           class="btn btn-outline-secondary btn-action"
                           title="Visualizar">

                            <i class="bi bi-eye"></i>

                        </a>


                        <!-- Editar -->

                        <a href="cadastro-usuario.jsp?id=1"
                           class="btn btn-outline-primary btn-action"
                           title="Editar">

                            <i class="bi bi-pencil"></i>

                        </a>


                        <!-- Desativar -->

                        <button
                            class="btn btn-outline-danger btn-action"
                            title="Desativar"
                            onclick="desativarUsuario(1)">

                            <i class="bi bi-person-x"></i>

                        </button>

                    </td>

                </tr>


                </tbody>

            </table>

        </div>


        <!-- RODAPÉ -->

        <div class="d-flex
                    justify-content-between
                    align-items-center
                    p-3">

            <span class="text-muted small">
                Mostrando 1 a 5 de 5 registros
            </span>

            <div>

                <button class="btn btn-sm btn-outline-secondary">
                    Anterior
                </button>

                <button class="btn btn-sm btn-primary">
                    1
                </button>

                <button class="btn btn-sm btn-outline-secondary">
                    Próximo
                </button>

            </div>

        </div>

    </div>

</div>


<script>

function desativarUsuario(id) {

    if (!confirm(
        "Deseja realmente desativar este usuário?"
    )) {
        return;
    }

    window.location.href =
        "../GerenciarUsuariosServlet"
        + "?acao=desativar&id="
        + id;
}

</script>

</body>
</html>