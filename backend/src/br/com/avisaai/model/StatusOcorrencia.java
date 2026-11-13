package br.com.avisaai.model;

public enum StatusOcorrencia {
    ABERTA,
    EM_ANDAMENTO,
    RESOLVIDA;

    public static StatusOcorrencia deTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (StatusOcorrencia s : values()) {
            if (s.name().equals(normalizado)) {
                return s;
            }
        }
        return null;
    }
}
