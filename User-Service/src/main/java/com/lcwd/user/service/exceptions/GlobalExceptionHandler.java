package com.lcwd.user.service.exceptions;

import com.lcwd.user.service.payload.ApiResponses;
import org.hibernate.annotations.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponses> handlerResourceNotFoundException(ResourceNotFoundException ex){
        String message = ex.getMessage();
        ApiResponses response = ApiResponses.builder().message(message).success(true).status(HttpStatus.NOT_FOUND).build();
        return new ResponseEntity<ApiResponses>(response, HttpStatus.NOT_FOUND);
    }
}
