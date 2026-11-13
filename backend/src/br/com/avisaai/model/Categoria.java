package br.com.avisaai.model;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public class Categoria implements JsonSerializavel {

    private Integer id;
    private String nome;

    public Categoria() {
    }

    public Categoria(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + Json.numero(id) + ",\"nome\":" + Json.texto(nome) + "}";
    }
}
