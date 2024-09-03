package com.example.ordersystem.ordering.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberEmail;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

//    cascade = CascadeType.PERSIST 를 사용하여 Ordering 엔티티를 저장할 때 연관된 OrderDetail 엔티티도 함께 저장
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();


    public void updateStatus(OrderStatus canceled) {
        this.status = canceled;
    }

}
