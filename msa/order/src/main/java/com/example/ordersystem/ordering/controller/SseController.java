package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dto.OrderListRsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class SseController implements MessageListener {
//    SseEmitter는 연결된 사용자 정보를 의미
//    ConcurrentHashMap은 Tread-safe 한 맵으로  동시성 문제를 해결하기 위해 사용
    private final Map<String, SseEmitter> emmiters = new ConcurrentHashMap<>();
//    여러번 구독을 방지하기 위한 ConcurrentHashSet 변수 생성
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();
    private final RedisMessageListenerContainer redisMessageListenerContainer;


    @Qualifier("4")
    private final RedisTemplate<String,Object> sseRedisTemplate;

//    email에 해당하는 메시지를 listen gksms litener를 등록
    public void subscribeChannel(String email){
//        이미 구독한 email이라면 구독 하지 않는 분기 처리
        if (!subscribeList.contains(email)) {
            MessageListenerAdapter messageListenerAdapter = messageListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }
    private MessageListenerAdapter messageListenerAdapter(SseController sseController){
        return new MessageListenerAdapter(sseController);
    }

    @GetMapping ("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(14400 * 1000L);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emmiters.put(email, emitter);
        emitter.onCompletion(() -> emmiters.remove(email));
        emitter.onTimeout(() -> emmiters.remove(email));

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
        try {
            emitter.send(SseEmitter.event().name("connected").data("connected!!!!!!"));
        } catch (IOException e) {
            e.printStackTrace();
            emitter.complete();
        }
        subscribeChannel(email);
        return emitter;
    }

    public void publishMessage(OrderListRsDto orderListRsDto, String email){
        SseEmitter emitter = emmiters.get(email);
//        단일서버에서 pub/sub을 확인하기 위해 주석처리함 만약 다중 서버 환경에서는 주석을 해제하고 사용
//        if(emitter != null){
//            try {
//                emitter.send(SseEmitter.event().name("ordered").data(orderListRsDto));
//            } catch (IOException e) {
//                emitter.complete();
//                throw new RuntimeException(e);
//            }
//        }
//        else {
        //       redis를 통한 메시지 전송
            sseRedisTemplate.convertAndSend(email, orderListRsDto);
//        }
    }


    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message내용 parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListRsDto dto = objectMapper.readValue(message.getBody(), OrderListRsDto.class);
            String email = new String(pattern, StandardCharsets.UTF_8);

            SseEmitter emitter = emmiters.get(email);
            if (emitter != null) {
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }
            System.out.println("listening!!!!!!!!!!!!!!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
