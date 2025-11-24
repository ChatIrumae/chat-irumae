package com.chatirumae.chatirumae.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TavilyConfig {

    @Value("${tavily.api.key:tavilykey}")
    private String apiKey;

    @Value("${tavily.api.base-url:https://api.tavily.com}")
    private String apiUrl;

    @Value("${tavily.api.timeout:30000}")
    private int timeout;

    @Bean
    public WebClient tavilyWebClient() {
        System.out.println("Tavily API 설정 확인 중...");
        System.out.println("Tavily API URL: " + apiUrl);
        
        if (apiKey.equals("tavilykey") || apiKey.isEmpty()) {
            System.err.println("Tavily API key가 설정되지 않았습니다.");
            System.err.println("환경변수 TAVILY_API_KEY를 설정하거나 application.properties에서 올바른 API 키를 설정해주세요.");
            throw new IllegalArgumentException("Tavily API key is not properly configured. Please set TAVILY_API_KEY environment variable with a valid API key.");
        }

        System.out.println("Tavily API Key가 올바르게 설정되었습니다.");
        
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }
}

