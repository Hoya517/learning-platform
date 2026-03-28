package com.hoya.learning.domain;

import lombok.Getter;

@Getter
public class SubjectiveAnswer {

    private final Long id;
    private final String content;

    public SubjectiveAnswer(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}
