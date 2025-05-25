FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
COPY lombok.config .

COPY bot/ ./bot/

RUN mvn clean install -f bot/pom.xml -DskipTests -e

FROM openjdk:23-jdk-slim

COPY --from=build /app/bot/target/*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
