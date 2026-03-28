package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemStatistic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problem_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemStatisticJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false, unique = true)
    private Long problemId;

    @Column(name = "total_solved_user_count", nullable = false)
    private int totalSolvedUserCount;

    @Column(name = "correct_solved_user_count", nullable = false)
    private int correctSolvedUserCount;

    public ProblemStatisticJpaEntity(Long problemId) {
        this.problemId = problemId;
        this.totalSolvedUserCount = 0;
        this.correctSolvedUserCount = 0;
    }

    public void recordResult(AnswerStatus answerStatus) {
        this.totalSolvedUserCount++;
        if (answerStatus.isCorrect()) {
            this.correctSolvedUserCount++;
        }
    }

    public void sync(ProblemStatistic domain) {
        this.totalSolvedUserCount = domain.getTotalSolvedUserCount();
        this.correctSolvedUserCount = domain.getCorrectSolvedUserCount();
    }

    public ProblemStatistic toDomain() {
        return new ProblemStatistic(problemId, totalSolvedUserCount, correctSolvedUserCount);
    }
}
