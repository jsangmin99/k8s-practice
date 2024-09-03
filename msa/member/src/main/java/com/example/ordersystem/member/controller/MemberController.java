package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.common.dto.CommonErrorDto;
import com.example.ordersystem.common.dto.CommonResDto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.*;
import com.example.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    @Value("${jwt.secretKeyRt}")
    private String secretKey;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/member/create")
    public ResponseEntity<?> createMember(@Valid @RequestBody MemberCreateRqDto request) {
        Member member = memberService.createMember(request);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Success", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


//    admin만 접근 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<Object> getMemberList(@PageableDefault Pageable pageable){
        Page<MemberListRsDto> memberList = memberService.getMemberList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Success", memberList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    본인은 본인 회원정보만 조회가능
    @GetMapping("/member/myinfo")
    public ResponseEntity<Object> getMyInfo(@AuthenticationPrincipal Member member){
        MemberListRsDto memberListRsDto = memberService.getMyInfo(member);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Success", memberListRsDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto request){
//        email, password가일치하는지 검증
        log.info("111111");
        Member member = memberService.login(request);
//        일치할경우 AccessToken을 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

//        Redis에 eamil을 key로 refreshToken을 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); //240시간

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
//        생성된 토큰을 CommonResDto에 담아서 리턴
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Success", loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto){
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try {
//            코드를 통해 Rt 검증
            claims = Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            log.error("refresh token error");
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "Invalid RefreshToken"), HttpStatus.BAD_REQUEST);
        }
        String email = claims.getSubject();
        String role = (String) claims.get("role");

//        redis를 조회하여 Rt 추가검증
        Object obj = redisTemplate.opsForValue().get(email);
        if (obj == null || !obj.equals(rt)){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST, "Invalid RefreshToken"), HttpStatus.BAD_REQUEST);
        }

        String newAt = jwtTokenProvider.createToken(email, role);

        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);
//        생성된 토큰을 CommonResDto에 담아서 리턴
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "At is Renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/member/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody MemberResetRqDto request){
        memberService.resetPassword(request);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Password Renew Success", "OK");
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}


