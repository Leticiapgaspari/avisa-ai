package br.com.avisaai.dao.memoria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Categoria;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;
import br.com.avisaai.model.Usuario;

// Provisório: armazenamento em memória até a integração com o banco. Os dados se perdem ao parar o servidor.
public class BancoMemoria {

    static final class LinhaUsuario {
        int id;
        String nome;
        String email;
        String tipo;
        int idBairro;
    }

    static final class LinhaOcorrencia {
        int id;
        String titulo;
        String descricao;
        String endereco;
        StatusOcorrencia status;
        LocalDate dataAbertura;
        int idUsuario;
        int idCategoria;
        int idBairro;
    }

    static final class LinhaApoio {
        int idUsuario;
        int idOcorrencia;
        LocalDate dataApoio;
    }

    final Map<Integer, String> bairros = new LinkedHashMap<Integer, String>();
    final Map<Integer, String> categorias = new LinkedHashMap<Integer, String>();
    final Map<Integer, LinhaUsuario> usuarios = new LinkedHashMap<Integer, LinhaUsuario>();
    final Map<Integer, LinhaOcorrencia> ocorrencias = new LinkedHashMap<Integer, LinhaOcorrencia>();
    final List<LinhaApoio> apoios = new ArrayList<LinhaApoio>();

    private int seqBairro;
    private int seqCategoria;
    private int seqUsuario;
    private int seqOcorrencia;

    int proximoIdBairro() {
        return ++seqBairro;
    }

    int proximoIdCategoria() {
        return ++seqCategoria;
    }

    int proximoIdUsuario() {
        return ++seqUsuario;
    }

    int proximoIdOcorrencia() {
        return ++seqOcorrencia;
    }

    Bairro montarBairro(int id) {
        String nome = bairros.get(id);
        return nome == null ? null : new Bairro(id, nome);
    }

    Categoria montarCategoria(int id) {
        String nome = categorias.get(id);
        return nome == null ? null : new Categoria(id, nome);
    }

    Usuario montarUsuario(int id) {
        LinhaUsuario linha = usuarios.get(id);
        if (linha == null) {
            return null;
        }
        Usuario u = Usuario.criar(linha.tipo);
        u.setId(linha.id);
        u.setNome(linha.nome);
        u.setEmail(linha.email);
        u.setBairro(montarBairro(linha.idBairro));
        return u;
    }

    Ocorrencia montarOcorrencia(LinhaOcorrencia linha) {
        Ocorrencia o = new Ocorrencia();
        o.setId(linha.id);
        o.setTitulo(linha.titulo);
        o.setDescricao(linha.descricao);
        o.setEndereco(linha.endereco);
        o.setStatus(linha.status);
        o.setDataAbertura(linha.dataAbertura);
        o.setAutor(montarUsuario(linha.idUsuario));
        o.setCategoria(montarCategoria(linha.idCategoria));
        o.setBairro(montarBairro(linha.idBairro));
        o.setTotalApoios(contarApoios(linha.id));
        return o;
    }

    int contarApoios(int idOcorrencia) {
        int total = 0;
        for (LinhaApoio a : apoios) {
            if (a.idOcorrencia == idOcorrencia) {
                total++;
            }
        }
        return total;
    }

    static void verificarUnico(Map<Integer, String> tabela, String valor, Integer idIgnorado) {
        for (Map.Entry<Integer, String> e : tabela.entrySet()) {
            if (e.getValue().equals(valor) && !e.getKey().equals(idIgnorado)) {
                throw DadosException.duplicado(null);
            }
        }
    }

