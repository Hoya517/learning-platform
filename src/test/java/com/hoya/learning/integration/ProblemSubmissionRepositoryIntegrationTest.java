package com.hoya.learning.integration;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemSubmission;
import com.hoya.learning.repository.ProblemSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class ProblemSubmissionRepositoryIntegrationTest {

    @Autowired
    private ProblemSubmissionRepository submissionRepository;

    @Test
    void existsByUserIdAndProblemId_제출_이력이_있으면_true를_반환한다() {
        assertThat(submissionRepository.existsByUserIdAndProblemId(30L, 1L)).isTrue();
    }

    @Test
    void existsByUserIdAndProblemId_제출_이력이_없으면_false를_반환한다() {
        assertThat(submissionRepository.existsByUserIdAndProblemId(1L, 1L)).isFalse();
    }

    @Test
    void findByUserIdAndProblemId_객관식_제출은_선택지_번호를_반환한다() {
        // user 30이 문제 1을 choice 3으로 제출
        Optional<ProblemSubmission> result = submissionRepository.findByUserIdAndProblemId(30L, 1L);

        assertThat(result).isPresent();
        ProblemSubmission submission = result.get();
        assertThat(submission.getAnswerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(submission.getSubmittedChoiceNumbers()).containsExactly(3);
        assertThat(submission.getSubmittedSubjectiveAnswer()).isNull();
    }

    @Test
    void findByUserIdAndProblemId_주관식_제출은_답안을_반환한다() {
        // user 40이 문제 2를 '서울'로 제출
        Optional<ProblemSubmission> result = submissionRepository.findByUserIdAndProblemId(40L, 2L);

        assertThat(result).isPresent();
        ProblemSubmission submission = result.get();
        assertThat(submission.getAnswerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(submission.getSubmittedSubjectiveAnswer()).isEqualTo("서울");
        assertThat(submission.getSubmittedChoiceNumbers()).isEmpty();
    }

    @Test
    void findByUserIdAndProblemId_이력이_없으면_empty를_반환한다() {
        Optional<ProblemSubmission> result = submissionRepository.findByUserIdAndProblemId(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_주관식_제출을_저장한다() {
        ProblemSubmission submission = new ProblemSubmission(99L, 2L, AnswerStatus.CORRECT, null, "서울");

        submissionRepository.save(submission);

        Optional<ProblemSubmission> saved = submissionRepository.findByUserIdAndProblemId(99L, 2L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSubmittedSubjectiveAnswer()).isEqualTo("서울");
        assertThat(saved.get().getSubmittedChoiceNumbers()).isEmpty();
    }

    @Test
    void save_객관식_제출을_저장한다() {
        ProblemSubmission submission = new ProblemSubmission(99L, 1L, AnswerStatus.CORRECT, List.of(3), null);

        submissionRepository.save(submission);

        Optional<ProblemSubmission> saved = submissionRepository.findByUserIdAndProblemId(99L, 1L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSubmittedChoiceNumbers()).containsExactly(3);
        assertThat(saved.get().getSubmittedSubjectiveAnswer()).isNull();
    }
}
