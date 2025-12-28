package com.lcwd.user.service.controllers;

import com.lcwd.user.service.dtos.UserDto;
import com.lcwd.user.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        UserDto userDto1 = userService.saveUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto1);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getSingleUser(@PathVariable String userId){
        UserDto userDto = userService.getUser(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUser(){
        List<UserDto> allUsers = userService.getAllUser();
        return ResponseEntity.ok(allUsers);
    }
}
