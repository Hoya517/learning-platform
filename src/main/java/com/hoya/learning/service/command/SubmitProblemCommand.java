package com.hoya.learning.service.command;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemSubmission;

import java.util.List;

public record SubmitProblemCommand(
        Long userId,
        Long problemId,
        List<Integer> choiceNumbers,
        String subjectiveAnswer
) {
    public ProblemSubmission toSubmission(AnswerStatus answerStatus) {
        return new ProblemSubmission(userId, problemId, answerStatus, choiceNumbers, subjectiveAnswer);
    }
}
