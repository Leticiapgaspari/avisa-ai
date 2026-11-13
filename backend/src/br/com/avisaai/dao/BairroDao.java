package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;

public interface BairroDao {

    Bairro inserir(Bairro bairro) throws DadosException;

    List<Bairro> listar() throws DadosException;

    Bairro buscarPorId(int id) throws DadosException;

    boolean atualizar(Bairro bairro) throws DadosException;

    boolean excluir(int id) throws DadosException;
}
