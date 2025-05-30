# Verwende Java 11
FROM openjdk:11-jdk-slim

# Setze das Arbeitsverzeichnis
WORKDIR /app

# Kopiere die JAR-Datei ins Image
COPY target/*.jar app.jar

# Öffne den Port für dein Backend (grp-13 = 53217)
EXPOSE 53217

# Startbefehl für deine App
ENTRYPOINT ["java", "-jar", "app.jar"]
