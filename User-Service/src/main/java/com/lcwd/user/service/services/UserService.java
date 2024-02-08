package com.lcwd.user.service.services;

import com.lcwd.user.service.entities.User;

import java.util.List;

public interface UserService {

    //Create
    User saveUser(User user);

    //Get All User
    List<User> getAllUser();

    User getUser(String userId);





}
