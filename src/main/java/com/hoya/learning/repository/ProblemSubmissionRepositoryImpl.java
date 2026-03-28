package com.hoya.learning.repository;

import com.hoya.learning.domain.ProblemSubmission;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProblemSubmissionRepositoryImpl implements ProblemSubmissionRepository {
    @Override
    public boolean existsByUserIdAndProblemId(Long userId, Long problemId) {
        return false;
    }

    @Override
    public Optional<ProblemSubmission> findByUserIdAndProblemId(Long userId, Long problemId) {
        return Optional.empty();
    }

    @Override
    public ProblemSubmission save(ProblemSubmission submission) {
        return null;
    }
}
