package com.joker.smartquiz.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joker
 * @since 2026/08/11
 */
public class CosineSimilarity {

    public static double calculateCosineSimilarity(List<String> text1, List<String> text2) {
        // 1. 分词

        // 2. 统计词频
        Map<String, Integer> freqMap1 = getWordFrequency(text1);
        Map<String, Integer> freqMap2 = getWordFrequency(text2);

        // 3. 计算余弦相似度
        return cosineSimilarity(freqMap1, freqMap2);
    }

    private static Map<String, Integer> getWordFrequency(List<String> words) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String word : words) {
            Integer i = frequencyMap.get(word);
            if(i == null) {
                i = 0;
            }
            frequencyMap.put(word, i + 1);
        }
        return frequencyMap;
    }

    private static double cosineSimilarity(Map<String, Integer> freqMap1, Map<String, Integer> freqMap2) {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(freqMap1.keySet());
        allWords.addAll(freqMap2.keySet());

        // 计算向量点积 & 模长
        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (String word : allWords) {
            Integer i1 = freqMap1.get(word);
            int v1 = i1 == null ? 0 : i1;
            Integer i2 = freqMap2.get(word);
            int v2 = i2 == null ? 0 : i2;

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        // 避免除 0
        return (norm1 == 0 || norm2 == 0) ? 0.0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
