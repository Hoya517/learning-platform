package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.ProblemStatisticJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemStatisticJpaRepository extends JpaRepository<ProblemStatisticJpaEntity, Long> {

    Optional<ProblemStatisticJpaEntity> findByProblemId(Long problemId);
}
