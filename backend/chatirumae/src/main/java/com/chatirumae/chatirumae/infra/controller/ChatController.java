package com.chatirumae.chatirumae.infra.controller;


import com.chatirumae.chatirumae.core.service.ChatService;
import com.chatirumae.chatirumae.core.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String userMessage = requestDto.getMessage();
        System.out.println("사용자 메시지: " + userMessage);

        // 주입받은 ChatService를 사용하여 비즈니스 로직 처리
        String responseMessage = chatService.getResponse(userMessage);

        return responseMessage; // 실제 응답 반환
    }
    
    @GetMapping("/health")
    public String health() {
        return healthCheckService.getSystemStatus();
    }
}

class ChatRequestDto {
    private String message;

    // Jackson 라이브러리가 JSON을 객체로 변환하려면
    // 기본 생성자와 Getter/Setter가 필요합니다.
    public ChatRequestDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}