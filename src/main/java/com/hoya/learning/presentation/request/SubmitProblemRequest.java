package com.hoya.learning.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "문제 제출 요청")
public record SubmitProblemRequest(
        @Size(min = 1, message = "선택지는 1개 이상이어야 합니다.")
        @Schema(description = "선택한 보기 번호 목록 (객관식)", example = "[1, 3]")
        List<Integer> choiceNumbers,

        @Schema(description = "주관식 답안 (주관식)", example = "Java")
        String subjectiveAnswer
) {
    @AssertTrue(message = "choiceNumbers 또는 subjectiveAnswer 중 하나는 필수입니다.")
    @Schema(hidden = true)
    public boolean isAnswerPresent() {
        return choiceNumbers != null || subjectiveAnswer != null;
    }
}
