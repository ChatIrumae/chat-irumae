package com.chatirumae.chatirumae.core.service;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HealthCheckService {
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private WebClient chromaWebClient;
    
    public boolean checkChromaDbHealth() {
        try {
            System.out.println("ChromaDB 연결 상태를 확인합니다...");
            
            // ChromaDB 서버 상태 확인
            String response = chromaWebClient.get()
                    .uri("/api/v1/heartbeat")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .block();
            
            System.out.println("ChromaDB 응답: " + response);
            return response != null && !response.isEmpty();
            
        } catch (Exception e) {
            System.err.println("ChromaDB 연결 확인 실패: " + e.getMessage());
            return false;
        }
    }
    
    public boolean checkVectorStoreHealth() {
        try {
            System.out.println("VectorStore 상태를 확인합니다...");
            
            if (vectorStore == null) {
                System.err.println("VectorStore가 null입니다.");
                return false;
            }
            
            // 간단한 검색 테스트
            vectorStore.similaritySearch("test");
            System.out.println("VectorStore가 정상적으로 작동합니다.");
            return true;
            
        } catch (Exception e) {
            System.err.println("VectorStore 상태 확인 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public String getSystemStatus() {
        StringBuilder status = new StringBuilder();
        
        status.append("=== 시스템 상태 ===\n");
        status.append("ChromaDB 연결: ").append(checkChromaDbHealth() ? "정상" : "오류").append("\n");
        status.append("VectorStore 상태: ").append(checkVectorStoreHealth() ? "정상" : "오류").append("\n");
        
        return status.toString();
    }
}
