package com.lcdw.rating.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="micro_rating")
public class Rating {
    @Id
    @Column(name ="ID")
    private String ratingId;
    @Column(name ="USER_ID")
    private String userId;
    @Column(name ="HOTEL_ID")
    private String hotelId;
    @Column(name ="RATING")
    private Integer rating;
    @Column(name ="FEEDBACK")
    private String feedback;
}
