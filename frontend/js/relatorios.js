// Relatórios: resumo, por bairro, por categoria e as mais apoiadas.
// As barras são feitas só com CSS.

const TAMANHO_RANKING = 10;
const DESTAQUES_RANKING = 3;   // as três primeiras posições ganham o círculo laranja

function buscarRelatorio(tipo) {
  return chamarApi("/relatorios" + montarQuery({ tipo: tipo }));
}

function linhaEstado(colunas, classe, texto) {
  return '<tr><td colspan="' + colunas + '" class="' + classe + '">' + escaparHtml(texto) + "</td></tr>";
}

function maiorValor(lista, campo) {
  let maior = 1;
  lista.forEach(function (item) {
    if (item[campo] > maior) {
      maior = item[campo];
    }
  });
  return maior;
}

// ---------- por bairro e por categoria ----------

function linhasComBarra(linhas) {
  const maior = maiorValor(linhas, "total");
  return linhas.map(function (l) {
    const largura = Math.round((l.total / maior) * 100);
    return (
      "<tr>" +
      '<td class="barras-nome">' + escaparHtml(l.nome) + "</td>" +
      '<td class="barras-valor"><div class="barra-total">' +
      '<span class="barra-trilho" aria-hidden="true"><span class="barra" style="width:' + largura + '%"></span></span>' +
      '<span class="barras-numero">' + l.total + "</span>" +
      "</div></td>" +
      "</tr>"
    );
  }).join("");
}

async function carregarBarras(tipo, idCorpo) {
  const corpo = document.getElementById(idCorpo);
  try {
    const linhas = await buscarRelatorio(tipo);
    corpo.innerHTML = linhas && linhas.length > 0
      ? linhasComBarra(linhas)
      : linhaEstado(2, "lista-vazia", "Nenhum dado para mostrar.");
  } catch (erro) {
    corpo.innerHTML = avisarSeSemConexao(erro) ? "" : linhaEstado(2, "lista-estado erro", erro.message);
  }
}

// ---------- mais apoiadas ----------

function linhasRanking(linhas) {
  const maior = maiorValor(linhas, "totalApoios");
  return linhas.map(function (l, indice) {
    const posicao = indice + 1;
    const classePosicao = posicao <= DESTAQUES_RANKING ? "posicao posicao-topo" : "posicao";
    const largura = Math.round((l.totalApoios / maior) * 100);
    return (
      "<tr>" +
      '<td class="coluna-posicao"><span class="' + classePosicao + '">' + posicao +
      '<span class="visualmente-oculto">º</span></span></td>' +
      '<td class="ranking-ocorrencia">' +
      "<strong>" + escaparHtml(l.titulo) + "</strong>" +
      '<span class="texto-suave">' + escaparHtml(l.bairro) + "</span>" +
      "</td>" +
      '<td class="ranking-status">' + etiquetaStatus(l.status) + "</td>" +
      '<td class="coluna-apoios"><div class="ranking-apoios">' +
      '<span class="ranking-numero">' + l.totalApoios +
      '<span class="visualmente-oculto">' + (l.totalApoios === 1 ? " apoio" : " apoios") + "</span></span>" +
      '<span class="ranking-trilho" aria-hidden="true"><span class="ranking-barra" style="width:' + largura + '%"></span></span>' +
      "</div></td>" +
      "</tr>"
    );
  }).join("");
}

// devolve o ranking inteiro (pra somar os apoios) ou null se falhar
async function carregarRanking() {
  const corpo = document.getElementById("corpo-ranking");
  try {
    const linhas = await buscarRelatorio("ranking");
    corpo.innerHTML = linhas && linhas.length > 0
      ? linhasRanking(linhas.slice(0, TAMANHO_RANKING))
      : linhaEstado(4, "lista-vazia", "Nenhum dado para mostrar.");
    return linhas || [];
  } catch (erro) {
    corpo.innerHTML = avisarSeSemConexao(erro) ? "" : linhaEstado(4, "lista-estado erro", erro.message);
    return null;
  }
}

// ---------- resumo do topo ----------

async function carregarResumo(promessaRanking) {
  const mensagem = document.getElementById("mensagem-resumo");
  try {
    // cada linha vem como { nome: "ABERTA", total: 7 }
    const porStatus = await buscarRelatorio("status");
    let totalAvisos = 0;
    let resolvidas = 0;
    porStatus.forEach(function (l) {
      totalAvisos += l.total;
      if (l.nome === "RESOLVIDA") {
        resolvidas = l.total;
      }
    });
    document.getElementById("resumo-avisos").textContent = totalAvisos;
    document.getElementById("resumo-resolvidas").textContent = resolvidas;
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagem, erro.message, "erro");
    }
  }

  // o ranking tem todas as ocorrências, então dá pra somar os apoios
  const ranking = await promessaRanking;
  if (ranking) {
    let totalApoios = 0;
    ranking.forEach(function (l) {
      totalApoios += l.totalApoios;
    });
    document.getElementById("resumo-apoios").textContent = totalApoios;
  }
}

function carregarRelatorios() {
  carregarBarras("bairro", "corpo-bairro");
  carregarBarras("categoria", "corpo-categoria");
  carregarResumo(carregarRanking());
}

carregarRelatorios();
