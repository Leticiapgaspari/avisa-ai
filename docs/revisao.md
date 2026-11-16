# Revisão de código: marcas de código gerado e não revisado

> Documento interno do grupo. **Não vai no .zip da entrega.**
> Revisão feita em 16/11/2026 sobre `backend/src`, `frontend/js`, `frontend/*.html`, `frontend/css/style.css`, `sql/` e os READMEs. Nenhum arquivo do projeto foi alterado; os números de linha valem para o estado de hoje.

## Resumo

- **Código morto:** está concentrado num lugar só, o modo "sem usuário" do front. Ele vem da época em que se escolhia o usuário numa lista. Hoje toda página que carrega o `sessao.js` exige login, então esse caminho nunca roda.
- **Inconsistência mais visível para o professor:** o back tem **três jeitos diferentes** de descobrir "quem está agindo", com status HTTP e mensagens diferentes.
- **Comentários:** quase todos explicam o porquê. Poucos são óbvios. Mas há uma diferença de estilo clara entre os arquivos antigos (quase sem comentário) e os novos (`Senha`, `Permissao`, `*DaoJdbc`, `componentes.js`), que têm mais comentários e outro jeito de escrever. Quem lê o projeto inteiro percebe.
- **Seção de IA:** a seção "Uso de IA generativa" **não existe** em nenhum `.md` do projeto. Se ela é obrigatória, ainda precisa ser escrita. Veja o fim deste documento.
- **Estudo:** a parte 7 lista os trechos que mais merecem atenção antes da apresentação, como o parser de JSON, o `? IS NULL`, o `MessageDigest.isEqual` e o bloco PL/SQL do DROP.

---

## 1. Comentários óbvios ou com cara de tutorial

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `sql/avisa_ai.sql` | 1, 16, 78 | `-- Drop Table`, `-- Create table`, `-- Insert`: repetem o comando logo abaixo, em inglês e com maiúsculas diferentes. | Trocar por um comentário que diga o porquê, por exemplo: `-- apaga as tabelas, se existirem, para o script poder rodar de novo`. |
| `frontend/login.html` | 15, 138 | `<!--section esquerda-->` e `<!--section direita-->` descrevem a tag, não o conteúdo. | `<!--formulários de entrar e de criar conta-->` e `<!--ilustração-->`, ou apagar. |
| `frontend/js/api.js` | 1 | `// Comunicação com o back-end e funções usadas`: "funções usadas" não diz nada. | `// Fala com o back (chamarApi) e funções de tela usadas por todas as páginas`. |
| `frontend/js/sessao.js` | 203 | `// marca a página atual` em cima de código que já diz isso. | Apagar, ou dizer o porquê (o `aria-current` faz o leitor de tela anunciar a página). |
| `frontend/js/sessao.js` | 41 | `// nada a fazer` num `catch` vazio. | Dizer o porquê: `// sessionStorage bloqueado (modo privado): não há o que limpar`. |
| `backend/.../service/Senha.java` | 35 | `// MessageDigest.isEqual compara em tempo constante.` Diz **o que** faz, mas não **por que** isso importa. | `// tempo constante: a resposta não demora mais quando os primeiros caracteres batem, o que dificulta adivinhar o hash por tentativa`. |
| `frontend/js/componentes.js` | 1-3, 43-45 | Comentários com cara de documentação de biblioteca ("O Bootstrap cuida do foco preso na janela..."). Estão corretos, mas o tom é diferente do resto do front. | Encurtar no estilo dos outros arquivos JS, que usam frases curtas e informais. |

