package com.hoya.learning.service.result;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemAnswer;

public record SubmissionResult(
        AnswerStatus answerStatus,
        String explanation,
        ProblemAnswer answer
) {
}
