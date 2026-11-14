package br.ufpb.dcx.poo.biblioteca.inicial;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import br.ufpb.dcx.poo.biblioteca.contrato.AcervoService;
import br.ufpb.dcx.poo.biblioteca.contrato.ExemplarView;
import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

/** Implementação em memória do acervo de jogos de tabuleiro. */
public class AcervoEmMemoria implements AcervoService {

    private final EstadoBiblioteca estado;
    private final Circulacao circulacao;
    public AcervoEmMemoria() { this(new EstadoBiblioteca()); }
    AcervoEmMemoria(EstadoBiblioteca estado) { this.estado = estado; this.circulacao = new Circulacao(estado); }

    @Override
    public void cadastrarItem(String codigo, String titulo, String autoria,
                              String categoria, int ano) throws RecursoDuplicadoException {
        exigirTextoPreenchido(codigo, "codigo");
        exigirTextoPreenchido(titulo, "titulo");
        if (estado.itens.containsKey(codigo)) {
            throw new RecursoDuplicadoException("Já existe item com o código " + codigo);
        }
        estado.itens.put(codigo, new Item(codigo, titulo, autoria, categoria, ano));
    }

    @Override
    public ItemView buscarItem(String codigo) throws RecursoNaoEncontradoException {
        return paraView(localizarItem(codigo));
    }

    @Override
    public List<ItemView> listarItens() {
        return estado.itens.values().stream()
                .map(this::paraView)
                .sorted(Comparator.comparing(ItemView::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<ItemView> buscarPorTitulo(String trecho) {
        exigirTextoPreenchido(trecho, "trecho");
        String trechoNormalizado = trecho.toLowerCase(Locale.ROOT);
        return estado.itens.values().stream()
                .filter(item -> item.titulo().toLowerCase(Locale.ROOT).contains(trechoNormalizado))
                .map(this::paraView)
                .sorted(Comparator.comparing(ItemView::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<ItemView> buscarPorCategoria(String categoria) {
        exigirTextoPreenchido(categoria, "categoria");
        return estado.itens.values().stream()
                .filter(item -> categoria.equalsIgnoreCase(item.categoria()))
                .map(this::paraView)
                .sorted(Comparator.comparing(ItemView::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public void adicionarExemplar(String codigoDoItem, String tombo)
            throws RecursoNaoEncontradoException, RecursoDuplicadoException {
        exigirTextoPreenchido(codigoDoItem, "codigo");
        exigirTextoPreenchido(tombo, "tombo");
        Item item = localizarItem(codigoDoItem);
        if (estado.exemplares.containsKey(tombo)) {
            throw new RecursoDuplicadoException("Já existe exemplar com o tombo " + tombo);
        }
        Exemplar exemplar = new Exemplar(tombo, item.codigo());
        item.adicionarExemplar(exemplar);
        estado.exemplares.put(tombo, exemplar);
    }

    @Override
    public List<ExemplarView> listarExemplares(String codigoDoItem)
            throws RecursoNaoEncontradoException {
        Item item = localizarItem(codigoDoItem);
        return item.exemplares().stream()
                .map(exemplar -> new ExemplarView(
                        exemplar.tombo(), exemplar.codigoDoItem(), exemplar.status()))
                .sorted(Comparator.comparing(ExemplarView::tombo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public void baixarExemplar(String tombo)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        circulacao.baixar(tombo);
    }

    private Item localizarItem(String codigo) throws RecursoNaoEncontradoException {
        return estado.item(codigo);
    }

    private ItemView paraView(Item item) { return item.view(); }

    private static void exigirTextoPreenchido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("O campo " + campo + " é obrigatório.");
        }
    }
}
