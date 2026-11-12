package br.com.avisaai.api;

import br.com.avisaai.dto.UsuarioDados;
import br.com.avisaai.service.UsuarioService;
import br.com.avisaai.util.Json;

public class UsuarioHandler extends HandlerBase {

    private final UsuarioService service;

    public UsuarioHandler(UsuarioService service) {
        super("/usuarios");
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
        return Resposta.criado(service.criar(lerDados(req)).toJson());
    }

    @Override
    protected Resposta put(Requisicao req) {
        int id = req.id();
        return Resposta.ok(service.atualizar(id, lerDados(req)).toJson());
    }

    @Override
    protected Resposta delete(Requisicao req) {
        service.excluir(req.id());
        return Resposta.mensagem("Usuário excluído com sucesso.");
    }

    private UsuarioDados lerDados(Requisicao req) {
        UsuarioDados dados = new UsuarioDados();
        dados.setNome(req.texto("nome"));
        dados.setEmail(req.texto("email"));
        dados.setTipo(req.texto("tipo"));
        dados.setIdBairro(req.inteiro("idBairro"));
        return dados;
    }
}
