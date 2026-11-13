package br.com.avisaai.service;

import java.util.List;
import java.util.regex.Pattern;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.dto.UsuarioDados;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Usuario;

public class UsuarioService {

    static final String MSG_NAO_ENCONTRADO = "Usuário não encontrado.";
    static final String MSG_NOME_EMAIL = "Informe nome e um e-mail válido.";
    static final String MSG_EMAIL_REPETIDO = "Já existe um usuário com este e-mail.";
    static final String MSG_TIPO_INVALIDO = "Tipo de usuário inválido.";
    static final String MSG_BAIRRO_OBRIGATORIO = "Informe o bairro.";

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TAMANHO_NOME = 100;
    private static final int TAMANHO_EMAIL = 120;

    private final UsuarioDao dao;
    private final BairroDao bairroDao;

    public UsuarioService(UsuarioDao dao, BairroDao bairroDao) {
        this.dao = dao;
        this.bairroDao = bairroDao;
    }

    public List<Usuario> listar() {
        return dao.listar();
    }

    public Usuario buscar(int id) {
        Usuario usuario = dao.buscarPorId(id);
        if (usuario == null) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return usuario;
    }

    public Usuario criar(UsuarioDados dados) {
        Usuario usuario = montarValidado(dados);
        verificarEmailLivre(usuario.getEmail(), null);
        try {
            return dao.inserir(usuario);
        } catch (DadosException e) {
            throw traduzirDuplicado(e);
        }
    }

    public Usuario atualizar(int id, UsuarioDados dados) {
        buscar(id);
        Usuario usuario = montarValidado(dados);
        usuario.setId(id);
        verificarEmailLivre(usuario.getEmail(), id);
        try {
            if (!dao.atualizar(usuario)) {
                throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
            }
        } catch (DadosException e) {
            throw traduzirDuplicado(e);
        }
        return usuario;
    }

    public void excluir(int id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }

    private Usuario montarValidado(UsuarioDados dados) {
        String nome = Validacao.limpar(dados.getNome());
        String email = Validacao.limpar(dados.getEmail());
        if (nome == null || email == null || !FORMATO_EMAIL.matcher(email).matches()) {
            throw new RegraNegocioException(MSG_NOME_EMAIL);
        }
        Validacao.tamanhoMaximo(nome, TAMANHO_NOME, "O nome");
        Validacao.tamanhoMaximo(email, TAMANHO_EMAIL, "O e-mail");

        Usuario usuario = Usuario.criar(dados.getTipo());
        if (usuario == null) {
            throw new RegraNegocioException(MSG_TIPO_INVALIDO);
        }

        if (dados.getIdBairro() == null) {
            throw new RegraNegocioException(MSG_BAIRRO_OBRIGATORIO);
        }
        Bairro bairro = bairroDao.buscarPorId(dados.getIdBairro());
        if (bairro == null) {
            throw new NaoEncontradoException(BairroService.MSG_NAO_ENCONTRADO);
        }

        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase());
        usuario.setBairro(bairro);
        return usuario;
    }

    private void verificarEmailLivre(String email, Integer idDoProprio) {
        Usuario existente = dao.buscarPorEmail(email);
        if (existente != null && !existente.getId().equals(idDoProprio)) {
            throw new RegraNegocioException(MSG_EMAIL_REPETIDO);
        }
    }

    private RuntimeException traduzirDuplicado(DadosException e) {
        if (e.getTipo() == DadosException.Tipo.DUPLICADO) {
            return new RegraNegocioException(MSG_EMAIL_REPETIDO);
        }
        return e;
    }
}
