package com.hoya.learning.service;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.domain.*;
import com.hoya.learning.mock.FakeProblemRepository;
import com.hoya.learning.mock.FakeProblemStatisticRepository;
import com.hoya.learning.mock.FakeProblemSubmissionRepository;
import com.hoya.learning.service.command.GetSolveDetailCommand;
import com.hoya.learning.service.command.SubmitProblemCommand;
import com.hoya.learning.service.result.ProblemSolveDetail;
import com.hoya.learning.service.result.SubmissionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionServiceTest {

    private SubmissionService service;
    private FakeProblemRepository problemRepository;
    private FakeProblemStatisticRepository statisticRepository;
    private FakeProblemSubmissionRepository submissionRepository;

    @BeforeEach
    void setUp() {
        problemRepository = new FakeProblemRepository();
        statisticRepository = new FakeProblemStatisticRepository();
        submissionRepository = new FakeProblemSubmissionRepository();
        service = new SubmissionServiceImpl(problemRepository, submissionRepository, statisticRepository);
    }

    @Test
    void 객관식_제출_시_채점_결과를_반환한다() {
        problemRepository.save(multipleChoiceProblem(1L));

        SubmissionResult result = service.submit(new SubmitProblemCommand(1L, 1L, List.of(1), null));

        assertThat(result.answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(result.explanation()).isEqualTo("해설");
        assertThat(result.answer().correctChoiceNumbers()).containsExactly(1);
    }

    @Test
    void 주관식_제출_시_채점_결과를_반환한다() {
        problemRepository.save(subjectiveProblem(2L));

        SubmissionResult result = service.submit(new SubmitProblemCommand(1L, 2L, null, "서울"));

        assertThat(result.answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(result.answer().correctSubjectiveAnswers()).containsExactly("서울");
    }

    @Test
    void 이미_제출한_문제는_ALREADY_SUBMITTED_PROBLEM_예외가_발생한다() {
        problemRepository.save(multipleChoiceProblem(1L));
        service.submit(new SubmitProblemCommand(1L, 1L, List.of(1), null));

        assertThatThrownBy(() -> service.submit(new SubmitProblemCommand(1L, 1L, List.of(1), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ALREADY_SUBMITTED_PROBLEM.getMessage());
    }

    @Test
    void 제출_시_통계가_갱신된다() {
        problemRepository.save(multipleChoiceProblem(1L));

        service.submit(new SubmitProblemCommand(1L, 1L, List.of(1), null));

        assertThat(statisticRepository.findByProblemId(1L))
                .isPresent()
                .hasValueSatisfying(s -> assertThat(s.getTotalSolvedUserCount()).isEqualTo(1));
    }

    @Test
    void 풀이_상세를_반환한다() {
        problemRepository.save(multipleChoiceProblem(1L));
        service.submit(new SubmitProblemCommand(1L, 1L, List.of(1), null));

        ProblemSolveDetail detail = service.getSolveDetail(new GetSolveDetailCommand(1L, 1L));

        assertThat(detail.answerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(detail.submittedChoiceNumbers()).containsExactly(1);
        assertThat(detail.answerCorrectRate()).isNull();
    }

    @Test
    void 풀이_이력이_없으면_SUBMISSION_NOT_FOUND_예외가_발생한다() {
        problemRepository.save(multipleChoiceProblem(1L));

        assertThatThrownBy(() -> service.getSolveDetail(new GetSolveDetailCommand(1L, 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SUBMISSION_NOT_FOUND.getMessage());
    }

    private Problem multipleChoiceProblem(Long id) {
        return new Problem(id, 1L, "문제", "해설", ProblemType.MULTIPLE_CHOICE, false,
                List.of(new Choice(1L, 1, "지문1", true)), List.of());
    }

    private Problem subjectiveProblem(Long id) {
        return new Problem(id, 1L, "문제", "해설", ProblemType.SUBJECTIVE, false, List.of(),
                List.of(new SubjectiveAnswer(1L, "서울")));
    }
}
