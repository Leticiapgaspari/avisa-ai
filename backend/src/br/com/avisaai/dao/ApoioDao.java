package br.com.avisaai.dao;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Apoio;

public interface ApoioDao {

    void inserir(Apoio apoio) throws DadosException;

    boolean excluir(int idUsuario, int idOcorrencia) throws DadosException;

    boolean existe(int idUsuario, int idOcorrencia) throws DadosException;
}
