# Verificação final: Avisa Aí × requisitos da Global Solution 2026/2

> Documento interno do grupo. **Não vai no .zip da entrega.**
> Verificação feita em 16/11/2026. Nenhum arquivo do projeto foi alterado. O `backend/db.properties` não foi aberto.

## Resumo

| Bloco | Estimativa | Por quê |
|---|---|---|
| Java | **6,25 / 7** | Diagrama de classes desatualizado no `Apoio` (−0,25). Apoio sem leitura exposta e sem justificativa escrita para não ter update (−0,5). |
| Banco | **4 / 4** | ER bate com o script. Script consistente e já rodado com sucesso no Oracle da FIAP. |
| Web | **3,75 / 4** | Navegação e responsivo ok. A tela não consegue mostrar "você já apoiou" depois de recarregar a página (−0,25). |
| **Total** | **14 / 15** | O pitch fica fora da estimativa. |

Fora da rubrica, o que **mais pode tirar ponto**:

### Pendências, em ordem de impacto na nota

1. ❌ **Falta o `.txt`** com nome do grupo, nome e RM dos integrantes e link do pitch. É item obrigatório do enunciado ("entrega fora do padrão").
2. ❌ **Falta a seção de créditos** no README: Bootstrap 5.3.3 (MIT), fontes Barlow (SIL OFL 1.1) e driver Oracle ojdbc17 (licença Oracle FUTC), cada um com a licença. Pode ser lido como cópia de terceiros sem crédito.
3. ⚠️ **Três tabelas do README estão quebradas** (sem a linha `|---|`): Contas de teste (linha 14), Banco de dados (linha 47) e Integrantes (linha 117). Na de Integrantes, as linhas também não começam com `|`. No GitHub elas aparecem como texto corrido.
4. ⚠️ **README com campos vazios:** o link do vídeo pitch (linha 126) e o comentário "Completar: usos de IA fora dessas sessões..." (linha 113), que ainda precisa ser preenchido ou apagado.
5. ⚠️ **Diagrama de classes desatualizado:** o `Apoio` ainda aparece com `+toJson()` e ligado a `JsonSerializavel`, mas isso saiu do código.
6. ⚠️ **Apoio sem leitura e sem justificativa escrita para não ter update.** Veja a seção A.3.
7. ⚠️ **No .zip, não incluir o `backend/db.properties`** (tem as credenciais de vocês), nem o `backend/out/`, o `docs/revisao.md` e este arquivo. O `.gitignore` protege o GitHub, mas não um .zip feito à mão.
8. ⚠️ **README da raiz não diz como rodar o script:** "rode o `sql/avisa_ai.sql` no Oracle" não avisa que, no SQL Developer, é preciso usar **F5 (Executar script)**. Com F9, roda só um comando e o bloco de DROP falha.
9. ⚠️ **Não foi possível conferir a conexão hoje:** o `db.properties` local está incompleto (o servidor diz: "O arquivo db.properties precisa ter db.url, db.user e db.password preenchidos"). A última conexão OK com o Oracle foi em 13/11. Antes de gravar o pitch, preencham o arquivo e rodem tudo de novo.

---

## A. Rubrica

### Java (7)

#### A.1 Diagrama de classes UML (1): ⚠️ atende em parte

- `docs/diagrama-classes.png` existe e mostra as classes do pacote `model`: `Usuario` (abstrata), `Cidadao`, `Agente`, `Bairro`, `Categoria`, `Ocorrencia`, `Apoio`, o enum `StatusOcorrencia` e a interface `JsonSerializavel`.
- **Bate:**
  - Atributos privados de todas as classes, por exemplo `Usuario.java:11-14` e `Ocorrencia.java:10-19`.
  - Herança `Usuario` → `Cidadao`/`Agente` (`Cidadao.java:3`, `Agente.java:3`).
  - Os 5 métodos abstratos, incluindo `podeGerenciarCadastros()` (`Usuario.java:30-39`).
  - `criar()`, `ehAutorDe()`, `toJson()` e `toJsonResumo()`.
  - As associações: mora em, autor, bairro, categoria, status, quem apoia e ocorrência do apoio.
