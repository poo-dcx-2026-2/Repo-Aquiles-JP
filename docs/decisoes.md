# Decisões de design — Entrega 2

**Composição em vez de herança.** O acervo combina itens, exemplares e metadados
específicos de jogos. Mantivemos a composição `Item`–`Exemplar` e a associação por
código entre `JogoDeTabuleiro` e `Item`; rejeitamos transformar jogo em subclasse
porque ele não substitui o comportamento de um item e o contrato não cadastra
subtipos. A consequência é manter a regra autoral isolada e corrigir o UML para
expressar associação, sem hierarquia artificial.

**GRASP Information Expert e coesão de Item.** O adaptador de acervo calculava a
quantidade de exemplares disponíveis percorrendo dados de `Item`. Transferimos a
criação de `ItemView`, as contagens e a procura da próxima reserva sem exemplar para
`Item`, que possui essas coleções. Rejeitamos repetir os cálculos em acervo e
empréstimos; a consequência é maior coesão e menor acoplamento dos serviços às
coleções internas. Os testes de acervo e as verificações das visões protegem essa
refatoração.

**GRASP Information Expert em Usuario e Emprestimo.** O serviço de usuários devolvia
sempre zero empréstimos e a desativação não consultava o histórico. O usuário agora
conhece seus empréstimos, calcula os ativos e verifica limite/ativação; `Emprestimo`
calcula o prazo e valida a devolução. Rejeitamos contadores paralelos nos serviços,
que poderiam divergir do histórico. A consequência é uma única fonte para a situação
do usuário, com prazo testável sem relógio e com regras junto aos dados.

**GRASP Controller, Creator e baixo acoplamento.** Empréstimos exigem coordenar
usuário, exemplar e fila. `Circulacao` coordena essa operação de domínio, cria
`Emprestimo`/`Reserva` a partir das entidades e delega as transições aos especialistas.
Os adaptadores `*EmMemoria` encaminham pedidos e consultas. Rejeitamos serviços que
modificam diretamente campos de entidades ou chamam outros serviços públicos. A
consequência é coordenação explícita, sem duplicação das regras em cada adaptador.
`EstadoBiblioteca` indexa os objetos e mantém uma identidade de estado por biblioteca.

**Facade e estado independente.** `BibliotecaInicial` implementa a interface congelada
`Biblioteca` e fornece os serviços que compartilham uma raiz de estado privada.
Rejeitamos acesso da interface gráfica a mapas, entidades ou `ArquivoBiblioteca`.
A consequência é uma porta única para a Entrega 3 e serviços estáveis mesmo após
`carregar()`. `Fabrica.novaBiblioteca()` cria uma instância vazia a cada chamada,
sem leitura automática e sem estado mutável estático.

**Reserva com identidade e estado.** A fila precisa sobreviver a devoluções,
cancelamentos e reinícios. `Reserva` guarda ID, usuário, item, data, situação e o
exemplar separado; `Item` preserva a ordem de inserção. Rejeitamos uma fila de
matrículas sem identidade e a ordenação apenas por data, pois datas fornecidas podem
ser iguais ou retroativas. A consequência é cancelamento por ID, posição recalculada
e histórico das reservas atendidas/canceladas. Só devolução e cancelamento distribuem
exemplares; novos exemplares continuam disponíveis, como exige o enunciado.

**Reservas de usuário desativado.** Um usuário sem empréstimos pode estar na fila ou
ter exemplar separado. Optamos por cancelar todas as suas reservas ao desativá-lo,
transferindo os exemplares à próxima reserva sem exemplar. Rejeitamos manter uma
reserva impossível de atender enquanto o usuário está inativo, que bloquearia o
acervo. A consequência é que reativação não restaura reservas antigas; o usuário
precisa reservar novamente. A recusa de desativação por empréstimos ativos ocorre
antes de qualquer cancelamento.

**Persistência separada, GRASP Pure Fabrication e Protected Variations.** O domínio
não deve conhecer arquivos. `ArquivoBiblioteca` concentra a E/S e o formato textual;
`DadosBiblioteca` transporta um snapshot imutável entre essa classe e o domínio.
Rejeitamos colocar leitores/escritores nas entidades ou serializar o grafo Java
diretamente, que amarraria os dados à implementação. A consequência é um formato
versionado e legível por linhas, com Base64 nos campos textuais e recursos fechados
por try-with-resources. Não criamos interface de repositório sem segunda implementação:
o polimorfismo existente é a substituição real dos serviços pelos tipos do contrato.

**Carga transacional em memória e escrita por substituição.** Um arquivo pode estar
truncado, conter número inválido, IDs duplicados ou referências inexistentes. A carga
constrói e valida uma raiz nova, trocando os mapas apenas depois do sucesso; rejeitamos
limpar o estado antes de ler. A escrita usa temporário no mesmo diretório e movimento
atômico quando suportado, com substituição normal como alternativa da plataforma.
A consequência é memória preservada em erro de carga e arquivo antigo preservado se a
escrita do temporário falhar. Toda IOException é encapsulada com causa via `initCause`,
pois `PersistenciaException` congelada só possui construtor com mensagem.

**Regressões e limites do processo.** Os 26 testes anteriores foram executados contra
a base da Entrega 1 e continuam passando após a redistribuição das responsabilidades.
Os testes da circulação detectaram as operações pendentes na base e passaram depois
da implementação. Rejeitamos apresentar revisão automática como revisão de colega ou
atribuir commits aos integrantes artificialmente. A consequência é um histórico
honesto de testes, implementação e documentação, com revisão humana e assinaturas
explicitamente pendentes.

## Entrega 3

**Contagem por categoria.** A consulta precisa somar o histórico e agrupar grafias que
diferem apenas em caixa. Escolhemos um Stream que localiza a categoria do item,
normaliza com `Locale.ROOT` e soma ocorrências com `toMap`. Rejeitamos um laço
com mapa mutável e `groupingBy`, que rejeita a categoria nula aceita no cadastro.
A consequência é uma contagem que preserva os dados legados sob a chave nula e
devolve um mapa de leitura.

**Taxa de ocupação.** A taxa considera emprestados no numerador e os exemplares
em circulação no denominador. Escolhemos filtrar as baixas e calcular a média de
1 para emprestado e 0 para os demais. Rejeitamos contar em dois percursos.
A consequência é uma única passagem, incluindo reservados no denominador e
retornando 0.0 quando não há exemplares em circulação.

**Ranking de itens.** A consulta precisa ordenar pelo total histórico, incluindo
devoluções. Escolhemos agrupar e contar por código, depois ordenar somente itens com
histórico, desempatar por título e construir as visões dos n primeiros. Rejeitamos
recontar o histórico dentro do comparador. A consequência é uma contagem única por
item e visões que refletem o estado atual.

**Usuários em atraso.** A consulta deve devolver usuários únicos e o prazo já
pertence ao empréstimo. Escolhemos filtrar com a regra de `Emprestimo`, deduplicar
as referências aos usuários e ordenar por matrícula antes de projetar as visões.
Rejeitamos repetir o cálculo do prazo e manter uma lista mutável dentro do Stream.
A consequência é a mesma fronteira da circulação, com um resultado por matrícula.

**Devolutiva anterior.** A Entrega 1 apontou a imagem ausente e a relação de
especialização incorreta no diagrama. A associação por código com `Item` e a imagem
versionada foram corrigidas na Entrega 2 e atualizadas com a classe de relatórios.
Mantivemos a regressão de igualdade de códigos por conteúdo e o histórico da extensão
do Aquiles. A contribuição e a revisão de cada integrante ficam registradas nos PRs.
