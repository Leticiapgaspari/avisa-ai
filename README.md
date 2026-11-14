# avisa-ai
Avisa Aí: projeto comunitário. Moradores registram e apoiam ocorrências, agentes acompanham a solução. Global Solution FIAP 2026/2 (CitizenTech).

## Front-end

Fizemos o front com HTML, CSS e JavaScript puros, sem framework. Como é um projeto acadêmico, a gente queria entender cada linha do que entregou, e assim as páginas abrem direto no navegador, sem instalar nada.

- **HTML semântico**, para o código ficar fácil de ler e funcionar bem com leitor de tela.
- **CSS** com as cores do projeto definidas uma vez só em variáveis, e layout com Flexbox e Grid que se ajusta do celular ao computador.
- **JavaScript** com um arquivo por página. A conversa com o back fica toda no `api.js`, e quem está logado é controlado pelo `sessao.js`.
- **Fontes Barlow** salvas no projeto e **ilustrações em SVG**, leves e nítidas em qualquer tela.

Enquanto o back-end não fica pronto, o `api.js` responde com dados de teste. Para usar a API de verdade, é só trocar `USAR_DADOS_PROVISORIOS` para `false`.

Para testar, abra `frontend/index.html` e entre com `ana.souza@exemplo.com` (morador) ou `patricia.mendes@exemplo.com` (agente), senha `avisa123`.
