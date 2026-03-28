package com.hoya.learning.problem.repository;

import com.hoya.learning.problem.domain.ProblemStatistic;

import java.util.Optional;

public interface ProblemStatisticRepository {

    Optional<ProblemStatistic> findByProblemId(Long problemId);

    ProblemStatistic save(ProblemStatistic statistic);
}
