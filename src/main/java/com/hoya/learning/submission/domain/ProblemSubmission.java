package com.hoya.learning.submission.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class ProblemSubmission {

    private final Long id;
    private final Long userId;
    private final Long problemId;
    private final AnswerStatus answerStatus;
    private final List<Integer> submittedChoiceNumbers;
    private final String submittedSubjectiveAnswer;

    public ProblemSubmission(Long id, Long userId, Long problemId, AnswerStatus answerStatus,
                             List<Integer> submittedChoiceNumbers, String submittedSubjectiveAnswer) {
        this.id = id;
        this.userId = userId;
        this.problemId = problemId;
        this.answerStatus = answerStatus;
        this.submittedChoiceNumbers = submittedChoiceNumbers;
        this.submittedSubjectiveAnswer = submittedSubjectiveAnswer;
    }
}
