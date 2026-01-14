# 🎬 Cinema Reservation System

Projekt **Cinema Reservation System** to przykład systemu mikroserwisowego, który umożliwia zarządzanie rezerwacjami biletów w kinie.  
Składa się z trzech mikroserwisów:

- **auth-service** – obsługa autoryzacji i użytkowników
- **cinema-service** – logika biznesowa: filmy, seanse, rezerwacje
- **mail-service** – wysyłka powiadomień e-mail

Mikroserwisy komunikują się **asynchronicznie przez Apache Kafka**, co pozwala na skalowalne i niezależne przetwarzanie zdarzeń.

---

## 🛠 Wymagania

- Java 25+
- Maven 3+
- Docker

---

## 🐳 Uruchomienie baz danych i infrastruktury

Projekt korzysta z kontenerów Docker dla baz danych, Kafki oraz monitoringu. Monitoring w projekcie oparty jest na **Prometheus + Grafana**.  
W katalogu głównym projektu uruchom:

```bash
# Bazy danych
docker-compose -f docker-compose-app.yml up -d

# Kafka
docker-compose -f docker-compose-kafka.yml up -d

# Monitoring (Prometheus + Grafana)
docker-compose -f docker-compose-monitoring.yml up -d
```

---

## 🚀 Uruchomienie mikroserwisów

W katalogu projektu uruchom każdy mikroserwis oddzielnie:

```bash
# Budowanie modułów
./mvnw clean install

# Serwis autoryzacji
./mvnw -pl auth-service spring-boot:run

# Serwis biznesowy
./mvnw -pl cinema-service spring-boot:run

# Serwis mailowy
./mvnw -pl mail-service spring-boot:run

