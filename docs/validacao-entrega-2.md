# Validação da Entrega 2

Data: 14/11/2026. Base: `a490f10` da branch `main`.
Fonte dos requisitos: PDF `entrega-2.pdf` fornecido pelo usuário, especialmente §3.
O guia mencionado no enunciado não está neste repositório nem foi fornecido como anexo;
não se afirma que ele foi consultado. O contrato local foi lido e preservado.

## Evidências executadas

1. A base da Entrega 1 foi aberta em uma worktree separada. Seus cinco conjuntos
   originais (`AcervoTest`, `AcervoTddTest`, `UsuarioTest`, `FabricaTest` e
   `JogoDeTabuleiroTest`) passaram: **26 testes, zero falhas/erros/pulados**.
2. `Entrega2Test` foi executado contra essa mesma base. Resultado: **39 testes,
   8 falhas e 4 erros**, incluindo empréstimos ainda lançando
   `UnsupportedOperationException`. Os novos testes detectam as pendências reais;
   a suíte anterior continua verde.
3. A implementação foi validada com `mvn -B verify`: inicialmente **56 testes**
   passando. Casos adicionais de múltiplos exemplares reservados, limite sem consumo
   da reserva, desativação com transferência, atraso sem bloqueio, visões imutáveis
   e limites da extensão elevaram a suíte para **64 testes**.
4. Resultado final local: **64 testes, zero falhas, zero erros, zero pulados;
   BUILD SUCCESS**. `git diff --check` passou e o diff de `contrato/` é vazio.
5. `docs/modelo.svg` foi gerado de `docs/modelo.puml` com PlantUML 1.2025.10.
   O SVG é versionado, incluindo a associação por código entre jogo e item.

O ambiente tinha apenas o runtime Java, sem um JDK completo e sem Maven.
Foram baixados Temurin JDK 21 e Maven 3.9.11 em uma pasta de trabalho externa ao
repositório. Proxy, truststore e cache Maven locais foram configurados apenas para
o ambiente de execução; não foram adicionados ao projeto. O comando efetivo usou
`mvn -B verify` com esses caminhos locais e opções de proxy/cache/truststore.
A configuração do build e o workflow da disciplina foram mantidos.

## Cobertura relevante

- Todas as famílias de exceções da §3 em `Entrega2Test`, incluindo entradas nulas,
  vazias e em branco, recursos ausentes, inatividade, limite e indisponibilidade.
- Datas fixas, prazo de 14 dias, limite de três empréstimos, ordenação do histórico
  por data e desempate por registro, ordenação dos atrasados e fronteira do prazo.
- Reserva atendida, cancelada, duplicada, sem exemplar, com exemplar separado,
  cancelamento que transfere a separação, baixa que faz voltar a esperar e recusa
  de empréstimo por outro usuário. Novo exemplar continua disponível.
- Snapshot completo, histórico devolvido, situações de usuário e exemplar, reservas
  ativas/atendidas/canceladas, IDs após carga, serviços retidos antes da carga e
  independência entre instâncias.
- `@TempDir` e restauração da propriedade `biblioteca.dados` após cada teste.
  Ausência de arquivo/diretório esvazia o estado. Corrupções de cabeçalho, linha,
  número, referência a tombo/item/usuário, data, prazo, status, booleano, duplicação,
  truncamento e reserva deixam as visões intactas. Erros de E/S preservam a causa.
- A extensão declarada na Entrega 1 permanece coberta por testes próprios.

## O que depende dos integrantes

Revisão substantiva do PR por outro integrante, contribuições pessoais, assinaturas
na declaração de IA, atividades/checkpoints e demonstração no encontro não são
substituídos por esta validação. Não foram criados comentários ou aprovações em nome
de colegas. A aprovação da equipe e o merge na `main` ficam para o processo de revisão.
A execução remota de CI deve ser conferida no PR e no commit que for integrado.
