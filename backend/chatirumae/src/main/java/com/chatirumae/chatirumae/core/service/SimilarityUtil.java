package com.chatirumae.chatirumae.core.service;

import java.util.List;

/**
 * 벡터 유사도 계산 유틸리티 클래스
 */
public class SimilarityUtil {

    /**
     * 코사인 유사도 계산
     * @param vector1 첫 번째 벡터
     * @param vector2 두 번째 벡터
     * @return 코사인 유사도 (0.0 ~ 1.0)
     */
    public static double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("벡터의 크기가 일치하지 않거나 null입니다.");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            double v1 = vector1.get(i);
            double v2 = vector2.get(i);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 유사도가 threshold 이상인지 확인
     * @param similarity 유사도 값
     * @param threshold 임계값
     * @return threshold 이상이면 true
     */
    public static boolean isSimilar(double similarity, double threshold) {
        return similarity >= threshold;
    }
}

