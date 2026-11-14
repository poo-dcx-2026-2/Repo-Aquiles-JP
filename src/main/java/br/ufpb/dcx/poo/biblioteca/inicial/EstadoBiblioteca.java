package br.ufpb.dcx.poo.biblioteca.inicial;
import java.util.*;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
import br.ufpb.dcx.poo.biblioteca.persistencia.DadosBiblioteca;
import br.ufpb.dcx.poo.biblioteca.persistencia.DadosBiblioteca.ReservaDados;
/** Raiz do estado de uma biblioteca, sem E/S e sem campos estáticos mutáveis. */
final class EstadoBiblioteca {
    Map<String, Item> itens = new LinkedHashMap<>();
    Map<String, Exemplar> exemplares = new LinkedHashMap<>();
    Map<String, Usuario> usuarios = new LinkedHashMap<>();
    Map<String, Emprestimo> emprestimos = new LinkedHashMap<>();
    Map<String, Reserva> reservas = new LinkedHashMap<>();
    Item item(String codigo) throws RecursoNaoEncontradoException {
        Validacao.texto(codigo, "Código");
        Item item = itens.get(codigo);
        if (item == null) throw new RecursoNaoEncontradoException("Item não encontrado: " + codigo);
        return item;
    }
    Exemplar exemplar(String tombo) throws RecursoNaoEncontradoException {
        Validacao.texto(tombo, "Tombo");
        Exemplar exemplar = exemplares.get(tombo);
        if (exemplar == null) throw new RecursoNaoEncontradoException("Exemplar não encontrado: " + tombo);
        return exemplar;
    }
    Usuario usuario(String matricula) throws RecursoNaoEncontradoException {
        Validacao.texto(matricula, "Matrícula");
        Usuario usuario = usuarios.get(matricula);
        if (usuario == null) throw new RecursoNaoEncontradoException("Usuário não encontrado: " + matricula);
        return usuario;
    }
    Reserva reservaDo(Exemplar exemplar) {
        return reservas.values().stream().filter(r -> r.ativa() && r.separado() == exemplar).findFirst().orElse(null);
    }
    String novoId() {
        String id;
        do { id = UUID.randomUUID().toString(); } while (emprestimos.containsKey(id) || reservas.containsKey(id));
        return id;
    }
    void substituir(EstadoBiblioteca novo) {
        itens = novo.itens; exemplares = novo.exemplares; usuarios = novo.usuarios;
        emprestimos = novo.emprestimos; reservas = novo.reservas;
    }
    DadosBiblioteca snapshot() {
        return new DadosBiblioteca(itens.values().stream().map(Item::view).toList(),
            exemplares.values().stream().map(e -> new ExemplarView(e.tombo(), e.codigoDoItem(), e.status())).toList(),
            usuarios.values().stream().map(Usuario::view).toList(),
            emprestimos.values().stream().map(e -> e.view(false)).toList(),
            reservas.values().stream().map(r -> new ReservaDados(r.id(), r.item().codigo(), r.usuario().matricula(),
                r.data(), r.situacao().name(), r.separado() == null ? null : r.separado().tombo())).toList());
    }
    /** Reconstrói e valida tudo antes de permitir substituir o estado em uso. */
    static EstadoBiblioteca reconstruir(DadosBiblioteca dados) throws BibliotecaException {
        EstadoBiblioteca e = new EstadoBiblioteca();
        for (ItemView i : dados.itens()) {
            Validacao.texto(i.codigo(), "Código"); Validacao.texto(i.titulo(), "Título");
            exigir(!e.itens.containsKey(i.codigo()), "Item duplicado");
            e.itens.put(i.codigo(), new Item(i.codigo(), i.titulo(), i.autoria(), i.categoria(), i.ano()));
        }
        for (UsuarioView u : dados.usuarios()) {
            Validacao.texto(u.matricula(), "Matrícula"); Validacao.texto(u.nome(), "Nome");
            exigir(!e.usuarios.containsKey(u.matricula()), "Usuário duplicado");
            Usuario usuario = new Usuario(u.matricula(), u.nome());
            if (!u.ativo()) usuario.desativar();
            e.usuarios.put(u.matricula(), usuario);
        }
        Map<String, StatusExemplar> status = new HashMap<>();
        for (ExemplarView v : dados.exemplares()) {
            Validacao.texto(v.tombo(), "Tombo");
            Item item = e.item(v.codigoDoItem());
            exigir(!e.exemplares.containsKey(v.tombo()) && v.status() != null, "Exemplar inválido");
            Exemplar exemplar = new Exemplar(v.tombo(), item.codigo());
            e.exemplares.put(v.tombo(), exemplar); item.adicionarExemplar(exemplar);
            status.put(v.tombo(), v.status());
            if (v.status() == StatusExemplar.INDISPONIVEL) exemplar.baixar();
        }
        Set<String> ativos = new HashSet<>();
        for (EmprestimoView v : dados.emprestimos()) {
            Validacao.texto(v.id(), "Id"); Validacao.data(v.dataDoEmprestimo());
            exigir(!e.emprestimos.containsKey(v.id()), "Empréstimo duplicado");
            Exemplar exemplar = e.exemplar(v.tombo()); Usuario usuario = e.usuario(v.matricula());
            Emprestimo emp = new Emprestimo(v.id(), exemplar, usuario, v.dataDoEmprestimo());
            exigir(emp.prevista().equals(v.dataPrevistaDeDevolucao()), "Prazo inválido");
            if (v.dataDaDevolucao() != null) {
                exigir(v.status() == StatusEmprestimo.DEVOLVIDO, "Status inválido");
                emp.devolver(v.dataDaDevolucao());
            } else {
                exigir(v.status() == StatusEmprestimo.ATIVO && usuario.ativo()
                    && status.get(v.tombo()) == StatusExemplar.EMPRESTADO && ativos.add(v.tombo()), "Empréstimo ativo inválido");
                usuario.exigirLimite(); exemplar.emprestar();
            }
            usuario.registrar(emp); e.emprestimos.put(emp.id(), emp);
        }
        Set<String> separados = new HashSet<>();
        for (ReservaDados v : dados.reservas()) {
            Validacao.texto(v.id(), "Id"); Validacao.data(v.data());
            exigir(!e.reservas.containsKey(v.id()) && !e.emprestimos.containsKey(v.id()), "Id duplicado");
            Item item = e.item(v.codigo()); Usuario usuario = e.usuario(v.matricula());
            Reserva reserva = new Reserva(v.id(), item, usuario, v.data());
            Reserva.Situacao situacao = Reserva.Situacao.valueOf(v.situacao());
            if (situacao == Reserva.Situacao.ATIVA) {
                exigir(usuario.ativo() && !item.temReserva(usuario), "Reserva ativa inválida");
                if (v.tombo() != null) {
                    Exemplar exemplar = e.exemplar(v.tombo());
                    exigir(exemplar.codigoDoItem().equals(item.codigo())
                        && status.get(v.tombo()) == StatusExemplar.RESERVADO && separados.add(v.tombo()), "Exemplar reservado inválido");
                    reserva.separar(exemplar);
                }
            } else {
                exigir(v.tombo() == null, "Reserva encerrada com exemplar");
                reserva.encerrar(situacao);
            }
            item.adicionarReserva(reserva); e.reservas.put(reserva.id(), reserva);
        }
        for (Exemplar exemplar : e.exemplares.values())
            exigir(exemplar.status() == status.get(exemplar.tombo()), "Estado do exemplar inconsistente");
        return e;
    }
    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new DadosInvalidosException(mensagem);
    }
}
