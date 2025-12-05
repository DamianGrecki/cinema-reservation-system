# Cinema Reservation System

---

## Wymagania

- Java 25+
- Maven 3+
- Docker

---

## 1. Uruchomienie baz danych w kontenerze

W katalogu projektu uruchom:

```bash
docker-compose up -d
```
## 2. Uruchomienie serwisów

W katalogu projektu uruchom:

```bash
./mvnw -pl auth-service spring-boot:run
./mvnw -pl cinema-service spring-boot:run
```