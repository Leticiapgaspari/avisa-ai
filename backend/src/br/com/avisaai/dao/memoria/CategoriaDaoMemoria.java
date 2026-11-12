package br.com.avisaai.dao.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Categoria;

public class CategoriaDaoMemoria implements CategoriaDao {

    private final BancoMemoria banco;

    public CategoriaDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public Categoria inserir(Categoria categoria) {
        synchronized (banco) {
            BancoMemoria.verificarUnico(banco.categorias, categoria.getNome(), null);
            int id = banco.proximoIdCategoria();
            banco.categorias.put(id, categoria.getNome());
            categoria.setId(id);
            return categoria;
        }
    }

    @Override
    public List<Categoria> listar() {
        synchronized (banco) {
            List<Categoria> lista = new ArrayList<Categoria>();
            for (Integer id : banco.categorias.keySet()) {
                lista.add(banco.montarCategoria(id));
            }
            Collections.sort(lista, new Comparator<Categoria>() {
                @Override
                public int compare(Categoria a, Categoria b) {
                    return a.getNome().compareToIgnoreCase(b.getNome());
                }
            });
            return lista;
        }
    }

    @Override
    public Categoria buscarPorId(int id) {
        synchronized (banco) {
            return banco.montarCategoria(id);
        }
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        synchronized (banco) {
            if (!banco.categorias.containsKey(categoria.getId())) {
                return false;
            }
            BancoMemoria.verificarUnico(banco.categorias, categoria.getNome(), categoria.getId());
            banco.categorias.put(categoria.getId(), categoria.getNome());
            return true;
        }
    }

    @Override
    public boolean excluir(int id) {
        synchronized (banco) {
            if (!banco.categorias.containsKey(id)) {
                return false;
            }
            for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                if (o.idCategoria == id) {
                    throw DadosException.emUso(null);
                }
            }
            banco.categorias.remove(id);
            return true;
        }
    }
}
