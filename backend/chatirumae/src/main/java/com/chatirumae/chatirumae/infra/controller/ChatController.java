package com.chatirumae.chatirumae.infra.controller;

import com.chatirumae.chatirumae.core.model.ChatHistory;
import com.chatirumae.chatirumae.core.model.ChatHistorySummary;
import com.chatirumae.chatirumae.core.service.ChatService;
import com.chatirumae.chatirumae.core.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;
    private final HealthCheckService healthCheckService;

    public ChatController(final ChatService chatService, final HealthCheckService healthCheckService) {
        this.chatService = chatService;
        this.healthCheckService = healthCheckService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequestDto requestDto) {
        // DTO 객체를 통해 깔끔하게 메시지 내용에 접근 가능
        String userMessage = requestDto.getContent();
        Date timestamp = requestDto.getTimestamp();
        String currentChatId = requestDto.getCurrentChatId();
        String sender = requestDto.getSender();

        System.out.println("사용자 메시지: " + userMessage);
        System.out.println("발신자: " + sender);

        System.out.println("");
        System.out.println("CHAT START");
        String responseMessage = chatService.getResponse(userMessage, timestamp, currentChatId, sender);
        CompletableFuture.runAsync(() -> {
            chatService.predict(userMessage, responseMessage, timestamp, currentChatId, sender);
        });
    
        return responseMessage; // 실제 응답 반환
    }

    @GetMapping("/health")
    public String health() {
        return healthCheckService.getSystemStatus();
    }

    @GetMapping("/history/{userId}")
    public List<ChatHistorySummary> getHistory(@PathVariable String userId) {
        return chatService.getHistory(userId);
    }

    @GetMapping("/history/{userId}/{chatId}")
    public ChatHistory getChatHistory(@PathVariable String userId, @PathVariable String chatId) {
        return chatService.getChatHistory(chatId, userId);
    }
}

class ChatRequestDto {
    private String id;
    private String content;
    private String sender;
    private Date timestamp;
    private String currentChatId;

    // Jackson 라이브러리가 JSON을 객체로 변환하려면
    // 기본 생성자와 Getter/Setter가 필요합니다.
    public ChatRequestDto() {
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    public String getSender() {return sender;}
    public void setSender(String sender) {this.sender = sender;}
    public Date getTimestamp() {return timestamp;}
    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}
    public String getCurrentChatId() {return currentChatId;}
    public void setCurrentChatId(String currentChatId) {this.currentChatId = currentChatId;}
}