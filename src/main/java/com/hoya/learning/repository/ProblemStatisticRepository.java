package com.hoya.learning.repository;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemStatistic;

import java.util.Optional;

public interface ProblemStatisticRepository {

    Optional<ProblemStatistic> findByProblemId(Long problemId);

    void record(Long problemId, AnswerStatus answerStatus);
}
