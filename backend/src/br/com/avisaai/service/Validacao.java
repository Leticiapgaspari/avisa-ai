package br.com.avisaai.service;

import br.com.avisaai.exception.RegraNegocioException;

final class Validacao {

    static final String MSG_USUARIO_OBRIGATORIO = "Informe o usuário que está realizando a operação.";

    private Validacao() {
    }

    static boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    static String limpar(String texto) {
        return vazio(texto) ? null : texto.trim();
    }

    static void tamanhoMaximo(String texto, int maximo, String nomeDoCampo) {
        if (texto != null && texto.length() > maximo) {
            throw new RegraNegocioException(nomeDoCampo + " deve ter no máximo " + maximo + " caracteres.");
        }
    }
}
