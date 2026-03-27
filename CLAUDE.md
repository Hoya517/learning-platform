# CLAUDE.md

이 파일은 Claude Code가 본 프로젝트의 설계 의도와 컨벤션을 이해하기 위한 컨텍스트 문서입니다.

---

## 1. 프로젝트 개요

학습 플랫폼의 핵심 기능인 **단원별 문제 풀이** 및 **풀이 이력 조회** API 서버입니다.

- 사용자는 단원(chapter)을 선택해 랜덤으로 문제를 받고, 풀거나 스킵할 수 있습니다.
- 객관식(복수 정답 포함) / 주관식 문제를 지원합니다.
- 제출 즉시 정답 여부(정답 / 부분정답 / 오답)와 해설이 반환됩니다.
- 문제별 평균 정답률을 제공합니다 (30명 이상 풀이 시에만).

---

## 2. 기술 스택

- Java 21
- Spring Boot 3.4.x
- Spring Data JPA
- MySQL 8.0
- Springdoc OpenAPI (Swagger)
- Docker Compose
- JUnit 5 + H2 (테스트)

---

## 3. 패키지 구조

```
src/main/java/com/example/learning
├── common
│   ├── config          # JpaConfig, SwaggerConfig
│   ├── exception       # BusinessException, ErrorCode, GlobalExceptionHandler
│   ├── response        # ApiResponse<T>, ErrorResponse
│   └── util            # AnswerNormalizer
├── chapter
│   ├── domain
│   └── repository
├── user
│   ├── domain
│   └── repository
├── problem
│   ├── domain          # Problem, ProblemType, Choice, SubjectiveAnswer
│   ├── dto
│   ├── repository
│   ├── service         # ProblemQueryService, ProblemSkipService
│   └── presentation    # ProblemController
├── submission
│   ├── domain          # ProblemSubmission, AnswerStatus, SubmissionChoiceAnswer, SubmissionSubjectiveAnswer
│   ├── dto
│   ├── repository
│   ├── service         # ProblemSubmissionService, ProblemSolveDetailService, AnswerGradingService
│   └── presentation    # SubmissionController
├── statistics
│   ├── domain          # ProblemStatistic
│   ├── repository
│   └── service         # ProblemStatisticService
└── skipstate
    ├── domain          # UserChapterSkipState
    ├── repository
    └── service         # UserChapterSkipStateService
```

---

## 4. 도메인 설계 규칙

### 엔티티 공통
- 모든 엔티티는 `BaseTimeEntity`를 상속합니다 (`createdAt`, `updatedAt`).
- 기본 생성자는 `protected`로 막고, 정적 팩토리 메서드 또는 패키지 접근 생성자를 사용합니다.
- Setter를 열지 않습니다. 상태 변경은 도메인 메서드로만 합니다.

### ID
- 모든 PK는 `Long` 타입, `GenerationType.IDENTITY` (Auto Increment) 를 사용합니다.
- UUID는 사용하지 않습니다. (단일 MySQL 환경에서 성능 이점 없음)

### 연관관계
- 모든 `@ManyToOne`은 `FetchType.LAZY`를 사용합니다.
- 양방향 연관관계는 필요한 경우에만 추가합니다. 기본은 단방향입니다.

---

## 5. 주요 설계 결정 사항

### 5-1. ProblemSubmission에 chapter_id 미포함
`Problem`이 이미 `Chapter`를 참조하므로 `ProblemSubmission`에 `chapter_id`를 중복 저장하지 않습니다.
chapter 기준 조회가 필요한 경우 `problem` JOIN으로 처리합니다.

```sql
SELECT ps.*
FROM problem_submissions ps
JOIN problems p ON p.id = ps.problem_id
WHERE ps.user_id = ?
  AND p.chapter_id = ?
```

### 5-2. UserChapterSkipState 네이밍
직전에 스킵한 문제 1건만 유지하는 구조이므로 `History`(로그성)가 아닌 `State`(현재 상태)로 명명했습니다.

```
UserChapterSkipState
- user_id
- chapter_id
- skipped_problem_id
- skipped_at
```

user + chapter 기준으로 unique 제약을 겁니다 (upsert 방식).

### 5-3. 스킵 API 분리
스킵 저장과 다음 문제 조회를 별도 API로 분리했습니다.

```
POST /api/problems/{problemId}/skip     # 스킵 상태 저장만
GET  /api/chapters/{chapterId}/problems/random?userId=  # 다음 문제 조회 (기존 API 재사용)
```

**장점:** API 책임 명확, 기존 랜덤 조회 API 재사용 가능  
**트레이드오프:** 두 요청 사이의 원자성이 보장되지 않습니다. 단, 통합형 API도 응답 수신 후 클라이언트 렌더링 전에 상태가 바뀔 수 있으므로 완전한 원자성은 어느 설계에서도 보장되지 않습니다. 본 과제에서는 책임 분리와 재사용성을 우선했습니다.

### 5-4. ORDER BY RAND() 미사용
MySQL의 `ORDER BY RAND()`는 데이터 양이 증가할수록 성능 저하가 큽니다.  
대신 조건을 만족하는 후보 문제 ID 목록을 먼저 조회하고, 애플리케이션 레벨에서 랜덤 선택합니다.

