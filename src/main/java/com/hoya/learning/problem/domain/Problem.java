package com.hoya.learning.problem.domain;

import com.hoya.learning.submission.domain.AnswerStatus;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Problem {

    private final Long id;
    private final Long chapterId;
    private final String content;
    private final String explanation;
    private final ProblemType type;
    private final boolean multipleAnswer;
    private final List<Choice> choices;
    private final List<SubjectiveAnswer> subjectiveAnswers;

    public Problem(Long id, Long chapterId, String content, String explanation,
                   ProblemType type, boolean multipleAnswer,
                   List<Choice> choices, List<SubjectiveAnswer> subjectiveAnswers) {
        this.id = id;
        this.chapterId = chapterId;
        this.content = content;
        this.explanation = explanation;
        this.type = type;
        this.multipleAnswer = multipleAnswer;
        this.choices = List.copyOf(choices);
        this.subjectiveAnswers = List.copyOf(subjectiveAnswers);
    }

    public AnswerStatus gradeChoice(List<Integer> submitted) {
        Set<Integer> correct = choices.stream()
                .filter(Choice::isCorrect)
                .map(Choice::getNumber)
                .collect(Collectors.toSet());
        Set<Integer> submittedSet = Set.copyOf(submitted);

        if (correct.equals(submittedSet)) {
            return AnswerStatus.CORRECT;
        }

        boolean hasAnyCorrect = submittedSet.stream().anyMatch(correct::contains);
        return hasAnyCorrect ? AnswerStatus.PARTIAL : AnswerStatus.WRONG;
    }

    public AnswerStatus gradeSubjective(String submitted) {
        String normalized = normalize(submitted);
        boolean isCorrect = subjectiveAnswers.stream()
                .map(a -> normalize(a.getContent()))
                .anyMatch(normalized::equals);
        return isCorrect ? AnswerStatus.CORRECT : AnswerStatus.WRONG;
    }

    private String normalize(String answer) {
        return answer.trim().toLowerCase();
    }
}