- **Não bate:** o `Apoio` aparece com `+toJson() : String` e realizando `JsonSerializavel`. No código, `Apoio.java:5` é `public class Apoio`, sem interface e sem `toJson`.
- **Falta:** tirar `toJson()` e a seta para `JsonSerializavel` do `Apoio` no diagrama.

#### A.2 POO de verdade (1): ✅ atende

| Conceito | Onde |
|---|---|
| Encapsulamento | Todos os atributos dos models são `private`, com getters e setters (`Usuario.java:11-14`, `Ocorrencia.java:10-19`, `Bairro.java:8-9`, `Apoio.java:7-9`). |
| Herança | `Cidadao extends Usuario` (`Cidadao.java:3`), `Agente extends Usuario` (`Agente.java:3`), os 10 handlers `extends HandlerBase` e as 4 exceções `extends RuntimeException`. |
| Polimorfismo | `getTipo()`, `podeApoiar()`, `podeMudarStatus()`, `podeEditar()` e `podeGerenciarCadastros()` sobrescritos em `Cidadao` e `Agente`. São usados sem `if` de tipo em `ApoioService.java:44`, `OcorrenciaService.java:76,90,103`, `Permissao.java:28` e `UsuarioService.java:90,125`. Os handlers também sobrescrevem `get`/`post`/`put`/`delete` da `HandlerBase`. |
| Classe abstrata | `Usuario` (`Usuario.java:6`) e `HandlerBase` (`HandlerBase.java:16`). |
| Interface | `JsonSerializavel` (implementada por `Usuario`, `Ocorrencia`, `Bairro`, `Categoria`, `LinhaRanking` e `LinhaRelatorio`) e os 6 contratos `BairroDao`, `CategoriaDao`, `UsuarioDao`, `OcorrenciaDao`, `ApoioDao` e `RelatorioDao`, implementados pelos `*DaoJdbc`. |

#### A.3 CRUD completo via JDBC (3): ⚠️ atende em parte

| Entidade | Create | Read | Update | Delete |
|---|---|---|---|---|
| Bairro | ✅ `inserir` · `POST /bairros` · `gestao.js` `enviar` | ✅ `listar`/`buscarPorId` · `GET /bairros` · `gestao.js` `carregar`, `login.js`, `perfil.js` | ✅ `atualizar` · `PUT /bairros` · `gestao.js` `enviar` | ✅ `excluir` · `DELETE /bairros` · `gestao.js` `excluir` |
| Categoria | ✅ `inserir` · `POST /categorias` · `gestao.js` `enviar` | ✅ `listar`/`buscarPorId` · `GET /categorias` · `gestao.js` `carregar`, `ocorrencias.js` | ✅ `atualizar` · `PUT /categorias` · `gestao.js` `enviar` | ✅ `excluir` · `DELETE /categorias` · `gestao.js` `excluir` |
| Usuário | ✅ `inserir` · `POST /usuarios` · `login.js` `enviarCadastro`, `usuarios.js` `enviarFormulario` | ✅ `listar`/`buscarPorId`/`buscarPorEmail` · `GET /usuarios` · `usuarios.js` `carregarUsuarios`, `perfil.js` `carregarDados` | ✅ `atualizar` (+ `atualizarSenhaHash`) · `PUT /usuarios`, `PUT /usuarios/senha` · `usuarios.js`, `perfil.js` `salvarDados`/`trocarSenha` | ✅ `excluir` · `DELETE /usuarios` · `usuarios.js` `excluirUsuario` |
| Ocorrência | ✅ `inserir` · `POST /ocorrencias` · `ocorrencias.js` `enviarFormulario` | ✅ `listar`/`buscarPorId` · `GET /ocorrencias` · `ocorrencias.js` `carregarOcorrencias`, `inicio.js`, `perfil.js` | ✅ `atualizar`/`atualizarStatus` · `PUT /ocorrencias`, `PUT /ocorrencias/status` · `ocorrencias.js` `enviarFormulario`/`mudarStatus` | ✅ `excluir` · `DELETE /ocorrencias` · `ocorrencias.js` `confirmarExclusao` |
| Apoio | ✅ `inserir` · `POST /apoios` · `ocorrencias.js` `apoiar` | ⚠️ só `existe()`, usado dentro do `ApoioService`. **Não há rota de leitura**: o total aparece pelo `totalApoios` da ocorrência e pelo ranking, mas a tela não sabe quais ocorrências o usuário já apoiou (usa `apoiadasNestaSessao`, que zera ao recarregar). | — (não se aplica) | ✅ `excluir` · `DELETE /apoios` · `ocorrencias.js` `retirarApoio` |

