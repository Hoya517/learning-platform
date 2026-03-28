package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.SubjectiveAnswerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectiveAnswerJpaRepository extends JpaRepository<SubjectiveAnswerJpaEntity, Long> {

    List<SubjectiveAnswerJpaEntity> findByProblemId(Long problemId);
}
