package br.com.avisaai.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.util.Json;

public class Requisicao {

    static final String MSG_FORMATO_INVALIDO = "Dados enviados em formato inválido.";
    static final String MSG_ID_OBRIGATORIO = "Informe o id na rota (ex.: ?id=1).";

    private final HttpExchange exchange;
    private final Map<String, String> query;
    private Map<String, String> corpo;

    public Requisicao(HttpExchange exchange) {
        this.exchange = exchange;
        this.query = lerQuery(exchange.getRequestURI().getRawQuery());
    }

    public String parametro(String nome) {
        String valor = query.get(nome);
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    public Integer parametroInteiro(String nome) {
        return converterInteiro(parametro(nome));
    }

    public int id() {
        Integer id = parametroInteiro("id");
        if (id == null) {
            throw new RegraNegocioException(MSG_ID_OBRIGATORIO);
        }
        return id;
    }

    public String texto(String campo) {
        return corpo().get(campo);
    }

    public Integer inteiro(String campo) {
        String valor = corpo().get(campo);
        return converterInteiro(valor == null || valor.trim().isEmpty() ? null : valor.trim());
    }

    private Map<String, String> corpo() {
        if (corpo == null) {
            try {
                corpo = Json.lerObjeto(lerCorpo());
            } catch (IllegalArgumentException e) {
                throw new RegraNegocioException(MSG_FORMATO_INVALIDO);
            }
        }
        return corpo;
    }

    private String lerCorpo() {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bloco = new byte[4096];
            int lidos;
            while ((lidos = in.read(bloco)) != -1) {
                buffer.write(bloco, 0, lidos);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RegraNegocioException(MSG_FORMATO_INVALIDO);
        }
    }

    private static Integer converterInteiro(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            throw new RegraNegocioException(MSG_FORMATO_INVALIDO);
        }
    }

    private static Map<String, String> lerQuery(String rawQuery) {
        Map<String, String> mapa = new HashMap<String, String>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return mapa;
        }
        for (String par : rawQuery.split("&")) {
            if (par.isEmpty()) {
                continue;
            }
            int igual = par.indexOf('=');
            String chave = igual >= 0 ? par.substring(0, igual) : par;
            String valor = igual >= 0 ? par.substring(igual + 1) : "";
            mapa.put(decodificar(chave), decodificar(valor));
        }
        return mapa;
    }

    private static String decodificar(String texto) {
        try {
            return URLDecoder.decode(texto, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            throw new RegraNegocioException(MSG_FORMATO_INVALIDO);
        }
    }
}
