package br.com.avisaai.api;

import br.com.avisaai.dto.OcorrenciaDados;
import br.com.avisaai.service.OcorrenciaService;
import br.com.avisaai.util.Json;

public class OcorrenciaHandler extends HandlerBase {

    private final OcorrenciaService service;

    public OcorrenciaHandler(OcorrenciaService service) {
        super("/ocorrencias");
        this.service = service;
    }

    @Override
    protected Resposta get(Requisicao req) {
        Integer id = req.parametroInteiro("id");
        if (id != null) {
            return Resposta.ok(service.buscar(id).toJson());
        }
        return Resposta.ok(Json.lista(service.listar(req.parametroInteiro("bairro"), req.parametro("status"))));
    }

    @Override
    protected Resposta post(Requisicao req) {
        OcorrenciaDados dados = lerDados(req);
        return Resposta.criado(service.criar(dados, req.inteiro("idUsuario")).toJson());
    }

    @Override
    protected Resposta put(Requisicao req) {
        int id = req.id();
        Integer usuario = req.parametroInteiro("usuario");
        return Resposta.ok(service.atualizar(id, usuario, lerDados(req)).toJson());
    }

    @Override
    protected Resposta delete(Requisicao req) {
        service.excluir(req.id(), req.parametroInteiro("usuario"));
        return Resposta.mensagem("Ocorrência excluída com sucesso.");
    }

    private OcorrenciaDados lerDados(Requisicao req) {
        OcorrenciaDados dados = new OcorrenciaDados();
        dados.setTitulo(req.texto("titulo"));
        dados.setDescricao(req.texto("descricao"));
        dados.setEndereco(req.texto("endereco"));
        dados.setIdCategoria(req.inteiro("idCategoria"));
        dados.setIdBairro(req.inteiro("idBairro"));
        return dados;
    }
}
