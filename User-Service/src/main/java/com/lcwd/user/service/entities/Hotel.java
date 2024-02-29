package com.lcwd.user.service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Hotel {

    private String hotelId;
    private String name;
    private String location;
    private String about;

}
