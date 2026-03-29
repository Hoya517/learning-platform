# 테스트 전략

## 단위 테스트 (Spring 없이)

도메인 로직과 서비스 로직을 Spring 컨텍스트 없이 검증합니다.

- `ProblemTest`: 객관식/주관식 채점 로직 (정답, 부분정답, 오답, 대소문자/공백 무시)
- `ProblemStatisticTest`: 정답률 계산 (30명 미만 null, 반올림, 부분정답 오답 처리)
- `ProblemServiceTest`: InMemory Repository로 Spring 없이 서비스 로직 검증

## 통합 테스트 (H2 + @SpringBootTest)

> **MySQL 대신 H2를 사용하는 이유**
> Testcontainers로 MySQL을 사용하면 테스트 실행 시 Docker가 필수이고, 첫 실행 시 컨테이너 기동으로 시작이 느려집니다.
> 이 프로젝트는 `ORDER BY RAND()` 같은 MySQL 전용 문법과 네이티브 쿼리를 사용하지 않으므로 H2의 MySQL 호환 모드(`MODE=MySQL`)로 동일한 동작을 보장할 수 있습니다.

검증 항목:

- 랜덤 문제 조회: 안 푼 문제만 반환, `excludeProblemId` 제외, 후보 없을 때 예외
- 문제 제출: 결과 반환, DB 저장, 통계 갱신, 중복 제출 예외, 입력 검증
- 풀이 상세 조회: 답안/정답/정답률 모두 반환

## 수동 테스트 (Swagger)

시나리오별 요청/응답 체크리스트는 [manual-test-cases.md](manual-test-cases.md)를 참고합니다.

Swagger UI 실행: `docker-compose up --build` 후 `http://localhost:8080/swagger-ui.html`
