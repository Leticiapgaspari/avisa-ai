// Comunicação com o back-end e funções usadas

// JSON que o back devolve:
//  ocorrência: { id, titulo, descricao, endereco, status, dataAbertura, autor, categoria, bairro, totalApoios }
//  usuário: { id, nome, email, tipo, bairro }   (a senha nunca vem)
//  bairro e categoria: { id, nome }
//  relatorios: { nome, total }   ranking: { id, titulo, bairro, status, totalApoios }
//  erro: { "erro": "mensagem" }

const API = "http://localhost:8080";

// true: usa os dados de teste do fi do arquivo, trocar para false quando o back estiver rodando
const USAR_DADOS_PROVISORIOS = true;

const MENSAGEM_SEM_CONEXAO = "Não consegui falar com o servidor. Confira se ele está ligado e recarregue a página.";

// Ex: chamarApi("/ocorrencias?bairro=2") ou chamarApi("/apoios", "POST", { ... })
// Se der erro, lança um Error com a mensagem para mostrar na tela
async function chamarApi(caminho, metodo, corpo) {
  metodo = metodo || "GET";

  if (USAR_DADOS_PROVISORIOS) {
    return simularApi(caminho, metodo, corpo);
  }

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

// ---------- DADOS PROVISÓRIOS (apagar antes da entrega) ----------
// Imita o back com a carga inicial do banco (sql/avisa_ai.sql), pra testar sem servidor.
// O que muda fica no localStorage. Pra zerar: restaurarDadosProvisorios() no console.

// trocar o número quando a carga inicial mudar
const CHAVE_DADOS_PROVISORIOS = "avisaAi.dadosProvisorios.v2";

// senha de todo mundo da carga inicial
const SENHA_PROVISORIA = "avisa123";
let bancoProvisorio = null;

function criarCargaInicial() {
  const apoios = [
    [1, 1], [3, 1], [4, 1], [5, 1], [6, 1],
    [1, 2], [2, 2], [4, 2],
    [2, 3], [5, 3],
    [1, 4], [2, 4], [3, 4], [6, 4],
    [6, 5],
    [3, 6], [4, 6],
    [5, 7],
    [1, 8], [2, 8],
    [1, 9], [3, 9], [5, 9],
    [4, 11]
  ];

  const usuarios = [
    { id: 1, nome: "Ana Beatriz Souza", email: "ana.souza@exemplo.com", tipo: "CIDADAO", idBairro: 1 },
    { id: 2, nome: "Carlos Eduardo Lima", email: "carlos.lima@exemplo.com", tipo: "CIDADAO", idBairro: 2 },
    { id: 3, nome: "Mariana Oliveira", email: "mariana.oliveira@exemplo.com", tipo: "CIDADAO", idBairro: 2 },
    { id: 4, nome: "João Pedro Santos", email: "joao.santos@exemplo.com", tipo: "CIDADAO", idBairro: 3 },
    { id: 5, nome: "Fernanda Costa", email: "fernanda.costa@exemplo.com", tipo: "CIDADAO", idBairro: 4 },
    { id: 6, nome: "Rafael Almeida", email: "rafael.almeida@exemplo.com", tipo: "CIDADAO", idBairro: 5 },
    { id: 7, nome: "Patrícia Mendes", email: "patricia.mendes@exemplo.com", tipo: "AGENTE", idBairro: 1 },
    { id: 8, nome: "Roberto Nunes", email: "roberto.nunes@exemplo.com", tipo: "AGENTE", idBairro: 3 }
  ];
  usuarios.forEach(function (u) {
    u.senha = SENHA_PROVISORIA;
  });

  return {
    proximoId: { bairro: 6, categoria: 7, usuario: 9, ocorrencia: 13 },
    bairros: [
      { id: 1, nome: "Centro" },
      { id: 2, nome: "Jardim das Flores" },
      { id: 3, nome: "Vila Nova" },
      { id: 4, nome: "Parque das Águas" },
      { id: 5, nome: "Alto da Serra" }
    ],
    categorias: [
      { id: 1, nome: "Buraco na via" },
      { id: 2, nome: "Iluminação pública" },
      { id: 3, nome: "Lixo e entulho" },
      { id: 4, nome: "Calçada e acessibilidade" },
      { id: 5, nome: "Árvore e poda" },
      { id: 6, nome: "Sinalização de trânsito" }
    ],
    usuarios: usuarios,
    ocorrencias: [
      { id: 1, titulo: "Buraco grande na Rua das Acácias", descricao: "Buraco com cerca de 1 metro, carros desviam para a contramão", endereco: "Rua das Acácias, 120", status: "ABERTA", dataAbertura: "2026-11-02", idUsuario: 2, idCategoria: 1, idBairro: 2 },
      { id: 2, titulo: "Poste apagado em frente à escola", descricao: "Rua escura na saída dos alunos à noite, risco de assalto e atropelamento", endereco: "Rua dos Girassóis, 45", status: "EM_ANDAMENTO", dataAbertura: "2026-11-03", idUsuario: 3, idCategoria: 2, idBairro: 2 },
      { id: 3, titulo: "Entulho acumulado na praça central", descricao: "Entulho ocupa metade do passeio e atrai ratos e insetos", endereco: "Praça da Matriz, s/n", status: "ABERTA", dataAbertura: "2026-11-04", idUsuario: 1, idCategoria: 3, idBairro: 1 },
      { id: 4, titulo: "Calçada quebrada e sem rampa", descricao: "Cadeirantes e idosos precisam descer para a rua, onde passam carros", endereco: "Avenida Brasil, 880", status: "ABERTA", dataAbertura: "2026-11-04", idUsuario: 4, idCategoria: 4, idBairro: 3 },
      { id: 5, titulo: "Galho caído bloqueando a calçada", descricao: "Pedestres desviam pela rua e o galho pode cair sobre quem passa", endereco: "Rua das Nascentes, 30", status: "RESOLVIDA", dataAbertura: "2026-10-28", idUsuario: 5, idCategoria: 5, idBairro: 4 },
      { id: 6, titulo: "Semáforo piscando no cruzamento", descricao: "Motoristas não sabem quem tem a preferência, risco de colisão", endereco: "Rua Quinze com Avenida Central", status: "EM_ANDAMENTO", dataAbertura: "2026-11-05", idUsuario: 1, idCategoria: 6, idBairro: 1 },
      { id: 7, titulo: "Lixo espalhado perto do ponto de ônibus", descricao: "Lixo na calçada atrai animais e dificulta o embarque", endereco: "Rua do Mirante, 210", status: "ABERTA", dataAbertura: "2026-11-06", idUsuario: 6, idCategoria: 3, idBairro: 5 },
      { id: 8, titulo: "Buraco na esquina da padaria", descricao: "Motos e bicicletas podem cair ao desviar do buraco", endereco: "Rua São José, 15", status: "RESOLVIDA", dataAbertura: "2026-10-25", idUsuario: 4, idCategoria: 1, idBairro: 3 },
      { id: 9, titulo: "Três postes apagados na mesma rua", descricao: "Quarteirão inteiro no escuro, moradores têm medo de sair à noite", endereco: "Rua do Cruzeiro, 300 a 380", status: "ABERTA", dataAbertura: "2026-11-07", idUsuario: 6, idCategoria: 2, idBairro: 5 },
      { id: 10, titulo: "Faixa de pedestres apagada", descricao: "Motoristas não param e crianças atravessam sem sinalização", endereco: "Avenida das Rosas, 500", status: "ABERTA", dataAbertura: "2026-11-08", idUsuario: 2, idCategoria: 6, idBairro: 2 },
      { id: 11, titulo: "Raiz de árvore levantando a calçada", descricao: "Piso irregular causa tropeços, principalmente em idosos", endereco: "Rua da Fonte, 72", status: "EM_ANDAMENTO", dataAbertura: "2026-11-09", idUsuario: 5, idCategoria: 5, idBairro: 4 },
      { id: 12, titulo: "Bueiro sem tampa", descricao: "Buraco aberto na rua, risco de queda de pedestres e acidentes", endereco: "Rua do Comércio, 18", status: "ABERTA", dataAbertura: "2026-11-10", idUsuario: 7, idCategoria: 1, idBairro: 1 }
    ],
    apoios: apoios.map(function (par) {
      return { idUsuario: par[0], idOcorrencia: par[1] };
    })
  };
}

function lerBancoProvisorio() {
  if (bancoProvisorio) {
    return bancoProvisorio;
  }
  try {
    const salvo = localStorage.getItem(CHAVE_DADOS_PROVISORIOS);
    if (salvo) {
      bancoProvisorio = JSON.parse(salvo);
    }
  } catch (erro) {
    bancoProvisorio = null;
  }
  if (!bancoProvisorio) {
    bancoProvisorio = criarCargaInicial();
  }
  return bancoProvisorio;
}

function gravarBancoProvisorio() {
  try {
    localStorage.setItem(CHAVE_DADOS_PROVISORIOS, JSON.stringify(bancoProvisorio));
  } catch (erro) {
    // sem localStorage, os dados ficam só na memória
  }
}

function restaurarDadosProvisorios() {
  try {
    localStorage.removeItem(CHAVE_DADOS_PROVISORIOS);
  } catch (erro) {
    // nada a fazer
  }
  bancoProvisorio = null;
  location.reload();
}

// espera um pouco, como se fosse a rede
function simularApi(caminho, metodo, corpo) {
  return new Promise(function (resolve, reject) {
    setTimeout(function () {
      try {
        const resultado = rotaProvisoria(caminho, metodo, corpo || {});
        if (metodo !== "GET") {
          gravarBancoProvisorio();
        }
        resolve(resultado === undefined ? null : JSON.parse(JSON.stringify(resultado)));
      } catch (erro) {
        reject(erro);
      }
    }, 300);
  });
}

function falhar(mensagem) {
  throw new Error(mensagem);
}

function hojeProvisorio() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return d.getFullYear() + "-" + mes + "-" + dia;
}

