package com.hoya.learning.service;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.domain.*;
import com.hoya.learning.mock.FakeProblemRepository;
import com.hoya.learning.mock.FakeProblemStatisticRepository;
import com.hoya.learning.service.command.GetRandomProblemCommand;
import com.hoya.learning.service.result.RandomProblem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemServiceTest {

    private ProblemService service;
    private FakeProblemRepository problemRepository;
    private FakeProblemStatisticRepository statisticRepository;

    @BeforeEach
    void setUp() {
        problemRepository = new FakeProblemRepository();
        statisticRepository = new FakeProblemStatisticRepository();
        service = new ProblemServiceImpl(problemRepository, statisticRepository, bound -> 0);
    }

    @Test
    void 안_푼_문제_중_랜덤_1개를_반환한다() {
        problemRepository.addCandidate(1L, 1L, problem(10L));

        RandomProblem result = service.getRandomProblem(new GetRandomProblemCommand(1L, 1L, null));

        assertThat(result.problemId()).isEqualTo(10L);
    }

    @Test
    void excludeProblemId에_해당하는_문제는_제외된다() {
        problemRepository.addCandidate(1L, 1L, problem(10L));
        problemRepository.addCandidate(1L, 1L, problem(20L));
        ProblemService serviceWithLastIndex = new ProblemServiceImpl(problemRepository, statisticRepository, bound -> bound - 1);

        RandomProblem result = serviceWithLastIndex.getRandomProblem(new GetRandomProblemCommand(1L, 1L, 20L));

        assertThat(result.problemId()).isEqualTo(10L);
    }

    @Test
    void excludeProblemId가_유일한_후보이면_NO_AVAILABLE_PROBLEM_예외가_발생한다() {
        problemRepository.addCandidate(1L, 1L, problem(10L));

        assertThatThrownBy(() -> service.getRandomProblem(new GetRandomProblemCommand(1L, 1L, 10L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NO_AVAILABLE_PROBLEM.getMessage());
    }

    @Test
    void 후보_문제가_없으면_NO_AVAILABLE_PROBLEM_예외가_발생한다() {
        assertThatThrownBy(() -> service.getRandomProblem(new GetRandomProblemCommand(1L, 1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NO_AVAILABLE_PROBLEM.getMessage());
    }

    @Test
    void 정답률이_없으면_null을_반환한다() {
        problemRepository.addCandidate(1L, 1L, problem(10L));

        RandomProblem result = service.getRandomProblem(new GetRandomProblemCommand(1L, 1L, null));

        assertThat(result.answerCorrectRate()).isNull();
    }

    @Test
    void 정답률이_있으면_반환한다() {
        problemRepository.addCandidate(1L, 1L, problem(10L));
        statisticRepository.save(new ProblemStatistic(10L, 30, 30));

        RandomProblem result = service.getRandomProblem(new GetRandomProblemCommand(1L, 1L, null));

        assertThat(result.answerCorrectRate()).isEqualTo(100.0);
    }

    private Problem problem(Long id) {
        return new Problem(id, 1L, "문제", "해설", ProblemType.MULTIPLE_CHOICE, false,
                List.of(new Choice(1L, 1, "지문1", true)), List.of());
    }
}
