# Build-Phase
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Nur die pom.xml kopieren und die Abhängigkeiten herunterladen
COPY pom.xml .
RUN mvn dependency:go-offline

# Jetzt den restlichen Code kopieren und das Projekt bauen
COPY . .
RUN mvn clean package -DskipTests

# Run-Phase
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/starlink-watchtower-0.0.1-SNAPSHOT.jar /app/starlink-watchtower.jar
EXPOSE 8111
CMD ["java", "-jar", "/app/starlink-watchtower.jar"]
