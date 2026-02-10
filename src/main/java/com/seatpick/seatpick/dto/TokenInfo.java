package com.seatpick.seatpick.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    private String grantType;   // "Bearer"
    private String accessToken;
    private String refreshToken;
}