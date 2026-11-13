package br.com.avisaai.dao.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;

public class BairroDaoMemoria implements BairroDao {

    private final BancoMemoria banco;

    public BairroDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public Bairro inserir(Bairro bairro) {
        synchronized (banco) {
            BancoMemoria.verificarUnico(banco.bairros, bairro.getNome(), null);
            int id = banco.proximoIdBairro();
            banco.bairros.put(id, bairro.getNome());
            bairro.setId(id);
            return bairro;
        }
    }

    @Override
    public List<Bairro> listar() {
        synchronized (banco) {
            List<Bairro> lista = new ArrayList<Bairro>();
            for (Integer id : banco.bairros.keySet()) {
                lista.add(banco.montarBairro(id));
            }
            Collections.sort(lista, new Comparator<Bairro>() {
                @Override
                public int compare(Bairro a, Bairro b) {
                    return a.getNome().compareToIgnoreCase(b.getNome());
                }
            });
            return lista;
        }
    }

    @Override
    public Bairro buscarPorId(int id) {
        synchronized (banco) {
            return banco.montarBairro(id);
        }
    }

    @Override
    public boolean atualizar(Bairro bairro) {
        synchronized (banco) {
            if (!banco.bairros.containsKey(bairro.getId())) {
                return false;
            }
            BancoMemoria.verificarUnico(banco.bairros, bairro.getNome(), bairro.getId());
            banco.bairros.put(bairro.getId(), bairro.getNome());
            return true;
        }
    }

    @Override
    public boolean excluir(int id) {
        synchronized (banco) {
            if (!banco.bairros.containsKey(id)) {
                return false;
            }
            for (BancoMemoria.LinhaUsuario u : banco.usuarios.values()) {
                if (u.idBairro == id) {
                    throw DadosException.emUso(null);
                }
            }
            for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                if (o.idBairro == id) {
                    throw DadosException.emUso(null);
                }
            }
            banco.bairros.remove(id);
            return true;
        }
    }
}
