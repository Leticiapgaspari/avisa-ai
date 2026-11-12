package br.com.avisaai.model;

import java.time.LocalDate;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public class Apoio implements JsonSerializavel {

    private Usuario usuario;
    private Ocorrencia ocorrencia;
    private LocalDate dataApoio;

    public Apoio() {
    }

    public Apoio(Usuario usuario, Ocorrencia ocorrencia, LocalDate dataApoio) {
        this.usuario = usuario;
        this.ocorrencia = ocorrencia;
        this.dataApoio = dataApoio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public LocalDate getDataApoio() {
        return dataApoio;
    }

    public void setDataApoio(LocalDate dataApoio) {
        this.dataApoio = dataApoio;
    }

    @Override
    public String toJson() {
        return "{\"idUsuario\":" + Json.numero(usuario == null ? null : usuario.getId())
                + ",\"idOcorrencia\":" + Json.numero(ocorrencia == null ? null : ocorrencia.getId())
                + ",\"dataApoio\":" + Json.texto(dataApoio == null ? null : dataApoio.toString())
                + "}";
    }
}
