package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.common.dto.CommonResDto;
import com.example.ordersystem.ordering.dto.OrderListRsDto;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderSaveRqDto;
import com.example.ordersystem.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/order/create")
    public ResponseEntity<?> createOrder(@RequestBody List<OrderSaveRqDto> request) {
        Ordering ordering = orderService.registerOrder(request);
        CommonResDto response = new CommonResDto(HttpStatus.CREATED,"Success", ordering.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/list")
    public ResponseEntity<?> getOrderList(@PageableDefault Pageable pageable) {
        Page<OrderListRsDto> orderListRsDtos = orderService.getAllOrderList(pageable);
        CommonResDto response = new CommonResDto(HttpStatus.OK, "Success", orderListRsDtos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    내주문만 볼수있는 myOrders : order/myorders
    @GetMapping("/order/myorders")
    public ResponseEntity<?> getMyOrders(@PageableDefault Pageable pageable) {
        Page<OrderListRsDto> orderListRsDtos = orderService.getMyOrderList(pageable);
        CommonResDto response = new CommonResDto(HttpStatus.OK, "Success", orderListRsDtos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    admin사용자가 주문 취소 orderstatus만 변경
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Ordering ordering =  orderService.cancelOrder(id);
        CommonResDto response = new CommonResDto(HttpStatus.OK, "Success", ordering);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
