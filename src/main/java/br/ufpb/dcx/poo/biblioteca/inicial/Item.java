package br.ufpb.dcx.poo.biblioteca.inicial;

import java.util.ArrayList;
import java.util.List;
import br.ufpb.dcx.poo.biblioteca.contrato.*;

/** Entidade do acervo cuja identidade é o código imutável. */
final class Item {

    private final String codigo;
    private final String titulo;
    private final String autoria;
    private final String categoria;
    private final int ano;
    private final List<Exemplar> exemplares = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();

    Item(String codigo, String titulo, String autoria, String categoria, int ano) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autoria = autoria;
        this.categoria = categoria;
        this.ano = ano;
    }

    String codigo() { return codigo; }
    String titulo() { return titulo; }
    String autoria() { return autoria; }
    String categoria() { return categoria; }
    int ano() { return ano; }

    void adicionarExemplar(Exemplar exemplar) {
        exemplares.add(exemplar);
    }

    ItemView view() {
        int disponiveis = 0;
        for (Exemplar e : exemplares) if (e.status() == StatusExemplar.DISPONIVEL) disponiveis++;
        return new ItemView(codigo, titulo, autoria, categoria, ano, exemplares.size(), disponiveis);
    }
    boolean temDisponivel() { return view().exemplaresDisponiveis() > 0; }
    void adicionarReserva(Reserva reserva) { reservas.add(reserva); }
    List<Reserva> reservasAtivas() {
        return reservas.stream().filter(Reserva::ativa).toList();
    }
    boolean temReserva(Usuario usuario) {
        return reservasAtivas().stream().anyMatch(r -> r.usuario() == usuario);
    }
    void separarProxima(Exemplar exemplar) {
        exemplar.disponibilizar();
        for (Reserva reserva : reservas) {
            if (reserva.ativa() && reserva.separado() == null) {
                reserva.separar(exemplar);
                return;
            }
        }
    }
    List<Exemplar> exemplares() {
        return List.copyOf(exemplares);
    }
}
