package com.hoya.learning.domain;

import com.hoya.learning.domain.AnswerStatus;
import lombok.Getter;

@Getter
public class ProblemStatistic {

    private static final int MIN_SOLVED_COUNT_FOR_RATE = 30;

    private final Long problemId;
    private int totalSolvedUserCount;
    private int correctSolvedUserCount;

    public ProblemStatistic(Long problemId, int totalSolvedUserCount, int correctSolvedUserCount) {
        this.problemId = problemId;
        this.totalSolvedUserCount = totalSolvedUserCount;
        this.correctSolvedUserCount = correctSolvedUserCount;
    }

    public void recordResult(AnswerStatus answerStatus) {
        totalSolvedUserCount++;
        if (answerStatus.isCorrect()) {
            correctSolvedUserCount++;
        }
    }

    public Double getCorrectRate() {
        if (totalSolvedUserCount < MIN_SOLVED_COUNT_FOR_RATE) {
            return null;
        }
        return (double) Math.round((double) correctSolvedUserCount / totalSolvedUserCount * 100);
    }
}
