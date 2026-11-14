// Sessão: quem está logado (fica no sessionStorage) e o que cada tipo pode ver.
// No <body>: data-pagina="privada" exige login e "so-agente" exige ser agente.
// Esconder botão aqui é só pra interface; quem valida de verdade é o back.

const AVISO_SEM_USUARIO = "Escolha um usuário para participar";

const usuarioAtual = lerUsuarioDaSessao();

function lerUsuarioDaSessao() {
  try {
    const id = sessionStorage.getItem("usuarioId");
    if (!id) {
      return null;
    }
    return {
      id: Number(id),
      nome: sessionStorage.getItem("usuarioNome"),
      tipo: sessionStorage.getItem("usuarioTipo")
    };
  } catch (erro) {
    return null;
  }
}

function guardarUsuarioNaSessao(id, nome, tipo) {
  try {
    sessionStorage.setItem("usuarioId", id);
    sessionStorage.setItem("usuarioNome", nome);
    sessionStorage.setItem("usuarioTipo", tipo);
  } catch (erro) {
    // sem sessionStorage a sessão dura até recarregar a página
  }
}

function limparUsuarioDaSessao() {
  try {
    sessionStorage.removeItem("usuarioId");
    sessionStorage.removeItem("usuarioNome");
    sessionStorage.removeItem("usuarioTipo");
  } catch (erro) {
    // nada a fazer
  }
}

function temUsuario() {
  return usuarioAtual !== null;
}

function ehAgente() {
  return temUsuario() && usuarioAtual.tipo === "AGENTE";
}

function ehCidadao() {
  return temUsuario() && usuarioAtual.tipo === "CIDADAO";
}

// manda pro login quem não entrou e pro painel o morador numa página de agente
// (devolve false quando vai redirecionar)
function protegerPagina() {
  const tipoPagina = document.body.dataset.pagina;
  if (!tipoPagina) {
    return true;
  }
  if (!temUsuario()) {
    location.replace("login.html#entrar");
    return false;
  }
  if (tipoPagina === "so-agente" && !ehAgente()) {
    location.replace("painel.html");
    return false;
  }
  return true;
}

function primeiroNome(nome) {
  return String(nome || "").trim().split(" ")[0];
}

// lê a sessão de novo porque o nome pode ter mudado no perfil
function preencherAreaUsuario() {
  const usuario = lerUsuarioDaSessao();
  const campoNome = document.getElementById("nome-usuario");
  const campoTipo = document.getElementById("tipo-usuario");
  if (!usuario || !campoNome || !campoTipo) {
    return;
  }
  campoNome.textContent = primeiroNome(usuario.nome);
  campoTipo.textContent = usuario.tipo === "AGENTE" ? "Agente" : "Morador";
  campoTipo.classList.toggle("agente", usuario.tipo === "AGENTE");

  const avatar = document.getElementById("avatar-usuario");
  if (avatar) {
    avatar.textContent = String(usuario.nome || "?").trim().charAt(0).toUpperCase();
  }
  const nomeCompleto = document.getElementById("conta-nome-completo");
  if (nomeCompleto) {
    nomeCompleto.textContent = usuario.nome;
  }
}

// ---------- menu da conta ----------
// fecha com Esc, com clique fora ou quando o foco sai dele

// o e-mail não fica na sessão, então busca no back
async function carregarEmailDaConta() {
  const campoEmail = document.getElementById("conta-email");
  if (!campoEmail || !temUsuario() || typeof chamarApi !== "function") {
    return;
  }
  try {
    const conta = await chamarApi("/usuarios?id=" + encodeURIComponent(usuarioAtual.id));
    campoEmail.textContent = conta && conta.email ? conta.email : "";
  } catch (erro) {
    campoEmail.textContent = "";
  }
}

function configurarMenuConta() {
  const botao = document.getElementById("botao-conta");
  const painel = document.getElementById("painel-conta");
  if (!botao || !painel) {
    return;
  }
  const menu = botao.parentElement;
  // "Mais" da barra do rodapé (só agente, no celular) abre o mesmo painel
  const botaoMais = document.getElementById("botao-mais");
  let origem = botao;

  function fazParteDoMenu(elemento) {
    return menu.contains(elemento) || (botaoMais !== null && botaoMais.contains(elemento));
  }

  function abrir(quem) {
    origem = quem;
    painel.hidden = false;
    painel.classList.toggle("aberto-pela-barra", quem === botaoMais);
    quem.setAttribute("aria-expanded", "true");
    // a barra fica no fim da página, então o foco vai direto pro painel
    if (quem === botaoMais) {
      const primeiroLink = painel.querySelector("a");
      if (primeiroLink) {
        primeiroLink.focus();
      }
    }
  }

  function fechar(devolverFoco) {
    if (painel.hidden) {
      return;
    }
    painel.hidden = true;
    painel.classList.remove("aberto-pela-barra");
    botao.setAttribute("aria-expanded", "false");
    if (botaoMais) {
      botaoMais.setAttribute("aria-expanded", "false");
    }
    if (devolverFoco) {
      origem.focus();
    }
  }

  function alternar(quem) {
    if (painel.hidden) {
      abrir(quem);
    } else {
      fechar(false);
    }
  }

  botao.addEventListener("click", function () {
    alternar(botao);
  });

  if (botaoMais) {
    botaoMais.addEventListener("click", function () {
      alternar(botaoMais);
    });
    botaoMais.addEventListener("keydown", function (evento) {
      if (evento.key === "Escape") {
        fechar(true);
      }
    });
  }

  menu.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      fechar(true);
    }
  });

  document.addEventListener("click", function (evento) {
    if (!fazParteDoMenu(evento.target)) {
      fechar(false);
    }
  });

  menu.addEventListener("focusout", function (evento) {
    if (evento.relatedTarget && !fazParteDoMenu(evento.relatedTarget)) {
      fechar(false);
    }
  });

  // marca a página atual
  const pagina = location.pathname.split("/").pop();
  painel.querySelectorAll("a").forEach(function (link) {
    if (link.getAttribute("href") === pagina) {
      link.setAttribute("aria-current", "page");
    }
  });

  carregarEmailDaConta();
}

