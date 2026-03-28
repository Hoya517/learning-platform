package com.hoya.learning.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문제 제출 요청")
public record SubmitProblemRequest(
        @Schema(description = "선택한 보기 번호 목록 (객관식)", example = "[1, 3]")
        List<Integer> choiceNumbers,

        @Schema(description = "주관식 답안 (주관식)", example = "Java")
        String subjectiveAnswer
) {
}