## 2. Comentários e textos que não batem com o código

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `frontend/js/sessao.js` | 5 | `AVISO_SEM_USUARIO = "Escolha um usuário para participar"`. Esse texto é da versão sem login, em que se escolhia o usuário numa lista. Hoje não há onde "escolher um usuário". | Apagar junto com o código morto da parte 4. |
| `frontend/js/sessao.js` | 293 | `// sem login a pessoa só consulta`. Nenhuma página permite consultar sem login: `protegerPagina()` (linha 64) manda para o login antes. | Apagar junto com o bloco. |
| `frontend/js/ocorrencias.js` | 25 | O comentário diz que o cartão mostra `"Excluir mesmo?"`, mas o texto real é `"Excluir esta ocorrência?"` (linha 127). | Corrigir o comentário. |
| `backend/.../Main.java` | 89 | A dica de porta ocupada manda rodar `java -cp out br.com.avisaai.Main 8081`, **sem o `;lib/*`**. Seguindo a dica, o servidor sobe sem o driver do Oracle e todas as chamadas dão erro 500. | `java -cp "out;lib/*" br.com.avisaai.Main 8081`, igual ao README. |
| `backend/.../service/UsuarioService.java` | 159 | Título de seção `// ---------- apoio ----------` para os métodos auxiliares. Neste projeto, "apoio" é uma entidade (tabela `APOIO`), e o título confunde. | `// ---------- auxiliares ----------`. |
| `frontend/js/inicio.js` | 11 | Mesmo caso: `// ---------- apoio ----------` para funções auxiliares. | `// ---------- auxiliares ----------`. |
| `frontend/js/api.js` | 53 | `// funções de apoio`: mesma ambiguidade. | `// funções auxiliares`. |
| `sql/avisa_ai.sql` | 191 | Os comentários dos apoios pulam da "Ocorrencia 9" para a "Ocorrencia 11". As ocorrências 10 e 12 ficam sem apoio de propósito (o ranking precisa de casos com 0), mas isso não está escrito. | `-- 10 e 12 ficam sem apoio, para o ranking mostrar ocorrência com 0`. |
| `README.md` | — | O pedido de revisão cita uma seção "Uso de IA generativa", mas ela **não existe** em nenhum `.md` do projeto. | Escrever a seção. Veja a sugestão no fim deste documento. |

## 3. Inconsistências de estilo entre arquivos

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `service/Permissao.java` × `service/OcorrenciaService.java` × `service/ApoioService.java` | 15 × 139 × 38-40 | **Três jeitos de achar "quem está agindo".** `Permissao.quemAge` devolve **400** com "O usuário que está fazendo a operação não existe.". `OcorrenciaService.buscarUsuarioQueAge` devolve **404** com "Usuário não encontrado.". O `ApoioService` faz a busca direto, também com 404. | Usar `Permissao.quemAge` nos três. É a inconsistência mais fácil de o professor notar. |
| `api/OcorrenciaHandler.java` × `api/UsuarioHandler.java` | 28 × 28 | Quem age vem do **corpo** (`idUsuario`) em `POST /ocorrencias` e `POST /apoios`, mas da **URL** (`?usuario=`) em todas as outras rotas. | Se não der para mudar agora, pelo menos saber explicar: o `idUsuario` do corpo é o autor (um dado da ocorrência), e o `?usuario` é quem está agindo. |
| `service/BairroService.java` × `service/OcorrenciaService.java` | 39 × 59 | O mesmo parâmetro tem nomes diferentes: `idQuemAge` nos services de usuário, bairro e categoria, e `idUsuario` nos de ocorrência e apoio. | Escolher um nome só. |
| `service/Validacao.java` × `service/Permissao.java` | 7 × 10 | "...que está **realizando** a operação" × "...que está **fazendo** a operação". | Mesma palavra nas duas mensagens. |
| `api/ApoioHandler.java` | 17-18 | O 201 é montado com `Resposta.mensagem(...)` e depois `new Resposta(201, ok.getJson())`. Os outros handlers usam `Resposta.criado(...)`. | `return Resposta.criado(Json.campo("mensagem", "Apoio registrado com sucesso."));` |
| `api/UsuarioSenhaHandler.java` | 18 | `"Senha trocada."`, enquanto os outros handlers usam "X excluído **com sucesso**.". | Padronizar as mensagens de sucesso. |
| `service/CategoriaService.java` | 64 | Usa `BairroService.MSG_NOME_CURTO`, uma constante de outra classe, só porque o texto é igual. Isso cria uma dependência sem motivo. | Constante própria, ou mover para `Validacao`. |
| `dao/BairroDaoJdbc.java` × `dao/RelatorioDaoJdbc.java` | 18 × 29 | As listas ordenam com `NLSSORT(... BINARY_AI)`, que ignora acento e maiúscula, mas os relatórios ordenam com `ORDER BY NOME` simples. Em caso de empate, "Árvore e poda" pode aparecer em posições diferentes nas duas telas. | Usar o mesmo critério de ordem nos dois, ou saber explicar a diferença. |
| `frontend/js/ocorrencias.js` × `usuarios.js`/`gestao.js` | 122-131 | Excluir ocorrência confirma **na própria linha**. Excluir usuário, categoria ou bairro confirma numa **janela do Bootstrap**. | Usar a janela também em Ocorrências, ou explicar por que só ali é diferente. |
| `sql/avisa_ai.sql` | 1-78 | Os comentários do SQL estão em inglês ("Drop Table", "Insert"), e o resto do projeto está em português. | Comentários em português. |
| Nomes de classes Java | — | Mistura de inglês e português: `HandlerBase`, `ConnectionFactory`, `*DaoJdbc` ao lado de `Servidor`, `Requisicao`, `Resposta`, `Leitor`. O `HandlerBase` mistura as duas ordens. | Não precisa renomear. Vale saber justificar: os sufixos de padrão (`Dao`, `Handler`, `Factory`) ficaram em inglês, e o resto em português. |
| HTMLs | vários | Comentários HTML sem padrão: `<!--Todo mundo ve-->` e `<!--Ultima chamada-->` sem acento (index.html 70 e 126), `<!--texto e botões vem do inicio.js-->` (painel.html 67, deveria ser "vêm"), com e sem espaço depois de `<!--`. | Revisar acentos e escolher um formato. |
| Densidade de comentários | — | Os arquivos do primeiro commit (services, handlers, models) quase não têm comentário. `Senha.java`, `Permissao.java`, `*DaoJdbc.java`, `UsuarioDao.java` e `componentes.js` têm vários, com frases mais longas e mais "redondas". | É a marca mais clara de que partes diferentes foram escritas de jeitos diferentes. Equilibrar: comentar o porquê nos arquivos antigos e enxugar os novos. |

