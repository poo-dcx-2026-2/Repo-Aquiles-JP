package br.ufpb.dcx.poo.biblioteca;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Entrega2Test {
    private Biblioteca b;
    private static final LocalDate DIA = LocalDate.of(2026, 10, 1);

    @BeforeEach void preparar() throws Exception {
        b = Fabrica.novaBiblioteca();
        b.acervo().cadastrarItem("I", "Catan", "Autor", "Jogos", 2020);
        b.acervo().adicionarExemplar("I", "T");
        b.usuarios().cadastrarUsuario("U", "Ana");
        b.usuarios().cadastrarUsuario("V", "Bruno");
        b.usuarios().cadastrarUsuario("W", "Carla");
    }

    @Test void emprestimoDevolucaoPrazoEAtrasoPelasVisoes() throws Exception {
        String id = b.emprestimos().emprestar("T", "U", DIA);
        assertFalse(id.isBlank());
        EmprestimoView e = b.emprestimos().listarEmprestimosDoUsuario("U").getFirst();
        assertEquals(new EmprestimoView(id, "T", "U", DIA, DIA.plusDays(14), null, StatusEmprestimo.ATIVO), e);
        assertEquals(1, b.usuarios().buscarUsuario("U").emprestimosAtivos());
        assertEquals(0, b.acervo().buscarItem("I").exemplaresDisponiveis());
        assertTrue(b.emprestimos().listarAtrasados(DIA.plusDays(14)).isEmpty());
        assertEquals(StatusEmprestimo.ATRASADO, b.emprestimos().listarAtrasados(DIA.plusDays(15)).getFirst().status());
        b.emprestimos().devolver("T", DIA.plusDays(15));
        e = b.emprestimos().listarEmprestimosDoUsuario("U").getFirst();
        assertEquals(StatusEmprestimo.DEVOLVIDO, e.status());
        assertEquals(DIA.plusDays(15), e.dataDaDevolucao());
        assertEquals(0, b.usuarios().buscarUsuario("U").emprestimosAtivos());
        assertEquals(1, b.acervo().buscarItem("I").exemplaresDisponiveis());
        assertTrue(b.emprestimos().listarAtrasados(DIA.plusDays(30)).isEmpty());
    }

    @Test void cicloDeReservaRespeitaFilaEAtendeSomenteReservante() throws Exception {
        b.emprestimos().emprestar("T", "U", DIA);
        String r = b.emprestimos().reservar("I", "V", DIA);
        String s = b.emprestimos().reservar("I", "W", DIA.minusDays(1));
        assertEquals(List.of(new ReservaView(r, "I", "V", DIA, 1), new ReservaView(s, "I", "W", DIA.minusDays(1), 2)), b.emprestimos().listarReservas("I"));
        b.emprestimos().devolver("T", DIA);
        assertEquals(StatusExemplar.RESERVADO, b.acervo().listarExemplares("I").getFirst().status());
        assertThrows(ExemplarIndisponivelException.class, () -> b.emprestimos().emprestar("T", "W", DIA));
        assertEquals(2, b.emprestimos().listarReservas("I").size());
        b.emprestimos().emprestar("T", "V", DIA);
        assertEquals(s, b.emprestimos().listarReservas("I").getFirst().id());
        assertEquals(1, b.emprestimos().listarReservas("I").getFirst().posicaoNaFila());
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().cancelarReserva(r));
        b.emprestimos().devolver("T", DIA);
        b.emprestimos().cancelarReserva(s);
        assertEquals(StatusExemplar.DISPONIVEL, b.acervo().listarExemplares("I").getFirst().status());
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().cancelarReserva(s));
    }

    @Test void cancelamentoTransfereExemplarEBaixaDeReservaVoltaAEsperar() throws Exception {
        b.emprestimos().emprestar("T", "U", DIA);
        String r = b.emprestimos().reservar("I", "V", DIA);
        b.emprestimos().reservar("I", "W", DIA);
        b.emprestimos().devolver("T", DIA);
        b.emprestimos().cancelarReserva(r);
        assertThrows(ExemplarIndisponivelException.class, () -> b.emprestimos().emprestar("T", "V", DIA));
        b.acervo().baixarExemplar("T");
        assertEquals(StatusExemplar.INDISPONIVEL, b.acervo().listarExemplares("I").getFirst().status());
        assertEquals(1, b.acervo().buscarItem("I").totalDeExemplares());
        assertEquals(1, b.emprestimos().listarReservas("I").size());
        b.acervo().adicionarExemplar("I", "NOVO");
        b.emprestimos().emprestar("NOVO", "V", DIA);
        b.emprestimos().devolver("NOVO", DIA);
        b.emprestimos().emprestar("NOVO", "W", DIA);
        assertTrue(b.emprestimos().listarReservas("I").isEmpty());
    }

    @Test void limiteDeTresEOrdemDeValidacoesSemResiduos() throws Exception {
        b.emprestimos().emprestar("T", "U", DIA);
        for (int i = 2; i <= 4; i++) b.acervo().adicionarExemplar("I", "T" + i);
        b.emprestimos().emprestar("T2", "U", DIA.plusDays(1));
        String ultimo = b.emprestimos().emprestar("T3", "U", DIA.plusDays(1));
        assertEquals(ultimo, b.emprestimos().listarEmprestimosDoUsuario("U").getFirst().id());
        assertThrows(LimiteDeEmprestimosExcedidoException.class, () -> b.emprestimos().emprestar("T4", "U", DIA));
        assertThrows(ExemplarIndisponivelException.class, () -> b.emprestimos().emprestar("T", "U", DIA));
        assertEquals(StatusExemplar.DISPONIVEL, b.acervo().listarExemplares("I").getLast().status());
        assertEquals(3, b.usuarios().buscarUsuario("U").emprestimosAtivos());
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.usuarios().desativarUsuario("U"));
        assertTrue(b.usuarios().buscarUsuario("U").ativo());
        b.usuarios().desativarUsuario("V");
        assertThrows(UsuarioInativoException.class, () -> b.emprestimos().emprestar("T", "V", DIA));
        assertThrows(UsuarioInativoException.class, () -> b.emprestimos().reservar("I", "V", DIA));
    }

    @Test void errosDeDevolucaoEBaixaPreservamEstado() throws Exception {
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.emprestimos().devolver("T", DIA));
        b.emprestimos().emprestar("T", "U", DIA);
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.acervo().baixarExemplar("T"));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().devolver("T", DIA.minusDays(1)));
        assertEquals(1, b.usuarios().buscarUsuario("U").emprestimosAtivos());
        assertNull(b.emprestimos().listarEmprestimosDoUsuario("U").getFirst().dataDaDevolucao());
        b.emprestimos().devolver("T", DIA);
        b.acervo().baixarExemplar("T");
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.acervo().baixarExemplar("T"));
        assertThrows(ExemplarIndisponivelException.class, () -> b.emprestimos().emprestar("T", "V", DIA));
    }

    @Test void reservasDuplicadasOuComDisponibilidadeSaoRecusadas() throws Exception {
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.emprestimos().reservar("I", "U", DIA));
        b.emprestimos().emprestar("T", "V", DIA);
        b.emprestimos().reservar("I", "U", DIA);
        assertThrows(OperacaoNaoPermitidaException.class, () -> b.emprestimos().reservar("I", "U", DIA));
        b.acervo().cadastrarItem("SEM", "Sem exemplar", "", "Jogos", 2020);
        b.emprestimos().reservar("SEM", "W", DIA);
        assertEquals(1, b.emprestimos().listarReservas("SEM").size());
        b.usuarios().desativarUsuario("U");
        assertTrue(b.emprestimos().listarReservas("I").isEmpty());
        b.usuarios().desativarUsuario("U");
        b.usuarios().reativarUsuario("U");
        b.usuarios().reativarUsuario("U");
        assertTrue(b.usuarios().buscarUsuario("U").ativo());
    }

    @Test void inexistentesEmTodasAsOperacoes() {
        assertThrows(RecursoNaoEncontradoException.class, () -> b.acervo().baixarExemplar("X"));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.usuarios().desativarUsuario("X"));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.usuarios().reativarUsuario("X"));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().emprestar("X", "U", DIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().emprestar("T", "X", DIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().devolver("X", DIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().listarEmprestimosDoUsuario("X"));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().reservar("X", "U", DIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().reservar("I", "X", DIA));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().listarReservas("X"));
        assertThrows(RecursoNaoEncontradoException.class, () -> b.emprestimos().cancelarReserva("X"));
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void entradasInvalidasAntesDeConsultarOuAlterar(String invalido) {
        assertThrows(DadosInvalidosException.class, () -> b.acervo().buscarPorCategoria(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.acervo().baixarExemplar(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.usuarios().desativarUsuario(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.usuarios().reativarUsuario(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().emprestar(invalido, "U", DIA));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().emprestar("X", invalido, DIA));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().devolver(invalido, DIA));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().listarEmprestimosDoUsuario(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().reservar(invalido, "U", DIA));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().reservar("X", invalido, DIA));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().listarReservas(invalido));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().cancelarReserva(invalido));
    }

    @Test void datasNulasTemPrecedenciaSobreRecursosInexistentes() {
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().emprestar("X", "X", null));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().devolver("X", null));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().reservar("X", "X", null));
        assertThrows(DadosInvalidosException.class, () -> b.emprestimos().listarAtrasados(null));
    }

    @Test void buscaPorCategoriaIgnoraCaixaEOrdenaTitulo() throws Exception {
        b.acervo().cadastrarItem("A", "azul", "", "JOGOS", 2020);
        b.acervo().cadastrarItem("B", "Beta", "", "Outra", 2020);
        assertEquals(List.of("A", "I"), b.acervo().buscarPorCategoria("jogos").stream().map(ItemView::codigo).toList());
        assertTrue(b.acervo().buscarPorCategoria("Nada").isEmpty());
    }
}
