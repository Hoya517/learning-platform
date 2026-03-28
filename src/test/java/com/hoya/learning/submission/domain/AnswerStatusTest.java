package com.hoya.learning.submission.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerStatusTest {

    @Test
    void CORRECT는_isCorrect가_true다() {
        assertThat(AnswerStatus.CORRECT.isCorrect()).isTrue();
    }

    @Test
    void PARTIAL은_isCorrect가_false다() {
        assertThat(AnswerStatus.PARTIAL.isCorrect()).isFalse();
    }

    @Test
    void WRONG은_isCorrect가_false다() {
        assertThat(AnswerStatus.WRONG.isCorrect()).isFalse();
    }
}
