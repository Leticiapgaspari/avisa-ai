package br.com.avisaai.api;

import br.com.avisaai.service.RelatorioService;
import br.com.avisaai.util.Json;

public class RelatorioHandler extends HandlerBase {

    private final RelatorioService service;

    public RelatorioHandler(RelatorioService service) {
        super("/relatorios");
        this.service = service;
    }

    @Override
    protected Resposta get(Requisicao req) {
        return Resposta.ok(Json.lista(service.gerar(req.parametro("tipo"))));
    }
}
