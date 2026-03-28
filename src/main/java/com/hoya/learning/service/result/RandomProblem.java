package com.hoya.learning.service.result;

import com.hoya.learning.domain.ProblemType;

import java.util.List;

public record RandomProblem(
        Long problemId,
        String content,
        ProblemType type,
        boolean multipleAnswer,
        List<ChoiceInfo> choices,
        Double answerCorrectRate
) {
    public record ChoiceInfo(int number, String content) {}
}
