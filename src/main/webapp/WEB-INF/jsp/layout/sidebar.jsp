<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="model.Usuario" %>
<%
    String ctx = request.getContextPath();
    Usuario usuarioSidebar = (Usuario) session.getAttribute("usuarioLogado");
    String nomeUsuarioSidebar = (usuarioSidebar != null) ? usuarioSidebar.getUsername() : "Usuário";
    String unidadeAtivaSidebar = (usuarioSidebar != null && usuarioSidebar.getUnidadeAtivaNome() != null) ? usuarioSidebar.getUnidadeAtivaNome() : "Filial Padrão";
%>

<%-- CONFIGURAÇÃO DE VARIÁVEIS DE PERMISSÃO --%>
<c:set var="perfilLogado" value="${fn:toLowerCase(sessionScope.usuarioLogado.perfil)}" />
<c:set var="isSuperAdmin" value="${fn:contains(perfilLogado, 'super')}" />
<c:set var="isAdmin" value="${fn:contains(perfilLogado, 'admin')}" />
<c:set var="hasFullAccess" value="${isSuperAdmin or isAdmin}" />
<c:set var="listaModulos" value="${sessionScope.usuarioLogado.modulosPermitidos}" />

<% 
    java.util.List<String> listaMod = (usuarioSidebar != null) ? usuarioSidebar.getModulosPermitidos() : null;
    String modulosStr = "";
    if (listaMod != null && !listaMod.isEmpty()) {
        StringBuilder sb = new StringBuilder(",");
        for (String m : listaMod) {
            if (m != null) {
                sb.append(m.toLowerCase().trim()).append(",");
            }
        }
        modulosStr = sb.toString();
    }
    request.setAttribute("modulosStr", modulosStr);
%>

<c:set var="canSeeEquipamentos" value="${hasFullAccess or fn:contains(modulosStr, ',equipamento,') or fn:contains(modulosStr, ',equipamentos,')}" />
<c:set var="canSeeProdutos" value="${hasFullAccess or fn:contains(modulosStr, ',produto,') or fn:contains(modulosStr, ',produtos,')}" />
<c:set var="canSeeTransportadoras" value="${hasFullAccess or fn:contains(modulosStr, ',transportadora,') or fn:contains(modulosStr, ',transportadoras,')}" />
<c:set var="canSeeFornecedores" value="${hasFullAccess or fn:contains(modulosStr, ',fornecedor,') or fn:contains(modulosStr, ',fornecedores,')}" />
<c:set var="canSeeMovimentacao" value="${hasFullAccess or fn:contains(modulosStr, ',movimentacao,') or fn:contains(modulosStr, ',movimentacoes,') or fn:contains(modulosStr, ',movimentacao_envio,') or fn:contains(modulosStr, ',movimentacao_recebimento,')}" />
<c:set var="canSeeManutencao" value="${hasFullAccess or fn:contains(modulosStr, ',manutencao,') or fn:contains(modulosStr, ',manutencao_chamados,')}" />
<c:set var="canSeeFabricantes" value="${hasFullAccess or fn:contains(modulosStr, ',fabricante,') or fn:contains(modulosStr, ',fabricantes,')}" />
<c:set var="canSeeMarcas" value="${hasFullAccess or fn:contains(modulosStr, ',marca,') or fn:contains(modulosStr, ',marcas,')}" />
<c:set var="canSeeEmpresas" value="${isSuperAdmin}" />
<c:set var="canSeeAtributo" value="${isSuperAdmin}" /> 
<c:set var="canSeeAuditoria" value="${isSuperAdmin}" />  
<c:set var="canSeeSessoes" value="${isSuperAdmin}" /> 
<c:set var="canSeeUsuarios" value="${hasFullAccess or fn:contains(modulosStr, ',usuario,') or fn:contains(modulosStr, ',usuarios,')}" />
<c:set var="canSeeGerenciarUsuarios" value="${hasFullAccess or fn:contains(modulosStr, ',usuario,') or fn:contains(modulosStr, ',usuarios,')}" />
<c:set var="canWriteEquipamentos" value="${hasFullAccess or sessionScope.usuarioLogado.temPermissao('equipamentos', 'INSERIR') or sessionScope.usuarioLogado.temPermissao('equipamentos', 'EDITAR')}" />
<c:set var="canWriteProdutos" value="${hasFullAccess or sessionScope.usuarioLogado.temPermissao('produtos', 'INSERIR') or sessionScope.usuarioLogado.temPermissao('produtos', 'EDITAR')}" />
<c:set var="canWriteManutencao" value="${hasFullAccess or sessionScope.usuarioLogado.temPermissao('manutencao_chamados', 'INSERIR') or sessionScope.usuarioLogado.temPermissao('manutencao_chamados', 'EDITAR')}" />
<c:set var="canWriteTransportadoras" value="${hasFullAccess or sessionScope.usuarioLogado.temPermissao('transportadoras', 'INSERIR') or sessionScope.usuarioLogado.temPermissao('transportadoras', 'EDITAR')}" />
<c:set var="canSeeFrete" value="${hasFullAccess or fn:contains(modulosStr, ',frete,') or sessionScope.usuarioLogado.temPermissao('frete', 'INSERIR') or sessionScope.usuarioLogado.temPermissao('frete', 'EDITAR')}" />

