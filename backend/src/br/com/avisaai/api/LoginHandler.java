package br.com.avisaai.api;

import br.com.avisaai.service.UsuarioService;

public class LoginHandler extends HandlerBase {

    private final UsuarioService service;

    public LoginHandler(UsuarioService service) {
        super("/login");
        this.service = service;
    }

    @Override
    protected Resposta post(Requisicao req) {
        return Resposta.ok(service.entrar(req.texto("email"), req.texto("senha")).toJson());
    }
}
