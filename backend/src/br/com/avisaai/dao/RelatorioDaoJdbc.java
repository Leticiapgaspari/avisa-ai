package br.com.avisaai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.avisaai.dto.LinhaRanking;
import br.com.avisaai.dto.LinhaRelatorio;

public class RelatorioDaoJdbc implements RelatorioDao {

    private static final String POR_BAIRRO =
            "SELECT b.NOME, COUNT(o.ID_OCORRENCIA) AS TOTAL "
            + "FROM BAIRRO b LEFT JOIN OCORRENCIA o ON o.ID_BAIRRO = b.ID_BAIRRO "
            + "GROUP BY b.NOME ORDER BY TOTAL DESC, b.NOME";

    private static final String POR_CATEGORIA =
            "SELECT c.NOME, COUNT(o.ID_OCORRENCIA) AS TOTAL "
            + "FROM CATEGORIA c LEFT JOIN OCORRENCIA o ON o.ID_CATEGORIA = c.ID_CATEGORIA "
            + "GROUP BY c.NOME ORDER BY TOTAL DESC, c.NOME";

    private static final String POR_STATUS =
            "SELECT STATUS AS NOME, COUNT(*) AS TOTAL FROM OCORRENCIA "
            + "GROUP BY STATUS ORDER BY TOTAL DESC, STATUS";

    private static final String RANKING =
            "SELECT o.ID_OCORRENCIA, o.TITULO, b.NOME AS NOME_BAIRRO, o.STATUS, COUNT(a.ID_USUARIO) AS TOTAL_APOIOS "
            + "FROM OCORRENCIA o "
            + "JOIN BAIRRO b ON b.ID_BAIRRO = o.ID_BAIRRO "
            + "LEFT JOIN APOIO a ON a.ID_OCORRENCIA = o.ID_OCORRENCIA "
            + "GROUP BY o.ID_OCORRENCIA, o.TITULO, b.NOME, o.STATUS "
            + "ORDER BY TOTAL_APOIOS DESC, o.ID_OCORRENCIA";

    @Override
    public List<LinhaRelatorio> porBairro() {
        return contagem(POR_BAIRRO);
    }

    @Override
    public List<LinhaRelatorio> porCategoria() {
        return contagem(POR_CATEGORIA);
    }

    @Override
    public List<LinhaRelatorio> porStatus() {
        return contagem(POR_STATUS);
    }

    @Override
    public List<LinhaRanking> rankingApoios() {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(RANKING);
             ResultSet rs = ps.executeQuery()) {
            List<LinhaRanking> linhas = new ArrayList<LinhaRanking>();
            while (rs.next()) {
                linhas.add(new LinhaRanking(rs.getInt("ID_OCORRENCIA"), rs.getString("TITULO"),
                        rs.getString("NOME_BAIRRO"), rs.getString("STATUS"), rs.getInt("TOTAL_APOIOS")));
            }
            return linhas;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }

    private List<LinhaRelatorio> contagem(String sql) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LinhaRelatorio> linhas = new ArrayList<LinhaRelatorio>();
            while (rs.next()) {
                linhas.add(new LinhaRelatorio(rs.getString("NOME"), rs.getInt("TOTAL")));
            }
            return linhas;
        } catch (SQLException e) {
            throw ErroBanco.traduzir(e);
        }
    }
}
