package controller;

import conexao.Conexao; 
import dao.ConsultableDAO;
import dao.ProdutoDAO;
import dao.EntidadeDAO;
import dao.TransportadoraDAO;
import dao.ConsultaFreteDAO; 
import dto.FreteConsultaFiltro; 
import model.Usuario;
import model.Frete; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Apache POI
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook; // streaming
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

// iText 7
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell; 
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize; 

// Java utils
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/ExportServlet")
public class ExportServlet extends HttpServlet {
    /*private static final long serialVersionUID = 1L;

    // REMOVIDA A INICIALIZAÇÃO ESTÁTICA DAS FONTES.
    // Elas serão inicializadas dentro do doGet.
    private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(220, 220, 220);

    // Variáveis de instância para gerenciar o streaming de fretes
    private ResultSet fretesResultSet = null;
    private Connection fretesConnection = null;
    private PreparedStatement fretesStatement = null;

    @Override
    public void destroy() {
        closeFreteResources();
        super.destroy();
    }
    
    private void closeFreteResources() {
        try {
            if (fretesResultSet != null) fretesResultSet.close();
        } catch (SQLException e) {
            System.err.println("Erro ao fechar ResultSet: " + e.getMessage());
        }
        try {
            if (fretesStatement != null) fretesStatement.close();
        } catch (SQLException e) {
            System.err.println("Erro ao fechar PreparedStatement: " + e.getMessage());
        }
        try {
            if (fretesConnection != null) {
                try {
                    if (!fretesConnection.getAutoCommit()) {
                        fretesConnection.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter autoCommit: " + ex.getMessage());
                }
                fretesConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar Connection: " + e.getMessage());
        }
        fretesResultSet = null;
        fretesStatement = null;
        fretesConnection = null;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        closeFreteResources(); // garante recursos anteriores limpos
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão expirou ou não autorizado.");
            return;
        }

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        String perfil = usuarioLogado.getPerfil();
        List<String> modulosPermitidos = usuarioLogado.getModulosPermitidos();

        boolean hasPermission = false;
        if ("super_administrador".equals(perfil) || "administrador".equals(perfil) || "gerente".equals(perfil) || "tecnico".equals(perfil) || "usuario".equals(perfil)) {
            hasPermission = true;
        } else if (modulosPermitidos != null && (!modulosPermitidos.isEmpty() && (modulosPermitidos.contains("produtos") || modulosPermitidos.contains("clientes_fornecedores") || modulosPermitidos.contains("fretes")))) {
            hasPermission = true;
        }

        if (!hasPermission) {
            if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar esta funcionalidade.");
            return;
        }

        String format = request.getParameter("exportType");
        if (format == null || format.isEmpty()) {
            format = request.getParameter("format"); 
            if (format == null || format.isEmpty()) {
                 if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de exportação não especificado.");
                 return;
            }
        }

        String dataType = request.getParameter("dataType");
        if (dataType == null || dataType.isEmpty()) {
            if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo de dados para exportação não especificado.");
            return;
        }
        
        String fileNameBase = "dados_exportados";
        String sheetName = "Dados";
        String documentTitle = "Relatório de Dados";
        List<String> headers = null;
        List<Map<String, Object>> items = null; 

        System.out.println("DEBUG ExportServlet: dataType=" + dataType + " format=" + format);

        try {
            if ("fretes".equals(dataType)) {
                ConsultaFreteDAO freteDAO = new ConsultaFreteDAO();
                FreteConsultaFiltro filtro = new FreteConsultaFiltro();

                String dataInicio = request.getParameter("dataInicio");
                String dataFim = request.getParameter("dataFim");
                String idTransportadoraStr = request.getParameter("transportadoraId");
                String termoPesquisa = request.getParameter("termoPesquisa");
                // ADICIONE ESTA LINHA: 29.04.2026
                String campoPesquisa = request.getParameter("campoPesquisa");
                
                // FILTRO OBRIGATÓRIO: Injeta a unidade da sessão
                if (usuarioLogado != null) {
                    filtro.setIdUnidade(usuarioLogado.getUnidadeAtivaId());
                }

                if (dataInicio != null && !dataInicio.trim().isEmpty()) filtro.setDataInicio(dataInicio);
                if (dataFim != null && !dataFim.trim().isEmpty()) filtro.setDataFim(dataFim);
                if (idTransportadoraStr != null && !idTransportadoraStr.trim().isEmpty() && !"0".equals(idTransportadoraStr)) {
                    filtro.setIdTransportadora(Integer.parseInt(idTransportadoraStr));
                }
                if (termoPesquisa != null && !termoPesquisa.trim().isEmpty()) filtro.setTermoPesquisa(termoPesquisa);
                
               // ADICIONE ESTA LINHA TAMBÉM: 29.04.2026
                filtro.setCampoPesquisa(campoPesquisa);
                headers = Arrays.asList("ID", "Unidade","Data Cadastro", "Hora", "Nota Fiscal", "Transportadora", "Origem", "Destino", "Volume", "Peso Líquido", "Valor NF", "%", "Valor Frete", "Observação");
                fileNameBase = "fretes_exportados";
                sheetName = "Fretes";
                documentTitle = "Relatório de Fretes";

                // ABRE CONEXÃO e configura streaming para PostgreSQL
                fretesConnection = Conexao.conectar();
                fretesConnection.setAutoCommit(false);

                PreparedStatement[] stmtArray = new PreparedStatement[1];
                fretesResultSet = freteDAO.consultarResultSetParaExportacao(fretesConnection, filtro, stmtArray);
                fretesStatement = stmtArray[0];

                // TENTA configurar fetchSize para streaming (importante no PostgreSQL)
                try {
                    if (fretesStatement != null) {
                        fretesStatement.setFetchSize(1000);
                    }
                } catch (SQLException sqle) {
                    System.err.println("Aviso: não foi possível setar fetchSize no PreparedStatement: " + sqle.getMessage());
                }

            } else {
                ConsultableDAO dao;
                String type = request.getParameter("typeFilter");
                if ("transportadoras".equals(dataType) && "TODAS AS TRANSPORTADORAS".equals(type)) type = null;
                String search = request.getParameter("search");
                String sortColumn = request.getParameter("sortColumn");
                String sortDirection = request.getParameter("sortDirection");

                if ("produtos".equals(dataType)) {
                    dao = new ProdutoDAO();
                    fileNameBase = "produtos_exportados";
                    sheetName = "Produtos";
                    documentTitle = "Relatório de Produtos";
                } else if ("entidades".equals(dataType)) {
                    dao = new EntidadeDAO();
                    fileNameBase = "entidades_exportadas";
                    sheetName = "Entidades";
                    documentTitle = "Relatório de Clientes e Fornecedores";
                } else if ("transportadoras".equals(dataType)) {
                    dao = new TransportadoraDAO();
                    fileNameBase = "transportadoras_exportadas";
                    sheetName = "Transportadoras";
                    documentTitle = "Relatório de Transportadoras";
                } else {
                    if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo de dados inválido para exportação.");
                    return;
                }

                headers = dao.getHeaders(type);
                List<?> rawItems = dao.listarItens(type, 0, Integer.MAX_VALUE, search, sortColumn, sortDirection); 
                items = rawItems.stream()
                                .map(dao::itemToMap)
                                .collect(Collectors.toList());
            }

            if (headers == null || headers.isEmpty() || ("fretes".equals(dataType) && fretesResultSet == null) || (!"fretes".equals(dataType) && (items == null || items.isEmpty()))) {
                 if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Falha na recuperação de dados ou cabeçalhos para exportação. Nenhum dado encontrado.");
                 return;
            }

            final List<String> finalHeaders = headers;
            
         // -- NOVO BLOCO EXCEL FORMATADO (Substitui todo o anterior até o 'else if pdf') --
            if ("excel".equalsIgnoreCase(format)) {
                // Usamos XSSFWorkbook para permitir a criação da Tabela Formatada (Estilo Imagem 2)
                org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet(sheetName);

                // 1. Criar o cabeçalho
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < finalHeaders.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(finalHeaders.get(i));
                }

                // 2. Preencher os dados
                int rowIndex = 1;
                if ("fretes".equals(dataType) && fretesResultSet != null) {
                    while (fretesResultSet.next()) {
                        Row row = sheet.createRow(rowIndex++);
                        Map<String, Object> itemMap = freteToMapFromResultSet(fretesResultSet);
                        for (int j = 0; j < finalHeaders.size(); j++) {
                            String propertyKey = getPropertyKey(finalHeaders.get(j), dataType);
                            Object value = itemMap.get(propertyKey);
                            row.createCell(j).setCellValue(value != null ? value.toString() : "");
                        }
                    }
                } else if (items != null) {
                    for (Map<String, Object> item : items) {
                        Row row = sheet.createRow(rowIndex++);
                        for (int j = 0; j < finalHeaders.size(); j++) {
                            String propertyKey = getPropertyKey(finalHeaders.get(j), dataType);
                            Object value = item.get(propertyKey);
                            row.createCell(j).setCellValue(value != null ? value.toString() : "");
                        }
                    }
                }

                // 3. APLICAR A FORMATAÇÃO DE TABELA (Garante filtros e cores da Imagem 2)
                if (rowIndex > 1) { 
                    int lastCol = finalHeaders.size() - 1;
                    int lastRow = rowIndex - 1;
                    
                    org.apache.poi.ss.util.AreaReference reference = workbook.getCreationHelper().createAreaReference(
                        new org.apache.poi.ss.util.CellReference(0, 0), 
                        new org.apache.poi.ss.util.CellReference(lastRow, lastCol)
                    );

                    org.apache.poi.xssf.usermodel.XSSFTable table = sheet.createTable(reference);
                    
                    // Configurações básicas de nome
                    table.setName("RelatorioExportado");
                    table.setDisplayName("Relatorio_Exportado");

                    // MÁGICA: Acessa o XML interno para definir o estilo e filtros
                    // Isso evita o erro "getStyleDefinition() is undefined"
                    table.getCTTable().addNewAutoFilter(); 
                    
                    // Define o estilo (TableStyleMedium9 é o azul com listras)
                    org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo style = table.getCTTable().addNewTableStyleInfo();
                    style.setName("TableStyleMedium9");
                    style.setShowRowStripes(true); // Ativa as listras (Imagem 2)
                    style.setShowColumnStripes(false);
                }

                // 4. AUTO-AJUSTE DAS COLUNAS (Corrige o visual da Imagem 1)
                for (int i = 0; i < finalHeaders.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // 5. ENVIAR PARA O BROWSER
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameBase + ".xlsx\"");
                try (OutputStream outputStream = response.getOutputStream()) {
                    workbook.write(outputStream);
                    workbook.close();
                }
            }else if ("pdf".equalsIgnoreCase(format)) {
                // --- PDF otimizado (chunking) ---
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameBase + ".pdf\"");

                try (OutputStream outputStream = response.getOutputStream()) {
                    PdfWriter writer = new PdfWriter(outputStream);
                    try {
                        writer.setSmartMode(true);
                    } catch (Throwable t) {
                        // ignora se não suportado
                    }

                    PdfDocument pdf = new PdfDocument(writer);
                    pdf.setDefaultPageSize(PageSize.A4.rotate());
                    Document document = new Document(pdf);
                    document.setMargins(20, 20, 20, 20);
                    
                    // SOLUÇÃO DO ERRO: INICIALIZAÇÃO LOCAL DAS FONTES DENTRO DO CONTEXTO PDF
                    PdfFont defaultFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                    PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                    // Título
                    Paragraph title = new Paragraph(documentTitle)
                                        .setFont(boldFont) // Usa a fonte local
                                        .setFontSize(18)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(20);
                    document.add(title);

                    // PREP: mapear headers -> propertyKey uma vez (economiza trabalho no loop)
                    Map<Integer, String> headerIndexToKey = new HashMap<>();
                    for (int i = 0; i < finalHeaders.size(); i++) {
                        headerIndexToKey.put(i, getPropertyKey(finalHeaders.get(i), dataType));
                    }

                    // largura das colunas (peso relativo)
                    float[] columnWidths = new float[finalHeaders.size()];
                    for (int i = 0; i < finalHeaders.size(); i++) {
                        String headerText = finalHeaders.get(i);
                        float weight = 1.0f;
                        if (headerText.equals("%")) {
                            weight = 0.8f; // Peso menor, pois é apenas um número pequeno
                        } else if (headerText.contains("Nome / Razão Social") || headerText.contains("Descrição Equipamento") || headerText.contains("Descrição Equip.") || headerText.contains("Transportadora")) {
                            weight = 2.5f;
                        } else if (headerText.contains("Endereço") || headerText.contains("Observação")) {
                            weight = 2.0f;
                        } else if (headerText.contains("Email Principal") || headerText.contains("Sistema Operacional") || headerText.contains("Valor Frete") || headerText.contains("Valor NF")) {
                            weight = 1.8f;
                        } else if (headerText.contains("Telefone") || headerText.contains("CNPJ / CPF") || headerText.contains("Nota Fiscal") || headerText.contains("Peso Líquido") || headerText.contains("Volume") || headerText.contains("Data Cadastro")) {
                            weight = 1.5f;
                        } else if (headerText.contains("ID") || headerText.contains("CEP") || headerText.contains("UF") || headerText.contains("Status") || headerText.contains("Grupo") || headerText.contains("Tipo") || headerText.contains("Modelo") || headerText.contains("Setor")) {
                            weight = 1.0f;
                        }
                        columnWidths[i] = weight;
                    }

                    // CHUNK size: ajuste conforme memória / perf. Recomendo 500-2000.
                    final int CHUNK_SIZE = 1000;
                    int processedRows = 0;

                    // Cria a primeira tabela (cada tabela é nova, não reaproveitada)
                    Table table = createNewTableWithHeaders(finalHeaders, columnWidths, defaultFont, boldFont);

                    if ("fretes".equals(dataType) && fretesResultSet != null) {
                        while (fretesResultSet.next()) {
                            Map<String, Object> itemMap = freteToMapFromResultSet(fretesResultSet);

                            for (int colIndex = 0; colIndex < finalHeaders.size(); colIndex++) {
                                String key = headerIndexToKey.get(colIndex);
                                Object value = itemMap.get(key);
                                String cellValue = value != null ? value.toString() : "";

                                float fontSize = 8f;
                                String headerText = finalHeaders.get(colIndex);
                                if (headerText.contains("Observação") || headerText.contains("Descrição")) {
                                    fontSize = 7f;
                                }

                                // cria nova célula / paragraph a cada iteração (nunca reaproveitar)
                                Cell dataCell = new Cell()
                                        .add(new Paragraph(cellValue)
                                                .setFont(defaultFont) // Usa a fonte local
                                                .setFontSize(fontSize))
                                        .setTextAlignment(TextAlignment.LEFT)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                        .setPadding(2f);
                                table.addCell(dataCell);
                            }

                            processedRows++;

                            if (processedRows % CHUNK_SIZE == 0) {
                                // adiciona e cria nova tabela (nunca reaproveite a antiga)
                                document.add(table);
                                table = createNewTableWithHeaders(finalHeaders, columnWidths, defaultFont, boldFont); // Usa a fonte local
                            }
                        }
                    } else if (items != null) {
                        for (Map<String, Object> item : items) {
                            for (int colIndex = 0; colIndex < finalHeaders.size(); colIndex++) {
                                String key = headerIndexToKey.get(colIndex);
                                Object value = item.get(key);
                                String cellValue = value != null ? value.toString() : "";

                                float fontSize = 8f;
                                String headerText = finalHeaders.get(colIndex);
                                if (headerText.contains("Observação") || headerText.contains("Descrição")) {
                                    fontSize = 7f;
                                }

                                Cell dataCell = new Cell()
                                        .add(new Paragraph(cellValue)
                                                .setFont(defaultFont) // Usa a fonte local
                                                .setFontSize(fontSize))
                                        .setTextAlignment(TextAlignment.LEFT)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                        .setPadding(2f);
                                table.addCell(dataCell);
                            }

                            processedRows++;
                            if (processedRows % CHUNK_SIZE == 0) {
                                document.add(table);
                                table = createNewTableWithHeaders(finalHeaders, columnWidths, defaultFont, boldFont); // Usa a fonte local
                            }
                        }
                    }

                    // adiciona o bloco final (se existir)
                    if (table != null && table.getNumberOfRows() > 0) {
                        document.add(table);
                    }

                    // Fecha documento (escreve tudo no OutputStream)
                    document.close();
                    pdf.close();

                } // fim try OutputStream
            } else {
                if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de exportação não suportado.");
            }

        } catch (NumberFormatException e) {
            System.err.println("Erro na conversão de parâmetros: " + e.getMessage());
            if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetros inválidos.");
        } catch (SQLException e) {
            System.err.println("Erro SQL ao exportar dados: " + e.getMessage());
            e.printStackTrace();
            if (!response.isCommitted()) response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados para exportação.");
        } catch (Exception e) {
            System.err.println("Erro inesperado ao gerar arquivo: " + e.getMessage());
            e.printStackTrace();
            // Se a resposta já foi parcialmente escrita, não podemos chamar sendError()
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno do servidor ao gerar o arquivo.");
                } catch (IOException ioEx) {
                    System.err.println("Falha ao enviar erro HTTP: " + ioEx.getMessage());
                }
            } else {
                // já foi comprometido — apenas log
                System.err.println("Resposta já comprometida, não é possível enviar sendError().");
            }
        } finally {
            if ("fretes".equals(dataType)) {
                closeFreteResources();
            }
        }
    }

    // Cria nova tabela com headers (cada chamada retorna uma tabela nova)
    private Table createNewTableWithHeaders(List<String> headers, float[] columnWidths, PdfFont defaultFont, PdfFont boldFont) {
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header)
                            .setFont(boldFont) // Usa a fonte passada por parâmetro
                            .setFontSize(10f)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(HEADER_BG_COLOR)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(4f);
            table.addHeaderCell(headerCell);
        }

        return table;
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private Map<String, Object> freteToMapFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        String dataFormatada = "";
        try {
            java.sql.Date sqlDate = rs.getDate("data_lancamento");
            if (sqlDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dataFormatada = dateFormat.format(sqlDate);
            }
        } catch (Exception ignored) {}
        
     // Tratamento da Hora
        String horaFormatada = "";
        try {
            java.sql.Time sqlTime = rs.getTime("hora_lancamento"); 
            if (sqlTime != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); 
                horaFormatada = timeFormat.format(sqlTime);
            }
        } catch (Exception ignored) {}

        map.put("id", rs.getObject("id"));
        map.put("unidade", rs.getObject("unidade_id")); // <--- ADICIONE ESTA LINHA
        map.put("dataCadastro", dataFormatada); 
        map.put("hora", horaFormatada); // <--- ADICIONE ESTA LINHA (Ela estava faltando no seu código)
        map.put("notaFiscal", rs.getString("nota_fiscal")); 
        map.put("transportadora", rs.getString("nome_transportadora") != null ? rs.getString("nome_transportadora") : "N/A"); 
        map.put("origem", rs.getString("nome_empresa")); 
        map.put("destino", rs.getString("nome_cliente")); 
        
        // --- ADICIONE O TRATAMENTO DA PORCENTAGEM AQUI ---
        Double porcentagem = rs.getDouble("porcentagem_frete");
        map.put("porcentagemFrete", rs.wasNull() ? "0,00%" : String.format("%.2f%%", porcentagem));

        Double volume = rs.getDouble("volume");
        map.put("volume", rs.wasNull() ? "0" : String.valueOf(volume.intValue())); 

        Double pesoLiquido = rs.getDouble("peso_liquido");
        map.put("pesoLiquido", rs.wasNull() ? "0,00 kg" : String.format("%.2f kg", pesoLiquido));

        Double valorNf = rs.getDouble("valor_nf");
        map.put("valorNf", rs.wasNull() ? "R$ 0,00" : String.format("R$ %.2f", valorNf));

        Double valorFrete = rs.getDouble("valor_frete");
        map.put("valorFrete", rs.wasNull() ? "R$ 0,00" : String.format("R$ %.2f", valorFrete));

        map.put("observacao", rs.getString("observacao"));

        return map;
    }
    
    // Método não utilizado no código PDF atual, removido para limpeza:
    // private Cell createPdfCell(String headerText, PdfFont dataFont, String cellValue) { ... }

    private Map<String, Object> freteToMap(Frete frete) {
       return new HashMap<>(); 
    }

    private String getPropertyKey(String headerText, String dataType) {
        if ("fretes".equals(dataType)) {
             switch (headerText) {
                case "ID": return "id";
                case "Unidade": return "unidade"; // <--- ADICIONE ESTA LINHA
                case "Data Cadastro": return "dataCadastro";
                case "Hora": return "hora";
                case "Nota Fiscal": return "notaFiscal";
                case "Transportadora": return "transportadora";
                case "Origem": return "origem";
                case "Destino": return "destino";
                case "Volume": return "volume";
                case "Peso Líquido": return "pesoLiquido";
                case "Valor NF": return "valorNf";
                // --- ADICIONE ESTA LINHA PARA VINCULAR O CABEÇALHO ---
                case "%": return "porcentagemFrete";
                case "Valor Frete": return "valorFrete";
                case "Observação": return "observacao";
                default: return headerText; 
            }
        }
        
        if ("produtos".equals(dataType)) {
             switch (headerText) {
             case "ID": return "id";
             case "Grupo": return "Grupo";
             case "Tipo": return "Tipo"; 
             case "Origem": return "origem";
             case "Numero Etiqueta": return "numeroEtiqueta"; // Ajustado para camelCase
             case "Nome": return "nome";
             case "Colaborador": return "colaborador";
             case "Departamento": return "departamento";
             case "Descrição Equipamento": return "descricaoEquipamento"; // Ajustado para camelCase
             case "Sistema Operacional": return "sistemaOperacional"; // Ajustado para camelCase
             case "Atendente": return "atendente";
             case "Status Atendimento": return "statusAtendimento"; // Ajustado para camelCase
             case "Observação":  return "observacao";
             case "Data Chegada": return "dataChegada"; // Ajustado para camelCase
             case "Data Envio": return "dataEnvio"; // Ajustado para camelCase (Data Saída = Data Envio)
             case "Status": return "status";
             case "Modelo": return "modelo";
             case "Numero Série": return "numeroSerie"; // Ajustado para camelCase
             case "Endereço IP": return "enderecoIp"; // Ajustado para camelCase
             case "Setor": return "setor";
             case "Equipamento": return "equipamento";
             case "Login": return "login";
             case "Senha": return "senha";
                default: 
                    return headerText; 
            }
        } else if ("entidades".equals(dataType)) {
            switch (headerText) {
                case "ID": return "id";
                case "Origem Tipo": return "origemTipo";
                case "Origem Detalhe": return "origemDetalhe";
                case "Nome / Razão Social": return "nomeRazaoSocial";
                case "CNPJ / CPF": return "cnpjCpf";
                case "Contato Principal": return "contatoPrincipal";
                case "Telefone Fixo": return "telefoneFixo";
                case "Telefone Celular": return "telefoneCelular";
                case "Email Principal": return "emailPrincipal";
                case "Endereço": return "endereco";
                case "CEP": return "cep";
                case "Cidade": return "cidade";
                case "UF": return "uf";
                case "Papel": return "papel";
                case "Observação Geral da Entidade": return "observacaoGeralEntidade"; 
                case "Tipo Fornecimento": return "tipoFornecimento";
                case "Observação Específica do Fornecedor": return "observacaoFornecedor";
                case "Data Cadastro": return "dataCadastro";
                case "Ativo": return "ativo"; 
                default: 
                    return headerText; 
            }
        } else if ("transportadoras".equals(dataType)) { 
            switch (headerText) {
                case "ID": return "id";
                case "Nome": return "nome";
                case "Região": return "regiao";
                case "Sub-Região": return "subRegiao";
                case "Cidade": return "cidade";
                case "Base": return "base";
                case "Observação": return "observacao";
                default:
                    return headerText;
            }
        }
        return headerText; 
    }*/
}