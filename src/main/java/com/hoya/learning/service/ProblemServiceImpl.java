package com.hoya.learning.service;

import com.hoya.learning.common.RandomHolder;
import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.repository.ProblemRepository;
import com.hoya.learning.repository.ProblemStatisticRepository;
import com.hoya.learning.service.command.GetRandomProblemCommand;
import com.hoya.learning.service.result.RandomProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemStatisticRepository problemStatisticRepository;
    private final RandomHolder randomHolder;

    @Override
    public RandomProblem getRandomProblem(GetRandomProblemCommand command) {
        List<Long> candidateIds = new ArrayList<>(
                problemRepository.findCandidateProblemIds(command.chapterId(), command.userId())
        );

        if (command.excludeProblemId() != null) {
            candidateIds.removeIf(id -> id.equals(command.excludeProblemId()));
        }

        if (candidateIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_AVAILABLE_PROBLEM);
        }

        Problem problem = problemRepository
                .findById(candidateIds.get(randomHolder.nextInt(candidateIds.size())))
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        Double correctRate = problemStatisticRepository.findByProblemId(problem.getId())
                .map(ProblemStatistic::getCorrectRate)
                .orElse(null);

        List<RandomProblem.ChoiceInfo> choices = problem.getChoices().stream()
                .map(c -> new RandomProblem.ChoiceInfo(c.getNumber(), c.getContent()))
                .toList();

        return new RandomProblem(problem.getId(), problem.getContent(), problem.getType(),
                problem.isMultipleAnswer(), choices, correctRate);
    }
}