    public void carregarDadosDeExemplo() {
        String[] nomesBairros = {"Centro", "Jardim das Flores", "Vila Nova", "Parque das Águas", "Alto da Serra"};
        for (String nome : nomesBairros) {
            bairros.put(proximoIdBairro(), nome);
        }

        String[] nomesCategorias = {"Buraco na via", "Iluminação pública", "Lixo e entulho",
            "Calçada e acessibilidade", "Árvore e poda", "Sinalização de trânsito"};
        for (String nome : nomesCategorias) {
            categorias.put(proximoIdCategoria(), nome);
        }

        usuario("Ana Beatriz Souza", "ana.souza@exemplo.com", "CIDADAO", 1);
        usuario("Carlos Eduardo Lima", "carlos.lima@exemplo.com", "CIDADAO", 2);
        usuario("Mariana Oliveira", "mariana.oliveira@exemplo.com", "CIDADAO", 2);
        usuario("João Pedro Santos", "joao.santos@exemplo.com", "CIDADAO", 3);
        usuario("Fernanda Costa", "fernanda.costa@exemplo.com", "CIDADAO", 4);
        usuario("Rafael Almeida", "rafael.almeida@exemplo.com", "CIDADAO", 5);
        usuario("Patrícia Mendes", "patricia.mendes@exemplo.com", "AGENTE", 1);
        usuario("Roberto Nunes", "roberto.nunes@exemplo.com", "AGENTE", 3);

        ocorrencia("Buraco grande na Rua das Acácias", "Buraco com cerca de 1 metro, carros desviam para a contramão",
                "Rua das Acácias, 120", StatusOcorrencia.ABERTA, "2026-11-02", 2, 1, 2);
        ocorrencia("Poste apagado em frente à escola", "Rua escura na saída dos alunos à noite, risco de assalto e atropelamento",
                "Rua dos Girassóis, 45", StatusOcorrencia.EM_ANDAMENTO, "2026-11-03", 3, 2, 2);
        ocorrencia("Entulho acumulado na praça central", "Entulho ocupa metade do passeio e atrai ratos e insetos",
                "Praça da Matriz, s/n", StatusOcorrencia.ABERTA, "2026-11-04", 1, 3, 1);
        ocorrencia("Calçada quebrada e sem rampa", "Cadeirantes e idosos precisam descer para a rua, onde passam carros",
                "Avenida Brasil, 880", StatusOcorrencia.ABERTA, "2026-11-04", 4, 4, 3);
        ocorrencia("Galho caído bloqueando a calçada", "Pedestres desviam pela rua e o galho pode cair sobre quem passa",
                "Rua das Nascentes, 30", StatusOcorrencia.RESOLVIDA, "2026-10-28", 5, 5, 4);
        ocorrencia("Semáforo piscando no cruzamento", "Motoristas não sabem quem tem a preferência, risco de colisão",
                "Rua Quinze com Avenida Central", StatusOcorrencia.EM_ANDAMENTO, "2026-11-05", 1, 6, 1);
        ocorrencia("Lixo espalhado perto do ponto de ônibus", "Lixo na calçada atrai animais e dificulta o embarque",
                "Rua do Mirante, 210", StatusOcorrencia.ABERTA, "2026-11-06", 6, 3, 5);
        ocorrencia("Buraco na esquina da padaria", "Motos e bicicletas podem cair ao desviar do buraco",
                "Rua São José, 15", StatusOcorrencia.RESOLVIDA, "2026-10-25", 4, 1, 3);
        ocorrencia("Três postes apagados na mesma rua", "Quarteirão inteiro no escuro, moradores têm medo de sair à noite",
                "Rua do Cruzeiro, 300 a 380", StatusOcorrencia.ABERTA, "2026-11-07", 6, 2, 5);
        ocorrencia("Faixa de pedestres apagada", "Motoristas não param e crianças atravessam sem sinalização",
                "Avenida das Rosas, 500", StatusOcorrencia.ABERTA, "2026-11-08", 2, 6, 2);
        ocorrencia("Raiz de árvore levantando a calçada", "Piso irregular causa tropeços, principalmente em idosos",
                "Rua da Fonte, 72", StatusOcorrencia.EM_ANDAMENTO, "2026-11-09", 5, 5, 4);
        ocorrencia("Bueiro sem tampa", "Buraco aberto na rua, risco de queda de pedestres e acidentes",
                "Rua do Comércio, 18", StatusOcorrencia.ABERTA, "2026-11-10", 7, 1, 1);

        int[][] paresApoio = {
            {1, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1},
            {1, 2}, {2, 2}, {4, 2},
            {2, 3}, {5, 3},
            {1, 4}, {2, 4}, {3, 4}, {6, 4},
            {6, 5},
            {3, 6}, {4, 6},
            {5, 7},
            {1, 8}, {2, 8},
            {1, 9}, {3, 9}, {5, 9},
            {4, 11}
        };
        for (int[] par : paresApoio) {
            LinhaApoio a = new LinhaApoio();
            a.idUsuario = par[0];
            a.idOcorrencia = par[1];
            a.dataApoio = ocorrencias.get(par[1]).dataAbertura.plusDays(1);
            apoios.add(a);
        }
    }

    private void usuario(String nome, String email, String tipo, int idBairro) {
        LinhaUsuario u = new LinhaUsuario();
        u.id = proximoIdUsuario();
        u.nome = nome;
        u.email = email;
        u.tipo = tipo;
        u.idBairro = idBairro;
        usuarios.put(u.id, u);
    }

    private void ocorrencia(String titulo, String descricao, String endereco, StatusOcorrencia status,
            String data, int idUsuario, int idCategoria, int idBairro) {
        LinhaOcorrencia o = new LinhaOcorrencia();
        o.id = proximoIdOcorrencia();
        o.titulo = titulo;
        o.descricao = descricao;
        o.endereco = endereco;
        o.status = status;
        o.dataAbertura = LocalDate.parse(data);
        o.idUsuario = idUsuario;
        o.idCategoria = idCategoria;
        o.idBairro = idBairro;
        ocorrencias.put(o.id, o);
    }
}
