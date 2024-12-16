FROM openjdk:17-alpine
COPY ./target/product-validation-service.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]