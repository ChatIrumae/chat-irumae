package com.chatirumae.chatirumae.core.repository;

import com.chatirumae.chatirumae.core.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    
    // 채팅 ID로 메시지 조회 (시간순)
    List<Message> findByCurrentChatIdOrderByTimestampAsc(String currentChatId);
    
    
    // 채팅 ID로 메시지 조회 (정렬 없음)
    List<Message> findByCurrentChatId(String currentChatId);
}
