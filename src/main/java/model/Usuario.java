package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dto.UnidadeDTO;


/**
 * Representa um usuário no sistema, incluindo suas credenciais, perfil e módulos permitidos.
 * Esta classe é Serializable para que possa ser armazenada corretamente na sessão HTTP.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L; 

    private int id;
    private String username;
    private String email;
    private String senha; // Armazena o hash da senha
    private String perfil; 
    private List<String> modulosPermitidos; 
    private boolean ativo; // Campo 'ativo' adicionado
    
 // --- NOVOS CAMPOS PARA MULTI-FILIAL ---
    private List<String> unidadesPermitidas; // Armazena ['161', '151', etc.]
    
 // --- NOVOS CAMPOS PARA CONTEXTO DE UNIDADE ---
    private Integer unidadeAtivaId;    // O ID da unidade selecionada no momento
    private String unidadeAtivaNome;  // O Nome da unidade (ex: "ES - MATRIZ")
    private List<UnidadeDTO> unidadesPermitidasObjetos; // Lista com ID e Nome para o select do menu
    
    private String nomeCompleto; // Novo campo para o nome completo
    private LocalDateTime ultimoAcesso; // ou String formatada

    /**
     * Construtor padrão. Inicializa a lista de módulos permitidos para evitar NullPointerException.
     */
    public Usuario() {
        this.modulosPermitidos = new ArrayList<>(); 
    }

    /**
     * Construtor completo para criar uma instância de Usuario com todos os detalhes.
     *
     * @param id O ID único do usuário.
     * @param username O nome de usuário.
     * @param email O endereço de email do usuário.
     * @param senha O hash da senha do usuário.
     * @param perfil O perfil de acesso do usuário.
     * @param ativo Indica se o usuário está ativo.
     * @param modulosPermitidos Uma lista de strings representando os módulos que o usuário pode acessar.
     */
    public Usuario(int id, String username, String email, String senha, String perfil, boolean ativo, List<String> modulosPermitidos, List<String> unidadesPermitidas) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.modulosPermitidos = (modulosPermitidos != null) ? new ArrayList<>(modulosPermitidos) : new ArrayList<>();
        this.unidadesPermitidas = (unidadesPermitidas != null) ? new ArrayList<>(unidadesPermitidas) : new ArrayList<>();
    }

    /**
     * Construtor para criação de novo usuário (sem ID, pois é gerado pelo BD).
     *
     * @param username O nome de usuário.
     * @param email O endereço de email do usuário.
     * @param senha O hash da senha do usuário.
     * @param perfil O perfil de acesso do usuário.
     * @param ativo Indica se o usuário está ativo.
     * @param modulosPermitidos Uma lista de strings representando os módulos que o usuário pode acessar.
     */
    public Usuario(String username, String email, String senha, String perfil, boolean ativo, List<String> modulosPermitidos, List<String> unidadesPermitidas) {
        this.username = username;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.modulosPermitidos = (modulosPermitidos != null) ? new ArrayList<>(modulosPermitidos) : new ArrayList<>();
        this.unidadesPermitidas = (unidadesPermitidas != null) ? new ArrayList<>(unidadesPermitidas) : new ArrayList<>();
    }


    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() { 
        return senha;
    }

    public void setSenha(String senha) { 
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public List<String> getModulosPermitidos() {
        return modulosPermitidos;
    }

    public void setModulosPermitidos(List<String> modulosPermitidos) {
        this.modulosPermitidos = (modulosPermitidos != null) ? new ArrayList<>(modulosPermitidos) : new ArrayList<>();
    }
    
    public List<String> getUnidadesPermitidas() {
        return unidadesPermitidas;
    }

    public void setUnidadesPermitidas(List<String> unidadesPermitidas) {
        this.unidadesPermitidas = (unidadesPermitidas != null) ? new ArrayList<>(unidadesPermitidas) : new ArrayList<>();
    }
    
    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimo_acesso) {
        this.ultimoAcesso = ultimo_acesso;
    }
    public boolean isAtivo() { 
        return ativo;
    }

    public void setAtivo(boolean ativo) { 
        this.ativo = ativo;
    }
    
    public Integer getUnidadeAtivaId() { return unidadeAtivaId; }
    public void setUnidadeAtivaId(Integer unidadeAtivaId) { this.unidadeAtivaId = unidadeAtivaId; }
    
    public String getUnidadeAtivaNome() { return unidadeAtivaNome; }
    public void setUnidadeAtivaNome(String unidadeAtivaNome) { this.unidadeAtivaNome = unidadeAtivaNome; }
    
    public List<UnidadeDTO> getUnidadesPermitidasObjetos() { return unidadesPermitidasObjetos; }
    public void setUnidadesPermitidasObjetos(List<UnidadeDTO> unidades) { this.unidadesPermitidasObjetos = unidades; }

    /**
     * Adiciona um módulo à lista de módulos permitidos do usuário, se ainda não estiver presente.
     * @param modulo O nome do módulo a ser adicionado.
     */
    public void addModuloPermitido(String modulo) {
        if (this.modulosPermitidos == null) {
            this.modulosPermitidos = new ArrayList<>();
        }
        if (modulo != null && !modulo.trim().isEmpty() && !this.modulosPermitidos.contains(modulo)) {
            this.modulosPermitidos.add(modulo);
        }
    }

    /**
     * Remove um módulo da lista de módulos permitidos do usuário.
     * @param modulo O nome do módulo a ser removido.
     */
    public void removeModuloPermitido(String modulo) {
        if (this.modulosPermitidos != null && modulo != null) {
            this.modulosPermitidos.remove(modulo);
        }
    }
    
    /**
     * Adiciona uma unidade à lista de unidades permitidas, se ainda não estiver presente.
     * @param unidadeCodigo O código da unidade (ex: '161', '151') a ser adicionado.
     */
    public void addUnidadePermitida(String unidadeCodigo) {
        if (this.unidadesPermitidas == null) {
            this.unidadesPermitidas = new ArrayList<>();
        }
        if (unidadeCodigo != null && !unidadeCodigo.trim().isEmpty() && !this.unidadesPermitidas.contains(unidadeCodigo)) {
            this.unidadesPermitidas.add(unidadeCodigo);
        }
    }

    /**
     * Remove uma unidade da lista de unidades permitidas.
     * @param unidadeCodigo O código da unidade a ser removido.
     */
    public void removeUnidadePermitida(String unidadeCodigo) {
        if (this.unidadesPermitidas != null && unidadeCodigo != null) {
            this.unidadesPermitidas.remove(unidadeCodigo);
        }
    }

    /**
     * Retorna uma representação em string do objeto Usuario.
     */
    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", perfil='" + perfil + '\'' +
               ", ativo=" + ativo +
               ", modulosPermitidos=" + modulosPermitidos +
               ", unidadesPermitidas=" + unidadesPermitidas + // Incluído aqui
               '}';
    }

    // --- CLASSE INTERNA PARA REPRESENTAR UM TOKEN DE REDEFINIÇÃO DE SENHA ---
    // Necessária para o PasswordResetServlet e UsuarioDAO
    public static class PasswordResetToken {
        private int id;
        private int idUsuario;
        private String token;
        private LocalDateTime expiraEm;
        private boolean usado;
        private LocalDateTime criadoEm;

        public PasswordResetToken(int id, int idUsuario, String token, LocalDateTime expiraEm, boolean usado, LocalDateTime criadoEm) {
            this.id = id;
            this.idUsuario = idUsuario;
            this.token = token;
            this.expiraEm = expiraEm;
            this.usado = usado;
            this.criadoEm = criadoEm;
        }

        // Getters
        public int getId() {
            return id;
        }

        public int getIdUsuario() {
            return idUsuario;
        }

        public String getToken() {
            return token;
        }

        public LocalDateTime getExpiraEm() {
            return expiraEm;
        }

        public boolean isUsado() {
            return usado;
        }

        public LocalDateTime getCriadoEm() {
            return criadoEm;
        }

        // Setters (adicionados para completude, embora tokens sejam geralmente imutáveis)
        public void setId(int id) {
            this.id = id;
        }

        public void setIdUsuario(int idUsuario) {
            this.idUsuario = idUsuario;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setExpiraEm(LocalDateTime expiraEm) {
            this.expiraEm = expiraEm;
        }

        public void setUsado(boolean usado) {
            this.usado = usado;
        }

        public void setCriadoEm(LocalDateTime criadoEm) {
            this.criadoEm = criadoEm;
        }
    }
    
    // Mapeia o nome do módulo para suas permissões detalhadas (Ex: "equipamentos" -> Objeto PermissaoModulo)
     private java.util.Map<String, PermissaoModulo> permissoesModulos = new java.util.HashMap<>();
     
     public java.util.Map<String, PermissaoModulo> getPermissoesModulos() {
         return permissoesModulos;
     }

     public void setPermissoesModulos(java.util.Map<String, PermissaoModulo> permissoesModulos) {
         this.permissoesModulos = permissoesModulos != null ? permissoesModulos : new java.util.HashMap<>();
     }

     /**
      * Verifica se o usuário possui uma ação específica (CONSULTAR, INSERIR, EDITAR, EXCLUIR, CANCELAR) em um módulo.
      */
     public boolean temPermissao(String nomeModulo, String acao) {
         if (this.perfil != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(this.perfil) || "ADMINISTRADOR".equalsIgnoreCase(this.perfil))) {
             return true; // Administradores possuem acesso total a tudo
         }
         
         if (permissoesModulos == null || !permissoesModulos.containsKey(nomeModulo.toLowerCase())) {
             return false;
         }
         
         PermissaoModulo p = permissoesModulos.get(nomeModulo.toLowerCase());
         if (p == null) return false;

         switch (acao.toUpperCase()) {
             case "CONSULTAR": return p.isPodeConsultar();
             case "INSERIR":   return p.isPodeInserir();
             case "EDITAR":    return p.isPodeEditar();
             case "EXCLUIR":   return p.isPodeExcluir();
             case "CANCELAR":  return p.isPodeCancelar();
             default: return false;
         }
     }

     // --- CLASSE AUXILIAR PARA GUARDAR AS 5 PERMISSÕES DO MÓDULO ---
     public static class PermissaoModulo implements Serializable {
         private static final long serialVersionUID = 1L;
         private boolean podeConsultar;
         private boolean podeInserir;
         private boolean podeEditar;
         private boolean podeExcluir;
         private boolean podeCancelar;

         public PermissaoModulo(boolean podeConsultar, boolean podeInserir, boolean podeEditar, boolean podeExcluir, boolean podeCancelar) {
             this.podeConsultar = podeConsultar;
             this.podeInserir = podeInserir;
             this.podeEditar = podeEditar;
             this.podeExcluir = podeExcluir;
             this.podeCancelar = podeCancelar;
         }

         public boolean isPodeConsultar() { return podeConsultar; }
         public boolean isPodeInserir() { return podeInserir; }
         public boolean isPodeEditar() { return podeEditar; }
         public boolean isPodeExcluir() { return podeExcluir; }
         public boolean isPodeCancelar() { return podeCancelar; }
     }
}
