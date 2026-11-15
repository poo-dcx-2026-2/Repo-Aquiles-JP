package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.inicial.JogoDeTabuleiro;

/** Testes da regra de negócio autoral do acervo de jogos. */
class JogoDeTabuleiroTest {

    @Test
    void faixaDeJogadoresValida() {
        assertEquals(2, new JogoDeTabuleiro("J-01", 2, 4).jogadoresMinimos());
    }

    @Test
    void comportaGrupoAceitaLimitesMinimoEMaximoInclusive() {
        JogoDeTabuleiro jogo = new JogoDeTabuleiro("J-01", 2, 4);

        assertTrue(jogo.comportaGrupo(2));
        assertTrue(jogo.comportaGrupo(3));
        assertTrue(jogo.comportaGrupo(4));
    }

    @Test
    void comportaGrupoRetornaFalseForaDaFaixaParaQuantidadesPositivas() {
        JogoDeTabuleiro jogo = new JogoDeTabuleiro("J-01", 2, 4);

        assertFalse(jogo.comportaGrupo(1));
        assertFalse(jogo.comportaGrupo(5));
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
        assertTrue(new JogoDeTabuleiro("SOLO", 1, 1).comportaGrupo(1));
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
