package com.chatirumae.chatirumae.core.repository;

import com.chatirumae.chatirumae.core.model.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
    
    // 사용자 ID로 채팅 히스토리 조회 (최신순)
    List<ChatHistory> findByUserIdOrderByUpdatedAtDesc(String userId);
    
    // 사용자 ID와 채팅 ID로 특정 채팅 히스토리 조회
    Optional<ChatHistory> findByIdAndUserId(String id, String userId);
    
    // 사용자 ID로 채팅 히스토리 개수 조회
    long countByUserId(String userId);
    
    // 사용자 ID와 제목으로 채팅 히스토리 검색
    List<ChatHistory> findByUserIdAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String userId, String title);
}
