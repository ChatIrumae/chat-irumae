package com.chatirumae.chatirumae.core.interfaces;

import reactor.core.publisher.Mono;

import java.util.List;

public interface GptApi {
    Mono<String> generateResponse(String prompt, List<List<String>> documents);
}
