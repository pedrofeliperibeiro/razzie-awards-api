# Golden Raspberry Awards API

[![CI](https://github.com/pedrofeliperibeiro/razzie-awards-api/actions/workflows/ci.yml/badge.svg)](https://github.com/pedrofeliperibeiro/razzie-awards-api/actions/workflows/ci.yml)

API RESTful que expõe os produtores com o **menor** e o **maior** intervalo entre
dois prêmios consecutivos na categoria *Pior Filme* do Golden Raspberry Awards.

A lista de indicados e vencedores é lida de um arquivo CSV e carregada em um banco
de dados em memória na inicialização da aplicação — nenhuma instalação externa é
necessária.

## Tecnologias

- Java 21
- Spring Boot 3.4 (Spring Web, Spring Data JPA)
- H2 (embarcado, em memória)
- Apache Commons CSV
- Lombok (apenas para reduzir boilerplate na entidade)
- Spring Boot Actuator (observabilidade)
- springdoc-openapi / Swagger UI (documentação da API)
- JUnit 5 + Spring MockMvc (testes de integração)
- Maven (e Docker, opcional)

## Pré-requisitos

- JDK 21+
- **Maven não é necessário**: o projeto acompanha o Maven Wrapper (`./mvnw`), que
  baixa a versão correta do Maven automaticamente.

> O banco H2 é embarcado e em memória: não há nada para instalar, e os dados não
> sobrevivem a um restart (são reconstruídos a partir do CSV a cada inicialização).

## Como rodar

```bash
./mvnw spring-boot:run
```

Ou empacotar e rodar o jar:

```bash
./mvnw clean package
java -jar target/razzie-awards-api-1.0.0.jar
```

A aplicação sobe em `http://localhost:8080`.

Para habilitar as conveniências de desenvolvimento (console do H2, métricas e
detalhes de health), rode com o perfil `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Como rodar os testes de integração

```bash
./mvnw test
```

Conforme pedido no desafio, **somente testes de integração** foram implementados.
Eles sobem o contexto completo da aplicação, carregam o CSV no H2 e validam a saída
do endpoint:

- o CSV **oficial** (`Movielist.csv`), validando o resultado exato esperado;
- um **segundo conjunto de dados** (`src/test/resources/datasets/ties.csv`) com
  empates tanto no menor quanto no maior intervalo, provando que a API retorna
  todos os produtores empatados;
- um caso específico da **vírgula de Oxford** (`datasets/oxford.csv`) num filme
  vencedor, garantindo que `"A, B, and C"` gera os produtores certos no cálculo;
- um teste sobre **servidor HTTP real** (`RANDOM_PORT` + `TestRestTemplate`), que
  cobre o que o MockMvc não enxerga — como o `Content-Type` de um erro;
- pontas **operacionais**: rota inexistente devolvendo `404` e o health do Actuator
  no ar.

> Cada teste que usa um dataset alternativo roda em um banco H2 isolado, para não
> contaminar os demais (H2 em memória é único por nome dentro do mesmo JVM).

## API

Maturidade: **nível 2 de Richardson** (URI baseada em recurso, verbo HTTP `GET`,
códigos de status adequados, representação em JSON).

### `GET /producers/award-intervals`

Retorna o(s) produtor(es) com o menor e o maior intervalo entre dois prêmios
consecutivos. Tanto `min` quanto `max` são listas, então empates são representados
por completo.

**Resposta — `200 OK`** (conjunto de dados oficial):

```json
{
  "min": [
    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
  ],
  "max": [
    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
  ]
}
```

| Campo          | Descrição                                    |
|----------------|----------------------------------------------|
| `producer`     | Nome do produtor                             |
| `interval`     | Anos entre os dois prêmios consecutivos      |
| `previousWin`  | Ano do prêmio anterior                       |
| `followingWin` | Ano do prêmio seguinte                       |

## Documentação da API (Swagger)

Com a aplicação no ar:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

## Observabilidade

O Spring Boot Actuator expõe endpoints operacionais (sem instalação externa). Por
padrão, só o essencial fica aberto; métricas e detalhes de health ficam sob o perfil
`dev` (decisão de segurança — nada de prod ligado à toa):

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Métricas (perfil `dev`): `http://localhost:8080/actuator/metrics`

Erros seguem o formato **ProblemDetail (RFC 7807)**, com
`Content-Type: application/problem+json` — inclusive `404`/`405` (o
`GlobalExceptionHandler` estende `ResponseEntityExceptionHandler`, que converte os
erros conhecidos do Spring MVC) e um handler global para o inesperado, sem vazar
detalhes internos.

## Rodar com Docker

Build multi-stage (compila com Maven e roda só com o JRE):

```bash
docker build -t razzie-awards-api .
docker run -p 8080:8080 razzie-awards-api
```

## Decisões de projeto

- **Ingestão fiel, parsing no domínio.** A linha do CSV é persistida como está (a
  string crua de `producers` é mantida). Quebrar os produtores é uma
  responsabilidade de domínio, tratada no cálculo dos intervalos — não na camada de
  persistência.
- **Quebra robusta de produtores.** O campo `producers` mistura separadores
  (`,`, ` and `, e a vírgula de Oxford `, and `). Uma única expressão regular
  normaliza as três formas, então `"A, B, and C"` resulta corretamente em
  `[A, B, C]`.
- **Empates são cidadãos de primeira classe.** O menor/maior intervalo pode ser
  compartilhado por vários produtores; todos os empatados são retornados, e é por
  isso que `min`/`max` são listas.
- **Anos de vitória distintos.** Um produtor é mapeado para os anos *distintos* em
  que venceu, então uma hipotética vitória dupla no mesmo ano não cria um intervalo
  de tamanho zero.
- **O banco faz a filtragem.** Apenas filmes vencedores são buscados
  (`findByWinnerTrue`), mantendo o trabalho em memória proporcional aos dados que
  realmente importam.

## Estrutura do projeto

```
src/main/java/br/com/pedrofelipe/razzie
├── RazzieAwardsApplication.java
├── csv/MovieCsvLoader.java        # carrega o CSV no H2 na inicialização
├── model/Movie.java              # entidade JPA (uma linha do CSV)
├── repository/MovieRepository.java
├── service/
│   ├── ProducerParser.java        # quebra o campo cru de produtores
│   └── AwardIntervalService.java  # o cálculo de menor/maior intervalo
├── controller/
│   ├── AwardController.java
│   └── GlobalExceptionHandler.java   # erros padronizados (RFC 7807)
└── dto/
    ├── AwardInterval.java
    └── AwardIntervalResult.java
```

## Evoluções / como isso escalaria

O escopo do desafio é proposital e pequeno, então o código foi mantido proporcional
ao problema (sem broker, sem normalização desnecessária). Num cenário de produção,
porém, alguns caminhos naturais de evolução seriam:

- **Ingestão contínua via mensageria.** Em vez de uma carga única no startup, novos
  filmes poderiam chegar como eventos (Kafka / RabbitMQ), com um consumidor
  alimentando a base de forma incremental.
- **Banco gerenciado.** O H2 em memória daria lugar a um PostgreSQL, com migrações
  versionadas (Flyway / Liquibase).
- **Cache do resultado.** O cálculo de intervalos, hoje trivial em memória, poderia
  ser materializado ou cacheado (Redis) caso o volume e a frequência de acesso
  crescessem.
- **Observabilidade ampliada.** As métricas do Actuator poderiam ser exportadas para
  uma stack de monitoramento (Datadog / New Relic / Prometheus + Grafana).

São decisões de arquitetura deixadas de fora **de propósito**: o desafio pede uma
solução em memória, sem instalação externa, e adicioná-las aqui seria complexidade
sem ganho real para o problema proposto.
