package com.hoya.learning.presentation.response;

import com.hoya.learning.domain.ProblemType;
import com.hoya.learning.service.result.RandomProblem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "랜덤 문제 조회 응답")
public record RandomProblemResponse(
        @Schema(description = "문제 ID", example = "42")
        Long problemId,

        @Schema(description = "문제 내용", example = "Java에서 불변 객체를 만드는 방법은?")
        String content,

        @Schema(description = "문제 유형 (MULTIPLE_CHOICE | SUBJECTIVE)")
        ProblemType type,

        @Schema(description = "복수 정답 여부", example = "false")
        boolean multipleAnswer,

        @Schema(description = "보기 목록 (객관식일 때만 존재)")
        List<ChoiceInfo> choices,

        @Schema(description = "문제 평균 정답률 (30명 미만 풀이 시 null)", example = "72.5")
        Double answerCorrectRate
) {
    @Schema(description = "보기 정보")
    public record ChoiceInfo(
            @Schema(description = "보기 번호", example = "1") int number,
            @Schema(description = "보기 내용", example = "final 키워드 사용") String content
    ) {}

    public static RandomProblemResponse from(RandomProblem result) {
        List<ChoiceInfo> choices = result.choices().stream()
                .map(c -> new ChoiceInfo(c.number(), c.content()))
                .toList();
        return new RandomProblemResponse(
                result.problemId(),
                result.content(),
                result.type(),
                result.multipleAnswer(),
                choices,
                result.answerCorrectRate()
        );
    }
}
