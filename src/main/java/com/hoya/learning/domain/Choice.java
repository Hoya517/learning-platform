package com.hoya.learning.domain;

import lombok.Getter;

@Getter
public class Choice {

    private final Long id;
    private final int number;
    private final String content;
    private final boolean correct;

    public Choice(Long id, int number, String content, boolean correct) {
        this.id = id;
        this.number = number;
        this.content = content;
        this.correct = correct;
    }
}
