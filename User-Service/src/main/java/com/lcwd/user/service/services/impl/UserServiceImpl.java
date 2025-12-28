package com.lcwd.user.service.services.impl;

import com.lcwd.user.service.dtos.UserDto;
import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Rating;
import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.exceptions.ResourceNotFoundException;
import com.lcwd.user.service.repositories.UserRepository;
import com.lcwd.user.service.services.UserService;
import com.lcwd.user.service.external.services.HotelService;
import com.lcwd.user.service.external.services.RatingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }


    @Override
    public List<UserDto> getAllUser() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single user with ratings and hotels
     */
    @Override
    public UserDto getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found " + userId));

        // Fetch ratings for the user
        List<Rating> ratings = ratingService.getRatings(userId);

        // Get all unique hotel IDs from the ratings
        String hotelIds = ratings.stream()
                .map(Rating::getHotelId)
                .distinct()
                .collect(Collectors.joining(","));

        // Fetch all hotels in a single batch call
        List<Hotel> hotels = hotelService.getHotelsByIds(hotelIds);

        // Create a map of hotels for quick lookup
        Map<String, Hotel> hotelMap = hotels.stream()
                .collect(Collectors.toMap(Hotel::getId, hotel -> hotel));

        // Set the hotel for each rating
        List<Rating> ratingList = ratings.stream().peek(rating ->
                rating.setHotel(hotelMap.get(rating.getHotelId()))
        ).collect(Collectors.toList());

        user.setRatings(ratingList);
        return modelMapper.map(user, UserDto.class);
    }

    /**
     * Cached method to get user ratings from Rating-Service
     */
    @Cacheable(value = "ratings", key = "#userId")
    @CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "getUserRatingsFallback")
    public List<Rating> getUserRatings(String userId) {
        return ratingService.getRatings(userId);
    }

    public List<Rating> getUserRatingsFallback(String userId, Exception ex) {
        return new ArrayList<>();
    }

    /**
     * Cached method to get hotel details from Hotel-Service
     */
    @Cacheable(value = "hotels", key = "#hotelIds")
    @CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "getHotelsByIdsFallback")
    public List<Hotel> getHotelsByIds(String hotelIds) {
        return hotelService.getHotelsByIds(hotelIds);
    }

    public List<Hotel> getHotelsByIdsFallback(String hotelIds, Exception ex) {
        return new ArrayList<>();
    }

    @CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "getHotelByIdFallback")
    public Hotel getHotelById(String hotelId) {
        return hotelService.getHotel(hotelId);
    }

    public Hotel getHotelByIdFallback(String hotelId, Exception ex) {
        return new Hotel("0", "None", "None", "None");
    }
}
