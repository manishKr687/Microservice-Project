package com.lcwd.hotel.entities;

import lombok.Data;
import jakarta.persistence.*;
@Data
@Entity
@Table(name="micro_hotels")
public class Hotel {
    @Id
    @Column(name="ID")
    private String hotelId;
    @Column(name="NAME")
    private String name;
    @Column(name="LOCATION")
    private String location;
    @Column(name="ABOUT")
    private String about;
}
