package br.com.avisaai.dao.memoria;

import java.util.Iterator;

import br.com.avisaai.dao.ApoioDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Apoio;

public class ApoioDaoMemoria implements ApoioDao {

    private final BancoMemoria banco;

    public ApoioDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public void inserir(Apoio apoio) {
        synchronized (banco) {
            int idUsuario = apoio.getUsuario().getId();
            int idOcorrencia = apoio.getOcorrencia().getId();
            if (existe(idUsuario, idOcorrencia)) {
                throw DadosException.duplicado(null);
            }
            BancoMemoria.LinhaApoio linha = new BancoMemoria.LinhaApoio();
            linha.idUsuario = idUsuario;
            linha.idOcorrencia = idOcorrencia;
            linha.dataApoio = apoio.getDataApoio();
            banco.apoios.add(linha);
        }
    }

    @Override
    public boolean excluir(int idUsuario, int idOcorrencia) {
        synchronized (banco) {
            Iterator<BancoMemoria.LinhaApoio> it = banco.apoios.iterator();
            while (it.hasNext()) {
                BancoMemoria.LinhaApoio a = it.next();
                if (a.idUsuario == idUsuario && a.idOcorrencia == idOcorrencia) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean existe(int idUsuario, int idOcorrencia) {
        synchronized (banco) {
            for (BancoMemoria.LinhaApoio a : banco.apoios) {
                if (a.idUsuario == idUsuario && a.idOcorrencia == idOcorrencia) {
                    return true;
                }
            }
            return false;
        }
    }
}
