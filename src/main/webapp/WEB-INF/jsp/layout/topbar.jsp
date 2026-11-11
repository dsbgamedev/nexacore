<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- TOPBAR -->
<header class="nexacore-topbar">
    <button id="sidebarToggle" class="btn-toggle"><i class="bi bi-list"></i></button>
    <div class="topbar-right">
        <div class="topbar-icon-badge" title="Notificações">
            <i class="bi bi-bell"></i>
            <span class="badge-count">3</span>
        </div>
        <div class="topbar-icon-badge" title="Ajuda">
            <i class="bi bi-question-circle"></i>
        </div>
        <div class="topbar-branch-selector d-flex align-items-center">
            <i class="bi bi-building-check me-1"></i>
            <span class="me-2">Filial Atual:</span>
            <select id="selectUnidadeAtiva" class="form-select form-select-sm w-auto d-inline-block">
                <c:forEach var="unidade" items="${sessionScope.usuarioLogado.unidadesPermitidasObjetos}">
                    <option value="${unidade.id}" ${unidade.id == sessionScope.usuarioLogado.unidadeAtivaId ? 'selected' : ''}>
                        ${unidade.nome}
                    </option>
                </c:forEach>
            </select>
        </div>
    </div>
</header>