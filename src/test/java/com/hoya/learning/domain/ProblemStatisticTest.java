package com.hoya.learning.domain;

import com.hoya.learning.domain.AnswerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemStatisticTest {

    @Test
    void CORRECT_제출_시_total과_correct_모두_증가한다() {
        ProblemStatistic statistic = new ProblemStatistic(1L, 0, 0);

        statistic.recordResult(AnswerStatus.CORRECT);

        assertThat(statistic.getTotalSolvedUserCount()).isEqualTo(1);
        assertThat(statistic.getCorrectSolvedUserCount()).isEqualTo(1);
    }

    @Test
    void PARTIAL_제출_시_total만_증가한다() {
        ProblemStatistic statistic = new ProblemStatistic(1L, 0, 0);

        statistic.recordResult(AnswerStatus.PARTIAL);

        assertThat(statistic.getTotalSolvedUserCount()).isEqualTo(1);
        assertThat(statistic.getCorrectSolvedUserCount()).isEqualTo(0);
    }

    @Test
    void WRONG_제출_시_total만_증가한다() {
        ProblemStatistic statistic = new ProblemStatistic(1L, 0, 0);

        statistic.recordResult(AnswerStatus.WRONG);

        assertThat(statistic.getTotalSolvedUserCount()).isEqualTo(1);
        assertThat(statistic.getCorrectSolvedUserCount()).isEqualTo(0);
    }

    @Test
    void 풀이_인원이_30명_미만이면_정답률은_null이다() {
        ProblemStatistic statistic = new ProblemStatistic(1L, 29, 20);

        assertThat(statistic.getCorrectRate()).isNull();
    }

    @Test
    void 풀이_인원이_30명_이상이면_정답률을_반환한다() {
        ProblemStatistic statistic = new ProblemStatistic(1L, 30, 30);

        assertThat(statistic.getCorrectRate()).isEqualTo(100.0);
    }

    @Test
    void 정답률은_소수점_첫째_자리에서_반올림한다() {
        // 20 / 30 = 0.6666... → 67%
        ProblemStatistic statistic = new ProblemStatistic(1L, 30, 20);

        assertThat(statistic.getCorrectRate()).isEqualTo(67.0);
    }
}
