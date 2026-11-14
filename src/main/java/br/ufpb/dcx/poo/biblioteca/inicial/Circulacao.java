package br.ufpb.dcx.poo.biblioteca.inicial;
import java.time.LocalDate;
import java.util.*;
import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
/** Coordena entidades do domínio; os adaptadores de serviço só encaminham pedidos. */
final class Circulacao {
    private final EstadoBiblioteca estado;
    Circulacao(EstadoBiblioteca estado) { this.estado = estado; }
    String emprestar(String tombo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, ExemplarIndisponivelException,
                   UsuarioInativoException, LimiteDeEmprestimosExcedidoException {
        Validacao.texto(tombo, "Tombo"); Validacao.texto(matricula, "Matrícula"); Validacao.data(data);
        Exemplar exemplar = estado.exemplar(tombo); Usuario usuario = estado.usuario(matricula);
        usuario.exigirAtivo();
        Reserva reserva = estado.reservaDo(exemplar);
        exemplar.exigirEmprestavel(usuario, reserva); usuario.exigirLimite();
        Emprestimo emprestimo = new Emprestimo(estado.novoId(), exemplar, usuario, data);
        // Só depois de todas as validações começam as mudanças.
        exemplar.emprestar(); usuario.registrar(emprestimo);
        estado.emprestimos.put(emprestimo.id(), emprestimo);
        if (reserva != null) reserva.encerrar(Reserva.Situacao.ATENDIDA);
        return emprestimo.id();
    }
    void devolver(String tombo, LocalDate data) throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        Validacao.texto(tombo, "Tombo"); Validacao.data(data);
        Exemplar exemplar = estado.exemplar(tombo);
        Emprestimo emprestimo = estado.emprestimos.values().stream()
                .filter(e -> e.exemplar() == exemplar && e.ativo()).findFirst().orElse(null);
        if (emprestimo == null) throw new OperacaoNaoPermitidaException("Exemplar sem empréstimo ativo.");
        emprestimo.devolver(data);
        estado.item(exemplar.codigoDoItem()).separarProxima(exemplar);
    }
    String reservar(String codigo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        Validacao.texto(codigo, "Código"); Validacao.texto(matricula, "Matrícula"); Validacao.data(data);
        Item item = estado.item(codigo); Usuario usuario = estado.usuario(matricula);
        usuario.exigirAtivo();
        if (item.temDisponivel() || item.temReserva(usuario))
            throw new OperacaoNaoPermitidaException("Item disponível ou reserva já existente.");
        Reserva reserva = new Reserva(estado.novoId(), item, usuario, data);
        item.adicionarReserva(reserva); estado.reservas.put(reserva.id(), reserva);
        return reserva.id();
    }
    void cancelar(String id) throws RecursoNaoEncontradoException {
        Validacao.texto(id, "Id da reserva");
        Reserva reserva = estado.reservas.get(id);
        if (reserva == null || !reserva.ativa()) throw new RecursoNaoEncontradoException("Reserva ativa não encontrada.");
        cancelar(reserva);
    }
    private void cancelar(Reserva reserva) {
        Exemplar separado = reserva.separado();
        reserva.encerrar(Reserva.Situacao.CANCELADA);
        if (separado != null) reserva.item().separarProxima(separado);
    }
    void desativar(String matricula) throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        Usuario usuario = estado.usuario(matricula);
        usuario.desativar();
        for (Reserva reserva : estado.reservas.values())
            if (reserva.ativa() && reserva.usuario() == usuario) cancelar(reserva);
    }
    void baixar(String tombo) throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        Exemplar exemplar = estado.exemplar(tombo);
        Reserva reserva = estado.reservaDo(exemplar);
        exemplar.baixar();
        if (reserva != null) reserva.voltarAEsperar();
    }
    List<EmprestimoView> historico(String matricula) throws RecursoNaoEncontradoException {
        List<Emprestimo> lista = new ArrayList<>(estado.usuario(matricula).emprestimos());
        Collections.reverse(lista); // desempate: registrado por último primeiro
        lista.sort(Comparator.comparing(Emprestimo::data).reversed());
        return lista.stream().map(e -> e.view(false)).toList();
    }
    List<EmprestimoView> atrasados(LocalDate data) {
        Validacao.data(data);
        return estado.emprestimos.values().stream().filter(e -> e.atrasado(data))
            .sorted(Comparator.comparing(Emprestimo::prevista)).map(e -> e.view(true)).toList();
    }
    List<ReservaView> reservas(String codigo) throws RecursoNaoEncontradoException {
        List<ReservaView> lista = new ArrayList<>();
        for (Reserva reserva : estado.item(codigo).reservasAtivas()) lista.add(reserva.view(lista.size() + 1));
        return List.copyOf(lista);
    }
}
