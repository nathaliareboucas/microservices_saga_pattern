FROM openjdk:17-alpine
COPY ./target/payment-service.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]