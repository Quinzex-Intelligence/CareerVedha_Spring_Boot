package com.quinzex.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "captchaClient", url = "https://www.google.com")
public interface CaptchaClient {

    @PostMapping("/recaptcha/api/siteverify")
    Map<String,Object> verifyCaptcha(@RequestParam("secret")String secret, @RequestParam("response")String response);
}
