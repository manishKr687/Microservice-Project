# Spring Boot Microservices Project

This project is a demonstration of a microservices architecture using Spring Boot. It consists of four main services: a Service Registry, a User Service, a Hotel Service, and a Rating Service.

## Architecture

The architecture follows a standard microservice pattern with a central service registry for service discovery.

-   **Service Registry**: All services register themselves here. This allows services to communicate with each other using logical service names instead of hardcoded IP addresses and ports.
-   **User-Service**: This is the main service that clients would interact with. It manages user information and aggregates data from the other services.
-   **Hotel-Service**: This service manages hotel information.
-   **Rating-Service**: This service manages user ratings for hotels.

When a request for a user's details comes to the User-Service, it fetches the user's information from its own database and then makes REST calls to the Rating-Service to get all the ratings for that user. It also fetches details about the hotels for each rating from the Hotel-Service.

```
+-----------------+      +------------------+      +-------------------+
|  Client         |----->|   User-Service   |----->|   Rating-Service  |
+-----------------+      +------------------+      +-------------------+
                             |
                             |
                             v
+-----------------+      +------------------+
| Service-Registry|      |   Hotel-Service  |
+-----------------+      +------------------+
```

## Services

### 1. Service-Registry

This is a Netflix Eureka server that acts as the service discovery mechanism for the microservices.

-   **Port**: 8761
-   **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

### 2. User-Service

This service manages users and their interactions with other services.

-   **Port**: 8081

### 3. Hotel-Service

This service manages all hotel-related information.

-   **Port**: 8082

### 4. Rating-Service

This service manages all ratings given by users to hotels.

-   **Port**: 8083

## Getting Started

To run this project, you need to have Java and Maven installed. You also need a running PostgreSQL instance.

### Database Setup

1.  Create a PostgreSQL database named `microservices`.
2.  Update the `spring.datasource.username` and `spring.datasource.password` in the `application.properties` file of each service (`User-Service`, `Hotel-Service`, `Rating-Service`) with your PostgreSQL credentials.

### Running the Services

You need to run each service in a separate terminal. Navigate to the root directory of each service and run the following command:

```bash
mvn spring-boot:run
```

The services should be started in the following order:

1.  `Service-Registry`
2.  `Hotel-Service`
3.  `Rating-Service`
4.  `User-Service`

## API Endpoints

### User-Service

| Method | Endpoint          | Description      |
|--------|-------------------|------------------|
| POST   | `/users`          | Create a new user|
| GET    | `/users/{userId}` | Get a single user|
| GET    | `/users`          | Get all users    |

### Hotel-Service

| Method | Endpoint            | Description         |
|--------|---------------------|---------------------|
| POST   | `/hotels`           | Create a new hotel  |
| GET    | `/hotels/{hotelId}` | Get a single hotel  |
| GET    | `/hotels`           | Get all hotels      |

### Rating-Service

| Method | Endpoint                  | Description                          |
|--------|---------------------------|--------------------------------------|
| POST   | `/ratings`                | Create a new rating                  |
| GET    | `/ratings`                | Get all ratings                      |
| GET    | `/ratings/users/{userId}` | Get all ratings for a specific user  |
| GET    | `/ratings/hotels/{hotelId}`| Get all ratings for a specific hotel |

## API Documentation (Swagger)

API documentation is available through Swagger UI. Once the services are running, you can access the UI at the following URLs:

-   **User-Service:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
-   **Hotel-Service:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
-   **Rating-Service:** [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

## Resilience with Circuit Breaker

### The Problem: Cascading Failures

The original implementation of inter-service communication using `RestTemplate` lacked resilience patterns. If a downstream service (like `Hotel-Service` or `Rating-Service`) became unavailable or slow, the `User-Service` would continue to make requests to it, leading to:

-   **Blocked Threads**: Requests to the failing service would tie up threads in the `User-Service`, eventually exhausting its thread pool.
-   **Increased Latency**: Users would experience long delays as the `User-Service` waited for timeouts from the failing service.
-   **Cascading Failures**: The `User-Service` itself could become unresponsive, potentially causing failures in other upstream services that depend on it.

### The Solution: Circuit Breaker Pattern with Resilience4j

To address the lack of resilience, we implemented the Circuit Breaker pattern using `Spring Cloud Circuit Breaker` with `Resilience4j`. This pattern helps prevent cascading failures and improves the fault tolerance of the microservice architecture.

Here's how the implementation works in the `User-Service`:

1.  **Dependency Addition**: The `spring-cloud-starter-circuitbreaker-resilience4j` dependency was added to the `User-Service/pom.xml`. This brings in the necessary libraries for implementing circuit breakers.

2.  **Circuit Breaker Annotation**: The `@CircuitBreaker` annotation was applied to the methods responsible for calling external services (`getUserRatings`, `getHotelsByIds`, and `getHotelById` in `UserServiceImpl.java`). This annotation tells Spring Cloud to wrap these method calls with a Resilience4j circuit breaker.
    -   Each circuit breaker instance is given a name (e.g., `ratingHotelBreaker`).
    -   A `fallbackMethod` is specified (e.g., `getUserRatingsFallback`). This method is invoked when the circuit breaker is open or if the original method call fails for other reasons (e.g., network issues, service unavailability).

3.  **Fallback Methods**: For each method protected by a circuit breaker, a corresponding fallback method was created (e.g., `getUserRatingsFallback(String userId, Exception ex)`).
    -   These methods provide a graceful degradation mechanism. Instead of throwing an exception or hanging, they return a default or empty response (e.g., an empty list of ratings or a default `Hotel` object). This allows the `User-Service` to continue functioning, albeit with potentially partial data, when a downstream service is struggling.
    -   The `Exception ex` parameter in the fallback method allows for logging or inspecting the cause of the failure.

4.  **Configuration in `application.properties`**: The behavior of the `ratingHotelBreaker` circuit breaker was configured in `User-Service/src/main/resources/application.properties`. Key configuration parameters include:
    -   `resilience4j.circuitbreaker.instances.ratingHotelBreaker.sliding-window-size`: Defines the number of calls to record for calculating the failure rate.
    -   `resilience4j.circuitbreaker.instances.ratingHotelBreaker.failure-rate-threshold`: The percentage of failed calls at which the circuit breaker opens.
    -   `resilience4j.circuitbreaker.instances.ratingHotelBreaker.wait-duration-in-open-state`: The time the circuit breaker stays open before transitioning to half-open.
    -   `resilience4j.circuitbreaker.instances.ratingHotelBreaker.permitted-number-of-calls-in-half-open-state`: The number of calls allowed in the half-open state to test if the service has recovered.
    -   `resilience4j.circuitbreaker.instances.ratingHotelBreaker.sliding-window-type`: Specifies whether the sliding window is time-based or count-based.

### Benefits of the Circuit Breaker Implementation

-   **Prevents Cascading Failures**: By quickly failing and short-circuiting calls to unhealthy services, the circuit breaker protects the `User-Service` from being overwhelmed and prevents failures from spreading throughout the system.
-   **Improved User Experience**: Fallback mechanisms ensure that users receive a response, even if it's a degraded one, rather than experiencing a complete service outage or long timeouts.
-   **Increased System Stability**: Services can recover gracefully from temporary outages or performance issues in their dependencies.
-   **Automated Recovery**: The circuit breaker automatically monitors the health of the downstream service and attempts to reconnect when it recovers, reducing manual intervention.
