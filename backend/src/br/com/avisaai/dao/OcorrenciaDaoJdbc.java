package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Categoria;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;
import br.com.avisaai.model.Usuario;

public class OcorrenciaDaoJdbc implements OcorrenciaDao {

    private static final String SQL_INSERIR =
            "INSERT INTO OCORRENCIA (TITULO, DESCRICAO, ENDERECO, STATUS, DATA_ABERTURA, "
            + "ID_USUARIO, ID_CATEGORIA, ID_BAIRRO) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT =
            "SELECT o.ID_OCORRENCIA, o.TITULO, o.DESCRICAO, o.ENDERECO, o.STATUS, o.DATA_ABERTURA, "
            + "u.ID_USUARIO, u.NOME AS NOME_AUTOR, u.TIPO, "
            + "c.ID_CATEGORIA, c.NOME AS NOME_CATEGORIA, "
            + "b.ID_BAIRRO, b.NOME AS NOME_BAIRRO, "
            + "(SELECT COUNT(*) FROM APOIO a WHERE a.ID_OCORRENCIA = o.ID_OCORRENCIA) AS TOTAL_APOIOS "
            + "FROM OCORRENCIA o "
            + "JOIN USUARIO u ON u.ID_USUARIO = o.ID_USUARIO "
            + "JOIN CATEGORIA c ON c.ID_CATEGORIA = o.ID_CATEGORIA "
            + "JOIN BAIRRO b ON b.ID_BAIRRO = o.ID_BAIRRO ";

    // parâmetro null = sem filtro
    private static final String SQL_LISTAR = SQL_SELECT
            + "WHERE (? IS NULL OR o.ID_BAIRRO = ?) "
            + "AND (? IS NULL OR o.STATUS = ?) "
            + "ORDER BY o.DATA_ABERTURA DESC, o.ID_OCORRENCIA DESC";

    private static final String SQL_BUSCAR = SQL_SELECT + "WHERE o.ID_OCORRENCIA = ?";

    // status, autor e data não mudam na edição
    private static final String SQL_ATUALIZAR =
            "UPDATE OCORRENCIA SET TITULO = ?, DESCRICAO = ?, ENDERECO = ?, ID_CATEGORIA = ?, ID_BAIRRO = ? "
            + "WHERE ID_OCORRENCIA = ?";
    private static final String SQL_ATUALIZAR_STATUS = "UPDATE OCORRENCIA SET STATUS = ? WHERE ID_OCORRENCIA = ?";
    // os apoios saem junto (ON DELETE CASCADE)
    private static final String SQL_EXCLUIR = "DELETE FROM OCORRENCIA WHERE ID_OCORRENCIA = ?";

    @Override
    public Ocorrencia inserir(Ocorrencia ocorrencia) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERIR, new String[] {"ID_OCORRENCIA"})) {
            ps.setString(1, ocorrencia.getTitulo());
            definirTextoOpcional(ps, 2, ocorrencia.getDescricao());
            ps.setString(3, ocorrencia.getEndereco());
            ps.setString(4, ocorrencia.getStatus().name());
            ps.setDate(5, Date.valueOf(ocorrencia.getDataAbertura()));
            ps.setInt(6, ocorrencia.getAutor().getId());
            ps.setInt(7, ocorrencia.getCategoria().getId());
            ps.setInt(8, ocorrencia.getBairro().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ocorrencia.setId(rs.getInt(1));
                }
            }
            return ocorrencia;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public List<Ocorrencia> listar(Integer idBairro, StatusOcorrencia status) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            if (idBairro == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, idBairro);
                ps.setInt(2, idBairro);
            }
            String textoStatus = status == null ? null : status.name();
            definirTextoOpcional(ps, 3, textoStatus);
            definirTextoOpcional(ps, 4, textoStatus);
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
    public Ocorrencia buscarPorId(int id) throws DadosException {
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
    public boolean atualizar(Ocorrencia ocorrencia) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, ocorrencia.getTitulo());
            definirTextoOpcional(ps, 2, ocorrencia.getDescricao());
            ps.setString(3, ocorrencia.getEndereco());
            ps.setInt(4, ocorrencia.getCategoria().getId());
            ps.setInt(5, ocorrencia.getBairro().getId());
            ps.setInt(6, ocorrencia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    @Override
    public boolean atualizarStatus(int id, StatusOcorrencia status) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
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

    private void definirTextoOpcional(PreparedStatement ps, int posicao, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicao, Types.VARCHAR);
        } else {
            ps.setString(posicao, valor);
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

        Usuario autor = Usuario.criar(rs.getString("TIPO"));
        autor.setId(rs.getInt("ID_USUARIO"));
        autor.setNome(rs.getString("NOME_AUTOR"));
        o.setAutor(autor);

        o.setCategoria(new Categoria(rs.getInt("ID_CATEGORIA"), rs.getString("NOME_CATEGORIA")));
        o.setBairro(new Bairro(rs.getInt("ID_BAIRRO"), rs.getString("NOME_BAIRRO")));
        o.setTotalApoios(rs.getInt("TOTAL_APOIOS"));
        return o;
    }
}
