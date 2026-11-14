# Acervo de jogos de tabuleiro — Aquiles e João

Projeto de Programação Orientada a Objetos, DCX/CCAE/UFPB, 2026.2.
A Entrega 2 mantém o acervo da Entrega 1 e implementa empréstimos, devoluções,
reservas, ativação de usuários e persistência em arquivo.

## Executar e validar

Requisitos: **JDK 21 completo** e **Maven**.

```sh
mvn -B verify
mvn -B -Dtest=Entrega2Test test
mvn -B -Dtest=PersistenciaTest test
mvn -B -Dtest=JogoDeTabuleiroTest test
```

A suíte tem 64 casos executados, sem testes desabilitados. O workflow
[build](.github/workflows/build.yml) executa `mvn -B verify` em pushes e PRs.
O registro das verificações está em [docs/validacao-entrega-2.md](docs/validacao-entrega-2.md).

## Mapa do projeto

| Local | Responsabilidade |
|---|---|
| `contrato/` | API congelada: serviços, visões, estados e exceções; preservada. |
| `Fabrica.java` | Cria uma biblioteca nova, vazia e independente; não carrega arquivos. |
| `inicial/BibliotecaInicial.java` | Facade e única entrada da aplicação; fornece os serviços do contrato. |
| `inicial/AcervoEmMemoria.java`, `UsuariosEmMemoria.java`, `EmprestimosEmMemoria.java` | Adaptam o contrato ao domínio compartilhado pela biblioteca. |
| `inicial/EstadoBiblioteca.java` | Indexa entidades, cria snapshots e valida a reconstrução do estado. |
| `inicial/Item.java`, `Exemplar.java`, `Usuario.java`, `Emprestimo.java`, `Reserva.java`, `Circulacao.java` | Modelo e regras de negócio; sem acesso a arquivos. O nome histórico do pacote foi mantido. |
| `persistencia/` | Formato textual, leitura, escrita e conversão de erros em `PersistenciaException`. |
| `inicial/JogoDeTabuleiro.java` | Extensão autoral independente do contrato. |
| `src/test/java/` | Regressões da Entrega 1, regras da Entrega 2 e extensão. |
| `docs/` | [Decisões de design](docs/decisoes.md), [UML textual](docs/modelo.puml) e [imagem SVG](docs/modelo.svg). |

Relatórios, importação em lote, interface gráfica e concorrência ficam para a Entrega 3.

## Uso pela Facade

```java
Biblioteca biblioteca = Fabrica.novaBiblioteca();
biblioteca.acervo().cadastrarItem("J1", "Catan", "Klaus Teuber", "Estratégia", 1995);
biblioteca.acervo().adicionarExemplar("J1", "T1");
biblioteca.usuarios().cadastrarUsuario("U1", "Ana");
String id = biblioteca.emprestimos().emprestar("T1", "U1", LocalDate.of(2026, 10, 1));
biblioteca.salvar();
```

As consultas retornam visões imutáveis. A futura interface gráfica deve utilizar
somente `Biblioteca` e os serviços obtidos dela, sem acessar entidades ou persistência.
As datas são sempre fornecidas pelo chamador. O prazo é 14 dias e o limite é 3
empréstimos ativos; atraso não impede um novo empréstimo no contrato comum.

A fila de reservas segue a ordem de registro, mesmo se as datas informadas forem
iguais ou retroativas. Devolução e cancelamento separam exemplares para a próxima
reserva ainda sem exemplar. Um exemplar novo entra disponível. Ao desativar um usuário
sem empréstimos ativos, suas reservas são canceladas e seus exemplares separados
seguem para a próxima pessoa da fila; essa decisão está documentada.

## Onde ficam os dados

`salvar()` cria `dados/biblioteca-v2.txt` no diretório de execução. A propriedade
`biblioteca.dados` substitui **o diretório**, por exemplo `-Dbiblioteca.dados=/tmp/acervo`.
O caminho é consultado a cada salvar/carregar. O arquivo de exemplo
`dados/itens-exemplo.csv` nunca é alterado; arquivos gerados são ignorados pelo Git.

O arquivo UTF-8 tem cabeçalho `BIBLIOTECA-2`, registros tabulados e contagem final.
Textos usam Base64 para preservar caracteres especiais, tabs, quebras de linha e
valores nulos. São gravados itens, exemplares, usuários, histórico de empréstimos e
reservas, incluindo as atendidas e canceladas. Contagens das visões são recalculadas.
IDs são UUIDs e são conferidos contra os IDs já existentes antes do uso.

A escrita usa um temporário e troca o arquivo ao final (movimento atômico quando
suportado). `carregar()` reconstrói e valida um estado separado antes de substituir
o atual. Arquivo ausente esvazia a biblioteca; arquivo malformado ou com referências
inconsistentes lança `PersistenciaException`, preservando a memória e a causa do erro.
Quem deseja recuperar dados precisa chamar `carregar()` explicitamente.

## Extensão autoral

A regra declarada na Entrega 1 foi mantida: **o mínimo de jogadores deve ser pelo
menos 1 e o máximo não pode ser menor que o mínimo**. `JogoDeTabuleiro` mantém
metadados associados ao código do item, sem herança de `Item` e sem alterar o contrato.

```java
JogoDeTabuleiro jogo = new JogoDeTabuleiro("J1", 2, 4); // válido
new JogoDeTabuleiro("J1", 4, 2); // DadosInvalidosException
```

Código: `src/main/java/br/ufpb/dcx/poo/biblioteca/inicial/JogoDeTabuleiro.java`.
Os testes próprios cobrem jogo solo, mínimo zero/negativo, faixa invertida e código
obrigatório. Demonstração no encontro: executar `JogoDeTabuleiroTest` e, depois,
`Entrega2Test#cicloDeReservaRespeitaFilaEAtendeSomenteReservante`.
A extensão é um objeto adicional; seu cadastro não integra o estado do contrato.
A exceção usada já pertence ao contrato congelado, não é uma exceção nova.

## Processo e autoria

Mudanças da Entrega 2 estão em branch própria e devem ser integradas por PR com
revisão substantiva de outro integrante. Um teste executado por IA não substitui
revisão humana, contribuição individual ou demonstração. Consulte a
[declaração de uso de IA](DECLARACAO-DE-USO-DE-IA.md); as assinaturas dependem dos integrantes.

O defeito da Entrega 1 que comparava códigos com `==` continua protegido pelo teste
com instâncias distintas de `String`; a busca usa igualdade de conteúdo por `Map`.

| Nome | Matrícula | GitHub |
|---|---|---|
| Aquiles Francisco da Silva | 20250104421 | AquilesFrancisco |
| João Pedro de Lira Tavares | 20250104501 | jpliratavares |
