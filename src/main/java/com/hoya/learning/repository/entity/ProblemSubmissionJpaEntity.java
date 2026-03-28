package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "problem_submissions", indexes = {
        @Index(name = "uk_submission_user_problem", columnList = "user_id, problem_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemSubmissionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_status", nullable = false)
    private AnswerStatus answerStatus;

    @Column(name = "submitted_subjective_answer")
    private String submittedSubjectiveAnswer;

    public ProblemSubmissionJpaEntity(Long userId, Long problemId, AnswerStatus answerStatus,
                                      String submittedSubjectiveAnswer) {
        this.userId = userId;
        this.problemId = problemId;
        this.answerStatus = answerStatus;
        this.submittedSubjectiveAnswer = submittedSubjectiveAnswer;
    }

    public ProblemSubmission toDomain(List<Integer> submittedChoiceNumbers) {
        return new ProblemSubmission(userId, problemId, answerStatus, submittedChoiceNumbers, submittedSubjectiveAnswer);
    }
}
