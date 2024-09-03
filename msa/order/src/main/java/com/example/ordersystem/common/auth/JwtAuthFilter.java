package com.example.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String bearerToken = ((HttpServletRequest) request).getHeader("Authorization");
        try {
            if (bearerToken != null) {
//            토큰은 관례적으로 Bearer로 시작하는 문구를 넣어서 요청
                if (!bearerToken.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다..");
                }
                String token = bearerToken.substring(7);
//            1. token 검증및 claims(사용자 정보) 추출
//            토큰 생성시에 사용한 시크릿 키값을 넣어 토큰 검증에 사용한다.
                Claims claims = Jwts.parser().setSigningKey(secretKey)
                        .parseClaimsJws(token).getBody();
//            2. Authentication 객체 생성 및 SecurityContext에 저장(UserDetails 객체 필요)
//            리스트 형태로 권한 목록을 받아서 SimpleGrantedAuthority 객체로 변환
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
//            UserDetails 객체 생성
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
//            Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, bearerToken, userDetails.getAuthorities());
//            SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);


            }
//        filterChain에서 다름 필터로 넘어가는 방법
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token 에러");
        }
    }
}
