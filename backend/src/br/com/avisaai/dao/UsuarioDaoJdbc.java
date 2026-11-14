package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Usuario;

public class UsuarioDaoJdbc implements UsuarioDao {

    private static final String SELECT =
            "SELECT u.ID_USUARIO, u.NOME, u.EMAIL, u.TIPO, b.ID_BAIRRO, b.NOME AS NOME_BAIRRO "
            + "FROM USUARIO u JOIN BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO ";

    @Override
    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO USUARIO (NOME, EMAIL, TIPO, ID_BAIRRO) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_USUARIO"})) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTipo());
            ps.setInt(4, usuario.getBairro().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Usuario> listar() {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "ORDER BY u.NOME");
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> lista = new ArrayList<Usuario>();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE u.ID_USUARIO = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE u.EMAIL = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE USUARIO SET NOME = ?, EMAIL = ?, TIPO = ?, ID_BAIRRO = ? WHERE ID_USUARIO = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTipo());
            ps.setInt(4, usuario.getBairro().getId());
            ps.setInt(5, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int id) {
        String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    private Usuario montar(ResultSet rs) throws SQLException {
        Usuario u = Usuario.criar(rs.getString("TIPO"));
        u.setId(rs.getInt("ID_USUARIO"));
        u.setNome(rs.getString("NOME"));
        u.setEmail(rs.getString("EMAIL"));
        u.setBairro(new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME_BAIRRO")));
        return u;
    }
}
