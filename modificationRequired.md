# Performance Improvement: Solving the N+1 Query Problem

## The Problem: N+1 Query Issue

The original implementation of the `User-Service` had a significant performance bottleneck known as the **N+1 query problem**. When fetching a user's details, the service would:

1.  Make a single call to the `User-Service` database to get the user's information.
2.  Make a single call to the `Rating-Service` to get a list of all ratings for that user. Let's say this returns *N* ratings.
3.  For **each** of the *N* ratings, make a separate network call to the `Hotel-Service` to fetch the details of the hotel associated with that rating.

This resulted in a total of `1 (user) + 1 (ratings) + N (hotels)` = `2 + N` network requests to assemble a single user's data. As the number of ratings per user grows, this approach becomes increasingly inefficient, leading to high latency and poor scalability.

## The Solution: Batching Requests

To solve this problem, we've implemented a **batching** strategy. The core idea is to fetch multiple resources in a single network request instead of making many small requests.

Here's how the improved implementation works:

1.  **`Hotel-Service` Enhancement**: We introduced a new endpoint in the `HotelController` (`GET /hotels?ids=...`) that can accept a list of hotel IDs and return a list of hotels. This allows us to fetch details for multiple hotels in one call.

2.  **`Rating-Service` Enhancement**: Similarly, we added an endpoint to the `RatingController` (`GET /ratings/hotel?ids=...`) to get all ratings for a given list of hotel IDs.

3.  **`User-Service` Refactoring**: The `UserServiceImpl` was refactored to:
    1.  Fetch the user's ratings as before.
    2.  Collect all the unique `hotelId`s from the list of ratings.
    3.  Make a **single** call to the new batch endpoint in the `Hotel-Service` (`/hotels?ids=...`) to get all the required hotel details at once.
    4.  Create a `Map` of hotels for quick lookup (`hotelId` -> `Hotel` object).
    5.  Iterate through the ratings and attach the `Hotel` object from the map, avoiding any further network calls.

4.  **Introducing Feign Client**: To make the inter-service communication cleaner, more readable, and more robust, we've introduced the Spring Cloud `Feign` client. It allows us to define a declarative client interface for our services, handling the boilerplate of `RestTemplate` for us.

## Benefits of the New Approach

-   **Reduced HTTP Requests**: The number of network requests to fetch a user's details is now constant: 1 for ratings and 1 for all hotels, regardless of the number of ratings. This changes the request pattern from `1 + N` to a much more efficient `1 + 1`.
-   **Lower Latency**: Fewer network calls mean a significant reduction in the overall time it takes to process a request.
-   **Improved Scalability**: The application can now handle users with a large number of ratings much more efficiently, making the system more scalable.
-   **Cleaner Code**: Using Feign clients makes the code in the `User-Service` that calls other services more declarative and easier to maintain.

## Performance Gain

The performance improvement can be quantified by looking at the reduction in network requests.

Let *N* be the number of ratings for a given user.

-   **Original Approach**: `1 + N` network requests (1 for ratings + N for hotels).
-   **New Approach**: `2` network requests (1 for ratings + 1 for all hotels).

The number of requests is reduced by `(1 + N) - 2 = N - 1`.

The percentage improvement in terms of network requests is:

`((Old - New) / Old) * 100 = ((1 + N) - 2) / (1 + N) * 100 = ((N - 1) / (N + 1)) * 100`

For example:
-   If a user has **10 ratings** (N=10), the improvement is `(9 / 11) * 100` which is approximately **82%**.
-   If a user has **50 ratings** (N=50), the improvement is `(49 / 51) * 100` which is approximately **96%**.
-   If a user has **100 ratings** (N=100), the improvement is `(99 / 101) * 100` which is approximately **98%**.

As *N* grows, the performance gain approaches 100%, making the new solution significantly more performant and scalable.

# Lack of Resilience: Circuit Breaker Implementation

## The Problem: Cascading Failures

The original implementation of inter-service communication using `RestTemplate` lacked resilience patterns. If a downstream service (like `Hotel-Service` or `Rating-Service`) became unavailable or slow, the `User-Service` would continue to make requests to it, leading to:

-   **Blocked Threads**: Requests to the failing service would tie up threads in the `User-Service`, eventually exhausting its thread pool.
-   **Increased Latency**: Users would experience long delays as the `User-Service` waited for timeouts from the failing service.
-   **Cascading Failures**: The `User-Service` itself could become unresponsive, potentially causing failures in other upstream services that depend on it.

