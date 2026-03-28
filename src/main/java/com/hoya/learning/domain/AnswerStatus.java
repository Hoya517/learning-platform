package com.hoya.learning.domain;

public enum AnswerStatus {
    CORRECT,
    PARTIAL,
    WRONG;

    public boolean isCorrect() {
        return this == CORRECT;
    }
}
