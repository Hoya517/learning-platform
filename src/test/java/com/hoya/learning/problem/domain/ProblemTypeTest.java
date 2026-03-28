package com.hoya.learning.problem.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemTypeTest {

    @Test
    void MULTIPLE_CHOICE는_isMultipleChoice가_true다() {
        assertThat(ProblemType.MULTIPLE_CHOICE.isMultipleChoice()).isTrue();
        assertThat(ProblemType.MULTIPLE_CHOICE.isSubjective()).isFalse();
    }

    @Test
    void SUBJECTIVE는_isSubjective가_true다() {
        assertThat(ProblemType.SUBJECTIVE.isSubjective()).isTrue();
        assertThat(ProblemType.SUBJECTIVE.isMultipleChoice()).isFalse();
    }
}
