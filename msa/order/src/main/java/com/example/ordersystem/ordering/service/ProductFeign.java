package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.configs.FeignConfig;
import com.example.ordersystem.common.dto.CommonResDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// url 설정을 추가하여 service 자원을 검색하도록 설정
// FeignConfig 설정을 추가하여 FeignClient의 설정을 추가
@FeignClient(name = "product-service", url = "http://product-service", configuration = FeignConfig.class)
public interface ProductFeign {

    @GetMapping(value = "/product/{id}")
    CommonResDto getProductById(@PathVariable("id") Long id);

    @PutMapping(value = "/product/updatestock")
    void updateStock(@RequestBody ProductUpdateStockDto dto);

}
