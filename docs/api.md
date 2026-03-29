# API 명세

모든 API는 `X-User-Id` 헤더로 사용자 ID를 전달합니다.

| Method | URL | 상태코드 | 설명 |
|--------|-----|----------|------|
| GET | `/api/chapters/{chapterId}/problems/random` | 200 | 단원 내 랜덤 문제 조회 |
| POST | `/api/problems/{problemId}/submissions` | 201 | 문제 제출 |
| GET | `/api/problems/{problemId}/submission` | 200 | 풀이 상세 조회 |

전체 명세와 직접 실행은 Swagger UI(`/swagger-ui.html`)를 사용합니다.

---

## GET /api/chapters/{chapterId}/problems/random

사용자가 풀지 않은 문제 중 1개를 랜덤으로 반환합니다.

**Query Parameters**

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| `excludeProblemId` | 선택 | 직전에 스킵한 문제 ID. 클라이언트가 전달하는 stateless 방식 |

**Request**

```
GET /api/chapters/1/problems/random?excludeProblemId=5
X-User-Id: 1
```

**Response**

```json
{
  "data": {
    "problemId": 3,
    "content": "다음 중 HTTP 메서드가 아닌 것은?",
    "type": "MULTIPLE_CHOICE",
    "multipleAnswer": false,
    "choices": [
      { "number": 1, "content": "GET" },
      { "number": 2, "content": "POST" },
      { "number": 3, "content": "SEND" },
      { "number": 4, "content": "DELETE" }
    ],
    "answerCorrectRate": 72.5
  }
}
```

- `answerCorrectRate`: 30명 이상 푼 경우에만 반환, 미만이면 `null`
- 후보 문제가 없으면 `NO_AVAILABLE_PROBLEM` (404)

---

## POST /api/problems/{problemId}/submissions

문제 답안을 제출합니다. 제출 즉시 채점 결과와 해설을 반환합니다.

**Request — 객관식**

```
POST /api/problems/3/submissions
X-User-Id: 1
Content-Type: application/json

{ "choiceNumbers": [3] }
```

**Request — 주관식**

```
POST /api/problems/7/submissions
X-User-Id: 1
Content-Type: application/json

{ "subjectiveAnswer": "서울" }
```

**Response**

```json
{
  "data": {
    "answerStatus": "CORRECT",
    "explanation": "SEND는 HTTP 표준 메서드가 아닙니다.",
    "correctChoiceNumbers": [3],
    "correctSubjectiveAnswers": null
  }
}
```

- `answerStatus`: `CORRECT` / `PARTIAL` / `WRONG`
- 부분정답(PARTIAL): 정답 선택지를 1개라도 포함했지만 완전하지 않은 경우. 주관식은 해당 없음
- 주관식 정답 비교: 대소문자 무시, 앞뒤 공백 무시
- 문제 타입과 다른 필드를 함께 보내면 `INVALID_ANSWER_TYPE` (400) — 객관식에 `subjectiveAnswer`, 주관식에 `choiceNumbers` 모두 해당
- 동일 문제 재제출 시 `ALREADY_SUBMITTED_PROBLEM` (409)

---

## GET /api/problems/{problemId}/submission

이전에 제출한 풀이 내역을 조회합니다.

**Request**

```
GET /api/problems/3/submission
X-User-Id: 1
```

**Response**

```json
{
  "data": {
    "answerStatus": "CORRECT",
    "explanation": "SEND는 HTTP 표준 메서드가 아닙니다.",
    "submittedChoiceNumbers": [3],
    "submittedSubjectiveAnswer": null,
    "correctChoiceNumbers": [3],
    "correctSubjectiveAnswers": null,
    "answerCorrectRate": 72.5
  }
}
```

---

## 에러 코드

```json
{
  "code": "NO_AVAILABLE_PROBLEM",
  "message": "더 이상 제공할 문제가 없습니다."
}
```

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
