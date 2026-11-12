package br.com.avisaai.api;

import br.com.avisaai.service.ApoioService;

public class ApoioHandler extends HandlerBase {

    private final ApoioService service;

    public ApoioHandler(ApoioService service) {
        super("/apoios");
        this.service = service;
    }

    @Override
    protected Resposta post(Requisicao req) {
        service.apoiar(req.inteiro("idUsuario"), req.inteiro("idOcorrencia"));
        Resposta ok = Resposta.mensagem("Apoio registrado com sucesso.");
        return new Resposta(201, ok.getJson());
    }

    @Override
    protected Resposta delete(Requisicao req) {
        service.remover(req.parametroInteiro("usuario"), req.parametroInteiro("ocorrencia"));
        return Resposta.mensagem("Apoio removido com sucesso.");
    }
}
