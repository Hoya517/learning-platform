package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission_choices", indexes = {
        @Index(name = "idx_submission_choice_submission", columnList = "submission_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionChoiceJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "choice_number", nullable = false)
    private int choiceNumber;

    public SubmissionChoiceJpaEntity(Long submissionId, int choiceNumber) {
        this.submissionId = submissionId;
        this.choiceNumber = choiceNumber;
    }
}
