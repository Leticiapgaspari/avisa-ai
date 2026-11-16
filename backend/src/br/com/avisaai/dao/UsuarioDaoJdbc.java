package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Usuario;

public class UsuarioDaoJdbc implements UsuarioDao {

    private static final String SQL_INSERIR =
            "INSERT INTO USUARIO (NOME, EMAIL, SENHA_HASH, TIPO, ID_BAIRRO) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT =
            "SELECT u.ID_USUARIO, u.NOME, u.EMAIL, u.TIPO, b.ID_BAIRRO, b.NOME AS NOME_BAIRRO "
            + "FROM USUARIO u JOIN BAIRRO b ON b.ID_BAIRRO = u.ID_BAIRRO ";

    private static final String SQL_LISTAR = SQL_SELECT + "ORDER BY NLSSORT(u.NOME, 'NLS_SORT=BINARY_AI')";
    private static final String SQL_BUSCAR_ID = SQL_SELECT + "WHERE u.ID_USUARIO = ?";
    private static final String SQL_BUSCAR_EMAIL = SQL_SELECT + "WHERE u.EMAIL = ?";

    private static final String SQL_ATUALIZAR =
            "UPDATE USUARIO SET NOME = ?, EMAIL = ?, TIPO = ?, ID_BAIRRO = ? WHERE ID_USUARIO = ?";
    // os apoios saem junto (ON DELETE CASCADE)
    private static final String SQL_EXCLUIR = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";

    private static final String SQL_BUSCAR_HASH = "SELECT SENHA_HASH FROM USUARIO WHERE ID_USUARIO = ?";
    private static final String SQL_ATUALIZAR_HASH = "UPDATE USUARIO SET SENHA_HASH = ? WHERE ID_USUARIO = ?";

    @Override
    public Usuario inserir(Usuario usuario, String senhaHash) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERIR, new String[] {"ID_USUARIO"})) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, senhaHash);
            ps.setString(4, usuario.getTipo());
            ps.setInt(5, usuario.getBairro().getId());
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
    public List<Usuario> listar() throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
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
    public Usuario buscarPorId(int id) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Usuario buscarPorEmail(String email) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Usuario usuario) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
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
    public boolean excluir(int id) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXCLUIR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public String buscarSenhaHash(int id) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_HASH)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("SENHA_HASH") : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizarSenhaHash(int id, String senhaHash) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_HASH)) {
            ps.setString(1, senhaHash);
            ps.setInt(2, id);
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
