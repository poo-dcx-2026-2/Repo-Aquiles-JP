package br.ufpb.dcx.poo.biblioteca.inicial;

/** Usuário com matrícula imutável e estado preparado para as próximas entregas. */
final class Usuario {

    private final String matricula;
    private final String nome;
    private boolean ativo = true;

    Usuario(String matricula, String nome) {
        this.matricula = matricula;
        this.nome = nome;
    }

    String matricula() { return matricula; }
    String nome() { return nome; }
    boolean ativo() { return ativo; }

    void desativar() {
        ativo = false;
    }

    void reativar() {
        ativo = true;
    }
}
