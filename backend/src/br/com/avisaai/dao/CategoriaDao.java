package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Categoria;

public interface CategoriaDao {

    Categoria inserir(Categoria categoria) throws DadosException;

    List<Categoria> listar() throws DadosException;

    Categoria buscarPorId(int id) throws DadosException;

    boolean atualizar(Categoria categoria) throws DadosException;

    boolean excluir(int id) throws DadosException;
}
