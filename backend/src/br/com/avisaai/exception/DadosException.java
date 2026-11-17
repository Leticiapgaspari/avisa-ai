package br.com.avisaai.exception;

public class DadosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        DUPLICADO,
        EM_USO,
        TEXTO_GRANDE,
        INDISPONIVEL
    }

    public static final String MSG_DUPLICADO = "Já existe um registro com este valor.";
    public static final String MSG_EM_USO = "Não é possível excluir: existem ocorrências ligadas a este registro.";
    public static final String MSG_TEXTO_GRANDE = "Texto maior que o permitido para este campo.";
    public static final String MSG_INDISPONIVEL = "Não foi possível acessar o banco de dados. Tente novamente.";

    private final Tipo tipo;

    public DadosException(Tipo tipo, String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.tipo = tipo;
    }

    public static DadosException duplicado(Throwable causa) {
        return new DadosException(Tipo.DUPLICADO, MSG_DUPLICADO, causa);
    }

    public static DadosException emUso(Throwable causa) {
        return new DadosException(Tipo.EM_USO, MSG_EM_USO, causa);
    }

    public static DadosException textoGrande(Throwable causa) {
        return new DadosException(Tipo.TEXTO_GRANDE, MSG_TEXTO_GRANDE, causa);
    }

    public static DadosException indisponivel(Throwable causa) {
        return new DadosException(Tipo.INDISPONIVEL, MSG_INDISPONIVEL, causa);
    }

    public Tipo getTipo() {
        return tipo;
    }
}
