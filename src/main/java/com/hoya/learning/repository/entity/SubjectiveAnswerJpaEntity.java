package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import com.hoya.learning.domain.SubjectiveAnswer;
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
@Table(name = "subjective_answers", indexes = {
        @Index(name = "idx_subjective_problem", columnList = "problem_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectiveAnswerJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private String content;

    public SubjectiveAnswerJpaEntity(Long problemId, String content) {
        this.problemId = problemId;
        this.content = content;
    }

    public SubjectiveAnswer toDomain() {
        return new SubjectiveAnswer(id, content);
    }
}
