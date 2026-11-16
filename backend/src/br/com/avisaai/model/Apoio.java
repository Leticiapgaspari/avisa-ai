package br.com.avisaai.model;

import java.time.LocalDate;

public class Apoio {

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

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public LocalDate getDataApoio() {
        return dataApoio;
    }
}
