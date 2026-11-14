package br.ufpb.dcx.poo.biblioteca.inicial;
import java.time.LocalDate;
import br.ufpb.dcx.poo.biblioteca.contrato.ReservaView;
/** Uma reserva guarda sua identidade e o exemplar separado, quando houver. */
final class Reserva {
    enum Situacao { ATIVA, ATENDIDA, CANCELADA }
    private final String id;
    private final Item item;
    private final Usuario usuario;
    private final LocalDate data;
    private Situacao situacao = Situacao.ATIVA;
    private Exemplar separado;
    Reserva(String id, Item item, Usuario usuario, LocalDate data) {
        this.id = id; this.item = item; this.usuario = usuario; this.data = data;
    }
    String id() { return id; }
    Item item() { return item; }
    Usuario usuario() { return usuario; }
    LocalDate data() { return data; }
    Situacao situacao() { return situacao; }
    boolean ativa() { return situacao == Situacao.ATIVA; }
    Exemplar separado() { return separado; }
    void separar(Exemplar exemplar) { separado = exemplar; exemplar.reservar(); }
    void voltarAEsperar() { separado = null; }
    void encerrar(Situacao situacao) { this.situacao = situacao; separado = null; }
    ReservaView view(int posicao) { return new ReservaView(id, item.codigo(), usuario.matricula(), data, posicao); }
}
