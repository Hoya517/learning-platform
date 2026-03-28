package com.hoya.learning.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    CHAPTER_NOT_FOUND("단원을 찾을 수 없습니다."),
    PROBLEM_NOT_FOUND("문제를 찾을 수 없습니다."),
    SUBMISSION_NOT_FOUND("풀이 이력을 찾을 수 없습니다."),
    NO_AVAILABLE_PROBLEM("더 이상 제공할 문제가 없습니다."),
    ALREADY_SUBMITTED_PROBLEM("이미 제출한 문제입니다."),
    INVALID_ANSWER_TYPE("문제 유형과 답안 유형이 일치하지 않습니다."),
    UNSUPPORTED_PROBLEM_TYPE("지원하지 않는 문제 유형입니다.");

    private final String message;
}
