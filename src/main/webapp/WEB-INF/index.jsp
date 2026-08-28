<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Porta de entrada única do sistema: Redireciona para o Servlet de Login
    response.sendRedirect(request.getContextPath() + "/LoginServlet");
%>
