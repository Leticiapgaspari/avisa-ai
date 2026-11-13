// Painel: muda conforme o tipo de conta.
// Morador vê o próprio bairro; agente vê as abertas com mais apoios.

// erro do servidor que não é falta de conexão
const MENSAGEM_FALHA_PAINEL =
  "Não consegui carregar estes dados agora. Recarregue a página para tentar de novo.";
const QUANTIDADE_PRIORIDADES = 3;   // agente: abertas com mais apoios
const QUANTIDADE_DO_BAIRRO = 4;     // morador: mais recentes do bairro
const QUANTIDADE_RECENTES = 5;      // os dois: mais recentes de todos os bairros

// ---------- apoio ----------

function textoEstado(texto, ehErro) {
  return '<p class="painel-estado' + (ehErro ? " erro" : "") + '">' + escaparHtml(texto) + "</p>";
}

function mostrarFalha(elemento, erro) {
  elemento.innerHTML = avisarSeSemConexao(erro) ? "" : textoEstado(MENSAGEM_FALHA_PAINEL, true);
}

function textoApoios(total) {
  return total === 1 ? "1 apoio" : total + " apoios";
}

// mais novas primeiro (no empate, o maior id)
function ordenarMaisRecentes(lista) {
  return lista.slice().sort(function (a, b) {
    return String(b.dataAbertura).localeCompare(String(a.dataAbertura)) || b.id - a.id;
  });
}

// "Bom dia" até 11h59, "Boa tarde" até 17h59, "Boa noite" depois
function saudacaoPelaHora() {
  const hora = new Date().getHours();
  if (hora < 12) {
    return "Bom dia";
  }
  if (hora < 18) {
    return "Boa tarde";
  }
  return "Boa noite";
}

// ---------- saudação ----------

function montarSaudacao(usuario) {
  const nome = primeiroNome(usuario.nome);
  document.getElementById("titulo-pagina").textContent =
    saudacaoPelaHora() + (nome ? ", " + nome : "") + ".";

  const texto = document.getElementById("texto-saudacao");
  const botoes = document.getElementById("botoes-saudacao");

  if (ehAgente()) {
    texto.textContent = "Estas são as ocorrências que os moradores mais estão pedindo. Comece por elas.";
    botoes.innerHTML =
      '<a href="ocorrencias.html?status=ABERTA" class="botao botao-cone">Ver as abertas</a>' +
      '<a href="relatorios.html" class="botao botao-contorno">Ver relatórios</a>';
  } else {
    texto.textContent = "Viu alguma coisa na rua hoje? Registre, ou confirme o que os vizinhos já avisaram.";
    // o link do bairro é preenchido depois que o GET /usuarios responder
    botoes.innerHTML =
      '<a href="ocorrencias.html#registrar" class="botao botao-cone">Registrar ocorrência</a>' +
      '<a href="ocorrencias.html" class="botao botao-contorno" id="link-meu-bairro">Ver o meu bairro</a>';
  }
}

// ---------- situação geral ----------

const STATUS_DO_PAINEL = [
  { valor: "ABERTA", nome: "Abertas", classe: "aberta" },
  { valor: "EM_ANDAMENTO", nome: "Em andamento", classe: "andamento" },
  { valor: "RESOLVIDA", nome: "Resolvidas", classe: "resolvida" }
];

async function carregarSituacao() {
  const area = document.getElementById("situacao");
  try {
    const linhas = await chamarApi("/relatorios?tipo=status");

    // cada linha vem como { nome: "ABERTA", total: 7 }
    const totais = { ABERTA: 0, EM_ANDAMENTO: 0, RESOLVIDA: 0 };
    linhas.forEach(function (linha) {
      if (linha.nome in totais) {
        totais[linha.nome] = Number(linha.total) || 0;
      }
    });
    const total = totais.ABERTA + totais.EM_ANDAMENTO + totais.RESOLVIDA;

    if (total === 0) {
      area.innerHTML = textoEstado("Nenhum aviso registrado ainda.");
      return;
    }

    const resolvidas = totais.RESOLVIDA;
    const resumo =
      '<p class="painel-resumo"><strong class="painel-resumo-numero">' + resolvidas + "</strong> de " +
      total + (total === 1 ? " aviso" : " avisos") +
      (resolvidas === 1 ? " já foi resolvido." : " já foram resolvidos.") + "</p>";

    // barra dividida na proporção de cada status
    const descricaoBarra = STATUS_DO_PAINEL.map(function (s) {
      return s.nome + ": " + totais[s.valor];
    }).join(", ");
    const partes = STATUS_DO_PAINEL.filter(function (s) {
      return totais[s.valor] > 0;
    }).map(function (s) {
      return '<span class="painel-barra-parte ' + s.classe + '" style="flex-grow:' + totais[s.valor] + '"></span>';
    }).join("");
    const barra = '<div class="painel-barra" role="img" aria-label="' + escaparHtml(descricaoBarra) + '">' + partes + "</div>";

    const legendas = '<ul class="painel-legendas">' + STATUS_DO_PAINEL.map(function (s) {
      return (
        "<li>" +
        '<a href="ocorrencias.html?status=' + s.valor + '">' +
        '<span class="painel-bolinha ' + s.classe + '" aria-hidden="true"></span>' +
        '<span class="painel-legenda-numero">' + totais[s.valor] + "</span>" +
        '<span class="painel-legenda-nome">' + s.nome + "</span>" +
        "</a>" +
        "</li>"
      );
    }).join("") + "</ul>";

    area.innerHTML = resumo + barra + legendas;
  } catch (erro) {
    mostrarFalha(area, erro);
  }
}

