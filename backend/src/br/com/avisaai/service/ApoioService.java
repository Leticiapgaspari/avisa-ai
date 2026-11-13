package br.com.avisaai.service;

import java.time.LocalDate;

import br.com.avisaai.dao.ApoioDao;
import br.com.avisaai.dao.OcorrenciaDao;
import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.exception.NaoEncontradoException;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Apoio;
import br.com.avisaai.model.Ocorrencia;
import br.com.avisaai.model.Usuario;

public class ApoioService {

    static final String MSG_AGENTE_NAO_APOIA = "Agentes não apoiam ocorrências.";
    static final String MSG_PROPRIA = "Você não pode apoiar a própria ocorrência.";
    static final String MSG_JA_APOIOU = "Você já apoiou esta ocorrência.";
    static final String MSG_RESOLVIDA = "Esta ocorrência já foi resolvida.";
    static final String MSG_OBRIGATORIOS = "Informe o usuário e a ocorrência.";
    static final String MSG_NAO_ENCONTRADO = "Apoio não encontrado.";

    private final ApoioDao dao;
    private final UsuarioDao usuarioDao;
    private final OcorrenciaDao ocorrenciaDao;

    public ApoioService(ApoioDao dao, UsuarioDao usuarioDao, OcorrenciaDao ocorrenciaDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.ocorrenciaDao = ocorrenciaDao;
    }

    public void apoiar(Integer idUsuario, Integer idOcorrencia) {
        if (idUsuario == null || idOcorrencia == null) {
            throw new RegraNegocioException(MSG_OBRIGATORIOS);
        }
        Usuario usuario = usuarioDao.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new NaoEncontradoException(UsuarioService.MSG_NAO_ENCONTRADO);
        }
        Ocorrencia ocorrencia = ocorrenciaDao.buscarPorId(idOcorrencia);
        if (ocorrencia == null) {
            throw new NaoEncontradoException(OcorrenciaService.MSG_NAO_ENCONTRADA);
        }

        if (!usuario.podeApoiar()) {
            throw new RegraNegocioException(MSG_AGENTE_NAO_APOIA);
        }
        if (usuario.ehAutorDe(ocorrencia)) {
            throw new RegraNegocioException(MSG_PROPRIA);
        }
        if (ocorrencia.isResolvida()) {
            throw new RegraNegocioException(MSG_RESOLVIDA);
        }
        if (dao.existe(idUsuario, idOcorrencia)) {
            throw new RegraNegocioException(MSG_JA_APOIOU);
        }

        try {
            dao.inserir(new Apoio(usuario, ocorrencia, LocalDate.now()));
        } catch (DadosException e) {
            if (e.getTipo() == DadosException.Tipo.DUPLICADO) {
                throw new RegraNegocioException(MSG_JA_APOIOU);
            }
            throw e;
        }
    }

    public void remover(Integer idUsuario, Integer idOcorrencia) {
        if (idUsuario == null || idOcorrencia == null) {
            throw new RegraNegocioException(MSG_OBRIGATORIOS);
        }
        if (!dao.excluir(idUsuario, idOcorrencia)) {
            throw new NaoEncontradoException(MSG_NAO_ENCONTRADO);
        }
    }
}
