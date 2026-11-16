package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;

public class BairroDaoJdbc implements BairroDao {

    private static final String SQL_INSERIR = "INSERT INTO BAIRRO (NOME) VALUES (?)";
    // ordem alfabética ignorando acento e maiúscula
    private static final String SQL_LISTAR =
            "SELECT ID_BAIRRO, NOME FROM BAIRRO ORDER BY NLSSORT(NOME, 'NLS_SORT=BINARY_AI')";
    private static final String SQL_BUSCAR = "SELECT ID_BAIRRO, NOME FROM BAIRRO WHERE ID_BAIRRO = ?";
    private static final String SQL_ATUALIZAR = "UPDATE BAIRRO SET NOME = ? WHERE ID_BAIRRO = ?";
    private static final String SQL_EXCLUIR = "DELETE FROM BAIRRO WHERE ID_BAIRRO = ?";

    @Override
    public Bairro inserir(Bairro bairro) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERIR, new String[] {"ID_BAIRRO"})) {
            ps.setString(1, bairro.getNome());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bairro.setId(rs.getInt(1));
                }
            }
            return bairro;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Bairro> listar() throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            List<Bairro> lista = new ArrayList<Bairro>();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Bairro buscarPorId(int id) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Bairro bairro) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, bairro.getNome());
            ps.setInt(2, bairro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int id) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXCLUIR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    private Bairro montar(ResultSet rs) throws SQLException {
        return new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME"));
    }
}