## 4. Código morto

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `frontend/js/sessao.js` | 53 | `ehCidadao()` não é chamada em lugar nenhum. | Apagar. |
| `frontend/js/sessao.js` | 294-303 | O bloco `if (!temUsuario())` do `aplicarPermissoes()` nunca roda: as páginas com `data-requer-usuario` são privadas e já redirecionaram. | Apagar o bloco e a constante `AVISO_SEM_USUARIO`. |
| `frontend/js/ocorrencias.js` | 105-111, 152-154, 388-391 | Três ramos `if (!temUsuario())` com botões desabilitados e a mensagem "Escolha um usuário". `ocorrencias.html` é `data-pagina="privada"`, então nunca acontecem. | Apagar os três. |
| `frontend/js/ocorrencias.js` | 80, 148 | `temUsuario() &&` dentro de condições que só rodam com usuário logado. | Tirar o `temUsuario() &&`. |
| `frontend/ocorrencias.html` | 78, 81 | `<p class="aviso-usuario" hidden>` e `data-requer-usuario` no fieldset só existem para o modo sem login. | Apagar junto com o JS. |
| `frontend/css/style.css` | 623 | `.aviso-usuario` só é usada pelo parágrafo acima. | Apagar do seletor. |
| `backend/.../model/Apoio.java` | 8, 14, 27-45, 48 | `implements JsonSerializavel` e `toJson()`: nenhum apoio é devolvido em JSON (as rotas de apoio devolvem só `{"mensagem":...}`). O construtor vazio e os 3 setters também não são usados. | Apagar o `toJson`, o `implements`, o construtor vazio e os setters. |
| `backend/.../dto/LinhaRelatorio.java` | 11, 27-33 | O construtor vazio, `getTotal()` e `setTotal()` não são usados. Só o construtor com argumentos e o `toJson()` são. | Apagar. |
| `backend/.../dto/LinhaRanking.java` | 14, 29+ | O construtor vazio e todos os getters e setters não são usados. | Apagar, ou manter e saber dizer que é o "padrão JavaBean". |
| `backend/.../model/Bairro.java` e `Categoria.java` | 11 | Construtor vazio `Bairro()` e `Categoria()`: ninguém faz `new Bairro()`. | Apagar. |

