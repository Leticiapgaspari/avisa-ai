package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.avisaai.model.Apoio;

public class ApoioDaoJdbc implements ApoioDao {

    @Override
    public void inserir(Apoio apoio) {
        String sql = "INSERT INTO APOIO (ID_USUARIO, ID_OCORRENCIA, DATA_APOIO) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, apoio.getUsuario().getId());
            ps.setInt(2, apoio.getOcorrencia().getId());
            ps.setDate(3, Date.valueOf(apoio.getDataApoio()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int idUsuario, int idOcorrencia) {
        String sql = "DELETE FROM APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idOcorrencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean existe(int idUsuario, int idOcorrencia) {
        String sql = "SELECT 1 FROM APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
