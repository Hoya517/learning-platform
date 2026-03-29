# Diagrams

## Architecture

| 색상 | 분류 | 설명 |
|------|------|------|
| 주황 | Humble | 프레임워크/인프라에 종속 — 단독 테스트 어려움 |
| 청록 | Port (interface) | 레이어 경계. 의존 역전의 기준점 |
| 흰색 | Core (ServiceImpl) | 비즈니스 로직 — 단독 테스트 가능 |
| 연회색 | Domain / Persistence | 도메인 객체, JPA 인프라 |

```mermaid
flowchart LR
    CTRL["Presentation\n(Controller)"]

    subgraph PORT_IN["UseCase Port"]
        SVC_IF["«interface»\nService"]
    end

    subgraph CORE["Core"]
        direction TB
        SVC_IMPL["ServiceImpl"]
        DOM["Domain"]
        SVC_IMPL --> DOM
    end

    subgraph PORT_OUT["Gateway Port"]
        REPO_IF["«interface»\nRepository"]
    end

    REPO_IMPL["Infrastructure\n(RepositoryImpl)"]

    DB[("MySQL\n(JPA)")]

    CTRL      -->  SVC_IF
    SVC_IF    -->  SVC_IMPL
    SVC_IMPL  -->  REPO_IF
    REPO_IF   -->  REPO_IMPL
    REPO_IMPL -->  DB

    classDef humble   fill:#F5A623,stroke:#333,color:#000,font-weight:bold
    classDef port     fill:#2EC4B6,stroke:#333,color:#000
    classDef core     fill:#FFFFFF,stroke:#333,color:#000,font-weight:bold
    classDef domain   fill:#F8F8F8,stroke:#555,color:#333
    classDef db       fill:#E0E0E0,stroke:#888,color:#555

    class CTRL,REPO_IMPL humble
    class SVC_IF,REPO_IF port
    class SVC_IMPL core
    class DOM domain
    class DB db
```

## ERD

JPA `@ManyToOne` 연관관계를 사용하지 않고 `Long` 타입 ID로만 참조합니다. Hibernate FK 제약 조건이 생성되지 않으며, 모든 관계는 애플리케이션 레벨에서 관리됩니다.

| 인덱스 이름 | 테이블 | 컬럼 | 종류 |
|---|---|---|---|
| `idx_problem_chapter` | `problems` | `chapter_id` | INDEX |
| `idx_choice_problem` | `choices` | `problem_id` | INDEX |
| `idx_subjective_problem` | `subjective_answers` | `problem_id` | INDEX |
| `uk_submission_user_problem` | `problem_submissions` | `(user_id, problem_id)` | UNIQUE |
| `idx_submission_choice_submission` | `submission_choices` | `submission_id` | INDEX |
| `uk_problem_statistic_problem_id` | `problem_statistics` | `problem_id` | UNIQUE |

```mermaid
erDiagram
    problems {
        BIGINT id PK
        BIGINT chapter_id
        TEXT content
        TEXT explanation
        VARCHAR type "MULTIPLE_CHOICE | SUBJECTIVE"
        BOOLEAN multiple_answer
        DATETIME created_at
        DATETIME updated_at
    }

    choices {
        BIGINT id PK
        BIGINT problem_id
        INT number
        TEXT content
        BOOLEAN correct
        DATETIME created_at
        DATETIME updated_at
    }

    subjective_answers {
        BIGINT id PK
        BIGINT problem_id
        VARCHAR content
        DATETIME created_at
        DATETIME updated_at
    }

    problem_statistics {
        BIGINT id PK
        BIGINT problem_id UK
        INT total_solved_user_count
        INT correct_solved_user_count
        DATETIME created_at
        DATETIME updated_at
    }

    problem_submissions {
        BIGINT id PK
        BIGINT user_id
        BIGINT problem_id
        VARCHAR answer_status "CORRECT | PARTIAL | WRONG"
        VARCHAR submitted_subjective_answer
        DATETIME created_at
        DATETIME updated_at
    }

    submission_choices {
        BIGINT id PK
        BIGINT submission_id
        INT choice_number
        DATETIME created_at
        DATETIME updated_at
    }

    problems ||--o{ choices : "problem_id (논리 참조)"
    problems ||--o{ subjective_answers : "problem_id (논리 참조)"
    problems ||--o| problem_statistics : "problem_id (논리 참조)"
    problems ||--o{ problem_submissions : "problem_id (논리 참조)"
    problem_submissions ||--o{ submission_choices : "submission_id (논리 참조)"
```

## Domain Model

```mermaid
classDiagram
    direction TB

    class Problem {
        +Long id
        +Long chapterId
        +String content
        +String explanation
        +ProblemType type
        +boolean multipleAnswer
        +List~Choice~ choices
        +List~SubjectiveAnswer~ subjectiveAnswers
        +grade(List~Integer~, String) AnswerStatus
        +getAnswer() ProblemAnswer
    }

    class Choice {
        +Long id
        +int number
        +String content
        +boolean correct
    }

    class SubjectiveAnswer {
        +Long id
        +String content
    }

    class ProblemAnswer {
        <<record>>
        +List~Integer~ correctChoiceNumbers
        +List~String~ correctSubjectiveAnswers
    }

    class ProblemSubmission {
        +Long userId
        +Long problemId
        +AnswerStatus answerStatus
        +List~Integer~ submittedChoiceNumbers
        +String submittedSubjectiveAnswer
    }

    class ProblemStatistic {
        +Long problemId
        +int totalSolvedUserCount
        +int correctSolvedUserCount
        +recordResult(AnswerStatus) void
        +getCorrectRate() Double
    }

    class ProblemType {
        <<enum>>
        MULTIPLE_CHOICE
        SUBJECTIVE
    }

    class AnswerStatus {
        <<enum>>
        CORRECT
        PARTIAL
        WRONG
    }

    Problem "1" --> "*" Choice
    Problem "1" --> "*" SubjectiveAnswer
    Problem ..> ProblemAnswer : creates
    Problem ..> AnswerStatus : grades
    ProblemStatistic ..> AnswerStatus : records
    Problem --> ProblemType
```

## ERD vs 도메인 모델

| DB (ERD) | 도메인 모델                                            | 이유 |
|----------|---------------------------------------------------|------|
| `choices`, `subjective_answers` 별도 테이블 | `Problem.choices`, `Problem.subjectiveAnswers` 필드 | DB는 정규화를 위해 분리하지만, 도메인에서 Problem은 자신의 선택지를 소유 |
| `submission_choices` 테이블 | `ProblemSubmission.submittedChoiceNumbers` 필드     | 제출 선택지는 단순 정수 목록이라 별도 도메인 객체가 불필요. DB에만 정규화된 테이블로 존재 |
| 없음 | `ProblemAnswer` (record)                          | 채점 시점에 조립되는 순간적 결과값. 저장할 필요 없는 순수 도메인 개념 |
| `problem_statistics` 별도 테이블 | `ProblemStatistic` 도메인 객체                         | 정답률 집계 성능을 위해 분리된 테이블. 도메인에서도 독립 객체로 모델링 |
| `type VARCHAR` | `ProblemType` enum                                | DB는 문자열로 저장, 도메인은 타입 안전성을 위해 열거형으로 표현 |
| `answer_status VARCHAR` | `AnswerStatus` enum                               | 위와 동일 |
