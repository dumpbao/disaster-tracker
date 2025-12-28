package com.app.collector.util;

import java.util.Random;

public class RateLimiter {

    private static final Random RAND = new Random();

    public static void sleep(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + RAND.nextInt(maxMs - minMs));
        } catch (InterruptedException ignored) {}
    }
}
