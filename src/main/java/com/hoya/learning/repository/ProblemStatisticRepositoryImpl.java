package com.hoya.learning.repository;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.repository.entity.ProblemStatisticJpaEntity;
import com.hoya.learning.repository.jpa.ProblemStatisticJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProblemStatisticRepositoryImpl implements ProblemStatisticRepository {

    private final ProblemStatisticJpaRepository statisticJpaRepository;

    @Override
    public Optional<ProblemStatistic> findByProblemId(Long problemId) {
        return statisticJpaRepository.findByProblemId(problemId)
                .map(ProblemStatisticJpaEntity::toDomain);
    }

    @Override
    public void record(Long problemId, AnswerStatus answerStatus) {
        ProblemStatisticJpaEntity entity = statisticJpaRepository
                .findByProblemId(problemId)
                .orElseGet(() -> new ProblemStatisticJpaEntity(problemId));
        ProblemStatistic domain = entity.toDomain();
        domain.recordResult(answerStatus);
        entity.sync(domain);
        statisticJpaRepository.save(entity);
    }
}
