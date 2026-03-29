-- ============================================================
-- MySQL 초기화 스크립트
-- Docker 최초 기동 시 자동 실행됩니다.
-- 재실행하려면: docker-compose down -v && docker-compose up
-- ============================================================

-- Schema

SET NAMES utf8mb4;
ALTER DATABASE learning_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS problems
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id      BIGINT       NOT NULL,
    content         TEXT         NOT NULL,
    explanation     TEXT,
    type            VARCHAR(50)  NOT NULL,
    multiple_answer BOOLEAN      NOT NULL,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    INDEX idx_problem_chapter (chapter_id)
);

CREATE TABLE IF NOT EXISTS choices
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT      NOT NULL,
    number     INT         NOT NULL,
    content    TEXT        NOT NULL,
    correct    BOOLEAN     NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_choice_problem (problem_id)
);

CREATE TABLE IF NOT EXISTS subjective_answers
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT       NOT NULL,
    content    VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_subjective_problem (problem_id)
);

CREATE TABLE IF NOT EXISTS problem_statistics
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id               BIGINT NOT NULL,
    total_solved_user_count  INT    NOT NULL,
    correct_solved_user_count INT   NOT NULL,
    created_at               DATETIME(6),
    updated_at               DATETIME(6),
    UNIQUE KEY uk_problem_statistic_problem_id (problem_id)
);

CREATE TABLE IF NOT EXISTS problem_submissions
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                     BIGINT      NOT NULL,
    problem_id                  BIGINT      NOT NULL,
    answer_status               VARCHAR(20) NOT NULL,
    submitted_subjective_answer VARCHAR(255),
    created_at                  DATETIME(6),
    updated_at                  DATETIME(6),
    UNIQUE INDEX uk_submission_user_problem (user_id, problem_id)
);

CREATE TABLE IF NOT EXISTS submission_choices
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    choice_number INT    NOT NULL,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    INDEX idx_submission_choice_submission (submission_id)
);


-- ============================================================
-- 수동 테스트용 초기 데이터 (Swagger 테스트 체크리스트 기준)
--
-- [테스트 계정 요약]
--   user 1   : chapter 1에서 problem 4,5만 풀었음 → 랜덤 조회 / 스킵 테스트용
--   user 2   : chapter 2의 유일한 문제를 풀었음 → 모든 문제 풀었을 때 404 테스트용
--   user 3   : problem 1을 이미 제출 → 중복 제출 409 테스트용
--   user 4   : problem 1 제출 (객관식 3번 정답) → 풀이 상세 조회 (객관식)
--   user 5   : problem 3 제출 (주관식 "서울" 정답) → 풀이 상세 조회 (주관식)
--   user 6   : 제출 이력 없음 → 풀지 않은 문제 조회 404 테스트용
--   user 10  : chapter 1에서 problem 1만 남음 → excludeProblemId=1 시 후보 없음 404
--   user 100 : chapter 3 제출 시나리오 전용 (problem 11~17, 각 1회씩)
-- ============================================================


-- ============================================================
-- Chapter 1 (chapter_id=1)
-- ============================================================

