package br.ufpb.dcx.poo.biblioteca.inicial;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
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
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

/** Consultas de leitura sobre o estado atual da biblioteca. */
final class RelatoriosEmMemoria implements RelatorioService {
    private final EstadoBiblioteca estado;

    RelatoriosEmMemoria(EstadoBiblioteca estado) {
        this.estado = estado;
    }

    @Override
    public List<ItemView> itensMaisEmprestados(int n) {
        throw new UnsupportedOperationException("Entrega 3: implementar itensMaisEmprestados");
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
        throw new UnsupportedOperationException("Entrega 3: implementar usuariosComAtraso");
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
