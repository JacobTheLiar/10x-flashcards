# Stage 1: Build application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Kopiowanie plików pom.xml dla wykorzystania cache warstw
COPY pom.xml .
# Kopiowanie skryptu Maven wrapper
COPY mvnw* .
COPY .mvn .mvn

# Pobieranie zależności (wykorzystanie cache)
RUN ./mvnw dependency:go-offline -B

# Kopiowanie kodu źródłowego
COPY src ./src

# Budowanie aplikacji
RUN ./mvnw package -DskipTests

# Stage 2: Uruchomienie aplikacji
FROM eclipse-temurin:21-jre

WORKDIR /app

# Kopiowanie zbudowanej aplikacji z poprzedniego etapu
COPY --from=builder /app/target/*.jar app.jar

# Definicja zmiennych środowiskowych
ENV SPRING_PROFILES_ACTIVE=prod

# Odsłonięcie portu aplikacji
EXPOSE 8080

# Ustawienie punktu wejścia
ENTRYPOINT ["java", "-jar", "app.jar"]