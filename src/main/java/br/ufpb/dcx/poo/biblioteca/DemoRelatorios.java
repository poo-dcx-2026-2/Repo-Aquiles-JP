package br.ufpb.dcx.poo.biblioteca;

import java.time.LocalDate;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.RelatorioService;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;

/** Demonstra os quatro relatórios com dados de exemplo em memória. */
public final class DemoRelatorios {
    private DemoRelatorios() {
    }

    public static void main(String[] args) throws BibliotecaException {
        Biblioteca biblioteca = Fabrica.novaBiblioteca();
        LocalDate dia = LocalDate.of(2026, 11, 20);

        biblioteca.acervo().cadastrarItem("J1", "Catan", "Klaus Teuber", "Estratégia", 1995);
        biblioteca.acervo().adicionarExemplar("J1", "T1");
        biblioteca.acervo().adicionarExemplar("J1", "T2");
        biblioteca.usuarios().cadastrarUsuario("U1", "Ana");
        biblioteca.emprestimos().emprestar("T1", "U1", dia);

        RelatorioService relatorios = biblioteca.relatorios();
        System.out.println("Empréstimos por categoria: " + relatorios.emprestimosPorCategoria());
        System.out.println("Taxa de ocupação: " + relatorios.taxaDeOcupacaoDoAcervo());
        System.out.println("Itens mais emprestados: " + relatorios.itensMaisEmprestados(5));
        System.out.println("Usuários com atraso: " + relatorios.usuariosComAtraso(dia.plusDays(15)));
    }
}
