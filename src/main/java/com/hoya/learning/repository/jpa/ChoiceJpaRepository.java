package com.hoya.learning.repository.jpa;

import com.hoya.learning.repository.entity.ChoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceJpaRepository extends JpaRepository<ChoiceJpaEntity, Long> {

    List<ChoiceJpaEntity> findByProblemId(Long problemId);
}
