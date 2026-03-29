-- 단원 1에 문제 3개
INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (1, 1, 'Java의 접근 제어자가 아닌 것은?', 'default는 접근 제어자가 아닙니다.', 'MULTIPLE_CHOICE', false);

INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (2, 1, '자바에서 기본 타입이 아닌 것은?', 'String은 참조 타입입니다.', 'MULTIPLE_CHOICE', false);

INSERT INTO problems (id, chapter_id, content, explanation, type, multiple_answer)
VALUES (3, 1, '가장 시간복잡도가 높은 정렬은?', '버블 정렬의 최악 시간복잡도는 O(n²)입니다.', 'MULTIPLE_CHOICE', false);

-- 문제 1 선택지 (정답: 3번 default)
INSERT INTO choices (problem_id, number, content, correct) VALUES
(1, 1, 'public', false),
(1, 2, 'protected', false),
(1, 3, 'default', true),
(1, 4, 'private', false),
(1, 5, 'package', false);

-- 문제 2 선택지 (정답: 3번 String)
INSERT INTO choices (problem_id, number, content, correct) VALUES
(2, 1, 'int', false),
(2, 2, 'double', false),
(2, 3, 'String', true),
(2, 4, 'boolean', false),
(2, 5, 'char', false);

-- 문제 3 선택지 (정답: 2번 버블 정렬)
INSERT INTO choices (problem_id, number, content, correct) VALUES
(3, 1, '퀵 정렬', false),
(3, 2, '버블 정렬', true),
(3, 3, '병합 정렬', false),
(3, 4, '힙 정렬', false),
(3, 5, '삽입 정렬', false);

-- 정답률 통계: 문제 1은 30명 이상 (20/30 = 66.7% → 67%), 문제 3은 30명 미만 (null)
INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (1, 30, 20);
INSERT INTO problem_statistics (problem_id, total_solved_user_count, correct_solved_user_count)
VALUES (3, 10, 5);

-- user 11: 문제 2, 3 풀었음 → 후보 = [1] (정답률 67%)
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (11, 2, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (11, 3, 'CORRECT');

-- user 10: 문제 1, 2 풀었음 → 후보 = [3] (정답률 null, 10명 미만)
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (10, 1, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (10, 2, 'CORRECT');

-- user 20: 모두 풀었음 → 후보 없음
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (20, 1, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (20, 2, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (20, 3, 'CORRECT');

-- user 30: 문제 2만 풀었음 → 후보 = [1, 3], excludeProblemId=1 지정 시 → [3]
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (30, 2, 'CORRECT');

-- user 40: 문제 1, 2 풀었음 → 후보 = [3], excludeProblemId=3 지정 시 → 빈 목록
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (40, 1, 'CORRECT');
INSERT INTO problem_submissions (user_id, problem_id, answer_status) VALUES (40, 2, 'CORRECT');
