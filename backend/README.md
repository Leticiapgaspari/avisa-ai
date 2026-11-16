# Avisa Aí: backend (Java)

API HTTP em Java puro: `HttpServer` do JDK, JSON sem biblioteca externa. Roda na porta 8080.

Os dados ficam no **Oracle**, acessado por JDBC: cada contrato do pacote `dao` tem uma implementação `*DaoJdbc`. O que cada DAO faz está em [`docs/contrato-persistencia.md`](../docs/contrato-persistencia.md). Antes de subir o servidor, rode o `sql/avisa_ai.sql` no banco.

## Como rodar

Pré-requisito: **JDK 17 ou mais novo** (`java -version`). O driver do Oracle usado exige Java 17.

### 1. Driver do Oracle

O projeto usa o `ojdbc17.jar` em `backend/lib/`. **No .zip da entrega, o jar já vem na pasta `lib`**, e não é preciso baixar nada.

Clonando do GitHub (arquivos `.jar` não vão para o repositório), baixe o [ojdbc17 23.26.4.0.0 no Maven Central](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc17/23.26.4.0.0/ojdbc17-23.26.4.0.0.jar), renomeie para `ojdbc17.jar` e coloque em `backend/lib/`.

### 2. Credenciais do banco

Copie `db.properties.example` para `db.properties` e preencha com seu usuário e senha do Oracle. O `db.properties` está no `.gitignore`.

```properties
db.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
db.user=
db.password=
```

### 3. Compilar e rodar

Dentro da pasta `backend/`:

