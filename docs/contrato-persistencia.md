# Contrato de persistência do backend

O que o backend em Java espera da camada de banco, para integrar o trabalho do SQL sem mexer em regra de negócio.

## Como está hoje

- Os contratos são as interfaces em `backend/src/br/com/avisaai/dao/`: `BairroDao`, `CategoriaDao`, `UsuarioDao`, `OcorrenciaDao`, `ApoioDao` e `RelatorioDao`.
- Quem implementa hoje é o pacote `dao/memoria/`. Essa implementação é **PROVISÓRIA**: guarda tudo em memória, traz a mesma carga de exemplo do `sql/avisa_ai.sql` e perde os dados quando o servidor para.
- Os services só conhecem as interfaces. Trocar a implementação não muda nenhuma regra, rota ou JSON.

## O que falta para integrar

1. Criar uma classe JDBC para cada interface, por exemplo `BairroDaoJdbc implements BairroDao`, no pacote `dao`. Pela especificação, o `dao` é o único pacote com SQL e com `java.sql`.
2. Em `Main.java`, trocar as linhas do bloco marcado como "PERSISTÊNCIA PROVISÓRIA" pelas classes JDBC.
3. Apagar o pacote `dao/memoria/` quando tudo estiver ligado ao Oracle.

A conexão já está pronta: `ConnectionFactory.getConnection()` lê o `backend/db.properties` e devolve uma `Connection` com o Oracle. Ela já foi testada no servidor da FIAP. Se o arquivo ou o driver faltar, ou o banco não responder, ela lança `DadosException`. Use com try-with-resources:

```java
try (Connection con = ConnectionFactory.getConnection();
     PreparedStatement ps = con.prepareStatement(...)) {
    ...
} catch (SQLException e) {
    throw ErroBanco.traduzir(e);
}
```

## Regras que valem para todos os DAOs

| Assunto | O que o backend espera |
|---|---|
| Erros | O DAO nunca imprime nem engole erro. Toda `SQLException` sai como `DadosException`, de preferência com `throw ErroBanco.traduzir(e);` (já existe em `dao/ErroBanco.java`). |
| Tradução dos erros | ORA-00001 vira `DUPLICADO` (HTTP 400). ORA-02292 vira `EM_USO` (HTTP 400). Qualquer outro erro, como banco fora do ar ou senha errada, vira `INDISPONIVEL` (HTTP 500). A `SQLException` original fica como causa e só aparece no console. |
| "Não encontrado" | `buscarPorId` devolve `null`. `atualizar`, `atualizarStatus` e `excluir` devolvem `false` quando o id não existe. Quem transforma isso em 404 é o service. |
| Id gerado | `inserir` coloca no objeto recebido o id gerado pelo banco e devolve o mesmo objeto. |
| Validação | O DAO não valida regra. Quando um método é chamado, o service já conferiu campos obrigatórios, tamanhos, permissões e se as chaves estrangeiras existem. |
| Datas | `java.time.LocalDate` no Java. O banco usa `DATE`. |

## Operações por DAO

### BairroDao e CategoriaDao

O formato é o mesmo para as duas tabelas (`BAIRRO`/`ID_BAIRRO` e `CATEGORIA`/`ID_CATEGORIA`).

| Método | Recebe | Devolve | Comportamento esperado |
|---|---|---|---|
| `inserir` | objeto com `nome` | o objeto com `id` preenchido | Nome repetido: `DUPLICADO` |
| `listar` | nada | lista | Em ordem alfabética de nome |
| `buscarPorId` | `int id` | objeto ou `null` | |
| `atualizar` | objeto com `id` e `nome` | `boolean` | Nome repetido: `DUPLICADO` |
| `excluir` | `int id` | `boolean` | Registro usado por usuário ou ocorrência: `EM_USO` |

### UsuarioDao

| Método | Recebe | Devolve | Comportamento esperado |
|---|---|---|---|
| `inserir` | `Usuario` com nome, email, `getTipo()` e `bairro.id` | o usuário com `id` | E-mail repetido: `DUPLICADO` |
| `listar` | nada | `List<Usuario>` | Ordem alfabética de nome, com bairro (id e nome) |
| `buscarPorId` | `int id` | `Usuario` ou `null` | Com bairro (id e nome) |
| `buscarPorEmail` | `String email` | `Usuario` ou `null` | **Operação extra**, fora do CRUD. Comparação exata: o service já grava o e-mail em minúsculas. |
| `atualizar` | `Usuario` com `id` | `boolean` | Altera nome, email, tipo e bairro. E-mail repetido: `DUPLICADO` |
| `excluir` | `int id` | `boolean` | Os apoios do usuário saem junto. Usuário autor de ocorrências: `EM_USO` |

