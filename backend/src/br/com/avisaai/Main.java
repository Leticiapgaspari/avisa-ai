package br.com.avisaai;

import java.io.IOException;
import java.net.BindException;

import br.com.avisaai.api.ApoioHandler;
import br.com.avisaai.api.BairroHandler;
import br.com.avisaai.api.CategoriaHandler;
import br.com.avisaai.api.LoginHandler;
import br.com.avisaai.api.OcorrenciaHandler;
import br.com.avisaai.api.OcorrenciaStatusHandler;
import br.com.avisaai.api.RaizHandler;
import br.com.avisaai.api.RelatorioHandler;
import br.com.avisaai.api.Servidor;
import br.com.avisaai.api.UsuarioHandler;
import br.com.avisaai.api.UsuarioSenhaHandler;
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

        // DAOs do Oracle (conexão no backend/db.properties)
        BairroDao bairroDao = new BairroDaoJdbc();
        CategoriaDao categoriaDao = new CategoriaDaoJdbc();
        UsuarioDao usuarioDao = new UsuarioDaoJdbc();
        OcorrenciaDao ocorrenciaDao = new OcorrenciaDaoJdbc();
        ApoioDao apoioDao = new ApoioDaoJdbc();
        RelatorioDao relatorioDao = new RelatorioDaoJdbc();

        BairroService bairroService = new BairroService(bairroDao, usuarioDao);
        CategoriaService categoriaService = new CategoriaService(categoriaDao, usuarioDao);
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
                    new UsuarioSenhaHandler(usuarioService),
                    new LoginHandler(usuarioService),
                    new BairroHandler(bairroService),
                    new CategoriaHandler(categoriaService),
                    new ApoioHandler(apoioService),
                    new RelatorioHandler(relatorioService));
            servidor.iniciar();
            System.out.println("Avisa Aí: API no ar em http://localhost:" + porta);
            System.out.println("Persistência: banco Oracle (JDBC).");
            String falha = ConnectionFactory.testarConexao();
            if (falha == null) {
                System.out.println("Conexão com o Oracle: OK.");
            } else {
                System.out.println("Conexão com o Oracle: FALHOU. " + falha);
            }
        } catch (BindException e) {
            System.err.println("A porta " + porta + " já está em uso. Feche o outro programa ou rode com outra porta:");
            System.err.println("  java -cp \"out;lib/*\" br.com.avisaai.Main 8081");
            System.err.println("  (no Linux/macOS, troque o ; por :)");
        } catch (IOException e) {
            System.err.println("Não foi possível iniciar o servidor: " + e.getMessage());
        }
    }
}
