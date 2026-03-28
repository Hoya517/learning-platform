package com.hoya.learning.domain;

public enum ProblemType {
    MULTIPLE_CHOICE,
    SUBJECTIVE;

    public boolean isMultipleChoice() {
        return this == MULTIPLE_CHOICE;
    }

    public boolean isSubjective() {
        return this == SUBJECTIVE;
    }
}
