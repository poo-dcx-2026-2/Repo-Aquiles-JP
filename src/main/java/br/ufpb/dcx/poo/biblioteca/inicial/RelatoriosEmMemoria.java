package br.ufpb.dcx.poo.biblioteca.inicial;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.RelatorioService;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

/** Consultas de leitura sobre o estado atual da biblioteca. */
final class RelatoriosEmMemoria implements RelatorioService {
    private final EstadoBiblioteca estado;

    RelatoriosEmMemoria(EstadoBiblioteca estado) {
        this.estado = estado;
    }

    @Override
    public List<ItemView> itensMaisEmprestados(int n) {
        if (n <= 0) {
            throw new DadosInvalidosException("O limite de itens deve ser positivo.");
        }

        Map<String, Long> contagens = estado.emprestimos.values().stream()
                .map(e -> e.exemplar().codigoDoItem())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return estado.itens.values().stream()
                .filter(item -> contagens.containsKey(item.codigo()))
                .sorted(Comparator.<Item>comparingLong(item -> contagens.get(item.codigo()))
                        .reversed()
                        .thenComparing(Item::titulo, String.CASE_INSENSITIVE_ORDER))
                .limit(n)
                .map(Item::view)
                .toList();
    }

    @Override
    public Map<String, Long> emprestimosPorCategoria() {
        Map<String, Long> contagens = estado.emprestimos.values().stream()
                .map(e -> estado.itens.get(e.exemplar().codigoDoItem()).categoria())
                .map(categoria -> categoria == null ? null : categoria.toLowerCase(Locale.ROOT))
                .collect(Collectors.toMap(Function.identity(), categoria -> 1L, Long::sum, HashMap::new));
        return Collections.unmodifiableMap(contagens);
    }

    @Override
    public List<UsuarioView> usuariosComAtraso(LocalDate data) {
        Validacao.data(data);
        return estado.emprestimos.values().stream()
                .filter(emprestimo -> emprestimo.atrasado(data))
                .map(Emprestimo::usuario)
                .distinct()
                .sorted(Comparator.comparing(Usuario::matricula))
                .map(Usuario::view)
                .toList();
    }

    @Override
    public double taxaDeOcupacaoDoAcervo() {
        return estado.exemplares.values().stream()
                .map(Exemplar::status)
                .filter(status -> status != StatusExemplar.INDISPONIVEL)
                .mapToInt(status -> status == StatusExemplar.EMPRESTADO ? 1 : 0)
                .average()
                .orElse(0.0);
    }

    @Override
    public int importarItensEmLote(Path arquivo) throws PersistenciaException {
        throw new UnsupportedOperationException("Importação em lote fora do escopo da Entrega 3");
    }
}
