package com.quinzex.jwtFilter;

import com.quinzex.dto.UserCacheDTO;
import com.quinzex.entity.LmsLogin;
import com.quinzex.repository.LmsLoginRepo;
import com.quinzex.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final LmsLoginRepo loginRepo;
    private final RedisTemplate<String,Object> redisTemplate;

   public JwtFilter(JwtService jwtService, RedisTemplate<String,Object> redisTemplate, LmsLoginRepo loginRepo) {
       this.jwtService = jwtService;
       this.redisTemplate =redisTemplate;
       this.loginRepo = loginRepo;
   }


   @Override
   protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)throws ServletException, IOException {
       if(SecurityContextHolder.getContext().getAuthentication()!=null){
           filterChain.doFilter(request,response);
           return;
       }


       String authHeader = request.getHeader("Authorization");
      if(authHeader==null|| !authHeader.startsWith("Bearer ")){
       filterChain.doFilter(request,response);
       return;
      }


   String token = authHeader.substring(7);
   try {
       Claims claims = jwtService.parseToken(token);
       if(!"ACCESS".equals(claims.get("type"))){
           filterChain.doFilter(request,response);
           return;
       }
       String email = claims.getSubject();

       if (!claims.containsKey("tokenVersion") || !claims.containsKey("roleVersion")) {
           throw new RuntimeException("Invalid token structure");
       }

       Integer tokenVersion = claims.get("tokenVersion", Integer.class);
       Integer roleVersion  = claims.get("roleVersion", Integer.class);

       String cacheKey = "user:" + email;
       UserCacheDTO dto = (UserCacheDTO) redisTemplate.opsForValue().get(cacheKey);
       if (dto == null) {
           LmsLogin user = loginRepo.findByEmail(email)
                   .orElseThrow(() -> new RuntimeException("Unauthorized"));

           dto = new UserCacheDTO(
                   user.getEmail(),
                   user.getTokenVersion(),
                   user.getRole().getRoleVersion()
           );

           redisTemplate.opsForValue().set(cacheKey, dto, 10, TimeUnit.MINUTES);
       }
       if(!tokenVersion.equals(dto.getTokenVersion())){
           throw new RuntimeException("Unauthorized");
       }
       if(!roleVersion.equals(dto.getRoleVersion())){
           throw new RuntimeException("Unauthorized");
       }

       if (!claims.containsKey("permissions")) {
           throw new RuntimeException("Invalid token permissions");
       }

       @SuppressWarnings("unchecked")
       List<String> roles = claims.get("roles", List.class);
       @SuppressWarnings("unchecked")
        List<String> permissions = claims.get("permissions",List.class);

       List<SimpleGrantedAuthority> authorities = new ArrayList<>();
       roles.forEach(role->{authorities.add(new SimpleGrantedAuthority("ROLE_" + role));});
       permissions.forEach(permission->{authorities.add(new SimpleGrantedAuthority(permission));});
       UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email,null,authorities);
       usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
       SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

   } catch (Exception e) {
       SecurityContextHolder.clearContext();
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
       return;
   }
       filterChain.doFilter(request,response);
   }

}
