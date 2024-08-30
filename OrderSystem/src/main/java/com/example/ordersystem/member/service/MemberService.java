package com.example.ordersystem.member.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.MemberCreateRqDto;
import com.example.ordersystem.member.dto.MemberListRsDto;
import com.example.ordersystem.member.dto.MemberLoginDto;
import com.example.ordersystem.member.dto.MemberResetRqDto;
import com.example.ordersystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;


    @Transactional
    public Member createMember(MemberCreateRqDto request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
        memberRepository.save(member);
        return member;
    }

    public Page<MemberListRsDto> getMemberList(Pageable pageable) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
//        System.out.println(member);
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(a -> a.fromEntity());
    }


    @Transactional
    public Member login(MemberLoginDto request) {
//        email 존재여부 확인
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
//        password 일치여부 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public MemberListRsDto getMyInfo(Member member) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member findMember = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
        return MemberListRsDto.fromEntity(findMember);
    }

    @Transactional
    public void resetPassword(MemberResetRqDto request) {
        String email = request.getEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
