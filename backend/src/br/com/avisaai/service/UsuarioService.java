package br.com.avisaai.service;

import java.util.List;
import java.util.regex.Pattern;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.dto.UsuarioDados;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.exception.NaoAutenticadoException;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Usuario;

public class UsuarioService {

    static final String MSG_NAO_ENCONTRADO = "Usuário não encontrado.";
    static final String MSG_NOME_EMAIL = "Informe nome e um e-mail válido.";
    static final String MSG_EMAIL_REPETIDO = "Esse e-mail já está em outra conta.";
    static final String MSG_EMAIL_JA_CADASTRADO = "Já existe uma conta com esse e-mail. Tente entrar.";
    static final String MSG_TIPO_INVALIDO = "Tipo de usuário inválido.";
    static final String MSG_BAIRRO_OBRIGATORIO = "Informe o bairro.";

    static final String MSG_LOGIN_INVALIDO = "E-mail ou senha incorretos.";
    static final String MSG_SENHA_CURTA = "A senha precisa ter pelo menos 6 caracteres.";
    static final String MSG_SENHA_ATUAL = "A senha atual não confere.";
    static final String MSG_NOVA_SENHA_CURTA = "A nova senha precisa ter pelo menos 6 caracteres.";
    static final String MSG_NOVA_SENHA_IGUAL = "A nova senha precisa ser diferente da atual.";

    static final String MSG_SO_PROPRIOS_DADOS = "Você só pode alterar os seus próprios dados.";
    static final String MSG_SO_VER_PROPRIOS = "Você só pode ver os seus próprios dados.";
    static final String MSG_LISTAR_SO_AGENTE = "Apenas agentes podem ver a lista de usuários.";
    static final String MSG_CRIAR_SO_AGENTE = "Apenas agentes podem cadastrar outros usuários.";
    static final String MSG_EXCLUIR_SO_AGENTE = "Apenas agentes podem excluir usuários.";
    static final String MSG_EXCLUIR_PROPRIA = "Você não pode excluir a sua própria conta.";

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TAMANHO_NOME = 100;
    private static final int TAMANHO_EMAIL = 120;

    private final UsuarioDao dao;
    private final BairroDao bairroDao;

    public UsuarioService(UsuarioDao dao, BairroDao bairroDao) {
        this.dao = dao;
        this.bairroDao = bairroDao;
    }

    // mesma mensagem nos dois casos pra não revelar quais e-mails existem
    public Usuario entrar(String email, String senha) {
        String emailLimpo = Validacao.limpar(email);
        if (emailLimpo == null || senha == null) {
            throw new NaoAutenticadoException(MSG_LOGIN_INVALIDO);
        }
        Usuario usuario = dao.buscarPorEmail(emailLimpo.toLowerCase());
        if (usuario == null || !Senha.confere(senha, dao.buscarSenhaHash(usuario.getId()))) {
            throw new NaoAutenticadoException(MSG_LOGIN_INVALIDO);
        }
        return usuario;
    }

    public void trocarSenha(int id, Integer idUsuario, String senhaAtual, String novaSenha) {
        buscarExistente(id);
        Usuario quem = Permissao.quemAge(dao, idUsuario);
        if (!quem.getId().equals(id)) {
            throw new RegraNegocioException(MSG_SO_PROPRIOS_DADOS);
        }
        if (!Senha.confere(senhaAtual, dao.buscarSenhaHash(id))) {
            throw new RegraNegocioException(MSG_SENHA_ATUAL);
        }
        if (Senha.curta(novaSenha)) {
            throw new RegraNegocioException(MSG_NOVA_SENHA_CURTA);
        }
        if (novaSenha.equals(senhaAtual)) {
            throw new RegraNegocioException(MSG_NOVA_SENHA_IGUAL);
        }
        if (!dao.atualizarSenhaHash(id, Senha.gerarHash(novaSenha))) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }

    public List<Usuario> listar(Integer idUsuario) {
        Permissao.exigirGestor(dao, idUsuario, MSG_LISTAR_SO_AGENTE);
        return dao.listar();
    }

