package com.example.ordersystem.member.domain;

import com.example.ordersystem.common.domain.BaseEntity;
import com.example.ordersystem.member.dto.MemberListRsDto;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Entity
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<Ordering> orderings = new ArrayList<>();

    public MemberListRsDto fromEntity(){
        return MemberListRsDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .orderCount(this.orderings.size())
                .build();
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }
}
