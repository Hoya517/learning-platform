package com.hoya.learning.repository;

import com.hoya.learning.domain.Choice;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.SubjectiveAnswer;
import com.hoya.learning.repository.entity.ChoiceJpaEntity;
import com.hoya.learning.repository.entity.SubjectiveAnswerJpaEntity;
import com.hoya.learning.repository.jpa.ChoiceJpaRepository;
import com.hoya.learning.repository.jpa.ProblemJpaRepository;
import com.hoya.learning.repository.jpa.SubjectiveAnswerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProblemRepositoryImpl implements ProblemRepository {

    private final ProblemJpaRepository problemJpaRepository;
    private final ChoiceJpaRepository choiceJpaRepository;
    private final SubjectiveAnswerJpaRepository subjectiveAnswerJpaRepository;

    @Override
    public Optional<Problem> findById(Long id) {
        return problemJpaRepository.findById(id).map(entity -> {
            List<Choice> choices = entity.getType().isMultipleChoice()
                    ? choiceJpaRepository.findByProblemId(id).stream().map(ChoiceJpaEntity::toDomain).toList()
                    : List.of();
            List<SubjectiveAnswer> subjectiveAnswers = entity.getType().isSubjective()
                    ? subjectiveAnswerJpaRepository.findByProblemId(id).stream().map(SubjectiveAnswerJpaEntity::toDomain).toList()
                    : List.of();
            return entity.toDomain(choices, subjectiveAnswers);
        });
    }

    @Override
    public List<Long> findCandidateProblemIds(Long chapterId, Long userId) {
        return problemJpaRepository.findCandidateProblemIds(chapterId, userId);
    }
}
