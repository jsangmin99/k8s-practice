package com.example.ordersystem.ordering.domain;

import com.example.ordersystem.ordering.dto.OrderListRsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;


    private Long productId;


    public OrderListRsDto.OrderDetailDto fromEntity(String productName) {
        return OrderListRsDto.OrderDetailDto.builder()
                .id(this.id)
                .productName(productName)
                .productCount(this.quantity)
                .build();
    }

}
