package br.com.avisaai.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(valor.length() + 2);
        sb.append('"');
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String numero(Number valor) {
        return valor == null ? "null" : valor.toString();
    }

    public static String objeto(JsonSerializavel valor) {
        return valor == null ? "null" : valor.toJson();
    }

    public static String lista(List<? extends JsonSerializavel> itens) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < itens.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(itens.get(i).toJson());
        }
        return sb.append(']').toString();
    }

    public static String campo(String chave, String valor) {
        return "{" + texto(chave) + ":" + texto(valor) + "}";
    }

    public static Map<String, String> lerObjeto(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON vazio");
        }
        Leitor leitor = new Leitor(json);
        Map<String, String> resultado = leitor.lerObjeto();
        leitor.pularEspacos();
        if (!leitor.fim()) {
            throw new IllegalArgumentException("Conteúdo após o fim do objeto");
        }
        return resultado;
    }

    private static final class Leitor {
        private final String s;
        private int pos;

        Leitor(String s) {
            this.s = s;
        }

        boolean fim() {
            return pos >= s.length();
        }

        void pularEspacos() {
            while (!fim() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        char atual() {
            if (fim()) {
                throw new IllegalArgumentException("Fim inesperado do JSON");
            }
            return s.charAt(pos);
        }

        void esperar(char c) {
            pularEspacos();
            if (atual() != c) {
                throw new IllegalArgumentException("Esperado '" + c + "' na posição " + pos);
            }
            pos++;
        }

        Map<String, String> lerObjeto() {
            Map<String, String> mapa = new LinkedHashMap<String, String>();
            esperar('{');
            pularEspacos();
            if (atual() == '}') {
                pos++;
                return mapa;
            }
            while (true) {
                pularEspacos();
                String chave = lerString();
                esperar(':');
                pularEspacos();
                mapa.put(chave, lerValor());
                pularEspacos();
                char c = atual();
                pos++;
                if (c == '}') {
                    return mapa;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Esperado ',' ou '}' na posição " + (pos - 1));
                }
            }
        }

        String lerValor() {
            char c = atual();
            if (c == '"') {
                return lerString();
            }
            if (c == '{' || c == '[') {
                throw new IllegalArgumentException("Objetos ou listas aninhados não são aceitos");
            }
            if (s.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            if (s.startsWith("true", pos)) {
                pos += 4;
                return "true";
            }
            if (s.startsWith("false", pos)) {
                pos += 5;
                return "false";
            }
            return lerNumero();
        }

        String lerNumero() {
            int inicio = pos;
            while (!fim() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            String numero = s.substring(inicio, pos);
            try {
                new BigDecimal(numero);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido na posição " + inicio);
            }
            return numero;
        }

        String lerString() {
            if (atual() != '"') {
                throw new IllegalArgumentException("Esperado texto na posição " + pos);
            }
            pos++;
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = atual();
                pos++;
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = atual();
                pos++;
                switch (e) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append(e);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (pos + 4 > s.length()) {
                            throw new IllegalArgumentException("Escape \\u incompleto");
                        }
                        try {
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Escape \\u inválido");
                        }
                        pos += 4;
                        break;
                    default:
                        throw new IllegalArgumentException("Escape inválido: \\" + e);
                }
            }
        }
    }
}