> Fora isso, a varredura de funções JS, métodos Java e classes CSS não achou mais nada sem uso. As três classes CSS que não aparecem escritas no HTML (`etiqueta-andamento`, `etiqueta-resolvida` e `modal-backdrop`) são montadas em tempo de execução (`"etiqueta-" + classe`) ou criadas pelo Bootstrap.

## 5. Generalização sem necessidade

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `backend/.../util/Json.java` | 85-236 | O leitor de JSON aceita escapes `\b`, `\f`, `\/`, `\uXXXX`, números com expoente (`1e5`) e `true`/`false`. O front só manda textos e números inteiros. São cerca de 150 linhas difíceis de explicar. | Manter (está correto e testado), mas saber explicar: veja a parte 7. Se o professor perguntar, a resposta honesta é "segue a especificação do JSON". |
| `backend/.../dao/ConnectionFactory.java` | 17 | Procura o `db.properties` em dois lugares (`db.properties` e `backend/db.properties`). | Tem uso real: funciona rodando o Java da pasta `backend` ou da raiz. Vale um comentário dizendo isso. |
| `backend/.../model/Usuario.java` | 20 | `Usuario.criar` faz `trim().toUpperCase()` no tipo. O banco e o front sempre mandam em maiúsculas. | Pode ficar, porque deixa a API tolerante a "agente". Saber explicar. |
| `backend/.../service/RelatorioService.java` | 20 | Mesmo caso: `trim().toLowerCase()` no `?tipo=`. | Idem. |
| `backend/.../service/UsuarioService.java` | 118 + 122 | O e-mail repetido é checado **duas vezes**: antes (`verificarEmailLivre`) e depois (pega o `DUPLICADO` do banco). | Tem motivo: duas pessoas se cadastrando ao mesmo tempo passam as duas pela primeira checagem, e a restrição `UNIQUE` do banco é a garantia final. Saber explicar. |
| `backend/.../api/Servidor.java` | 18 | `Executors.newFixedThreadPool(8)`: 8 requisições em paralelo. | Pode ficar. O número 8 é arbitrário; saber dizer isso. |
| `frontend/js/componentes.js` | 46-52 | Trata o caso de a pessoa confirmar **durante** a animação de abertura da janela. | Tem motivo real: sem isso a janela travava, e o teste pegou esse bug. Manter. |

## 6. Textos com cara de marketing genérico

O texto das telas está, no geral, concreto e no tom do grupo ("Viu um problema na rua? Avisa aí.", "Todo mundo vê. Quase ninguém avisa."). Pontos para olhar:

| Arquivo | Linha | O que foi encontrado | Sugestão |
|---|---|---|---|
| `frontend/index.html` | 78-79 | "**119,9 milhões** de brasileiros moram em ruas sem rampa para cadeirante. IBGE, Censo 2022". É um número forte, e o professor pode perguntar a fonte. | Conferir o número na publicação do IBGE (Censo 2022, características do entorno dos domicílios) e guardar o link. Se não achar exatamente esse número, trocar. |
| `frontend/index.html` | 146 | "Alinhado ao ODS 11 da ONU, que pede cidades mais seguras e mais bem cuidadas, com a participação de quem mora nelas." O tom é de relatório institucional, diferente do resto da página. | Algo mais direto: "Ligado ao ODS 11 da ONU: cidades mais seguras, cuidadas por quem mora nelas." |
| `frontend/index.html` | 111 | "Feito para os dois lados da rua": é slogan, mas combina com a marca. | Pode ficar. |
| `README.md` | 29 | O item sobre o Bootstrap é longo e justifica demais ("só onde ele ajuda", "para o resto do visual continuar sendo o nosso"). | Encurtar para 1 ou 2 linhas: o que usamos, onde e por quê. |
| `README.md` | 37 | "Camadas com uma responsabilidade cada" é uma frase de livro. | "Cada pacote faz uma coisa: ..." |

## 7. Trechos para estudar

Cada trecho tem uma explicação curta. Vale cada pessoa do grupo conseguir explicar pelo menos alguns.

