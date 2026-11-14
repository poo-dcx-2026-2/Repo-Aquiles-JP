package br.ufpb.dcx.poo.biblioteca;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaTest {
    @TempDir Path pasta;
    private String anterior;
    private Biblioteca b;
    private static final LocalDate DIA = LocalDate.of(2026, 10, 1);
    @BeforeEach void preparar() throws Exception {
        anterior = System.getProperty("biblioteca.dados");
        System.setProperty("biblioteca.dados", pasta.resolve("dados").toString());
        b = Fabrica.novaBiblioteca();
        b.acervo().cadastrarItem("I", "Catan\tEdição\nEspecial", null, "Jogos", 2020);
        for (String t : List.of("T", "T2", "T3", "T4")) b.acervo().adicionarExemplar("I", t);
        for (String u : List.of("U", "V", "W", "X", "Y")) b.usuarios().cadastrarUsuario(u, "Pessoa " + u);
    }
    @AfterEach void restaurar() {
        if (anterior == null) System.clearProperty("biblioteca.dados");
        else System.setProperty("biblioteca.dados", anterior);
    }
    private Path arquivo() { return pasta.resolve("dados/biblioteca-v2.txt"); }

    @Test void idaEVoltaDoEstadoCompletoEContinuidadeDosIds() throws Exception {
        b.emprestimos().emprestar("T", "U", DIA);
        b.emprestimos().devolver("T", DIA);
        b.emprestimos().emprestar("T", "U", DIA);
        b.emprestimos().emprestar("T2", "V", DIA);
        b.acervo().baixarExemplar("T3");
        b.acervo().baixarExemplar("T4");
        String cancelada = b.emprestimos().reservar("I", "Y", DIA);
        b.emprestimos().cancelarReserva(cancelada);
        String atendida = b.emprestimos().reservar("I", "W", DIA);
        b.emprestimos().devolver("T", DIA);
        b.emprestimos().emprestar("T", "W", DIA);
        b.emprestimos().reservar("I", "U", DIA);
        b.emprestimos().reservar("I", "X", DIA);
        b.emprestimos().devolver("T", DIA.plusDays(1));
        b.usuarios().desativarUsuario("Y");
        Path exemplo = pasta.resolve("dados/itens-exemplo.csv");
        Files.createDirectories(exemplo.getParent()); Files.writeString(exemplo, "não sobrescrever");
        b.salvar();
        Biblioteca nova = Fabrica.novaBiblioteca();
        var acervoRetido = nova.acervo(); var usuariosRetidos = nova.usuarios(); var emprestimosRetidos = nova.emprestimos();
        assertTrue(nova.acervo().listarItens().isEmpty()); // fábrica não carrega automaticamente
        nova.carregar();
        assertEquals(b.acervo().listarItens(), acervoRetido.listarItens());
        assertEquals(b.acervo().listarExemplares("I"), acervoRetido.listarExemplares("I"));
        assertEquals(b.usuarios().listarUsuarios(), usuariosRetidos.listarUsuarios());
        for (UsuarioView u : b.usuarios().listarUsuarios())
            assertEquals(b.emprestimos().listarEmprestimosDoUsuario(u.matricula()), emprestimosRetidos.listarEmprestimosDoUsuario(u.matricula()));
        assertEquals(b.emprestimos().listarReservas("I"), nova.emprestimos().listarReservas("I"));
        assertEquals(b.emprestimos().listarAtrasados(DIA.plusDays(15)), nova.emprestimos().listarAtrasados(DIA.plusDays(15)));
        assertThrows(RecursoNaoEncontradoException.class, () -> nova.emprestimos().cancelarReserva(cancelada));
        assertThrows(RecursoNaoEncontradoException.class, () -> nova.emprestimos().cancelarReserva(atendida));
        Set<String> ids = new HashSet<>();
        for (UsuarioView u : b.usuarios().listarUsuarios())
            for (EmprestimoView e : b.emprestimos().listarEmprestimosDoUsuario(u.matricula())) ids.add(e.id());
        for (ReservaView r : b.emprestimos().listarReservas("I")) ids.add(r.id());
        assertTrue(ids.add(nova.emprestimos().emprestar("T", "U", DIA.plusDays(2))));
        assertEquals("não sobrescrever", Files.readString(exemplo));
        assertEquals(2, b.emprestimos().listarReservas("I").size()); // instâncias independentes
    }

    @Test void ausenciaDeArquivoOuDiretorioSubstituiPorEstadoVazio() throws Exception {
        b.carregar();
        assertTrue(b.acervo().listarItens().isEmpty()); assertTrue(b.usuarios().listarUsuarios().isEmpty());
        Files.createDirectories(arquivo().getParent());
        b.acervo().cadastrarItem("NOVO", "Novo", "", "", 2020);
        b.carregar(); assertTrue(b.acervo().listarItens().isEmpty());
    }

    @Test void carregarSubstituiEstadoEmVezDeMesclar() throws Exception {
        b.salvar();
        b.acervo().cadastrarItem("EXTRA", "Extra", "", "", 2020);
        b.usuarios().cadastrarUsuario("EXTRA", "Extra");
        b.carregar();
        assertEquals(1, b.acervo().listarItens().size());
        assertThrows(RecursoNaoEncontradoException.class, () -> b.usuarios().buscarUsuario("EXTRA"));
        b.salvar(); b.carregar();
        assertEquals(4, b.acervo().buscarItem("I").totalDeExemplares());
    }

    @ParameterizedTest @ValueSource(strings = {"cabecalho", "linha", "numero", "tombo", "matricula", "codigo", "data", "status", "duplicado", "truncado", "reserva", "booleano", "prazo"})
    void arquivoCorrompidoNaoAlteraNenhumaVisao(String erro) throws Exception {
        b.emprestimos().emprestar("T", "U", DIA);
        b.salvar();
        List<String> linhas = new ArrayList<>(Files.readAllLines(arquivo()));
        int indice = switch (erro) {
            case "numero", "duplicado" -> 1;
            case "tombo", "matricula", "data", "prazo" -> linhas.size() - 2;
            case "codigo", "status" -> 2;
            case "booleano" -> 6;
            default -> 0;
        };
        String[] c = linhas.get(indice).split("\t", -1);
        switch (erro) {
            case "cabecalho" -> linhas.set(0, "INVALIDO");
            case "linha" -> linhas.set(1, "I\tmalformada");
            case "numero" -> { c[5] = "nao-e-numero"; linhas.set(indice, String.join("\t", c)); }
            case "tombo" -> { c[2] = "WA=="; linhas.set(indice, String.join("\t", c)); }
            case "matricula" -> { c[3] = "Wg=="; linhas.set(indice, String.join("\t", c)); }
            case "codigo" -> { c[2] = "Wg=="; linhas.set(indice, String.join("\t", c)); }
            case "data" -> { c[4] = "2026-02-30"; linhas.set(indice, String.join("\t", c)); }
            case "prazo" -> { c[5] = "2026-12-31"; linhas.set(indice, String.join("\t", c)); }
            case "status" -> { c[3] = "RESERVADO"; linhas.set(indice, String.join("\t", c)); }
            case "duplicado" -> { linhas.add(2, linhas.get(1)); linhas.set(linhas.size() - 1, "FIM\t" + (linhas.size() - 2)); }
            case "truncado" -> linhas.removeLast();
            case "booleano" -> { c[3] = "talvez"; linhas.set(indice, String.join("\t", c)); }
            case "reserva" -> { linhas.add(linhas.size() - 1, "R\tUg==\tSQ==\tVg==\t2026-10-01\tATIVA\tVEFCTy1JTkVYSVNURU5URQ=="); linhas.set(linhas.size() - 1, "FIM\t" + (linhas.size() - 2)); }
        }
        Files.write(arquivo(), linhas);
        var itens = b.acervo().listarItens(); var usuarios = b.usuarios().listarUsuarios();
        var exemplares = b.acervo().listarExemplares("I"); var historico = b.emprestimos().listarEmprestimosDoUsuario("U");
        PersistenciaException falha = assertThrows(PersistenciaException.class, b::carregar);
        assertNotNull(falha.getCause());
        assertEquals(itens, b.acervo().listarItens()); assertEquals(usuarios, b.usuarios().listarUsuarios());
        assertEquals(exemplares, b.acervo().listarExemplares("I")); assertEquals(historico, b.emprestimos().listarEmprestimosDoUsuario("U"));
        assertTrue(b.emprestimos().listarReservas("I").isEmpty());
    }

    @Test void falhasDeEscritaELeituraPreservamCausa() throws Exception {
        Path bloqueado = pasta.resolve("arquivo-como-diretorio"); Files.writeString(bloqueado, "arquivo");
        System.setProperty("biblioteca.dados", bloqueado.toString());
        assertInstanceOf(IOException.class, assertThrows(PersistenciaException.class, b::salvar).getCause());
        System.setProperty("biblioteca.dados", pasta.resolve("dados").toString());
        Files.createDirectories(arquivo());
        assertInstanceOf(IOException.class, assertThrows(PersistenciaException.class, b::carregar).getCause());
        assertEquals(1, b.acervo().listarItens().size());
    }
}
