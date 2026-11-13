// Login: abas Entrar/Criar conta, validação e envio dos dois formulários.

const FORMATO_EMAIL_LOGIN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const TEMPO_ANTES_DE_ENTRAR = 900; // ms mostrando "Conta criada. Entrando..."

// quem já está logado vai direto pro painel
if (temUsuario()) {
  location.replace("painel.html");
}

const abas = {
  entrar: document.getElementById("aba-entrar"),
  cadastro: document.getElementById("aba-cadastro")
};
const caixas = {
  entrar: document.getElementById("caixa-entrar"),
  cadastro: document.getElementById("caixa-cadastro")
};

const formEntrar = document.getElementById("form-entrar");
const campoEmailEntrar = document.getElementById("email-entrar");
const campoSenhaEntrar = document.getElementById("senha-entrar");
const botaoMostrarSenha = document.getElementById("mostrar-senha-entrar");
const botaoEntrar = document.getElementById("botao-entrar");
const mensagemEntrar = document.getElementById("mensagem-entrar");

const formCadastro = document.getElementById("form-cadastro");
const campoNomeCadastro = document.getElementById("nome-cadastro");
const campoEmailCadastro = document.getElementById("email-cadastro");
const campoBairroCadastro = document.getElementById("bairro-cadastro");
const campoSenhaCadastro = document.getElementById("senha-cadastro");
const campoConfirmarCadastro = document.getElementById("confirmar-cadastro");
const botaoCadastrar = document.getElementById("botao-cadastrar");
const mensagemCadastro = document.getElementById("mensagem-cadastro");

// ---------- abas ----------

// nome: "entrar" ou "cadastro"
function mostrarAba(nome, atualizarEndereco) {
  for (const chave in abas) {
    const ativa = chave === nome;
    abas[chave].setAttribute("aria-selected", ativa ? "true" : "false");
    // só a aba ativa recebe o Tab; as setas trocam de aba
    abas[chave].tabIndex = ativa ? 0 : -1;
    caixas[chave].hidden = !ativa;
  }
  if (atualizarEndereco) {
    try {
      history.replaceState(null, "", "#" + nome);
    } catch (erro) {
      location.hash = nome;
    }
  }
}

function abaDoEndereco() {
  return location.hash === "#cadastro" ? "cadastro" : "entrar";
}

// setas, Home e End trocam de aba (padrão de abas do WAI-ARIA)
const ORDEM_ABAS = ["entrar", "cadastro"];

abas.entrar.parentElement.addEventListener("keydown", function (evento) {
  const atual = ORDEM_ABAS.indexOf(abas.entrar === document.activeElement ? "entrar" : "cadastro");
  let proxima = null;
  if (evento.key === "ArrowRight") {
    proxima = ORDEM_ABAS[(atual + 1) % ORDEM_ABAS.length];
  } else if (evento.key === "ArrowLeft") {
    proxima = ORDEM_ABAS[(atual - 1 + ORDEM_ABAS.length) % ORDEM_ABAS.length];
  } else if (evento.key === "Home") {
    proxima = ORDEM_ABAS[0];
  } else if (evento.key === "End") {
    proxima = ORDEM_ABAS[ORDEM_ABAS.length - 1];
  }
  if (proxima) {
    evento.preventDefault();
    mostrarAba(proxima, true);
    abas[proxima].focus();
  }
});

abas.entrar.addEventListener("click", function () {
  mostrarAba("entrar", true);
});

abas.cadastro.addEventListener("click", function () {
  mostrarAba("cadastro", true);
});

// botões "Criar conta" e "Entrar" do pé de cada caixa
document.querySelectorAll("[data-ir-para]").forEach(function (botao) {
  botao.addEventListener("click", function () {
    const destino = botao.dataset.irPara;
    mostrarAba(destino, true);
    abas[destino].focus();
  });
});

window.addEventListener("hashchange", function () {
  mostrarAba(abaDoEndereco(), false);
});

// ---------- mostrar senha ----------

botaoMostrarSenha.addEventListener("click", function () {
  const mostrando = campoSenhaEntrar.type === "text";
  campoSenhaEntrar.type = mostrando ? "password" : "text";
  botaoMostrarSenha.textContent = mostrando ? "Mostrar" : "Esconder";
  botaoMostrarSenha.setAttribute("aria-pressed", mostrando ? "false" : "true");
});

// ---------- validação ----------

