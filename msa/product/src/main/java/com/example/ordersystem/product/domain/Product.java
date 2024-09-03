package com.example.ordersystem.product.domain;

import com.example.ordersystem.product.dto.ProductListRsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;

    private LocalDateTime localDateTime;


    public ProductListRsDto fromEntity() {
        return ProductListRsDto.builder()
                .id(id)
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .image_path(imagePath)
                .category(category)
                .build();
    }

    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void removeStockQuantity(int orderCount) {
        this.stockQuantity -= orderCount;
    }


}
