package com.hoya.learning.integration;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.repository.ProblemStatisticRepository;
import com.hoya.learning.repository.entity.ProblemStatisticJpaEntity;
import com.hoya.learning.repository.jpa.ProblemStatisticJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class ProblemStatisticRepositoryIntegrationTest {

    @Autowired
    private ProblemStatisticRepository statisticRepository;

    @Autowired
    private ProblemStatisticJpaRepository statisticJpaRepository;

    /**
     * 문제 2는 통계가 없는 상태 → record() 호출 시 신규 row 생성
     */
    @Test
    void record_통계가_없는_문제에_첫_제출_시_신규_통계가_생성된다() {
        statisticRepository.record(2L, AnswerStatus.CORRECT);

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(2L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(1);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(1);
    }

    /**
     * 문제 1은 통계가 이미 존재 (30/20) → record() 호출 시 카운트 증가
     */
    @Test
    void record_통계가_있는_문제는_기존_카운트에서_증가한다() {
        statisticRepository.record(1L, AnswerStatus.CORRECT);

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(1L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(31);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(21);
    }

    @Test
    void record_WRONG_제출_시_total만_증가하고_correct는_변하지_않는다() {
        statisticRepository.record(1L, AnswerStatus.WRONG);

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(1L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(31);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(20);
    }

    @Test
    void record_PARTIAL_제출_시_total만_증가하고_correct는_변하지_않는다() {
        statisticRepository.record(1L, AnswerStatus.PARTIAL);

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(1L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(31);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(20);
    }

    @Test
    void findByProblemId_통계가_없으면_empty를_반환한다() {
        assertThat(statisticRepository.findByProblemId(2L)).isEmpty();
    }

    @Test
    void findByProblemId_통계가_있으면_도메인_객체를_반환한다() {
        var stat = statisticRepository.findByProblemId(1L).orElseThrow();

        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(30);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(20);
    }
}
