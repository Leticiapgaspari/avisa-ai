package br.com.avisaai.service;

import java.time.LocalDate;
import java.util.List;

import br.com.avisaai.dao.BairroDao;
import br.com.avisaai.dao.CategoriaDao;
import br.com.avisaai.dao.OcorrenciaDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.dto.OcorrenciaDados;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Bairro;
import br.com.avisaai.model.Categoria;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.StatusOcorrencia;
import br.com.avisaai.model.Usuario;

public class OcorrenciaService {

    static final String MSG_NAO_ENCONTRADA = "Ocorrência não encontrada.";
    static final String MSG_OBRIGATORIOS = "Preencha título, endereço, categoria e bairro.";
    static final String MSG_SO_AGENTE_STATUS = "Apenas agentes podem alterar o status.";
    static final String MSG_STATUS_INVALIDO = "Status inválido.";
    static final String MSG_SO_PROPRIAS = "Você só pode alterar ocorrências que registrou.";

    private static final int TAMANHO_TITULO = 120;
    private static final int TAMANHO_DESCRICAO = 500;
    private static final int TAMANHO_ENDERECO = 200;

    private final OcorrenciaDao dao;
    private final UsuarioDao usuarioDao;
    private final CategoriaDao categoriaDao;
    private final BairroDao bairroDao;

    public OcorrenciaService(OcorrenciaDao dao, UsuarioDao usuarioDao, CategoriaDao categoriaDao, BairroDao bairroDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.categoriaDao = categoriaDao;
        this.bairroDao = bairroDao;
    }

    public List<Ocorrencia> listar(Integer idBairro, String status) {
        StatusOcorrencia filtroStatus = null;
        if (!Validacao.vazio(status)) {
            filtroStatus = converterStatus(status);
        }
        return dao.listar(idBairro, filtroStatus);
    }

    public Ocorrencia buscar(int id) {
        Ocorrencia ocorrencia = dao.buscarPorId(id);
        if (ocorrencia == null) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return ocorrencia;
    }

    public Ocorrencia criar(OcorrenciaDados dados, Integer idUsuario) {
        validarCampos(dados);
        Usuario autor = buscarUsuarioQueAge(idUsuario);

        Ocorrencia ocorrencia = new Ocorrencia();
        aplicarCampos(ocorrencia, dados);
        ocorrencia.setStatus(StatusOcorrencia.ABERTA);
        ocorrencia.setDataAbertura(LocalDate.now());
        ocorrencia.setAutor(autor);

        dao.inserir(ocorrencia);
        return buscar(ocorrencia.getId());
    }

    public Ocorrencia atualizar(int id, Integer idUsuario, OcorrenciaDados dados) {
        Ocorrencia ocorrencia = buscar(id);
        Usuario usuario = buscarUsuarioQueAge(idUsuario);
        if (!usuario.podeEditar(ocorrencia)) {
            throw new RegraNegocioException(MSG_SO_PROPRIAS);
        }
        validarCampos(dados);
        aplicarCampos(ocorrencia, dados);
        if (!dao.atualizar(ocorrencia)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return buscar(id);
    }

    public Ocorrencia atualizarStatus(int id, Integer idUsuario, String status) {
        buscar(id);
        Usuario usuario = buscarUsuarioQueAge(idUsuario);
        if (!usuario.podeMudarStatus()) {
            throw new RegraNegocioException(MSG_SO_AGENTE_STATUS);
        }
        StatusOcorrencia novoStatus = converterStatus(status);
        if (!dao.atualizarStatus(id, novoStatus)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
        return buscar(id);
    }

    public void excluir(int id, Integer idUsuario) {
        Ocorrencia ocorrencia = buscar(id);
        Usuario usuario = buscarUsuarioQueAge(idUsuario);
        if (!usuario.podeEditar(ocorrencia)) {
            throw new RegraNegocioException(MSG_SO_PROPRIAS);
        }
        if (!dao.excluir(id)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADA);
        }
    }

    private void validarCampos(OcorrenciaDados dados) {
        if (Validacao.vazio(dados.getTitulo())
                || Validacao.vazio(dados.getEndereco())
                || dados.getIdCategoria() == null
                || dados.getIdBairro() == null) {
            throw new RegraNegocioException(MSG_OBRIGATORIOS);
        }
        Validacao.tamanhoMaximo(dados.getTitulo().trim(), TAMANHO_TITULO, "O título");
        Validacao.tamanhoMaximo(Validacao.limpar(dados.getDescricao()), TAMANHO_DESCRICAO, "A descrição");
        Validacao.tamanhoMaximo(dados.getEndereco().trim(), TAMANHO_ENDERECO, "O endereço");
    }

    private void aplicarCampos(Ocorrencia ocorrencia, OcorrenciaDados dados) {
        Categoria categoria = categoriaDao.buscarPorId(dados.getIdCategoria());
        if (categoria == null) {
            throw new NaoEncontradoException(CategoriaService.MSG_NAO_ENCONTRADA);
        }
        Bairro bairro = bairroDao.buscarPorId(dados.getIdBairro());
        if (bairro == null) {
            throw new NaoEncontradoException(BairroService.MSG_NAO_ENCONTRADO);
        }
        ocorrencia.setTitulo(dados.getTitulo().trim());
        ocorrencia.setDescricao(Validacao.limpar(dados.getDescricao()));
        ocorrencia.setEndereco(dados.getEndereco().trim());
        ocorrencia.setCategoria(categoria);
        ocorrencia.setBairro(bairro);
    }

    private Usuario buscarUsuarioQueAge(Integer idUsuario) {
        if (idUsuario == null) {
            throw new RegraNegocioException(Validacao.MSG_USUARIO_OBRIGATORIO);
        }
        Usuario usuario = usuarioDao.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new NaoEncontradoException(UsuarioService.MSG_NAO_ENCONTRADO);
        }
        return usuario;
    }

    private StatusOcorrencia converterStatus(String texto) {
        StatusOcorrencia status = StatusOcorrencia.deTexto(texto);
        if (status == null) {
            throw new RegraNegocioException(MSG_STATUS_INVALIDO);
        }
        return status;
    }
}
