package br.ufpb.dcx.poo.biblioteca;

import java.time.LocalDate;
import java.util.Map;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaOcupacaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 20);
    private Biblioteca biblioteca;

    @BeforeEach
    void preparar() throws Exception {
        biblioteca = Fabrica.novaBiblioteca();
        biblioteca.acervo().cadastrarItem("A", "Alfa", "Autor", "Livro", 2000);
        biblioteca.acervo().cadastrarItem("B", "Beta", "Autor", "livro", 2000);
        biblioteca.acervo().cadastrarItem("C", "Gama", "Autor", "Jogos", 2000);
        biblioteca.usuarios().cadastrarUsuario("U", "Ana");
        biblioteca.usuarios().cadastrarUsuario("V", "Bia");
    }

    @Test
    void agrupaCategoriasSemDistinguirCaixaEContaDevolvidos() throws Exception {
        biblioteca.acervo().adicionarExemplar("A", "A1");
        biblioteca.acervo().adicionarExemplar("B", "B1");
        biblioteca.emprestimos().emprestar("A1", "U", DIA);
        biblioteca.emprestimos().devolver("A1", DIA);
        biblioteca.emprestimos().emprestar("B1", "U", DIA);

        assertEquals(Map.of("livro", 2L), biblioteca.relatorios().emprestimosPorCategoria());
        assertEquals(1, biblioteca.usuarios().buscarUsuario("U").emprestimosAtivos());
    }

    @Test
    void semEmprestimosMapeamentoVazio() {
        assertEquals(Map.of(), biblioteca.relatorios().emprestimosPorCategoria());
    }

    @ParameterizedTest(name = "sem exemplares em circulação, com exemplar baixado = {0}")
    @ValueSource(booleans = {false, true})
    void ocupacaoZeroSemExemplaresEmCirculacao(boolean incluirBaixado) throws Exception {
        if (incluirBaixado) {
            biblioteca.acervo().adicionarExemplar("A", "A1");
            biblioteca.acervo().baixarExemplar("A1");
        }

        assertEquals(0.0, biblioteca.relatorios().taxaDeOcupacaoDoAcervo());
    }

    @Test
    void ocupacaoIncluiReservadosNoDenominadorEMantemAReserva() throws Exception {
        biblioteca.acervo().adicionarExemplar("A", "A1");
        biblioteca.acervo().adicionarExemplar("B", "B1");
        biblioteca.acervo().adicionarExemplar("C", "C1");
        biblioteca.acervo().baixarExemplar("C1");
        biblioteca.emprestimos().emprestar("A1", "U", DIA);
        biblioteca.emprestimos().emprestar("B1", "U", DIA);
        String reserva = biblioteca.emprestimos().reservar("B", "V", DIA);
        assertEquals(1.0, biblioteca.relatorios().taxaDeOcupacaoDoAcervo());

        biblioteca.emprestimos().devolver("B1", DIA);

        assertEquals(0.5, biblioteca.relatorios().taxaDeOcupacaoDoAcervo());
        assertEquals(reserva, biblioteca.emprestimos().listarReservas("B").getFirst().id());
    }
}
