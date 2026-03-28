package com.hoya.learning.service;

import com.hoya.learning.common.exception.BusinessException;
import com.hoya.learning.common.exception.ErrorCode;
import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemStatistic;
import com.hoya.learning.domain.ProblemSubmission;
import com.hoya.learning.repository.ProblemRepository;
import com.hoya.learning.repository.ProblemStatisticRepository;
import com.hoya.learning.repository.ProblemSubmissionRepository;
import com.hoya.learning.service.command.GetSolveDetailCommand;
import com.hoya.learning.service.command.SubmitProblemCommand;
import com.hoya.learning.service.result.ProblemSolveDetail;
import com.hoya.learning.service.result.SubmissionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {

    private final ProblemRepository problemRepository;
    private final ProblemSubmissionRepository submissionRepository;
    private final ProblemStatisticRepository statisticRepository;

    @Override
    @Transactional
    public SubmissionResult submit(SubmitProblemCommand command) {
        if (submissionRepository.existsByUserIdAndProblemId(command.userId(), command.problemId())) {
            throw new BusinessException(ErrorCode.ALREADY_SUBMITTED_PROBLEM);
        }

        Problem problem = problemRepository.findById(command.problemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        AnswerStatus answerStatus = problem.grade(command.choiceNumbers(), command.subjectiveAnswer());

        statisticRepository.record(problem.getId(), answerStatus);
        submissionRepository.save(command.toSubmission(answerStatus));

        return new SubmissionResult(problem.getId(), answerStatus, problem.getExplanation(), problem.getAnswer());
    }

    @Override
    public ProblemSolveDetail getSolveDetail(GetSolveDetailCommand command) {
        Problem problem = problemRepository.findById(command.problemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        ProblemSubmission submission = submissionRepository
                .findByUserIdAndProblemId(command.userId(), command.problemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND));

        Double correctRate = statisticRepository.findByProblemId(command.problemId())
                .map(ProblemStatistic::getCorrectRate)
                .orElse(null);

        return new ProblemSolveDetail(
                problem.getId(), submission.getAnswerStatus(), problem.getExplanation(), problem.getAnswer(),
                submission.getSubmittedChoiceNumbers(), submission.getSubmittedSubjectiveAnswer(),
                correctRate
        );
    }
}
