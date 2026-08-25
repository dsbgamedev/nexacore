package dao;

import model.Usuario;
import conexao.Conexao; // Usando sua classe de Conexao

import com.google.gson.Gson; // Gson não será mais usado para modulos_permitidos diretamente, mas pode ser para outros fins
// import com.google.gson.reflect.TypeToken; // Não será mais usado para modulos_permitidos

// import java.lang.reflect.Type; // Não será mais usado para modulos_permitidos
import org.mindrot.jbcrypt.BCrypt; // Importar BCrypt

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // Para LocalDateTime no JDBC
import java.time.LocalDateTime; // Para manipulacao de datas/horas
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dto.UnidadeDTO;


public class UsuarioDAO {

    // Manter Gson caso seja usado para outras serializações/desserializações no futuro,
    // mas não mais para modulos_permitidos
    private Gson gson = new Gson(); 

    public UsuarioDAO() {
        // O construtor não precisa mais inicializar 'conexao' como um campo de instância.
        // As conexões serão obtidas e fechadas dentro de cada método usando try-with-resources.
    }

    /**
     * Valida as credenciais de login de um usuário e carrega suas permissões de módulo.
     * @param username O nome de usuário.
     * @return Um objeto Usuario se as credenciais forem válidas, null caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public Usuario validarLogin(String username) throws SQLException {
        // A query agora não busca mais 'modulos_permitidos' diretamente da tabela usuarios.
        // Ela fará um JOIN implícito via o método carregarModulosDoUsuario.
        String sql = "SELECT id, username, email, senha, perfil, ativo FROM usuarios WHERE username = ?";
        Usuario usuario = null;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenha(rs.getString("senha")); // Pega o hash da senha
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    
                    // Carrega os módulos permitidos da tabela de junção
                    usuario.setModulosPermitidos(carregarModulosDoUsuario(conn, usuario.getId()));
                    usuario.setUnidadesPermitidas(carregarUnidadesDoUsuario(conn, usuario.getId())); // ADICIONE ESTA LINHA
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao validar login: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao validar login: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro inesperado durante a validação de login.", e);
        }
        return usuario;
    }

    /**
     * Insere um novo usuário no banco de dados e suas permissões de módulo na tabela de junção.
     * A senha deve ser um hash BCrypt antes de ser passada para este método.
     * @param usuario O objeto Usuario a ser inserido, com a lista de módulos permitidos.
     * @return true se o usuário foi inserido com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public boolean cadastrarUsuario(Usuario usuario) throws SQLException {
        // Removida a coluna modulos_permitidos do INSERT
        String sql = "INSERT INTO usuarios (username, senha, perfil, email, ativo) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // Inicia a transação

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getUsername());
                ps.setString(2, BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt())); // Hash da senha antes de salvar
                ps.setString(3, usuario.getPerfil());
                ps.setString(4, usuario.getEmail());
                ps.setBoolean(5, usuario.isAtivo());
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback(); // Desfaz se o usuário não foi inserido
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1)); // Define o ID gerado para o objeto Usuario
                    } else {
                        conn.rollback(); // Desfaz se o ID não foi gerado
                        return false;
                    }
                }
            }

            // Salva as permissões de módulo na tabela de junção
            salvarModulosDoUsuario(conn, usuario.getId(), usuario.getModulosPermitidos());
            salvarUnidadesDoUsuario(conn, usuario.getId(), usuario.getUnidadesPermitidas()); //NOVO 04.03.2026

            conn.commit(); // Confirma a transação
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Desfaz em caso de erro
            }
            System.err.println("Erro SQL ao cadastrar usuário: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback(); // Desfaz em caso de erro
            }
            System.err.println("Erro inesperado ao cadastrar usuário: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro inesperado durante o cadastro do usuário.", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restaura o auto-commit
                conn.close();
            }
        }
    }

    /**
     * Lista todos os usuários cadastrados no banco de dados, incluindo seus módulos permitidos.
     * @return Uma lista de objetos Usuario.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public List<Usuario> listarTodosUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        // A query não busca mais 'modulos_permitidos' diretamente.
        String sql = "SELECT id, username, perfil, email, ativo FROM usuarios ORDER BY username";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario user = new Usuario();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPerfil(rs.getString("perfil"));
                user.setEmail(rs.getString("email"));
                user.setAtivo(rs.getBoolean("ativo"));
                
                // Carrega os módulos permitidos da tabela de junção
                user.setModulosPermitidos(carregarModulosDoUsuario(conn, user.getId()));
                user.setUnidadesPermitidas(carregarUnidadesDoUsuario(conn, user.getId())); // ADICIONE ESTA LINHA
                usuarios.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar usuários: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao listar usuários: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro inesperado durante a listagem de usuários.", e);
        }
        return usuarios;
    }

    /**
     * Busca um usuário pelo seu ID e carrega suas permissões de módulo.
     * Retorna o hash da senha, pois pode ser necessário para a atualização inteligente.
     * @param id O ID do usuário.
     * @return O objeto Usuario encontrado, ou null se não encontrado.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public Usuario buscarUsuarioPorId(int id) throws SQLException {
        String sql = "SELECT id, username, perfil, email, senha, ativo FROM usuarios WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPerfil(rs.getString("perfil"));
                    user.setEmail(rs.getString("email"));
                    user.setSenha(rs.getString("senha"));
                    user.setAtivo(rs.getBoolean("ativo"));
                    
                    // Carrega módulos e IDs de unidades (Strings)
                    user.setModulosPermitidos(carregarModulosDoUsuario(conn, user.getId()));
                    user.setUnidadesPermitidas(carregarUnidadesDoUsuario(conn, user.getId()));
                    
                    // NOVO: Carrega os nomes das unidades e define qual é a PADRÃO/ATIVA
                    carregarContextoDeUnidades(conn, user);
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao buscar usuário por ID: " + e.getMessage());
            throw e;
        }
        return null;
    }
    
    /**
     * Método auxiliar para carregar os objetos UnidadeDTO e definir a Unidade Ativa inicial.
     */
    private void carregarContextoDeUnidades(Connection conn, Usuario user) throws SQLException {
        List<UnidadeDTO> listaCompleta = new ArrayList<>();
        
        // Ajustado para apontar direto para a tabela 'filiais' e colunas reais do banco
        String sql = "SELECT uu.unidade_id, f.nome_empresa, uu.e_padrao " +
                     "FROM public.usuario_unidades uu " +
                     "JOIN public.filiais f ON uu.unidade_id = f.id_filial " +
                     "WHERE uu.usuario_id = ? " +
                     "ORDER BY uu.e_padrao DESC, f.nome_empresa ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("unidade_id");
                    String nome = rs.getString("nome_empresa"); // Pega o nome real da filial
                    boolean ePadrao = rs.getBoolean("e_padrao");

                    listaCompleta.add(new UnidadeDTO(id, nome));

                    if (ePadrao) {
                        user.setUnidadeAtivaId(id);
                        user.setUnidadeAtivaNome(nome);
                    }
                }
            }
        }
        
        if (user.getUnidadeAtivaId() == null && !listaCompleta.isEmpty()) {
            user.setUnidadeAtivaId(listaCompleta.get(0).getId());
            user.setUnidadeAtivaNome(listaCompleta.get(0).getNome());
        }

        user.setUnidadesPermitidasObjetos(listaCompleta);
    }
    /**
     * Atualiza os dados de um usuário existente e suas permissões de módulo.
     * Inclui a senha se um novo hash for fornecido.
     * @param usuario O objeto Usuario com os dados atualizados, incluindo a lista de módulos.
     * @return true se o usuário foi atualizado com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public boolean atualizarUsuario(Usuario usuario) throws SQLException {
        String sql;
        Connection conn = null;
        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // Inicia a transação

            if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
                // Removida a coluna modulos_permitidos do UPDATE
                sql = "UPDATE usuarios SET username = ?, perfil = ?, email = ?, senha = ?, ativo = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, usuario.getUsername());
                    ps.setString(2, usuario.getPerfil());
                    ps.setString(3, usuario.getEmail());
                    ps.setString(4, BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt())); // Hash da nova senha
                    ps.setBoolean(5, usuario.isAtivo());
                    ps.setInt(6, usuario.getId());
                    ps.executeUpdate();
                }
            } else {
                // Removida a coluna modulos_permitidos do UPDATE
                sql = "UPDATE usuarios SET username = ?, perfil = ?, email = ?, ativo = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, usuario.getUsername());
                    ps.setString(2, usuario.getPerfil());
                    ps.setString(3, usuario.getEmail());
                    ps.setBoolean(4, usuario.isAtivo());
                    ps.setInt(5, usuario.getId());
                    ps.executeUpdate();
                }
            }

            // Atualiza as permissões de módulo na tabela de junção
            // Primeiro, remove todas as permissões antigas para este usuário
            String deleteModulosSql = "DELETE FROM usuario_modulos WHERE usuario_id = ?";
            String deleteUnidadesSql = "DELETE FROM usuario_unidades WHERE usuario_id = ?"; //NOVO 04.03.2026
            try (PreparedStatement psDeleteModulos = conn.prepareStatement(deleteModulosSql);
            		PreparedStatement psDeleteUnidades = conn.prepareStatement(deleteUnidadesSql)) {
                psDeleteModulos.setInt(1, usuario.getId());
                psDeleteModulos.executeUpdate();
                
                psDeleteUnidades.setInt(1, usuario.getId()); //NOVO 04.03.2026 DELETA AS UNIDADES ANTIGAS
                psDeleteUnidades.executeUpdate(); //NOVO 04.03.2026
            }

            // Depois, insere as novas permissões
            salvarModulosDoUsuario(conn, usuario.getId(), usuario.getModulosPermitidos());
            salvarUnidadesDoUsuario(conn, usuario.getId(), usuario.getUnidadesPermitidas()); // INSERE AS UNIDADES NOVAS

            conn.commit(); // Confirma a transação
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Desfaz em caso de erro
            }
            System.err.println("Erro SQL ao atualizar usuário: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback(); // Desfaz em caso de erro
            }
            System.err.println("Erro inesperado ao atualizar usuário: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro inesperado durante a atualização do usuário.", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restaura o auto-commit
                conn.close();
            }
        }
    }

    /**
     * Exclui um usuário do banco de dados.
     * Devido ao ON DELETE CASCADE na FK, as permissões em usuario_modulos serão excluídas automaticamente.
     * @param id O ID do usuário a ser excluído.
     * @return true se o usuário foi excluído com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public boolean excluirUsuario(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro SQL ao excluir usuário: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Atualiza a senha de um usuário pelo username.
     * Este método pode ser usado pelo LoginServlet para alteração de senha.
     * A nova senha deve ser um hash BCrypt.
     * @param username O nome de usuário.
     * @param novaSenhaHash A nova senha já em formato hash BCrypt.
     * @return true se a senha foi atualizada com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public boolean atualizarSenha(String username, String novaSenhaHash) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ? WHERE username = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaSenhaHash);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar senha: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Exclui múltiplos usuários do banco de dados pelos seus IDs.
     * Devido ao ON DELETE CASCADE nas FKs, as permissões em usuario_modulos serão excluídas automaticamente.
     * @param ids Uma lista de IDs de usuários a serem excluídos.
     * @return O número de linhas afetadas (usuários excluídos).
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public int excluirMultiplosUsuarios(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        String placeholders = ids.stream()
                                 .map(id -> "?")
                                 .collect(Collectors.joining(", "));

        String sql = "DELETE FROM usuarios WHERE id IN (" + placeholders + ")";
        int linhasAfetadas = 0;
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            linhasAfetadas = ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao excluir múltiplos usuários: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao excluir múltiplos usuários: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro inesperado durante a exclusão de múltiplos usuários.", e);
        }
        return linhasAfetadas;
    }

    /**
     * Busca um usuário pelo seu nome de usuário.
     * Usado para verificar unicidade no cadastro.
     * @param username O nome de usuário a ser buscado.
     * @return O objeto Usuario encontrado, ou null se não existir.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public Usuario buscarUsuarioPorUsername(String username) throws SQLException {
        // A query não busca mais 'modulos_permitidos' diretamente.
        String sql = "SELECT id, username, email, senha, perfil, ativo FROM usuarios WHERE username = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenha(rs.getString("senha")); // Incluir a senha hash
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    // Carrega os módulos permitidos da tabela de junção
                    usuario.setModulosPermitidos(carregarModulosDoUsuario(conn, usuario.getId()));
                    return usuario;
                }
            }
        }
        return null;
    }

    /**
     * Busca um usuário pelo seu endereço de email.
     * Usado para a redefinição de senha.
     * @param email O email a ser buscado.
     * @return O objeto Usuario encontrado, ou null se não existir.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public Usuario buscarUsuarioPorEmail(String email) throws SQLException {
        // A query não busca mais 'modulos_permitidos' diretamente.
        String sql = "SELECT id, username, email, senha, perfil, ativo FROM usuarios WHERE email = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenha(rs.getString("senha")); // Incluir a senha hash
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    // Carrega os módulos permitidos da tabela de junção
                    usuario.setModulosPermitidos(carregarModulosDoUsuario(conn, usuario.getId()));
                    return usuario;
                }
            }
        }
        return null;
    }

    /**
     * Lista os nomes de todos os módulos disponíveis no sistema.
     * Assume que existe uma tabela 'modulos' com uma coluna 'nome_modulo'.
     * @return Uma lista de strings com os nomes dos módulos.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public List<String> listarNomesModulos() throws SQLException {
        List<String> modulos = new ArrayList<>();
        String sql = "SELECT nome_modulo FROM modulos ORDER BY nome_modulo";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modulos.add(rs.getString("nome_modulo"));
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar nomes de módulos: " + e.getMessage());
            throw e;
        }
        return modulos;
    }
    
    // NOVO METODO 04.03.2026
    

    // --- NOVOS MÉTODOS PARA REDEFINIÇÃO DE SENHA (Sem Alterações) ---

    /**
     * Salva um token de redefinição de senha no banco de dados.
     * Primeiro, invalida/remove tokens antigos ou existentes para este usuário.
     * @param idUsuario O ID do usuário.
     * @param token O token gerado.
     * @param expiraEm A data/hora de expiração do token.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public void salvarTokenRedefinicao(int idUsuario, String token, LocalDateTime expiraEm) throws SQLException {
        // Primeiro, invalida/remove tokens antigos ou existentes para este usuário
        String deleteSql = "DELETE FROM password_reset_tokens WHERE id_usuario = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, idUsuario);
            deleteStmt.executeUpdate();
        }

        // Insere o novo token
        String insertSql = "INSERT INTO password_reset_tokens (id_usuario, token, expira_em, usado, criado_em) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, idUsuario);
            insertStmt.setString(2, token);
            insertStmt.setTimestamp(3, Timestamp.valueOf(expiraEm));
            insertStmt.setBoolean(4, false); // Token não usado inicialmente
            insertStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now())); // Data de criação
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao salvar token de redefinição: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Busca um token de redefinição de senha e retorna um objeto PasswordResetToken.
     * @param token O token a ser buscado.
     * @return Um objeto Usuario.PasswordResetToken se encontrado e válido, ou null.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public Usuario.PasswordResetToken buscarTokenRedefinicao(String token) throws SQLException {
        String sql = "SELECT id, id_usuario, token, expira_em, usado, criado_em FROM password_reset_tokens WHERE token = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario.PasswordResetToken(
                        rs.getInt("id"),
                        rs.getInt("id_usuario"),
                        rs.getString("token"),
                        rs.getTimestamp("expira_em").toLocalDateTime(),
                        rs.getBoolean("usado"),
                        rs.getTimestamp("criado_em").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao buscar token de redefinição: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Marca um token de redefinição de senha como usado.
     * @param token O token a ser marcado.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public void marcarTokenComoUsado(String token) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET usado = true WHERE token = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao marcar token como usado: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Atualiza a senha de um usuário pelo seu ID.
     * @param userId O ID do usuário.
     * @param novaSenha O nova senha (será hash BCrypt dentro do método).
     * @return true se a senha foi atualizada com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public boolean atualizarSenhaUsuario(int userId, String novaSenha) throws SQLException {
        String hashedNewSenha = BCrypt.hashpw(novaSenha, BCrypt.gensalt()); // Hash da nova senha
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedNewSenha);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar senha por ID: " + e.getMessage());
            throw e;
        }
    }

    // --- MÉTODOS AUXILIARES PARA GERENCIAR MÓDULOS (NOVOS OU MODIFICADOS) ---

    /**
     * Carrega a lista de nomes de módulos associados a um usuário específico
     * da tabela de junção 'usuario_modulos'.
     * @param conn A conexão JDBC (passada para reuso em transações).
     * @param userId O ID do usuário.
     * @return Uma lista de strings com os nomes dos módulos.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    private List<String> carregarModulosDoUsuario(Connection conn, int userId) throws SQLException {
        List<String> modulos = new ArrayList<>();
        String sql = "SELECT m.nome_modulo FROM modulos m " +
                     "JOIN usuario_modulos um ON m.id = um.modulo_id " +
                     "WHERE um.usuario_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modulos.add(rs.getString("nome_modulo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao carregar módulos do usuário: " + e.getMessage());
            throw e;
        }
        return modulos;
    }

    /**
     * Salva as permissões de módulo para um usuário específico na tabela de junção 'usuario_modulos'.
     * Este método assume que permissões antigas para este usuário já foram removidas, se necessário.
     * @param conn A conexão JDBC (passada para reuso em transações).
     * @param userId O ID do usuário.
     * @param modulosPermitidos Uma lista de nomes de módulos a serem associados.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    private void salvarModulosDoUsuario(Connection conn, int userId, List<String> modulosPermitidos) throws SQLException {
        if (modulosPermitidos == null || modulosPermitidos.isEmpty()) {
            return; // Nada para salvar
        }

        // Prepara o SQL para inserir múltiplas permissões
        String sql = "INSERT INTO usuario_modulos (usuario_id, modulo_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String nomeModulo : modulosPermitidos) {
                int moduloId = getModuloIdByNome(conn, nomeModulo);
                if (moduloId != -1) { // Garante que o módulo existe
                    ps.setInt(1, userId);
                    ps.setInt(2, moduloId);
                    ps.addBatch(); // Adiciona ao batch para inserção eficiente
                } else {
                    System.err.println("Aviso: Módulo '" + nomeModulo + "' não encontrado na tabela 'modulos'. Permissão ignorada para o usuário ID " + userId);
                }
            }
            ps.executeBatch(); // Executa todas as inserções em lote
        } catch (SQLException e) {
            System.err.println("Erro SQL ao salvar módulos do usuário: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Busca o ID de um módulo dado o seu nome.
     * @param conn A conexão JDBC (passada para reuso em transações).
     * @param nomeModulo O nome do módulo.
     * @return O ID do módulo, ou -1 se não encontrado.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    private int getModuloIdByNome(Connection conn, String nomeModulo) throws SQLException {
        String sql = "SELECT id FROM modulos WHERE nome_modulo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeModulo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao buscar ID do módulo por nome: " + e.getMessage());
            throw e;
        }
        return -1; // Módulo não encontrado
    }
    
    /**
     * Carrega a lista de códigos de unidades associadas a um usuário. NOVOS 04.03.2026
     */
    private List<String> carregarUnidadesDoUsuario(Connection conn, int userId) throws SQLException {
        List<String> unidades = new ArrayList<>();
        // Ajustado para bater com sua coluna 'unidade_id'
        String sql = "SELECT unidade_id FROM public.usuario_unidades WHERE usuario_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Convertemos para String aqui para manter a compatibilidade com o resto do sistema
                    unidades.add(String.valueOf(rs.getInt("unidade_id")));
                }
            }
        }
        return unidades;
    }
    
    /**
     * Busca apenas o ID da unidade que está marcada como padrão no banco.NOVO 12.03.2026
     */
    private Integer buscarIdUnidadePadraoAtual(Connection conn, int userId) throws SQLException {
        String sql = "SELECT unidade_id FROM public.usuario_unidades WHERE usuario_id = ? AND e_padrao = true";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("unidade_id");
            }
        }
        return null;
    }

    /**
     * Salva as unidades permitidas para um usuário na tabela de junção.
     * Limpa os registros antigos antes de inserir os novos para evitar duplicidade.
     */
    private void salvarUnidadesDoUsuario(Connection conn, int userId, List<String> unidadesPermitidas) throws SQLException {
        // 1. Limpa os vínculos atuais
        String sqlDelete = "DELETE FROM public.usuario_unidades WHERE usuario_id = ?";
        try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
            psDelete.setInt(1, userId);
            psDelete.executeUpdate();
        }

        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) return;

        // 2. O Servlet agora garante que o INDEX 0 é a Unidade Principal escolhida no Select
        int idParaSerPadrao = Integer.parseInt(unidadesPermitidas.get(0).trim());

        // 3. Grava as novas unidades
        String sqlInsert = "INSERT INTO public.usuario_unidades (usuario_id, unidade_id, e_padrao) VALUES (?, ?, ?)";
        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
            for (String idStr : unidadesPermitidas) {
                int unidadeId = Integer.parseInt(idStr.trim());
                psInsert.setInt(1, userId);
                psInsert.setInt(2, unidadeId);
                
                // Regra simplificada: O primeiro item (Index 0) é SEMPRE o padrão
                psInsert.setBoolean(3, (unidadeId == idParaSerPadrao)); 
                
                psInsert.addBatch();
            }
            psInsert.executeBatch();
        }
    }
    
    public List<String[]> listarUnidadesDisponiveis() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        // Ajustado para apontar para a tabela 'filiais' e colunas 'id_filial' e 'nome_empresa' do NexaCore
        String sql = "SELECT id_filial, nome_empresa FROM filiais ORDER BY nome_empresa ASC";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexao.Conexao.conectar(); 
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_filial")), 
                    rs.getString("nome_empresa")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades no UsuarioDAO: " + e.getMessage());
            throw e;
        } finally {
            conexao.Conexao.fechar(rs, ps, conn);
        }
        return lista;
    }
    
}

