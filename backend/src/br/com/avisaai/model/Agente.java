package br.com.avisaai.model;

public class Agente extends Usuario {

    @Override
    public String getTipo() {
        return TIPO_AGENTE;
    }

    @Override
    public boolean podeApoiar() {
        return false;
    }

    @Override
    public boolean podeMudarStatus() {
        return true;
    }

    @Override
    public boolean podeEditar(Ocorrencia ocorrencia) {
        return true;
    }
}
