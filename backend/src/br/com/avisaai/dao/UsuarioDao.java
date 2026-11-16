package br.com.avisaai.dao;

import java.util.List;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Usuario;

public interface UsuarioDao {

    // recebe o hash, nunca a senha
    Usuario inserir(Usuario usuario, String senhaHash) throws DadosException;

    List<Usuario> listar() throws DadosException;

    Usuario buscarPorId(int id) throws DadosException;

    Usuario buscarPorEmail(String email) throws DadosException;

    boolean atualizar(Usuario usuario) throws DadosException;

    boolean excluir(int id) throws DadosException;

    // o hash fica fora do Usuario pra não ir parar no JSON
    String buscarSenhaHash(int id) throws DadosException;

    boolean atualizarSenhaHash(int id, String senhaHash) throws DadosException;
}
