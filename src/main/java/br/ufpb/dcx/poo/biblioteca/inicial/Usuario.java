package br.ufpb.dcx.poo.biblioteca.inicial;
import java.util.ArrayList;
import java.util.List;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
/** Usuário conhece sua situação e seus empréstimos, inclusive o histórico. */
final class Usuario {
    private final String matricula;
    private final String nome;
    private boolean ativo = true;
    private final List<Emprestimo> emprestimos = new ArrayList<>();
    Usuario(String matricula, String nome) { this.matricula = matricula; this.nome = nome; }
    String matricula() { return matricula; }
    String nome() { return nome; }
    boolean ativo() { return ativo; }
    int emprestimosAtivos() { return (int) emprestimos.stream().filter(Emprestimo::ativo).count(); }
    List<Emprestimo> emprestimos() { return List.copyOf(emprestimos); }
    void registrar(Emprestimo emprestimo) { emprestimos.add(emprestimo); }
    void exigirAtivo() throws UsuarioInativoException {
        if (!ativo) throw new UsuarioInativoException("Usuário inativo: " + matricula);
    }
    void exigirLimite() throws LimiteDeEmprestimosExcedidoException {
        if (emprestimosAtivos() >= EmprestimoService.LIMITE_DE_EMPRESTIMOS)
            throw new LimiteDeEmprestimosExcedidoException("Limite de empréstimos atingido.");
    }
    void desativar() throws OperacaoNaoPermitidaException {
        if (emprestimosAtivos() > 0) throw new OperacaoNaoPermitidaException("Usuário possui empréstimos ativos.");
        ativo = false;
    }
    void reativar() { ativo = true; }
    UsuarioView view() { return new UsuarioView(matricula, nome, ativo, emprestimosAtivos()); }
}
