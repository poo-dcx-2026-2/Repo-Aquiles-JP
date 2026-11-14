package br.ufpb.dcx.poo.biblioteca.inicial;
import java.time.LocalDate;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
/** Identidade, prazo e transição de devolução pertencem ao empréstimo. */
final class Emprestimo {
    private final String id;
    private final Exemplar exemplar;
    private final Usuario usuario;
    private final LocalDate data;
    private final LocalDate prevista;
    private LocalDate devolucao;

    Emprestimo(String id, Exemplar exemplar, Usuario usuario, LocalDate data) {
        this.id = id; this.exemplar = exemplar; this.usuario = usuario; this.data = data;
        this.prevista = data.plusDays(EmprestimoService.PRAZO_EM_DIAS);
    }
    String id() { return id; }
    Exemplar exemplar() { return exemplar; }
    Usuario usuario() { return usuario; }
    boolean ativo() { return devolucao == null; }
    LocalDate data() { return data; }
    LocalDate prevista() { return prevista; }
    boolean atrasado(LocalDate referencia) { return ativo() && prevista.isBefore(referencia); }
    void devolver(LocalDate data) {
        if (data.isBefore(this.data)) throw new DadosInvalidosException("Devolução anterior ao empréstimo.");
        devolucao = data;
    }
    EmprestimoView view(boolean atraso) {
        return new EmprestimoView(id, exemplar.tombo(), usuario.matricula(), data, prevista, devolucao,
                !ativo() ? StatusEmprestimo.DEVOLVIDO : atraso ? StatusEmprestimo.ATRASADO : StatusEmprestimo.ATIVO);
    }
}
