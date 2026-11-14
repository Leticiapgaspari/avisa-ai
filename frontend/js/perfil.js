// Perfil: dados da conta, troca de senha e os avisos da pessoa.

const FORMATO_EMAIL_PERFIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const NOMES_TIPO_CONTA = { CIDADAO: "Morador", AGENTE: "Agente" };

const formDados = document.getElementById("form-dados");
const camposDados = document.getElementById("campos-dados");
const campoNome = document.getElementById("perfil-nome");
const campoEmail = document.getElementById("perfil-email");
const campoBairro = document.getElementById("perfil-bairro");
const botaoSalvarDados = document.getElementById("botao-salvar-dados");
const mensagemDados = document.getElementById("mensagem-dados");

const formSenha = document.getElementById("form-senha");
const campoSenhaAtual = document.getElementById("senha-atual");
const campoSenhaNova = document.getElementById("senha-nova");
const campoSenhaConfirmar = document.getElementById("senha-confirmar");
const botaoTrocarSenha = document.getElementById("botao-trocar-senha");
const mensagemSenha = document.getElementById("mensagem-senha");

const areaAvisos = document.getElementById("meus-avisos");

let dadosConta = null;     // último usuário recebido do back
let valoresSalvos = null;  // { nome, email, bairro } para saber se algo mudou

// ---------- seus dados ----------

function nomeDoTipo(tipo) {
  return NOMES_TIPO_CONTA[tipo] || tipo;
}

function mostrarIdentidade(conta) {
  document.getElementById("perfil-inicial").textContent = String(conta.nome || "?").trim().charAt(0).toUpperCase();
  document.getElementById("perfil-nome-completo").textContent = conta.nome;

  const etiqueta = document.getElementById("perfil-tipo");
  etiqueta.textContent = nomeDoTipo(conta.tipo);
  etiqueta.classList.toggle("agente", conta.tipo === "AGENTE");

  document.getElementById("perfil-tipo-conta").textContent =
    "Tipo de conta: " + nomeDoTipo(conta.tipo) + ". Só a equipe da prefeitura pode mudar o tipo de uma conta.";
}

function lerValoresDoFormulario() {
  return {
    nome: campoNome.value.trim(),
    email: campoEmail.value.trim(),
    bairro: campoBairro.value
  };
}

function preencherFormulario(conta) {
  campoNome.value = conta.nome;
  campoEmail.value = conta.email;
  campoBairro.value = conta.bairro ? String(conta.bairro.id) : "";
  valoresSalvos = lerValoresDoFormulario();
  botaoSalvarDados.disabled = true;
}

// só habilita o Salvar quando algo mudou
function atualizarBotaoSalvar() {
  if (!valoresSalvos) {
    return;
  }
  const atuais = lerValoresDoFormulario();
  botaoSalvarDados.disabled =
    atuais.nome === valoresSalvos.nome &&
    atuais.email === valoresSalvos.email &&
    atuais.bairro === valoresSalvos.bairro;
}

// mesmas regras do cadastro
function validarDados() {
  limparErros(formDados);
  let primeiroInvalido = null;

  function erro(campo, texto) {
    marcarErro(campo, texto);
    if (!primeiroInvalido) {
      primeiroInvalido = campo;
    }
  }

  if (campoNome.value.trim().length < 3) {
    erro(campoNome, "Informe seu nome.");
  }
  if (!FORMATO_EMAIL_PERFIL.test(campoEmail.value.trim())) {
    erro(campoEmail, "Informe um e-mail válido.");
  }
  if (!campoBairro.value) {
    erro(campoBairro, "Escolha seu bairro.");
  }

  if (primeiroInvalido) {
    primeiroInvalido.focus();
    return false;
  }
  return true;
}

async function salvarDados(evento) {
  evento.preventDefault();
  mostrarMensagem(mensagemDados, "");
  if (!validarDados()) {
    return;
  }

  botaoSalvarDados.disabled = true;
  try {
    // o tipo vai igual, ninguém muda o próprio tipo
    const conta = await chamarApi("/usuarios" + montarQuery({ id: dadosConta.id }), "PUT", {
      nome: campoNome.value.trim(),
      email: campoEmail.value.trim(),
      idBairro: Number(campoBairro.value),
      tipo: dadosConta.tipo
    });
    dadosConta = conta;

    // atualiza a sessão pro cabeçalho mudar na hora
    guardarUsuarioNaSessao(conta.id, conta.nome, conta.tipo);
    preencherAreaUsuario();
    const emailNoMenu = document.getElementById("conta-email");
    if (emailNoMenu) {
      emailNoMenu.textContent = conta.email;
    }

    mostrarIdentidade(conta);
    preencherFormulario(conta);
    mostrarMensagem(mensagemDados, "Pronto, seus dados foram atualizados.", "sucesso");
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemDados, erro.message, "erro");
    }
    atualizarBotaoSalvar();
  }
}

async function carregarDados(usuario) {
  try {
    const respostas = await Promise.all([
      chamarApi("/usuarios" + montarQuery({ id: usuario.id })),
      chamarApi("/bairros")
    ]);
    dadosConta = respostas[0];
    preencherSelect(campoBairro, respostas[1], "Escolha seu bairro");
    mostrarIdentidade(dadosConta);
    preencherFormulario(dadosConta);
    camposDados.disabled = false;
  } catch (erro) {
    document.getElementById("perfil-nome-completo").textContent = primeiroNome(usuario.nome);
    preencherSelect(campoBairro, [], "Não foi possível carregar os bairros");
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemDados, erro.message, "erro");
    }
  }
}

