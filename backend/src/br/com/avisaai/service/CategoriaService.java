package br.com.avisaai.service;

import java.util.List;

import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Categoria;

public class CategoriaService {

    static final String MSG_NAO_ENCONTRADA = "Categoria não encontrada.";
    private static final int TAMANHO_NOME = 80;

    private final CategoriaDao dao;

    public CategoriaService(CategoriaDao dao) {
        this.dao = dao;
    }

    public List<Categoria> listar() {
        return dao.listar();
    }

    public Categoria buscar(int id) {
        Categoria categoria = dao.buscarPorId(id);
        if (categoria == null) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return categoria;
    }

    public Categoria criar(String nome) {
        Categoria categoria = new Categoria(null, validarNome(nome));
        return dao.inserir(categoria);
    }

    public Categoria atualizar(int id, String nome) {
        Categoria categoria = buscar(id);
        categoria.setNome(validarNome(nome));
        if (!dao.atualizar(categoria)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return categoria;
    }

    public void excluir(int id) {
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
    }

    private String validarNome(String nome) {
        String limpo = Validacao.limpar(nome);
        if (limpo == null) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        Validacao.tamanhoMaximo(limpo, TAMANHO_NOME, "O nome da categoria");
        return limpo;
    }
}
