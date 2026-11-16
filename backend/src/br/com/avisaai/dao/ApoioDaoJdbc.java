package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Apoio;

public class ApoioDaoJdbc implements ApoioDao {

    private static final String SQL_INSERIR =
            "INSERT INTO APOIO (ID_USUARIO, ID_OCORRENCIA, DATA_APOIO) VALUES (?, ?, ?)";
    private static final String SQL_EXCLUIR = "DELETE FROM APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
    private static final String SQL_EXISTE = "SELECT 1 FROM APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
    private static final String SQL_OCORRENCIAS_APOIADAS =
            "SELECT ID_OCORRENCIA FROM APOIO WHERE ID_USUARIO = ? ORDER BY ID_OCORRENCIA";

    @Override
    public void inserir(Apoio apoio) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERIR)) {
            ps.setInt(1, apoio.getUsuario().getId());
            ps.setInt(2, apoio.getOcorrencia().getId());
            ps.setDate(3, Date.valueOf(apoio.getDataApoio()));
            ps.executeUpdate();
        } catch (SQLException e) {
            // apoio repetido cai na PK do APOIO e vira DUPLICADO
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int idUsuario, int idOcorrencia) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXCLUIR)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idOcorrencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean existe(int idUsuario, int idOcorrencia) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTE)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Integer> listarOcorrenciasApoiadas(int idUsuario) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_OCORRENCIAS_APOIADAS)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new ArrayList<Integer>();
                while (rs.next()) {
                    ids.add(rs.getInt("ID_OCORRENCIA"));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
