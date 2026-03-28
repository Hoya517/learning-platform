package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import com.hoya.learning.domain.Choice;
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
@Table(name = "choices", indexes = {
        @Index(name = "idx_choice_problem", columnList = "problem_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean correct;

    public ChoiceJpaEntity(Long problemId, int number, String content, boolean correct) {
        this.problemId = problemId;
        this.number = number;
        this.content = content;
        this.correct = correct;
    }

    public Choice toDomain() {
        return new Choice(id, number, content, correct);
    }
}