- **Apoio sem update:** faz sentido, porque um apoio é só o par (usuário, ocorrência). Para "mudar" um apoio, a pessoa retira e apoia de novo. Mas essa justificativa **não está escrita** em nenhum documento.
- **Falta:**
  1. Uma linha no `backend/README.md` explicando por que o apoio não tem update.
  2. Uma leitura dos apoios do usuário (por exemplo `GET /apoios?usuario=`), para a tela mostrar "Apoiado" e "Retirar apoio" mesmo depois de recarregar a página.

#### A.4 Camadas separadas (1): ✅ atende

```
$ grep -rln "import java.sql" backend/src | grep -v "/dao/"
(nenhum resultado)

$ grep -rl "import java.sql" backend/src/.../dao
ApoioDaoJdbc BairroDaoJdbc CategoriaDaoJdbc ConnectionFactory ErroBanco OcorrenciaDaoJdbc RelatorioDaoJdbc UsuarioDaoJdbc

$ grep -rnE "SELECT |INSERT |UPDATE |DELETE FROM" backend/src/.../service backend/src/.../api
(nenhum resultado)

$ grep -nE "\bif \(|throw new" backend/src/.../api/*Handler.java
BairroHandler.java:18, CategoriaHandler.java:18, OcorrenciaHandler.java:19, UsuarioHandler.java:20:  if (id != null) {
```

Os únicos `if` dos handlers escolhem entre "buscar um" e "listar" (`?id=`). Não são regra de negócio.

#### A.5 Tratamento de erros (1): ✅ atende

- **try-with-resources:** toda conexão abre dentro de `try (Connection con = ConnectionFactory.getConnection(); ...)`. São 29 de 29 nos `*DaoJdbc`, mais o `ConnectionFactory.java:47`. Nenhum `printStackTrace` nos DAOs.
- **ORA-xxxxx traduzidos:** `ErroBanco.java:15-24` transforma ORA-00001 em `DUPLICADO` (400), ORA-02292 em `EM_USO` (400) e o resto em `INDISPONIVEL` (500). São 29 `throw ErroBanco.traduzir(e)`.
- **Exceções próprias com status coerente:** `RegraNegocioException` dá 400, `NaoAutenticadoException` 401, `NaoEncontradoException` 404 e `DadosException` 400 ou 500 (`HandlerBase.java:84-99`).
- **Mensagens claras na tela:** `chamarApi` (`api.js`) lança o `{"erro": ...}` do back como `Error`, e cada tela mostra o texto no formulário ou na linha. Servidor fora do ar aparece numa faixa no topo, testado nas 7 telas em 13/11.

### Banco (4)

#### A.6 Diagrama ER (2): ✅ atende

`docs/diagrama-er.png` × `sql/avisa_ai.sql`:

