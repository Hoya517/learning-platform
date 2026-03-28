package com.hoya.learning.common;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SystemRandomHolder implements RandomHolder {

    private final Random random = new Random();

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
