package com.hoya.learning.mock;

import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.repository.ProblemStatisticRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeProblemStatisticRepository implements ProblemStatisticRepository {

    private final Map<Long, ProblemStatistic> store = new HashMap<>();

    @Override
    public Optional<ProblemStatistic> findByProblemId(Long problemId) {
        return Optional.ofNullable(store.get(problemId));
    }

    @Override
    public ProblemStatistic save(ProblemStatistic statistic) {
        store.put(statistic.getProblemId(), statistic);
        return statistic;
    }
}
