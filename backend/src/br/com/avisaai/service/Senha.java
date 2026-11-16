package br.com.avisaai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// o banco guarda só o hash da senha (SHA-256 em hexa, 64 caracteres)
final class Senha {

    static final int TAMANHO_MINIMO = 6;

    private Senha() {
    }

    static String gerarHash(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // todo JDK tem SHA-256, então não deve acontecer
            throw new IllegalStateException("SHA-256 indisponível neste Java.", e);
        }
    }

    static boolean confere(String senha, String hashGuardado) {
        if (senha == null || hashGuardado == null) {
            return false;
        }
        // tempo constante: não dá pra adivinhar o hash pelo tempo da resposta
        return MessageDigest.isEqual(
                gerarHash(senha).getBytes(StandardCharsets.UTF_8),
                hashGuardado.getBytes(StandardCharsets.UTF_8));
    }

    static boolean curta(String senha) {
        return senha == null || senha.length() < TAMANHO_MINIMO;
    }
}
