package br.com.avisaai.api;

import br.com.avisaai.service.UsuarioService;

public class UsuarioSenhaHandler extends HandlerBase {

    private final UsuarioService service;

    public UsuarioSenhaHandler(UsuarioService service) {
        super("/usuarios/senha");
        this.service = service;
    }

    @Override
    protected Resposta put(Requisicao req) {
        int id = req.id();
        service.trocarSenha(id, req.parametroInteiro("usuario"), req.texto("senhaAtual"), req.texto("novaSenha"));
        return Resposta.mensagem("Senha trocada com sucesso.");
    }
}
