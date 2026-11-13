// Ocorrências: registrar e editar (mesmo formulário), filtros, "Também vi isso",
// mudança de status (agente) e exclusão.

const formOcorrencia = document.getElementById("form-ocorrencia");
const tituloFormulario = document.getElementById("titulo-formulario");
const botaoEnviar = document.getElementById("botao-enviar");
const botaoCancelar = document.getElementById("botao-cancelar");
const mensagemFormulario = document.getElementById("mensagem-formulario");
const contadorDescricao = document.getElementById("contador-descricao");

const campoTitulo = document.getElementById("titulo");
const campoDescricao = document.getElementById("descricao");
const campoEndereco = document.getElementById("endereco");
const campoCategoria = document.getElementById("categoria");
const campoBairro = document.getElementById("bairro");

const filtroBairro = document.getElementById("filtro-bairro");
const filtroStatus = document.getElementById("filtro-status");
const listaOcorrencias = document.getElementById("lista-ocorrencias");
const mensagemLista = document.getElementById("mensagem-lista");
const totalLista = document.getElementById("total-lista");

let ocorrencias = [];               // última lista recebida do back
let idEmEdicao = null;              // id da ocorrência sendo editada (null = registrando)
let idConfirmandoExclusao = null;   // cartão que está mostrando "Excluir mesmo?"
let mensagemCartao = null;          // { id, texto, tipo } mostrada dentro de um cartão
const apoiadasNestaSessao = [];     // ids que o usuário atual apoiou nesta visita

// ---------- carregamento ----------

async function carregarListasDoFormulario() {
  try {
    const categorias = await chamarApi("/categorias");
    const bairros = await chamarApi("/bairros");
    preencherSelect(campoCategoria, categorias, "Escolha uma categoria");
    preencherSelect(campoBairro, bairros, "Escolha um bairro");
    preencherSelect(filtroBairro, bairros, "Todos");
  } catch (erro) {
    preencherSelect(campoCategoria, [], "Indisponível");
    preencherSelect(campoBairro, [], "Indisponível");
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemFormulario, erro.message, "erro");
    }
  }
}

async function carregarOcorrencias() {
  listaOcorrencias.innerHTML = '<li class="lista-estado">Carregando...</li>';
  totalLista.textContent = "";
  idConfirmandoExclusao = null;
  mensagemCartao = null;
  marcarBotoesStatus();

  const query = montarQuery({ bairro: filtroBairro.value, status: filtroStatus.value });
  try {
    ocorrencias = await chamarApi("/ocorrencias" + query);
    desenharLista();
    atualizarContagens();
  } catch (erro) {
    ocorrencias = [];
    listaOcorrencias.innerHTML = avisarSeSemConexao(erro)
      ? ""
      : '<li class="lista-estado erro">' + escaparHtml(erro.message) + "</li>";
  }
}

// ---------- listagem ----------

function buscarOcorrencia(id) {
  return ocorrencias.find(function (o) {
    return o.id === id;
  });
}

function textoApoios(total) {
  return total === 1 ? "1 apoio" : total + " apoios";
}

function permissoesDoCartao(o) {
  const ehMinha = temUsuario() && o.autor && o.autor.id === usuarioAtual.id;
  return {
    ehMinha: ehMinha,
    // só morador apoia, e não nas próprias nem nas resolvidas
    podeApoiar: o.status !== "RESOLVIDA" && !ehAgente() && !ehMinha,
    // agente edita todas; morador só as próprias
    podeAlterar: ehAgente() || ehMinha
  };
}

// coluna dos apoios: quando a pessoa pode apoiar, vira o botão
function montarColunaApoios(o, permissoes) {
  const numero = '<span class="apoios-numero">' + o.totalApoios + "</span>";
  const palavra = o.totalApoios === 1 ? "apoio" : "apoios";

  if (!permissoes.podeApoiar) {
    return (
      '<div class="cartao-apoios">' + numero +
      '<span class="apoios-texto">' + palavra + "</span></div>"
    );
  }

  // o leitor de tela lê "3 apoios Também vi isso"
  const palavraOculta = '<span class="visualmente-oculto"> ' + palavra + " </span>";

  if (!temUsuario()) {
    return (
      '<button type="button" class="cartao-apoios apoiar" disabled title="' + AVISO_SEM_USUARIO + '">' +
      numero + palavraOculta +
      '<span class="apoios-texto">Também vi isso</span></button>'
    );
  }

  const apoiada = apoiadasNestaSessao.includes(o.id);
  return (
    '<button type="button" class="cartao-apoios apoiar' + (apoiada ? " apoiada" : "") + '" data-acao="apoiar"' +
    ' aria-pressed="' + (apoiada ? "true" : "false") + '">' +
    numero + palavraOculta +
    '<span class="apoios-texto">' + (apoiada ? "Apoiado" : "Também vi isso") + "</span></button>"
  );
}