- **Tabelas e colunas:** as 5 tabelas, as colunas, os tipos e os tamanhos batem, inclusive `SENHA_HASH VARCHAR2(64)`.
- **Chaves:** PKs com identity e PK composta no `APOIO`. FKs e UKs (`NOME` em Bairro e Categoria, `EMAIL` em Usuário).
- **CHECK** de `TIPO` e `STATUS`, `ON DELETE CASCADE` nas FKs do `APOIO` e os defaults `ABERTA` e `SYSDATE`.
- **Cardinalidades:** 1:N bairro → usuário, bairro → ocorrência, categoria → ocorrência e usuário → ocorrência. N:N usuário ↔ ocorrência pelo `APOIO`.
- Os nomes das restrições (`AV_...`) não aparecem no diagrama, o que é normal num ER.

#### A.7 Script .sql (2): ✅ atende

- **DROP tolerante:** o bloco PL/SQL (`avisa_ai.sql:2-14`) percorre `APOIO, OCORRENCIA, USUARIO, CATEGORIA, BAIRRO`, das filhas para as mães, e ignora só o erro -942, de tabela inexistente (linha 10).
- **CREATE na ordem das FKs:** `BAIRRO → CATEGORIA → USUARIO → OCORRENCIA → APOIO`.
- **INSERTs que respeitam as constraints**, considerando que os ids `IDENTITY` recomeçam em 1 a cada execução (as tabelas são recriadas):
  - **Quantidades:** 5 bairros, 6 categorias, 8 usuários, 12 ocorrências e 24 apoios.
  - **Usuários** usam bairros 1 a 5. **Ocorrências** usam usuários 1 a 7, categorias 1 a 6 e bairros 1 a 5, todos existentes.
  - **Apoios:** usam usuários 1 a 6 e ocorrências 1 a 11, sem par repetido (`uniq -d` = 0). Nenhum morador apoia a própria ocorrência, e nenhum agente apoia.
  - **Hash e restrições:** todo `SENHA_HASH` tem 64 caracteres. `TIPO` e `STATUS` usam só valores aceitos pelo CHECK.
- **COMMIT no final:** `avisa_ai.sql:194`.
- **Rodar duas vezes:** pela leitura do script, funciona (o DROP apaga tudo e o CREATE recria). Na prática, o script foi rodado com sucesso no Oracle da FIAP depois da correção do prefixo `AV_`: em 13/11, as telas mostraram 12 ocorrências, 8 usuários, 6 categorias e 5 bairros. Hoje não deu para rodar de novo, porque o `db.properties` está incompleto.

### Web (4)

#### A.8 Navegação (1): ✅ atende

| Origem | Destinos |
|---|---|
| `index.html` | `index.html`, `login.html#entrar`, `login.html#cadastro` |
| `login.html` | `index.html`. Depois de entrar, `login.js:171` vai para `painel.html`; quem já está logado, `login.js:8` manda para `painel.html` |
| As 6 páginas internas (menu) | `painel.html`, `ocorrencias.html`, `ocorrencias.html#registrar`, `relatorios.html`, `usuarios.html`, `gestao.html`, `perfil.html`, `perfil.html#seus-avisos` |
| `sessao.js` | `login.html#entrar` (sem login, linha 59), `painel.html` (morador em página de agente, linha 63), `index.html` (Sair, linha 210) e a barra do celular (linhas 253-269) |
| `inicio.js` | `ocorrencias.html?status=ABERTA`, `ocorrencias.html?status=<status>`, `ocorrencias.html?bairro=<id>`, `ocorrencias.html#registrar`, `ocorrencias.html`, `relatorios.html` |
| `perfil.js` | `ocorrencias.html#registrar`, `ocorrencias.html` |

- **Destinos:** as 8 páginas existem.
- **Âncoras:** `#entrar` e `#cadastro` são tratadas em `login.js:57`, `#registrar` em `ocorrencias.js:580`, e `#seus-avisos` existe no `perfil.html`. Os parâmetros `?status=` e `?bairro=` são lidos em `ocorrencias.js`.
- Nenhum link quebrado.

