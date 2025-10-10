package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.interfaces.GptApi;
import com.chatirumae.chatirumae.infra.ChatGptApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChatService {
    private final VectorStore vectorStore;
    private GptApi gptApi;

    @Value("${chromadb.collection.name}")
    private String collectionName;


    public ChatService(VectorStore vectorStore, ChatGptApi gptApi) {
        this.vectorStore = vectorStore;
        this.gptApi = gptApi;
    }

    public String getResponse(String userMessage, Date date, String currentChatId, String sender) {
        try {
            System.out.println("사용자 메시지: " + userMessage);
            System.out.println("사용자 메시지: " + date);
            System.out.println("사용자 메시지: " + currentChatId);
            System.out.println("사용자 메시지: " + sender);

            // VectorStore 검색 시도
            try {
                System.out.println("=== VectorStore 검색 시작 ===");
                System.out.println("VectorStore에서 유사한 문서를 검색합니다...");
                System.out.println("사용자 메시지: " + userMessage);
                System.out.println("사용자 메시지 길이: " + userMessage.length());

                // Embedding 테스트를 위한 간단한 검색
                System.out.println("Embedding을 통한 유사도 검색을 시작합니다...");
                List<Document> similarDocuments = vectorStore.similaritySearch(userMessage);
                
                if (similarDocuments != null && !similarDocuments.isEmpty()) {
                    System.out.println("검색된 문서 수: " + similarDocuments.size());
                    
                    // 각 문서의 내용(content)만 추출하여 하나의 문자열로 합칩니다.
                    String context = similarDocuments.stream().toString();

                    // 컨텍스트와 함께 GPT API 호출
//                    return gptApi.generateResponse(userMessage, List.of(context)).block();
                    return "테스트";
                } else {
                    System.out.println("유사한 문서를 찾지 못했습니다. 컨텍스트 없이 GPT API를 호출합니다.");
                }
            } catch (Exception vectorStoreError) {
                System.err.println("VectorStore 검색 중 오류 발생: " + vectorStoreError.getMessage());
                System.err.println("오류 타입: " + vectorStoreError.getClass().getSimpleName());
                System.err.println("오류 상세: " + vectorStoreError.getCause());
                vectorStoreError.printStackTrace();
                System.out.println("VectorStore 오류를 무시하고 직접 GPT API를 호출합니다.");
            }
            
            // VectorStore 검색 실패 시 또는 결과가 없을 때 컨텍스트 없이 GPT API 호출
            try {
                return "테스트";
            } catch (Exception gptError) {
                System.err.println("GPT API 호출 중 오류 발생: " + gptError.getMessage());
                gptError.printStackTrace();
                return "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
        } catch (Exception e) {
            System.err.println("Error in ChatService.getResponse: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 현재 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}
