package com.hoya.learning.mock;

import com.hoya.learning.domain.AnswerStatus;
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

    @Override
    public void record(Long problemId, AnswerStatus answerStatus) {
        ProblemStatistic statistic = store.getOrDefault(problemId, new ProblemStatistic(problemId, 0, 0));
        statistic.recordResult(answerStatus);
        store.put(problemId, statistic);
    }
}
