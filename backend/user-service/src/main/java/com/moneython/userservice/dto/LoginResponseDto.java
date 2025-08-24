package com.moneython.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String accessToken;
    private String tokenType;
    private UserResponseDto user;
    
    public LoginResponseDto(String accessToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.user = user;
    }
}