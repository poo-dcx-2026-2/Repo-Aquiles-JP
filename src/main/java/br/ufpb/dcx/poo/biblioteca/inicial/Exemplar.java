package br.ufpb.dcx.poo.biblioteca.inicial;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
/** Cópia física: suas transições de estado ficam junto do estado. */
final class Exemplar {
    private final String tombo;
    private final String codigoDoItem;
    private StatusExemplar status = StatusExemplar.DISPONIVEL;
    Exemplar(String tombo, String codigoDoItem) { this.tombo = tombo; this.codigoDoItem = codigoDoItem; }
    String tombo() { return tombo; }
    String codigoDoItem() { return codigoDoItem; }
    StatusExemplar status() { return status; }
    void exigirEmprestavel(Usuario usuario, Reserva reserva) throws ExemplarIndisponivelException {
        if (status == StatusExemplar.EMPRESTADO || status == StatusExemplar.INDISPONIVEL
                || (status == StatusExemplar.RESERVADO && (reserva == null || reserva.usuario() != usuario)))
            throw new ExemplarIndisponivelException("Exemplar indisponível: " + tombo);
    }
    void baixar() throws OperacaoNaoPermitidaException {
        if (status == StatusExemplar.EMPRESTADO || status == StatusExemplar.INDISPONIVEL)
            throw new OperacaoNaoPermitidaException("Não é possível baixar " + tombo);
        status = StatusExemplar.INDISPONIVEL;
    }
    void emprestar() { status = StatusExemplar.EMPRESTADO; }
    void disponibilizar() { status = StatusExemplar.DISPONIVEL; }
    void reservar() { status = StatusExemplar.RESERVADO; }
}
