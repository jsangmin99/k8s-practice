package com.example.ordersystem.product.controller;

import com.example.ordersystem.common.dto.CommonResDto;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductCreateRqDto;
import com.example.ordersystem.product.dto.ProductListRsDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/product/create")
    public ResponseEntity<?> createProduct(ProductCreateRqDto request) {
        Product product =  productService.createAwsProduct(request);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED,"Item is successfully created", product);
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/product/list")
    public ResponseEntity<?> getProductList( ProductSearchDto productSearchDto, @PageableDefault Pageable pageable) {
        Page<ProductListRsDto> product = productService.productList(productSearchDto,pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK ,"Success", product);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
    @GetMapping("/product/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id) {
        ProductListRsDto dto = productService.productDetail(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Success", dto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PutMapping("/product/updatestock")
    public ResponseEntity<?>stockUpdate(@RequestBody ProductUpdateStockDto dto) {
        Product product = productService.productUpdateStock(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "Product Update is Success", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


}