// ---------- coluna principal do agente ----------

async function carregarPrioridades() {
  const titulo = document.getElementById("titulo-principal");
  const area = document.getElementById("conteudo-principal");
  titulo.textContent = "Pedem atenção primeiro";

  try {
    // o ranking já vem ordenado por apoios
    const ranking = await chamarApi("/relatorios?tipo=ranking");
    const abertas = ranking.filter(function (o) {
      return o.status === "ABERTA";
    }).slice(0, QUANTIDADE_PRIORIDADES);

    if (abertas.length === 0) {
      area.innerHTML = textoEstado("Nenhuma ocorrência aberta agora. Bom sinal.");
      return;
    }

    area.innerHTML = '<ol class="painel-prioridades" tabindex="0">' + abertas.map(function (o) {
      return (
        "<li>" +
        '<span class="painel-prioridade-apoios">' +
        '<span class="painel-prioridade-numero">' + o.totalApoios + "</span>" +
        "<span>" + (o.totalApoios === 1 ? "apoio" : "apoios") + "</span>" +
        "</span>" +
        '<span class="painel-prioridade-texto">' +
        "<strong>" + escaparHtml(o.titulo) + "</strong>" +
        '<span class="texto-suave">' + escaparHtml(o.bairro) + "</span>" +
        "</span>" +
        "</li>"
      );
    }).join("") + "</ol>";
  } catch (erro) {
    mostrarFalha(area, erro);
  }
}

// ---------- coluna principal do morador ----------

async function carregarMeuBairro(usuario) {
  const titulo = document.getElementById("titulo-principal");
  const area = document.getElementById("conteudo-principal");
  titulo.textContent = "No seu bairro";

  try {
    const dadosUsuario = await chamarApi("/usuarios?id=" + encodeURIComponent(usuario.id));
    const bairro = dadosUsuario.bairro;
    if (!bairro) {
      throw new Error("Usuário sem bairro.");
    }

    titulo.textContent = "No " + bairro.nome;
    const linkBairro = "ocorrencias.html?bairro=" + encodeURIComponent(bairro.id);
    const botaoBairro = document.getElementById("link-meu-bairro");
    if (botaoBairro) {
      botaoBairro.href = linkBairro;
    }

    const doBairro = ordenarMaisRecentes(
      await chamarApi("/ocorrencias?bairro=" + encodeURIComponent(bairro.id))
    ).slice(0, QUANTIDADE_DO_BAIRRO);

    if (doBairro.length === 0) {
      area.innerHTML = textoEstado("Nada registrado no seu bairro por enquanto. Se você viu algum problema, é só avisar.");
      return;
    }

    area.innerHTML =
      '<ul class="painel-lista" tabindex="0">' + doBairro.map(function (o) {
        return (
          "<li>" +
          '<span class="painel-item-texto">' +
          "<strong>" + escaparHtml(o.titulo) + "</strong>" +
          '<span class="texto-suave">' + escaparHtml(o.categoria ? o.categoria.nome : "") + "</span>" +
          "</span>" +
          '<span class="painel-item-lado">' +
          etiquetaStatus(o.status) +
          '<span class="painel-apoios">' + textoApoios(o.totalApoios) + "</span>" +
          "</span>" +
          "</li>"
        );
      }).join("") + "</ul>" +
      '<a href="' + linkBairro + '" class="painel-ver-todas">Ver todas do bairro</a>';
  } catch (erro) {
    mostrarFalha(area, erro);
  }
}

// ---------- coluna lateral (os dois) ----------

async function carregarRecentes() {
  const area = document.getElementById("conteudo-recentes");
  try {
    const recentes = ordenarMaisRecentes(await chamarApi("/ocorrencias")).slice(0, QUANTIDADE_RECENTES);

    if (recentes.length === 0) {
      area.innerHTML = textoEstado("Nenhuma ocorrência registrada ainda.");
      return;
    }

    area.innerHTML =
      '<ul class="painel-lista" tabindex="0">' + recentes.map(function (o) {
        const detalhes = [o.bairro ? o.bairro.nome : "", o.categoria ? o.categoria.nome : ""]
          .filter(Boolean)
          .join(", ");
        return (
          "<li>" +
          '<span class="painel-item-texto">' +
          "<strong>" + escaparHtml(o.titulo) + "</strong>" +
          '<span class="texto-suave">' + escaparHtml(detalhes) + "</span>" +
          "</span>" +
          '<span class="painel-item-lado">' +
          etiquetaStatus(o.status) +
          '<span class="painel-apoios">' + textoApoios(o.totalApoios) + "</span>" +
          "</span>" +
          "</li>"
        );
      }).join("") + "</ul>" +
      '<a href="ocorrencias.html" class="painel-ver-todas">Ver todas</a>';
  } catch (erro) {
    mostrarFalha(area, erro);
  }
}

// ---------- início ----------

function iniciarPainel() {
  const usuario = lerUsuarioDaSessao();
  // sem login o sessao.js já redireciona
  if (!usuario) {
    return;
  }

  montarSaudacao(usuario);
  carregarSituacao();
  if (ehAgente()) {
    carregarPrioridades();
  } else {
    carregarMeuBairro(usuario);
  }
  carregarRecentes();
}

iniciarPainel();
