package com.chatirumae.chatirumae.infra.controller;

import com.chatirumae.chatirumae.core.model.ChatHistory;
import com.chatirumae.chatirumae.core.model.Message;
import com.chatirumae.chatirumae.core.service.ChatHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat-history")
public class ChatHistoryController {
    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    // 사용자의 모든 채팅 히스토리 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatHistory>> getChatHistoriesByUserId(@PathVariable String userId) {
        List<ChatHistory> chatHistories = chatHistoryService.getChatHistoriesByUserId(userId);
        return ResponseEntity.ok(chatHistories);
    }

    // 특정 채팅 히스토리 조회
    @GetMapping("/{chatId}/user/{userId}")
    public ResponseEntity<ChatHistory> getChatHistoryById(@PathVariable String chatId, @PathVariable String userId) {
        Optional<ChatHistory> chatHistory = chatHistoryService.getChatHistoryById(chatId, userId);
        return chatHistory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 새 채팅 히스토리 생성
    @PostMapping("/user/{userId}")
    public ResponseEntity<ChatHistory> createChatHistory(@PathVariable String userId, @RequestBody CreateChatHistoryRequest request) {
        ChatHistory chatHistory = chatHistoryService.createChatHistory(userId, request.getTitle());
        return ResponseEntity.ok(chatHistory);
    }

    // 채팅 히스토리 업데이트 (메시지 추가)
    @PutMapping("/{chatId}/user/{userId}")
    public ResponseEntity<ChatHistory> updateChatHistory(@PathVariable String chatId, @PathVariable String userId, @RequestBody UpdateChatHistoryRequest request) {
        try {
            ChatHistory chatHistory = chatHistoryService.updateChatHistory(chatId, userId, request.getMessages());
            return ResponseEntity.ok(chatHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 메시지 저장
    @PostMapping("/message")
    public ResponseEntity<Message> saveMessage(@RequestBody SaveMessageRequest request) {
        Message message = chatHistoryService.saveMessage(
            request.getContent(),
            request.getSender(),
            request.getCurrentChatId()
        );
        return ResponseEntity.ok(message);
    }

    // 채팅 히스토리 삭제
    @DeleteMapping("/{chatId}/user/{userId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable String chatId, @PathVariable String userId) {
        try {
            chatHistoryService.deleteChatHistory(chatId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅 히스토리 제목 업데이트
    @PatchMapping("/{chatId}/user/{userId}/title")
    public ResponseEntity<ChatHistory> updateChatHistoryTitle(@PathVariable String chatId, @PathVariable String userId, @RequestBody UpdateTitleRequest request) {
        try {
            ChatHistory chatHistory = chatHistoryService.updateChatHistoryTitle(chatId, userId, request.getTitle());
            return ResponseEntity.ok(chatHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 채팅 히스토리 검색
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<ChatHistory>> searchChatHistories(@PathVariable String userId, @RequestParam String q) {
        List<ChatHistory> chatHistories = chatHistoryService.searchChatHistories(userId, q);
        return ResponseEntity.ok(chatHistories);
    }

    // DTO 클래스들
    public static class CreateChatHistoryRequest {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class UpdateChatHistoryRequest {
        private List<Message> messages;

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }

    public static class SaveMessageRequest {
        private String content;
        private String sender;
        private String currentChatId;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getCurrentChatId() {
            return currentChatId;
        }

        public void setCurrentChatId(String currentChatId) {
            this.currentChatId = currentChatId;
        }
    }

    public static class UpdateTitleRequest {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
