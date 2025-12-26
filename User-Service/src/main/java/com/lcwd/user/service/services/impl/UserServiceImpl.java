package com.lcwd.user.service.services.impl;

import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Rating;
import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.exceptions.ResourceNotFoundException;
import com.lcwd.user.service.repositories.UserRepository;
import com.lcwd.user.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    /**
     * Fetch a single user with ratings and hotels
     */
    @Override
    public User getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found " + userId));

        // Fetch ratings for the user
        List<Rating> ratings = getUserRatings(userId);

        // Get all unique hotel IDs from the ratings
        String hotelIds = ratings.stream()
                .map(Rating::getHotelId)
                .distinct()
                .collect(Collectors.joining(","));

        // Fetch all hotels in a single batch call
        List<Hotel> hotels = getHotelsByIds(hotelIds);

        // Create a map of hotels for quick lookup
        Map<String, Hotel> hotelMap = hotels.stream()
                .collect(Collectors.toMap(Hotel::getHotelId, hotel -> hotel));

        // Set the hotel for each rating
        List<Rating> ratingList = ratings.stream().peek(rating ->
                rating.setHotel(hotelMap.get(rating.getHotelId()))
        ).collect(Collectors.toList());

        user.setRatings(ratingList);
        return user;
    }

    /**
     * Cached method to get user ratings from Rating-Service
     */
    @Cacheable(value = "ratings", key = "#userId")
    public List<Rating> getUserRatings(String userId) {
        Rating[] userRatings = restTemplate.getForObject(
                "http://RATING-SERVICE/ratings/users/" + userId,
                Rating[].class
        );
        return Arrays.asList(userRatings);
    }

    /**
     * Cached method to get hotel details from Hotel-Service
     */
    @Cacheable(value = "hotels", key = "#hotelIds")
    public List<Hotel> getHotelsByIds(String hotelIds) {
        Hotel[] hotels = restTemplate.getForObject(
                "http://HOTEL-SERVICE/hotels?ids=" + hotelIds,
                Hotel[].class
        );
        return Arrays.asList(hotels);
    }

    public Hotel getHotelById(String hotelId) {
        return restTemplate.getForObject(
                "http://HOTEL-SERVICE/hotels/" + hotelId,
                Hotel.class
        );
    }
}
