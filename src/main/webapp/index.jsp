<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Redireciona automaticamente para a nossa tela de atributos
    	response.sendRedirect(request.getContextPath() + "/jsp/cadastro-empresa.jsp");
    response.sendRedirect(request.getContextPath() + "/jsp/gerenciar-atributos.jsp");
	response.sendRedirect(request.getContextPath() + "/jsp/cadastro-produto.jsp");
	response.sendRedirect(request.getContextPath() + "/jsp/consulta-produto.jsp");
	response.sendRedirect(request.getContextPath() + "/jsp/cadastro-equipamento.jsp");
	response.sendRedirect(request.getContextPath() + "/jsp/marcas.jsp");
	response.sendRedirect(request.getContextPath() + "/jsp/fabricantes.jsp");
	
%>

