package br.ufpb.dcx.poo.biblioteca.inicial;
import java.time.LocalDate;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
final class Validacao {
    private Validacao() { }
    static void texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new DadosInvalidosException(campo + " é obrigatório.");
    }
    static void data(LocalDate data) {
        if (data == null) throw new DadosInvalidosException("Data é obrigatória.");
    }
}
