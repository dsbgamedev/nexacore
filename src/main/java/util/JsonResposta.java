package util;

import java.util.List;

/**
 * Classe utilitária para padronizar as respostas JSON dos Servlets.
 * Inclui status de sucesso, mensagem, dados e informações de paginação.
 */
public class JsonResposta {
    private boolean success;
    private String message;
    private Object data; // Pode ser um objeto único, uma lista, ou qualquer outro dado
    private List<?> items; // Para listas de itens paginadas
    private int totalItems;
    private int totalPages;

    // Construtor para respostas simples (sucesso/erro com mensagem)
    public JsonResposta(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Construtor para respostas com dados (objeto único)
    public JsonResposta(boolean success, String message, Object data) {
        this(success, message);
        this.data = data;
    }

    // Construtor para respostas com dados paginados (lista de itens)
    public JsonResposta(boolean success, String message, List<?> items, int totalItems, int totalPages) {
        this(success, message);
        this.items = items;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }
    
    // Construtor para respostas com contagem (ex: contagem de itens em manutenção)
    public JsonResposta(boolean success, String message, int count) {
        this(success, message);
        this.data = count; // Usa 'data' para a contagem
    }

    // Getters para que o Gson possa serializar para JSON
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public List<?> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