**PowerShell (Windows)**

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;lib/*" br.com.avisaai.Main
```

**Linux ou macOS** (no classpath, troque `;` por `:`)

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp "out:lib/*" br.com.avisaai.Main
```

No Git Bash do Windows, use os comandos de Linux, mas mantenha o `;` do PowerShell: `java -cp "out;lib/*" br.com.avisaai.Main`.

Ao subir, o console mostra `Conexão com o Oracle: OK.` ou o motivo da falha. A API sobe mesmo sem banco.

Abra http://localhost:8080/ocorrencias para conferir. Para usar outra porta, passe o número no final: `java -cp "out;lib/*" br.com.avisaai.Main 8081`.

## Pacotes

```
src/br/com/avisaai/
├── Main.java        monta DAOs JDBC -> services -> handlers e sobe o servidor
├── model/           Usuario (abstrata), Cidadao, Agente, Bairro, Categoria, Ocorrencia, Apoio, StatusOcorrencia
├── dto/             OcorrenciaDados, UsuarioDados (entrada) e LinhaRelatorio, LinhaRanking (relatórios)
├── dao/             contratos: BairroDao, CategoriaDao, UsuarioDao, OcorrenciaDao, ApoioDao, RelatorioDao
│                    JDBC/Oracle: BairroDaoJdbc, CategoriaDaoJdbc, UsuarioDaoJdbc, OcorrenciaDaoJdbc,
│                                 ApoioDaoJdbc, RelatorioDaoJdbc
│                    ConnectionFactory (lê o db.properties) e ErroBanco (traduz ORA-xxxxx)
├── service/         BairroService, CategoriaService, UsuarioService, OcorrenciaService, ApoioService,
│                    RelatorioService (regras de negócio)
│                    Permissao (quem está agindo), Senha (hash SHA-256), Validacao
├── api/             Servidor, HandlerBase, Requisicao, Resposta
│                    um handler por rota: RaizHandler, LoginHandler, UsuarioHandler, UsuarioSenhaHandler,
│                    BairroHandler, CategoriaHandler, OcorrenciaHandler, OcorrenciaStatusHandler,
│                    ApoioHandler, RelatorioHandler
├── exception/       RegraNegocioException (400), NaoAutenticadoException (401),
│                    NaoEncontradoException (404), DadosException (erros do banco)
└── util/            Json (escrita e leitura simples) e JsonSerializavel
```

Cada camada só chama a seguinte: `api` → `service` → `dao`. O handler não tem regra, o service não tem SQL e só o pacote `dao` usa `java.sql`.

## Rotas

O parâmetro `usuario` (ou o campo `idUsuario`) diz quem está agindo: o front manda o id de quem fez login.

> **Nota:** o back confia no `?usuario` enviado pelo front. Num sistema real, o servidor geraria um token de sessão no login e descobriria quem está agindo por ele, sem aceitar um id vindo do navegador.

A senha é guardada só como hash SHA-256 (coluna `SENHA_HASH`) e nunca aparece em resposta nenhuma. Mínimo de 6 caracteres.

| Método e rota | Corpo / parâmetros | Quem pode | Sucesso |
|---|---|---|---|
| `POST /login` | `email, senha` | qualquer um | 200, usuário |
| `GET /ocorrencias` | opcionais `?bairro=2&status=ABERTA` | qualquer um | 200, lista |
| `GET /ocorrencias?id=5` | | qualquer um | 200, ocorrência |
| `POST /ocorrencias` | `titulo, descricao, endereco, idCategoria, idBairro, idUsuario` | qualquer usuário | 201, ocorrência criada |
| `PUT /ocorrencias?id=5&usuario=2` | `titulo, descricao, endereco, idCategoria, idBairro` | o autor ou agente | 200, ocorrência |
| `PUT /ocorrencias/status?id=5&usuario=7` | `status` | agente | 200, ocorrência |
| `DELETE /ocorrencias?id=5&usuario=2` | | o autor ou agente | 200, `{"mensagem":...}` |
| `GET /usuarios?usuario=7` | | agente | 200, lista |
| `GET /usuarios?id=3&usuario=3` | | a própria conta ou agente | 200, usuário |
| `POST /usuarios` (sem `?usuario`) | `nome, email, idBairro, senha` | qualquer um (cadastro público; o tipo é sempre `CIDADAO`) | 201 |
| `POST /usuarios?usuario=7` | `nome, email, tipo, idBairro, senha` (senha inicial) | agente | 201 |
| `PUT /usuarios?id=3&usuario=3` | `nome, email, tipo, idBairro` | a própria conta (sem mudar o tipo) ou agente | 200 |
| `PUT /usuarios/senha?id=3&usuario=3` | `senhaAtual, novaSenha` | só a própria conta | 200, `{"mensagem":...}` |
| `DELETE /usuarios?id=3&usuario=7` | | agente, menos a própria conta | 200 |
| `GET /bairros`, `GET /bairros?id=1` | | qualquer um | 200 |
| `POST/PUT/DELETE /bairros?usuario=7` | `nome`; `?id=` para alterar e excluir | agente | 200 / 201 |
| `GET /categorias`, `GET /categorias?id=1` | | qualquer um | 200 |
| `POST/PUT/DELETE /categorias?usuario=7` | `nome`; `?id=` para alterar e excluir | agente | 200 / 201 |
| `GET /apoios?usuario=1` | | o próprio usuário | 200, ids das ocorrências que ele apoiou, ex.: `[3,7,11]` |
| `POST /apoios` | `idUsuario, idOcorrencia` | morador (agente não apoia), menos na própria ocorrência ou numa já resolvida | 201, `{"mensagem":...}` |
| `DELETE /apoios?usuario=1&ocorrencia=5` | | quem deu o apoio | 200 |
| `GET /relatorios?tipo=bairro` | `tipo`: `bairro`, `categoria`, `ranking` ou `status` | qualquer um | 200, lista |

O apoio não tem rota de alteração: ele é só o par (usuário, ocorrência), então mudar um apoio é retirar e apoiar de novo.

Todo erro volta como `{"erro":"mensagem"}`:

| Situação | Status |
|---|---|
| Regra violada, sem permissão, campo inválido, JSON malformado, valor duplicado, exclusão de registro em uso, `?usuario` ausente ou inexistente | 400 |
| Login com e-mail ou senha errados (`"E-mail ou senha incorretos."` nos dois casos) | 401 |
| Id inexistente | 404 |
| Método não aceito na rota | 405 |
| Banco inacessível ou erro inesperado (detalhe só no console) | 500 |

Toda resposta usa UTF-8 e leva os cabeçalhos de CORS. `OPTIONS` responde 204.

## Teste rápido

```bash
curl http://localhost:8080/ocorrencias?bairro=2
curl -X PUT "http://localhost:8080/ocorrencias/status?id=1&usuario=2" -d '{"status":"RESOLVIDA"}'
# {"erro":"Apenas agentes podem alterar o status."}
curl -X POST http://localhost:8080/apoios -d '{"idUsuario":2,"idOcorrencia":1}'
# {"erro":"Você não pode apoiar a própria ocorrência."}
```

Na carga de exemplo, os usuários 1 a 6 são cidadãos e os usuários 7 e 8 são agentes. Todos usam a senha `avisa123`.
