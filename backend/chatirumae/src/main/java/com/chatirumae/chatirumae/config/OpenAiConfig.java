package com.chatirumae.chatirumae.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com/v1}")
    private String apiUrl;

    @Bean
    public WebClient openAiWebClient() {
        System.out.println("OpenAI API Key 설정 확인 중...");
        System.out.println("API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        
        if (apiKey == null || apiKey.isEmpty() || 
            apiKey.equals("your-api-key-here") || 
            apiKey.equals("sk-your-actual-api-key-here") ||
            !apiKey.startsWith("sk-")) {
            throw new IllegalArgumentException("OpenAI API key is not properly configured. Please set OPENAI_API_KEY environment variable with a valid API key starting with 'sk-'.");
        }
        
        System.out.println("OpenAI API Key가 올바르게 설정되었습니다.");
        
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
