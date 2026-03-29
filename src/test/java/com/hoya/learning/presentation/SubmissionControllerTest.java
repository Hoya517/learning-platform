package com.hoya.learning.presentation;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.common.response.ApiResponse;
import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.Choice;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.domain.ProblemSubmission;
import com.hoya.learning.domain.ProblemType;
import com.hoya.learning.domain.SubjectiveAnswer;
import com.hoya.learning.mock.TestContainer;
import com.hoya.learning.presentation.request.SubmitProblemRequest;
import com.hoya.learning.presentation.response.SolveDetailResponse;
import com.hoya.learning.presentation.response.SubmitProblemResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionControllerTest {

    private static final Long USER_ID = 1L;

    private Problem 객관식_문제() {
        return new Problem(1L, 1L, "Java의 접근 제어자가 아닌 것은?", "default는 접근 제어자가 아닙니다.",
                ProblemType.MULTIPLE_CHOICE, false,
                List.of(
                        new Choice(1L, 1, "public", false),
                        new Choice(2L, 2, "protected", false),
                        new Choice(3L, 3, "default", true),
                        new Choice(4L, 4, "private", false)
                ),
                List.of());
    }

    private Problem 복수정답_객관식_문제() {
        return new Problem(3L, 1L, "컴파일 에러가 발생하는 것을 모두 고르시오.", "해설",
                ProblemType.MULTIPLE_CHOICE, true,
                List.of(
                        new Choice(1L, 1, "지문1", true),
                        new Choice(2L, 2, "지문2", false),
                        new Choice(3L, 3, "지문3", true)
                ),
                List.of());
    }

    private Problem 주관식_문제() {
        return new Problem(2L, 1L, "대한민국의 수도는?", "대한민국의 수도는 서울입니다.",
                ProblemType.SUBJECTIVE, false,
                List.of(),
                List.of(new SubjectiveAnswer(1L, "서울")));
    }

    @Test
    void 객관식_정답_제출_시_CORRECT를_반환한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(객관식_문제());

        ApiResponse<SubmitProblemResponse> response = tc.submissionController.submit(
                USER_ID, 1L, new SubmitProblemRequest(List.of(3), null));

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(response.data().answer().correctChoiceNumbers()).containsExactly(3);
    }

    @Test
    void 객관식_부분정답_제출_시_PARTIAL을_반환한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(복수정답_객관식_문제());

        ApiResponse<SubmitProblemResponse> response = tc.submissionController.submit(
                USER_ID, 3L, new SubmitProblemRequest(List.of(1, 2), null));

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.PARTIAL);
    }

    @Test
    void 객관식_오답_제출_시_WRONG을_반환한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(객관식_문제());

        ApiResponse<SubmitProblemResponse> response = tc.submissionController.submit(
                USER_ID, 1L, new SubmitProblemRequest(List.of(1), null));

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.WRONG);
    }

    @Test
    void 주관식_정답_제출_시_CORRECT를_반환한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(주관식_문제());

        ApiResponse<SubmitProblemResponse> response = tc.submissionController.submit(
                USER_ID, 2L, new SubmitProblemRequest(null, "서울"));

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(response.data().answer().correctSubjectiveAnswers()).containsExactly("서울");
    }

    @Test
    void 주관식_대소문자_무시하고_정답_처리된다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(new Problem(3L, 1L, "Java를 만든 회사는?", "Sun Microsystems입니다.",
                ProblemType.SUBJECTIVE, false, List.of(),
                List.of(new SubjectiveAnswer(1L, "Sun Microsystems"))));

        ApiResponse<SubmitProblemResponse> response = tc.submissionController.submit(
                USER_ID, 3L, new SubmitProblemRequest(null, "SUN MICROSYSTEMS"));

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    void 이미_제출한_문제_재제출_시_ALREADY_SUBMITTED_PROBLEM_예외가_발생한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(객관식_문제());
        tc.submissionRepository.save(new ProblemSubmission(USER_ID, 1L, AnswerStatus.CORRECT, List.of(3), null));

        assertThatThrownBy(() -> tc.submissionController.submit(
                USER_ID, 1L, new SubmitProblemRequest(List.of(3), null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_SUBMITTED_PROBLEM);
    }

    @Test
    void 존재하지_않는_문제_제출_시_PROBLEM_NOT_FOUND_예외가_발생한다() {
        TestContainer tc = new TestContainer(bound -> 0);

        assertThatThrownBy(() -> tc.submissionController.submit(
                USER_ID, 999L, new SubmitProblemRequest(List.of(1), null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROBLEM_NOT_FOUND);
    }

    @Test
    void 풀이_상세_조회_시_제출_정보와_정답이_반환된다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(객관식_문제());
        tc.submissionRepository.save(new ProblemSubmission(USER_ID, 1L, AnswerStatus.CORRECT, List.of(3), null));
        tc.statisticRepository.save(new ProblemStatistic(1L, 30, 25));

        ApiResponse<SolveDetailResponse> response = tc.submissionController.getSolveDetail(USER_ID, 1L);

        assertThat(response.data().answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(response.data().submittedChoiceNumbers()).containsExactly(3);
        assertThat(response.data().answer().correctChoiceNumbers()).containsExactly(3);
        assertThat(response.data().answerCorrectRate()).isEqualTo(83.0);
    }

    @Test
    void 풀이_이력이_없는_문제_조회_시_SUBMISSION_NOT_FOUND_예외가_발생한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.save(객관식_문제());

        assertThatThrownBy(() -> tc.submissionController.getSolveDetail(USER_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBMISSION_NOT_FOUND);
    }
}
