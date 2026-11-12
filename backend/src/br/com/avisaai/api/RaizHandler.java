package br.com.avisaai.api;

public class RaizHandler extends HandlerBase {

    public RaizHandler() {
        super("/");
    }

    @Override
    protected boolean rotaConfere(String caminho) {
        return "/".equals(caminho);
    }

    @Override
    protected Resposta get(Requisicao req) {
        return Resposta.mensagem("API Avisa Aí no ar.");
    }
}
