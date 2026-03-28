package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.ProblemSubmissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemSubmissionJpaRepository extends JpaRepository<ProblemSubmissionJpaEntity, Long> {

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    Optional<ProblemSubmissionJpaEntity> findByUserIdAndProblemId(Long userId, Long problemId);
}