// rodapé do cartão: a confirmação de exclusão ou os botões de ação
function montarRodape(o, permissoes) {
  if (idConfirmandoExclusao === o.id) {
    return (
      '<div class="confirmacao" role="group" aria-label="Confirmar exclusão">' +
      "<span>Excluir esta ocorrência?</span>" +
      '<button type="button" class="botao botao-perigo-cheio" data-acao="confirmar-exclusao">Excluir</button>' +
      '<button type="button" class="botao botao-leve" data-acao="cancelar-exclusao">Cancelar</button>' +
      "</div>"
    );
  }

  let html = "";

  // só agente muda o status
  if (ehAgente()) {
    html += '<div class="controle-status">' +
      '<label for="status-' + o.id + '">Mudar status</label>' +
      '<select id="status-' + o.id + '" data-acao="status">';
    for (const valor in NOMES_STATUS) {
      const marcado = valor === o.status ? " selected" : "";
      html += '<option value="' + valor + '"' + marcado + ">" + NOMES_STATUS[valor] + "</option>";
    }
    html += "</select></div>";
  }

  if (permissoes.podeApoiar && temUsuario() && apoiadasNestaSessao.includes(o.id)) {
    html += '<button type="button" class="botao botao-leve" data-acao="retirar-apoio">Retirar apoio</button>';
  }

  if (!temUsuario()) {
    html += '<button type="button" class="botao botao-leve" disabled title="' + AVISO_SEM_USUARIO + '">Editar</button>';
    html += '<button type="button" class="botao botao-leve-perigo" disabled title="' + AVISO_SEM_USUARIO + '">Excluir</button>';
  } else if (permissoes.podeAlterar) {
    html += '<button type="button" class="botao botao-leve" data-acao="editar">Editar</button>';
    html += '<button type="button" class="botao botao-leve-perigo" data-acao="excluir">Excluir</button>';
  }

  return html ? '<div class="cartao-rodape">' + html + "</div>" : "";
}

function montarCartao(o) {
  const permissoes = permissoesDoCartao(o);
  const classes = ["cartao-ocorrencia", "status-" + (CLASSES_STATUS[o.status] || "")];
  if (o.id === idEmEdicao) {
    classes.push("em-edicao");
  }

  let mensagem = "";
  if (mensagemCartao && mensagemCartao.id === o.id) {
    const classe = mensagemCartao.tipo === "erro" ? "mensagem-erro" : "mensagem-sucesso";
    mensagem = '<p class="mensagem ' + classe + '" role="status">' + escaparHtml(mensagemCartao.texto) + "</p>";
  }

  const local = [o.bairro ? o.bairro.nome : "", o.categoria ? o.categoria.nome : ""]
    .filter(Boolean)
    .join(", ");
  const data = o.dataAbertura ? ", em " + formatarData(o.dataAbertura) : "";
  const descricao = o.descricao ? '<p class="cartao-descricao">' + escaparHtml(o.descricao) + "</p>" : "";
  const autor = o.autor ? '<p class="cartao-autor">Registrado por ' + escaparHtml(o.autor.nome) + "</p>" : "";

  return (
    '<li class="' + classes.join(" ") + '" data-id="' + o.id + '">' +
    "<article>" +
    '<div class="cartao-grade">' +
    montarColunaApoios(o, permissoes) +
    '<div class="cartao-corpo">' +
    '<div class="cartao-topo">' +
    "<h3>" + escaparHtml(o.titulo) + "</h3>" +
    etiquetaStatus(o.status) +
    "</div>" +
    '<p class="cartao-meta">' + escaparHtml(local) + "</p>" +
    '<p class="cartao-meta">' + escaparHtml(o.endereco) + data + "</p>" +
    descricao +
    autor +
    "</div>" +
    "</div>" +
    montarRodape(o, permissoes) +
    mensagem +
    "</article>" +
    "</li>"
  );
}