    public Usuario buscar(int id, Integer idUsuario) {
        Usuario quem = Permissao.quemAge(dao, idUsuario);
        if (!quem.podeGerenciarCadastros() && !quem.getId().equals(id)) {
            throw new RegraNegocioException(MSG_SO_VER_PROPRIOS);
        }
        return buscarExistente(id);
    }

    // sem ?usuario é o cadastro da tela de login: sempre morador
    public Usuario criar(UsuarioDados dados, Integer idUsuario) {
        boolean cadastroPublico = idUsuario == null;
        String tipo;
        if (cadastroPublico) {
            tipo = Usuario.TIPO_CIDADAO;
        } else {
            Permissao.exigirGestor(dao, idUsuario, MSG_CRIAR_SO_AGENTE);
            tipo = dados.getTipo();
        }
        if (Senha.curta(dados.getSenha())) {
            throw new RegraNegocioException(MSG_SENHA_CURTA);
        }
        Usuario usuario = montarValidado(dados, tipo);

        String msgRepetido = cadastroPublico ? MSG_EMAIL_JA_CADASTRADO : MSG_EMAIL_REPETIDO;
        verificarEmailLivre(usuario.getEmail(), null, msgRepetido);
        try {
            return dao.inserir(usuario, Senha.gerarHash(dados.getSenha()));
        } catch (DadosException e) {
            throw traduzirDuplicado(e, msgRepetido);
        }
    }

    // ninguém muda o próprio tipo
    public Usuario atualizar(int id, UsuarioDados dados, Integer idUsuario) {
        Usuario atual = buscarExistente(id);
        Usuario quem = Permissao.quemAge(dao, idUsuario);
        boolean propriaConta = quem.getId().equals(id);
        if (!quem.podeGerenciarCadastros() && !propriaConta) {
            throw new RegraNegocioException(MSG_SO_PROPRIOS_DADOS);
        }
        String tipo = propriaConta ? atual.getTipo() : dados.getTipo();

        Usuario usuario = montarValidado(dados, tipo);
        usuario.setId(id);
        verificarEmailLivre(usuario.getEmail(), id, MSG_EMAIL_REPETIDO);
        try {
            if (!dao.atualizar(usuario)) {
                throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
            }
        } catch (DadosException e) {
            throw traduzirDuplicado(e, MSG_EMAIL_REPETIDO);
        }
        return usuario;
    }

    public void excluir(int id, Integer idUsuario) {
        Usuario quem = Permissao.exigirGestor(dao, idUsuario, MSG_EXCLUIR_SO_AGENTE);
        if (quem.getId().equals(id)) {
            throw new RegraNegocioException(MSG_EXCLUIR_PROPRIA);
        }
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }

    private Usuario buscarExistente(int id) {
        Usuario usuario = dao.buscarPorId(id);
        if (usuario == null) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return usuario;
    }

    private Usuario montarValidado(UsuarioDados dados, String tipo) {
        String nome = Validacao.limpar(dados.getNome());
        String email = Validacao.limpar(dados.getEmail());
        if (nome == null || email == null || !FORMATO_EMAIL.matcher(email).matches()) {
            throw new RegraNegocioException(MSG_NOME_EMAIL);
        }
        Validacao.tamanhoMaximo(nome, TAMANHO_NOME, "O nome");
        Validacao.tamanhoMaximo(email, TAMANHO_EMAIL, "O e-mail");

        Usuario usuario = Usuario.criar(tipo);
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

    private void verificarEmailLivre(String email, Integer idDoProprio, String mensagem) {
        Usuario existente = dao.buscarPorEmail(email);
        if (existente != null && !existente.getId().equals(idDoProprio)) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private RuntimeException traduzirDuplicado(DadosException e, String mensagem) {
        if (e.getTipo() == DadosException.Tipo.DUPLICADO) {
            return new RegraNegocioException(mensagem);
        }
        return e;
    }
}
