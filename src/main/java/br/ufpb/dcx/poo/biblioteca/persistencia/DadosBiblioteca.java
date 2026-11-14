package br.ufpb.dcx.poo.biblioteca.persistencia;
import java.time.LocalDate;
import java.util.List;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
/** Snapshot sem entidades mutáveis: fronteira entre domínio e arquivos. */
public record DadosBiblioteca(List<ItemView> itens, List<ExemplarView> exemplares,
        List<UsuarioView> usuarios, List<EmprestimoView> emprestimos, List<ReservaDados> reservas) {
    public DadosBiblioteca {
        itens = List.copyOf(itens); exemplares = List.copyOf(exemplares);
        usuarios = List.copyOf(usuarios); emprestimos = List.copyOf(emprestimos); reservas = List.copyOf(reservas);
    }
    public record ReservaDados(String id, String codigo, String matricula, LocalDate data,
                               String situacao, String tombo) { }
    public static DadosBiblioteca vazio() {
        return new DadosBiblioteca(List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
