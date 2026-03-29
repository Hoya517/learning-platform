package com.hoya.learning.presentation;

import com.hoya.learning.common.response.ApiResponse;
import com.hoya.learning.presentation.request.SubmitProblemRequest;
import com.hoya.learning.presentation.response.SolveDetailResponse;
import com.hoya.learning.presentation.response.SubmitProblemResponse;
import com.hoya.learning.service.SubmissionService;
import com.hoya.learning.service.command.GetSolveDetailCommand;
import com.hoya.learning.service.command.SubmitProblemCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Submission", description = "문제 제출 및 풀이 이력 API")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "문제 제출", description = "문제의 답안을 제출하고 채점 결과를 반환합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/problems/{problemId}/submissions")
    public ApiResponse<SubmitProblemResponse> submit(
            @Parameter(description = "사용자 ID", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER) @Positive(message = "userId는 양수여야 합니다.") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "문제 ID") @Positive(message = "problemId는 양수여야 합니다.") @PathVariable Long problemId,
            @Valid @RequestBody SubmitProblemRequest request
    ) {
        SubmitProblemCommand command = new SubmitProblemCommand(
                userId, problemId, request.choiceNumbers(), request.subjectiveAnswer());
        return ApiResponse.of(SubmitProblemResponse.from(submissionService.submit(command)));
    }

    @Operation(summary = "풀이 상세 조회", description = "사용자의 특정 문제 풀이 이력을 조회합니다.")
    @GetMapping("/problems/{problemId}/submission")
    public ApiResponse<SolveDetailResponse> getSolveDetail(
            @Parameter(description = "사용자 ID", in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER) @Positive(message = "userId는 양수여야 합니다.") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "문제 ID") @Positive(message = "problemId는 양수여야 합니다.") @PathVariable Long problemId
    ) {
        GetSolveDetailCommand command = new GetSolveDetailCommand(userId, problemId);
        return ApiResponse.of(SolveDetailResponse.from(submissionService.getSolveDetail(command)));
    }
}
