package br.com.avisaai.api;

import br.com.avisaai.util.Json;

public class Resposta {

    private final int status;
    private final String json;

    public Resposta(int status, String json) {
        this.status = status;
        this.json = json;
    }

    public static Resposta ok(String json) {
        return new Resposta(200, json);
    }

    public static Resposta criado(String json) {
        return new Resposta(201, json);
    }

    public static Resposta mensagem(String texto) {
        return new Resposta(200, Json.campo("mensagem", texto));
    }

    public static Resposta erro(int status, String texto) {
        return new Resposta(status, Json.campo("erro", texto));
    }

    public int getStatus() {
        return status;
    }

    public String getJson() {
        return json;
    }
}
