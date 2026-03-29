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
- Spring Boot 4.0.5
- Spring Data JPA
- MySQL 8.0
- Springdoc OpenAPI 3.0.0 (Swagger)
- Docker Compose
- JUnit 5 + H2 (테스트)

---

## 3. 패키지 구조

```
src/main/java/com/hoya/learning
├── common                  # 레이어 공통 인프라 (BaseEntity, RandomHolder 포함)
│   ├── config              # JPA Auditing, Swagger UI 설정
│   ├── exception           # BusinessException, ErrorCode, 전역 예외 핸들러
│   └── response            # ApiResponse / ErrorResponse 공통 응답 래퍼
├── domain                  # 핵심 도메인 객체 — 엔티티, 열거형, 채점 로직
├── repository              # 도메인 리포지토리 인터페이스 + 구현체
│   ├── entity              # JPA 영속성 엔티티 (도메인 객체와 분리)
│   └── jpa                 # Spring Data JPA 인터페이스
├── service                 # 비즈니스 유스케이스 (인터페이스 + 구현체)
│   ├── command             # 서비스 호출 입력 파라미터 객체
│   └── result              # 서비스 호출 출력 결과 객체
└── presentation            # REST 컨트롤러
    ├── request             # 요청 DTO
    └── response            # 응답 DTO
```

---

## 4. 도메인 설계 규칙

### 엔티티 공통
- 모든 엔티티는 `BaseEntity`를 상속합니다 (`createdAt`, `updatedAt`).
- 기본 생성자는 `protected`로 막고, 정적 팩토리 메서드 또는 패키지 접근 생성자를 사용합니다.
- Setter를 열지 않습니다. 상태 변경은 도메인 메서드로만 합니다.

### ID
- 모든 PK는 `Long` 타입, `GenerationType.IDENTITY` (Auto Increment) 를 사용합니다.
- UUID는 사용하지 않습니다. (단일 MySQL 환경에서 성능 이점 없음)

### 연관관계
- JPA 엔티티 간 `@ManyToOne` 등 JPA 연관관계를 사용하지 않습니다.
- 연관 테이블은 `Long` 컬럼(e.g. `problemId`, `chapterId`)으로만 참조합니다.
- 따라서 Hibernate가 FK 제약 조건을 생성하지 않습니다.
- 연관 데이터 조회와 도메인 객체 조립은 리포지토리 구현체가 명시적으로 처리합니다. JPA 프록시 없이 어떤 데이터를 언제 로딩할지 완전히 제어하기 위함입니다.

---

## 5. 주요 설계 결정

### 5-1. ProblemSubmission에 chapter_id 미포함
`Problem`이 이미 `chapter_id`를 가지므로 중복 저장하지 않습니다.
chapter 기준 조회는 `problem` JOIN으로 처리합니다.

### 5-2. 스킵 stateless 처리
스킵 상태를 서버에서 관리하지 않고, 클라이언트가 `excludeProblemId` 쿼리 파라미터로 직접 전달합니다.
별도 스킵 API와 상태 저장 테이블이 불필요합니다.

### 5-3. 랜덤 선택 — ORDER BY RAND() 미사용
후보 ID 목록을 먼저 조회하고 애플리케이션 레벨에서 랜덤 선택합니다.
`ORDER BY RAND()`는 데이터 증가 시 성능이 급격히 저하되며, 단원 내 문제 수가 적어 메모리 필터링 비용이 무시할 수준입니다.

### 5-4. 정답률 — 집계 테이블 분리
제출 시점에 `problem_statistics` 테이블을 갱신해 조회 성능을 확보합니다.
조회마다 `problem_submissions`를 집계하면 데이터 증가 시 성능이 저하되기 때문입니다.
> **동시성 주의:** 동일 문제 동시 제출 시 lost update 가능성 있음. 확장 시 낙관적 락 또는 atomic update 적용 고려.

### 5-5. 채점 책임 — Tell, Don't Ask
채점 로직은 `Problem.grade()`에 위임합니다. 서비스는 문제 타입을 직접 판단하지 않습니다.

### 5-6. 부분정답 정책
- 객관식: 정답 선택지 1개라도 포함 시 PARTIAL
- 주관식: 부분정답 없이 CORRECT / WRONG만 판정
- 정답률 계산에서 PARTIAL은 오답으로 간주

### 5-7. 중복 제출 방지 — 이중 방어
`existsByUserIdAndProblemId()` 선행 체크 + DB unique 제약으로 이중 방어합니다.
동시 요청이 선행 체크를 통과해도 `DataIntegrityViolationException`을 409로 변환합니다.

