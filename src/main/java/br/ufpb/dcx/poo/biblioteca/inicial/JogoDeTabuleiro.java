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

    /**
     * Informa se a quantidade de jogadores está dentro da faixa permitida.
     *
     * @param quantidadeDeJogadores quantidade de participantes do grupo
     * @return true se a quantidade estiver entre o mínimo e o máximo, inclusive
     * @throws DadosInvalidosException se a quantidade for menor que um
     */
    public boolean comportaGrupo(int quantidadeDeJogadores) {
        if (quantidadeDeJogadores < 1) {
            throw new DadosInvalidosException("A quantidade de jogadores deve ser positiva.");
        }
        return quantidadeDeJogadores >= jogadoresMinimos
                && quantidadeDeJogadores <= jogadoresMaximos;
    }
}
