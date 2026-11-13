// Usuários: cadastro, edição e exclusão de moradores e agentes.

const formUsuario = document.getElementById("form-usuario");
const tituloFormulario = document.getElementById("titulo-formulario");
const botaoEnviar = document.getElementById("botao-enviar");
const botaoCancelar = document.getElementById("botao-cancelar");
const mensagemFormulario = document.getElementById("mensagem-formulario");

const campoNome = document.getElementById("nome");
const campoEmail = document.getElementById("email");
const campoTipo = document.getElementById("tipo");
const campoBairro = document.getElementById("bairro");

const corpoUsuarios = document.getElementById("corpo-usuarios");
const mensagemLista = document.getElementById("mensagem-lista");
const totalLista = document.getElementById("total-lista");

const FORMATO_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

let usuarios = [];
let idEmEdicao = null;
let idConfirmandoExclusao = null;
let mensagemLinha = null; // { id, texto, tipo }

// ---------- carregamento ----------

async function carregarBairros() {
  try {
    const bairros = await chamarApi("/bairros");
    preencherSelect(campoBairro, bairros, "Escolha um bairro");
  } catch (erro) {
    preencherSelect(campoBairro, [], "Indisponível");
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemFormulario, erro.message, "erro");
    }
  }
}

async function carregarUsuarios() {
  corpoUsuarios.innerHTML = '<tr><td colspan="5" class="lista-estado">Carregando...</td></tr>';
  totalLista.textContent = "";
  idConfirmandoExclusao = null;
  try {
    usuarios = await chamarApi("/usuarios");
    desenharTabela();
  } catch (erro) {
    usuarios = [];
    corpoUsuarios.innerHTML = avisarSeSemConexao(erro)
      ? ""
      : '<tr><td colspan="5" class="lista-estado erro">' + escaparHtml(erro.message) + "</td></tr>";
  }
}

// ---------- tabela ----------

function buscarUsuario(id) {
  return usuarios.find(function (u) {
    return u.id === id;
  });
}

function montarAcoes(u) {
  if (idConfirmandoExclusao === u.id) {
    return (
      '<div class="confirmacao" role="group" aria-label="Confirmar exclusão">' +
      "<span>Excluir este usuário?</span>" +
      '<button type="button" class="botao botao-perigo-cheio" data-acao="confirmar-exclusao">Excluir</button>' +
      '<button type="button" class="botao botao-leve" data-acao="cancelar-exclusao">Cancelar</button>' +
      "</div>"
    );
  }
  return (
    '<div class="acoes-linha">' +
    '<button type="button" class="botao botao-leve" data-acao="editar">Editar</button>' +
    '<button type="button" class="botao botao-leve-perigo" data-acao="excluir">Excluir</button>' +
    "</div>"
  );
}

function montarLinha(u) {
  const classeTipo = u.tipo === "AGENTE" ? "etiqueta-tipo agente" : "etiqueta-tipo";
  let linha =
    '<tr data-id="' + u.id + '"' + (u.id === idEmEdicao ? ' class="em-edicao"' : "") + ">" +
    '<td data-label="Nome"><strong>' + escaparHtml(u.nome) + "</strong></td>" +
    '<td data-label="E-mail">' + escaparHtml(u.email) + "</td>" +
    '<td data-label="Tipo"><span class="' + classeTipo + '">' + escaparHtml(NOMES_TIPO[u.tipo] || u.tipo) + "</span></td>" +
    '<td data-label="Bairro">' + escaparHtml(u.bairro ? u.bairro.nome : "") + "</td>" +
    "<td>" + montarAcoes(u);

  if (mensagemLinha && mensagemLinha.id === u.id) {
    const classe = mensagemLinha.tipo === "erro" ? "mensagem-erro" : "mensagem-sucesso";
    linha += '<p class="mensagem ' + classe + '" role="status">' + escaparHtml(mensagemLinha.texto) + "</p>";
  }

  return linha + "</td></tr>";
}

function desenharTabela() {
  if (usuarios.length === 0) {
    corpoUsuarios.innerHTML = '<tr><td colspan="5" class="lista-vazia">Nenhum usuário cadastrado.</td></tr>';
    totalLista.textContent = "";
    return;
  }
  corpoUsuarios.innerHTML = usuarios.map(montarLinha).join("");
  totalLista.textContent = usuarios.length === 1 ? "1 usuário" : usuarios.length + " usuários";
}

function focarNaLinha(id, seletor) {
  const elemento = corpoUsuarios.querySelector('[data-id="' + id + '"] ' + seletor);
  if (elemento) {
    elemento.focus();
  }
}

// ---------- exclusão ----------

async function confirmarExclusao(id) {
  try {
    await chamarApi("/usuarios" + montarQuery({ id: id }), "DELETE");

    // excluiu a própria conta: sai do sistema
    if (temUsuario() && usuarioAtual.id === id) {
      limparUsuarioDaSessao();
      location.reload();
      return;
    }

    if (idEmEdicao === id) {
      sairDoModoEdicao();
    }
    idConfirmandoExclusao = null;
    mensagemLinha = null;
    usuarios = usuarios.filter(function (u) {
      return u.id !== id;
    });
    desenharTabela();
    mostrarMensagem(mensagemLista, "Usuário excluído.", "sucesso");
    preencherAreaUsuario();
  } catch (erro) {
    // ex.: usuário com ocorrências (a mensagem vem do back)
    idConfirmandoExclusao = null;
    mensagemLinha = avisarSeSemConexao(erro) ? null : { id: id, texto: erro.message, tipo: "erro" };
    desenharTabela();
  }
}

