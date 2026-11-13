package br.com.avisaai.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import br.com.avisaai.exception.DadosException;

public final class ConnectionFactory {

    private static final String[] CAMINHOS = {"db.properties", "backend/db.properties"};

    static final String MSG_SEM_ARQUIVO =
            "Arquivo db.properties não encontrado. Copie backend/db.properties.example para "
            + "backend/db.properties e preencha db.url, db.user e db.password.";
    static final String MSG_INCOMPLETO =
            "O arquivo db.properties precisa ter db.url, db.user e db.password preenchidos.";
    static final String MSG_SEM_DRIVER =
            "Driver do Oracle não encontrado. Coloque o ojdbc11.jar em backend/lib e rode com -cp \"out;lib/*\".";

    private static Properties configuracao;

    private ConnectionFactory() {
    }

    public static Connection getConnection() {
        Properties p = carregarConfiguracao();
        try {
            return DriverManager.getConnection(
                    p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("No suitable driver")) {
                throw new DadosException(DadosException.Tipo.INDISPONIVEL, DadosException.MSG_INDISPONIVEL,
                        new IllegalStateException(MSG_SEM_DRIVER, e));
            }
            throw ErroBanco.traduzir(e);
        }
    }

    public static String testarConexao() {
        try (Connection conexao = getConnection()) {
            return conexao.isValid(5) ? null : "o banco não respondeu.";
        } catch (DadosException e) {
            Throwable causa = e.getCause();
            return causa == null ? e.getMessage() : causa.getMessage();
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    private static synchronized Properties carregarConfiguracao() {
        if (configuracao != null) {
            return configuracao;
        }
        Path arquivo = null;
        for (String caminho : CAMINHOS) {
            Path candidato = Paths.get(caminho);
            if (Files.isRegularFile(candidato)) {
                arquivo = candidato;
                break;
            }
        }
        if (arquivo == null) {
            throw configuracaoInvalida(MSG_SEM_ARQUIVO, null);
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(arquivo)) {
            p.load(in);
        } catch (IOException e) {
            throw configuracaoInvalida(MSG_SEM_ARQUIVO, e);
        }
        for (String chave : new String[] {"db.url", "db.user", "db.password"}) {
            String valor = p.getProperty(chave);
            if (valor == null || valor.trim().isEmpty()) {
                throw configuracaoInvalida(MSG_INCOMPLETO, null);
            }
            p.setProperty(chave, valor.trim());
        }
        configuracao = p;
        return p;
    }

    private static DadosException configuracaoInvalida(String mensagem, Throwable causa) {
        return new DadosException(DadosException.Tipo.INDISPONIVEL, DadosException.MSG_INDISPONIVEL,
                new IllegalStateException(mensagem, causa));
    }
}
