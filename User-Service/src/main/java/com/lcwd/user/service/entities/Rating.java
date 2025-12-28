package com.lcwd.user.service.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rating {
    private String ratingId;
    private String userId;
    private String hotelId;
    private int rating;
    private String feedback;
    private Hotel hotel;
}
