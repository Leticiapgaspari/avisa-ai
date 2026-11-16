package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.dto.LinhaRanking;
import br.com.avisaai.dto.LinhaRelatorio;
import br.com.avisaai.exception.DadosException;

// mesmas consultas do sql/relatorios.sql
public class RelatorioDaoJdbc implements RelatorioDao {

    private static final String SQL_POR_BAIRRO =
            "SELECT b.NOME, COUNT(o.ID_OCORRENCIA) AS TOTAL "
            + "FROM BAIRRO b "
            + "LEFT JOIN OCORRENCIA o ON o.ID_BAIRRO = b.ID_BAIRRO "
            + "GROUP BY b.NOME "
            + "ORDER BY TOTAL DESC, b.NOME";

    private static final String SQL_POR_CATEGORIA =
            "SELECT c.NOME, COUNT(o.ID_OCORRENCIA) AS TOTAL "
            + "FROM CATEGORIA c "
            + "LEFT JOIN OCORRENCIA o ON o.ID_CATEGORIA = c.ID_CATEGORIA "
            + "GROUP BY c.NOME "
            + "ORDER BY TOTAL DESC, c.NOME";

    private static final String SQL_POR_STATUS =
            "SELECT o.STATUS, COUNT(*) AS TOTAL "
            + "FROM OCORRENCIA o "
            + "GROUP BY o.STATUS "
            + "ORDER BY TOTAL DESC, o.STATUS";

    private static final String SQL_RANKING =
            "SELECT o.ID_OCORRENCIA, o.TITULO, b.NOME AS BAIRRO, o.STATUS, COUNT(a.ID_USUARIO) AS TOTAL_APOIOS "
            + "FROM OCORRENCIA o "
            + "JOIN BAIRRO b ON b.ID_BAIRRO = o.ID_BAIRRO "
            + "LEFT JOIN APOIO a ON a.ID_OCORRENCIA = o.ID_OCORRENCIA "
            + "GROUP BY o.ID_OCORRENCIA, o.TITULO, b.NOME, o.STATUS, o.DATA_ABERTURA "
            + "ORDER BY TOTAL_APOIOS DESC, o.DATA_ABERTURA DESC";

    @Override
    public List<LinhaRelatorio> porBairro() throws DadosException {
        return contagem(SQL_POR_BAIRRO);
    }

    @Override
    public List<LinhaRelatorio> porCategoria() throws DadosException {
        return contagem(SQL_POR_CATEGORIA);
    }

    @Override
    public List<LinhaRelatorio> porStatus() throws DadosException {
        return contagem(SQL_POR_STATUS);
    }

    @Override
    public List<LinhaRanking> rankingApoios() throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_RANKING);
             ResultSet rs = ps.executeQuery()) {
            List<LinhaRanking> linhas = new ArrayList<LinhaRanking>();
            while (rs.next()) {
                linhas.add(new LinhaRanking(rs.getInt("ID_OCORRENCIA"), rs.getString("TITULO"),
                        rs.getString("BAIRRO"), rs.getString("STATUS"), rs.getInt("TOTAL_APOIOS")));
            }
            return linhas;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    // 1ª coluna: nome ou status; 2ª: total
    private List<LinhaRelatorio> contagem(String sql) throws DadosException {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LinhaRelatorio> linhas = new ArrayList<LinhaRelatorio>();
            while (rs.next()) {
                linhas.add(new LinhaRelatorio(rs.getString(1), rs.getInt(2)));
            }
            return linhas;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
