package br.com.avisaai.api;

import br.com.avisaai.service.BairroService;
import br.com.avisaai.util.Json;

public class BairroHandler extends HandlerBase {

    private final BairroService service;

    public BairroHandler(BairroService service) {
        super("/bairros");
        this.service = service;
    }

    @Override
    protected Resposta get(Requisicao req) {
        Integer id = req.parametroInteiro("id");
        if (id != null) {
            return Resposta.ok(service.buscar(id).toJson());
        }
        return Resposta.ok(Json.lista(service.listar()));
    }

    @Override
    protected Resposta post(Requisicao req) {
        return Resposta.criado(service.criar(req.texto("nome")).toJson());
    }

    @Override
    protected Resposta put(Requisicao req) {
        int id = req.id();
        return Resposta.ok(service.atualizar(id, req.texto("nome")).toJson());
    }

    @Override
    protected Resposta delete(Requisicao req) {
        service.excluir(req.id());
        return Resposta.mensagem("Bairro excluído com sucesso.");
    }
}
