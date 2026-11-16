package br.com.avisaai.service;

import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.exception.RegraNegocioException;
import br.com.avisaai.model.Usuario;

// quem está agindo vem do ?usuario que o front manda
final class Permissao {

    static final String MSG_USUARIO_INEXISTENTE = "O usuário que está fazendo a operação não existe.";

    private Permissao() {
    }

    static Usuario quemAge(UsuarioDao usuarioDao, Integer idUsuario) {
        if (idUsuario == null) {
            throw new RegraNegocioException(Validacao.MSG_USUARIO_OBRIGATORIO);
        }
        Usuario usuario = usuarioDao.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new RegraNegocioException(MSG_USUARIO_INEXISTENTE);
        }
        return usuario;
    }

    static Usuario exigirGestor(UsuarioDao usuarioDao, Integer idUsuario, String mensagemNegado) {
        Usuario usuario = quemAge(usuarioDao, idUsuario);
        if (!usuario.podeGerenciarCadastros()) {
            throw new RegraNegocioException(mensagemNegado);
        }
        return usuario;
    }
}
