package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.model.ChatHistory;
import com.chatirumae.chatirumae.core.model.Message;
import com.chatirumae.chatirumae.core.repository.ChatHistoryRepository;
import com.chatirumae.chatirumae.core.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;
    private final MessageRepository messageRepository;

    public ChatHistoryService(ChatHistoryRepository chatHistoryRepository, MessageRepository messageRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.messageRepository = messageRepository;
    }

    // 사용자의 모든 채팅 히스토리 조회
    public List<ChatHistory> getChatHistoriesByUserId(String userId) {
        return chatHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    // 특정 채팅 히스토리 조회
    public Optional<ChatHistory> getChatHistoryById(String chatId, String userId) {
        return chatHistoryRepository.findByIdAndUserId(chatId, userId);
    }

    // 새 채팅 히스토리 생성
    public ChatHistory createChatHistory(String userId, String title) {
        String chatId = "chat_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();
        
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(chatId);
        chatHistory.setTitle(title);
        chatHistory.setUserId(userId);
        chatHistory.setCreatedAt(now);
        chatHistory.setUpdatedAt(now);
        chatHistory.setMessages(List.of()); // 빈 메시지 리스트로 시작
        
        return chatHistoryRepository.save(chatHistory);
    }

    // 채팅 히스토리 업데이트 (메시지 추가)
    public ChatHistory updateChatHistory(String chatId, String userId, List<Message> messages) {
        Optional<ChatHistory> optionalChatHistory = chatHistoryRepository.findByIdAndUserId(chatId, userId);
        
        if (optionalChatHistory.isPresent()) {
            ChatHistory chatHistory = optionalChatHistory.get();
            chatHistory.setMessages(messages);
            chatHistory.setUpdatedAt(LocalDateTime.now());
            
            // 제목이 없거나 기본값인 경우 첫 번째 메시지로 제목 설정
            if (chatHistory.getTitle() == null || chatHistory.getTitle().isEmpty() || 
                chatHistory.getTitle().equals("새 채팅")) {
                String firstUserMessage = messages.stream()
                    .filter(msg -> !"assistant".equals(msg.getSender()))
                    .findFirst()
                    .map(Message::getContent)
                    .orElse("새 채팅");
                
                String title = firstUserMessage.length() > 30 
                    ? firstUserMessage.substring(0, 30) + "..." 
                    : firstUserMessage;
                chatHistory.setTitle(title);
            }
            
            return chatHistoryRepository.save(chatHistory);
        }
        
        throw new IllegalArgumentException("채팅 히스토리를 찾을 수 없습니다: " + chatId);
    }

    // 메시지 저장
    public Message saveMessage(String content, String sender, String currentChatId) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now());
        message.setCurrentChatId(currentChatId);
        
        return messageRepository.save(message);
    }

    // currentChatId로 ChatHistory 찾아서 메시지 추가 (없으면 새로 생성)
    public ChatHistory addMessageToChatHistory(String currentChatId, String sender, String content, String messageSender) {
        System.out.println("=== addMessageToChatHistory 시작 ===");
        System.out.println("currentChatId: " + currentChatId);
        System.out.println("sender: " + sender);
        System.out.println("content: " + content);
        System.out.println("messageSender: " + messageSender);
        
        // 먼저 currentChatId로 ChatHistory 찾기
        Optional<ChatHistory> existingChat = chatHistoryRepository.findById(currentChatId);
        System.out.println("기존 ChatHistory 존재 여부: " + existingChat.isPresent());

        ChatHistory chatHistory;
        if (existingChat.isPresent()) {
            // 기존 채팅 히스토리가 있으면 메시지 추가
            chatHistory = existingChat.get();
            System.out.println("기존 ChatHistory ID: " + chatHistory.getId());
            System.out.println("기존 ChatHistory userId: " + chatHistory.getUserId());
            
            // 사용자 ID 확인 (보안) - sender를 userId로 사용
            if (!sender.equals(chatHistory.getUserId())) {
                throw new IllegalArgumentException("해당 채팅에 접근할 권한이 없습니다.");
            }
        } else {
            // 기존 채팅 히스토리가 없으면 새로 생성
            System.out.println("새 ChatHistory 생성 중...");
            String title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
            chatHistory = new ChatHistory();
            chatHistory.setId(currentChatId); // 프론트엔드에서 생성한 ID 사용
            chatHistory.setTitle(title);
            chatHistory.setUserId(sender); // sender를 userId로 사용
            chatHistory.setCreatedAt(LocalDateTime.now());
            chatHistory.setUpdatedAt(LocalDateTime.now());
            chatHistory.setMessages(new ArrayList<>()); // 빈 메시지 리스트로 시작
            System.out.println("새 ChatHistory 생성 완료 - ID: " + chatHistory.getId());
        }
        
        // 메시지 생성 및 저장
        Message message = saveMessage(content, messageSender, currentChatId);

        System.out.println(message);

        // ChatHistory의 메시지 리스트에 추가
        List<Message> messages = chatHistory.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
        chatHistory.setMessages(messages);
        chatHistory.setUpdatedAt(LocalDateTime.now());
        
        // 제목이 기본값이거나 비어있으면 첫 번째 사용자 메시지로 설정
        if (chatHistory.getTitle() == null || chatHistory.getTitle().isEmpty() || 
            chatHistory.getTitle().equals("새 채팅")) {
            String firstUserMessage = messages.stream()
                .filter(msg -> !"assistant".equals(msg.getSender()))
                .findFirst()
                .map(Message::getContent)
                .orElse("새 채팅");
            
            String title = firstUserMessage.length() > 30 
                ? firstUserMessage.substring(0, 30) + "..." 
                : firstUserMessage;
            chatHistory.setTitle(title);
        }
        
        ChatHistory savedChatHistory = chatHistoryRepository.save(chatHistory);
        System.out.println("ChatHistory 저장 완료 - ID: " + savedChatHistory.getId());
        System.out.println("=== addMessageToChatHistory 종료 ===");
        return savedChatHistory;
    }

    // 채팅 히스토리 삭제
    public void deleteChatHistory(String chatId, String userId) {
        Optional<ChatHistory> optionalChatHistory = chatHistoryRepository.findByIdAndUserId(chatId, userId);
        
        if (optionalChatHistory.isPresent()) {
            // 관련 메시지들도 삭제
            List<Message> messages = messageRepository.findByCurrentChatId(chatId);
            messageRepository.deleteAll(messages);
            
            // 채팅 히스토리 삭제
            chatHistoryRepository.deleteById(chatId);
        } else {
            throw new IllegalArgumentException("채팅 히스토리를 찾을 수 없습니다: " + chatId);
        }
    }

    // 채팅 히스토리 제목 업데이트
    public ChatHistory updateChatHistoryTitle(String chatId, String userId, String newTitle) {
        Optional<ChatHistory> optionalChatHistory = chatHistoryRepository.findByIdAndUserId(chatId, userId);
        
        if (optionalChatHistory.isPresent()) {
            ChatHistory chatHistory = optionalChatHistory.get();
            chatHistory.setTitle(newTitle);
            chatHistory.setUpdatedAt(LocalDateTime.now());
            return chatHistoryRepository.save(chatHistory);
        }
        
        throw new IllegalArgumentException("채팅 히스토리를 찾을 수 없습니다: " + chatId);
    }

    // 사용자의 채팅 히스토리 검색
    public List<ChatHistory> searchChatHistories(String userId, String searchTerm) {
        return chatHistoryRepository.findByUserIdAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(userId, searchTerm);
    }
}
