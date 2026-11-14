package br.ufpb.dcx.poo.biblioteca.inicial;

import java.time.LocalDate;
import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoService;
import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoView;
import br.ufpb.dcx.poo.biblioteca.contrato.ReservaView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.ExemplarIndisponivelException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.LimiteDeEmprestimosExcedidoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.UsuarioInativoException;

/** Adaptador do contrato para o domínio de circulação. */
public class EmprestimosEmMemoria implements EmprestimoService {
    private final Circulacao circulacao;
    EmprestimosEmMemoria(EstadoBiblioteca estado) { circulacao = new Circulacao(estado); }

    @Override
    public String emprestar(String tombo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, ExemplarIndisponivelException,
                   UsuarioInativoException, LimiteDeEmprestimosExcedidoException {
        return circulacao.emprestar(tombo, matricula, data);
    }

    @Override
    public void devolver(String tombo, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        circulacao.devolver(tombo, data);
    }

    @Override
    public List<EmprestimoView> listarEmprestimosDoUsuario(String matricula)
            throws RecursoNaoEncontradoException {
        return circulacao.historico(matricula);
    }

    @Override
    public List<EmprestimoView> listarAtrasados(LocalDate data) {
        return circulacao.atrasados(data);
    }

    @Override
    public String reservar(String codigoDoItem, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        return circulacao.reservar(codigoDoItem, matricula, data);
    }

    @Override
    public List<ReservaView> listarReservas(String codigoDoItem)
            throws RecursoNaoEncontradoException {
        return circulacao.reservas(codigoDoItem);
    }

    @Override
    public void cancelarReserva(String idDaReserva) throws RecursoNaoEncontradoException {
        circulacao.cancelar(idDaReserva);
    }
}
