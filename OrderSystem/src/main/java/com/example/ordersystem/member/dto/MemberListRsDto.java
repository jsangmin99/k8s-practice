package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Address;
import com.example.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberListRsDto {
    private Long id;
    private String name;
    private String email;
    private Address address;
    private int orderCount;


    public MemberListRsDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.address = member.getAddress();

    }

    public static MemberListRsDto fromEntity(Member findMember) {
        return MemberListRsDto.builder()
                .id(findMember.getId())
                .name(findMember.getName())
                .email(findMember.getEmail())
                .address(findMember.getAddress())
                .build();
    }
}
