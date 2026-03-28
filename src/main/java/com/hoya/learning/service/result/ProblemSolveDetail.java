package com.hoya.learning.service.result;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemAnswer;

import java.util.List;

public record ProblemSolveDetail(
        Long problemId,
        AnswerStatus answerStatus,
        String explanation,
        ProblemAnswer answer,
        List<Integer> submittedChoiceNumbers,
        String submittedSubjectiveAnswer,
        Double answerCorrectRate
) {
}
