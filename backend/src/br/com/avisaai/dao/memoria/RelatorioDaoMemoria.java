package br.com.avisaai.dao.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import br.com.avisaai.dao.RelatorioDao;
import br.com.avisaai.dto.LinhaRanking;
import br.com.avisaai.dto.LinhaRelatorio;
import br.com.avisaai.model.StatusOcorrencia;

public class RelatorioDaoMemoria implements RelatorioDao {

    private final BancoMemoria banco;

    public RelatorioDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public List<LinhaRelatorio> porBairro() {
        synchronized (banco) {
            List<LinhaRelatorio> linhas = new ArrayList<LinhaRelatorio>();
            for (Map.Entry<Integer, String> b : banco.bairros.entrySet()) {
                int total = 0;
                for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                    if (o.idBairro == b.getKey()) {
                        total++;
                    }
                }
                linhas.add(new LinhaRelatorio(b.getValue(), total));
            }
            return ordenarPorTotal(linhas);
        }
    }

    @Override
    public List<LinhaRelatorio> porCategoria() {
        synchronized (banco) {
            List<LinhaRelatorio> linhas = new ArrayList<LinhaRelatorio>();
            for (Map.Entry<Integer, String> c : banco.categorias.entrySet()) {
                int total = 0;
                for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                    if (o.idCategoria == c.getKey()) {
                        total++;
                    }
                }
                linhas.add(new LinhaRelatorio(c.getValue(), total));
            }
            return ordenarPorTotal(linhas);
        }
    }

    @Override
    public List<LinhaRanking> rankingApoios() {
        synchronized (banco) {
            List<LinhaRanking> linhas = new ArrayList<LinhaRanking>();
            for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                linhas.add(new LinhaRanking(o.id, o.titulo, banco.bairros.get(o.idBairro),
                        o.status.name(), banco.contarApoios(o.id)));
            }
            Collections.sort(linhas, new Comparator<LinhaRanking>() {
                @Override
                public int compare(LinhaRanking a, LinhaRanking b) {
                    int porTotal = Integer.compare(b.getTotalApoios(), a.getTotalApoios());
                    return porTotal != 0 ? porTotal : a.getId().compareTo(b.getId());
                }
            });
            return linhas;
        }
    }

    @Override
    public List<LinhaRelatorio> porStatus() {
        synchronized (banco) {
            List<LinhaRelatorio> linhas = new ArrayList<LinhaRelatorio>();
            for (StatusOcorrencia s : StatusOcorrencia.values()) {
                int total = 0;
                for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                    if (o.status == s) {
                        total++;
                    }
                }
                linhas.add(new LinhaRelatorio(s.name(), total));
            }
            return linhas;
        }
    }

    private List<LinhaRelatorio> ordenarPorTotal(List<LinhaRelatorio> linhas) {
        Collections.sort(linhas, new Comparator<LinhaRelatorio>() {
            @Override
            public int compare(LinhaRelatorio a, LinhaRelatorio b) {
                int porTotal = Integer.compare(b.getTotal(), a.getTotal());
                return porTotal != 0 ? porTotal : a.getNome().compareToIgnoreCase(b.getNome());
            }
        });
        return linhas;
    }
}
