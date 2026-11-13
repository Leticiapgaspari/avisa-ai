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
import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.dao.ConnectionFactory;
import br.com.avisaai.dao.OcorrenciaDao;
import br.com.avisaai.dao.RelatorioDao;
import br.com.avisaai.dao.UsuarioDao;
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
        if (args.length > 0) {
            try {
                porta = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + args[0] + ". Usando " + PORTA_PADRAO + ".");
            }
        }

        // Persistência provisória em memória: trocar pelas implementações JDBC na integração com o banco.
        BancoMemoria banco = new BancoMemoria();
        banco.carregarDadosDeExemplo();
        BairroDao bairroDao = new BairroDaoMemoria(banco);
        CategoriaDao categoriaDao = new CategoriaDaoMemoria(banco);
        UsuarioDao usuarioDao = new UsuarioDaoMemoria(banco);
        OcorrenciaDao ocorrenciaDao = new OcorrenciaDaoMemoria(banco);
        ApoioDao apoioDao = new ApoioDaoMemoria(banco);
        RelatorioDao relatorioDao = new RelatorioDaoMemoria(banco);

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
            System.out.println("Persistência: EM MEMÓRIA (provisória; os dados se perdem ao parar).");
            String falha = ConnectionFactory.testarConexao();
            if (falha == null) {
                System.out.println("Conexão com o Oracle: OK.");
            } else {
                System.out.println("Conexão com o Oracle: FALHOU. " + falha);
            }
        } catch (BindException e) {
            System.err.println("A porta " + porta + " já está em uso. Feche o outro programa ou rode com outra porta:");
            System.err.println("  java -cp out br.com.avisaai.Main 8081");
        } catch (IOException e) {
            System.err.println("Não foi possível iniciar o servidor: " + e.getMessage());
        }
    }
}