### 5-8. userId 인증 구조
`X-User-Id` 헤더로 전달받습니다. JWT 도입 시 인증 필터에서 동일 헤더로 주입하는 구조로 교체하면 됩니다.
현재는 클라이언트가 직접 설정하므로 사칭 가능 — 실 서비스에서는 서버가 주입해야 합니다.

### 5-9. 주관식 정답 비교 정책
- 대소문자 무시 (`toLowerCase`)
- 앞뒤 공백 무시 (`trim`)
- 주관식 문제 제출 시 `choiceNumbers`가 함께 오면 400 (`INVALID_ANSWER_TYPE`) — 타입 혼용 요청은 클라이언트 버그로 간주

---

## 6. API 명세

모든 API는 `X-User-Id` 헤더로 사용자 ID를 전달합니다.

| Method | URL | 상태코드 | 설명 |
|--------|-----|----------|------|
| GET | `/api/chapters/{chapterId}/problems/random?excludeProblemId=` | 200 | 랜덤 문제 조회 |
| POST | `/api/problems/{problemId}/submissions` | 201 | 문제 제출 |
| GET | `/api/problems/{problemId}/submission` | 200 | 풀이 상세 조회 |

---

## 7. 인덱스 설계

```sql
-- 단원별 문제 조회
CREATE INDEX idx_problem_chapter ON problems(chapter_id);

-- 제출 이력 조회 + 중복 제출 방지
CREATE UNIQUE INDEX uk_submission_user_problem ON problem_submissions(user_id, problem_id);

-- 선택지 조회
CREATE INDEX idx_choice_problem ON choices(problem_id);

-- 주관식 정답 조회
CREATE INDEX idx_subjective_problem ON subjective_answers(problem_id);

-- 제출 선택지 조회
CREATE INDEX idx_submission_choice_submission ON submission_choices(submission_id);
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
| `NO_AVAILABLE_PROBLEM` | 404 | 조건에 맞는 문제 없음 (모두 풀었거나 제외 후 후보 없음) |
| `ALREADY_SUBMITTED_PROBLEM` | 409 | 이미 제출한 문제 |
| `INVALID_ANSWER_TYPE` | 400 | 문제/답안 타입 불일치 |
| `UNSUPPORTED_PROBLEM_TYPE` | 500 | 미지원 문제 유형 (도달 불가 방어 코드) |

---

## 9. 테스트 전략

### 단위 테스트 (Spring 없이)
- `ProblemTest`: gradeChoice 정답/부분정답/오답, gradeSubjective 대소문자/공백 무시
- `ProblemStatisticTest`: 30명 미만 null, 반올림, 부분정답 오답 간주
- 서비스 단위 테스트: InMemory Repository로 Spring 없이 검증

### 통합 테스트 (H2 + @SpringBootTest)

> MySQL 대신 H2를 사용하는 이유:
> 테스트 환경에서 MySQL 컨테이너를 띄우면 CI 속도가 느려지고 환경 의존성이 생깁니다.
> 이 프로젝트는 `ORDER BY RAND()` 같은 MySQL 전용 문법을 사용하지 않고, 네이티브 쿼리도 없으므로
> H2의 MySQL 호환 모드(`MODE=MySQL`)로 동일한 동작을 보장할 수 있습니다.
> Testcontainers 도입 시 실 DB 환경에서의 검증도 가능하지만, 현재 규모에서는 H2로 충분합니다.

- 랜덤 문제 조회: 안 푼 문제만, excludeProblemId 제외, 후보 없을 때 예외
- 문제 제출: 결과 반환, DB 저장, 통계 갱신, 중복 제출 예외
- 풀이 상세 조회: 답안/정답/정답률 모두 반환

### 수동 테스트

Swagger UI를 통한 수동 테스트 시나리오는 [docs/manual-test-cases.md](docs/manual-test-cases.md)를 참고합니다.

---

## 10. 코딩 컨벤션

- 서비스는 인터페이스 + 구현 클래스 분리
- 트랜잭션: 클래스 레벨에 `@Transactional(readOnly = true)`, 쓰기 메서드만 `@Transactional`로 override
- DTO는 record 또는 `@Getter` + `@NoArgsConstructor` Lombok 사용
- 응답은 항상 `ApiResponse<T>`로 감싸서 반환
- 컨트롤러는 얇게 유지 (검증 + 서비스 호출 + 응답 반환만)
