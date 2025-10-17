package com.lcwd.user.service.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="micro_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long userId;
    @Column(name="NAME")
    private String name;
    @Column(name="EMAIL")
    private String email;
    @Column(name = "ABOUT")
    private String about;
    //ratings are dynamically fetched from another microservice and not stored in the micro_users table,
    // so @Transient is the correct and necessary choice.
    //Not store in database
    @Transient
    private List<Rating> ratings= new ArrayList<>();
}
