package com.hoya.learning.presentation;

import com.hoya.learning.common.response.ApiResponse;
import com.hoya.learning.presentation.response.RandomProblemResponse;
import com.hoya.learning.service.ProblemService;
import com.hoya.learning.service.command.GetRandomProblemCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Problem", description = "문제 조회 API")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/chapters/{chapterId}/problems")
public class ProblemController {

    private final ProblemService problemService;

    @Operation(summary = "랜덤 문제 조회", description = "단원에서 아직 풀지 않은 문제를 랜덤으로 반환합니다.")
    @GetMapping("/random")
    public ApiResponse<RandomProblemResponse> getRandomProblem(
            @Parameter(description = "단원 ID") @Positive(message = "chapterId는 양수여야 합니다.") @PathVariable Long chapterId,
            @Parameter(description = "사용자 ID", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER) @Positive(message = "userId는 양수여야 합니다.") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "제외할 문제 ID (스킵한 문제)") @Positive(message = "excludeProblemId는 양수여야 합니다.") @RequestParam(required = false) Long excludeProblemId
    ) {
        GetRandomProblemCommand command = new GetRandomProblemCommand(chapterId, userId, excludeProblemId);
        return ApiResponse.of(RandomProblemResponse.from(problemService.getRandomProblem(command)));
    }
}
