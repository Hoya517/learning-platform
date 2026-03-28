package com.hoya.learning.repository;

import com.hoya.learning.domain.ProblemStatistic;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProblemStatisticRepositoryImpl implements ProblemStatisticRepository {
    @Override
    public Optional<ProblemStatistic> findByProblemId(Long problemId) {
        return Optional.empty();
    }

    @Override
    public ProblemStatistic save(ProblemStatistic statistic) {
        return null;
    }
}
