package br.ufpb.dcx.poo.biblioteca.inicial;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

/** Implementação em memória que indexa usuários por sua matrícula imutável. */
public class UsuariosEmMemoria implements UsuarioService {

    private final Map<String, Usuario> usuariosPorMatricula = new HashMap<>();

    @Override
    public void cadastrarUsuario(String matricula, String nome) throws RecursoDuplicadoException {
        exigirTextoPreenchido(matricula, "matrícula");
        exigirTextoPreenchido(nome, "nome");
        if (usuariosPorMatricula.containsKey(matricula)) {
            throw new RecursoDuplicadoException("Já existe usuário com a matrícula " + matricula);
        }
        usuariosPorMatricula.put(matricula, new Usuario(matricula, nome));
    }

    @Override
    public UsuarioView buscarUsuario(String matricula) throws RecursoNaoEncontradoException {
        return paraView(localizarUsuario(matricula));
    }

    @Override
    public List<UsuarioView> listarUsuarios() {
        return usuariosPorMatricula.values().stream()
                .map(this::paraView)
                .sorted(Comparator.comparing(UsuarioView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public void desativarUsuario(String matricula)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        localizarUsuario(matricula).desativar();
    }

    @Override
    public void reativarUsuario(String matricula) throws RecursoNaoEncontradoException {
        localizarUsuario(matricula).reativar();
    }

    private Usuario localizarUsuario(String matricula) throws RecursoNaoEncontradoException {
        Usuario usuario = usuariosPorMatricula.get(matricula);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + matricula);
        }
        return usuario;
    }

    private UsuarioView paraView(Usuario usuario) {
        return new UsuarioView(usuario.matricula(), usuario.nome(), usuario.ativo(), 0);
    }

    private static void exigirTextoPreenchido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("A " + campo + " é obrigatória.");
        }
    }
}
