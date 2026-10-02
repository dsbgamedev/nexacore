package controller;

import dao.TransportadoraDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Transportadora;
import model.TransportadoraEndereco;
import model.Usuario;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/ConsultaTransportadoraServlet")
public class ConsultaTransportadoraServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response, String acaoPermissao) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        return usuario != null && usuario.temPermissao("transportadoras", acaoPermissao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = request.getParameter("acao");
        String idStr = request.getParameter("id");

        // 1. Trata a requisição AJAX para exibir os detalhes no Modal de Visualização
        if ("detalhes".equals(acao) && idStr != null) {
            if (!validarPermissao(request, response, "CONSULTAR")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("<p class='text-danger text-center'>Acesso negado.</p>");
                return;
            }

            try {
                Long id = Long.parseLong(idStr);
                TransportadoraDAO dao = new TransportadoraDAO();
                Transportadora t = dao.buscarPorId(id);
                List<TransportadoraEndereco> enderecos = dao.listarEnderecosPorTransportadora(id);

                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();

                if (t != null) {
                    out.println("<div class='container-fluid'>");
                    out.println("<h6 class='fw-bold text-primary mb-3'>Dados Gerais</h6>");
                    out.println("<p><b>Razão Social:</b> " + t.getRazaoSocial() + "</p>");
                    out.println("<p><b>Nome Fantasia:</b> " + (t.getNomeFantasia() != null ? t.getNomeFantasia() : "-") + "</p>");
                    out.println("<p><b>CNPJ:</b> " + t.getCnpj() + " | <b>IE:</b> " + (t.getInscricaoEstadual() != null ? t.getInscricaoEstadual() : "-") + "</p>");
                    out.println("<p><b>Telefone:</b> " + (t.getTelefone() != null ? t.getTelefone() : "-") + " | <b>E-mail:</b> " + (t.getEmail() != null ? t.getEmail() : "-") + "</p>");
                    out.println("<p><b>Status:</b> <span class='badge " + ("ATIVA".equalsIgnoreCase(t.getStatus()) ? "bg-success" : "bg-secondary") + "'>" + t.getStatus() + "</span></p>");
                    
                    out.println("<hr class='my-3'>");
                    out.println("<h6 class='fw-bold text-primary mb-3'>Endereços Cadastrados</h6>");
                    if (enderecos != null && !enderecos.isEmpty()) {
                        for (TransportadoraEndereco end : enderecos) {
                            out.println("<div class='border p-2 rounded mb-2 bg-light'>");
                            out.println("<small><b>CEP:</b> " + end.getCep() + " - " + end.getLogradouro() + ", " + end.getNumero() + " (" + end.getBairro() + " - " + end.getCidade() + "/" + end.getUf() + ")</small>");
                            out.println("</div>");
                        }
                    } else {
                        out.println("<p class='text-muted'>Nenhum endereço vinculado.</p>");
                    }
                    out.println("</div>");
                } else {
                    out.println("<p class='text-danger text-center'>Transportadora não encontrada.</p>");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().println("<p class='text-danger text-center'>Erro ao carregar os detalhes.</p>");
            }
            return;
        }

        // 2. Fluxo padrão de listagem e filtros
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        if (usuario == null || !usuario.temPermissao("transportadoras", "CONSULTAR")) {
            response.sendRedirect(request.getContextPath() + "/menu.jsp?erro=sem_permissao");
            return;
        }

        String pesquisa = request.getParameter("pesquisa");
        String cnpj = request.getParameter("cnpj");
        String status = request.getParameter("status");

        try {
            TransportadoraDAO dao = new TransportadoraDAO();
            List<Transportadora> lista = dao.listarComFiltros(pesquisa, cnpj, status);
            
            request.setAttribute("transportadoras", lista);
            request.setAttribute("filtroPesquisa", pesquisa);
            request.setAttribute("filtroCnpj", cnpj);
            request.setAttribute("filtroStatus", status);

            request.getRequestDispatcher("/WEB-INF/jsp/consulta-transportadora.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao consultar transportadoras.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (usuario == null || (!usuario.temPermissao("transportadoras", "EXCLUIR") && !usuario.temPermissao("transportadoras", "EDITAR"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"sucesso\": false, \"mensagem\": \"Sem permissão para realizar esta operação.\"}");
            return;
        }

        String acao = request.getParameter("acao");
        String idStr = request.getParameter("id");

        if ("excluir".equals(acao) && idStr != null) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                Long id = Long.parseLong(idStr);
                TransportadoraDAO dao = new TransportadoraDAO();
                boolean sucesso = dao.excluir(id);
                if (sucesso) {
                    out.write("{\"sucesso\": true}");
                } else {
                    out.write("{\"sucesso\": false, \"mensagem\": \"Registro não encontrado ou já excluído.\"}");
                }
            } catch (SQLException e) {
                out.write("{\"sucesso\": false, \"mensagem\": \"Erro ao excluir (provavelmente possui registros vinculados em uso).\"}");
            }
        }
    }
}