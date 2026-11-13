package br.com.avisaai.dao;

import java.sql.SQLException;

import br.com.avisaai.exception.DadosException;

public final class ErroBanco {

    public static final int ORA_VALOR_DUPLICADO = 1;
    public static final int ORA_REGISTRO_EM_USO = 2292;

    private ErroBanco() {
    }

    public static DadosException traduzir(SQLException e) {
        switch (e.getErrorCode()) {
            case ORA_VALOR_DUPLICADO:
                return DadosException.duplicado(e);
            case ORA_REGISTRO_EM_USO:
                return DadosException.emUso(e);
            default:
                return DadosException.indisponivel(e);
        }
    }
}