function buscarPorId(lista, id, mensagem) {
  const item = lista.find(function (x) {
    return x.id === Number(id);
  });
  if (!item) {
    falhar(mensagem);
  }
  return item;
}

function ordenarPorNome(lista) {
  return lista.slice().sort(function (a, b) {
    return a.nome.localeCompare(b.nome, "pt-BR");
  });
}

function referencia(lista, id) {
  const item = lista.find(function (x) {
    return x.id === id;
  });
  return item ? { id: item.id, nome: item.nome } : { id: id, nome: "?" };
}

function idUsuarioLogadoProvisorio() {
  try {
    return sessionStorage.getItem("usuarioId");
  } catch (erro) {
    return null;
  }
}

function usuarioQueEstaAgindo(banco) {
  const id = Number(idUsuarioLogadoProvisorio());
  return banco.usuarios.find(function (u) { return u.id === id; }) || null;
}

// nunca devolve a senha
function usuarioJson(banco, u) {
  return { id: u.id, nome: u.nome, email: u.email, tipo: u.tipo, bairro: referencia(banco.bairros, u.idBairro) };
}

function ocorrenciaJson(banco, o) {
  return {
    id: o.id,
    titulo: o.titulo,
    descricao: o.descricao,
    endereco: o.endereco,
    status: o.status,
    dataAbertura: o.dataAbertura,
    autor: referencia(banco.usuarios, o.idUsuario),
    categoria: referencia(banco.categorias, o.idCategoria),
    bairro: referencia(banco.bairros, o.idBairro),
    totalApoios: banco.apoios.filter(function (a) {
      return a.idOcorrencia === o.id;
    }).length
  };
}

