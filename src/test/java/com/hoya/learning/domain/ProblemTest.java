package com.hoya.learning.domain;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemTest {

    @Test
    void 객관식_정답_선택지를_모두_선택하면_CORRECT다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false),
                new Choice(3L, 3, "지문3", true)
        ));

        assertThat(problem.gradeChoice(List.of(1, 3))).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void 객관식_정답을_하나라도_포함하면_PARTIAL이다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false),
                new Choice(3L, 3, "지문3", true)
        ));

        assertThat(problem.gradeChoice(List.of(1, 2))).isEqualTo(AnswerStatus.PARTIAL);
    }

    @Test
    void 객관식_정답을_하나도_포함하지_않으면_WRONG이다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false),
                new Choice(3L, 3, "지문3", true)
        ));

        assertThat(problem.gradeChoice(List.of(2))).isEqualTo(AnswerStatus.WRONG);
    }

    @Test
    void 주관식_정답과_일치하면_CORRECT다() {
        Problem problem = subjectiveProblem(List.of(
                new SubjectiveAnswer(1L, "서울"),
                new SubjectiveAnswer(2L, "Seoul")
        ));

        assertThat(problem.gradeSubjective("서울")).isEqualTo(AnswerStatus.CORRECT);
        assertThat(problem.gradeSubjective("Seoul")).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void 주관식_정답_비교는_대소문자를_무시한다() {
        Problem problem = subjectiveProblem(List.of(
                new SubjectiveAnswer(1L, "Seoul")
        ));

        assertThat(problem.gradeSubjective("SEOUL")).isEqualTo(AnswerStatus.CORRECT);
        assertThat(problem.gradeSubjective("seoul")).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void 주관식_정답_비교는_앞뒤_공백을_무시한다() {
        Problem problem = subjectiveProblem(List.of(
                new SubjectiveAnswer(1L, "서울")
        ));

        assertThat(problem.gradeSubjective("  서울  ")).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void 주관식_오답이면_WRONG이다() {
        Problem problem = subjectiveProblem(List.of(
                new SubjectiveAnswer(1L, "서울")
        ));

        assertThat(problem.gradeSubjective("부산")).isEqualTo(AnswerStatus.WRONG);
    }

    @Test
    void grade_객관식_답안이_null이면_INVALID_ANSWER_TYPE이다() {
        Problem problem = multipleChoiceProblem(List.of(new Choice(1L, 1, "지문1", true)));

        assertThatThrownBy(() -> problem.grade(null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ANSWER_TYPE.getMessage());
    }

    @Test
    void grade_주관식_답안이_null이면_INVALID_ANSWER_TYPE이다() {
        Problem problem = subjectiveProblem(List.of(new SubjectiveAnswer(1L, "서울")));

        assertThatThrownBy(() -> problem.grade(null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ANSWER_TYPE.getMessage());
    }

    @Test
    void grade_단일정답_문제에_복수_선택지를_제출하면_INVALID_ANSWER_TYPE이다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false)
        ));

        assertThatThrownBy(() -> problem.grade(List.of(1, 2), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ANSWER_TYPE.getMessage());
    }

    @Test
    void grade_객관식은_gradeChoice에_위임한다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false)
        ));

        assertThat(problem.grade(List.of(1), null)).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void grade_주관식은_gradeSubjective에_위임한다() {
        Problem problem = subjectiveProblem(List.of(new SubjectiveAnswer(1L, "서울")));

        assertThat(problem.grade(null, "서울")).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void getAnswer_객관식은_정답_선택지_번호를_반환한다() {
        Problem problem = multipleChoiceProblem(List.of(
                new Choice(1L, 1, "지문1", true),
                new Choice(2L, 2, "지문2", false),
                new Choice(3L, 3, "지문3", true)
        ));

        ProblemAnswer answer = problem.getAnswer();

        assertThat(answer.correctChoiceNumbers()).containsExactlyInAnyOrder(1, 3);
        assertThat(answer.correctSubjectiveAnswers()).isNull();
    }

    @Test
    void getAnswer_주관식은_정답_텍스트를_반환한다() {
        Problem problem = subjectiveProblem(List.of(
                new SubjectiveAnswer(1L, "서울"),
                new SubjectiveAnswer(2L, "Seoul")
        ));

        ProblemAnswer answer = problem.getAnswer();

        assertThat(answer.correctSubjectiveAnswers()).containsExactlyInAnyOrder("서울", "Seoul");
        assertThat(answer.correctChoiceNumbers()).isNull();
    }

    private Problem multipleChoiceProblem(List<Choice> choices) {
        return new Problem(1L, 1L, "문제", "해설", ProblemType.MULTIPLE_CHOICE, false, choices, List.of());
    }

    private Problem subjectiveProblem(List<SubjectiveAnswer> answers) {
        return new Problem(1L, 1L, "문제", "해설", ProblemType.SUBJECTIVE, false, List.of(), answers);
    }
}