**Montagem a partir do banco:** use `Usuario.criar(rs.getString("TIPO"))`. Esse método devolve um `Cidadao` ou um `Agente`, e é isso que faz as permissões funcionarem.

### OcorrenciaDao

Toda ocorrência devolvida deve vir **completa e de uma vez só**, numa consulta com JOIN:

- `autor`: id, nome e tipo, montado com `Usuario.criar(...)`, porque as permissões dependem do tipo;
- `categoria` e `bairro`: id e nome;
- `totalApoios`: contagem dos apoios da ocorrência (não é coluna).

| Método | Recebe | Devolve | Comportamento esperado |
|---|---|---|---|
| `inserir` | `Ocorrencia` | a ocorrência com `id` | Grava titulo, descricao (pode ser `null`), endereco, status, dataAbertura, `autor.id`, `categoria.id` e `bairro.id`. O status e a data já vêm definidos pelo service. |
| `listar` | `Integer idBairro`, `StatusOcorrencia status` | `List<Ocorrencia>` | Os dois filtros são opcionais, e `null` significa sem filtro. Mais recentes primeiro (`DATA_ABERTURA` e depois id, decrescentes). |
| `buscarPorId` | `int id` | `Ocorrencia` ou `null` | |
| `atualizar` | `Ocorrencia` com `id` | `boolean` | Altera **só** titulo, descricao, endereco, categoria e bairro. Não mexe em status, autor nem data. |
| `atualizarStatus` | `int id`, `StatusOcorrencia` | `boolean` | Grava `status.name()`: `ABERTA`, `EM_ANDAMENTO` ou `RESOLVIDA` |
| `excluir` | `int id` | `boolean` | Os apoios da ocorrência saem junto. O `ON DELETE CASCADE` do script atual já faz isso. |

### ApoioDao

| Método | Recebe | Devolve | Comportamento esperado |
|---|---|---|---|
| `inserir` | `Apoio` (usuario.id, ocorrencia.id, dataApoio) | nada | Par repetido: `DUPLICADO`. O service transforma em "Você já apoiou esta ocorrência." |
| `excluir` | `int idUsuario, int idOcorrencia` | `boolean` | |
| `existe` | `int idUsuario, int idOcorrencia` | `boolean` | |

### RelatorioDao

As consultas são as do `sql/relatorios.sql`. O backend só precisa que cada linha chegue neste formato:

| Método | Linha devolvida | JSON gerado |
|---|---|---|
| `porBairro` | `LinhaRelatorio(nome, total)` | `{"nome":"Centro","total":3}` |
| `porCategoria` | `LinhaRelatorio(nome, total)` | idem |
| `porStatus` | `LinhaRelatorio(nome, total)` | `{"nome":"ABERTA","total":6}` |
| `rankingApoios` | `LinhaRanking(id, titulo, nomeDoBairro, status, totalApoios)` | `{"id":1,"titulo":"...","bairro":"Jardim das Flores","status":"ABERTA","totalApoios":5}` |

A ordem das linhas é a que a consulta devolver. A versão em memória faz uma aproximação (maior total primeiro). Para os resultados oficiais, valem as consultas do responsável pelo banco.

## Dados que o backend persiste

O backend precisa guardar e ler estes dados. Os nomes das colunas são definidos pelo responsável pelo banco.

| Entidade | Campos | Restrições que o backend espera do banco |
|---|---|---|
| Bairro | id (gerado), nome | nome único |
| Categoria | id (gerado), nome | nome único |
| Usuário | id (gerado), nome, email, tipo (`CIDADAO`/`AGENTE`), bairro | email único; bairro existente |
| Ocorrência | id (gerado), titulo, descricao (opcional), endereco, status (`ABERTA`/`EM_ANDAMENTO`/`RESOLVIDA`), data de abertura, autor, categoria, bairro | chaves estrangeiras para usuário, categoria e bairro |
| Apoio | usuário, ocorrência, data | par usuário+ocorrência único; sai junto ao excluir a ocorrência ou o usuário |

Tamanhos máximos que o service já valida antes de gravar: nome de bairro e de categoria 80; nome de usuário 100; e-mail 120; título 120; descrição 500; endereço 200. Se alguma coluna mudar de tamanho, ajuste as constantes nos services.
