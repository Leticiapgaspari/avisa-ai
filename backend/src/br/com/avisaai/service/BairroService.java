package br.com.avisaai.service;

import java.util.List;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Bairro;

public class BairroService {

    static final String MSG_NAO_ENCONTRADO = "Bairro não encontrado.";
    private static final int TAMANHO_NOME = 80;

    private final BairroDao dao;

    public BairroService(BairroDao dao) {
        this.dao = dao;
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

    public Bairro criar(String nome) {
        Bairro bairro = new Bairro(null, validarNome(nome));
        return dao.inserir(bairro);
    }

    public Bairro atualizar(int id, String nome) {
        Bairro bairro = buscar(id);
        bairro.setNome(validarNome(nome));
        if (!dao.atualizar(bairro)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
        return bairro;
    }

    public void excluir(int id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }

    private String validarNome(String nome) {
        String limpo = Validacao.limpar(nome);
        if (limpo == null) {
            throw new RegraNegocioException("Informe o nome do bairro.");
        }
        Validacao.tamanhoMaximo(limpo, TAMANHO_NOME, "O nome do bairro");
        return limpo;
    }
}
