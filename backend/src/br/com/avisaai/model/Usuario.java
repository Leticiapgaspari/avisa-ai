package br.com.avisaai.model;

import br.com.avisaai.util.Json;
import br.com.avisaai.util.JsonSerializavel;

public abstract class Usuario implements JsonSerializavel {

    public static final String TIPO_CIDADAO = "CIDADAO";
    public static final String TIPO_AGENTE = "AGENTE";

    private Integer id;
    private String nome;
    private String email;
    private Bairro bairro;

    public static Usuario criar(String tipo) {
        if (tipo == null) {
            return null;
        }
        String normalizado = tipo.trim().toUpperCase();
        if (TIPO_CIDADAO.equals(normalizado)) {
            return new Cidadao();
        }
        if (TIPO_AGENTE.equals(normalizado)) {
            return new Agente();
        }
        return null;
    }

    public abstract String getTipo();

    public abstract boolean podeApoiar();

    public abstract boolean podeMudarStatus();

    public abstract boolean podeEditar(Ocorrencia ocorrencia);

    public boolean ehAutorDe(Ocorrencia ocorrencia) {
        return id != null
                && ocorrencia != null
                && ocorrencia.getAutor() != null
                && id.equals(ocorrencia.getAutor().getId());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + Json.numero(id)
                + ",\"nome\":" + Json.texto(nome)
                + ",\"email\":" + Json.texto(email)
                + ",\"tipo\":" + Json.texto(getTipo())
                + ",\"bairro\":" + Json.objeto(bairro)
                + "}";
    }

    public String toJsonResumo() {
        return "{\"id\":" + Json.numero(id) + ",\"nome\":" + Json.texto(nome) + "}";
    }
}
