package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.dto.CommonResDto;
import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.OrderStatus;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.*;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderingRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final SseController sseController;
    private final RestTemplate restTemplate;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final ProductFeign productFeign;
//    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Ordering orderRestTemplateCreate(List<OrderSaveRqDto> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .status(OrderStatus.ORDERED)
                .build();

        for (OrderSaveRqDto orderDto : request) {
            int quantity = orderDto.getProductCount();
//            product의 API의 요청을 통해 product 정보를 가져온다.
            String productGetUrl = "http://product-service/product/" + orderDto.getProductId();
//            토큰떄문에 헤더에 토큰세팅
            HttpHeaders httpHeaders = new HttpHeaders();
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            httpHeaders.set("Authorization",token);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<CommonResDto> productEntity = restTemplate.exchange(productGetUrl, HttpMethod.GET, entity, CommonResDto.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(productEntity.getBody().getResult(), ProductDto.class);
            System.out.println(productDto);
            if (productDto.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), quantity).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
//                rdb에 재고를 업데이트. rabbitmq를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), orderDto.getProductCount()));;

            }else {
                if(productDto.getStockQuantity() < quantity){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
//                resttemplate을 통해 update 요청을 보낸다.
                String productUpdateUrl = "http://product-service/product/updatestock";
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(new ProductUpdateStockDto(productDto.getId(), quantity), httpHeaders);
                restTemplate.exchange(productUpdateUrl, HttpMethod.PUT, updateEntity, Void.class);
//                productDto.removeStockQuantity(quantity); 이게 안됨
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderRepository.save(ordering);
        sseController.publishMessage(convertToDto(savedOrdering), "admin@test.com");
        return orderRepository.save(ordering);
    }

    @Transactional
    public Ordering orderFeignClientCreate(List<OrderSaveRqDto> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .status(OrderStatus.ORDERED)
                .build();

        for (OrderSaveRqDto orderDto : request) {
            int quantity = orderDto.getProductCount();
//            product의 API의 요청을 통해 product 정보를 가져온다.
            CommonResDto commonResDto = productFeign.getProductById(orderDto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(),ProductDto.class);

//            ResponseEntity 가 기본 응답값이므로 바로 CommonResDto로 변환한다.
            if (productDto.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), quantity).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), orderDto.getProductCount()));;

            }else {
                if(productDto.getStockQuantity() < quantity){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
                productFeign.updateStock(new ProductUpdateStockDto(productDto.getId(), quantity));
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderRepository.save(ordering);
        sseController.publishMessage(convertToDto(savedOrdering), "admin@test.com");
        return orderRepository.save(ordering);
    }
//    @Transactional
//    public Ordering orderFeignKafkaCreate(List<OrderSaveRqDto> request) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        Ordering ordering = Ordering.builder()
//                .memberEmail(email)
//                .status(OrderStatus.ORDERED)
//                .build();
//
//        for (OrderSaveRqDto orderDto : request) {
//            int quantity = orderDto.getProductCount();
////            product의 API의 요청을 통해 product 정보를 가져온다.
//            CommonResDto commonResDto = productFeign.getProductById(orderDto.getProductId());
//            ObjectMapper objectMapper = new ObjectMapper();
//            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(),ProductDto.class);
//
////            ResponseEntity 가 기본 응답값이므로 바로 CommonResDto로 변환한다.
//            if (productDto.getName().contains("sale")){
//                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), quantity).intValue();
//                if(newQuantity < 0){
//                    throw new IllegalArgumentException("재고가 부족합니다.");
//                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), orderDto.getProductCount()));;
//
//            }else {
//                if(productDto.getStockQuantity() < quantity){
//                    throw new IllegalArgumentException("재고가 부족합니다.");
//                }
//                ProductUpdateStockDto updateStockDto = new ProductUpdateStockDto(productDto.getId(), quantity);
//                kafkaTemplate.send("product-update-topic", updateStockDto);
//            }
//
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .productId(productDto.getId())
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        Ordering savedOrdering = orderRepository.save(ordering);
//        sseController.publishMessage(convertToDto(savedOrdering), "admin@test.com");
//        return orderRepository.save(ordering);
//    }

    public Page<OrderListRsDto> getAllOrderList(Pageable pageable) {
        Page<Ordering> orderList = orderRepository.findAll(pageable);
        return orderList.map(this::convertToDto);
    }

    private OrderListRsDto convertToDto(Ordering ordering) {
        return OrderListRsDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMemberEmail())
                .orderStatus(ordering.getStatus())
                .orderDetailDtos (ordering.getOrderDetails().stream()
                        .map(detail -> OrderListRsDto.OrderDetailDto.builder()
                                .id(detail.getProductId())
//                                .productName(detail.getProduct().getName())
                                .productCount(detail.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public Page<OrderListRsDto> getMyOrderList(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<Ordering> orderList = orderRepository.findByMemberEmail(email, pageable);
        return orderList.map(this::convertToDto);
    }

    @Transactional
    public Ordering cancelOrder(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering ordering = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 주문은 없습니다."));
        if(ordering.getStatus() == OrderStatus.CANCELED){
            throw new IllegalArgumentException("이미 취소된 주문입니다.");
        }
        ordering.updateStatus(OrderStatus.CANCELED);
        return ordering;
    }
}
