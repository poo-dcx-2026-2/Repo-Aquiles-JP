package br.ufpb.dcx.poo.biblioteca;

import java.time.LocalDate;
import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RankingAtrasosTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 20);
    private Biblioteca biblioteca;

    @BeforeEach
    void preparar() throws Exception {
        biblioteca = Fabrica.novaBiblioteca();
        biblioteca.acervo().cadastrarItem("A", "alfa", "Autor", "Jogos", 2000);
        biblioteca.acervo().cadastrarItem("B", "Beta", "Autor", "Jogos", 2000);
        biblioteca.acervo().cadastrarItem("C", "Gama", "Autor", "Jogos", 2000);
        biblioteca.acervo().cadastrarItem("D", "Delta", "Autor", "Jogos", 2000);
        biblioteca.acervo().adicionarExemplar("A", "A1");
        biblioteca.acervo().adicionarExemplar("B", "B1");
        biblioteca.acervo().adicionarExemplar("C", "C1");
        biblioteca.usuarios().cadastrarUsuario("V", "Bia");
        biblioteca.usuarios().cadastrarUsuario("U", "Ana");
        biblioteca.usuarios().cadastrarUsuario("W", "Caio");
    }

    @ParameterizedTest(name = "ranking rejeita limite {0}")
    @ValueSource(ints = {0, -1})
    void rejeitaLimitesInvalidos(int limite) {
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.relatorios().itensMaisEmprestados(limite));
    }

    @Test
    void rankingContaDevolvidosDesempataIgnorandoCaixaEAplicaLimite() throws Exception {
        assertEquals(List.of(), biblioteca.relatorios().itensMaisEmprestados(1));
        biblioteca.emprestimos().emprestar("B1", "V", DIA);
        biblioteca.emprestimos().emprestar("A1", "U", DIA);
        biblioteca.emprestimos().emprestar("C1", "U", DIA);
        biblioteca.emprestimos().devolver("C1", DIA);
        biblioteca.emprestimos().emprestar("C1", "U", DIA);

        List<ItemView> ranking = biblioteca.relatorios().itensMaisEmprestados(5);
        assertEquals(List.of("C", "A", "B"), ranking.stream().map(ItemView::codigo).toList());
        assertEquals(ranking.subList(0, 2), biblioteca.relatorios().itensMaisEmprestados(2));
        assertEquals(biblioteca.acervo().buscarItem("C"), ranking.getFirst());
        biblioteca.emprestimos().devolver("C1", DIA);
        assertEquals(0, ranking.getFirst().exemplaresDisponiveis());
        assertEquals(1, biblioteca.relatorios().itensMaisEmprestados(1).getFirst().exemplaresDisponiveis());
    }

    @Test
    void atrasosDistinguemVencimentoDeAtrasoEDeduplicamPorMatricula() throws Exception {
        biblioteca.emprestimos().emprestar("C1", "W", DIA);
        biblioteca.emprestimos().devolver("C1", DIA.plusDays(16));
        biblioteca.emprestimos().emprestar("C1", "V", DIA);
        biblioteca.emprestimos().emprestar("A1", "U", DIA);
        biblioteca.emprestimos().emprestar("B1", "U", DIA);

        assertEquals(List.of(), biblioteca.relatorios().usuariosComAtraso(DIA.plusDays(14)));
        List<UsuarioView> atrasados = biblioteca.relatorios().usuariosComAtraso(DIA.plusDays(15));
        assertEquals(List.of("U", "V"), atrasados.stream().map(UsuarioView::matricula).toList());
        assertEquals(2, atrasados.getFirst().emprestimosAtivos());
        biblioteca.emprestimos().devolver("A1", DIA.plusDays(15));
        assertEquals(2, atrasados.getFirst().emprestimosAtivos());
        assertEquals(1, biblioteca.relatorios().usuariosComAtraso(DIA.plusDays(15))
                .getFirst().emprestimosAtivos());
    }

    @Test
    void dataNulaEhInvalidaMesmoComAcervoSemEmprestimos() {
        assertThrows(DadosInvalidosException.class, () -> biblioteca.relatorios().usuariosComAtraso(null));
    }
}
