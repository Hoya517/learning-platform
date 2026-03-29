package com.hoya.learning.integration;

import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemType;
import com.hoya.learning.repository.ProblemRepository;
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
class ProblemRepositoryIntegrationTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Test
    void findById_객관식_문제는_선택지를_함께_로딩한다() {
        Optional<Problem> result = problemRepository.findById(1L);

        assertThat(result).isPresent();
        Problem problem = result.get();
        assertThat(problem.getType()).isEqualTo(ProblemType.MULTIPLE_CHOICE);
        assertThat(problem.getChoices()).hasSize(5);
        assertThat(problem.getSubjectiveAnswers()).isEmpty();
    }

    @Test
    void findById_주관식_문제는_정답_텍스트를_함께_로딩한다() {
        Optional<Problem> result = problemRepository.findById(2L);

        assertThat(result).isPresent();
        Problem problem = result.get();
        assertThat(problem.getType()).isEqualTo(ProblemType.SUBJECTIVE);
        assertThat(problem.getSubjectiveAnswers()).hasSize(1);
        assertThat(problem.getSubjectiveAnswers().get(0).getContent()).isEqualTo("서울");
        assertThat(problem.getChoices()).isEmpty();
    }

    @Test
    void findById_존재하지_않는_id는_empty를_반환한다() {
        Optional<Problem> result = problemRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findCandidateProblemIds_풀지_않은_문제_id만_반환한다() {
        // user 10은 문제 2만 풀었음 → 후보 = [1]
        List<Long> candidates = problemRepository.findCandidateProblemIds(1L, 10L);

        assertThat(candidates).containsExactly(1L);
    }

    @Test
    void findCandidateProblemIds_모두_풀었으면_빈_목록을_반환한다() {
        // user 20은 문제 1, 2 모두 풀었음
        List<Long> candidates = problemRepository.findCandidateProblemIds(1L, 20L);

        assertThat(candidates).isEmpty();
    }

    @Test
    void findCandidateProblemIds_아무것도_풀지_않았으면_전체를_반환한다() {
        // user 1은 제출 이력 없음 → 단원 1의 전체 문제 반환
        List<Long> candidates = problemRepository.findCandidateProblemIds(1L, 1L);

        assertThat(candidates).hasSize(2).containsExactlyInAnyOrder(1L, 2L);
    }
}
