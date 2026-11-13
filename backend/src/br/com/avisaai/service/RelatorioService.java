package br.com.avisaai.service;

import java.util.List;

import br.com.avisaai.dao.RelatorioDao;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.util.JsonSerializavel;

public class RelatorioService {

    static final String MSG_TIPO_INVALIDO = "Tipo de relatório inválido. Use bairro, categoria, ranking ou status.";

    private final RelatorioDao dao;

    public RelatorioService(RelatorioDao dao) {
        this.dao = dao;
    }

    public List<? extends JsonSerializavel> gerar(String tipo) {
        String normalizado = Validacao.vazio(tipo) ? "" : tipo.trim().toLowerCase();
        if ("bairro".equals(normalizado)) {
            return dao.porBairro();
        }
        if ("categoria".equals(normalizado)) {
            return dao.porCategoria();
        }
        if ("ranking".equals(normalizado)) {
            return dao.rankingApoios();
        }
        if ("status".equals(normalizado)) {
            return dao.porStatus();
        }
        throw new RegraNegocioException(MSG_TIPO_INVALIDO);
    }
}