// ---------- formulário ----------

function entrarNoModoEdicao(id) {
  const u = buscarUsuario(id);
  idEmEdicao = id;
  mensagemLinha = null;

  campoNome.value = u.nome;
  campoEmail.value = u.email;
  campoTipo.value = u.tipo;
  campoBairro.value = u.bairro ? String(u.bairro.id) : "";

  limparErros(formUsuario);
  mostrarMensagem(mensagemFormulario, "");
  tituloFormulario.textContent = "Editar usuário";
  botaoEnviar.textContent = "Salvar alterações";
  botaoCancelar.hidden = false;

  desenharTabela();
  formUsuario.scrollIntoView({ behavior: "smooth", block: "start" });
  campoNome.focus({ preventScroll: true });
}

function sairDoModoEdicao() {
  idEmEdicao = null;
  formUsuario.reset();
  limparErros(formUsuario);
  tituloFormulario.textContent = "Cadastrar usuário";
  botaoEnviar.textContent = "Cadastrar";
  botaoCancelar.hidden = true;
}

function validarFormulario() {
  limparErros(formUsuario);
  let primeiroInvalido = null;

  function erro(campo, texto) {
    marcarErro(campo, texto);
    if (!primeiroInvalido) {
      primeiroInvalido = campo;
    }
  }

  const nome = campoNome.value.trim();
  if (nome.length < 3) {
    erro(campoNome, "Informe um nome com pelo menos 3 caracteres.");
  } else if (nome.length > 100) {
    erro(campoNome, "O nome pode ter no máximo 100 caracteres.");
  }

  const email = campoEmail.value.trim();
  if (!FORMATO_EMAIL.test(email)) {
    erro(campoEmail, "Informe um e-mail válido, como nome@exemplo.com.");
  } else if (email.length > 120) {
    erro(campoEmail, "O e-mail pode ter no máximo 120 caracteres.");
  }

  if (!campoTipo.value) {
    erro(campoTipo, "Escolha o tipo de usuário.");
  }

  if (!campoBairro.value) {
    erro(campoBairro, "Escolha um bairro.");
  }

  if (primeiroInvalido) {
    primeiroInvalido.focus();
    return false;
  }
  return true;
}

async function enviarFormulario(evento) {
  evento.preventDefault();
  mostrarMensagem(mensagemFormulario, "");
  if (!validarFormulario()) {
    return;
  }

  const dados = {
    nome: campoNome.value.trim(),
    email: campoEmail.value.trim(),
    tipo: campoTipo.value,
    idBairro: Number(campoBairro.value)
  };

  botaoEnviar.disabled = true;
  try {
    if (idEmEdicao === null) {
      await chamarApi("/usuarios", "POST", dados);
      sairDoModoEdicao();
      mostrarMensagem(mensagemFormulario, "Usuário cadastrado.", "sucesso");
    } else {
      const idEditado = idEmEdicao;
      await chamarApi("/usuarios" + montarQuery({ id: idEditado }), "PUT", dados);
      sairDoModoEdicao();
      mostrarMensagem(mensagemFormulario, "Usuário atualizado.", "sucesso");

      // editou a própria conta: atualiza a sessão
      if (temUsuario() && usuarioAtual.id === idEditado) {
        guardarUsuarioNaSessao(idEditado, dados.nome, dados.tipo);
        if (usuarioAtual.tipo !== dados.tipo) {
          location.reload(); // o tipo mudou: as permissões da tela mudam também
          return;
        }
      }
    }
    mostrarMensagem(mensagemLista, "");
    carregarUsuarios();
    preencherAreaUsuario();
  } catch (erro) {
    // ex.: e-mail repetido (a mensagem vem do back)
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemFormulario, erro.message, "erro");
    }
  } finally {
    botaoEnviar.disabled = false;
  }
}

// ---------- eventos ----------

formUsuario.addEventListener("submit", enviarFormulario);

botaoCancelar.addEventListener("click", function () {
  sairDoModoEdicao();
  mostrarMensagem(mensagemFormulario, "");
  desenharTabela();
});

limparErroAoCorrigir(formUsuario);

corpoUsuarios.addEventListener("click", function (evento) {
  const botao = evento.target.closest("button[data-acao]");
  if (!botao) {
    return;
  }
  const id = Number(botao.closest("[data-id]").dataset.id);
  const acao = botao.dataset.acao;

  if (acao === "editar") {
    entrarNoModoEdicao(id);
  } else if (acao === "excluir") {
    idConfirmandoExclusao = id;
    mensagemLinha = null;
    desenharTabela();
    focarNaLinha(id, "[data-acao='cancelar-exclusao']");
  } else if (acao === "cancelar-exclusao") {
    idConfirmandoExclusao = null;
    desenharTabela();
    focarNaLinha(id, "[data-acao='excluir']");
  } else if (acao === "confirmar-exclusao") {
    confirmarExclusao(id);
  }
});

// ---------- início ----------

carregarBairros();
carregarUsuarios();
