package com.example.ordersystem.common.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

   @Bean
   public RequestInterceptor requestInterceptor() {
       return request -> {
//           모든 Feign 요청에 대해 SecurityContextHolder에서 인증 정보를 가져와 헤더에 추가
           String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
           request.header(HttpHeaders.AUTHORIZATION, token);
       };
   }
}


