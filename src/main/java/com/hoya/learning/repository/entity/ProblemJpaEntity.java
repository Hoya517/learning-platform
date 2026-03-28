package com.hoya.learning.repository.entity;

import com.hoya.learning.common.BaseEntity;
import com.hoya.learning.domain.Choice;
import com.hoya.learning.domain.Problem;
import com.hoya.learning.domain.ProblemType;
import com.hoya.learning.domain.SubjectiveAnswer;
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
@Table(name = "problems", indexes = {
        @Index(name = "idx_problem_chapter", columnList = "chapter_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemType type;

    @Column(name = "multiple_answer", nullable = false)
    private boolean multipleAnswer;

    public Problem toDomain(List<Choice> choices, List<SubjectiveAnswer> subjectiveAnswers) {
        return new Problem(id, chapterId, content, explanation, type, multipleAnswer, choices, subjectiveAnswers);
    }
}
