package dao;

import conexao.Conexao; // Importando a sua classe de conexão
import model.MovimentacaoEnvio;
import java.sql.*;
import java.util.List;

public class MovimentacaoEnvioDAO {

    public Long inserir(MovimentacaoEnvio envio, List<Long> idsEquipamentos) throws SQLException {
        String sqlEnvio = "INSERT INTO movimentacao_envio (data_envio, origem_id, destino_id, responsavel, transportadora, codigo_rastreio, data_previsa_entrega, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_envio";
        String sqlItem = "INSERT INTO movimentacao_envio_itens (id_envio, id_equipamento, status_equipamento_momento) VALUES (?, ?, ?)";
        String sqlUpdateEquipamento = "UPDATE equipamentos SET status_atual = 'Em Trânsito' WHERE id_equipamento = ?";

        Connection conn = null;
        PreparedStatement stmtEnvio = null;
        PreparedStatement stmtItem = null;
        PreparedStatement stmtUpdate = null;
        ResultSet rs = null;
        Long idEnvioGerado = null;

        try {
            conn = Conexao.conectar(); // Usando o seu método de conexão
            conn.setAutoCommit(false); // Transação para garantir integridade

            // 1. Inserir Cabeçalho
            stmtEnvio = conn.prepareStatement(sqlEnvio);
            stmtEnvio.setDate(1, Date.valueOf(envio.getDataEnvio()));
            stmtEnvio.setLong(2, envio.getOrigemId());
            stmtEnvio.setLong(3, envio.getDestinoId());
            stmtEnvio.setString(4, envio.getResponsavel());
            stmtEnvio.setString(5, envio.getTransportadora());
            stmtEnvio.setString(6, envio.getCodigoRastreio());
            if (envio.getDataPrevisaoEntrega() != null) {
                stmtEnvio.setDate(7, Date.valueOf(envio.getDataPrevisaoEntrega()));
            } else {
                stmtEnvio.setNull(7, Types.DATE);
            }
            stmtEnvio.setString(8, envio.getObservacoes());

            rs = stmtEnvio.executeQuery();
            if (rs.next()) {
                idEnvioGerado = rs.getLong(1);
            }

            // 2. Inserir Itens e Atualizar Status dos Equipamentos
            stmtItem = conn.prepareStatement(sqlItem);
            stmtUpdate = conn.prepareStatement(sqlUpdateEquipamento);

            for (Long idEquipamento : idsEquipamentos) {
                // Inserir item no envio
                stmtItem.setLong(1, idEnvioGerado);
                stmtItem.setLong(2, idEquipamento);
                stmtItem.setString(3, "Em Trânsito");
                stmtItem.addBatch();

                // Atualizar tabela principal de equipamentos
                stmtUpdate.setLong(1, idEquipamento);
                stmtUpdate.addBatch();
            }

            stmtItem.executeBatch();
            stmtUpdate.executeBatch();

            conn.commit(); // Confirma a transação
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            // Fechamento seguro individual de cada recurso para evitar vazamento de conexão (Connection Leak)
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) { e.printStackTrace(); }

            try {
                if (stmtEnvio != null) stmtEnvio.close();
            } catch (SQLException e) { e.printStackTrace(); }

            try {
                if (stmtItem != null) stmtItem.close();
            } catch (SQLException e) { e.printStackTrace(); }

            try {
                if (stmtUpdate != null) stmtUpdate.close();
            } catch (SQLException e) { e.printStackTrace(); }

            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Boa prática restaurar o autocommit padrão
                    conn.close();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        return idEnvioGerado;
    }
}