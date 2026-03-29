-- 문제 1: 객관식 (정답: choice 3)
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

-- 문제 1 통계 (사전 존재, 30/20)
INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (1, 30, 20);

-- user 10: 문제 2만 풀었음 → chapter 1 후보 = [1]
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (10, 2, 'CORRECT');

-- user 20: 문제 1, 2 모두 풀었음 → 후보 없음
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (20, 1, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (20, 2, 'CORRECT');

-- user 30: 문제 1을 choice 3으로 제출 (객관식 제출 조회 테스트용)
INSERT INTO problem_submissions (user_id, problem_id, answer_status)
VALUES (30, 1, 'CORRECT');
INSERT INTO submission_choices (submission_id, choice_number)
SELECT id, 3 FROM problem_submissions WHERE user_id = 30 AND problem_id = 1;

-- user 40: 문제 2를 '서울'로 제출 (주관식 제출 조회 테스트용)
INSERT INTO problem_submissions (user_id, problem_id, answer_status, submitted_subjective_answer)
VALUES (40, 2, 'CORRECT', '서울');
