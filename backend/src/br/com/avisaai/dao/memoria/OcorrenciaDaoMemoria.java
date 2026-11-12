package br.com.avisaai.dao.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import br.com.avisaai.dao.OcorrenciaDao;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;

public class OcorrenciaDaoMemoria implements OcorrenciaDao {

    private final BancoMemoria banco;

    public OcorrenciaDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public Ocorrencia inserir(Ocorrencia ocorrencia) {
        synchronized (banco) {
            BancoMemoria.LinhaOcorrencia linha = new BancoMemoria.LinhaOcorrencia();
            linha.id = banco.proximoIdOcorrencia();
            linha.status = ocorrencia.getStatus();
            linha.dataAbertura = ocorrencia.getDataAbertura();
            linha.idUsuario = ocorrencia.getAutor().getId();
            copiarCamposEditaveis(ocorrencia, linha);
            banco.ocorrencias.put(linha.id, linha);
            ocorrencia.setId(linha.id);
            return ocorrencia;
        }
    }

    @Override
    public List<Ocorrencia> listar(Integer idBairro, StatusOcorrencia status) {
        synchronized (banco) {
            List<Ocorrencia> lista = new ArrayList<Ocorrencia>();
            for (BancoMemoria.LinhaOcorrencia linha : banco.ocorrencias.values()) {
                boolean bairroConfere = idBairro == null || linha.idBairro == idBairro;
                boolean statusConfere = status == null || linha.status == status;
                if (bairroConfere && statusConfere) {
                    lista.add(banco.montarOcorrencia(linha));
                }
            }
            Collections.sort(lista, new Comparator<Ocorrencia>() {
                @Override
                public int compare(Ocorrencia a, Ocorrencia b) {
                    int porData = b.getDataAbertura().compareTo(a.getDataAbertura());
                    return porData != 0 ? porData : b.getId().compareTo(a.getId());
                }
            });
            return lista;
        }
    }

    @Override
    public Ocorrencia buscarPorId(int id) {
        synchronized (banco) {
            BancoMemoria.LinhaOcorrencia linha = banco.ocorrencias.get(id);
            return linha == null ? null : banco.montarOcorrencia(linha);
        }
    }

    @Override
    public boolean atualizar(Ocorrencia ocorrencia) {
        synchronized (banco) {
            BancoMemoria.LinhaOcorrencia linha = banco.ocorrencias.get(ocorrencia.getId());
            if (linha == null) {
                return false;
            }
            copiarCamposEditaveis(ocorrencia, linha);
            return true;
        }
    }

    @Override
    public boolean atualizarStatus(int id, StatusOcorrencia status) {
        synchronized (banco) {
            BancoMemoria.LinhaOcorrencia linha = banco.ocorrencias.get(id);
            if (linha == null) {
                return false;
            }
            linha.status = status;
            return true;
        }
    }

    @Override
    public boolean excluir(int id) {
        synchronized (banco) {
            if (banco.ocorrencias.remove(id) == null) {
                return false;
            }
            Iterator<BancoMemoria.LinhaApoio> it = banco.apoios.iterator();
            while (it.hasNext()) {
                if (it.next().idOcorrencia == id) {
                    it.remove();
                }
            }
            return true;
        }
    }

    private void copiarCamposEditaveis(Ocorrencia o, BancoMemoria.LinhaOcorrencia linha) {
        linha.titulo = o.getTitulo();
        linha.descricao = o.getDescricao();
        linha.endereco = o.getEndereco();
        linha.idCategoria = o.getCategoria().getId();
        linha.idBairro = o.getBairro().getId();
    }
}
