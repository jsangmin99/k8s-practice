package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.OrderStatus;
import com.example.ordersystem.ordering.dto.OrderListRsDto;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderSaveRqDto;
import com.example.ordersystem.ordering.dto.StockDecreaseEvent;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.regions.servicemetadata.ApiPricingServiceMetadata;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderingRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

//Syncronize를 설정한다 하더라도, 재고감소 DB에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점이다.
    @Transactional
    public Ordering registerOrder(List<OrderSaveRqDto> request) {
//        방법 2 Jpa의 persist를 이용한 방법
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .status(OrderStatus.ORDERED)
                .build();

        for (OrderSaveRqDto orderDto : request) {
            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("해당 상품은 없습니다."));
            int quantity = orderDto.getProductCount();
//            redis를 이용한 재고 관리 및 재고 잔량 확인
            if (product.getName().contains("sale")){
//                redis 를 통한 재고관리 및 재고 잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(product.getId(), quantity).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
//                rdb에 재고를 업데이트. rabbitmq를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), orderDto.getProductCount()));;

            }else {
                if(product.getStockQuantity() < quantity){
                    throw new IllegalArgumentException("재고가 부족합니다.");
                }
                product.removeStockQuantity(quantity);
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderRepository.save(ordering);
        sseController.publishMessage(convertToDto(savedOrdering), "admin@test.com");

        return orderRepository.save(ordering);
    }


    public Page<OrderListRsDto> getAllOrderList(Pageable pageable) {
        Page<Ordering> orderList = orderRepository.findAll(pageable);
        return orderList.map(this::convertToDto);
    }

    private OrderListRsDto convertToDto(Ordering ordering) {
        return OrderListRsDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getStatus())
                .orderDetailDtos (ordering.getOrderDetails().stream()
                        .map(detail -> OrderListRsDto.OrderDetailDto.builder()
                                .id(detail.getProduct().getId())
                                .productName(detail.getProduct().getName())
                                .productCount(detail.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public Page<OrderListRsDto> getMyOrderList(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
        Page<Ordering> orderList = orderRepository.findByMember(member, pageable);
        return orderList.map(this::convertToDto);
    }

    @Transactional
    public Ordering cancelOrder(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
        Ordering ordering = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 주문은 없습니다."));
        if(ordering.getStatus() == OrderStatus.CANCELED){
            throw new IllegalArgumentException("이미 취소된 주문입니다.");
        }
        ordering.updateStatus(OrderStatus.CANCELED);
        return ordering;
    }

//    @Transactional
//    public Ordering registerOrder(List<OrderSaveRqDto> request) {
////        방법 1
////        order 생성
//        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(() -> new EntityNotFoundException("해당 유저는 없습니다."));
//        Ordering ordering = orderRepository.save(request.toEntity(member));
//
////        orderDetail 생성
//        for (OrderSaveRqDto.OrderDto orderDto : request.getOrderDtoList()) {
//            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("해당 상품은 없습니다."));
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//        return ordering;
//    }
}
