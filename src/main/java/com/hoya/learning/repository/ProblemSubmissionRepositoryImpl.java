package com.hoya.learning.repository;

import com.hoya.learning.domain.ProblemSubmission;
import com.hoya.learning.repository.entity.ProblemSubmissionJpaEntity;
import com.hoya.learning.repository.entity.SubmissionChoiceJpaEntity;
import com.hoya.learning.repository.jpa.ProblemSubmissionJpaRepository;
import com.hoya.learning.repository.jpa.SubmissionChoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProblemSubmissionRepositoryImpl implements ProblemSubmissionRepository {

    private final ProblemSubmissionJpaRepository submissionJpaRepository;
    private final SubmissionChoiceJpaRepository submissionChoiceJpaRepository;

    @Override
    public boolean existsByUserIdAndProblemId(Long userId, Long problemId) {
        return submissionJpaRepository.existsByUserIdAndProblemId(userId, problemId);
    }

    @Override
    public Optional<ProblemSubmission> findByUserIdAndProblemId(Long userId, Long problemId) {
        return submissionJpaRepository.findByUserIdAndProblemId(userId, problemId).map(entity -> {
            List<Integer> choiceNumbers = submissionChoiceJpaRepository.findBySubmissionId(entity.getId()).stream()
                    .map(SubmissionChoiceJpaEntity::getChoiceNumber)
                    .toList();
            return entity.toDomain(choiceNumbers);
        });
    }

    @Override
    public ProblemSubmission save(ProblemSubmission submission) {
        ProblemSubmissionJpaEntity entity = submissionJpaRepository.save(
                new ProblemSubmissionJpaEntity(
                        submission.getUserId(),
                        submission.getProblemId(),
                        submission.getAnswerStatus(),
                        submission.getSubmittedSubjectiveAnswer()
                )
        );

        if (submission.getSubmittedChoiceNumbers() != null) {
            List<SubmissionChoiceJpaEntity> choices = submission.getSubmittedChoiceNumbers().stream()
                    .map(number -> new SubmissionChoiceJpaEntity(entity.getId(), number))
                    .toList();
            submissionChoiceJpaRepository.saveAll(choices);
        }

        return entity.toDomain(submission.getSubmittedChoiceNumbers());
    }
}
