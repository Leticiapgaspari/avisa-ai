package br.com.avisaai.model;

import java.time.LocalDate;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public class Ocorrencia implements JsonSerializavel {

    private Integer id;
    private String titulo;
    private String descricao;
    private String endereco;
    private StatusOcorrencia status;
    private LocalDate dataAbertura;
    private Usuario autor;
    private Categoria categoria;
    private Bairro bairro;
    private int totalApoios;

    public boolean isResolvida() {
        return status == StatusOcorrencia.RESOLVIDA;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public void setStatus(StatusOcorrencia status) {
        this.status = status;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
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
                + ",\"descricao\":" + Json.texto(descricao)
                + ",\"endereco\":" + Json.texto(endereco)
                + ",\"status\":" + Json.texto(status == null ? null : status.name())
                + ",\"dataAbertura\":" + Json.texto(dataAbertura == null ? null : dataAbertura.toString())
                + ",\"autor\":" + (autor == null ? "null" : autor.toJsonResumo())
                + ",\"categoria\":" + Json.objeto(categoria)
                + ",\"bairro\":" + Json.objeto(bairro)
                + ",\"totalApoios\":" + totalApoios
                + "}";
    }
}
