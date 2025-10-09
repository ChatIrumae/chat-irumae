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
        // 1. 유사한 문서를 검색합니다.
        List<Document> similarDocuments = vectorStore.similaritySearch(userMessage);

        // 2. 검색된 문서가 있는지 확인합니다.
        if (similarDocuments != null && !similarDocuments.isEmpty()) {
            // ⭐️ [수정된 부분] ⭐️
            // 각 문서의 내용(content)만 추출하여 하나의 문자열로 합칩니다.
            String context = similarDocuments.stream()
                    .map(Document::getContent) // 각 Document에서 getContent() 메서드로 내용만 가져옴
                    .collect(Collectors.joining("\n---\n")); // 각 문서 내용을 줄바꿈으로 구분하여 합침

            System.out.println("Context for GPT: " + context);

            // 3. 추출한 순수 텍스트(context)를 컨텍스트로 사용하여 GPT API를 호출합니다.
            // gptApi.generateResponse의 두 번째 인자 형식을 확인하여 List.of(context) 등으로 맞추세요.
            return gptApi.generateResponse(userMessage, List.of(Collections.singletonList(context))).block();
        } else {
            // 4. 유사한 문서를 찾지 못한 경우 컨텍스트 없이 GPT API를 호출합니다.
            return gptApi.generateResponse(userMessage, null).block();
        }
    }
}
