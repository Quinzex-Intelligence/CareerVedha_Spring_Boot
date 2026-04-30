package com.quinzex.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String otp;
    private String captchaToken;
}
