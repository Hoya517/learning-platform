package com.hoya.learning.repository;

import com.hoya.learning.domain.Problem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProblemRepositoryImpl implements ProblemRepository {
    @Override
    public Optional<Problem> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Long> findCandidateProblemIds(Long chapterId, Long userId) {
        return List.of();
    }
}
