package com.lcwd.hotel.controllers;

import com.lcwd.hotel.entities.Hotel;
import com.lcwd.hotel.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    //Create
    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel){
        Hotel hotel1= hotelService.saveHotel(hotel);
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel1);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getSingleHotel(@PathVariable String hotelId){
        Hotel hotel1= hotelService.getHotel(hotelId);
        return ResponseEntity.ok(hotel1);
    }

    //Get All Hotels
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotel(){
        List<Hotel> allHotels= hotelService.getAllHotel();
        return ResponseEntity.ok(allHotels);
    }
}
