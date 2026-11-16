# Contrato de persistência do backend

O que o backend em Java espera da camada de banco, e como a persistência no Oracle está organizada.

## Organização

- Os contratos são as interfaces em `backend/src/br/com/avisaai/dao/`: `BairroDao`, `CategoriaDao`, `UsuarioDao`, `OcorrenciaDao`, `ApoioDao` e `RelatorioDao`.
- Cada contrato é implementado por uma classe JDBC no mesmo pacote: `BairroDaoJdbc`, `CategoriaDaoJdbc`, `UsuarioDaoJdbc`, `OcorrenciaDaoJdbc`, `ApoioDaoJdbc` e `RelatorioDaoJdbc`. O `Main.java` liga essas classes aos services.
- O `dao` é o único pacote com SQL e com `java.sql`. Os services só conhecem as interfaces.
- As tabelas e a carga de exemplo vêm do `sql/avisa_ai.sql`. As consultas dos relatórios estão em `sql/relatorios.sql`.

A conexão vem de `ConnectionFactory.getConnection()`, que lê o `backend/db.properties` e devolve uma `Connection` com o Oracle. Se o arquivo ou o driver faltar, ou se o banco não responder, ela lança `DadosException`. Todos os DAOs usam assim, com try-with-resources e `PreparedStatement` com `?`:

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
| `inserir` | `Usuario` com nome, email, `getTipo()` e `bairro.id`, e `String senhaHash` | o usuário com `id` | Grava o hash em `SENHA_HASH`. E-mail repetido: `DUPLICADO` |
| `listar` | nada | `List<Usuario>` | Ordem alfabética de nome, com bairro (id e nome) |
| `buscarPorId` | `int id` | `Usuario` ou `null` | Com bairro (id e nome) |
| `buscarPorEmail` | `String email` | `Usuario` ou `null` | **Operação extra**, fora do CRUD. Comparação exata: o service já grava o e-mail em minúsculas. Usada também no login. |
| `atualizar` | `Usuario` com `id` | `boolean` | Altera nome, email, tipo e bairro. **Não mexe na senha.** E-mail repetido: `DUPLICADO` |
| `excluir` | `int id` | `boolean` | Os apoios do usuário saem junto. Usuário autor de ocorrências: `EM_USO` |
| `buscarSenhaHash` | `int id` | `String` ou `null` | **Operação extra**, para o login e a troca de senha conferirem a senha. Devolve o conteúdo de `SENHA_HASH`. |
| `atualizarSenhaHash` | `int id`, `String senhaHash` | `boolean` | **Operação extra**, troca de senha. `false` se o id não existe. |

**Senha:** o DAO só recebe e devolve o hash, já calculado pelo service (SHA-256 em hexadecimal minúsculo, 64 caracteres). Ele nunca vê a senha em texto. O hash fica fora do model `Usuario`, para nunca ir parar num JSON.

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
| `listarOcorrenciasApoiadas` | `int idUsuario` | `List<Integer>` | Ids das ocorrências que o usuário apoiou, em ordem crescente. Lista vazia se ele não apoiou nenhuma. Usado pela tela de ocorrências para mostrar "Apoiado" e "Retirar apoio". |

Não há `atualizar`: o apoio é só o par (usuário, ocorrência). Mudar um apoio é excluir e inserir de novo.

### RelatorioDao

As consultas são as do `sql/relatorios.sql`. O backend só precisa que cada linha chegue neste formato:

| Método | Linha devolvida | JSON gerado |
|---|---|---|
| `porBairro` | `LinhaRelatorio(nome, total)` | `{"nome":"Centro","total":3}` |
| `porCategoria` | `LinhaRelatorio(nome, total)` | idem |
| `porStatus` | `LinhaRelatorio(nome, total)` | `{"nome":"ABERTA","total":6}` |
| `rankingApoios` | `LinhaRanking(id, titulo, nomeDoBairro, status, totalApoios)` | `{"id":1,"titulo":"...","bairro":"Jardim das Flores","status":"ABERTA","totalApoios":5}` |

A ordem das linhas é a que a consulta devolver: bairro e categoria por total decrescente e depois por nome; ranking por total de apoios decrescente e depois pela data de abertura mais recente.

## Dados que o backend persiste

O backend guarda e lê estes dados. As tabelas e colunas estão definidas no `sql/avisa_ai.sql`.

| Entidade | Campos | Restrições que o backend espera do banco |
|---|---|---|
| Bairro | id (gerado), nome | nome único |
| Categoria | id (gerado), nome | nome único |
| Usuário | id (gerado), nome, email, hash da senha (`SENHA_HASH`, 64 caracteres), tipo (`CIDADAO`/`AGENTE`), bairro | email único; bairro existente; hash obrigatório |
| Ocorrência | id (gerado), titulo, descricao (opcional), endereco, status (`ABERTA`/`EM_ANDAMENTO`/`RESOLVIDA`), data de abertura, autor, categoria, bairro | chaves estrangeiras para usuário, categoria e bairro |
| Apoio | usuário, ocorrência, data | par usuário+ocorrência único; sai junto ao excluir a ocorrência ou o usuário |

Tamanhos máximos que o service já valida antes de gravar: nome de bairro e de categoria 80; nome de usuário 100; e-mail 120; título 120; descrição 500; endereço 200. Se alguma coluna mudar de tamanho, ajuste as constantes nos services.
