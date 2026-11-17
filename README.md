# Uptime Monitor

A lightweight uptime monitoring and health check service built with **Spring Boot 3.3**, **Java 21**, and **Spring WebClient**. The application automatically pings registered endpoints on a configurable schedule, tracks historical performance metrics, and exposes a REST API for management and reporting.

---

## Tech Stack

* **Java 21**
* **Spring Boot 3.3**
    * Spring Web / WebFlux (`WebClient`)
    * Spring Data JPA
    * Spring Validation
    * Spring Scheduling
* **TimescaleDB** (PostgreSQL 16)
* **Testing:** JUnit 5, MockWebServer, Reactor Test, MockMvc

---

## Key Features

* **Endpoint Management:** Register, list, and delete endpoints with custom ping intervals, timeout settings, and HTTP methods.
* **Asynchronous Health Checks:** Non-blocking health checks utilizing reactive streams.
* **Smart Scheduler:** Background task scheduling designed to prevent overlapping execution runs.
* **Historical Tracking:** Persists status codes, response times, success flags, and error messages for each check.

---

## Getting Started

### Prerequisites
* Java 21 or higher installed.
* Docker with Docker Compose.

### Running the Application
1. Start the TimescaleDB container:
   ```powershell
   docker compose up -d db
   ```
2. Start the application using Maven:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Flyway applies the database migrations on startup. The local database defaults are
`pingslayer` / `ping` / `ping`; override them with `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` when needed.