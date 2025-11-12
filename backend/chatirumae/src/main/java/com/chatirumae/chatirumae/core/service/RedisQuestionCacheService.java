package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.model.CachedQuestion;
import com.chatirumae.chatirumae.infra.EmbeddingRequestDto;
import com.chatirumae.chatirumae.infra.EmbeddingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis를 사용한 질문-답변 캐시 서비스
 * 임베딩을 사용하여 유사한 질문을 찾고 답변을 캐시합니다.
 */
@Service
public class RedisQuestionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient openAiWebClient;
    
    private static final String CACHE_KEY_PREFIX = "chat:question:";
    private static final String USER_QUESTIONS_KEY_PREFIX = "chat:user:";
    private static final String ALL_QUESTIONS_KEY = "chat:questions:all"; // 모든 질문 목록
    private static final long CACHE_TTL_HOURS = 24; // 캐시 유지 시간 (24시간)
    
    @Value("${chat.cache.similarity-threshold:0.8}")
    private double similarityThreshold;
    
    @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}")
    private String embeddingModel;

    public RedisQuestionCacheService(
            RedisTemplate<String, Object> redisTemplate,
            WebClient openAiWebClient) {
        this.redisTemplate = redisTemplate;
        this.openAiWebClient = openAiWebClient;
    }

    /**
     * 질문에 대한 임베딩 벡터 생성
     * @param question 질문 텍스트
     * @return 임베딩 벡터
     */
    public List<Double> generateEmbedding(String question) {
        try {
            System.out.println("임베딩 생성 중: " + question);
            
            // OpenAI Embedding API 직접 호출
            EmbeddingRequestDto request = new EmbeddingRequestDto(embeddingModel, question);
            
            EmbeddingResponseDto response = openAiWebClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponseDto.class)
                    .block();
            
            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                List<Double> embedding = response.getData().get(0).getEmbedding();
                System.out.println("임베딩 생성 완료. 벡터 크기: " + embedding.size());
                return embedding;
            }
            
            throw new RuntimeException("임베딩 생성 실패: 응답이 비어있습니다.");
        } catch (Exception e) {
            System.err.println("임베딩 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("임베딩 생성 실패", e);
        }
    }

    /**
     * 질문-답변 쌍을 Redis에 저장 (임베딩 포함)
     * @param question 질문
     * @param answer 답변
     * @param userId 사용자 ID
     */
    public void cacheQuestionAnswer(String question, String answer, String userId) {
        try {
            System.out.println("질문-답변 캐시 저장 시작: " + question);
            
            // 임베딩 생성
            List<Double> embedding = generateEmbedding(question);
            
            // CachedQuestion 객체 생성
            CachedQuestion cachedQuestion = new CachedQuestion(question, answer, embedding, userId);
            
            // Redis에 저장 (userId 없이 질문만으로 키 생성)
            String cacheKey = CACHE_KEY_PREFIX + question.hashCode();
            redisTemplate.opsForValue().set(cacheKey, cachedQuestion, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            // 사용자별 질문 목록에 추가 (선택적, 사용자별 관리가 필요한 경우)
            String userQuestionsKey = USER_QUESTIONS_KEY_PREFIX + userId;
            redisTemplate.opsForSet().add(userQuestionsKey, cacheKey);
            redisTemplate.expire(userQuestionsKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            // 전역 질문 목록에 추가 (모든 사용자의 질문 검색을 위해)
            redisTemplate.opsForSet().add(ALL_QUESTIONS_KEY, cacheKey);
            redisTemplate.expire(ALL_QUESTIONS_KEY, CACHE_TTL_HOURS, TimeUnit.HOURS);

        } catch (Exception e) {
            System.err.println("질문-답변 캐시 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 유사한 질문을 찾아 캐시된 답변 반환 (모든 사용자의 질문에서 검색)
     * @param question 질문
     * @param userId 사용자 ID (호환성을 위해 유지, 실제로는 사용하지 않음)
     * @return 유사한 질문이 있으면 답변, 없으면 null
     */
    public String findSimilarQuestion(String question, String userId) {
        try {
            // 현재 질문의 임베딩 생성
            List<Double> questionEmbedding = generateEmbedding(question);
            
            // 모든 사용자의 캐시된 질문 가져오기 (userId 무시)
            Set<Object> cacheKeys = redisTemplate.opsForSet().members(ALL_QUESTIONS_KEY);
            
            if (cacheKeys == null || cacheKeys.isEmpty()) {
                System.out.println("캐시된 질문이 없습니다.");
                return null;
            }
            
            System.out.println("캐시된 질문 수 (전체): " + cacheKeys.size());
            
            // 각 캐시된 질문과 유사도 비교
            double maxSimilarity = 0.0;
            CachedQuestion mostSimilarQuestion = null;
            
            for (Object keyObj : cacheKeys) {
                String cacheKey = (String) keyObj;
                CachedQuestion cachedQuestion = (CachedQuestion) redisTemplate.opsForValue().get(cacheKey);
                
                if (cachedQuestion == null || cachedQuestion.getEmbedding() == null) {
                    continue;
                }
                
                // 코사인 유사도 계산
                double similarity = SimilarityUtil.cosineSimilarity(
                    questionEmbedding, 
                    cachedQuestion.getEmbedding()
                );
                
                System.out.println("질문: " + cachedQuestion.getQuestion() + 
                                 ", 유사도: " + similarity);
                
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilarQuestion = cachedQuestion;
                }
            }
            
            // 유사도가 threshold 이상인 경우에만 반환
            if (mostSimilarQuestion != null && 
                SimilarityUtil.isSimilar(maxSimilarity, similarityThreshold)) {
                System.out.println("유사한 질문 발견! 유사도: " + maxSimilarity + 
                                 ", 답변: " + mostSimilarQuestion.getAnswer());
                return mostSimilarQuestion.getAnswer();
            }
            
            System.out.println("유사한 질문을 찾지 못했습니다. 최대 유사도: " + maxSimilarity);
            return null;
            
        } catch (Exception e) {
            System.err.println("유사한 질문 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 사용자의 모든 캐시된 질문 삭제
     * @param userId 사용자 ID
     */
    public void clearUserCache(String userId) {
        try {
            String userQuestionsKey = USER_QUESTIONS_KEY_PREFIX + userId;
            Set<Object> cacheKeys = redisTemplate.opsForSet().members(userQuestionsKey);
            
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                for (Object keyObj : cacheKeys) {
                    String cacheKey = (String) keyObj;
                    redisTemplate.delete(cacheKey);
                    // 전역 질문 목록에서도 제거
                    redisTemplate.opsForSet().remove(ALL_QUESTIONS_KEY, cacheKey);
                }
            }
            
            redisTemplate.delete(userQuestionsKey);
            System.out.println("사용자 캐시 삭제 완료: " + userId);
        } catch (Exception e) {
            System.err.println("사용자 캐시 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 특정 질문-답변 쌍 삭제
     * @param question 질문
     * @param userId 사용자 ID (호환성을 위해 유지, 실제로는 사용하지 않음)
     */
    public void deleteCachedQuestion(String question, String userId) {
        try {
            // userId 없이 질문만으로 키 생성
            String cacheKey = CACHE_KEY_PREFIX + question.hashCode();
            redisTemplate.delete(cacheKey);
            
            // 모든 사용자의 질문 목록에서 제거 (필요한 경우)
            if (userId != null) {
                String userQuestionsKey = USER_QUESTIONS_KEY_PREFIX + userId;
                redisTemplate.opsForSet().remove(userQuestionsKey, cacheKey);
            }
            
            // 전역 질문 목록에서도 제거
            redisTemplate.opsForSet().remove(ALL_QUESTIONS_KEY, cacheKey);
            
            System.out.println("질문-답변 캐시 삭제 완료: " + cacheKey);
        } catch (Exception e) {
            System.err.println("질문-답변 캐시 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

