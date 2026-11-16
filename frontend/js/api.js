// Comunicação com o back-end e funções usadas

// JSON que o back devolve:
//  ocorrência: { id, titulo, descricao, endereco, status, dataAbertura, autor, categoria, bairro, totalApoios }
//  usuário: { id, nome, email, tipo, bairro }   (a senha nunca vem)
//  bairro e categoria: { id, nome }
//  relatorios: { nome, total }   ranking: { id, titulo, bairro, status, totalApoios }
//  erro: { "erro": "mensagem" }

const API = "http://localhost:8080";

const MENSAGEM_SEM_CONEXAO = "Não consegui falar com o servidor. Confira se ele está ligado e recarregue a página.";

// Ex: chamarApi("/ocorrencias?bairro=2") ou chamarApi("/apoios", "POST", { ... })
// Se der erro, lança um Error com a mensagem para mostrar na tela
async function chamarApi(caminho, metodo, corpo) {
  metodo = metodo || "GET";

  const opcoes = { method: metodo, headers: {} };
  if (corpo !== undefined && corpo !== null) {
    opcoes.headers["Content-Type"] = "application/json";
    opcoes.body = JSON.stringify(corpo);
  }

  let resposta;
  try {
    resposta = await fetch(API + caminho, opcoes);
  } catch (erro) {
    // só cai aqui se não chegou no servidor (desligado ou endereço errado)
    throw new Error(MENSAGEM_SEM_CONEXAO);
  }

  const texto = await resposta.text();
  let dados = null;
  if (texto) {
    try {
      dados = JSON.parse(texto);
    } catch (erro) {
      dados = null;
    }
  }

  if (!resposta.ok) {
    if (dados && dados.erro) {
      throw new Error(dados.erro);
    }
    throw new Error("O servidor recusou a operação (erro " + resposta.status + ").");
  }

  return dados;
}

// funções de apoio

const NOMES_STATUS = {
  ABERTA: "Aberta",
  EM_ANDAMENTO: "Em andamento",
  RESOLVIDA: "Resolvida"
};

const CLASSES_STATUS = {
  ABERTA: "aberta",
  EM_ANDAMENTO: "andamento",
  RESOLVIDA: "resolvida"
};

// na tela aparece "Morador", mas pra API continua CIDADAO
const NOMES_TIPO = {
  CIDADAO: "Morador",
  AGENTE: "Agente"
};

// Monta "?a=1&b=2" ignorando os valores vazios
function montarQuery(parametros) {
  const partes = [];
  for (const chave in parametros) {
    const valor = parametros[chave];
    if (valor !== "" && valor !== null && valor !== undefined) {
      partes.push(encodeURIComponent(chave) + "=" + encodeURIComponent(valor));
    }
  }
  return partes.length > 0 ? "?" + partes.join("&") : "";
}

// evita que o texto digitado vire HTML na página
function escaparHtml(texto) {
  if (texto === null || texto === undefined) {
    return "";
  }
  return String(texto)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function etiquetaStatus(status) {
  const classe = CLASSES_STATUS[status] || "";
  const nome = NOMES_STATUS[status] || status;
  return '<span class="etiqueta etiqueta-' + classe + '">' + escaparHtml(nome) + "</span>";
}

// "2026-11-02" -> "02/11/2026"
function formatarData(data) {
  if (!data) {
    return "";
  }
  const partes = String(data).substring(0, 10).split("-");
  if (partes.length !== 3) {
    return data;
  }
  return partes[2] + "/" + partes[1] + "/" + partes[0];
}

// texto vazio esconde a mensagem
function mostrarMensagem(elemento, texto, tipo) {
  if (!elemento) {
    return;
  }
  elemento.textContent = texto || "";
  elemento.className = "mensagem";
  if (texto) {
    elemento.classList.add(tipo === "erro" ? "mensagem-erro" : "mensagem-sucesso");
  }
}

// -servidor fora do ar-
// O aviso aparece uma vez só

function ehErroDeConexao(erro) {
  return Boolean(erro) && erro.message === MENSAGEM_SEM_CONEXAO;
}

function mostrarFaixaSemConexao() {
  let faixa = document.getElementById("faixa-sem-conexao");
  if (!faixa) {
    faixa = document.createElement("p");
    faixa.id = "faixa-sem-conexao";
    faixa.className = "faixa-sem-conexao";
    faixa.setAttribute("role", "alert");

    const cabecalho = document.querySelector(".cabecalho-pagina, .painel-saudacao");
    const cartaoLogin = document.querySelector(".cartao-login");
    if (cabecalho) {
      cabecalho.insertAdjacentElement("afterend", faixa);
    } else if (cartaoLogin) {
      cartaoLogin.insertAdjacentElement("afterbegin", faixa);
    } else {
      (document.querySelector("main") || document.body).prepend(faixa);
    }
  }
  // limpa e escreve de novo pro leitor de tela anunciar outra vez
  faixa.textContent = "";
  window.setTimeout(function () {
    faixa.textContent = MENSAGEM_SEM_CONEXAO;
  }, 50);
}

// Usada nos catch: devolve true se foi falta de conexão, aí o bloco fica vazio.
function avisarSeSemConexao(erro) {
  if (ehErroDeConexao(erro)) {
    mostrarFaixaSemConexao();
    return true;
  }
  return false;
}

// preenche um select com [{ id, nome }] mantendo a opção escolhida
function preencherSelect(select, itens, textoPrimeiraOpcao) {
  const valorAtual = select.value;
  select.innerHTML = "";
  select.appendChild(new Option(textoPrimeiraOpcao, ""));
  itens.forEach(function (item) {
    select.appendChild(new Option(item.nome, item.id));
  });
  select.value = valorAtual;
  if (select.value !== valorAtual) {
    select.value = "";
  }
}

function marcarErro(campo, texto) {
  campo.classList.add("campo-invalido");
  campo.setAttribute("aria-invalid", "true");
  const areaErro = document.getElementById("erro-" + campo.id);
  if (areaErro) {
    areaErro.textContent = texto;
  }
}

function limparErros(formulario) {
  formulario.querySelectorAll(".campo-invalido").forEach(function (campo) {
    campo.classList.remove("campo-invalido");
    campo.removeAttribute("aria-invalid");
  });
  formulario.querySelectorAll(".erro-campo").forEach(function (area) {
    area.textContent = "";
  });
}

// o erro some quando a pessoa começa a corrigir o campo
function limparErroAoCorrigir(formulario) {
  formulario.addEventListener("input", function (evento) {
    const campo = evento.target;
    if (campo.classList.contains("campo-invalido")) {
      campo.classList.remove("campo-invalido");
      campo.removeAttribute("aria-invalid");
      const areaErro = document.getElementById("erro-" + campo.id);
      if (areaErro) {
        areaErro.textContent = "";
      }
    }
  });
}
