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