function desenharLista() {
  if (ocorrencias.length === 0) {
    listaOcorrencias.innerHTML = '<li class="lista-vazia">Nenhuma ocorrência com esses filtros. Tente outro bairro ou status.</li>';
    totalLista.textContent = "";
    return;
  }
  listaOcorrencias.innerHTML = ocorrencias.map(montarCartao).join("");
  totalLista.textContent = ocorrencias.length === 1 ? "1 ocorrência" : ocorrencias.length + " ocorrências";
}

function focarNoCartao(id, seletor) {
  const elemento = listaOcorrencias.querySelector('[data-id="' + id + '"] ' + seletor);
  if (elemento) {
    elemento.focus();
  }
}

// ---------- ações dos cartões ----------

async function apoiar(id) {
  const o = buscarOcorrencia(id);
  try {
    await chamarApi("/apoios", "POST", { idUsuario: usuarioAtual.id, idOcorrencia: id });
    o.totalApoios = o.totalApoios + 1;
    if (!apoiadasNestaSessao.includes(id)) {
      apoiadasNestaSessao.push(id);
    }
    mensagemCartao = { id: id, texto: "Apoio registrado. Obrigado por confirmar.", tipo: "sucesso" };
  } catch (erro) {
    mensagemCartao = avisarSeSemConexao(erro) ? null : { id: id, texto: erro.message, tipo: "erro" };
  }
  desenharLista();
  focarNoCartao(id, "[data-acao='apoiar']");
}

async function retirarApoio(id) {
  const o = buscarOcorrencia(id);
  try {
    await chamarApi("/apoios" + montarQuery({ usuario: usuarioAtual.id, ocorrencia: id }), "DELETE");
    o.totalApoios = Math.max(0, o.totalApoios - 1);
    apoiadasNestaSessao.splice(apoiadasNestaSessao.indexOf(id), 1);
    mensagemCartao = { id: id, texto: "Apoio retirado.", tipo: "sucesso" };
  } catch (erro) {
    mensagemCartao = avisarSeSemConexao(erro) ? null : { id: id, texto: erro.message, tipo: "erro" };
  }
  desenharLista();
  focarNoCartao(id, "[data-acao='apoiar']");
}

async function mudarStatus(id, novoStatus) {
  const o = buscarOcorrencia(id);
  try {
    await chamarApi("/ocorrencias/status" + montarQuery({ id: id, usuario: usuarioAtual.id }), "PUT", { status: novoStatus });
    o.status = novoStatus;
    mensagemCartao = { id: id, texto: "Status alterado para " + NOMES_STATUS[novoStatus] + ".", tipo: "sucesso" };
    atualizarContagens();
  } catch (erro) {
    // se der erro, a lista volta ao status anterior
    mensagemCartao = avisarSeSemConexao(erro) ? null : { id: id, texto: erro.message, tipo: "erro" };
  }
  desenharLista();
  focarNoCartao(id, "select");
}

function pedirConfirmacaoExclusao(id) {
  idConfirmandoExclusao = id;
  mensagemCartao = null;
  desenharLista();
  focarNoCartao(id, "[data-acao='cancelar-exclusao']");
}

function cancelarExclusao(id) {
  idConfirmandoExclusao = null;
  desenharLista();
  focarNoCartao(id, "[data-acao='excluir']");
}

async function confirmarExclusao(id) {
  try {
    await chamarApi("/ocorrencias" + montarQuery({ id: id, usuario: usuarioAtual.id }), "DELETE");
    idConfirmandoExclusao = null;
    if (idEmEdicao === id) {
      sairDoModoEdicao();
    }
    ocorrencias = ocorrencias.filter(function (o) {
      return o.id !== id;
    });
    desenharLista();
    atualizarContagens();
    mostrarMensagem(mensagemLista, "Ocorrência excluída.", "sucesso");
  } catch (erro) {
    idConfirmandoExclusao = null;
    mensagemCartao = avisarSeSemConexao(erro) ? null : { id: id, texto: erro.message, tipo: "erro" };
    desenharLista();
  }
}

// ---------- formulário: registrar e editar ----------

function atualizarContador() {
  contadorDescricao.textContent = campoDescricao.value.length + "/500";
}

