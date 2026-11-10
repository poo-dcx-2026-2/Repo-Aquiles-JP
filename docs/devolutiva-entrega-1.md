# Devolutiva da Entrega 1 — Aquiles e João

## Nota: 9,0 de 10 (90 de 100 pontos)

Avaliei o último commit da branch `main` até 12/10/2026 às 23h59 (o prazo original, 05/10, mais uma semana de extensão), pela leitura do código, dos testes, do histórico de commits, dos pull requests e da documentação. 

## Pontos por critério

| Critério | O que é avaliado | Pontos | Nível |
|---|---|---|---|
| 4.1 | Correção funcional e aderência ao contrato | 20 de 20 | Excelente |
| 4.2 | Modelagem OO, responsabilidades, coesão e acoplamento | 23 de 25 | Excelente |
| 4.3 | Testes e evidências de depuração | 14 de 15 | Excelente |
| 4.4 | Robustez, exceções e casos-limite | 10 de 10 | Excelente |
| 4.5 | Evolução, refatoração e qualidade do código | 9 de 10 | Excelente |
| 4.6 | Git, build, pipeline e documentação | 7 de 10 | Adequado |
| 4.7 | Processo, checkpoints, explicação e autoria | 7 de 10 | Parcial |
| | **Total** | **90 de 100** | |

## Justificativa (onde tirei pontos)

- Não há revisão formal nos PRs
- Imagem UML não foi versionada, embora fosse requisito explícito.
- Parte importante da implementação aparece associada ao agente de IA. A IA foi declarada, porém faltam assinaturas.

## Checklist de correções

- [ ] **Registrar a revisão de colega no pull request.** Pull request (PR) é o pedido para juntar uma *branch* (linha de trabalho separada) na `main`. A revisão só conta como evidência se aparecer no GitHub: na aba *Files changed* do PR, o colega que **não** escreveu o código clica em *Review changes*, escreve pelo menos um comentário sobre o que leu e escolhe *Approve* ou *Request changes*. Mensagem de commit dizendo "revisado" não substitui isso.
- [ ] **Equilibrar os commits de código entre os integrantes.** Parte importante da implementação aparece associada ao agente de IA, e a contribuição dos dois ficou desigual. Cada integrante precisa ter commits de código e de testes com o próprio usuário do GitHub e conseguir explicar a parte do colega.
- [ ] **Versionar a imagem do UML.** Além do `docs/modelo.puml` (o texto do diagrama), o enunciado pede a imagem gerada a partir dele (PNG ou SVG) dentro de `docs/`, adicionada ao repositório com `git add` e `commit`. Gerem a imagem pelo plugin PlantUML do IntelliJ ou pelo site do PlantUML e confiram que ela aparece no GitHub.
- [ ] **Assinar a declaração de uso de IA.** O arquivo já está preenchido; faltam os nomes de todos os integrantes no campo de assinatura. Sem a assinatura, a declaração não vale como compromisso da equipe.
- [ ] **Corrigir a relação de `JogoDeTabuleiro` no UML.** No código, `JogoDeTabuleiro` se liga ao item por um código (uma associação), mas o diagrama desenha uma especialização (seta de herança, com triângulo vazado). Troquem a seta pela de associação.
