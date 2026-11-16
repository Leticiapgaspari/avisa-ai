package br.com.avisaai.service;

import java.util.List;

import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Categoria;

public class CategoriaService {

    static final String MSG_NAO_ENCONTRADA = "Categoria não encontrada.";
    static final String MSG_SO_AGENTE = "Apenas agentes podem cadastrar, alterar ou excluir categorias.";
    static final String MSG_NOME_CURTO = "Informe um nome com pelo menos 3 caracteres.";
    private static final int TAMANHO_MINIMO = 3;
    private static final int TAMANHO_NOME = 80;

    private final CategoriaDao dao;
    private final UsuarioDao usuarioDao;

    public CategoriaService(CategoriaDao dao, UsuarioDao usuarioDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
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

    public Categoria criar(String nome, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        Categoria categoria = new Categoria(null, validarNome(nome));
        return dao.inserir(categoria);
    }

    public Categoria atualizar(int id, String nome, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        Categoria categoria = buscar(id);
        categoria.setNome(validarNome(nome));
        if (!dao.atualizar(categoria)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return categoria;
    }

    public void excluir(int id, Integer idUsuario) {
        Permissao.exigirGestor(usuarioDao, idUsuario, MSG_SO_AGENTE);
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
    }

    private String validarNome(String nome) {
        String limpo = Validacao.limpar(nome);
        if (limpo == null || limpo.length() < TAMANHO_MINIMO) {
            throw new RegraNegocioException(MSG_NOME_CURTO);
        }
        Validacao.tamanhoMaximo(limpo, TAMANHO_NOME, "O nome da categoria");
        return limpo;
    }
}
