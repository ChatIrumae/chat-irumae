package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.interfaces.GptApi;
import com.chatirumae.chatirumae.infra.ChatGptApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public String getResponse(String userMessage) {
        try {
            System.out.println("사용자 메시지: " + userMessage);
            
            // VectorStore 검색 시도
            try {
                System.out.println("VectorStore에서 유사한 문서를 검색합니다...");
                System.out.println("VectorStore 타입: " + vectorStore.getClass().getSimpleName());
                System.out.println("사용자 메시지 길이: " + userMessage.length());
                
                List<Document> similarDocuments = vectorStore.similaritySearch(userMessage);
                
                if (similarDocuments != null && !similarDocuments.isEmpty()) {
                    System.out.println("검색된 문서 수: " + similarDocuments.size());
                    
                    // 각 문서의 내용(content)만 추출하여 하나의 문자열로 합칩니다.
                    String context = similarDocuments.stream()
                            .map(Document::getContent)
                            .collect(Collectors.joining("\n---\n"));
                    
                    System.out.println("Context for GPT: " + context);

                    return "테스트중";
                    // 컨텍스트와 함께 GPT API 호출
//                    return gptApi.generateResponse(userMessage, List.of(Collections.singletonList(context))).block();
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
            return gptApi.generateResponse(userMessage, null).block();
            
        } catch (Exception e) {
            System.err.println("Error in ChatService.getResponse: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 현재 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}