function sair() {
  limparUsuarioDaSessao();
  location.href = "index.html";
}

function marcarLinkAtual() {
  let pagina = location.pathname.split("/").pop();
  if (!pagina) {
    pagina = "index.html";
  }
  document.querySelectorAll(".menu a").forEach(function (link) {
    if (link.getAttribute("href") === pagina) {
      link.classList.add("ativo");
      link.setAttribute("aria-current", "page");
    }
  });
}

// ---------- barra do rodapé (celular) ----------
// o CSS só mostra a barra abaixo de 880 px; acima dela vale o menu do cabeçalho

const ICONES_BARRA = {
  painel: '<path d="M4 4h7v7H4zM13 4h7v5h-7zM13 11h7v9h-7zM4 13h7v7H4z"/>',
  ocorrencias: '<path d="M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z"/><circle cx="12" cy="10" r="2.3"/>',
  relatorios: '<path d="M4 20h16M7 16v-5M12 16V6M17 16v-8"/>',
  mais: '<circle cx="5.5" cy="12" r="1.3"/><circle cx="12" cy="12" r="1.3"/><circle cx="18.5" cy="12" r="1.3"/>'
};

function iconeBarra(nome) {
  return '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" fill="none" stroke="currentColor" ' +
    'stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">' + ICONES_BARRA[nome] + "</svg>";
}

function montarBarraInferior() {
  if (!temUsuario() || !document.querySelector(".topo .menu")) {
    return;
  }
  const pagina = location.pathname.split("/").pop();

  function item(href, texto, icone) {
    const atual = href === pagina ? ' ativo" aria-current="page' : "";
    return '<li><a href="' + href + '" class="barra-item' + atual + '">' +
      iconeBarra(icone) + "<span>" + texto + "</span></a></li>";
  }

  let html = item("painel.html", "Painel", "painel") +
    item("ocorrencias.html", "Ocorrências", "ocorrencias") +
    '<li><a href="ocorrencias.html#registrar" class="barra-registrar" aria-label="Registrar ocorrência">' +
    '<span aria-hidden="true">+</span></a></li>' +
    item("relatorios.html", "Relatórios", "relatorios");

  if (ehAgente()) {
    const maisAtivo = pagina === "usuarios.html" || pagina === "gestao.html" ? " ativo" : "";
    html += '<li><button type="button" class="barra-item' + maisAtivo + '" id="botao-mais" ' +
      'aria-expanded="false" aria-controls="painel-conta">' + iconeBarra("mais") + "<span>Mais</span></button></li>";

    // Usuários e Gestão no topo do menu da conta (o CSS só mostra no celular)
    const linksConta = document.querySelector(".menu-conta-links");
    if (linksConta) {
      linksConta.insertAdjacentHTML("afterbegin",
        '<li class="menu-conta-so-celular"><a href="usuarios.html">Usuários</a></li>' +
        '<li class="menu-conta-so-celular"><a href="gestao.html">Gestão</a></li>');
    }
  }

  const barra = document.createElement("nav");
  barra.className = "barra-inferior";
  barra.setAttribute("aria-label", "Menu principal");
  barra.innerHTML = "<ul>" + html + "</ul>";
  document.body.appendChild(barra);
  document.body.classList.add("tem-barra-inferior");
}

function aplicarPermissoes() {
  // ex.: Usuários e Gestão no menu
  document.querySelectorAll("[data-so-agente]").forEach(function (elemento) {
    elemento.hidden = !ehAgente();
  });

  // sem login a pessoa só consulta
  if (!temUsuario()) {
    document.querySelectorAll("[data-requer-usuario]").forEach(function (elemento) {
      elemento.disabled = true;
    });
    document.querySelectorAll(".aviso-usuario").forEach(function (aviso) {
      aviso.textContent = AVISO_SEM_USUARIO + ".";
      aviso.hidden = false;
    });
  }
}

const botaoSair = document.getElementById("botao-sair");
if (botaoSair) {
  botaoSair.addEventListener("click", sair);
}

if (protegerPagina()) {
  marcarLinkAtual();
  montarBarraInferior();
  aplicarPermissoes();
  preencherAreaUsuario();
  configurarMenuConta();
}