```java
List<Long> candidateIds = problemRepository.findCandidateProblemIds(...);
Long selectedId = candidateIds.get(random.nextInt(candidateIds.size()));
```

### 5-5. 정답률 집계 테이블 분리
조회 시마다 `problem_submissions`를 집계하면 데이터 증가 시 성능 저하가 발생합니다.  
제출 시점에 `problem_statistics` 테이블을 갱신(누적 카운트)하는 방식으로 조회 성능을 확보했습니다.

- `total_solved_user_count`: 문제를 푼 사용자 수 누적
- `correct_solved_user_count`: 정답(CORRECT)만 포함, 부분정답은 오답으로 간주

**동시성 이슈:** 동일 문제에 동시 제출이 몰리면 lost update 가능성이 있습니다.  
현재는 단순 누적 방식으로 구현하며, 확장 시 `@Version` 기반 낙관적 락 또는 DB atomic update 쿼리 적용을 고려합니다.

### 5-6. 채점 전략 패턴 (AnswerGrader)
객관식/주관식 채점 로직을 `AnswerGrader` 인터페이스로 추상화하고 구현체를 분리합니다.  
새로운 문제 타입 추가 시 기존 코드를 수정하지 않고 구현체만 추가할 수 있습니다.

```java
public interface AnswerGrader {
    boolean supports(ProblemType problemType);
    AnswerStatus grade(Problem problem, SubmitProblemRequest request);
    ProblemAnswerDto getProblemAnswers(Problem problem);
}
```

### 5-7. 부분정답 정책
- 객관식: 정답 선택지를 1개라도 포함하면 부분정답 (`[1,2]` 정답 시 `[1,3]` 제출 → 부분정답)
- 주관식: 부분정답 기준이 불명확하므로 정답/오답만 판정
- 정답률 계산에서 부분정답은 오답으로 간주

### 5-8. 중복 제출 방지
사용자당 동일 문제는 1회만 제출 가능합니다.  
`problem_submissions(user_id, problem_id)` unique 제약으로 보장합니다.

---

## 6. API 명세

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/chapters/{chapterId}/problems/random?userId=` | 랜덤 문제 조회 |
| POST | `/api/problems/{problemId}/skip` | 문제 스킵 |
| POST | `/api/problems/{problemId}/submissions` | 문제 제출 |
| GET | `/api/users/{userId}/problems/{problemId}` | 풀이 상세 조회 |

---

## 7. 인덱스 설계

```sql
-- 단원별 문제 조회
CREATE INDEX idx_problem_chapter ON problems(chapter_id);

-- 제출 이력 조회 + 중복 제출 방지
CREATE UNIQUE INDEX uk_submission_user_problem ON problem_submissions(user_id, problem_id);

-- 스킵 상태 upsert
CREATE UNIQUE INDEX uk_skip_state_user_chapter ON user_chapter_skip_states(user_id, chapter_id);

-- 선택지 조회
CREATE INDEX idx_choice_problem ON choices(problem_id);

-- 주관식 정답 조회
CREATE INDEX idx_subjective_problem ON subjective_answers(problem_id);
```

---

## 8. 예외 처리

모든 비즈니스 예외는 `BusinessException(ErrorCode)`로 던집니다.  
`GlobalExceptionHandler`에서 `ErrorResponse`로 통일해 응답합니다.

```json
{
  "code": "NO_AVAILABLE_PROBLEM",
  "message": "더 이상 제공할 문제가 없습니다."
}
```

주요 ErrorCode:

| Code | Status | 설명 |
|------|--------|------|
| `USER_NOT_FOUND` | 404 | 사용자 없음 |
| `CHAPTER_NOT_FOUND` | 404 | 단원 없음 |
| `PROBLEM_NOT_FOUND` | 404 | 문제 없음 |
| `SUBMISSION_NOT_FOUND` | 404 | 풀이 이력 없음 |
| `NO_AVAILABLE_PROBLEM` | 409 | 제공할 문제 없음 |
| `ALREADY_SUBMITTED_PROBLEM` | 409 | 이미 제출한 문제 |
| `INVALID_ANSWER_TYPE` | 400 | 문제/답안 타입 불일치 |

---

## 9. 테스트 전략

### 단위 테스트 (Spring 없이)
- `MultipleChoiceAnswerGraderTest`: 정답/부분정답/오답 케이스
- `SubjectiveAnswerGraderTest`: trim, 대소문자 무시, 복수 정답 후보
- `ProblemStatisticTest`: 30명 미만 null, 반올림, 부분정답 오답 간주

### 통합 테스트 (H2 + @SpringBootTest)
- 랜덤 문제 조회: 안 푼 문제만, 직전 스킵 제외, 후보 없을 때 예외
- 문제 제출: 결과 반환, DB 저장, 통계 갱신, 중복 제출 예외
- 풀이 상세 조회: 답안/정답/정답률 모두 반환

---

## 10. 코딩 컨벤션

- 서비스는 인터페이스 없이 구현 클래스 직접 사용 (과제 범위, 불필요한 추상화 지양)
- `@Transactional(readOnly = true)` 조회 메서드에 적용
- DTO는 record 또는 `@Getter` + `@NoArgsConstructor` Lombok 사용
- 응답은 항상 `ApiResponse<T>`로 감싸서 반환
- 컨트롤러는 얇게 유지 (검증 + 서비스 호출 + 응답 반환만)