<!-- SIDEBAR LATERAL ESQUERDA -->
<aside id="sidebar" class="nexacore-sidebar">
    <!-- Botão flutuante na bordinha para recolher/expandir o menu principal -->
    <button type="button" class="sidebar-toggle-btn" onclick="toggleSidebar()" title="Recolher/Expandir Menu">
        <i class="bi bi-chevron-left" id="sidebarToggleIcon"></i>
    </button>

    <div class="sidebar-brand">
        <div class="logo-symbol">N</div>
        <div class="logo-info">
            <span class="logo-name">NEXACORE</span>
            <span class="logo-sub">Sistema de Gestão</span>
        </div>
    </div>

    <div class="sidebar-menu-container">
        <!-- PRINCIPAL -->
        <span class="menu-category">PRINCIPAL</span>
        <ul class="sidebar-menu">
            <li><a href="<%=ctx%>/MenuServlet"><i class="bi bi-speedometer2"></i> Dashboard</a></li>
        </ul>
        
        <!-- EQUIPAMENTOS -->
        <c:if test="${canSeeEquipamentos or canSeeProdutos}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>EQUIPAMENTOS</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <c:if test="${canSeeEquipamentos}">
                        <li><a href="<%=ctx%>/ConsultaEquipamentosServlet"><i class="bi bi-cpu"></i> Consulta de Equipamentos</a></li>
                        <c:if test="${canWriteEquipamentos}">
                            <li><a href="<%=ctx%>/CadastrarEquipamentoServlet"><i class="bi bi-plus-circle"></i> Novo Equipamento</a></li>
                        </c:if>
                    </c:if>
                    <c:if test="${canSeeProdutos}">
                        <c:if test="${canWriteProdutos}">
                            <li><a href="<%=ctx%>/ProdutoServlet"><i class="bi bi-plus-circle"></i> Novo Produto</a></li>                  
                        </c:if>
                        <li><a href="<%=ctx%>/ConsultaProdutoServlet"><i class="bi bi-box-seam"></i> Consulta de Produtos</a></li>                  
                    </c:if>
                </ul>
            </div>
        </c:if>
        
        <!-- TRANSPORTADORA -->
        <c:if test="${canSeeTransportadoras}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>TRANSPORTADORA</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <c:if test="${canSeeTransportadoras}">
                        <li><a href="<%=ctx%>/TransportadoraServlet"><i class="bi bi-truck"></i> Transportadoras</a></li>
                    </c:if>
                    <li><a href="<%=ctx%>/ConsultaTransportadoraServlet"><i class="bi bi-list-check"></i> Consultar Transportadoras </a></li>
                </ul>
            </div>
        </c:if>
        
        <!-- FRETE -->
        <c:if test="${canSeeFrete}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>FRETE</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <li><a href="<%=ctx%>/LancarFreteServlet"><i class="bi bi-truck-flatbed"></i> Lançar Frete</a></li>
                    <li><a href="<%=ctx%>/ConsultaFreteServlet"><i class="bi bi-search"></i> Consultar Fretes</a></li>
                </ul>
            </div>
        </c:if>
        
        <!-- FORNECEDORES -->
        <c:if test="${canSeeFornecedores}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>FORNECEDORES</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <li><a href="<%=ctx%>/ConsultaFornecedorServlet"><i class="bi bi-shop"></i> Consulta de Fornecedores</a></li>
                    <li><a href="<%=ctx%>/FornecedorServlet"><i class="bi bi-plus-circle"></i> Cadastrar Fornecedor</a></li>
                </ul>
            </div>
        </c:if>

        <!-- MOVIMENTAÇÕES -->
        <c:if test="${canSeeMovimentacao}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>MOVIMENTAÇÕES</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <li><a href="<%=ctx%>/EnvioEquipamentoServlet"><i class="bi bi-arrow-up-right-circle"></i> Envios</a></li>
                    <li><a href="<%=ctx%>/ConsultaEnvioServlet"><i class="bi bi-search"></i> Consulta de Envios</a></li>
                    <li><a href="<%=ctx%>/DevolucaoEquipamentoServlet"><i class="bi bi-arrow-return-left"></i> Devolução de Equipamento</a></li>
                    <li><a href="<%=ctx%>/RecebimentoServlet"><i class="bi bi-arrow-down-left-circle"></i> Recebimentos/Devoluções</a></li>
                </ul>
            </div>
        </c:if>

        <!-- MANUTENÇÕES -->
        <c:if test="${canSeeManutencao}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>MANUTENÇÕES</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <c:if test="${canWriteManutencao}">
                        <li><a href="<%=ctx%>/ManutencaoServlet"><i class="bi bi-tools"></i> Abrir Chamado</a></li>
                    </c:if>
                    <li><a href="<%=ctx%>/ConsultaChamadosServlet"><i class="bi bi-list-check"></i> Consulta de Chamados</a></li>
                </ul>
            </div>
        </c:if>

        <!-- CADASTROS -->
        <c:if test="${canSeeFabricantes or canSeeMarcas or canSeeEmpresas or canSeeAtributos or canSeeUsuarios or canSeeGerenciarUsuarios}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>CADASTROS</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">           	
                    <c:if test="${canSeeFabricantes}">
                        <li><a href="<%=ctx%>/FabricanteServlet"><i class="bi bi-building"></i> Fabricantes</a></li>
                    </c:if>
                    <c:if test="${canSeeMarcas}">
                        <li><a href="<%=ctx%>/MarcaServlet"><i class="bi bi-tag"></i> Marcas</a></li>
                    </c:if>
                    <c:if test="${canSeeEmpresas}">
                        <li><a href="<%=ctx%>/CadastrarEmpresaServlet"><i class="bi bi-grid"></i> Empresas</a></li>
                    </c:if>
                    <c:if test="${canSeeAtributo}">
                        <li><a href="<%=ctx%>/AtributoServlet"><i class="bi bi-list-nested"></i> Atributos</a></li>
                    </c:if>
                    <c:if test="${canSeeUsuarios}">
                        <li><a href="<%=ctx%>/CadastrarUsuarioServlet"><i class="bi bi-people"></i> Usuários</a></li>
                    </c:if>
                    <c:if test="${canSeeGerenciarUsuarios}">
                        <li><a href="<%=ctx%>/GerenciarUsuariosServlet"><i class="bi bi-people"></i> Gerenciar Usuários</a></li>
                    </c:if>
                </ul>
            </div>
        </c:if>
        
        <!-- AUDITORIA -->
        <c:if test="${canSeeAuditoria}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>AUDITORIA</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <li><a href="<%=ctx%>/AuditoriaServlet"><i class="bi bi-cpu"></i> Consulta Auditoria</a></li>
                </ul>       
            </div>
        </c:if>
        
        <!-- GERENCIAR SESSÕES -->
        <c:if test="${canSeeSessoes}">
            <div class="sidebar-dropdown-group collapsed">
                <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                    <span>GERENCIAR SESSÕES</span>
                    <i class="bi bi-chevron-down toggle-icon"></i>
                </span>
                <ul class="sidebar-menu">
                    <li><a href="<%=ctx%>/AdminSessoesServlet"><i class="bi bi-cpu"></i> GERENCIAR SESSÕES </a></li>
                </ul>       
            </div>
        </c:if>
         
        <!-- CONFIGURAÇÕES -->
        <div class="sidebar-dropdown-group collapsed">
            <span class="menu-category dropdown-toggle-title" onclick="toggleSidebarMenu(this)">
                <span>CONFIGURAÇÕES</span>
                <i class="bi bi-chevron-down toggle-icon"></i>
            </span>
            <ul class="sidebar-menu">
                <li><a href="<%=ctx%>/ConfiguracoesServlet"><i class="bi bi-gear"></i> Configurações</a></li>
            </ul>
        </div>
    </div>

    <!-- USER FOOTER NA SIDEBAR -->
    <div class="sidebar-user-footer">
        <div class="user-avatar"><%= nomeUsuarioSidebar.substring(0, Math.min(nomeUsuarioSidebar.length(), 2)).toUpperCase() %></div>
        <div class="user-details">
            <span class="user-name"><%= nomeUsuarioSidebar %></span>
            <span class="user-branch">Filial: <%= unidadeAtivaSidebar %></span>
        </div>
        <a href="<%=ctx%>/LogoutServlet" class="user-logout-icon" title="Sair"><i class="bi bi-box-arrow-right"></i></a>
    </div>
</aside>