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

        // Fetch ratings for the user (cache per user)
        List<Rating> ratings = getUserRatings(userId);

        // Fetch hotel details for each rating (cache per hotel)
        List<Rating> ratingList = ratings.stream().map(rating -> {
            Hotel hotel = getHotelById(rating.getHotelId());
            rating.setHotel(hotel);
            return rating;
        }).collect(Collectors.toList());

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
    @Cacheable(value = "hotels", key = "#hotelId")
    public Hotel getHotelById(String hotelId) {
        return restTemplate.getForObject(
                "http://HOTEL-SERVICE/hotels/" + hotelId,
                Hotel.class
        );
    }
}
