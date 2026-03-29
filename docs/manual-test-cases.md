# 수동 테스트 케이스 (Swagger)

Docker 최초 기동 시 `init.sql`이 자동으로 실행되어 스키마와 초기 데이터가 세팅됩니다.
데이터를 초기화하려면 볼륨을 삭제 후 재기동합니다:

```bash
docker-compose down -v && docker-compose up --build
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 사전 세팅된 데이터 요약

| userId | 상태 | 용도 |
|--------|------|------|
| 1 | chapter 1에서 problem 4, 5만 풀었음 | 랜덤 조회 / 스킵 테스트 |
| 2 | chapter 2의 problem 6만 풀었음 | 모든 문제 소진 → 404 |
| 3 | problem 1 이미 제출 | 중복 제출 → 409 |
| 4 | problem 1 제출 (3번 선택, 정답) | 풀이 상세 조회 (객관식) |
| 5 | problem 3 제출 (주관식 "서울", 정답) | 풀이 상세 조회 (주관식) |
| 6 | 제출 이력 없음 | 풀지 않은 문제 조회 → 404 |
| 10 | chapter 1에서 problem 2,3,4,5 풀었음 (problem 1만 남음) | 스킵 후 후보 없음 → 404 |
| 100 | chapter 3 전용 (problem 11~17 미제출) | 제출 시나리오 순서대로 |

| problemId | chapter | 설명 | 정답 | 통계 |
|-----------|---------|------|------|------|
| 1 | 1 | 객관식 단답 (접근 제어자) | 3번 | 30명, 정답률 80% |
| 2 | 1 | 객관식 복수정답 (컴파일 에러) | 1번+3번 | 10명 (30명 미만) |
| 3 | 1 | 주관식 (수도) | 서울 | 없음 |
| 4 | 1 | 객관식 (HTTP 404) | 2번 | 없음 |
| 5 | 1 | 객관식 (HTTP 메서드) | 3번 | 없음 |
| 6 | 2 | 객관식 (OOP 4대 특성) | 2번 | 없음 |
| 11 | 3 | 객관식 (JVM 역할) | 1번 | 30명, 정답률 73% |
| 12 | 3 | 객관식 (정수 타입) | 1번 | 없음 |
| 13 | 3 | 객관식 복수정답 (컬렉션) | 1번+3번 | 없음 |
| 14 | 3 | 주관식 (인터페이스) | 인터페이스 | 없음 |
| 15 | 3 | 주관식 (interface 소문자) | interface | 없음 |
| 16 | 3 | 주관식 (JVM 약자) | JVM | 없음 |
| 17 | 3 | 주관식 (컴파일러) | 컴파일러 | 없음 |
| 18 | 3 | 객관식 단답 (접근 제어자) — 타입 불일치 ① 전용 | 1번 | 없음 |
| 19 | 3 | 주관식 (최상위 클래스) — 타입 불일치 ② 전용 | Object | 없음 |
| 20 | 3 | 객관식 단답 (최상위 클래스) — 통계 연속 갱신 전용 | 1번 | 30명, 정답률 67% |

---

## 1. 랜덤 문제 조회

`GET /api/chapters/{chapterId}/problems/random`

### 1-1. 정상 조회 — 안 푼 문제 반환

- `X-User-Id: 1`, `GET /api/chapters/1/problems/random`
- 기대: problem 1, 2, 3 중 하나 반환 (problem 4, 5는 이미 풀었음)

### 1-2. 정답률 있음 (30명 이상)

- `X-User-Id: 1`, `GET /api/chapters/1/problems/random`
- problem 1이 반환된 경우: `answerCorrectRate: 80.0`

### 1-3. 정답률 없음 (30명 미만)

- `X-User-Id: 1`, `GET /api/chapters/1/problems/random`
- problem 2 또는 3이 반환된 경우: `answerCorrectRate: null`

> 1-2, 1-3은 랜덤이므로 여러 번 호출하거나 스킵 파라미터를 활용해 원하는 문제를 유도합니다.
> ex) problem 1만 남기려면 `X-User-Id: 10` 사용

### 1-4. 모든 문제 풀었음 → 404

- `X-User-Id: 2`, `GET /api/chapters/2/problems/random`
- 기대: `404 NO_AVAILABLE_PROBLEM`

### 1-5. 스킵 직후 조회 — 해당 문제 제외

- `X-User-Id: 1`, `GET /api/chapters/1/problems/random?excludeProblemId=1`
- 기대: problem 2 또는 3 반환 (problem 1 제외)

### 1-6. 스킵 후 다시 조회 — 스킵 문제 복귀

- `X-User-Id: 1`, `GET /api/chapters/1/problems/random` (excludeProblemId 없음)
- 기대: problem 1, 2, 3 모두 후보 (이전 스킵 상태 없음, stateless)

### 1-7. 스킵 후 후보 없음 → 404

- `X-User-Id: 10`, `GET /api/chapters/1/problems/random?excludeProblemId=1`
- 기대: `404 NO_AVAILABLE_PROBLEM` (남은 후보 problem 1을 스킵했으므로)

### 1-8. 존재하지 않는 chapterId → 404

- `X-User-Id: 1`, `GET /api/chapters/999/problems/random`
- 기대: `404 NO_AVAILABLE_PROBLEM`

---

## 2. 문제 제출

`POST /api/problems/{problemId}/submissions`

> **주의:** user+problem 조합은 1회만 제출 가능합니다.
> chapter 3 전용 문제(11~17)와 `X-User-Id: 100`을 사용하면 충돌 없이 순서대로 테스트할 수 있습니다.

### 2-1. 객관식 정답 + 통계 갱신 확인

- `X-User-Id: 100`, `POST /api/problems/11/submissions`
- Body: `{ "choiceNumbers": [1] }`
- 기대: `answerStatus: CORRECT`, `explanation` 포함, `answer.choiceNumbers: [1]`
- 제출 후 `GET /api/problems/11/submission` (`X-User-Id: 100`) → `answerCorrectRate: 74.0`
  - 갱신 전 73.0% (30명 중 22명) → 갱신 후 74.0% (31명 중 23명)

### 2-2. 객관식 오답

- `X-User-Id: 100`, `POST /api/problems/12/submissions`
- Body: `{ "choiceNumbers": [2] }` (정답은 1번)
- 기대: `answerStatus: WRONG`

### 2-3. 객관식 부분정답

- `X-User-Id: 100`, `POST /api/problems/13/submissions`
- Body: `{ "choiceNumbers": [1] }` (정답은 1번+3번)
- 기대: `answerStatus: PARTIAL`

### 2-4. 주관식 정답

- `X-User-Id: 100`, `POST /api/problems/14/submissions`
- Body: `{ "subjectiveAnswer": "인터페이스" }`
- 기대: `answerStatus: CORRECT`

### 2-5. 주관식 대소문자 무시

- `X-User-Id: 100`, `POST /api/problems/15/submissions`
- Body: `{ "subjectiveAnswer": "INTERFACE" }` (정답은 "interface")
- 기대: `answerStatus: CORRECT`

### 2-6. 주관식 앞뒤 공백 무시

- `X-User-Id: 100`, `POST /api/problems/16/submissions`
- Body: `{ "subjectiveAnswer": "  JVM  " }` (정답은 "JVM")
- 기대: `answerStatus: CORRECT`

### 2-7. 주관식 오답

- `X-User-Id: 100`, `POST /api/problems/17/submissions`
- Body: `{ "subjectiveAnswer": "인터프리터" }` (정답은 "컴파일러")
- 기대: `answerStatus: WRONG`

### 2-8. 중복 제출 → 409

- `X-User-Id: 3`, `POST /api/problems/1/submissions`
- Body: `{ "choiceNumbers": [3] }`
- 기대: `409 ALREADY_SUBMITTED_PROBLEM`

### 2-9. 타입 불일치 ③ — 단일 정답 문제에 복수 선택지 제출 → 400

- `X-User-Id: 100`, `POST /api/problems/18/submissions`
- Body: `{ "choiceNumbers": [1, 2] }` (단일 정답 문제에 복수 제출)
- 기대: `400 INVALID_ANSWER_TYPE`
- 참고: 400 에러는 저장되지 않으므로 몇 번이든 재시도 가능

### 2-11. 타입 불일치 ① — 객관식 문제에 주관식 답안 → 400

- `X-User-Id: 100`, `POST /api/problems/18/submissions`
- Body: `{ "subjectiveAnswer": "public" }`
- 기대: `400 INVALID_ANSWER_TYPE`
- 참고: 400 에러는 저장되지 않으므로 몇 번이든 재시도 가능

### 2-12. 타입 불일치 ② — 주관식 문제에 객관식 답안 → 400

- `X-User-Id: 100`, `POST /api/problems/19/submissions`
- Body: `{ "choiceNumbers": [1] }`
- 기대: `400 INVALID_ANSWER_TYPE`
- 참고: 400 에러는 저장되지 않으므로 몇 번이든 재시도 가능

### 2-13. 존재하지 않는 problemId → 404

- `X-User-Id: 100`, `POST /api/problems/999/submissions`
- Body: `{ "choiceNumbers": [1] }`
- 기대: `404 PROBLEM_NOT_FOUND`

### 2-14. 통계 연속 갱신 확인

`POST /api/problems/20/submissions` + `GET /api/problems/20/submission`

사전 통계: **30명 중 20명 정답 → 67.0%**
정답: `{ "choiceNumbers": [1] }` / 오답: `{ "choiceNumbers": [2] }`

| 순서 | X-User-Id | Body | answerStatus | 누적 (total / correct) | 기대 answerCorrectRate |
|------|-----------|------|--------------|------------------------|------------------------|
| 1 | 201 | `{choiceNumbers:[1]}` | CORRECT | 31 / 21 | 68.0 |
| 2 | 202 | `{choiceNumbers:[2]}` | WRONG   | 32 / 21 | 66.0 |
| 3 | 203 | `{choiceNumbers:[1]}` | CORRECT | 33 / 22 | 67.0 |
| 4 | 204 | `{choiceNumbers:[2]}` | WRONG   | 34 / 22 | 65.0 |
| 5 | 205 | `{choiceNumbers:[1]}` | CORRECT | 35 / 23 | 66.0 |
| 6 | 206 | `{choiceNumbers:[2]}` | WRONG   | 36 / 23 | 64.0 |

제출 후 확인 방법: `GET /api/problems/20/submission` (`X-User-Id`: 방금 제출한 userId)

---

## 3. 풀이 상세 조회

`GET /api/problems/{problemId}/submission`

### 3-1. 정상 조회 (객관식)

- `X-User-Id: 4`, `GET /api/problems/1/submission`
- 기대:
  - `answerStatus: CORRECT`
  - `userAnswers: [3]`
  - `answer.choiceNumbers: [3]`
  - `answerCorrectRate: 80.0`
  - `explanation` 포함

### 3-2. 정상 조회 (주관식)

- `X-User-Id: 5`, `GET /api/problems/3/submission`
- 기대:
  - `answerStatus: CORRECT`
  - `userAnswers: ["서울"]`
  - `answer.subjectiveAnswers: ["서울"]`
  - `answerCorrectRate: null` (통계 없음)

### 3-3. 풀지 않은 문제 → 404

- `X-User-Id: 6`, `GET /api/problems/1/submission`
- 기대: `404 SUBMISSION_NOT_FOUND`
