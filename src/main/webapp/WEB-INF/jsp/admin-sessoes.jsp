<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>NexaCore | Gerenciamento de Sessões</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body class="p-4 bg-light" data-context-path="${pageContext.request.contextPath}">
    <div class="container-fluid">
        <nav aria-label="breadcrumb">
	        <ol class="breadcrumb mb-4">
	           <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/MenuServlet">Home</a></li>
	           <li class="breadcrumb-item active" aria-current="page">Gerencias Sessões</li>
	        </ol>
    	</nav>  
    	
    	<!-- Filtros de Pesquisa -->
	    <div class="d-flex align-items-center text-primary mb-3">
            <i class="fa-solid fa-filter me-2"></i>
            <span class="fw-bold small text-uppercase">Filtros de Pesquisa</span>
        </div> 

        <div class="card p-4 shadow-sm">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="card-title fw-bold text-primary m-0"><i class="fa fa-users me-1"></i> Usuários Online</div>
                <span class="badge bg-primary px-3 py-2" id="contador-sessoes">0 sessão(ões) ativa(s)</span>
            </div>

            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th class="sortable" data-column="sessionId" style="cursor:pointer;">ID da Sessão <i class="fa fa-sort text-muted"></i></th>
                            <th class="sortable" data-column="nomeUsuario" style="cursor:pointer;">Usuário <i class="fa fa-sort text-muted"></i></th>
                            <th class="sortable" data-column="emailUsuario" style="cursor:pointer;">E-mail <i class="fa fa-sort text-muted"></i></th>
                            <th class="sortable" data-column="tempoAtivo" style="cursor:pointer;">Tempo Ativo <i class="fa fa-sort text-muted"></i></th>
                            <th class="sortable" data-column="consumoMemoria" style="cursor:pointer;">Peso Estimado <i class="fa fa-sort text-muted"></i></th>
                            <th class="sortable" data-column="ultimoAcesso" style="cursor:pointer;">Último Acesso <i class="fa fa-sort text-muted"></i></th>
                            <th class="text-center" style="width: 130px;">Ações</th>
                        </tr>
                    </thead>
                    <tbody id="tabela-sessoes-corpo">
                        <!-- Preenchido via JavaScript dinamicamente -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script> const contextPath = '${pageContext.request.contextPath}'; </script>
    <script src="${pageContext.request.contextPath}/assets/js/sessoes.js"></script>
</body>
</html>