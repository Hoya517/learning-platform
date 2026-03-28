package com.hoya.learning.presentation;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.common.response.ApiResponse;
import com.hoya.learning.domain.Choice;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.domain.ProblemType;
import com.hoya.learning.mock.TestContainer;
import com.hoya.learning.presentation.response.RandomProblemResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemControllerTest {

    private static final Long CHAPTER_ID = 1L;
    private static final Long USER_ID = 1L;

    private Problem 객관식_문제() {
        return new Problem(1L, CHAPTER_ID, "Java의 접근 제어자가 아닌 것은?", "default는 접근 제어자가 아닙니다.",
                ProblemType.MULTIPLE_CHOICE, false,
                List.of(
                        new Choice(1L, 1, "public", false),
                        new Choice(2L, 2, "protected", false),
                        new Choice(3L, 3, "default", true),
                        new Choice(4L, 4, "private", false)
                ),
                List.of());
    }

    @Test
    void 랜덤_문제를_조회하면_문제_정보가_반환된다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.addCandidate(CHAPTER_ID, USER_ID, 객관식_문제());

        ApiResponse<RandomProblemResponse> response = tc.problemController.getRandomProblem(CHAPTER_ID, USER_ID, null);

        assertThat(response.data().problemId()).isEqualTo(1L);
        assertThat(response.data().type()).isEqualTo(ProblemType.MULTIPLE_CHOICE);
        assertThat(response.data().multipleAnswer()).isFalse();
        assertThat(response.data().choices()).hasSize(4);
        assertThat(response.data().choices().get(0).number()).isEqualTo(1);
        assertThat(response.data().choices().get(0).content()).isEqualTo("public");
    }

    @Test
    void 정답률이_30명_미만이면_null이다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.addCandidate(CHAPTER_ID, USER_ID, 객관식_문제());
        tc.statisticRepository.save(new ProblemStatistic(1L, 29, 20));

        ApiResponse<RandomProblemResponse> response = tc.problemController.getRandomProblem(CHAPTER_ID, USER_ID, null);

        assertThat(response.data().answerCorrectRate()).isNull();
    }

    @Test
    void 정답률이_30명_이상이면_값이_반환된다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.addCandidate(CHAPTER_ID, USER_ID, 객관식_문제());
        tc.statisticRepository.save(new ProblemStatistic(1L, 30, 15));

        ApiResponse<RandomProblemResponse> response = tc.problemController.getRandomProblem(CHAPTER_ID, USER_ID, null);

        assertThat(response.data().answerCorrectRate()).isEqualTo(50.0);
    }

    @Test
    void 후보_문제가_없으면_NO_AVAILABLE_PROBLEM_예외가_발생한다() {
        TestContainer tc = new TestContainer(bound -> 0);

        assertThatThrownBy(() -> tc.problemController.getRandomProblem(CHAPTER_ID, USER_ID, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AVAILABLE_PROBLEM);
    }

    @Test
    void excludeProblemId_제외_후_후보가_없으면_NO_AVAILABLE_PROBLEM_예외가_발생한다() {
        TestContainer tc = new TestContainer(bound -> 0);
        tc.problemRepository.addCandidate(CHAPTER_ID, USER_ID, 객관식_문제());

        assertThatThrownBy(() -> tc.problemController.getRandomProblem(CHAPTER_ID, USER_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AVAILABLE_PROBLEM);
    }
}
