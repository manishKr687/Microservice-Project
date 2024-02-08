package com.lcwd.user.service.entities;

import lombok.Data;

@Data
public class Rating {
    private String userId;
    private String ratingId;
    private String hotelId;
    private int rating;
    private String feedback;
}