## The Solution: Circuit Breaker Pattern with Resilience4j

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

## Benefits of the Circuit Breaker Implementation

-   **Prevents Cascading Failures**: By quickly failing and short-circuiting calls to unhealthy services, the circuit breaker protects the `User-Service` from being overwhelmed and prevents failures from spreading throughout the system.
-   **Improved User Experience**: Fallback mechanisms ensure that users receive a response, even if it's a degraded one, rather than experiencing a complete service outage or long timeouts.
-   **Increased System Stability**: Services can recover gracefully from temporary outages or performance issues in their dependencies.
-   **Automated Recovery**: The circuit breaker automatically monitors the health of the downstream service and attempts to reconnect when it recovers, reducing manual intervention.

# Outdated Inter-service Communication: Migration to Spring Cloud OpenFeign

## The Problem: Legacy `RestTemplate`

The project initially used `RestTemplate` for inter-service communication. While functional, `RestTemplate` is considered a legacy approach in modern Spring Boot applications for several reasons:

-   **Boilerplate Code**: Requires manual construction of URLs, request bodies, and parsing of responses, leading to verbose and error-prone code.
-   **Lack of Type Safety**: Responses often need to be manually cast, which can lead to runtime errors.
-   **Limited Features**: Lacks built-in support for advanced features like retry mechanisms, load balancing (without manual configuration), and declarative client definitions.
-   **Less Readable**: The imperative style of `RestTemplate` can make the intent of inter-service calls less clear compared to declarative approaches.

## The Solution: Spring Cloud OpenFeign

To modernize and improve inter-service communication, we migrated from `RestTemplate` to `Spring Cloud OpenFeign`. OpenFeign is a declarative REST client that significantly simplifies making HTTP requests to other microservices.

Here's how the implementation works in the `User-Service`:

1.  **Dependency Addition**: The `spring-cloud-starter-openfeign` dependency was added to the `User-Service/pom.xml`. This integrates the OpenFeign library into the project.

2.  **Enable Feign Clients**: The `@EnableFeignClients` annotation was added to the main application class (`UserServiceApplication.java`). This annotation enables Feign client scanning and proxy creation, allowing Spring Boot to find and implement the defined Feign interfaces.

3.  **Declarative Feign Interfaces**: New Java interfaces (`HotelService.java` and `RatingService.java`) were created in the `com.lcwd.user.service.external.services` package.
    -   These interfaces are annotated with `@FeignClient(name = "SERVICE-NAME")`, where `SERVICE-NAME` is the logical name of the target microservice (e.g., `HOTEL-SERVICE`, `RATING-SERVICE`) as registered in Eureka.
    -   Methods within these interfaces are defined using Spring Web annotations (`@GetMapping`, `@PathVariable`, `@RequestParam`) to mimic the REST endpoints of the target service. Feign automatically generates an implementation of these interfaces at runtime, handling the HTTP communication details.

4.  **Refactoring `UserServiceImpl`**: The `UserServiceImpl.java` was refactored to:
    -   Remove the `@Autowired RestTemplate` field.
    -   Inject the newly created `HotelService` and `RatingService` Feign client interfaces.
    -   Replace all direct `restTemplate.getForObject()` calls with calls to the corresponding methods on the `hotelService` and `ratingService` Feign clients. This makes the code much cleaner and more readable.

5.  **Remove `RestTemplate` Bean**: The `RestTemplate` bean definition was removed from `MyConfig.java`, as it is no longer required.

## Benefits of Using Spring Cloud OpenFeign

-   **Declarative API**: Defines REST clients as simple interfaces, making the code more readable and reducing boilerplate.
-   **Type Safety**: Methods in Feign interfaces are type-safe, preventing common runtime errors associated with manual type casting.
-   **Simplified Integration**: Seamlessly integrates with Spring Cloud components like Eureka (for service discovery) and Ribbon (for client-side load balancing).
-   **Improved Maintainability**: Changes to external service APIs can be reflected more easily by updating the Feign interface, centralizing API contract definitions.
-   **Built-in Resilience**: Works well with circuit breakers (like Resilience4j, as implemented) and retries, enhancing the overall resilience of the system.
-   **Cleaner Codebase**: Eliminates the need for manual HTTP client configuration, leading to a more concise and focused business logic.