package br.ufpb.dcx.poo.biblioteca.inicial;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
import br.ufpb.dcx.poo.biblioteca.persistencia.ArquivoBiblioteca;

/** Facade da aplicação: entrada única, serviços estáveis e estado independente. */
public class BibliotecaInicial implements Biblioteca {
    private final EstadoBiblioteca estado = new EstadoBiblioteca();
    private final AcervoEmMemoria acervo = new AcervoEmMemoria(estado);
    private final UsuariosEmMemoria usuarios = new UsuariosEmMemoria(estado);
    private final EmprestimosEmMemoria emprestimos = new EmprestimosEmMemoria(estado);
    private final RelatoriosEmMemoria relatorios = new RelatoriosEmMemoria(estado);
    private final ArquivoBiblioteca arquivo = new ArquivoBiblioteca();
    @Override public AcervoService acervo() { return acervo; }
    @Override public UsuarioService usuarios() { return usuarios; }
    @Override public EmprestimoService emprestimos() { return emprestimos; }
    @Override public RelatorioService relatorios() { return relatorios; }
    @Override public void salvar() throws PersistenciaException { arquivo.salvar(estado.snapshot()); }
    @Override public void carregar() throws PersistenciaException {
        var dados = arquivo.carregar();
        final EstadoBiblioteca novo;
        try {
            novo = EstadoBiblioteca.reconstruir(dados);
        } catch (BibliotecaException | IllegalArgumentException | java.time.DateTimeException ex) {
            throw ArquivoBiblioteca.falha("Estado gravado inconsistente.", ex);
        }
        estado.substituir(novo);
    }
}
