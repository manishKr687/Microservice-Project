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

-   **Reduced HTTP Requests**: The number of network requests to fetch a user's details is now constant: 1 for ratings and 1 for hotels, regardless of the number of ratings. This changes the request pattern from `1 + N` to a much more efficient `1 + 1`.
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
