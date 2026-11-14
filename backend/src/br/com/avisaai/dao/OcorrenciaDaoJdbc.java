package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Categoria;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;
import br.com.avisaai.model.Usuario;

public class OcorrenciaDaoJdbc implements OcorrenciaDao {

    private static final String SELECT =
            "SELECT o.ID_OCORRENCIA, o.TITULO, o.DESCRICAO, o.ENDERECO, o.STATUS, o.DATA_ABERTURA, "
            + "u.ID_USUARIO, u.NOME AS NOME_AUTOR, u.TIPO AS TIPO_AUTOR, "
            + "c.ID_CATEGORIA, c.NOME AS NOME_CATEGORIA, "
            + "b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, "
            + "(SELECT COUNT(*) FROM APOIO a WHERE a.ID_OCORRENCIA = o.ID_OCORRENCIA) AS TOTAL_APOIOS "
            + "FROM OCORRENCIA o "
            + "JOIN USUARIO u ON u.ID_USUARIO = o.ID_USUARIO "
            + "JOIN CATEGORIA c ON c.ID_CATEGORIA = o.ID_CATEGORIA "
            + "JOIN BAIRRO b ON b.ID_BAIRRO = o.ID_BAIRRO ";

    @Override
    public Ocorrencia inserir(Ocorrencia o) {
        String sql = "INSERT INTO OCORRENCIA (TITULO, DESCRICAO, ENDERECO, STATUS, DATA_ABERTURA, "
                + "ID_USUARIO, ID_CATEGORIA, ID_BAIRRO) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_OCORRENCIA"})) {
            ps.setString(1, o.getTitulo());
            setTextoOuNulo(ps, 2, o.getDescricao());
            ps.setString(3, o.getEndereco());
            ps.setString(4, o.getStatus().name());
            ps.setDate(5, Date.valueOf(o.getDataAbertura()));
            ps.setInt(6, o.getAutor().getId());
            ps.setInt(7, o.getCategoria().getId());
            ps.setInt(8, o.getBairro().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    o.setId(rs.getInt(1));
                }
            }
            return o;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Ocorrencia> listar(Integer idBairro, StatusOcorrencia status) {
        String sql = SELECT
                + "WHERE (? IS NULL OR o.ID_BAIRRO = ?) AND (? IS NULL OR o.STATUS = ?) "
                + "ORDER BY o.DATA_ABERTURA DESC, o.ID_OCORRENCIA DESC";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idBairro == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, idBairro);
                ps.setInt(2, idBairro);
            }
            String textoStatus = status == null ? null : status.name();
            setTextoOuNulo(ps, 3, textoStatus);
            setTextoOuNulo(ps, 4, textoStatus);
            try (ResultSet rs = ps.executeQuery()) {
                List<Ocorrencia> lista = new ArrayList<Ocorrencia>();
                while (rs.next()) {
                    lista.add(montar(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public Ocorrencia buscarPorId(int id) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE o.ID_OCORRENCIA = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Ocorrencia o) {
        String sql = "UPDATE OCORRENCIA SET TITULO = ?, DESCRICAO = ?, ENDERECO = ?, ID_CATEGORIA = ?, "
                + "ID_BAIRRO = ? WHERE ID_OCORRENCIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, o.getTitulo());
            setTextoOuNulo(ps, 2, o.getDescricao());
            ps.setString(3, o.getEndereco());
            ps.setInt(4, o.getCategoria().getId());
            ps.setInt(5, o.getBairro().getId());
            ps.setInt(6, o.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizarStatus(int id, StatusOcorrencia status) {
        String sql = "UPDATE OCORRENCIA SET STATUS = ? WHERE ID_OCORRENCIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean excluir(int id) {
        String sql = "DELETE FROM OCORRENCIA WHERE ID_OCORRENCIA = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    private Ocorrencia montar(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getInt("ID_OCORRENCIA"));
        o.setTitulo(rs.getString("TITULO"));
        o.setDescricao(rs.getString("DESCRICAO"));
        o.setEndereco(rs.getString("ENDERECO"));
        o.setStatus(StatusOcorrencia.deTexto(rs.getString("STATUS")));
        o.setDataAbertura(rs.getDate("DATA_ABERTURA").toLocalDate());

        Usuario autor = Usuario.criar(rs.getString("TIPO_AUTOR"));
        autor.setId(rs.getInt("ID_USUARIO"));
        autor.setNome(rs.getString("NOME_AUTOR"));
        o.setAutor(autor);

        o.setCategoria(new Categoria(rs.getInt("ID_CATEGORIA"), rs.getString("NOME_CATEGORIA")));
        o.setBairro(new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME_BAIRRO")));
        o.setTotalApoios(rs.getInt("TOTAL_APOIOS"));
        return o;
    }

    private static void setTextoOuNulo(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }
}