// ---------- trocar senha ----------

// botões "Mostrar" dos campos de senha
function configurarBotoesMostrar() {
  document.querySelectorAll(".botao-mostrar").forEach(function (botao) {
    const campo = document.getElementById(botao.getAttribute("aria-controls"));
    botao.addEventListener("click", function () {
      const mostrando = campo.type === "text";
      campo.type = mostrando ? "password" : "text";
      botao.textContent = mostrando ? "Mostrar" : "Esconder";
      botao.setAttribute("aria-pressed", mostrando ? "false" : "true");
    });
  });
}

function esconderSenhas() {
  document.querySelectorAll(".botao-mostrar").forEach(function (botao) {
    document.getElementById(botao.getAttribute("aria-controls")).type = "password";
    botao.textContent = "Mostrar";
    botao.setAttribute("aria-pressed", "false");
  });
}

function validarSenha() {
  limparErros(formSenha);
  let primeiroInvalido = null;

  function erro(campo, texto) {
    marcarErro(campo, texto);
    if (!primeiroInvalido) {
      primeiroInvalido = campo;
    }
  }

  if (!campoSenhaAtual.value) {
    erro(campoSenhaAtual, "Informe a senha atual.");
  }
  if (campoSenhaNova.value.length < 6) {
    erro(campoSenhaNova, "A nova senha precisa ter pelo menos 6 caracteres.");
  } else if (campoSenhaNova.value === campoSenhaAtual.value) {
    erro(campoSenhaNova, "A nova senha precisa ser diferente da atual.");
  }
  if (campoSenhaConfirmar.value !== campoSenhaNova.value) {
    erro(campoSenhaConfirmar, "As senhas não são iguais.");
  }

  if (primeiroInvalido) {
    primeiroInvalido.focus();
    return false;
  }
  return true;
}

async function trocarSenha(evento) {
  evento.preventDefault();
  mostrarMensagem(mensagemSenha, "");
  if (!validarSenha()) {
    return;
  }

  botaoTrocarSenha.disabled = true;
  try {
    await chamarApi("/usuarios/senha" + montarQuery({ id: usuarioAtual.id }), "PUT", {
      senhaAtual: campoSenhaAtual.value,
      novaSenha: campoSenhaNova.value
    });
    formSenha.reset();
    esconderSenhas();
    mostrarMensagem(mensagemSenha, "Senha trocada. Use a nova no próximo acesso.", "sucesso");
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemSenha, erro.message, "erro");
    }
    if (erro.message === "A senha atual não confere.") {
      campoSenhaAtual.focus();
    }
  } finally {
    botaoTrocarSenha.disabled = false;
  }
}

// ---------- seus avisos ----------

function textoApoiosPerfil(total) {
  return total === 1 ? "1 apoio" : total + " apoios";
}

async function carregarMeusAvisos(usuario) {
  try {
    const todas = await chamarApi("/ocorrencias");
    const minhas = todas
      .filter(function (o) {
        return o.autor && o.autor.id === usuario.id;
      })
      .sort(function (a, b) {
        return String(b.dataAbertura).localeCompare(String(a.dataAbertura)) || b.id - a.id;
      });

    if (minhas.length === 0) {
      areaAvisos.innerHTML =
        '<div class="perfil-vazio">' +
        "<p>Você ainda não registrou nenhum aviso. Quando vir algum problema na rua, é só registrar.</p>" +
        '<a href="ocorrencias.html#registrar" class="botao botao-cone">Registrar ocorrência</a>' +
        "</div>";
      return;
    }

    areaAvisos.innerHTML = '<ul class="perfil-avisos">' + minhas.map(function (o) {
      const bairro = o.bairro ? o.bairro.nome : "";
      const detalhes = [bairro, formatarData(o.dataAbertura)].filter(Boolean).join(", ");
      const link = "ocorrencias.html" + montarQuery({ bairro: o.bairro ? o.bairro.id : "" });
      return (
        "<li>" +
        '<a href="' + link + '">' +
        '<span class="perfil-aviso-texto">' +
        "<strong>" + escaparHtml(o.titulo) + "</strong>" +
        '<span class="texto-suave">' + escaparHtml(detalhes) + "</span>" +
        "</span>" +
        '<span class="perfil-aviso-lado">' +
        etiquetaStatus(o.status) +
        '<span class="texto-suave">' + textoApoiosPerfil(o.totalApoios) + "</span>" +
        "</span>" +
        "</a>" +
        "</li>"
      );
    }).join("") + "</ul>";
  } catch (erro) {
    areaAvisos.innerHTML = avisarSeSemConexao(erro)
      ? ""
      : '<p class="lista-estado erro">' + escaparHtml(erro.message) + "</p>";
  }
}

// ---------- início ----------

function iniciarPerfil() {
  const usuario = lerUsuarioDaSessao();
  // sem login o sessao.js já redireciona
  if (!usuario) {
    return;
  }

  formDados.addEventListener("submit", salvarDados);
  formDados.addEventListener("input", atualizarBotaoSalvar);
  formDados.addEventListener("change", atualizarBotaoSalvar);
  limparErroAoCorrigir(formDados);

  formSenha.addEventListener("submit", trocarSenha);
  limparErroAoCorrigir(formSenha);
  configurarBotoesMostrar();

  carregarDados(usuario);
  carregarMeusAvisos(usuario);
}

iniciarPerfil();
