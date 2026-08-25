package model.enums;

/**
 * Enumeração para representar os diferentes perfis de usuário no sistema.
 * Cada perfil tem uma hierarquia associada, onde 0 é o mais alto (Super Administrador).
 */
public enum PerfilUsuario {
    SUPER_ADMINISTRADOR("super_administrador", 0),
    ADMINISTRADOR("administrador", 1),
    GERENTE("gerente", 2),
    TECNICO("tecnico", 3),
    USUARIO("usuario", 4);

    private final String nome;
    private final int hierarquia;

    /**
     * Construtor para o enum PerfilUsuario.
     * @param nome O nome do perfil (minúsculas, para consistência com strings do banco/frontend).
     * @param hierarquia O nível de hierarquia do perfil (quanto menor o número, maior a hierarquia).
     */
    PerfilUsuario(String nome, int hierarquia) {
        this.nome = nome;
        this.hierarquia = hierarquia;
    }

    /**
     * Retorna o nome do perfil.
     * @return O nome do perfil em string (ex: "super_administrador").
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o nível de hierarquia do perfil.
     * @return Um inteiro representando a hierarquia (0 para Super Administrador, 1 para Administrador, etc.).
     */
    public int getHierarquia() {
        return hierarquia;
    }

    /**
     * Converte uma string de nome de perfil para o objeto PerfilUsuario correspondente.
     * Ignora maiúsculas/minúsculas.
     * @param text O nome do perfil em formato de string.
     * @return O objeto PerfilUsuario correspondente.
     * @throws IllegalArgumentException se nenhum perfil com o nome fornecido for encontrado.
     */
    public static PerfilUsuario fromString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("O texto do perfil não pode ser nulo.");
        }
        for (PerfilUsuario b : PerfilUsuario.values()) {
            if (b.nome.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Nenhum perfil com o nome " + text + " encontrado.");
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
