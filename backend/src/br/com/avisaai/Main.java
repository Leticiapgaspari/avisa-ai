package br.com.avisaai;

import java.io.IOException;
import java.net.BindException;

import br.com.avisaai.api.ApoioHandler;
import br.com.avisaai.api.BairroHandler;
import br.com.avisaai.api.CategoriaHandler;
import br.com.avisaai.api.OcorrenciaHandler;
import br.com.avisaai.api.OcorrenciaStatusHandler;
import br.com.avisaai.api.RaizHandler;
import br.com.avisaai.api.RelatorioHandler;
import br.com.avisaai.api.Servidor;
import br.com.avisaai.api.UsuarioHandler;
import br.com.avisaai.dao.ApoioDao;
import br.com.avisaai.dao.ApoioDaoJdbc;
import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.BairroDaoJdbc;
import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.dao.CategoriaDaoJdbc;
import br.com.avisaai.dao.ConnectionFactory;
import br.com.avisaai.dao.OcorrenciaDao;
import br.com.avisaai.dao.OcorrenciaDaoJdbc;
import br.com.avisaai.dao.RelatorioDao;
import br.com.avisaai.dao.RelatorioDaoJdbc;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.dao.UsuarioDaoJdbc;
import br.com.avisaai.dao.memoria.ApoioDaoMemoria;
import br.com.avisaai.dao.memoria.BairroDaoMemoria;
import br.com.avisaai.dao.memoria.BancoMemoria;
import br.com.avisaai.dao.memoria.CategoriaDaoMemoria;
import br.com.avisaai.dao.memoria.OcorrenciaDaoMemoria;
import br.com.avisaai.dao.memoria.RelatorioDaoMemoria;
import br.com.avisaai.dao.memoria.UsuarioDaoMemoria;
import br.com.avisaai.service.ApoioService;
import br.com.avisaai.service.BairroService;
import br.com.avisaai.service.CategoriaService;
import br.com.avisaai.service.OcorrenciaService;
import br.com.avisaai.service.RelatorioService;
import br.com.avisaai.service.UsuarioService;

public class Main {

    private static final int PORTA_PADRAO = 8080;

    public static void main(String[] args) {
        int porta = PORTA_PADRAO;
        boolean emMemoria = false;
        for (String arg : args) {
            if ("--memoria".equals(arg)) {
                emMemoria = true;
            } else {
                try {
                    porta = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    System.err.println("Argumento inválido: " + arg + ". Use a porta (ex.: 8081) e/ou --memoria.");
                }
            }
        }

        BairroDao bairroDao;
        CategoriaDao categoriaDao;
        UsuarioDao usuarioDao;
        OcorrenciaDao ocorrenciaDao;
        ApoioDao apoioDao;
        RelatorioDao relatorioDao;
        if (emMemoria) {
            // Modo provisório, para desenvolver sem banco: dados de exemplo em memória.
            BancoMemoria banco = new BancoMemoria();
            banco.carregarDadosDeExemplo();
            bairroDao = new BairroDaoMemoria(banco);
            categoriaDao = new CategoriaDaoMemoria(banco);
            usuarioDao = new UsuarioDaoMemoria(banco);
            ocorrenciaDao = new OcorrenciaDaoMemoria(banco);
            apoioDao = new ApoioDaoMemoria(banco);
            relatorioDao = new RelatorioDaoMemoria(banco);
        } else {
            bairroDao = new BairroDaoJdbc();
            categoriaDao = new CategoriaDaoJdbc();
            usuarioDao = new UsuarioDaoJdbc();
            ocorrenciaDao = new OcorrenciaDaoJdbc();
            apoioDao = new ApoioDaoJdbc();
            relatorioDao = new RelatorioDaoJdbc();
        }

        BairroService bairroService = new BairroService(bairroDao);
        CategoriaService categoriaService = new CategoriaService(categoriaDao);
        UsuarioService usuarioService = new UsuarioService(usuarioDao, bairroDao);
        OcorrenciaService ocorrenciaService = new OcorrenciaService(ocorrenciaDao, usuarioDao, categoriaDao, bairroDao);
        ApoioService apoioService = new ApoioService(apoioDao, usuarioDao, ocorrenciaDao);
        RelatorioService relatorioService = new RelatorioService(relatorioDao);

        try {
            Servidor servidor = new Servidor(porta,
                    new RaizHandler(),
                    new OcorrenciaHandler(ocorrenciaService),
                    new OcorrenciaStatusHandler(ocorrenciaService),
                    new UsuarioHandler(usuarioService),
                    new BairroHandler(bairroService),
                    new CategoriaHandler(categoriaService),
                    new ApoioHandler(apoioService),
                    new RelatorioHandler(relatorioService));
            servidor.iniciar();
            System.out.println("Avisa Aí: API no ar em http://localhost:" + porta);
            if (emMemoria) {
                System.out.println("Persistência: EM MEMÓRIA (provisória; os dados se perdem ao parar).");
            } else {
                String falha = ConnectionFactory.testarConexao();
                if (falha == null) {
                    System.out.println("Persistência: Oracle. Conexão OK.");
                } else {
                    System.out.println("Persistência: Oracle. Conexão FALHOU: " + falha);
                    System.out.println("A API continua no ar e responde com erro até o banco voltar.");
                }
            }
        } catch (BindException e) {
            System.err.println("A porta " + porta + " já está em uso. Feche o outro programa ou rode com outra porta:");
            System.err.println("  java -cp \"out;lib/*\" br.com.avisaai.Main 8081");
        } catch (IOException e) {
            System.err.println("Não foi possível iniciar o servidor: " + e.getMessage());
        }
    }
}
