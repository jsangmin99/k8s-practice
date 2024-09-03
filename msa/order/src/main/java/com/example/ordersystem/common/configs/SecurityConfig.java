package com.example.ordersystem.common.configs;

import com.example.ordersystem.common.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 설정을 활성화
@EnableGlobalMethodSecurity(prePostEnabled = true) // pre: 사전검증, post: 사후검증
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf().disable() // csrf 비활성화
                .cors().and() // cors 활성화
                .httpBasic().disable() // httpBasic 비활성화
                .authorizeRequests()
                .antMatchers("/","/member/create", "/doLogin", "/refresh-token", "/product/list","/member/reset-password").permitAll()
                .anyRequest().authenticated() // 모든 요청에 대해 인증을 요구
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않겠다.
                .and()
//                로그인시 사용자는 서버로부터 서버로부터 발급 받고
//                매 요청마다 헤더에 토큰을 실어서 요청
//                아래 코드는 사용자로부터 받아온 토큰이 정상인지 아닌지를 검증하는 코드
//                사용자로부터 받아온 토큰이 정상인지 아닌지를 검증하는 코드
                // jwtAuthFilter를 UsernamePasswordAuthenticationFilter 앞에 넣겠다.
                // UsernamePasswordAuthenticationFilter는 이름 비밀번호로 인증하는 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }


}
