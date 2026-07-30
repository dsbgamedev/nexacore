package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/api/imagens/*")
public class ImagemServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "C:\\uploads_nexacore\\";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Pega o nome do arquivo após /api/imagens/
        String nomeArquivo = request.getPathInfo();
        
        if (nomeArquivo == null || nomeArquivo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome da imagem não informado.");
            return;
        }

        // Remove a barra inicial se houver
        if (nomeArquivo.startsWith("/")) {
            nomeArquivo = nomeArquivo.substring(1);
        }

        File arquivo = new File(UPLOAD_DIR + nomeArquivo);

        if (!arquivo.exists() || arquivo.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Imagem não encontrada.");
            return;
        }

        // Define o tipo de conteúdo com base na extensão
        String mimeType = getServletContext().getMimeType(arquivo.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setContentLength((int) arquivo.length());

        // Escreve os bytes da imagem na resposta HTTP
        try (FileInputStream in = new FileInputStream(arquivo);
             OutputStream out = response.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}