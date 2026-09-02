FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app-0.1.0.jar app.jar
EXPOSE 24037
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--server.port=24037"]
