package br.com.avisaai.api;

import br.com.avisaai.service.OcorrenciaService;

public class OcorrenciaStatusHandler extends HandlerBase {

    private final OcorrenciaService service;

    public OcorrenciaStatusHandler(OcorrenciaService service) {
        super("/ocorrencias/status");
        this.service = service;
    }

    @Override
    protected Resposta put(Requisicao req) {
        int id = req.id();
        Integer usuario = req.parametroInteiro("usuario");
        return Resposta.ok(service.atualizarStatus(id, usuario, req.texto("status")).toJson());
    }
}
