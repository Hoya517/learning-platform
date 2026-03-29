-- 문제 1: 객관식 (정답: 3번 default)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (1, 1, 'Java의 접근 제어자가 아닌 것은?', 'default는 접근 제어자가 아닙니다.', 'MULTIPLE_CHOICE', false);

-- 문제 2: 주관식 (정답: 서울)
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (2, 1, '대한민국의 수도는?', '대한민국의 수도는 서울입니다.', 'SUBJECTIVE', false);

-- 문제 1 선택지
INSERT INTO choices (problem_id, number, content, correct) VALUES
(1, 1, 'public', false),
(1, 2, 'protected', false),
(1, 3, 'default', true),
(1, 4, 'private', false),
(1, 5, 'package', false);

-- 문제 2 주관식 정답
INSERT INTO subjective_answers (problem_id, content) VALUES (2, '서울');

-- 문제 3: 객관식 복수정답 (정답: 1번+3번) — 부분정답 테스트용
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (3, 1, '컴파일 에러가 발생하는 것을 모두 고르시오.', '해설', 'MULTIPLE_CHOICE', true);

INSERT INTO choices (problem_id, number, content, correct) VALUES
(3, 1, '미사용 지역 변수', true),
(3, 2, 'NullPointerException 미처리', false),
(3, 3, '타입 불일치 대입', true),
(3, 4, '배열 인덱스 초과', false);

-- 정답률 통계: 문제 1은 30명 (15/30 = 50%)
INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (1, 30, 15);

-- user 99: 문제 1 이미 제출 (중복 제출 방지 테스트용)
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (99, 1, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3 FROM problem_submissions WHERE user_id = 99 AND problem_id = 1;

-- user 88: 문제 1 이미 제출 (풀이 상세 조회 테스트용)
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (88, 1, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3 FROM problem_submissions WHERE user_id = 88 AND problem_id = 1;

-- user 77: 문제 2를 '서울'로 제출 (주관식 풀이 상세 조회 테스트용)
INSERT INTO problem_submissions (user_id, problem_id, answer_status, submitted_subjective_answer)
VALUES (77, 2, 'CORRECT', '서울');
