package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.SubmissionChoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionChoiceJpaRepository extends JpaRepository<SubmissionChoiceJpaEntity, Long> {

    List<SubmissionChoiceJpaEntity> findBySubmissionId(Long submissionId);
}
