package com.lcdw.rating.services;

import com.lcdw.rating.entities.Rating;

import java.util.List;

public interface RatingService {
    List<Rating> getAllRatings();
    Rating saveRating(Rating rating);
    List<Rating> getRatingsByUserId(String userId);
    List<Rating> getRatingByHotelId(String hotelId);
    List<Rating> getRatingsByHotelIds(List<String> hotelIds);
}
