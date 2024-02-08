package com.lcwd.hotel.services;

import com.lcwd.hotel.entities.Hotel;

import java.util.List;

public interface HotelService {
    Hotel saveHotel(Hotel hotel);

    //Get All Hotel
    List<Hotel> getAllHotel();

    Hotel getHotel(String hotelId);
}
