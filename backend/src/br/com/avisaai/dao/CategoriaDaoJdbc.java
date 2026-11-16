package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Categoria;

public class CategoriaDaoJdbc implements CategoriaDao {

    private static final String SQL_INSERIR = "INSERT INTO CATEGORIA (NOME) VALUES (?)";
    // ordem alfabética ignorando acento e maiúscula (senão "Árvore" vai pro fim)
    private static final String SQL_LISTAR =
            "SELECT ID_CATEGORIA, NOME FROM CATEGORIA ORDER BY NLSSORT(NOME, 'NLS_SORT=BINARY_AI')";
    private static final String SQL_BUSCAR = "SELECT ID_CATEGORIA, NOME FROM CATEGORIA WHERE ID_CATEGORIA = ?";
    private static final String SQL_ATUALIZAR = "UPDATE CATEGORIA SET NOME = ? WHERE ID_CATEGORIA = ?";
    private static final String SQL_EXCLUIR = "DELETE FROM CATEGORIA WHERE ID_CATEGORIA = ?";

    @Override
    public Categoria inserir(Categoria categoria) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERIR, new String[] {"ID_CATEGORIA"})) {
            ps.setString(1, categoria.getNome());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Categoria> listar() throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<Categoria>();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws DadosException {
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
    public boolean atualizar(Categoria categoria) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, categoria.getNome());
            ps.setInt(2, categoria.getId());
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

    private Categoria montar(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("ID_CATEGORIA"), rs.getString("NOME"));
    }
}
