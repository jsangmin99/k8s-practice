package com.example.ordersystem.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductListRsDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer stockQuantity;
    private String category;
    private String image_path;
}
