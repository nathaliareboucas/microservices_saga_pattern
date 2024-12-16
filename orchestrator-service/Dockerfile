FROM openjdk:17-alpine
COPY ./target/orchestrator-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]