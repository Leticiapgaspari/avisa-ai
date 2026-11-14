package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.model.Categoria;

public class CategoriaDaoJdbc implements CategoriaDao {

    @Override
    public Categoria inserir(Categoria categoria) {
        String sql = "INSERT INTO CATEGORIA (NOME) VALUES (?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_CATEGORIA"})) {
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
    public List<Categoria> listar() {
        String sql = "SELECT ID_CATEGORIA, NOME FROM CATEGORIA ORDER BY NOME";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<Categoria>();
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("ID_CATEGORIA"), rs.getString("NOME")));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) {
        String sql = "SELECT ID_CATEGORIA, NOME FROM CATEGORIA WHERE ID_CATEGORIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt("ID_CATEGORIA"), rs.getString("NOME")) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE CATEGORIA SET NOME = ? WHERE ID_CATEGORIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setInt(2, categoria.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int id) {
        String sql = "DELETE FROM CATEGORIA WHERE ID_CATEGORIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
