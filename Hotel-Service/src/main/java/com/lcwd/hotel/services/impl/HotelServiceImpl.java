package com.lcwd.hotel.services.impl;

import com.lcwd.hotel.entities.Hotel;
import com.lcwd.hotel.exceptions.ResourceNotFoundException;
import com.lcwd.hotel.repositories.HotelRepository;
import com.lcwd.hotel.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class HotelServiceImpl implements HotelService {
    @Autowired
    private HotelRepository hotelRepository;
    @Override
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel) ;
    }

    //Get All Hotel
    @Override
    public List<Hotel> getAllHotel() {
        return hotelRepository.findAll();
    }
    @Override
    public Hotel getHotel(String hotelId) {
        return hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Not Found"+hotelId));
    }

    @Override
    public List<Hotel> getAllHotelsByIds(List<String> hotelIds) {
        return hotelRepository.findAllById(hotelIds);
    }
}
