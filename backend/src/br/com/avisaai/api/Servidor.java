package br.com.avisaai.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class Servidor {

    private final HttpServer http;

    public Servidor(int porta, HandlerBase... handlers) throws IOException {
        http = HttpServer.create(new InetSocketAddress(porta), 0);
        for (HandlerBase handler : handlers) {
            http.createContext(handler.getRota(), handler);
        }
        http.setExecutor(Executors.newFixedThreadPool(8));
    }

    public void iniciar() {
        http.start();
    }
}
