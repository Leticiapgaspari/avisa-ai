package br.com.avisaai.service;

import java.util.List;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Bairro;

public class BairroService {

    static final String MSG_NAO_ENCONTRADO = "Bairro não encontrado.";
    static final String MSG_SO_AGENTE = "Apenas agentes podem cadastrar, alterar ou excluir bairros.";
    static final String MSG_NOME_CURTO = "Informe um nome com pelo menos 3 caracteres.";
    static final String MSG_EM_USO = "Não é possível excluir: há ocorrências ou moradores neste bairro.";
    private static final int TAMANHO_MINIMO = 3;
    private static final int TAMANHO_NOME = 80;

    private final BairroDao dao;
    private final UsuarioDao usuarioDao;

    public BairroService(BairroDao dao, UsuarioDao usuarioDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
    }

    public List<Bairro> listar() {
        return dao.listar();
    }

    public Bairro buscar(int id) {
        Bairro bairro = dao.buscarPorId(id);
        if (bairro == null) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return bairro;
    }

    public Bairro criar(String nome, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        Bairro bairro = new Bairro(null, validarNome(nome));
        return dao.inserir(bairro);
    }

    public Bairro atualizar(int id, String nome, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        Bairro bairro = buscar(id);
        bairro.setNome(validarNome(nome));
        if (!dao.atualizar(bairro)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return bairro;
    }

    public void excluir(int id, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        boolean excluiu;
        try {
            excluiu = dao.excluir(id);
        } catch (DadosException e) {
            if (e.getTipo() == DadosException.Tipo.EM_USO) {
                throw new RegraNegocioException(MSG_EM_USO);
            }
            throw e;
        }
        if (!excluiu) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }

    private String validarNome(String nome) {
        String limpo = Validacao.limpar(nome);
        if (limpo == null || limpo.length() < TAMANHO_MINIMO) {
            throw new RegraNegocioException(MSG_NOME_CURTO);
        }
        Validacao.tamanhoMaximo(limpo, TAMANHO_NOME, "O nome do bairro");
        return limpo;
    }
}
