package br.com.avisaai.dto;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public class LinhaRelatorio implements JsonSerializavel {

    private String nome;
    private int total;

    public LinhaRelatorio() {
    }

    public LinhaRelatorio(String nome, int total) {
        this.nome = nome;
        this.total = total;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toJson() {
        return "{\"nome\":" + Json.texto(nome) + ",\"total\":" + total + "}";
    }
}
