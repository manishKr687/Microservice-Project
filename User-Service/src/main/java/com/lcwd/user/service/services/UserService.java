package com.lcwd.user.service.services;

import com.lcwd.user.service.dtos.UserDto;
import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Rating;

import java.util.List;

public interface UserService {

    //Create
    UserDto saveUser(UserDto userDto);

    //Get All User
    List<UserDto> getAllUser();

    UserDto getUser(String userId);

    List<Rating> getUserRatings(String userId);

    Hotel getHotelById(String hotelId);
}
