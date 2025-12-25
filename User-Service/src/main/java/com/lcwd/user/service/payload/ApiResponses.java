package com.lcwd.user.service.payload;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponses {
    private String message;
    private boolean success;
    private Integer status;
}
