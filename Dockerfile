FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY logging-starter ./logging-starter
RUN mvn -B -ntp -f logging-starter/pom.xml -Dmaven.test.skip=true install
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/senla-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
