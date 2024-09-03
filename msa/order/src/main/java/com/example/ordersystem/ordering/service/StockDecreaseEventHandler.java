//package com.example.ordersystem.ordering.service;
//
//import com.example.ordersystem.common.configs.RabbitMqConfig;
//import com.example.ordersystem.ordering.dto.StockDecreaseEvent;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class StockDecreaseEventHandler {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
////    @Autowired
////    private ProductRepository productRepository;
//
//
//    public void publish(StockDecreaseEvent stockDecreaseEvent){
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, stockDecreaseEvent);
//    }
//
////    트랜젝션이 완료된 이후에 그다음 메시지를 수신하므로 , 동시성 문제가 발생하지 않는다.
//    @Transactional
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen(Message message){
//        String messageBody = new String(message.getBody());
//        System.out.println(messageBody);
////        json메시지를 ObjecctMapper를 parsing
//        ObjectMapper objectMapper = new ObjectMapper();
//        StockDecreaseEvent stockDecreaseEvent = null;
//        try {
//            stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
//            //        배고 update
////            Product product = productRepository.findById(stockDecreaseEvent.getProductId()).orElseThrow(() -> new RuntimeException("해당 상품이 없습니다."));
////            product.removeStockQuantity(stockDecreaseEvent.getProductCount());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }
//
//}
