package com.hoya.learning.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class ProblemSubmission {

    private final Long userId;
    private final Long problemId;
    private final AnswerStatus answerStatus;
    private final List<Integer> submittedChoiceNumbers;
    private final String submittedSubjectiveAnswer;

    public ProblemSubmission(Long userId, Long problemId, AnswerStatus answerStatus,
                             List<Integer> submittedChoiceNumbers, String submittedSubjectiveAnswer) {
        this.userId = userId;
        this.problemId = problemId;
        this.answerStatus = answerStatus;
        this.submittedChoiceNumbers = submittedChoiceNumbers;
        this.submittedSubjectiveAnswer = submittedSubjectiveAnswer;
    }
}
