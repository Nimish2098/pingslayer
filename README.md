# Uptime Monitor

A lightweight, reliable uptime monitoring and health check service built with **Spring Boot 3.3**, **Java 17**, and **Spring WebClient**. The application automatically pings registered endpoints on a configurable schedule, tracks historical performance metrics, and exposes a REST API for management and reporting.

---

## Tech Stack

* **Java 17**
* **Spring Boot 3.3**
    * Spring Web / WebFlux (`WebClient`)
    * Spring Data JPA
    * Spring Validation
    * Spring Scheduling
* **H2 Database** (File-based)
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
* Java 17 or higher installed.

### Running the Application
1. Clone the repository and navigate to the project directory.
2. Start the application using Maven:
   ```bash
   ./mvnw spring-boot:run