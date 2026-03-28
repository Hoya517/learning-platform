package com.hoya.learning.domain;

import java.util.List;

public record ProblemAnswer(
        List<Integer> correctChoiceNumbers,
        List<String> correctSubjectiveAnswers
) {
    public static ProblemAnswer ofMultipleChoice(List<Integer> numbers) {
        return new ProblemAnswer(numbers, null);
    }

    public static ProblemAnswer ofSubjective(List<String> answers) {
        return new ProblemAnswer(null, answers);
    }
}
