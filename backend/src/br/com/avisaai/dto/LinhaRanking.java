package br.com.avisaai.dto;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public class LinhaRanking implements JsonSerializavel {

    private Integer id;
    private String titulo;
    private String bairro;
    private String status;
    private int totalApoios;

    public LinhaRanking() {
    }

    public LinhaRanking(Integer id, String titulo, String bairro, String status, int totalApoios) {
        this.id = id;
        this.titulo = titulo;
        this.bairro = bairro;
        this.status = status;
        this.totalApoios = totalApoios;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalApoios() {
        return totalApoios;
    }

    public void setTotalApoios(int totalApoios) {
        this.totalApoios = totalApoios;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + Json.numero(id)
                + ",\"titulo\":" + Json.texto(titulo)
                + ",\"bairro\":" + Json.texto(bairro)
                + ",\"status\":" + Json.texto(status)
                + ",\"totalApoios\":" + totalApoios
                + "}";
    }
}
