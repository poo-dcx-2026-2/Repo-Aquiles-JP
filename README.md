# Sistema de Gestão de Biblioteca/Acervo

Projeto incremental da disciplina de **Programação Orientada a Objetos** — DCX/CCAE/UFPB.

Este repositório é o ponto de partida do seu projeto. Ele compila, executa e tem testes passando, mas está **deliberadamente incompleto** e contém decisões de design questionáveis. Encontrá-las, justificá-las e corrigi-las faz parte da disciplina.

---

## Começando

Você precisa de **JDK 21** e **Maven**. No IntelliJ IDEA, abra a pasta do projeto e ele reconhece o `pom.xml` sozinho.

```bash
mvn -B verify      # compila e roda os testes
mvn -B test        # só os testes
```

Se tudo estiver certo, você verá `Tests run: 19, Failures: 0, Errors: 0, Skipped: 5`.

Os 5 pulados são os testes marcados com `@Disabled`: eles cobrem o que você ainda vai implementar. Remova a anotação de cada um quando implementar o método correspondente.

---

## Mapa do projeto

```
src/main/java/br/ufpb/dcx/poo/biblioteca/
├── contrato/          ← CONGELADO. Não edite nada aqui.
│   ├── Biblioteca.java              ponto único de acesso aos serviços
│   ├── AcervoService.java           itens e exemplares
│   ├── UsuarioService.java          usuários
│   ├── EmprestimoService.java       empréstimos, devoluções e reservas
│   ├── RelatorioService.java        consultas e importação em lote
│   ├── *View.java                   o que as consultas devolvem
│   ├── Status*.java                 estados de exemplar e de empréstimo
│   └── excecoes/                    a hierarquia de erros do sistema
│
├── Fabrica.java       ← nome e assinatura congelados; o corpo é seu
│
└── inicial/           ← ponto de partida; altere, mova, renomeie ou apague
    ├── Item.java
    ├── Exemplar.java
    ├── AcervoEmMemoria.java
    ├── UsuariosEmMemoria.java
    ├── EmprestimosNaoImplementados.java
    ├── RelatoriosNaoImplementados.java
    └── BibliotecaInicial.java

src/test/java/…       ← seus testes; comece pelos que já estão aqui
dados/                ← arquivos de exemplo
.github/workflows/    ← a integração contínua, já configurada
```

---

## As duas regras

**1. O pacote `contrato` é congelado.** Não renomeie, não altere assinaturas, não acrescente nem remova métodos. Os testes de correção são escritos contra esses tipos: se você mudar qualquer coisa ali, eles não compilam e a entrega não pode ser avaliada.

**2. `Fabrica.novaBiblioteca()` precisa continuar funcionando.** É por esse método que os testes obtêm o seu sistema. Você vai trocar o que ele devolve — não troque o nome, o pacote nem a assinatura. Cada chamada precisa devolver uma instância nova e independente.

Fora isso, **tudo é seu**. O pacote `inicial` não é modelo: é matéria-prima. Você pode reescrevê-lo inteiro.

---

## O que já funciona e o que falta

| Serviço | Situação |
|---|---|
| `AcervoService` | `cadastrarItem`, `buscarItem` e `listarItens` funcionam para os casos simples. O resto é seu. |
| `UsuarioService` | `cadastrarUsuario`, `buscarUsuario` e `listarUsuarios` funcionam. `desativar`/`reativar` são da Entrega 2. |
| `EmprestimoService` | Esqueleto. Entrega 2. |
| `RelatorioService` | Esqueleto. Entrega 3. |
| `Biblioteca.salvar/carregar` | Esqueleto. Entrega 2. |

Métodos ainda não implementados lançam `UnsupportedOperationException` com a indicação da entrega. Cada mensagem diz o que fazer.

## Como executar

Pré-requisitos: JDK 21 e Maven.

```bash
mvn -B verify       # compila, executa os testes e valida o projeto
mvn -B test         # executa somente a suíte JUnit 5
```

O projeto usa JUnit 5 e o workflow em `.github/workflows/build.yml` executa
`mvn -B verify` em cada push e Pull Request.

## Decisões de modelagem e coleções

O domínio protege as identidades de `Item`, `Exemplar` e `Usuario`: código, tombo e
matrícula são definidos na construção e não possuem *setters*. As operações de
alteração passam pelos serviços, onde as regras de negócio são verificadas.

| Estrutura | Uso | Justificativa |
|---|---|---|
| `Map<String, Item>` | Itens indexados pelo código. | Busca e verificação de duplicidade por código são diretas e usam igualdade de conteúdo de `String`. |
| `Map<String, Exemplar>` | Exemplares indexados pelo tombo. | O tombo é globalmente único, inclusive entre itens diferentes. |
| `Map<String, Usuario>` | Usuários indexados pela matrícula. | Evita listas paralelas e mantém nome e matrícula no mesmo objeto de domínio. |
| `List<Exemplar>` | Exemplares pertencentes a um item. | Preserva a composição item–exemplar; o acesso externo é uma cópia imutável. |

As consultas devolvem `ItemView`, `ExemplarView` e `UsuarioView`, não entidades
internas. Isso evita vazamento de estado do domínio.

## Relato do defeito oculto

O método de localização do código comparava textos com `==`. Em Java, `==` compara a
identidade do objeto, e não o conteúdo; portanto, dois objetos `String` com o mesmo
texto poderiam não ser reconhecidos como o mesmo código. O teste de regressão foi
registrado primeiro no commit `cceeda0` e cria duas instâncias distintas de `String`
com o valor `"L1"`. A correção substitui a comparação por uma busca no `Map`, cuja
chave usa igualdade de conteúdo. Esse fluxo mantém o defeito coberto pela suíte.

---

## Um aviso honesto

O código do pacote `inicial` **tem problemas**. Alguns são de design e você vai reconhecê-los conforme a disciplina avançar. Pelo menos um é um defeito de comportamento que os testes atuais **não** pegam: o sistema faz a coisa errada em uma situação que ninguém testou ainda.

Isso é proposital. Encontrar esse tipo de defeito — reproduzir, escrever o teste que falha, corrigir, ver o teste passar — é uma das competências avaliadas. Não confie em suíte verde como prova de correção.

---

## Extensão autoral

Sua equipe escolhe um acervo próprio: jogos, filmes, quadrinhos, instrumentos, obras locais, recursos de laboratório. A extensão vive **por fora** do contrato — novos tipos, novos serviços, novas regras — e precisa incluir ao menos **uma regra de negócio própria**, documentada abaixo, que os testes de correção não conhecem e que você demonstra na defesa.

### Nossa extensão

O acervo da equipe é de **jogos de tabuleiro**. A extensão `JogoDeTabuleiro` registra
a faixa de participantes de cada jogo. Sua regra de negócio autoral é: o mínimo de
jogadores deve ser pelo menos 1 e o máximo não pode ser menor que o mínimo. A classe
valida a regra no construtor e há teste automatizado para o caso válido e para a faixa
inválida. A extensão é adicional e não altera o pacote `contrato`.

---

## Uso de ferramentas de IA

O uso é permitido como apoio, desde que declarado em [`DECLARACAO-DE-USO-DE-IA.md`](DECLARACAO-DE-USO-DE-IA.md). Você continua responsável por explicar, testar e adaptar todo o código entregue — inclusive em uma alteração feita presencialmente, sem consulta, na defesa da Entrega 3.

---

## Equipe

| Nome | Matrícula | GitHub |
|---|---|---|
| | | |
| | | |
