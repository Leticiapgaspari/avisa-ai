package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;

public interface OcorrenciaDao {

    Ocorrencia inserir(Ocorrencia ocorrencia) throws DadosException;

    List<Ocorrencia> listar(Integer idBairro, StatusOcorrencia status) throws DadosException;

    Ocorrencia buscarPorId(int id) throws DadosException;

    boolean atualizar(Ocorrencia ocorrencia) throws DadosException;

    boolean atualizarStatus(int id, StatusOcorrencia status) throws DadosException;

    boolean excluir(int id) throws DadosException;
}
