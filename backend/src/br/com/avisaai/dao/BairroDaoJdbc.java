package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.model.Bairro;

public class BairroDaoJdbc implements BairroDao {

    @Override
    public Bairro inserir(Bairro bairro) {
        String sql = "INSERT INTO BAIRRO (NOME) VALUES (?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_BAIRRO"})) {
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
    public List<Bairro> listar() {
        String sql = "SELECT ID_BAIRRO, NOME FROM BAIRRO ORDER BY NOME";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Bairro> lista = new ArrayList<Bairro>();
            while (rs.next()) {
                lista.add(new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME")));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Bairro buscarPorId(int id) {
        String sql = "SELECT ID_BAIRRO, NOME FROM BAIRRO WHERE ID_BAIRRO = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME")) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Bairro bairro) {
        String sql = "UPDATE BAIRRO SET NOME = ? WHERE ID_BAIRRO = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bairro.getNome());
            ps.setInt(2, bairro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int id) {
        String sql = "DELETE FROM BAIRRO WHERE ID_BAIRRO = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
