package util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FiltroUnidadeUtil {

    /**
     * Gera a string de placeholders para cláusulas IN, ex: "(?, ?, ?)"
     */
    public static String gerarClausulaIn(List<?> lista) {
        if (lista == null || lista.isEmpty()) {
            return "(NULL)";
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Preenche os parâmetros inteiros no PreparedStatement a partir de um índice inicial
     */
    public static int preencherParametros(PreparedStatement stmt, int indiceInicial, List<Integer> unidades) throws SQLException {
        if (unidades != null) {
            for (int i = 0; i < unidades.size(); i++) {
                stmt.setInt(indiceInicial + i, unidades.get(i));
            }
            return indiceInicial + unidades.size();
        }
        return indiceInicial;
    }
}