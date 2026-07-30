package conexao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Importar Statement

public class Conexao {
    // Adicionado ?client_encoding=UTF8 para garantir a codificação correta
    //private static final String URL = "jdbc:postgresql://localhost:5432/nexacore?client_encoding=UTF8";
    private static final String URL = "jdbc:postgresql://localhost:5432/nexacore?currentSchema=public&client_encoding=UTF8";
    // private static final String URL = "jdbc:postgresql://localhost:5432/teste?client_encoding=UTF8"; // Se usar o banco 'teste'
    private static final String USER = "postgres";
    private static final String PASS = "cba";

    // Bloco estático para carregar o driver JDBC uma vez
    static {
        try {
            // Carrega o driver JDBC para PostgreSQL
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver JDBC carregado com sucesso.");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar o driver JDBC: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao carregar o driver JDBC.", e);
        }
    }

    public static Connection conectar() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            //DatabaseMetaData meta = conn.getMetaData();
            //System.out.println("URL DE CONEXÃO REAL: " + meta.getURL());
           // System.out.println("NOME DO PRODUTO: " + meta.getDatabaseProductName());
            // System.out.println("Conexão com o banco de dados estabelecida."); // Opcional para depuração
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relança a exceção para que o chamador possa tratá-la
        }
        return conn;
    }

    /**
     * Fecha os recursos do banco de dados (ResultSet, PreparedStatement, Connection).
     * É seguro chamar mesmo que um dos parâmetros seja null.
     * @param rs ResultSet a ser fechado (pode ser null)
     * @param stmt PreparedStatement a ser fechado (pode ser null)
     * @param conn Connection a ser fechada (pode ser null)
     */
    public static void fechar(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            // Importante: Só fecha a conexão aqui se ela não for nula.
            // Em DAOs com injeção de conexão, a conexão pode ser gerenciada externamente.
            if (conn != null) {
                if (!conn.isClosed()) { // Verifica se já não está fechada
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar recursos do banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NOVO MÉTODO: Fecha os recursos do banco de dados (ResultSet, Statement, Connection).
     * Esta é uma sobrecarga para aceitar Statement genérico.
     * @param rs ResultSet a ser fechado (pode ser null)
     * @param stmt Statement a ser fechado (pode ser null)
     * @param conn Connection a ser fechada (pode ser null)
     */
    public static void fechar(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                if (!conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar recursos do banco de dados (Statement): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
