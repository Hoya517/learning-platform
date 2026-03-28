package com.hoya.learning.submission.domain;

public enum AnswerStatus {
    CORRECT,
    PARTIAL,
    WRONG;

    public boolean isCorrect() {
        return this == CORRECT;
    }
}
