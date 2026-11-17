package controller;
/*
import com.google.gson.Gson;
import dao.TransportadoraDAO;
import model.Transportadora;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/AnalisarNotaServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class AnalisarNotaServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final TransportadoraDAO transportadoraDAO = new TransportadoraDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Validação de Segurança (Padrão CBA DIESEL)
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        if (usuarioLogado == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 2. Recebe a foto enviada pelo LancarFrete.js
            Part filePart = request.getPart("imagem");
            if (filePart == null) throw new Exception("Nenhuma imagem recebida.");
            
            InputStream imageStream = filePart.getInputStream();
            byte[] imageBytes = imageStream.readAllBytes();

            // 3. Chamada à API (Google Vision ou similar)
            // Aqui simulamos os dados extraídos pela IA
            String notaLida = "363162";
            double pesoLido = 173.00;
            double valorLido = 5400.50;
            String nomeTranspLido = "BRASPRESS"; // Nome que veio escrito na nota
            String clienteLido = "CBA DIESEL PEÇAS LTDA";

            // 4. Lógica de Busca de Transportadora no Banco
            int idEncontrado = 0;
            String nomeOficial = "";
            
            // Usamos seu método listarItens para buscar por nome aproximado
            List<Transportadora> possiveis = transportadoraDAO.listarItens(null, 0, 1, nomeTranspLido, "nome", "ASC");
            
            if (!possiveis.isEmpty()) {
                idEncontrado = possiveis.get(0).getId();
                nomeOficial = possiveis.get(0).getNome();
            }

            // 5. Montagem da Resposta JSON para o seu JavaScript
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", true);
            resposta.put("notaFiscal", notaLida);
            resposta.put("peso", pesoLido);
            resposta.put("valorNf", valorLido);
            resposta.put("cliente", clienteLido);
            resposta.put("idTransportadora", idEncontrado);
            resposta.put("nomeTransportadora", nomeOficial.isEmpty() ? nomeTranspLido : nomeOficial);

            out.print(gson.toJson(resposta));

        } catch (Exception e) {
            response.setStatus(500);
            out.print(gson.toJson(Map.of("sucesso", false, "mensagem", e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
}*/