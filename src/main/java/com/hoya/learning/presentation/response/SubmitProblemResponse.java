package com.hoya.learning.presentation.response;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemAnswer;
import com.hoya.learning.service.result.SubmissionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문제 제출 응답")
public record SubmitProblemResponse(
        @Schema(description = "문제 ID", example = "1")
        Long problemId,

        @Schema(description = "채점 결과 (CORRECT | PARTIAL | WRONG)")
        AnswerStatus answerStatus,

        @Schema(description = "해설", example = "final 키워드를 사용하면 재할당이 불가능합니다.")
        String explanation,

        @Schema(description = "정답 정보")
        AnswerInfo answer
) {
    @Schema(description = "정답 정보")
    public record AnswerInfo(
            @Schema(description = "정답 보기 번호 목록 (객관식)", example = "[1, 3]")
            List<Integer> correctChoiceNumbers,

            @Schema(description = "정답 텍스트 목록 (주관식)", example = "[\"final\", \"FINAL\"]")
            List<String> correctSubjectiveAnswers
    ) {}

    public static SubmitProblemResponse from(SubmissionResult result) {
        ProblemAnswer answer = result.answer();
        return new SubmitProblemResponse(
                result.problemId(),
                result.answerStatus(),
                result.explanation(),
                new AnswerInfo(answer.correctChoiceNumbers(), answer.correctSubjectiveAnswers())
        );
    }
}
