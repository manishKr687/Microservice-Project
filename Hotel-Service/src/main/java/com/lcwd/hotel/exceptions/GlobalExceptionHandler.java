package com.lcwd.hotel.exceptions;

import com.lcwd.hotel.payload.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponses> handlerResourceNotFoundException(ResourceNotFoundException ex){
        String message = ex.getMessage();
        ApiResponses response = ApiResponses.builder().message(message).success(false).status(HttpStatus.NOT_FOUND.value()).build();
        return new ResponseEntity<ApiResponses>(response, HttpStatus.NOT_FOUND);
    }
}