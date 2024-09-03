package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Address;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateRqDto {
    private String name;
    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    private String email;
    @Size(min = 8, message = "password는 8자 이상이어야 합니다.")
    private String password;
    private Address address;
    private Role role = Role.USER;

    public Member toEntity(String password) {
        return Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(this.role)
                .address(address)
                .build();
    }
}
