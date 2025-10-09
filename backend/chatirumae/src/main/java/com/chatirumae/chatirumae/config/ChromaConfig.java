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
        System.out.println("ChromaDB 연결 설정: " + chromaUrl);
        return WebClient.builder()
                .baseUrl(chromaUrl)
                .build();
    }
}
