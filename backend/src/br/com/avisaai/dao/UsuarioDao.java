package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Usuario;

public interface UsuarioDao {

    Usuario inserir(Usuario usuario) throws DadosException;

    List<Usuario> listar() throws DadosException;

    Usuario buscarPorId(int id) throws DadosException;

    Usuario buscarPorEmail(String email) throws DadosException;

    boolean atualizar(Usuario usuario) throws DadosException;

    boolean excluir(int id) throws DadosException;
}
