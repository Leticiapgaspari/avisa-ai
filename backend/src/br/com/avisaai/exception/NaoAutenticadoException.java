package br.com.avisaai.exception;

public class NaoAutenticadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