function entrarNoModoEdicao(id) {
  const o = buscarOcorrencia(id);
  idEmEdicao = id;
  abrirFormulario();

  campoTitulo.value = o.titulo;
  campoDescricao.value = o.descricao || "";
  campoEndereco.value = o.endereco;
  campoCategoria.value = o.categoria ? String(o.categoria.id) : "";
  campoBairro.value = o.bairro ? String(o.bairro.id) : "";
  atualizarContador();

  limparErros(formOcorrencia);
  mostrarMensagem(mensagemFormulario, "");
  tituloFormulario.textContent = "Editar ocorrência";
  botaoEnviar.textContent = "Salvar alterações";
  botaoCancelar.hidden = false;

  desenharLista();
  formOcorrencia.scrollIntoView({ behavior: "smooth", block: "start" });
  campoTitulo.focus({ preventScroll: true });
}

function sairDoModoEdicao() {
  idEmEdicao = null;
  formOcorrencia.reset();
  limparErros(formOcorrencia);
  atualizarContador();
  tituloFormulario.textContent = "Registrar ocorrência";
  botaoEnviar.textContent = "Registrar";
  botaoCancelar.hidden = true;
}

function validarFormulario() {
  limparErros(formOcorrencia);
  let primeiroInvalido = null;

  function erro(campo, texto) {
    marcarErro(campo, texto);
    if (!primeiroInvalido) {
      primeiroInvalido = campo;
    }
  }

  const titulo = campoTitulo.value.trim();
  if (titulo.length < 5) {
    erro(campoTitulo, "Informe um título com pelo menos 5 caracteres.");
  } else if (titulo.length > 120) {
    erro(campoTitulo, "O título pode ter no máximo 120 caracteres.");
  }

  if (campoDescricao.value.trim().length > 500) {
    erro(campoDescricao, "A descrição pode ter no máximo 500 caracteres.");
  }

  const endereco = campoEndereco.value.trim();
  if (endereco.length === 0 || endereco.length > 200) {
    erro(campoEndereco, "Informe o endereço da ocorrência.");
  }

  if (!campoCategoria.value) {
    erro(campoCategoria, "Escolha uma categoria.");
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

  if (!temUsuario()) {
    mostrarMensagem(mensagemFormulario, AVISO_SEM_USUARIO + ".", "erro");
    return;
  }
  if (!validarFormulario()) {
    return;
  }

  const dados = {
    titulo: campoTitulo.value.trim(),
    descricao: campoDescricao.value.trim(),
    endereco: campoEndereco.value.trim(),
    idCategoria: Number(campoCategoria.value),
    idBairro: Number(campoBairro.value)
  };

  botaoEnviar.disabled = true;
  try {
    let texto;
    if (idEmEdicao === null) {
      dados.idUsuario = usuarioAtual.id;
      await chamarApi("/ocorrencias", "POST", dados);
      texto = "Pronto, sua ocorrência foi registrada.";
    } else {
      await chamarApi("/ocorrencias" + montarQuery({ id: idEmEdicao, usuario: usuarioAtual.id }), "PUT", dados);
      texto = "Ocorrência atualizada.";
    }
    sairDoModoEdicao();
    mostrarMensagem(mensagemFormulario, texto, "sucesso");
    mostrarMensagem(mensagemLista, "");
    carregarOcorrencias();
  } catch (erro) {
    if (!avisarSeSemConexao(erro)) {
      mostrarMensagem(mensagemFormulario, erro.message, "erro");
    }
  } finally {
    botaoEnviar.disabled = false;
  }
}

// ---------- eventos ----------

formOcorrencia.addEventListener("submit", enviarFormulario);

botaoCancelar.addEventListener("click", function () {
  sairDoModoEdicao();
  mostrarMensagem(mensagemFormulario, "");
  desenharLista();
});

campoDescricao.addEventListener("input", atualizarContador);

limparErroAoCorrigir(formOcorrencia);

filtroBairro.addEventListener("change", function () {
  mostrarMensagem(mensagemLista, "");
  carregarOcorrencias();
});

filtroStatus.addEventListener("change", function () {
  mostrarMensagem(mensagemLista, "");
  carregarOcorrencias();
});

// ---------- botões de filtro por status ----------
// os botões só mudam o #filtro-status (escondido) e disparam o "change" dele

const botoesStatus = document.querySelectorAll(".filtro-status");

function marcarBotoesStatus() {
  botoesStatus.forEach(function (botao) {
    botao.setAttribute("aria-pressed", botao.dataset.status === filtroStatus.value ? "true" : "false");
  });
}

botoesStatus.forEach(function (botao) {
  botao.addEventListener("click", function () {
    if (filtroStatus.value === botao.dataset.status) {
      return;
    }
    filtroStatus.value = botao.dataset.status;
    filtroStatus.dispatchEvent(new Event("change"));
  });
});

// total de cada status no bairro escolhido
function escreverContagens(lista) {
  const totais = { "": lista.length, ABERTA: 0, EM_ANDAMENTO: 0, RESOLVIDA: 0 };
  lista.forEach(function (o) {
    if (o.status in totais) {
      totais[o.status] += 1;
    }
  });
  botoesStatus.forEach(function (botao) {
    botao.querySelector(".filtro-total").textContent = "(" + totais[botao.dataset.status] + ")";
  });
}

async function atualizarContagens() {
  // sem filtro de status, a lista já tem tudo
  if (!filtroStatus.value) {
    escreverContagens(ocorrencias);
    return;
  }
  try {
    escreverContagens(await chamarApi("/ocorrencias" + montarQuery({ bairro: filtroBairro.value })));
  } catch (erro) {
    botoesStatus.forEach(function (botao) {
      botao.querySelector(".filtro-total").textContent = "";
    });
  }
}

// um listener só para os botões de todos os cartões
listaOcorrencias.addEventListener("click", function (evento) {
  const botao = evento.target.closest("button[data-acao]");
  if (!botao) {
    return;
  }
  const id = Number(botao.closest("[data-id]").dataset.id);
  const acao = botao.dataset.acao;

  if (acao === "apoiar") {
    apoiar(id);
  } else if (acao === "retirar-apoio") {
    retirarApoio(id);
  } else if (acao === "editar") {
    entrarNoModoEdicao(id);
  } else if (acao === "excluir") {
    pedirConfirmacaoExclusao(id);
  } else if (acao === "confirmar-exclusao") {
    confirmarExclusao(id);
  } else if (acao === "cancelar-exclusao") {
    cancelarExclusao(id);
  }
});

listaOcorrencias.addEventListener("change", function (evento) {
  if (evento.target.dataset.acao === "status") {
    const id = Number(evento.target.closest("[data-id]").dataset.id);
    mudarStatus(id, evento.target.value);
  }
});

// ---------- início ----------

// ignora valor da URL que não existe no select
function escolherOpcao(select, valor) {
  if (!valor) {
    return;
  }
  const existe = Array.prototype.some.call(select.options, function (opcao) {
    return opcao.value === valor;
  });
  if (existe) {
    select.value = valor;
  }
}

// o painel manda ?status=, ?bairro= ou #registrar
function aplicarFiltrosDaUrl() {
  const parametros = new URLSearchParams(location.search);
  escolherOpcao(filtroStatus, parametros.get("status"));
  escolherOpcao(filtroBairro, parametros.get("bairro"));
}

// ---------- celular: formulário recolhido ----------
// abaixo de 880 px o formulário começa fechado

const telaCelular = window.matchMedia("(max-width: 879.98px)");
const botaoAbrirFormulario = document.getElementById("abrir-formulario");
const painelFormulario = document.getElementById("painel-formulario");
let formularioAberto = false;

function aplicarFormularioRecolhido() {
  if (telaCelular.matches) {
    botaoAbrirFormulario.hidden = false;
    botaoAbrirFormulario.setAttribute("aria-expanded", formularioAberto ? "true" : "false");
    painelFormulario.hidden = !formularioAberto;
  } else {
    botaoAbrirFormulario.hidden = true;
    painelFormulario.hidden = false;
  }
}

function abrirFormulario() {
  formularioAberto = true;
  aplicarFormularioRecolhido();
}

botaoAbrirFormulario.addEventListener("click", function () {
  formularioAberto = !formularioAberto;
  aplicarFormularioRecolhido();
  if (formularioAberto) {
    campoTitulo.focus();
  }
});

if (telaCelular.addEventListener) {
  telaCelular.addEventListener("change", aplicarFormularioRecolhido);
} else {
  telaCelular.addListener(aplicarFormularioRecolhido);
}

aplicarFormularioRecolhido();

function focarFormularioSePedido() {
  if (location.hash === "#registrar") {
    abrirFormulario();
    formOcorrencia.scrollIntoView({ block: "start" });
    campoTitulo.focus({ preventScroll: true });
  }
}

// o "+ Registrar" do cabeçalho, nesta mesma página, só muda o hash
window.addEventListener("hashchange", focarFormularioSePedido);

async function iniciarPaginaOcorrencias() {
  await carregarListasDoFormulario();
  aplicarFiltrosDaUrl();
  carregarOcorrencias();
  focarFormularioSePedido();
}

iniciarPaginaOcorrencias();
