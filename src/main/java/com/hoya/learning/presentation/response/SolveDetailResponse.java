package com.hoya.learning.presentation.response;

import com.hoya.learning.domain.AnswerStatus;
import com.hoya.learning.domain.ProblemAnswer;
import com.hoya.learning.service.result.ProblemSolveDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "풀이 상세 조회 응답")
public record SolveDetailResponse(
        @Schema(description = "문제 ID", example = "1")
        Long problemId,

        @Schema(description = "채점 결과 (CORRECT | PARTIAL | WRONG)")
        AnswerStatus answerStatus,

        @Schema(description = "해설", example = "final 키워드를 사용하면 재할당이 불가능합니다.")
        String explanation,

        @Schema(description = "정답 정보")
        AnswerInfo answer,

        @Schema(description = "제출한 보기 번호 목록 (객관식)", example = "[1]")
        List<Integer> submittedChoiceNumbers,

        @Schema(description = "제출한 주관식 답안", example = "final")
        String submittedSubjectiveAnswer,

        @Schema(description = "문제 평균 정답률 (30명 미만 풀이 시 null)", example = "72.5")
        Double answerCorrectRate
) {
    @Schema(description = "정답 정보")
    public record AnswerInfo(
            @Schema(description = "정답 보기 번호 목록 (객관식)", example = "[1, 3]")
            List<Integer> correctChoiceNumbers,

            @Schema(description = "정답 텍스트 목록 (주관식)", example = "[\"final\", \"FINAL\"]")
            List<String> correctSubjectiveAnswers
    ) {}

    public static SolveDetailResponse from(ProblemSolveDetail result) {
        ProblemAnswer answer = result.answer();
        return new SolveDetailResponse(
                result.problemId(),
                result.answerStatus(),
                result.explanation(),
                new AnswerInfo(answer.correctChoiceNumbers(), answer.correctSubjectiveAnswers()),
                result.submittedChoiceNumbers(),
                result.submittedSubjectiveAnswer(),
                result.answerCorrectRate()
        );
    }
}
