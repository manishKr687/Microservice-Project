package com.lcdw.rating.controller;

import com.lcdw.rating.entities.Rating;
import com.lcdw.rating.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {
    @Autowired
    private RatingService ratingService;
    @PostMapping
    public ResponseEntity<Rating> create(@RequestBody Rating rating){
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.saveRating(rating));
    }
    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings(){
        return ResponseEntity.ok(ratingService.getAllRatings());
    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Rating>> getRatingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ratingService.getRatingsByUserId(userId));
    }
    @GetMapping("/hotel")
    public ResponseEntity<List<Rating>> getRatingByHotel(@RequestParam(required = true) String ids){
        List<String> hotelIds = Arrays.asList(ids.split(","));
        return ResponseEntity.ok(ratingService.getRatingsByHotelIds(hotelIds));
    }
}
