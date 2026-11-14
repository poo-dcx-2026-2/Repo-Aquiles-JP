package br.ufpb.dcx.poo.biblioteca.inicial;

import java.util.Comparator;
import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

/** Implementação em memória que indexa usuários por sua matrícula imutável. */
public class UsuariosEmMemoria implements UsuarioService {

    private final EstadoBiblioteca estado;
    private final Circulacao circulacao;
    public UsuariosEmMemoria() { this(new EstadoBiblioteca()); }
    UsuariosEmMemoria(EstadoBiblioteca estado) { this.estado = estado; this.circulacao = new Circulacao(estado); }

    @Override
    public void cadastrarUsuario(String matricula, String nome) throws RecursoDuplicadoException {
        exigirTextoPreenchido(matricula, "matrícula");
        exigirTextoPreenchido(nome, "nome");
        if (estado.usuarios.containsKey(matricula)) {
            throw new RecursoDuplicadoException("Já existe usuário com a matrícula " + matricula);
        }
        estado.usuarios.put(matricula, new Usuario(matricula, nome));
    }

    @Override
    public UsuarioView buscarUsuario(String matricula) throws RecursoNaoEncontradoException {
        return paraView(localizarUsuario(matricula));
    }

    @Override
    public List<UsuarioView> listarUsuarios() {
        return estado.usuarios.values().stream()
                .map(this::paraView)
                .sorted(Comparator.comparing(UsuarioView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public void desativarUsuario(String matricula)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        circulacao.desativar(matricula);
    }

    @Override
    public void reativarUsuario(String matricula) throws RecursoNaoEncontradoException {
        localizarUsuario(matricula).reativar();
    }

    private Usuario localizarUsuario(String matricula) throws RecursoNaoEncontradoException {
        return estado.usuario(matricula);
    }

    private UsuarioView paraView(Usuario usuario) { return usuario.view(); }

    private static void exigirTextoPreenchido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("A " + campo + " é obrigatória.");
        }
    }
}
