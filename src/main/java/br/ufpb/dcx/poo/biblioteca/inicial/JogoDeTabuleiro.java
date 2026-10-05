package br.ufpb.dcx.poo.biblioteca.inicial;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;

/** Extensão autoral: metadados específicos de um jogo de tabuleiro. */
public final class JogoDeTabuleiro {

    private final String codigoDoItem;
    private final int jogadoresMinimos;
    private final int jogadoresMaximos;

    public JogoDeTabuleiro(String codigoDoItem, int jogadoresMinimos, int jogadoresMaximos) {
        if (codigoDoItem == null || codigoDoItem.isBlank()) {
            throw new DadosInvalidosException("O código do item é obrigatório.");
        }
        if (jogadoresMinimos < 1 || jogadoresMaximos < jogadoresMinimos) {
            throw new DadosInvalidosException("A faixa de jogadores deve ser válida.");
        }
        this.codigoDoItem = codigoDoItem;
        this.jogadoresMinimos = jogadoresMinimos;
        this.jogadoresMaximos = jogadoresMaximos;
    }

    public String codigoDoItem() { return codigoDoItem; }
    public int jogadoresMinimos() { return jogadoresMinimos; }
    public int jogadoresMaximos() { return jogadoresMaximos; }
}
