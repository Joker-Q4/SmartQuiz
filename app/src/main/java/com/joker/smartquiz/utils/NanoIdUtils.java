package com.joker.smartquiz.utils;

import java.security.SecureRandom;

/**
 * @author Joker
 * @since 2026/08/11
 */
public class NanoIdUtils {

    private static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();
    //长度必须大于0，小于等于256，自定义字符
    private static final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    //位数必须大于0，自定义长度
    private static final int DEFAULT_SIZE = 21;

    private NanoIdUtils() {
    }

    public static String randomNanoId() {
        return randomNanoId(DEFAULT_SIZE);
    }

    public static String randomNanoId(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero.");
        }
        if (DEFAULT_ALPHABET.length == 0 || DEFAULT_ALPHABET.length > 256) {
            throw new IllegalArgumentException("alphabet must contain between 1 and 255 symbols.");
        }

        int mask = (2 << (int) Math.floor(Math.log((DEFAULT_ALPHABET.length - 1)) / Math.log(2.0D))) - 1;
        int step = (int) Math.ceil(1.6D * (double) mask * (double) size / (double) DEFAULT_ALPHABET.length);
        StringBuilder idBuilder = new StringBuilder();
        while (true) {
            byte[] bytes = new byte[step];
            DEFAULT_NUMBER_GENERATOR.nextBytes(bytes);
            for (int i = 0; i < step; ++i) {
                int alphabetIndex = bytes[i] & mask;
                if (alphabetIndex < DEFAULT_ALPHABET.length) {
                    idBuilder.append(DEFAULT_ALPHABET[alphabetIndex]);
                    if (idBuilder.length() == size) {
                        return idBuilder.toString();
                    }
                }
            }
        }
    }
}
