package br.com.avisaai.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;

public abstract class HandlerBase implements HttpHandler {

    static final String MSG_METODO = "Método não suportado.";
    static final String MSG_ROTA = "Rota não encontrada.";
    static final String MSG_INTERNO = "Erro interno. Tente novamente.";

    private final String rota;

    protected HandlerBase(String rota) {
        this.rota = rota;
    }

    public String getRota() {
        return rota;
    }

    protected Resposta get(Requisicao req) {
        throw new MetodoNaoSuportado();
    }

    protected Resposta post(Requisicao req) {
        throw new MetodoNaoSuportado();
    }

    protected Resposta put(Requisicao req) {
        throw new MetodoNaoSuportado();
    }

    protected Resposta delete(Requisicao req) {
        throw new MetodoNaoSuportado();
    }

    @Override
    public final void handle(HttpExchange exchange) {
        try {
            adicionarCors(exchange.getResponseHeaders());
            String metodo = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(metodo)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!rotaConfere(exchange.getRequestURI().getPath())) {
                enviar(exchange, Resposta.erro(404, MSG_ROTA));
                return;
            }
            enviar(exchange, processar(metodo, new Requisicao(exchange)));
        } catch (IOException e) {
            System.err.println("Falha ao enviar resposta: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private Resposta processar(String metodo, Requisicao req) {
        try {
            switch (metodo) {
                case "GET":
                    return get(req);
                case "POST":
                    return post(req);
                case "PUT":
                    return put(req);
                case "DELETE":
                    return delete(req);
                default:
                    throw new MetodoNaoSuportado();
            }
        } catch (MetodoNaoSuportado e) {
            return Resposta.erro(405, MSG_METODO);
        } catch (RegraNegocioException e) {
            return Resposta.erro(400, e.getMessage());
        } catch (NaoEncontradoException e) {
            return Resposta.erro(404, e.getMessage());
        } catch (DadosException e) {
            registrarNoConsole(metodo, e);
            int status = e.getTipo() == DadosException.Tipo.INDISPONIVEL ? 500 : 400;
            return Resposta.erro(status, e.getMessage());
        } catch (Exception e) {
            registrarNoConsole(metodo, e);
            return Resposta.erro(500, MSG_INTERNO);
        }
    }

    private void registrarNoConsole(String metodo, Exception e) {
        System.err.println("[" + metodo + " " + rota + "] " + e.getMessage());
        Throwable detalhe = e instanceof DadosException ? e.getCause() : e;
        if (detalhe != null) {
            detalhe.printStackTrace();
        }
    }

    protected boolean rotaConfere(String caminho) {
        return caminho.equals(rota) || caminho.equals(rota + "/");
    }

    private static void adicionarCors(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void enviar(HttpExchange exchange, Resposta resposta) throws IOException {
        byte[] bytes = resposta.getJson().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(resposta.getStatus(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static final class MetodoNaoSuportado extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
