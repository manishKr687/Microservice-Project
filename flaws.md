Here is a checklist of flaws identified in the codebase, along with their solutions. We can mark them as done as we resolve them.

- [Done] **Code Duplication and Tight Coupling:** The `User-Service` contains its own entity definitions for `Hotel` and `Rating`, which are already defined in their respective services. This leads to code duplication and creates a tight coupling between the services. Any change in the `Hotel` or `Rating` service's data model would require a corresponding change in the `User-Service`.

    **Solution:** Remove the `Hotel` and `Rating` entity classes from the `User-Service`. Instead, create plain old Java object (POJO) classes for `Hotel` and `Rating` in the `User-Service` that contain only the fields needed by the `User-Service`. These POJOs will be used to deserialize the responses from the `Hotel-Service` and `Rating-Service`. **(DONE)**

    **Changes Made:**
    - Replaced the content of `User-Service/src/main/java/com/lcwd/user/service/entities/Hotel.java` with the following POJO class:
    ```java
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public class Hotel {                    //older code

            private String hotelId;
            private String name;
            private String location;
            private String about;

        }
        @Getter  
        @Setter
        public class Hotel {       //Modified Code
            private String id;
            private String name;   
            private String location;
            private String about;
        }

    ```
    - Replaced the content of `User-Service/src/main/java/com/lcwd/user/service/entities/Rating.java` with the following POJO class:
    ```java
    @Data
    public class Rating {     //Older code
        private String userId;
        private String ratingId;
        private String hotelId;
        private int rating;
        private String feedback;
        private Hotel hotel;
    }
    
    @Getter
    @Setter
    public class Rating {  //Modified Code
        private String ratingId;
        private String userId;
        private String hotelId;
        private int rating;
        private String feedback;
        private Hotel hotel;
    }
    ```

- [Done] **Lack of Resilience:** The communication between services is done using `RestTemplate` without any resilience patterns like circuit breakers. If the `Hotel-Service` or `Rating-Service` is unavailable, the calls from `User-Service` will fail, a cascading failure.

    **Solution:** Implement a circuit breaker pattern using a library like `Spring Cloud Circuit Breaker` with `Resilience4j`. Wrap the `RestTemplate` calls in a circuit breaker to provide fallback mechanisms when a downstream service is unavailable. This will prevent a single service failure from cascading to other services. **(DONE)**

    **Changes Made:**
    - Added `spring-cloud-starter-circuitbreaker-resilience4j` dependency to `pom.xml` of `User-Service`.
    - Applied `@CircuitBreaker` annotation to `getUserRatings`, `getHotelsByIds`, and `getHotelById` methods in `UserServiceImpl.java`.
    - Created fallback methods `getUserRatingsFallback`, `getHotelsByIdsFallback`, and `getHotelByIdFallback` in `UserServiceImpl.java`.
    - Configured Resilience4j circuit breaker named `ratingHotelBreaker` in `application.properties` of `User-Service`.

- [Done] **Outdated Inter-service Communication:** The project uses `RestTemplate`, which is a legacy approach for inter-service communication. A more modern and robust approach is to use declarative REST clients like Spring Cloud OpenFeign.

    **Solution:** Replace `RestTemplate` with `Spring Cloud OpenFeign`. Define Feign client interfaces for the `Hotel-Service` and `Rating-Service` and let Spring Boot automatically implement them. This will result in cleaner and more maintainable code for inter-service communication. **(DONE)**

    **Changes Made:**
    - Added `spring-cloud-starter-openfeign` dependency to `pom.xml` of `User-Service`.
    - Added `@EnableFeignClients` annotation to `UserServiceApplication.java`.
    - Created `HotelService` and `RatingService` Feign client interfaces in `com.lcwd.user.service.external.services` package.
    - Modified `UserServiceImpl.java` to use `HotelService` and `RatingService` Feign clients instead of `RestTemplate`.
    - Removed `RestTemplate` bean configuration from `MyConfig.java`.

- [Done] **Configuration and Security Issues:**
    - [ ] Database credentials are hardcoded in the `application.properties` file. This is a security risk.
    - [ ] The property `spring.jpa.hibernate.ddl-auto` is set to `update`, which is not safe for production environments.

    **Solutions:**
    *   Externalize the database credentials using environment variables or a configuration server like `Spring Cloud Config`.
    *   Change the `spring.jpa.hibernate.ddl-auto` property to `validate` or `none` in production and use a database migration tool like `Flyway` or `Liquibase` to manage schema changes.

- [Done] **Missing Observability:** The services are missing the `spring-boot-starter-actuator` dependency. This dependency is essential for monitoring and managing the microservices in a production environment, as it provides health checks, metrics, and other operational data.

    **Solution:** Add the `spring-boot-starter-actuator` dependency to the `pom.xml` of each microservice. This will expose several production-ready endpoints that can be used for monitoring and management.

- [Done] **Poor API Design:** The controllers in the `User-Service` directly expose the JPA entity (`User`) in the API requests and responses. It is a better practice to use Data Transfer Objects (DTOs) to decouple the API contract from the internal database schema.

    **Solution:** Introduce DTOs for the `User` entity. The controller should accept DTOs as request bodies and return DTOs as response bodies. This decouples the API from the database schema and allows the API to evolve independently of the internal data model. A mapping library like `ModelMapper` or `MapStruct` can be used to simplify the conversion between entities and DTOs.

---
## Notes
- It is recommended to apply these changes in the order they are listed.
- After applying the changes, it is important to test the application thoroughly to ensure that everything is working as expected.
- For a production environment, it is highly recommended to use a configuration server to manage the configuration of all the microservices.