| Arquivo | Linha | Trecho | O que faz |
|---|---|---|---|
| `service/Senha.java` | 18-23 | `MessageDigest.getInstance("SHA-256")` e `String.format("%02x", b)` | Calcula o hash SHA-256 da senha, que são 32 bytes. Cada byte vira 2 caracteres hexadecimais (`%02x` = hexa minúsculo com 2 dígitos, completando com 0), e o resultado tem 64 caracteres, o tamanho da coluna `SENHA_HASH`. |
| `service/Senha.java` | 36-38 | `MessageDigest.isEqual(...)` | Compara os dois hashes sempre no mesmo tempo, mesmo quando já diferem no primeiro caractere. Um `equals` comum para no primeiro caractere diferente, e medindo o tempo daria para adivinhar o hash aos poucos. |
| `dao/OcorrenciaDaoJdbc.java` | 39-40, 80-91 | `WHERE (? IS NULL OR o.ID_BAIRRO = ?)` com `setNull` | Filtro opcional numa consulta só. O mesmo valor vai nos dois `?`. Se for `null`, `? IS NULL` é verdadeiro e o `OR` deixa passar todas as linhas, ou seja, não filtra. Se tiver valor, vale o `ID_BAIRRO = ?`. Assim não é preciso montar o SQL concatenando texto. |
| `dao/BairroDaoJdbc.java` | 26-33 | `con.prepareStatement(sql, new String[] {"ID_BAIRRO"})` e `getGeneratedKeys()` | Diz ao driver: "depois do INSERT, me devolva o valor que o banco gerou na coluna `ID_BAIRRO`" (a coluna `IDENTITY`). É assim que o objeto recebe o id novo. |
| `dao/BairroDaoJdbc.java` | 18 | `ORDER BY NLSSORT(NOME, 'NLS_SORT=BINARY_AI')` | Ordena sem diferenciar acento (A = Á) nem maiúscula (a = A). Sem isso, no Oracle o "Árvore e poda" iria para o fim da lista, depois do Z. |
| `util/Json.java` | 85-236 | classe interna `Leitor` | Um leitor de JSON feito à mão. Anda pela string com um índice (`pos`), pula espaços, espera `{`, lê `"chave"`, espera `:`, lê o valor e repete até `}`. Só aceita objeto simples (sem objeto dentro de objeto) e devolve um `Map<String, String>`. |
| `util/Json.java` | 220-230 | `case 'u':` com `Integer.parseInt(..., 16)` | Trata o escape `á` do JSON: os 4 caracteres depois do `u` são um número em hexadecimal (base 16), que vira o caractere correspondente (`á`). |
| `util/Json.java` | 37-42 | `if (c < 0x20) ... String.format("\\u%04x", ...)` | Na hora de escrever o JSON, caracteres de controle (código menor que 32, como a tabulação) não podem ir crus. Eles viram o escape `\uXXXX` com 4 dígitos hexa. |
| `service/UsuarioService.java` e `frontend/js/usuarios.js` | 38 e 21 | regex `^[^\s@]+@[^\s@]+\.[^\s@]+$` | Confere o formato do e-mail: um ou mais caracteres que não são espaço nem `@`, depois `@`, depois outro trecho sem espaço nem `@`, um ponto, e mais um trecho. `^` e `$` exigem que o texto inteiro bata. No Java, a barra é dobrada (`\\s`) por causa da string. |
| `api/HandlerBase.java` | 70-99, 129 | `MetodoNaoSuportado` (exceção privada) e a cadeia de `catch` | Os métodos `get`/`post`/`put`/`delete` da classe base lançam essa exceção. Se o handler não sobrescreveu o método, ela cai no `catch` e vira 405. A ordem dos `catch` decide o status: 400, 401, 404, 400/500 e, por último, 500 para qualquer outro erro. |
| `exception/DadosException.java` | 5 | `serialVersionUID = 1L` | As exceções do Java são "serializáveis" (podem ser salvas como bytes). Esse número é a versão da classe. Aqui não é usado, mas o compilador avisa se faltar. |
| `service/RelatorioService.java` | 19 | `List<? extends JsonSerializavel>` | Um "coringa" de generics: o método pode devolver uma `List<LinhaRelatorio>` ou uma `List<LinhaRanking>`, desde que os itens implementem `JsonSerializavel`. O handler só precisa chamar `toJson()` em cada um. |
| `model/Usuario.java` | 16-27 | `Usuario.criar(tipo)` e os métodos abstratos | Uma "fábrica": recebe o texto do banco e devolve um `Cidadao` ou um `Agente`. Depois, `usuario.podeApoiar()` chama a versão de cada classe (polimorfismo), e nenhum `if (tipo == "AGENTE")` fica espalhado pelo código. |
| `dao/ConnectionFactory.java` | 57 | `private static synchronized Properties carregarConfiguracao()` | Lê o `db.properties` uma vez só e guarda em memória (`static`). O `synchronized` impede que duas requisições ao mesmo tempo (o servidor tem 8 threads) leiam o arquivo juntas. |
| `api/Servidor.java` | 16 | `http.createContext(rota, handler)` | O `HttpServer` escolhe o handler pelo **começo** do caminho, e o mais longo ganha. Por isso `/usuarios/senha` precisa de um handler próprio: senão cairia no de `/usuarios`. |
| `api/Requisicao.java` | 28, 112 | `getRawQuery()` e `URLDecoder.decode(..., "UTF-8")` | Pega a parte depois do `?` ainda codificada (`nome=Jo%C3%A3o`) e decodifica cada valor (`João`). A separação em `&` e `=` é feita antes de decodificar, para um `&` dentro de um valor não quebrar os pares. |
| `frontend/js/api.js` | 86-95 | `escaparHtml` com `.replace(/&/g, "&amp;")...` | Troca os caracteres especiais do HTML pelas entidades. O `/g` troca **todas** as ocorrências, não só a primeira. O `&` vem primeiro para não estragar as entidades criadas depois. Evita que um título como `<script>` vire código na página (XSS). |
| `frontend/js/api.js` | 153-157 | limpar o texto e `setTimeout(..., 50)` | Leitor de tela só anuncia uma região `role="alert"` quando o conteúdo **muda**. Esvaziar e escrever de novo 50 ms depois força o anúncio, mesmo quando a mensagem é a mesma. |
| `frontend/js/perfil.js` | 140 | `await Promise.all([...])` | Faz as duas chamadas (usuário e bairros) **ao mesmo tempo** e espera as duas. Se uma falhar, cai no `catch`. |
| `frontend/js/componentes.js` | 76-83 | `return new Promise(function (resolver) {...})` | Transforma "abrir a janela e esperar o clique" em algo que dá para usar com `await`. O `resolver` é guardado e só é chamado quando a janela fecha, com `true` (confirmou) ou `false`. |
| `frontend/js/login.js` | 63-80 | setas, Home e End nas abas | Padrão de acessibilidade do WAI-ARIA para abas. `(atual - 1 + n) % n` é o "anterior com volta": na primeira aba, a seta para a esquerda vai para a última. |
| `frontend/js/ocorrencias.js` | 503 | `evento.target.closest("button[data-acao]")` | "Delegação de eventos": um único `listener` na lista inteira. O `closest` sobe do elemento clicado até achar o botão, e o `data-acao` diz o que fazer. Funciona mesmo com cartões recriados pelo `innerHTML`. |
| `sql/avisa_ai.sql` | 2-14 | bloco `BEGIN ... sys.odcivarchar2list ... EXECUTE IMMEDIATE ... SQLCODE != -942` | PL/SQL que percorre a lista de nomes de tabela e tenta `DROP TABLE` em cada uma. O erro -942 ("tabela não existe") é ignorado, e qualquer outro é relançado. É isso que deixa rodar o script duas vezes sem erro. |
| `sql/relatorios.sql` | 5-9, 31 | `LEFT JOIN` + `COUNT(o.ID_OCORRENCIA)` | O `LEFT JOIN` mantém o bairro mesmo sem ocorrência, com as colunas da ocorrência em `NULL`. O `COUNT(coluna)` não conta `NULL`, então esse bairro sai com 0. Já o `COUNT(*)` contaria 1. |

