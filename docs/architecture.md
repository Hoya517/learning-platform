# 아키텍처

## 레이어 구조

Controller와 RepositoryImpl은 **인터페이스에만 의존**합니다. ServiceImpl은 웹·DB 레이어를 직접 알지 못하며, Domain과 함께 Spring·JPA 없이 순수 Java로 단위 테스트할 수 있습니다.

```
Controller ──→ «interface» Service ←── ServiceImpl ──→ «interface» Repository ←── RepositoryImpl
                                              │
                                              ▼
                                           Domain
```

상세 다이어그램은 [diagrams.md#architecture](diagrams.md#architecture)를 참고합니다.

## 패키지 구조

```
src/main/java/com/hoya/learning
├── common
│   ├── config          # JPA Auditing, Swagger UI 설정
│   ├── exception       # 비즈니스 예외, 에러 코드 정의, 전역 예외 핸들러
│   └── response        # 공통 API 응답 / 에러 응답 래퍼
├── domain              # 핵심 도메인 엔티티, 열거형, 채점 로직
├── repository          # 도메인 리포지토리 인터페이스 + 구현체
│   ├── entity          # JPA 영속성 엔티티 (도메인 객체와 분리)
│   └── jpa             # Spring Data JPA 인터페이스
├── service             # 비즈니스 유스케이스 (인터페이스 + 구현체)
│   ├── command         # 서비스 호출 입력 파라미터 객체
│   └── result          # 서비스 호출 출력 결과 객체
└── presentation        # REST 컨트롤러
    ├── request         # 요청 DTO
    └── response        # 응답 DTO
```

ERD와 도메인 모델 다이어그램은 [diagrams.md](diagrams.md)를 참고합니다.

## 단일 모듈 선택 이유

멀티 모듈 도입을 검토했으나 아래 이유로 단일 모듈을 선택했습니다.

**1. 계층형 패키지 구조로 레이어 책임 분리**
기술 레이어 기준(`domain / repository / service / presentation`)으로 패키지를 나눠 각 레이어의 책임을 명확히 했습니다. 도메인을 순수 Java로 작성해 Spring 없이 단위 테스트할 수 있으며, 이 구조는 확장 시 계층형 멀티 모듈(`domain-module / application-module / infrastructure-module / presentation-module`)로 자연스럽게 매핑됩니다.

**2. 도메인형 멀티 모듈은 팀·규모가 커질 때 의미 있음**
도메인형 멀티 모듈(`user-module`, `problem-module` 등)은 각 모듈이 자체 레이어를 내부에 갖는 수직 분리 방식입니다. 팀이 커져 팀별로 도메인을 소유하거나, MSA 전환 전 모듈형 모놀리식(Modular Monolith) 단계로 도메인을 물리적으로 격리할 때 진가를 발휘합니다. 현재는 단일 개발자·단일 도메인 구조이므로 해당 장점이 없습니다.

---

## 도메인 정책

비즈니스 규칙의 근거와 해석입니다. 구현 방식이 아닌 "왜 이 정책인가"에 대한 설명입니다.

### 채점 결과 — CORRECT / PARTIAL / WRONG
- **정책:** 객관식은 정답 선택지 1개 이상 포함 시 PARTIAL, 주관식은 CORRECT / WRONG만 판정
- **이유:** 완전히 틀린 것과 일부 맞힌 것을 같은 오답으로 처리하면 사용자에게 정보가 없어짐. 주관식은 부분 일치 기준을 정의하기 어려워 이분 판정이 적합
- **구현:** 채점 로직은 `Problem.grade()`에 위임. 서비스는 타입을 직접 판단하지 않음 (Tell, Don't Ask)

### 정답률 — 30명 이상 풀이 시에만 공개
- **정책:** 30명 미만이면 정답률을 노출하지 않음. PARTIAL은 오답으로 간주
- **이유:** 표본이 적으면 정답률이 의미 없는 숫자가 됨. 첫 제출자가 틀리면 0%, 맞히면 100%인 상황을 방지. PARTIAL을 정답으로 허용하면 실력을 과대 반영
- **구현:** 제출 시점에 `problem_statistics` 테이블을 누적 갱신. 동시 제출이 몰리면 lost update 가능성이 있으나 현재 규모에서는 감수 가능한 수준. 트래픽이 늘어 정확도가 중요해지면 `@Version` 기반 낙관적 락으로 교체를 고려

### 스킵 — stateless 처리
- **정책:** 스킵 상태를 서버에서 관리하지 않고 클라이언트가 `excludeProblemId`로 전달
- **이유:** 스킵은 "지금 이 문제를 보기 싫다"는 일시적 의사이지 저장할 이력이 아님. 별도 스킵 API 및 상태 저장 테이블이 불필요
- **확장 고려:** 세션 간 스킵 이력을 유지해야 한다면 Redis로 사용자별 제외 목록을 관리하는 구조를 고려할 수 있음. 현재 요구사항에서는 불필요

---

## 설계 결정

구현 수준의 기술적 트레이드오프입니다.

### 핵심 결정

#### 랜덤 조회 — 앱 레벨 랜덤 선택
- **결정:** 후보 ID 목록 조회 후 애플리케이션에서 `Random`으로 랜덤 선택
- **이유:** `ORDER BY RAND()`는 데이터 증가 시 전체 테이블 정렬 비용 발생. 단원 내 문제 수가 적어 메모리 처리 비용이 무시할 수준. `excludeProblemId` 필터도 DB에서 처리하면 인덱스를 제대로 활용하기 어려움
- **트레이드오프:** 문제 수가 수만 건 이상이면 ID 목록 조회 자체가 부담 → 그 시점에 커서 페이징 또는 샘플링 쿼리 검토

#### 정답률 — 집계 테이블 분리
- **결정:** 제출 시점에 `problem_statistics` 테이블을 갱신(누적 카운트)
- **이유:** 조회마다 `problem_submissions`를 집계하면 데이터 증가 시 성능 저하. `@Modifying` 벌크 쿼리는 영속성 컨텍스트를 우회하므로 리포지토리 내부에서 SELECT → 도메인 로직 → 명시적 `save()` 흐름으로 처리
- **트레이드오프:** 동일 문제에 동시 제출이 몰리면 lost update 가능성 있음. 확장 시 `@Version` 기반 낙관적 락 또는 DB atomic update 쿼리 적용 고려

#### JPA 연관관계 미사용
- **결정:** JPA 엔티티 간 `@ManyToOne` 등 연관관계 없이 `Long` 컬럼으로만 참조
- **이유:** 어떤 데이터를 언제 로딩할지 완전히 제어. Hibernate 프록시·지연 로딩 없이 리포지토리 구현체가 명시적으로 조립
- **트레이드오프:** 연관 데이터 조회 코드가 늘어남. 대신 N+1 문제나 의도치 않은 즉시 로딩이 발생하지 않음

### 기타 결정 요약

| 결정 | 내용 |
|------|------|
| 중복 제출 방어 | 선행 체크(`existsByUserIdAndProblemId`) + DB unique 제약 이중 방어. 동시 요청 충돌 시 409 반환 |
| chapter_id 미중복 저장 | `Problem`이 이미 `chapter_id`를 가지므로 `Submission`에 미포함. chapter 기준 조회는 JOIN으로 처리 |
| userId 전달 | `X-User-Id` 헤더로 전달. 추후 인증 필터에서 토큰 파싱 후 동일 헤더로 주입하는 구조로 교체 가능 |
