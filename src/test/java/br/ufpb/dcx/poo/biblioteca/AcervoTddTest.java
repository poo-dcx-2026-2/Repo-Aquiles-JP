package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.ExemplarView;
import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;

/** Cenários de fronteira adicionados pela equipe com o ciclo TDD. */
class AcervoTddTest {

    private Biblioteca biblioteca;

    @BeforeEach
    void criarBibliotecaVazia() {
        biblioteca = Fabrica.novaBiblioteca();
    }

    @Test
    @DisplayName("busca por título mantém a ordenação alfabética dos resultados")
    void buscaPorTituloOrdenada() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("J2", "Zombicide", "Guillotin", "jogo", 2012);
        biblioteca.acervo().cadastrarItem("J1", "Azul", "Kiesling", "jogo", 2017);

        List<ItemView> resultado = biblioteca.acervo().buscarPorTitulo("z");

        assertEquals(List.of("Azul", "Zombicide"), resultado.stream().map(ItemView::titulo).toList());
    }

    @Test
    @DisplayName("tombo em branco é rejeitado antes de alterar o item")
    void tomboEmBrancoNaoDeixaResiduo() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("J1", "Azul", "Kiesling", "jogo", 2017);

        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.acervo().adicionarExemplar("J1", " "));

        assertEquals(0, biblioteca.acervo().buscarItem("J1").totalDeExemplares());
    }

    @Test
    @DisplayName("lista de exemplares é devolvida ordenada por tombo")
    void listarExemplaresOrdenada() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("J1", "Azul", "Kiesling", "jogo", 2017);
        biblioteca.acervo().adicionarExemplar("J1", "T-002");
        biblioteca.acervo().adicionarExemplar("J1", "T-001");

        List<ExemplarView> exemplares = biblioteca.acervo().listarExemplares("J1");

        assertEquals(List.of("T-001", "T-002"), exemplares.stream().map(ExemplarView::tombo).toList());
    }
}
