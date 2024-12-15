package com.blog.payloads;

import lombok.Data;

@Data
public class JwtAuthResponse {

    private String jwtToken;
    
    private UserDto user;

}
