# 학습 플랫폼 — 단원별 문제 풀이 API

단원별 문제 풀이 및 풀이 이력 조회 기능을 제공하는 API 서버입니다.
포트&어댑터 구조로 레이어를 분리하고, 도메인 객체에 채점 책임을 위임하는 방식으로 설계했습니다.

## 주요 기능

- 단원 내 랜덤 문제 조회 (풀었던 문제 자동 제외, 스킵 지원)
- 객관식(복수 정답 포함) / 주관식 제출 및 즉시 채점
- 제출 즉시 정답 여부(정답 / 부분정답 / 오답)와 해설 반환
- 문제별 평균 정답률 제공 (30명 이상 풀이 시)

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

## 문서

| 문서 | 내용 |
|------|------|
| [docs/architecture.md](docs/architecture.md) | 레이어 구조, 패키지 구조, 설계 결정 |
| [docs/api.md](docs/api.md) | 엔드포인트 상세, 요청/응답 예시, 에러 코드 |
| [docs/testing.md](docs/testing.md) | 단위/통합/수동 테스트 전략 |
| [docs/diagrams.md](docs/diagrams.md) | 아키텍처 다이어그램, ERD, 도메인 모델 |
| [docs/manual-test-cases.md](docs/manual-test-cases.md) | Swagger 수동 테스트 시나리오 |
