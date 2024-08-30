package com.example.ordersystem.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResetRqDto {
    private String email;
    private String password;
    @Size(min = 8, message = "password는 8자 이상이어야 합니다.")
    private String newPassword;
}
