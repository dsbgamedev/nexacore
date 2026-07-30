package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import conexao.Conexao;
import model.Departamento;

public class DepartamentoDAO {

    public List<Departamento> listarTodos() throws Exception {
        List<Departamento> departamentos = new ArrayList<>();
        String sql = "SELECT id_departamento, nome_departamento FROM public.departamentos ORDER BY nome_departamento ASC";

        // Usando o seu método correto: Conexao.conectar()
        try (Connection conexao = Conexao.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Departamento d = new Departamento();
                d.setIdDepartamento(rs.getInt("id_departamento"));
                d.setNomeDepartamento(rs.getString("nome_departamento"));
                departamentos.add(d);
            }
        }
        return departamentos;
    }
}
