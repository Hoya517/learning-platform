# 학습 플랫폼 — 단원별 문제 풀이 API

단원별 문제 풀이 및 풀이 이력 조회 API 서버입니다.
사용자는 단원을 선택해 랜덤으로 문제를 받고, 풀거나 스킵할 수 있습니다.
객관식(복수 정답 포함) / 주관식을 지원하며, 제출 즉시 정답 여부와 해설을 반환합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA |
| Database | MySQL 8.0 (운영) / H2 (테스트) |
| API Docs | Springdoc OpenAPI 3.0 (Swagger) |
| Infrastructure | Docker Compose |
| Test | JUnit 5 |

---

## 실행 방법

```bash
docker-compose up --build
```

- 앱: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

초기 데이터를 포함한 MySQL이 함께 기동됩니다. 데이터를 초기화하려면:

```bash
docker-compose down -v && docker-compose up --build
```

테스트는 Docker 없이 실행 가능합니다 (H2 인메모리 DB 사용):

```bash
./gradlew test
```

---

## API

모든 API는 `X-User-Id` 헤더로 사용자 ID를 전달합니다.

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/chapters/{chapterId}/problems/random` | 단원 내 랜덤 문제 조회 |
| POST | `/api/problems/{problemId}/submissions` | 문제 제출 |
| GET | `/api/problems/{problemId}/submission` | 풀이 상세 조회 |

상세 명세 및 에러 코드 → [docs/api.md](docs/api.md)

---

## 설계 선택

### 레이어 구조 — 포트&어댑터 패턴 적용

```
Controller → «interface» Service ← ServiceImpl → «interface» Repository ← RepositoryImpl
                                         │
                                         ▼
                                      Domain
```

ServiceImpl은 웹·DB 레이어를 직접 알지 못합니다. 도메인과 서비스는 Spring/JPA 없이 순수 Java로 단위 테스트할 수 있고, 리포지토리 구현체 교체 시 서비스 코드를 건드리지 않습니다.

### 채점 — Tell, Don't Ask

채점 로직을 `Problem.grade()`에 위임합니다. 서비스는 문제 타입을 분기하지 않습니다. 타입이 추가돼도 서비스 코드는 수정되지 않습니다.

### 정답률 — 집계 테이블 분리

제출 시점에 `problem_statistics` 테이블을 누적 갱신합니다. 조회마다 `problem_submissions`를 집계하면 데이터 증가 시 성능이 저하되므로 집계 결과를 미리 유지합니다.

### 랜덤 조회 — 앱 레벨 랜덤 선택

`ORDER BY RAND()` 대신 후보 ID 목록을 조회한 뒤 애플리케이션에서 랜덤 선택합니다. `ORDER BY RAND()`는 데이터 증가 시 전체 정렬 비용이 발생하고, 단원 내 문제 수가 적어 메모리 처리 비용이 무시할 수준입니다.

### 스킵 — stateless 처리

스킵 상태를 서버에 저장하지 않고 클라이언트가 `excludeProblemId`로 전달합니다. 스킵은 저장할 이력이 아닌 일시적 의사이므로 별도 API와 상태 테이블이 불필요합니다.

### 단일 모듈 선택

멀티 모듈 도입을 검토했으나 현재 규모에서 세팅 비용 대비 실익이 없다고 판단했습니다. 계층형 패키지(`domain / repository / service / presentation`)로 레이어 책임을 분리했으며, 확장 시 동일한 구조가 멀티 모듈로 자연스럽게 매핑됩니다. 자세한 근거는 [docs/architecture.md](docs/architecture.md)를 참고합니다.

---

## 문서

| 문서 | 내용 |
|------|------|
| [docs/architecture.md](docs/architecture.md) | 레이어 구조, 패키지 구조, 설계 결정 |
| [docs/api.md](docs/api.md) | 엔드포인트 상세, 요청/응답 예시, 에러 코드 |
| [docs/testing.md](docs/testing.md) | 단위/통합/수동 테스트 전략 |
| [docs/diagrams.md](docs/diagrams.md) | 아키텍처 다이어그램, ERD, 도메인 모델 |
| [docs/manual-test-cases.md](docs/manual-test-cases.md) | Swagger 수동 테스트 시나리오 |
