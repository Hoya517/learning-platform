package com.hoya.learning.service.command;

public record GetRandomProblemCommand(
        Long chapterId,
        Long userId,
        Long excludeProblemId
) {
}
