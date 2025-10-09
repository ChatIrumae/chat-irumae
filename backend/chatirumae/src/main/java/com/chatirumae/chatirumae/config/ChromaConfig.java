package com.chatirumae.chatirumae.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ChromaConfig {
    
    @Value("${spring.ai.vectorstore.chroma.url}")
    private String chromaUrl;
    
    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;
    
    @Bean
    public WebClient chromaWebClient() {
        System.out.println("=== ChromaDB 설정 정보 ===");
        System.out.println("ChromaDB URL: " + chromaUrl);
        System.out.println("Collection Name: " + collectionName);
        System.out.println("=========================");
        
        return WebClient.builder()
                .baseUrl(chromaUrl)
                .build();
    }
    
    // VectorStore는 Spring AI의 자동 설정에 의해 자동으로 생성됩니다
    // application.properties의 설정을 통해 ChromaVectorStore가 자동으로 구성됩니다
}
