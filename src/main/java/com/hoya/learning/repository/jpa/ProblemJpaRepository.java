package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.ProblemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemJpaRepository extends JpaRepository<ProblemJpaEntity, Long> {

    @Query("""
            SELECT p.id FROM ProblemJpaEntity p
              LEFT JOIN ProblemSubmissionJpaEntity ps ON ps.problemId = p.id AND ps.userId = :userId
             WHERE p.chapterId = :chapterId
               AND ps.problemId IS NULL
            """)
    List<Long> findCandidateProblemIds(@Param("chapterId") Long chapterId,
                                       @Param("userId") Long userId);
}
