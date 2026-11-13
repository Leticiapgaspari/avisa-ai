package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.dto.LinhaRanking;
import br.com.avisaai.dto.LinhaRelatorio;
import br.com.avisaai.exception.DadosException;

public interface RelatorioDao {

    List<LinhaRelatorio> porBairro() throws DadosException;

    List<LinhaRelatorio> porCategoria() throws DadosException;

    List<LinhaRanking> rankingApoios() throws DadosException;

    List<LinhaRelatorio> porStatus() throws DadosException;
}