#### A.9 Telas do CRUD (2): ✅ atende

| Operação | Tela e função |
|---|---|
| Bairro e Categoria: criar, listar, editar, excluir | `gestao.html` · `gestao.js`: `enviar` (POST/PUT), `carregar` (GET), `excluir` (DELETE) |
| Usuário: criar | `login.html` · `login.js` `enviarCadastro` (cadastro público); `usuarios.html` · `usuarios.js` `enviarFormulario` (agente) |
| Usuário: listar, ver | `usuarios.js` `carregarUsuarios`; `perfil.js` `carregarDados`; `sessao.js` `carregarEmailDaConta` |
| Usuário: editar | `usuarios.js` `enviarFormulario`; `perfil.js` `salvarDados` e `trocarSenha` |
| Usuário: excluir | `usuarios.js` `excluirUsuario` |
| Ocorrência: criar, editar | `ocorrencias.html` · `ocorrencias.js` `enviarFormulario` |
| Ocorrência: listar | `ocorrencias.js` `carregarOcorrencias`; `inicio.js` (painel); `perfil.js` `carregarMeusAvisos` |
| Ocorrência: mudar status, excluir | `ocorrencias.js` `mudarStatus`, `confirmarExclusao` |
| Apoio: criar, excluir | `ocorrencias.js` `apoiar`, `retirarApoio` |
| Relatórios | `relatorios.html` · `relatorios.js` `buscarRelatorio`; `inicio.js` |

#### A.10 Responsivo e bem desenhado (1): ✅ atende

- `<meta name="viewport" content="width=device-width, initial-scale=1">` nas 8 páginas.
- 22 `@media` no `style.css`, escrito pensando primeiro no celular, com cortes em 420, 480, 768, 880, 1024 e 1152 px, além de `prefers-reduced-motion`.
- Nenhuma largura fixa maior que 390 px: as maiores são `width: 260px` (painel do menu da conta, linha 3414), `min-width: 160px` e `min-width: 150px`.
- Testado em tela de 390 px no Chrome em 13/11, com prints da Gestão, da janela de confirmação e do aviso acima da barra inferior.

---

## B. Checklist do enunciado

| Item | | Evidência |
|---|---|---|
| Compila e roda seguindo só o README | ⚠️ | O comando de PowerShell do `backend/README.md` compila (`javac exit=0`, 59 classes), e o `lib/ojdbc17.jar` está no lugar. **Passos ambíguos:** (1) o README da raiz não diz como rodar o script, e no SQL Developer é preciso F5, não F9; (2) o README da raiz não cita o JDK 17, só o `backend/README.md` cita. Não deu para rodar contra o banco hoje: o `db.properties` local está incompleto. |
| `index.html` abre direto do disco, sem internet | ✅ | O único `http` em HTML, CSS e JS fora do `vendor` é `api.js:10` → `http://localhost:8080` (a API local). Nenhum `<script>` ou `<link>` externo, nenhum `@import`, nenhuma fonte remota: as 6 fontes `.woff2` estão em `frontend/fonts`, e o Bootstrap está em `css/` e `js/vendor/`. |
| Diagramas em imagem ou PDF | ✅ | `docs/diagrama-classes.png` (1,8 MB) e `docs/diagrama-er.png` (1,4 MB). |
| `.txt` com grupo, RMs, nomes e link do pitch | ❌ | `find . -name "*.txt"` não encontrou nenhum arquivo. **Falta criar**, por exemplo `integrantes.txt` na raiz. |

## C. O que tira pontos

