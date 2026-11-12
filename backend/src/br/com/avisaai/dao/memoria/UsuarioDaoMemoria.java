package br.com.avisaai.dao.memoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import br.com.avisaai.dao.UsuarioDao;
import br.com.avisaai.exception.DadosException;
import br.com.avisaai.model.Usuario;

public class UsuarioDaoMemoria implements UsuarioDao {

    private final BancoMemoria banco;

    public UsuarioDaoMemoria(BancoMemoria banco) {
        this.banco = banco;
    }

    @Override
    public Usuario inserir(Usuario usuario) {
        synchronized (banco) {
            verificarEmailUnico(usuario.getEmail(), null);
            BancoMemoria.LinhaUsuario linha = new BancoMemoria.LinhaUsuario();
            linha.id = banco.proximoIdUsuario();
            copiar(usuario, linha);
            banco.usuarios.put(linha.id, linha);
            usuario.setId(linha.id);
            return usuario;
        }
    }

    @Override
    public List<Usuario> listar() {
        synchronized (banco) {
            List<Usuario> lista = new ArrayList<Usuario>();
            for (Integer id : banco.usuarios.keySet()) {
                lista.add(banco.montarUsuario(id));
            }
            Collections.sort(lista, new Comparator<Usuario>() {
                @Override
                public int compare(Usuario a, Usuario b) {
                    return a.getNome().compareToIgnoreCase(b.getNome());
                }
            });
            return lista;
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        synchronized (banco) {
            return banco.montarUsuario(id);
        }
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        synchronized (banco) {
            for (BancoMemoria.LinhaUsuario u : banco.usuarios.values()) {
                if (u.email.equals(email)) {
                    return banco.montarUsuario(u.id);
                }
            }
            return null;
        }
    }

    @Override
    public boolean atualizar(Usuario usuario) {
        synchronized (banco) {
            BancoMemoria.LinhaUsuario linha = banco.usuarios.get(usuario.getId());
            if (linha == null) {
                return false;
            }
            verificarEmailUnico(usuario.getEmail(), usuario.getId());
            copiar(usuario, linha);
            return true;
        }
    }

    @Override
    public boolean excluir(int id) {
        synchronized (banco) {
            if (!banco.usuarios.containsKey(id)) {
                return false;
            }
            for (BancoMemoria.LinhaOcorrencia o : banco.ocorrencias.values()) {
                if (o.idUsuario == id) {
                    throw DadosException.emUso(null);
                }
            }
            Iterator<BancoMemoria.LinhaApoio> it = banco.apoios.iterator();
            while (it.hasNext()) {
                if (it.next().idUsuario == id) {
                    it.remove();
                }
            }
            banco.usuarios.remove(id);
            return true;
        }
    }

    private void copiar(Usuario usuario, BancoMemoria.LinhaUsuario linha) {
        linha.nome = usuario.getNome();
        linha.email = usuario.getEmail();
        linha.tipo = usuario.getTipo();
        linha.idBairro = usuario.getBairro().getId();
    }

    private void verificarEmailUnico(String email, Integer idIgnorado) {
        for (BancoMemoria.LinhaUsuario u : banco.usuarios.values()) {
            if (u.email.equals(email) && !Integer.valueOf(u.id).equals(idIgnorado)) {
                throw DadosException.duplicado(null);
            }
        }
    }
}
