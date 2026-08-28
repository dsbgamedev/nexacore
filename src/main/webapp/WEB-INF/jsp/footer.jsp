<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="util.VersionUtils" %>
<%@ page import="java.time.ZonedDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Pega a data bruta do VersionUtils - Versão do Sistema
    String rawDate = VersionUtils.getBuildFormatado();
    String formattedDate = rawDate; // padrão caso dê erro

    try {
        // Converte a string ISO (ex: 2026-06-01T14:39:01Z) para um objeto de data
        ZonedDateTime zdt = ZonedDateTime.parse(rawDate);
        // Formata para o seu modelo: 01/06/2026 | Hr: 14:39:01
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy '| Hr:' HH:mm:ss");
        formattedDate = zdt.format(formatter);
    } catch (Exception e) {
        // Se falhar a formatação, exibe como está
    }
%>

<hr style="border: 0; border-top: 1px solid rgba(0, 0, 0, 0.1); width: 80%; margin: 10px auto;">

<div class="versao-sistema" style="text-align: center; font-size: 11px; color: #666; margin-bottom: 10px;">
<%--<footer style="margin-top: auto; padding: 20px; text-align: center; width: 100%;">--%>
    NexaCore | Versão <%= VersionUtils.getVersao() %> | <%= formattedDate %>
</div>