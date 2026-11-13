package br.com.avisaai.model;

public class Cidadao extends Usuario {

    @Override
    public String getTipo() {
        return TIPO_CIDADAO;
    }

    @Override
    public boolean podeApoiar() {
        return true;
    }

    @Override
    public boolean podeMudarStatus() {
        return false;
    }

    @Override
    public boolean podeEditar(Ocorrencia ocorrencia) {
        return ehAutorDe(ocorrencia);
    }
}
