package com.lcwd.user.service.dtos;

import com.lcwd.user.service.entities.Rating;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto {
    private String userId;
    private String name;
    private String email;
    private String about;
    private List<Rating> ratings = new ArrayList<>();
}
