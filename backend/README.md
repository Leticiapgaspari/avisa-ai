# Avisa Aí: backend (Java)

API HTTP em Java puro: `HttpServer` do JDK, JSON sem biblioteca externa. Roda na porta 8080.

> **Persistência provisória:** por enquanto os dados ficam **em memória**, com a mesma carga de exemplo do script do banco. Eles se perdem quando o servidor para. A integração com o Oracle está descrita em [`docs/contrato-persistencia.md`](../docs/contrato-persistencia.md).

## Como rodar

Pré-requisito: JDK 11 ou mais novo (`java -version`).

### 1. Driver do Oracle

Baixe o [ojdbc11.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.5.0.24.07/ojdbc11-23.5.0.24.07.jar), renomeie para `ojdbc11.jar` e coloque em `backend/lib/`. Arquivos `.jar` não vão para o repositório.

### 2. Credenciais do banco

Copie `db.properties.example` para `db.properties` e preencha com seu usuário e senha. O `db.properties` está no `.gitignore`.

```properties
db.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
db.user=rmSEU_RM
db.password=sua_senha
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
├── Main.java        monta DAOs -> services -> handlers e sobe o servidor
├── model/           Usuario (abstrata), Cidadao, Agente, Bairro, Categoria, Ocorrencia, Apoio, StatusOcorrencia
├── dto/             dados de entrada (OcorrenciaDados, UsuarioDados) e linhas de relatório
├── dao/             interfaces de persistência (contratos) + ErroBanco
│   └── memoria/     PROVISÓRIO: implementação em memória dos contratos
├── service/         regras de negócio e validações
├── api/             um handler por rota, CORS, tradução de erros em JSON
├── exception/       RegraNegocioException, NaoEncontradoException, DadosException
└── util/            Json (escrita e leitura simples) e JsonSerializavel
```

Cada camada só chama a seguinte: `api` → `service` → `dao`. O handler não tem regra e o service não tem SQL.

## Rotas

O parâmetro `usuario` (ou o campo `idUsuario`) diz quem está agindo, já que não há login.

| Método e rota | Corpo / parâmetros | Sucesso |
|---|---|---|
| `GET /ocorrencias` | opcionais `?bairro=2&status=ABERTA` | 200, lista |
| `GET /ocorrencias?id=5` | | 200, ocorrência |
| `POST /ocorrencias` | `titulo, descricao, endereco, idCategoria, idBairro, idUsuario` | 201, ocorrência criada |
| `PUT /ocorrencias?id=5&usuario=2` | `titulo, descricao, endereco, idCategoria, idBairro` | 200, ocorrência |
| `PUT /ocorrencias/status?id=5&usuario=7` | `status` | 200, ocorrência |
| `DELETE /ocorrencias?id=5&usuario=2` | | 200, `{"mensagem":...}` |
| `GET /usuarios`, `GET /usuarios?id=3` | | 200 |
| `POST /usuarios` | `nome, email, tipo, idBairro` | 201 |
| `PUT /usuarios?id=3` | `nome, email, tipo, idBairro` | 200 |
| `DELETE /usuarios?id=3` | | 200 |
| `GET/POST/PUT/DELETE /bairros` | `nome`; `?id=` para buscar, alterar e excluir | 200 / 201 |
| `GET/POST/PUT/DELETE /categorias` | `nome`; `?id=` para buscar, alterar e excluir | 200 / 201 |
| `POST /apoios` | `idUsuario, idOcorrencia` | 201, `{"mensagem":...}` |
| `DELETE /apoios?usuario=1&ocorrencia=5` | | 200 |
| `GET /relatorios?tipo=bairro` | `tipo`: `bairro`, `categoria`, `ranking` ou `status` | 200, lista |

Todo erro volta como `{"erro":"mensagem"}`:

| Situação | Status |
|---|---|
| Regra violada, campo inválido, JSON malformado, valor duplicado, exclusão de registro em uso | 400 |
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

Na carga de exemplo, os usuários 1 a 6 são cidadãos e os usuários 7 e 8 são agentes.
