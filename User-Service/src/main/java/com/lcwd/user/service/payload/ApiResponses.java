package com.lcwd.user.service.payload;

import lombok.*;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponses {
    private String message;
    private boolean success;
    private HttpStatus status;
}