---

## 10 perguntas que o professor pode fazer

1. **Por que `PreparedStatement` e não `Statement`?** O `?` separa o comando dos dados e impede SQL injection. Veja qualquer `*DaoJdbc`, por exemplo `backend/.../dao/UsuarioDaoJdbc.java`.
2. **Como o sistema sabe se o usuário é agente?** A coluna `TIPO` vira `Cidadao` ou `Agente` em `Usuario.criar` (`model/Usuario.java`), chamado em `UsuarioDaoJdbc.montar` (linha 148), e as permissões ficam nos métodos `podeX()` de `model/Agente.java` e `model/Cidadao.java`.
3. **Como a senha é guardada e conferida no login?** Em `service/Senha.java` (`gerarHash` e `confere`) e em `UsuarioService.entrar` (linha 53).
4. **Por que e-mail errado e senha errada dão a mesma mensagem?** Para não revelar quais e-mails têm conta. Veja o comentário em `UsuarioService.java` (linha 52).
5. **O que impede um morador de criar um bairro chamando a API direto? E o que não impede?** O `service/Permissao.java` (`exigirGestor`) confere o tipo de quem age. O que não impede é alguém falsificar o `?usuario`, como explica a seção "Segurança" do `README.md`.
6. **Como a ocorrência vem com autor, categoria, bairro e total de apoios numa consulta só?** Pelos JOINs e pela subconsulta `COUNT` em `OcorrenciaDaoJdbc.SQL_SELECT` (linha 26).
7. **Como o N:N entre usuário e ocorrência foi modelado, e o que impede apoiar duas vezes?** A tabela `APOIO` tem chave primária composta (`sql/avisa_ai.sql`, linha 71). O ORA-00001 vira "Você já apoiou esta ocorrência." em `dao/ErroBanco.java` e `service/ApoioService.java`.
8. **O que acontece com os apoios quando uma ocorrência é excluída? E por que não dá para excluir um bairro em uso?** O `ON DELETE CASCADE` apaga os apoios junto (`sql/avisa_ai.sql`, linhas 73-75). Já a FK sem cascade gera ORA-02292, que vira "em uso" em `dao/ErroBanco.java`.
9. **Como um erro do banco vira uma mensagem na tela?** O caminho é `ErroBanco.traduzir` → `DadosException` → `HandlerBase.processar` (status HTTP + `{"erro":...}`) → `chamarApi` em `frontend/js/api.js`, que lança o `Error` com a mensagem.
10. **Por que o front funciona aberto direto do disco e mesmo assim conversa com `localhost:8080`?** Por causa dos cabeçalhos de CORS e da resposta 204 ao `OPTIONS` em `api/HandlerBase.java` (`adicionarCors`, linha 51, e linha 54).