function validarOcorrenciaProvisoria(corpo) {
  if (!corpo.titulo || !corpo.endereco || !corpo.idCategoria || !corpo.idBairro) {
    falhar("Preencha título, endereço, categoria e bairro.");
  }
}

function validarUsuarioProvisorio(banco, corpo, idAtual) {
  if (!corpo.nome || !corpo.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(corpo.email)) {
    falhar("Informe nome e um e-mail válido.");
  }
  if (corpo.tipo !== "CIDADAO" && corpo.tipo !== "AGENTE") {
    falhar("Tipo de usuário inválido.");
  }
  const repetido = banco.usuarios.some(function (u) {
    return u.email.toLowerCase() === corpo.email.toLowerCase() && u.id !== idAtual;
  });
  if (repetido) {
    falhar("Esse e-mail já está em outra conta.");
  }
}

function verificarPodeEditar(usuario, ocorrencia) {
  if (usuario.tipo !== "AGENTE" && ocorrencia.idUsuario !== usuario.id) {
    falhar("Você só pode alterar ocorrências que registrou.");
  }
}

// bairros e categorias têm o mesmo CRUD
function crudSimplesProvisorio(banco, colecao, chaveId, nomeFeminino, metodo, params, corpo) {
  const lista = banco[colecao];
  const naoEncontrado = nomeFeminino ? "Categoria não encontrada." : "Bairro não encontrado.";

  if (metodo === "GET") {
    return ordenarPorNome(lista);
  }
  if (metodo === "DELETE") {
    const item = buscarPorId(lista, params.get("id"), naoEncontrado);
    const campo = colecao === "bairros" ? "idBairro" : "idCategoria";
    const emUso = banco.ocorrencias.some(function (o) { return o[campo] === item.id; }) ||
      banco.usuarios.some(function (u) { return u[campo] === item.id; });
    if (emUso) {
      falhar("Não é possível excluir: existem ocorrências ligadas a este registro.");
    }
    banco[colecao] = lista.filter(function (x) { return x.id !== item.id; });
    return { mensagem: "Registro excluído." };
  }

  const nome = (corpo.nome || "").trim();
  if (nome.length < 3) {
    falhar("Informe um nome com pelo menos 3 caracteres.");
  }
  const idAtual = metodo === "PUT" ? Number(params.get("id")) : null;
  const repetido = lista.some(function (x) {
    return x.nome.toLowerCase() === nome.toLowerCase() && x.id !== idAtual;
  });
  if (repetido) {
    falhar("Já existe um registro com este valor.");
  }

  if (metodo === "POST") {
    const novo = { id: banco.proximoId[chaveId]++, nome: nome };
    lista.push(novo);
    return novo;
  }
  if (metodo === "PUT") {
    const item = buscarPorId(lista, idAtual, naoEncontrado);
    item.nome = nome;
    return item;
  }
  falhar("Método não suportado.");
}

