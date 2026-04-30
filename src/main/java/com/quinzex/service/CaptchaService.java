package com.quinzex.service;

import com.quinzex.FeignClient.CaptchaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaClient captchaClient;
    @Value("${recaptcha.secret-key}")
    private String secretKey;

    public boolean verifyCaptcha(String captchaResponse) {
        if(captchaResponse == null||captchaResponse.isEmpty()){
            return false;
        }
        Map<String,Object> response = captchaClient.verifyCaptcha(secretKey,captchaResponse);
        System.out.println("GOOGLE RESPONSE: " + response);
        return (Boolean)response.get("success");
    }
}
