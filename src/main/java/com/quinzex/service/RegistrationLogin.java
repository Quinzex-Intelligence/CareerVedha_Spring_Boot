package com.quinzex.service;

import com.quinzex.dto.*;
import com.quinzex.entity.LmsLogin;
import com.quinzex.entity.Permission;
import com.quinzex.entity.Roles;
import com.quinzex.repository.LmsLoginRepo;
import com.quinzex.repository.RolesRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RegistrationLogin implements IRegistrationLogin {

    private final LmsLoginRepo loginRepo;

    private final RolesRepo rolesRepo;

    private final IotpService iotpService;

    private final JwtService jwtService;

    private final IemailService iemailService;

    private final IrefreshTokenService irefreshTokenService;

    private final RedisTemplate<String,Object> redisTemplate;

    private final IRoleNotificationProducer roleNotificationProducer;

    private static final int MAX_ATTEMPTS = 3;


    public RegistrationLogin(LmsLoginRepo loginRepo, RolesRepo rolesRepo, IotpService iotpService, JwtService jwtService , IrefreshTokenService irefreshTokenService,RedisTemplate<String,Object> redisTemplate,IemailService iemailService,IRoleNotificationProducer roleNotificationProducer  ) {
        this.loginRepo = loginRepo;
        this.rolesRepo = rolesRepo;
        this.iotpService = iotpService;
        this.jwtService = jwtService;
        this.irefreshTokenService=irefreshTokenService;
        this.redisTemplate = redisTemplate;
        this.iemailService=iemailService;
        this.roleNotificationProducer = roleNotificationProducer;
    }

    @Override
    public String RegisterUser(RegisterUser registerUser) {
        if (loginRepo.existsByEmail(registerUser.getEmail())) {
            throw new RuntimeException("Email is Already Registered");
        }
        boolean isValid = iotpService.validateOtp(registerUser.getEmail(), registerUser.getOtp());
        if (!isValid) {
            throw new RuntimeException("Invalid or Expired OTP");
        }
        Roles role = rolesRepo.findByRoleName(registerUser.getRole()).orElseThrow(() -> new RuntimeException("Invalid Role" + registerUser.getRole()));
        if(!Boolean.TRUE.equals(role.getRoleStatus())){
            throw new RuntimeException("Invalid Role Status please contact admin");
        }
        LmsLogin register = new LmsLogin();
        register.setEmail(registerUser.getEmail());
        register.setFirstName(registerUser.getFirstName());
        register.setLastName(registerUser.getLastName());
        register.setRole(role);
        String responseMessage;
        switch (role.getRoleName()){
            case "STUDENT"->{
               register.setIsAuthorized(true);
               register.setStatus("APPROVED");
                responseMessage = "Registration successful. You can login now.";
            }
            case "PUBLISHER"->{
                register.setIsAuthorized(false);
                register.setStatus("PENDING");
                notifyAdmins(register);
                responseMessage =
                        "Your request has been sent to Admins. You can login once it is approved.";

            }
            case "ADMIN" -> {
                register.setIsAuthorized(false);
                register.setStatus("PENDING");
                notifySuperAdmin(register);
                responseMessage =
                        "Your request has been sent to Super Admin. You can login once it is approved.";
            }
            case "SUPER_ADMIN" -> {
                
                if (loginRepo.existsByRole_RoleName("SUPER_ADMIN")) {
                    throw new RuntimeException("SUPER_ADMIN already exists");
                }
                register.setIsAuthorized(true);
                register.setStatus("APPROVED");
                responseMessage = "Super Admin registered successfully.";
            }
            default -> {
                register.setIsAuthorized(false);
                register.setStatus("PENDING");
                notifyAdmins(register);
                responseMessage="Your request has been sent to  Admins. You can login once it is approved.";
            }

        }

        try {
            loginRepo.save(register);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("SUPER_ADMIN already exists");
        }

        return responseMessage;

    }
    @Override
    public LoginResponse login(LoginRequest loginResponse, HttpServletResponse response) {
        LmsLogin user = loginRepo
                .findByEmailWithRoleAndPermissions(loginResponse.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!Boolean.TRUE.equals(user.getRole().getRoleStatus())){
            throw new RuntimeException("Your roles status is inactive please try again");
        }
        boolean validateOtp = iotpService.validateLoginOtp(loginResponse.getEmail(), loginResponse.getOtp());
        if (!validateOtp) {
            Long attempts = redisTemplate.opsForValue().increment("otp-fail:"+loginResponse.getEmail());
            if (attempts!=null&& attempts==1){
                redisTemplate.expire("otp-fail:"+loginResponse.getEmail(),5, TimeUnit.MINUTES);
            }


            if (attempts!=null&& attempts>=MAX_ATTEMPTS){
              iemailService.sendSuspeciousEmail(loginResponse.getEmail());
                redisTemplate.delete("otp-fail:"+loginResponse.getEmail());
                throw new RuntimeException(
                        "Too many wrong OTP attempts. Suspicious login detected."
                );
            }
            throw new RuntimeException("Invalid or Expired Otp");
        }
        if (!Boolean.TRUE.equals(user.getIsAuthorized())) {
            throw new RuntimeException("Account not approved yet");
        }

        redisTemplate.delete("otp-fail:"+loginResponse.getEmail());

        Set<String> roles = Set.of(user.getRole().getRoleName());
        Set<String> permissions = user.getRole()
                .getPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                roles,
                permissions,
                user.getTokenVersion(),
                user.getRole().getRoleVersion()
        );

        String refreshToken = irefreshTokenService.createRefreshToken(
                user.getEmail(),
                user.getTokenVersion()
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/spring/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader("Set-Cookie",cookie.toString());

        //  Return response
        return new LoginResponse(accessToken,user.getRole().getRoleName());



    }

    @Override
    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        RefreshTokenData data = irefreshTokenService.validate(refreshToken);
        LmsLogin user = loginRepo.findByEmailWithRoleAndPermissions(data.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        if(!Boolean.TRUE.equals(user.getRole().getRoleStatus())){
            throw new RuntimeException("Your roles status is inactive please try again");
        }
        if(data.getTokenVersion() != user.getTokenVersion()){
            throw new RuntimeException("Refresh token invalidated");
        }
        irefreshTokenService.revoke(refreshToken);
        String newRefreshToken = irefreshTokenService.createRefreshToken(user.getEmail(), user.getTokenVersion());
        Set<String> roles = Set.of(user.getRole().getRoleName());
        Set<String> permissions = user.getRole()
                .getPermissions()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), roles,permissions, user.getTokenVersion(),user.getRole().getRoleVersion());

        ResponseCookie  cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/spring/refresh")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader("Set-Cookie",cookie.toString());
        return new LoginResponse(newAccessToken,user.getRole().getRoleName());

    }

    @Override
    public String Logout(String authHeader, HttpServletResponse response) {
           if(authHeader==null|| !authHeader.startsWith("Bearer ")){
               throw new RuntimeException("Invalid token");
           }
           String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);
        String email = claims.getSubject();
        loginRepo.incrementTokenVersion(email);
        redisTemplate.delete("user:"+email);

           ResponseCookie cookie = ResponseCookie.from("refreshToken","")
                   .httpOnly(true)
                   .secure(true)
                   .path("/api/spring/refresh")
                   .maxAge(0)
                   .sameSite("None")
                   .build();
           response.addHeader("Set-Cookie",cookie.toString());


       return "you are Logged out successfully";
    }


    @PreAuthorize("hasAuthority('LOGIN')")
    public String testing(){
        return "welcome you are authenticated";
    }


    private void notifyAdmins(LmsLogin user) {
        if ("ADMIN".equals(user.getRole().getRoleName())) {
            return;
        }
        RoleNotificationDTO roleNotificationDTO = new RoleNotificationDTO();
        roleNotificationDTO.setEmail(user.getEmail());
        roleNotificationDTO.setRole("ADMIN");
        roleNotificationDTO.setMessage(user.getEmail() + " is requesting approval for " +user.getRole().getRoleName()+ " role");
     roleNotificationProducer.publish(roleNotificationDTO);

    }

    private void notifySuperAdmin(LmsLogin user) {
        RoleNotificationDTO roleNotificationDTO = new RoleNotificationDTO();
        roleNotificationDTO.setEmail(user.getEmail());
        roleNotificationDTO.setRole("SUPER_ADMIN");
        roleNotificationDTO.setMessage(user.getEmail() + " is requesting approval for  " +user.getRole().getRoleName()+ " role");
        roleNotificationProducer.publish(roleNotificationDTO);
    }

    public String sendLoginOtp(String email){
        if (!loginRepo.existsByEmail(email)) {
            throw new RuntimeException("Email not registered. Please sign up first.");
        }

        iemailService.sendLoginEmail(email); // async
        return "OTP sent successfully";
    }
}

