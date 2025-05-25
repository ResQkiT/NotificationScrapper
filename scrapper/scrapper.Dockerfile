FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
COPY lombok.config .

COPY scrapper/ ./scrapper/

RUN mvn clean install -f scrapper/pom.xml -DskipTests -e

FROM openjdk:23-jdk-slim

COPY --from=build /app/scrapper/target/*.jar /app.jar

EXPOSE 8081
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app.jar"]