---

## Sobre a seção "Uso de IA generativa"

O pedido de revisão diz para manter essa seção do README como está, mas ela não existe em nenhum arquivo `.md`, nem no histórico do git. Se a disciplina exige essa declaração, ela precisa ser escrita. Para ajudar a descrever com precisão, estes arquivos foram criados ou reescritos com o Claude (IA) nas sessões de 13 a 16/11/2026:

- **Back:** os 6 `*DaoJdbc`, `LoginHandler`, `UsuarioSenhaHandler`, `NaoAutenticadoException`, `Permissao`, `Senha`, as mudanças em `UsuarioService`, `BairroService`, `CategoriaService`, `UsuarioDao`, `HandlerBase`, `Main` e nos models (`podeGerenciarCadastros`).
- **SQL:** `sql/relatorios.sql`, a coluna `SENHA_HASH` e o prefixo `AV_` em `sql/avisa_ai.sql`.
- **Front:** `js/componentes.js`, `css/bootstrap-componentes.css` (recorte do Bootstrap), as mudanças em `usuarios.js`/`.html`, `gestao.js`/`.html`, `perfil.js`, `sessao.js`, `inicio.js`, `login.js`/`.html`, a remoção dos dados provisórios do `api.js` e a seção do Bootstrap no `style.css`.
- **Documentação:** grande parte do `README.md`, do `backend/README.md` e do `docs/contrato-persistencia.md`, e este relatório.