| Item | | Evidência |
|---|---|---|
| Entrega no padrão (README, .sql, 2 diagramas, .txt) | ⚠️ | O README, o `sql/avisa_ai.sql`, o `sql/relatorios.sql` e os dois PNG existem. **Falta o `.txt`.** |
| Tecnologia fora do conteúdo | ✅ | Back: Java SE (`com.sun.net.httpserver` com `HttpServer`, `HttpHandler`, `HttpExchange` e `Headers`, mais `java.sql`/JDBC), sem nenhum outro import externo. Driver: `ojdbc17.jar`, necessário para falar com o Oracle. Front: HTML, CSS e JS puros mais o Bootstrap 5.3.3 (só Modal e Toast). Fontes Barlow são arquivos de fonte, não biblioteca. O HttpServer e o Bootstrap foram autorizados; **vale confirmar que o professor considera o ojdbc parte do conteúdo** (normalmente sim, porque é o driver oficial do banco da disciplina). |
| Não compila, não conecta ou .sql com erro | ⚠️ | Compila. O `.sql` está consistente e já rodou no Oracle. **Conexão não conferida hoje** por causa do `db.properties` local incompleto; a última conexão OK foi em 13/11. |
| Cópia de terceiros sem crédito | ❌ | O README não tem seção de créditos. O Bootstrap só é citado como funcionalidade (`README.md:28`), sem licença. As fontes Barlow aparecem sem licença no README (só num comentário do `style.css:4`). O ojdbc aparece só como arquivo (`README.md:64`). **Falta** uma seção "Créditos" com: Bootstrap 5.3.3 (MIT, cabeçalho mantido em `css/bootstrap-componentes.css` e `js/vendor/bootstrap.min.js`), Barlow e Barlow Condensed (SIL Open Font License 1.1) e Oracle JDBC ojdbc17 (Oracle Free Use Terms and Conditions). |
| Uso de IA declarado | ⚠️ | A seção existe (`README.md:85`) e lista ferramenta, período e o que foi feito em cada área. Ainda tem o comentário "Completar..." na linha 113, para preencher ou apagar. |

## D. Higiene da entrega

| Item | | Evidência |
|---|---|---|
| Nenhuma credencial no código, nos READMEs ou no histórico | ✅ | `git log --all -p` com "password": 12 ocorrências, todas sem valor, menos uma no commit `c97567c`, em `backend/README.md`, que é o texto de exemplo `sua_senha`. O `backend/db.properties` nunca foi commitado (`git log --all -- backend/db.properties` vazio). Na árvore atual, nenhum `db.password=` ou `db.user=rm...` preenchido fora do `db.properties`. |
| `.gitignore` cobre db.properties, out/ e *.class | ✅ | `.gitignore:2` `*.class`, `:27` `backend/out/` e `:28` `backend/db.properties`, confirmado com `git check-ignore`. |
| Sobras | ✅ | Nenhum arquivo vazio. Nenhum TODO, FIXME, `console.log` ou `debugger` (o "TODO" que aparece no grep é o nome `MSG_METODO`). Nenhum código comentado. Todas as imagens, fontes, CSS e JS são usados por alguma página. Só ficam o `docs/revisao.md` e este arquivo, que são internos e não vão no .zip. |
| README sem campo vazio, tabelas válidas, links internos ok | ⚠️ | Os links internos estão todos ok (`backend/README.md`, os dois PNG, os dois `.sql` e o `contrato-persistencia.md`). **Problemas:** as tabelas das linhas 14, 47 e 117 estão sem separador `\|---\|`, e a de Integrantes também está sem os `\|` nas pontas; o link do pitch está vazio (linha 126); e o comentário "Completar..." continua na linha 113. A tabela de rotas do `backend/README.md` está válida. |

---

## O que não deu para verificar hoje

- Rodar o `sql/avisa_ai.sql` duas vezes seguidas e subir o back contra o Oracle: o `backend/db.properties` local está sem algum dos campos, e ele não foi aberto. Depois de preenchê-lo, refaçam o roteiro:
  1. Rodar o script duas vezes (F5).
  2. `SELECT COUNT(*)` em cada tabela: 5, 6, 8, 12 e 24.
  3. Subir o back e ver "Conexão com o Oracle: OK.".
  4. Entrar como morador e como agente e passar por cada tela.