function relatorioProvisorio(banco, tipo) {
  function contar(lista, campo) {
    return lista.map(function (item) {
      return {
        nome: item.nome,
        total: banco.ocorrencias.filter(function (o) { return o[campo] === item.id; }).length
      };
    }).sort(function (a, b) {
      return b.total - a.total || a.nome.localeCompare(b.nome, "pt-BR");
    });
  }

  if (tipo === "bairro") {
    return contar(banco.bairros, "idBairro");
  }
  if (tipo === "categoria") {
    return contar(banco.categorias, "idCategoria");
  }
  if (tipo === "status") {
    return ["ABERTA", "EM_ANDAMENTO", "RESOLVIDA"].map(function (s) {
      return { nome: s, total: banco.ocorrencias.filter(function (o) { return o.status === s; }).length };
    });
  }
  if (tipo === "ranking") {
    return banco.ocorrencias
      .map(function (o) { return ocorrenciaJson(banco, o); })
      .sort(function (a, b) { return b.totalApoios - a.totalApoios || a.id - b.id; })
      .map(function (o) {
        return { id: o.id, titulo: o.titulo, bairro: o.bairro.nome, status: o.status, totalApoios: o.totalApoios };
      });
  }
  falhar("Tipo de relatório inválido.");
}

function rotaProvisoria(caminho, metodo, corpo) {
  const partes = caminho.split("?");
  const rota = partes[0];
  const params = new URLSearchParams(partes[1] || "");
  const banco = lerBancoProvisorio();

  if (rota === "/bairros") {
    return crudSimplesProvisorio(banco, "bairros", "bairro", false, metodo, params, corpo);
  }
  if (rota === "/categorias") {
    return crudSimplesProvisorio(banco, "categorias", "categoria", true, metodo, params, corpo);
  }

  if (rota === "/login" && metodo === "POST") {
    const email = String(corpo.email || "").trim().toLowerCase();
    const usuario = banco.usuarios.find(function (u) {
      return u.email.toLowerCase() === email;
    });
    // mesma mensagem nos dois casos pra não revelar quais e-mails existem
    if (!usuario || usuario.senha !== corpo.senha) {
      falhar("E-mail ou senha incorretos.");
    }
    return usuarioJson(banco, usuario);
  }

  if (rota === "/relatorios" && metodo === "GET") {
    return relatorioProvisorio(banco, params.get("tipo"));
  }

  if (rota === "/usuarios/senha" && metodo === "PUT") {
    const u = buscarPorId(banco.usuarios, params.get("id"), "Usuário não encontrado.");
    const quem = usuarioQueEstaAgindo(banco);
    if (quem === null || quem.id !== u.id) {
      falhar("Você só pode alterar os seus próprios dados.");
    }
    if (u.senha !== corpo.senhaAtual) {
      falhar("A senha atual não confere.");
    }
    if (!corpo.novaSenha || String(corpo.novaSenha).length < 6) {
      falhar("A nova senha precisa ter pelo menos 6 caracteres.");
    }
    if (corpo.novaSenha === corpo.senhaAtual) {
      falhar("A nova senha precisa ser diferente da atual.");
    }
    u.senha = String(corpo.novaSenha);
    return { mensagem: "Senha trocada." };
  }

  if (rota === "/usuarios") {
    if (metodo === "GET") {
      if (params.get("id")) {
        return usuarioJson(banco, buscarPorId(banco.usuarios, params.get("id"), "Usuário não encontrado."));
      }
      return ordenarPorNome(banco.usuarios).map(function (u) { return usuarioJson(banco, u); });
    }
    if (metodo === "POST") {
      // sem ninguém logado é o cadastro da tela de login: sempre morador e com senha
      const cadastroPublico = !idUsuarioLogadoProvisorio();
      if (cadastroPublico) {
        corpo.tipo = "CIDADAO";
        if (!corpo.senha || String(corpo.senha).length < 6) {
          falhar("A senha precisa ter pelo menos 6 caracteres.");
        }
        const emailUsado = banco.usuarios.some(function (u) {
          return u.email.toLowerCase() === String(corpo.email || "").trim().toLowerCase();
        });
        if (emailUsado) {
          falhar("Já existe uma conta com esse e-mail. Tente entrar.");
        }
      }
      validarUsuarioProvisorio(banco, corpo, null);
      const novo = {
        id: banco.proximoId.usuario++,
        nome: corpo.nome,
        email: corpo.email,
        tipo: corpo.tipo,
        idBairro: Number(corpo.idBairro),
        senha: corpo.senha || SENHA_PROVISORIA
      };
      banco.usuarios.push(novo);
      return usuarioJson(banco, novo);
    }
    if (metodo === "PUT") {
      const u = buscarPorId(banco.usuarios, params.get("id"), "Usuário não encontrado.");
      // morador só altera a própria conta, e ninguém muda o próprio tipo
      const quem = usuarioQueEstaAgindo(banco);
      const agindoComoAgente = quem !== null && quem.tipo === "AGENTE";
      const ehAPropriaConta = quem !== null && quem.id === u.id;
      if (!agindoComoAgente && !ehAPropriaConta) {
        falhar("Você só pode alterar os seus próprios dados.");
      }
      if (ehAPropriaConta) {
        corpo.tipo = u.tipo;
      }
      validarUsuarioProvisorio(banco, corpo, u.id);
      u.nome = corpo.nome;
      u.email = corpo.email;
      u.tipo = corpo.tipo;
      u.idBairro = Number(corpo.idBairro);
      return usuarioJson(banco, u);
    }
    if (metodo === "DELETE") {
      const u = buscarPorId(banco.usuarios, params.get("id"), "Usuário não encontrado.");
      if (banco.ocorrencias.some(function (o) { return o.idUsuario === u.id; })) {
        falhar("Não é possível excluir: existem ocorrências ligadas a este registro.");
      }
      banco.usuarios = banco.usuarios.filter(function (x) { return x.id !== u.id; });
      banco.apoios = banco.apoios.filter(function (a) { return a.idUsuario !== u.id; });
      return { mensagem: "Usuário excluído." };
    }
  }

  if (rota === "/ocorrencias") {
    if (metodo === "GET") {
      if (params.get("id")) {
        return ocorrenciaJson(banco, buscarPorId(banco.ocorrencias, params.get("id"), "Ocorrência não encontrada."));
      }
      const bairro = params.get("bairro");
      const status = params.get("status");
      return banco.ocorrencias
        .filter(function (o) {
          return (!bairro || o.idBairro === Number(bairro)) && (!status || o.status === status);
        })
        .sort(function (a, b) {
          return b.dataAbertura.localeCompare(a.dataAbertura) || b.id - a.id;
        })
        .map(function (o) { return ocorrenciaJson(banco, o); });
    }
    if (metodo === "POST") {
      validarOcorrenciaProvisoria(corpo);
      buscarPorId(banco.usuarios, corpo.idUsuario, "Usuário não encontrado.");
      const nova = {
        id: banco.proximoId.ocorrencia++,
        titulo: corpo.titulo,
        descricao: corpo.descricao || null,
        endereco: corpo.endereco,
        status: "ABERTA",
        dataAbertura: hojeProvisorio(),
        idUsuario: Number(corpo.idUsuario),
        idCategoria: Number(corpo.idCategoria),
        idBairro: Number(corpo.idBairro)
      };
      banco.ocorrencias.push(nova);
      return ocorrenciaJson(banco, nova);
    }
    if (metodo === "PUT") {
      const o = buscarPorId(banco.ocorrencias, params.get("id"), "Ocorrência não encontrada.");
      const quem = buscarPorId(banco.usuarios, params.get("usuario"), "Usuário não encontrado.");
      verificarPodeEditar(quem, o);
      validarOcorrenciaProvisoria(corpo);
      o.titulo = corpo.titulo;
      o.descricao = corpo.descricao || null;
      o.endereco = corpo.endereco;
      o.idCategoria = Number(corpo.idCategoria);
      o.idBairro = Number(corpo.idBairro);
      return ocorrenciaJson(banco, o);
    }
    if (metodo === "DELETE") {
      const o = buscarPorId(banco.ocorrencias, params.get("id"), "Ocorrência não encontrada.");
      const quem = buscarPorId(banco.usuarios, params.get("usuario"), "Usuário não encontrado.");
      verificarPodeEditar(quem, o);
      banco.ocorrencias = banco.ocorrencias.filter(function (x) { return x.id !== o.id; });
      banco.apoios = banco.apoios.filter(function (a) { return a.idOcorrencia !== o.id; });
      return { mensagem: "Ocorrência excluída." };
    }
  }

  if (rota === "/ocorrencias/status" && metodo === "PUT") {
    const o = buscarPorId(banco.ocorrencias, params.get("id"), "Ocorrência não encontrada.");
    const quem = buscarPorId(banco.usuarios, params.get("usuario"), "Usuário não encontrado.");
    if (quem.tipo !== "AGENTE") {
      falhar("Apenas agentes podem alterar o status.");
    }
    if (!NOMES_STATUS[corpo.status]) {
      falhar("Status inválido.");
    }
    o.status = corpo.status;
    return ocorrenciaJson(banco, o);
  }

  if (rota === "/apoios") {
    if (metodo === "POST") {
      const quem = buscarPorId(banco.usuarios, corpo.idUsuario, "Usuário não encontrado.");
      const o = buscarPorId(banco.ocorrencias, corpo.idOcorrencia, "Ocorrência não encontrada.");
      if (quem.tipo === "AGENTE") {
        falhar("Agentes não apoiam ocorrências.");
      }
      if (o.idUsuario === quem.id) {
        falhar("Você não pode apoiar a própria ocorrência.");
      }
      if (o.status === "RESOLVIDA") {
        falhar("Esta ocorrência já foi resolvida.");
      }
      if (banco.apoios.some(function (a) { return a.idUsuario === quem.id && a.idOcorrencia === o.id; })) {
        falhar("Você já apoiou esta ocorrência.");
      }
      banco.apoios.push({ idUsuario: quem.id, idOcorrencia: o.id });
      return { mensagem: "Apoio registrado." };
    }
    if (metodo === "DELETE") {
      const idUsuario = Number(params.get("usuario"));
      const idOcorrencia = Number(params.get("ocorrencia"));
      const antes = banco.apoios.length;
      banco.apoios = banco.apoios.filter(function (a) {
        return !(a.idUsuario === idUsuario && a.idOcorrencia === idOcorrencia);
      });
      if (banco.apoios.length === antes) {
        falhar("Apoio não encontrado.");
      }
      return { mensagem: "Apoio retirado." };
    }
  }

  falhar("Método não suportado.");
}
// ---------- fim dos DADOS PROVISÓRIOS ----------
