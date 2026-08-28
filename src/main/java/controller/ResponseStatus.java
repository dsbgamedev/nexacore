package controller;

/**
 * Classe de status de resposta padronizada para APIs JSON.
 * Usada para retornar o resultado de operações (sucesso/falha)
 * com mensagens, IDs de itens afetados e mensagens de depuração opcionais.
 */
public class ResponseStatus {
    private boolean success;
    private String message;
    private Integer id; // Novo campo para o ID do item afetado (opcional)
    private String debugMessage; // Renomeado de 'error' para 'debugMessage' (opcional)

    // Construtor para sucesso/falha com mensagem
    public ResponseStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Construtor para sucesso/falha com mensagem e ID
    public ResponseStatus(boolean success, String message, Integer id) {
        this.success = success;
        this.message = message;
        this.id = id;
    }

    // Construtor para sucesso/falha com mensagem e debugMessage (anteriormente 'error')
    public ResponseStatus(boolean success, String message, String debugMessage) {
        this.success = success;
        this.message = message;
        this.debugMessage = debugMessage;
    }

    // Construtor completo
    public ResponseStatus(boolean success, String message, Integer id, String debugMessage) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.debugMessage = debugMessage;
    }

    // Getters (necessários para Gson serializar)
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getId() {
        return id;
    }

    public String getDebugMessage() { // Getter para o campo renomeado
        return debugMessage;
    }
}

