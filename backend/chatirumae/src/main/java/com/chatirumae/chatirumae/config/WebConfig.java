package com.chatirumae.chatirumae.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 스프링의 설정 파일임을 명시합니다.
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // "/**"는 모든 경로에 대한 CORS 설정을 적용하겠다는 의미입니다.
                .allowedOrigins("http://localhost:3000", "http://chat-irumae.kro.kr/", "https://chat-irumae.kro.kr/") // 허용할 출처(Origin)를 명시합니다. React, Vue 등 프론트엔드 서버 주소를 넣습니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드를 지정합니다.
                .allowedHeaders("*") // 허용할 요청 헤더를 지정합니다. "*"는 모든 헤더를 허용합니다.
                .allowCredentials(true) // 쿠키 등 자격 증명을 허용할지 여부를 설정합니다.
                .maxAge(3600); // Pre-flight 요청의 결과를 캐시할 시간(초)을 설정합니다.
    }
}