-- [Problem 1] 객관식 단답 (정답: 3번 'default') | 통계 30명, 정답률 80%
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (1, 1, 'Java의 접근 제어자가 아닌 것은?',
        'default는 접근 제어자 키워드가 아니라, 접근 제어자를 명시하지 않은 상태(패키지 접근)를 의미합니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (1, 1, 'public', false),
       (1, 2, 'protected', false),
       (1, 3, 'default', true),
       (1, 4, 'private', false);

INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (1, 30, 24);


-- [Problem 2] 객관식 복수정답 (정답: 1번+3번) | 통계 10명 (30명 미만 → null)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (2, 1, '컴파일 에러가 발생하는 경우를 모두 고르시오.',
        '선언만 하고 사용하지 않는 지역 변수와 호환되지 않는 타입 간 직접 대입은 컴파일 에러입니다. NPE와 배열 인덱스 초과는 런타임 에러입니다.',
        'MULTIPLE_CHOICE', true);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (2, 1, '선언만 하고 사용하지 않는 지역 변수', true),
       (2, 2, 'NullPointerException 미처리', false),
       (2, 3, '호환되지 않는 타입 간 직접 대입', true),
       (2, 4, '배열 인덱스 초과 접근', false);

INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (2, 10, 4);


-- [Problem 3] 주관식 (정답: "서울")
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (3, 1, '대한민국의 수도는?', '대한민국의 수도는 서울입니다.', 'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (3, '서울');


-- [Problem 4] 객관식 (정답: 2번) — user 1 이미 제출
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (4, 1, 'HTTP 상태 코드 404의 의미는?',
        '404 Not Found: 요청한 리소스를 서버에서 찾을 수 없음을 의미합니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (4, 1, '서버 내부 오류', false),
       (4, 2, '리소스를 찾을 수 없음', true),
       (4, 3, '요청 형식이 잘못됨', false),
       (4, 4, '인증이 필요함', false);


-- [Problem 5] 객관식 (정답: 3번) — user 1 이미 제출
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (5, 1, 'REST API에서 새로운 리소스 생성에 사용하는 HTTP 메서드는?',
        'POST 메서드는 새로운 리소스를 생성할 때 사용합니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (5, 1, 'GET', false),
       (5, 2, 'DELETE', false),
       (5, 3, 'POST', true),
       (5, 4, 'PATCH', false);


-- ============================================================
-- Chapter 2 (chapter_id=2)
-- ============================================================

-- [Problem 6] 객관식 (정답: 2번) — user 2 이미 제출
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (6, 2, '객체지향 프로그래밍의 4대 특성이 아닌 것은?',
        '캡슐화, 상속, 다형성, 추상화가 4대 특성입니다. 동시성은 포함되지 않습니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (6, 1, '캡슐화', false),
       (6, 2, '동시성', true),
       (6, 3, '다형성', false),
       (6, 4, '추상화', false);


-- ============================================================
-- Chapter 3 (chapter_id=3)
-- 제출 시나리오 전용 — X-User-Id: 100 으로 순서대로 테스트
-- ============================================================

-- [Problem 11] 객관식 정답 + 통계 갱신 (정답: 1번) | 사전 통계 73.0% (22/30)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (11, 3, 'JVM의 역할로 옳은 것은?',
        'JVM(Java Virtual Machine)은 바이트코드를 OS에 맞게 해석·실행합니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (11, 1, '바이트코드를 OS에 맞게 해석·실행한다', true),
       (11, 2, '소스 코드를 바이트코드로 컴파일한다', false),
       (11, 3, '클래스 파일의 문법을 검사한다', false),
       (11, 4, '네트워크 통신을 추상화한다', false);

INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (11, 30, 22);


-- [Problem 12] 객관식 오답 전용 (정답: 1번 / 오답 제출: 2번)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (12, 3, 'Java에서 정수 기본 타입인 것은?',
        'int는 Java의 정수 기본(primitive) 타입입니다. Integer는 래퍼 클래스입니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (12, 1, 'int', true),
       (12, 2, 'Integer', false),
       (12, 3, 'Number', false),
       (12, 4, 'Long', false);


-- [Problem 13] 객관식 부분정답 전용 (정답: 1번+3번)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (13, 3, 'Java 컬렉션 프레임워크에서 중복을 허용하지 않는 자료구조를 모두 고르시오.',
        'Set 계열(HashSet, TreeSet)은 중복을 허용하지 않습니다.',
        'MULTIPLE_CHOICE', true);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (13, 1, 'HashSet', true),
       (13, 2, 'ArrayList', false),
       (13, 3, 'TreeSet', true),
       (13, 4, 'LinkedList', false);


-- [Problem 14] 주관식 정답 전용 (정답: "인터페이스")
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (14, 3, 'Java에서 다중 구현을 지원하며, 메서드 시그니처만 정의하는 타입을 무엇이라 하는가?',
        '인터페이스(interface)는 다중 구현을 지원하고 메서드 시그니처를 정의합니다.',
        'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (14, '인터페이스');


-- [Problem 15] 주관식 대소문자 무시 전용 (정답: "interface")
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (15, 3, 'Java의 interface 키워드를 영문 소문자로 쓰시오.',
        '정답은 interface 입니다. 대소문자는 무시합니다.',
        'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (15, 'interface');


-- [Problem 16] 주관식 앞뒤 공백 무시 전용 (정답: "JVM")
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (16, 3, 'Java 바이트코드를 실행하는 가상 머신의 약자를 쓰시오.',
        '정답은 JVM(Java Virtual Machine)입니다.',
        'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (16, 'JVM');


-- [Problem 17] 주관식 오답 전용 (정답: "컴파일러")
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (17, 3, 'Java 소스 파일(.java)을 바이트코드(.class)로 변환하는 도구는?',
        'javac 컴파일러가 소스 파일을 바이트코드로 변환합니다.',
        'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (17, '컴파일러');


-- [Problem 18] 타입 불일치 전용 ① — 객관식 문제에 주관식 답안 제출 시 400
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (18, 3, 'Java에서 어디서든 접근 가능한 접근 제어자는?',
        'public은 모든 패키지에서 접근 가능한 접근 제어자입니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (18, 1, 'public', true),
       (18, 2, 'protected', false),
       (18, 3, 'default', false),
       (18, 4, 'private', false);


-- [Problem 19] 타입 불일치 전용 ② — 주관식 문제에 객관식 답안 제출 시 400
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (19, 3, 'Java 최상위 클래스의 이름을 쓰시오.',
        '모든 Java 클래스는 Object 클래스를 암묵적으로 상속합니다.',
        'SUBJECTIVE', false);

INSERT INTO subjective_answers (problem_id, content)
VALUES (19, 'Object');


-- [Problem 20] 통계 연속 갱신 전용 (정답: 1번) | 사전 통계 67.0% (20/30)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (20, 3, 'Java에서 모든 클래스의 최상위 부모 클래스는?',
        '모든 Java 클래스는 Object 클래스를 암묵적으로 상속합니다.',
        'MULTIPLE_CHOICE', false);

INSERT INTO choices (problem_id, number, content, correct)
VALUES (20, 1, 'Object', true),
       (20, 2, 'Class', false),
       (20, 3, 'Super', false),
       (20, 4, 'Base', false);

INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (20, 30, 20);


-- ============================================================
-- 제출 이력 (사전 제출된 케이스)
-- ============================================================

-- [user 1] problem 4, 5 제출 → problem 1,2,3이 후보
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (1, 4, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 2
FROM problem_submissions
WHERE user_id = 1
  AND problem_id = 4;

INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (1, 5, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3
FROM problem_submissions
WHERE user_id = 1
  AND problem_id = 5;


-- [user 2] problem 6 제출 → chapter 2 후보 없음 → 404
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (2, 6, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 2
FROM problem_submissions
WHERE user_id = 2
  AND problem_id = 6;


-- [user 3] problem 1 제출 → 중복 제출 409 테스트용
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (3, 1, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3
FROM problem_submissions
WHERE user_id = 3
  AND problem_id = 1;


-- [user 4] problem 1 제출 (3번 선택, 정답) → 풀이 상세 조회 (객관식)
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (4, 1, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3
FROM problem_submissions
WHERE user_id = 4
  AND problem_id = 1;


-- [user 5] problem 3 제출 (주관식 "서울", 정답) → 풀이 상세 조회 (주관식)
INSERT INTO problem_submissions (user_id, problem_id, answer_status, submitted_subjective_answer)
VALUES (5, 3, 'CORRECT', '서울');


-- [user 10] problem 2,3,4,5 제출 → problem 1만 후보 → excludeProblemId=1 시 404
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (10, 2, 'WRONG');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 2
FROM problem_submissions
WHERE user_id = 10
  AND problem_id = 2;

INSERT INTO problem_submissions (user_id, problem_id, answer_status, submitted_subjective_answer)
VALUES (10, 3, 'WRONG', '부산');

INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (10, 4, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 2
FROM problem_submissions
WHERE user_id = 10
  AND problem_id = 4;

INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (10, 5, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3
FROM problem_submissions
WHERE user_id = 10
  AND problem_id = 5;
