FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/finalProject_3-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9080
ENTRYPOINT ["java", "-jar", "app.jar"]
