// Gestão: CRUD de categorias e de bairros.
// Os dois blocos funcionam igual, então uma função só monta os dois.

const TEXTO_EM_USO = "Não dá para excluir enquanto houver ocorrências com este item.";

function textoUso(total) {
  if (!total) {
    return "Nenhuma ocorrência";
  }
  return total === 1 ? "1 ocorrência" : total + " ocorrências";
}

function configurarBloco(config) {
  const formulario = document.getElementById("form-" + config.prefixo);
  const campoNome = document.getElementById("nome-" + config.prefixo);
  const botaoEnviar = formulario.querySelector("button[type='submit']");
  const botaoCancelar = document.getElementById("cancelar-" + config.prefixo);
  const mensagem = document.getElementById("mensagem-" + config.prefixo);
  const lista = document.getElementById("lista-" + config.prefixo);
  const total = document.getElementById("total-" + config.prefixo);

  let itens = [];
  let usos = null;                // { "Centro": 3, ... } ou null se o relatório falhar
  let idEmEdicao = null;
  let idConfirmandoExclusao = null;

  // quantas ocorrências usam cada item (o relatório vem como [{ nome, total }])
  async function carregarUsos() {
    try {
      const linhas = await chamarApi("/relatorios" + montarQuery({ tipo: config.prefixo }));
      const mapa = {};
      linhas.forEach(function (linha) {
        mapa[linha.nome] = linha.total;
      });
      return mapa;
    } catch (erro) {
      // sem a contagem a lista aparece igual; o back barra a exclusão de item em uso
      return null;
    }
  }

  async function carregar() {
    lista.innerHTML = '<li class="lista-estado">Carregando...</li>';
    total.textContent = "";
    idConfirmandoExclusao = null;
    const promessaUsos = carregarUsos();
    try {
      itens = await chamarApi(config.rota);
      usos = await promessaUsos;
      desenhar();
    } catch (erro) {
      itens = [];
      lista.innerHTML = avisarSeSemConexao(erro)
        ? ""
        : '<li class="lista-estado erro">' + escaparHtml(erro.message) + "</li>";
    }
  }

  function montarAcoes(item, emUso) {
    if (idConfirmandoExclusao === item.id) {
      return (
        '<div class="confirmacao" role="group" aria-label="Confirmar exclusão">' +
        "<span>" + config.textoConfirmar + "</span>" +
        '<button type="button" class="botao botao-perigo-cheio" data-acao="confirmar-exclusao">Excluir</button>' +
        '<button type="button" class="botao botao-leve" data-acao="cancelar-exclusao">Cancelar</button>' +
        "</div>"
      );
    }

    const excluir = emUso
      ? '<button type="button" class="botao botao-leve-perigo" disabled title="' + TEXTO_EM_USO + '">Em uso</button>'
      : '<button type="button" class="botao botao-leve-perigo" data-acao="excluir">Excluir</button>';

    return (
      '<div class="acoes-linha">' +
      '<button type="button" class="botao botao-leve" data-acao="editar">Editar</button>' +
      excluir +
      "</div>"
    );
  }

  function desenhar() {
    total.textContent = itens.length === 1 ? "1 " + config.nomeSingular : itens.length + " " + config.nomePlural;

    if (itens.length === 0) {
      lista.innerHTML = '<li class="lista-vazia">' + config.textoVazio + "</li>";
      return;
    }

    lista.innerHTML = itens.map(function (item) {
      const quantos = usos ? usos[item.nome] || 0 : null;
      const emUso = quantos !== null && quantos > 0;
      const uso = quantos === null ? "" : '<span class="texto-suave">' + textoUso(quantos) + "</span>";
      const classes = [];
      if (item.id === idEmEdicao) {
        classes.push("em-edicao");
      }
      if (idConfirmandoExclusao === item.id) {
        classes.push("confirmando");
      }
      return (
        '<li data-id="' + item.id + '"' + (classes.length ? ' class="' + classes.join(" ") + '"' : "") + ">" +
        '<span class="gestao-item">' +
        '<strong class="nome-item">' + escaparHtml(item.nome) + "</strong>" +
        uso +
        "</span>" +
        montarAcoes(item, emUso) +
        "</li>"
      );
    }).join("");
  }

  function focar(id, seletor) {
    const elemento = lista.querySelector('[data-id="' + id + '"] ' + seletor);
    if (elemento) {
      elemento.focus();
    }
  }

  function sairDoModoEdicao() {
    idEmEdicao = null;
    formulario.reset();
    limparErros(formulario);
    botaoEnviar.textContent = "Adicionar";
    botaoCancelar.hidden = true;
  }

  function validar() {
    limparErros(formulario);
    const nome = campoNome.value.trim();
    if (nome.length < 3) {
      marcarErro(campoNome, "Informe um nome com pelo menos 3 caracteres.");
    } else if (nome.length > 80) {
      marcarErro(campoNome, "O nome pode ter no máximo 80 caracteres.");
    } else {
      return true;
    }
    campoNome.focus();
    return false;
  }

  async function enviar(evento) {
    evento.preventDefault();
    mostrarMensagem(mensagem, "");
    if (!validar()) {
      return;
    }

    const dados = { nome: campoNome.value.trim() };
    botaoEnviar.disabled = true;
    try {
      if (idEmEdicao === null) {
        await chamarApi(config.rota, "POST", dados);
        mostrarMensagem(mensagem, config.textoCadastrado, "sucesso");
      } else {
        await chamarApi(config.rota + montarQuery({ id: idEmEdicao }), "PUT", dados);
        mostrarMensagem(mensagem, config.textoAtualizado, "sucesso");
      }
      sairDoModoEdicao();
      carregar();
    } catch (erro) {
      if (!avisarSeSemConexao(erro)) {
        mostrarMensagem(mensagem, erro.message, "erro");
      }
    } finally {
      botaoEnviar.disabled = false;
    }
  }

  async function excluir(id) {
    try {
      await chamarApi(config.rota + montarQuery({ id: id }), "DELETE");
      if (idEmEdicao === id) {
        sairDoModoEdicao();
      }
      mostrarMensagem(mensagem, config.textoExcluido, "sucesso");
      carregar();
    } catch (erro) {
      // ex.: item em uso (a mensagem vem do back)
      idConfirmandoExclusao = null;
      desenhar();
      if (!avisarSeSemConexao(erro)) {
        mostrarMensagem(mensagem, erro.message, "erro");
      }
    }
  }

  formulario.addEventListener("submit", enviar);
  limparErroAoCorrigir(formulario);

  botaoCancelar.addEventListener("click", function () {
    sairDoModoEdicao();
    mostrarMensagem(mensagem, "");
    desenhar();
  });

  lista.addEventListener("click", function (evento) {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) {
      return;
    }
    const id = Number(botao.closest("[data-id]").dataset.id);
    const acao = botao.dataset.acao;

    if (acao === "editar") {
      const item = itens.find(function (x) { return x.id === id; });
      idEmEdicao = id;
      campoNome.value = item.nome;
      limparErros(formulario);
      mostrarMensagem(mensagem, "");
      botaoEnviar.textContent = "Salvar";
      botaoCancelar.hidden = false;
      desenhar();
      campoNome.focus();
    } else if (acao === "excluir") {
      idConfirmandoExclusao = id;
      mostrarMensagem(mensagem, "");
      desenhar();
      focar(id, "[data-acao='cancelar-exclusao']");
    } else if (acao === "cancelar-exclusao") {
      idConfirmandoExclusao = null;
      desenhar();
      focar(id, "[data-acao='excluir']");
    } else if (acao === "confirmar-exclusao") {
      excluir(id);
    }
  });

  carregar();
}

if (ehAgente()) {
  configurarBloco({
    prefixo: "categoria",
    rota: "/categorias",
    nomeSingular: "categoria",
    nomePlural: "categorias",
    textoVazio: "Nenhuma categoria cadastrada.",
    textoConfirmar: "Excluir esta categoria?",
    textoCadastrado: "Categoria cadastrada.",
    textoAtualizado: "Categoria atualizada.",
    textoExcluido: "Categoria excluída."
  });

  configurarBloco({
    prefixo: "bairro",
    rota: "/bairros",
    nomeSingular: "bairro",
    nomePlural: "bairros",
    textoVazio: "Nenhum bairro cadastrado.",
    textoConfirmar: "Excluir este bairro?",
    textoCadastrado: "Bairro cadastrado.",
    textoAtualizado: "Bairro atualizado.",
    textoExcluido: "Bairro excluído."
  });
}
