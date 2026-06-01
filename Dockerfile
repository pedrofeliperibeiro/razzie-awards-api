# ---- Estágio de build: compila e empacota o jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primeiro só o pom, pra aproveitar o cache de dependências do Docker
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Agora o código e o empacotamento (testes rodam no build)
COPY src ./src
RUN mvn -q clean package

# ---- Estágio de runtime: imagem leve, só com o JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/razzie-awards-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