// guarda o primeiro campo com erro pra receber o foco
function criarValidador(formulario) {
  limparErros(formulario);
  const validador = {
    primeiro: null,
    erro: function (campo, texto) {
      marcarErro(campo, texto);
      if (!validador.primeiro) {
        validador.primeiro = campo;
      }
    },
    ok: function () {
      if (validador.primeiro) {
        validador.primeiro.focus();
        return false;
      }
      return true;
    }
  };
  return validador;
}

function validarEntrar() {
  const v = criarValidador(formEntrar);
  if (!FORMATO_EMAIL_LOGIN.test(campoEmailEntrar.value.trim())) {
    v.erro(campoEmailEntrar, "Informe um e-mail válido.");
  }
  if (campoSenhaEntrar.value.length < 6) {
    v.erro(campoSenhaEntrar, "A senha precisa ter pelo menos 6 caracteres.");
  }
  return v.ok();
}

function validarCadastro() {
  const v = criarValidador(formCadastro);
  if (campoNomeCadastro.value.trim().length < 3) {
    v.erro(campoNomeCadastro, "Informe seu nome.");
  }
  if (!FORMATO_EMAIL_LOGIN.test(campoEmailCadastro.value.trim())) {
    v.erro(campoEmailCadastro, "Informe um e-mail válido.");
  }
  if (!campoBairroCadastro.value) {
    v.erro(campoBairroCadastro, "Escolha seu bairro.");
  }
  if (campoSenhaCadastro.value.length < 6) {
    v.erro(campoSenhaCadastro, "A senha precisa ter pelo menos 6 caracteres.");
  }
  if (campoConfirmarCadastro.value !== campoSenhaCadastro.value) {
    v.erro(campoConfirmarCadastro, "As senhas não são iguais.");
  }
  return v.ok();
}

// ---------- envio ----------

function entrarNoSistema(usuario) {
  guardarUsuarioNaSessao(usuario.id, usuario.nome, usuario.tipo);
  location.href = "painel.html";
}

async function enviarEntrar(evento) {
  evento.preventDefault();
  mostrarMensagem(mensagemEntrar, "");
  if (!validarEntrar()) {
    return;
  }

  botaoEntrar.disabled = true;
  botaoEntrar.textContent = "Entrando...";
  try {
    const usuario = await chamarApi("/login", "POST", {
      email: campoEmailEntrar.value.trim(),
      senha: campoSenhaEntrar.value
    });
    entrarNoSistema(usuario);
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemEntrar, erro.message, "erro");
    }
    botaoEntrar.disabled = false;
    botaoEntrar.textContent = "Entrar";
  }
}

async function enviarCadastro(evento) {
  evento.preventDefault();
  mostrarMensagem(mensagemCadastro, "");
  if (!validarCadastro()) {
    return;
  }

  botaoCadastrar.disabled = true;
  botaoCadastrar.textContent = "Criando conta...";
  try {
    // cadastro aberto é sempre morador; conta de agente só a prefeitura cria
    const usuario = await chamarApi("/usuarios", "POST", {
      nome: campoNomeCadastro.value.trim(),
      email: campoEmailCadastro.value.trim(),
      idBairro: Number(campoBairroCadastro.value),
      senha: campoSenhaCadastro.value,
      tipo: "CIDADAO"
    });
    mostrarMensagem(mensagemCadastro, "Conta criada. Entrando...", "sucesso");
    setTimeout(function () {
      entrarNoSistema(usuario);
    }, TEMPO_ANTES_DE_ENTRAR);
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemCadastro, erro.message, "erro");
    }
    botaoCadastrar.disabled = false;
    botaoCadastrar.textContent = "Criar conta";
  }
}

async function carregarBairrosCadastro() {
  try {
    const bairros = await chamarApi("/bairros");
    preencherSelect(campoBairroCadastro, bairros, "Escolha seu bairro");
  } catch (erro) {
    preencherSelect(campoBairroCadastro, [], "Indisponível");
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemCadastro, erro.message, "erro");
    }
  }
}

formEntrar.addEventListener("submit", enviarEntrar);
formCadastro.addEventListener("submit", enviarCadastro);
limparErroAoCorrigir(formEntrar);
limparErroAoCorrigir(formCadastro);

// ---------- início ----------

mostrarAba(abaDoEndereco(), false);
carregarBairrosCadastro();

// "Contas para testar" só aparece com os dados provisórios
if (typeof USAR_DADOS_PROVISORIOS !== "undefined" && USAR_DADOS_PROVISORIOS) {
  document.getElementById("contas-teste").hidden = false;
}
