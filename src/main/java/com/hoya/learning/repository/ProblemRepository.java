package com.hoya.learning.repository;

import com.hoya.learning.domain.Problem;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository {

    Optional<Problem> findById(Long id);

    List<Long> findCandidateProblemIds(Long chapterId, Long userId);
}
