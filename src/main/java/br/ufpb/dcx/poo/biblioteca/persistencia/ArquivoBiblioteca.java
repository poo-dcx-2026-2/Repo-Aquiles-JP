package br.ufpb.dcx.poo.biblioteca.persistencia;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;
import br.ufpb.dcx.poo.biblioteca.persistencia.DadosBiblioteca.ReservaDados;

/** Arquivo textual versionado. Base64 protege tabulações e quebras de linha dos textos. */
public final class ArquivoBiblioteca {
    private Path arquivo() {
        return Path.of(System.getProperty("biblioteca.dados", "dados")).resolve("biblioteca-v2.txt");
    }
    public void salvar(DadosBiblioteca dados) throws PersistenciaException {
        Path temporario = null;
        try {
            Path destino = arquivo();
            Files.createDirectories(destino.getParent());
            temporario = Files.createTempFile(destino.getParent(), "biblioteca-", ".tmp");
            try (BufferedWriter out = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                out.write("BIBLIOTECA-2"); out.newLine();
                int linhas = 0;
                for (ItemView i : dados.itens()) {
                    linha(out, "I", texto(i.codigo()), texto(i.titulo()), texto(i.autoria()), texto(i.categoria()), i.ano()); linhas++;
                }
                for (ExemplarView e : dados.exemplares()) {
                    linha(out, "E", texto(e.tombo()), texto(e.codigoDoItem()), e.status()); linhas++;
                }
                for (UsuarioView u : dados.usuarios()) {
                    linha(out, "U", texto(u.matricula()), texto(u.nome()), u.ativo()); linhas++;
                }
                for (EmprestimoView e : dados.emprestimos()) {
                    linha(out, "P", texto(e.id()), texto(e.tombo()), texto(e.matricula()), e.dataDoEmprestimo(),
                        e.dataPrevistaDeDevolucao(), e.dataDaDevolucao() == null ? "-" : e.dataDaDevolucao(), e.status()); linhas++;
                }
                for (ReservaDados r : dados.reservas()) {
                    linha(out, "R", texto(r.id()), texto(r.codigo()), texto(r.matricula()), r.data(), r.situacao(), texto(r.tombo())); linhas++;
                }
                linha(out, "FIM", linhas);
            }
            try {
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | InvalidPathException | SecurityException ex) {
            throw falha("Não foi possível salvar a biblioteca.", ex);
        } finally {
            if (temporario != null) {
                try { Files.deleteIfExists(temporario); }
                catch (IOException ex) { throw falha("Não foi possível remover o arquivo temporário.", ex); }
            }
        }
    }
    public DadosBiblioteca carregar() throws PersistenciaException {
        try {
            Path origem = arquivo();
            // notExists distingue ausência de falta de permissão para consultar o caminho.
            if (Files.notExists(origem)) return DadosBiblioteca.vazio();
            List<ItemView> itens = new ArrayList<>();
            List<ExemplarView> exemplares = new ArrayList<>();
            List<UsuarioView> usuarios = new ArrayList<>();
            List<EmprestimoView> emprestimos = new ArrayList<>();
            List<ReservaDados> reservas = new ArrayList<>();
            try (BufferedReader in = Files.newBufferedReader(origem, StandardCharsets.UTF_8)) {
                exigir("BIBLIOTECA-2".equals(in.readLine()), "Cabeçalho inválido");
                int linhas = 0;
                boolean fim = false;
                String linha;
                while ((linha = in.readLine()) != null) {
                    exigir(!fim, "Conteúdo após o fim");
                    String[] c = linha.split("\t", -1);
                    switch (c[0]) {
                        case "I" -> { campos(c, 6); itens.add(new ItemView(valor(c[1]), valor(c[2]), valor(c[3]), valor(c[4]), Integer.parseInt(c[5]), 0, 0)); }
                        case "E" -> { campos(c, 4); exemplares.add(new ExemplarView(valor(c[1]), valor(c[2]), StatusExemplar.valueOf(c[3]))); }
                        case "U" -> {
                            campos(c, 4); exigir(c[3].equals("true") || c[3].equals("false"), "Booleano inválido");
                            usuarios.add(new UsuarioView(valor(c[1]), valor(c[2]), Boolean.parseBoolean(c[3]), 0));
                        }
                        case "P" -> {
                            campos(c, 8); emprestimos.add(new EmprestimoView(valor(c[1]), valor(c[2]), valor(c[3]),
                                LocalDate.parse(c[4]), LocalDate.parse(c[5]), c[6].equals("-") ? null : LocalDate.parse(c[6]), StatusEmprestimo.valueOf(c[7])));
                        }
                        case "R" -> { campos(c, 7); reservas.add(new ReservaDados(valor(c[1]), valor(c[2]), valor(c[3]), LocalDate.parse(c[4]), c[5], valor(c[6]))); }
                        case "FIM" -> { campos(c, 2); exigir(Integer.parseInt(c[1]) == linhas, "Contagem inválida"); fim = true; }
                        default -> throw new IllegalArgumentException("Registro desconhecido");
                    }
                    if (!fim) linhas++;
                }
                exigir(fim, "Arquivo truncado");
            }
            return new DadosBiblioteca(itens, exemplares, usuarios, emprestimos, reservas);
        } catch (IOException | IllegalArgumentException | java.time.DateTimeException | SecurityException ex) {
            throw falha("Não foi possível carregar a biblioteca.", ex);
        }
    }
    private static void linha(BufferedWriter out, Object... campos) throws IOException {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) out.write('\t');
            out.write(campos[i].toString());
        }
        out.newLine();
    }
    private static String texto(String valor) {
        return valor == null ? "-" : Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
    private static String valor(String texto) {
        return texto.equals("-") ? null : new String(Base64.getDecoder().decode(texto), StandardCharsets.UTF_8);
    }
    private static void campos(String[] c, int quantidade) { exigir(c.length == quantidade, "Linha mal formada"); }
    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new IllegalArgumentException(mensagem);
    }
    public static PersistenciaException falha(String mensagem, Throwable causa) {
        PersistenciaException falha = new PersistenciaException(mensagem);
        falha.initCause(causa); // contrato congelado não oferece construtor com causa
        return falha;
    }
}
