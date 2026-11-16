// Confirmação e aviso rápido, com o Modal e o Toast do Bootstrap.

let janelaConfirmacao = null;

function criarJanelaConfirmacao() {
  const elemento = document.createElement("div");
  elemento.className = "modal fade";
  elemento.id = "janela-confirmacao";
  elemento.tabIndex = -1;
  elemento.setAttribute("aria-labelledby", "janela-confirmacao-titulo");
  elemento.setAttribute("aria-describedby", "janela-confirmacao-texto");
  elemento.innerHTML =
    '<div class="modal-dialog modal-dialog-centered">' +
    '<div class="modal-content">' +
    '<div class="modal-header">' +
    '<h2 class="modal-title" id="janela-confirmacao-titulo"></h2>' +
    '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>' +
    "</div>" +
    '<div class="modal-body"><p id="janela-confirmacao-texto"></p></div>' +
    '<div class="modal-footer">' +
    '<button type="button" class="botao botao-secundario" data-bs-dismiss="modal" id="janela-confirmacao-nao">Cancelar</button>' +
    '<button type="button" class="botao botao-perigo-cheio" id="janela-confirmacao-sim">Excluir</button>' +
    "</div>" +
    "</div>" +
    "</div>";
  document.body.appendChild(elemento);

  const janela = {
    elemento: elemento,
    modal: new bootstrap.Modal(elemento),
    confirmou: false,
    resolver: null,
    origem: null
  };

  document.getElementById("janela-confirmacao-sim").addEventListener("click", function () {
    janela.confirmou = true;
    janela.modal.hide();
  });

  // foco no Cancelar, que não apaga nada
  // se confirmou durante a animação de abertura, o Bootstrap ignorou o hide(): fecha agora
  elemento.addEventListener("shown.bs.modal", function () {
    if (janela.confirmou) {
      janela.modal.hide();
      return;
    }
    document.getElementById("janela-confirmacao-nao").focus();
  });

  elemento.addEventListener("hidden.bs.modal", function () {
    if (janela.origem && document.body.contains(janela.origem)) {
      janela.origem.focus();
    }
    const resolver = janela.resolver;
    janela.resolver = null;
    if (resolver) {
      resolver(janela.confirmou);
    }
  });

  return janela;
}

// devolve true se a pessoa confirmou
function perguntarConfirmacao(titulo, texto, origem) {
  if (!janelaConfirmacao) {
    janelaConfirmacao = criarJanelaConfirmacao();
  }
  const janela = janelaConfirmacao;
  document.getElementById("janela-confirmacao-titulo").textContent = titulo;
  document.getElementById("janela-confirmacao-texto").textContent = texto;
  janela.confirmou = false;
  janela.origem = origem || null;
  return new Promise(function (resolver) {
    janela.resolver = resolver;
    janela.modal.show();
  });
}

// some sozinho; o role="status" faz o leitor de tela anunciar
function mostrarAviso(texto) {
  let area = document.getElementById("area-avisos");
  if (!area) {
    area = document.createElement("div");
    area.id = "area-avisos";
    area.className = "toast-container area-avisos";
    document.body.appendChild(area);
  }

  const aviso = document.createElement("div");
  aviso.className = "toast aviso-sucesso";
  aviso.setAttribute("role", "status");
  aviso.setAttribute("aria-live", "polite");
  aviso.setAttribute("aria-atomic", "true");
  aviso.innerHTML =
    '<div class="toast-body">' +
    '<span class="aviso-texto"></span>' +
    '<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Fechar aviso"></button>' +
    "</div>";
  aviso.querySelector(".aviso-texto").textContent = texto;
  area.appendChild(aviso);

  aviso.addEventListener("hidden.bs.toast", function () {
    aviso.remove();
  });
  new bootstrap.Toast(aviso, { delay: 4000 }).show();
}
