package com.hoya.learning.repository;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemStatistic;

import java.util.Optional;

public interface ProblemStatisticRepository {

    Optional<ProblemStatistic> findByProblemId(Long problemId);

    ProblemStatistic save(ProblemStatistic statistic);

    void record(Long problemId, AnswerStatus answerStatus);
}
