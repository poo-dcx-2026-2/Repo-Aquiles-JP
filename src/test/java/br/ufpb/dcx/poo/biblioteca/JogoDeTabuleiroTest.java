package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.inicial.JogoDeTabuleiro;

/** Testes da regra de negócio autoral do acervo de jogos. */
class JogoDeTabuleiroTest {

    @Test
    void faixaDeJogadoresValida() {
        assertEquals(0, new JogoDeTabuleiro("J-01", 2, 4).jogadoresMinimos() - 2);
    }

    @Test
    void comportaGrupoAceitaLimitesMinimoEMaximoInclusive() {
        JogoDeTabuleiro jogo = new JogoDeTabuleiro("J-01", 2, 4);

        assertEquals(true, jogo.comportaGrupo(2));
        assertEquals(true, jogo.comportaGrupo(3));
        assertEquals(true, jogo.comportaGrupo(4));
    }

    @Test
    void comportaGrupoRetornaFalseForaDaFaixaParaQuantidadesPositivas() {
        JogoDeTabuleiro jogo = new JogoDeTabuleiro("J-01", 2, 4);

        assertEquals(false, jogo.comportaGrupo(1));
        assertEquals(false, jogo.comportaGrupo(5));
    }

    @Test
    void comportaGrupoRejeitaQuantidadeMenorQueUm() {
        JogoDeTabuleiro jogo = new JogoDeTabuleiro("J-01", 2, 4);

        assertThrows(DadosInvalidosException.class, () -> jogo.comportaGrupo(0));
        assertThrows(DadosInvalidosException.class, () -> jogo.comportaGrupo(-1));
    }

    @Test
    void maximoDeJogadoresNaoPodeSerMenorQueMinimo() {
        assertThrows(DadosInvalidosException.class,
                () -> new JogoDeTabuleiro("J-01", 4, 2));
    }

    @Test
    void jogoSoloEhValido() {
        assertEquals(true, new JogoDeTabuleiro("SOLO", 1, 1).comportaGrupo(1));
    }

    @Test
    void minimoDeveSerPositivo() {
        assertThrows(DadosInvalidosException.class, () -> new JogoDeTabuleiro("J", 0, 4));
        assertThrows(DadosInvalidosException.class, () -> new JogoDeTabuleiro("J", -1, 4));
    }

    @Test
    void codigoObrigatorio() {
        assertThrows(DadosInvalidosException.class, () -> new JogoDeTabuleiro(null, 2, 4));
        assertThrows(DadosInvalidosException.class, () -> new JogoDeTabuleiro(" ", 2, 4));
    }
}
