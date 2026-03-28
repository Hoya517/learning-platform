package com.hoya.learning.service.command;

public record GetSolveDetailCommand(
        Long userId,
        Long problemId
) {
}
