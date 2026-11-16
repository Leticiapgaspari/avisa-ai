package br.com.avisaai.api;

import br.com.avisaai.service.ApoioService;
import br.com.avisaai.util.Json;

public class ApoioHandler extends HandlerBase {

    private final ApoioService service;

    public ApoioHandler(ApoioService service) {
        super("/apoios");
        this.service = service;
    }

    @Override
    protected Resposta get(Requisicao req) {
        return Resposta.ok(Json.numeros(service.listarOcorrenciasApoiadas(req.parametroInteiro("usuario"))));
    }

    @Override
    protected Resposta post(Requisicao req) {
        service.apoiar(req.inteiro("idUsuario"), req.inteiro("idOcorrencia"));
        return Resposta.criado(Json.campo("mensagem", "Apoio registrado com sucesso."));
    }

    @Override
    protected Resposta delete(Requisicao req) {
        service.remover(req.parametroInteiro("usuario"), req.parametroInteiro("ocorrencia"));
        return Resposta.mensagem("Apoio removido com sucesso.");
    }
}
