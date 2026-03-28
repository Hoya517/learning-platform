package com.hoya.learning.repository;

import com.hoya.learning.domain.ProblemSubmission;

import java.util.Optional;

public interface ProblemSubmissionRepository {

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    Optional<ProblemSubmission> findByUserIdAndProblemId(Long userId, Long problemId);

    ProblemSubmission save(ProblemSubmission submission);
}
